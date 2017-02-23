/**
 * Hub Rest Common
 *
 * Copyright (C) 2017 Black Duck Software, Inc.
 * http://www.blackducksoftware.com/
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package com.blackducksoftware.integration.hub.rest;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;
import java.net.URLConnection;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

import com.blackducksoftware.integration.exception.IntegrationException;
import com.blackducksoftware.integration.hub.rest.exception.IntegrationRestException;
import com.blackducksoftware.integration.log.IntLogger;
import com.blackducksoftware.integration.log.LogLevel;
import com.blackducksoftware.integration.util.proxy.ProxyUtil;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParser;

import okhttp3.FormBody;
import okhttp3.Headers;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * The parent class of all Hub connections.
 */
public abstract class RestConnection {
    public static final String JSON_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSSX";

    private final Gson gson = new GsonBuilder().setDateFormat(JSON_DATE_FORMAT).create();

    private final JsonParser jsonParser = new JsonParser();

    private final OkHttpClient.Builder builder = new OkHttpClient.Builder();

    private final URL hubBaseUrl;

    private int timeout = 120;

    private String proxyHost;

    private int proxyPort;

    private String proxyNoHosts;

    private String proxyUsername;

    private String proxyPassword;

    private OkHttpClient client;

    private IntLogger logger;

    public static Date parseDateString(final String dateString) throws ParseException {
        final SimpleDateFormat sdf = new SimpleDateFormat(JSON_DATE_FORMAT);
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        return sdf.parse(dateString);
    }

    public static String formatDate(final Date date) {
        final SimpleDateFormat sdf = new SimpleDateFormat(JSON_DATE_FORMAT);
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        return sdf.format(date);
    }

    public RestConnection(final IntLogger logger, final URL hubBaseUrl, final int timeout) {
        this.logger = logger;
        this.hubBaseUrl = hubBaseUrl;
        setTimeout(timeout);
    }

    public void connect() throws IntegrationException {
        addBuilderConnectionTimes();
        addBuilderProxyInformation();
        addBuilderAuthentication();
        setClient(getBuilder().build());
        clientAuthenticate();
    }

    public abstract void addBuilderAuthentication() throws IntegrationException;

    public abstract void clientAuthenticate() throws IntegrationException;

    public HttpUrl createHttpUrl() {
        return HttpUrl.get(getHubBaseUrl()).newBuilder().build();
    }

    public HttpUrl createHttpUrl(final URL providedUrl) {
        final HttpUrl.Builder urlBuilder = HttpUrl.get(providedUrl).newBuilder();
        return urlBuilder.build();
    }

    public HttpUrl createHttpUrl(final String providedUrl) {
        final HttpUrl.Builder urlBuilder = HttpUrl.parse(providedUrl).newBuilder();
        return urlBuilder.build();
    }

    public HttpUrl createHttpUrl(final List<String> urlSegments) {
        return createHttpUrl(urlSegments, null);
    }

    public HttpUrl createHttpUrl(final List<String> urlSegments,
            final Map<String, String> queryParameters) {
        return createHttpUrl(getHubBaseUrl().toString(), urlSegments, queryParameters);
    }

    public HttpUrl createHttpUrl(final String providedUrl, final List<String> urlSegments,
            final Map<String, String> queryParameters) {
        final HttpUrl.Builder urlBuilder = HttpUrl.parse(providedUrl).newBuilder();
        if (urlSegments != null) {
            for (final String urlSegment : urlSegments) {
                urlBuilder.addPathSegment(urlSegment);
            }
        }
        if (queryParameters != null) {
            for (final Entry<String, String> queryParameter : queryParameters.entrySet()) {
                urlBuilder.addQueryParameter(queryParameter.getKey(), queryParameter.getValue());
            }
        }
        return urlBuilder.build();
    }

    private void addBuilderConnectionTimes() throws IntegrationRestException {
        builder.connectTimeout(timeout, TimeUnit.SECONDS);
        builder.writeTimeout(timeout, TimeUnit.SECONDS);
        builder.readTimeout(timeout, TimeUnit.SECONDS);
    }

    private void addBuilderProxyInformation() throws IntegrationRestException {
        if (shouldUseProxyForUrl(getHubBaseUrl())) {
            builder.proxy(getProxy(getHubBaseUrl()));
            builder.proxyAuthenticator(
                    new com.blackducksoftware.integration.hub.proxy.OkAuthenticator(getProxyUsername(),
                            getProxyPassword()));
        }
    }

    public URLConnection openConnection(final URL url) throws IOException {
        final Proxy proxy = getProxy(url);
        return url.openConnection(proxy);
    }

    public Proxy getProxy(final URL hubUrl) {
        final Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(getProxyHost(), getProxyPort()));
        return proxy;
    }

    public boolean shouldUseProxyForUrl(final URL url) {
        if (StringUtils.isBlank(getProxyHost()) || getProxyPort() <= 0) {
            return false;
        }
        final List<Pattern> ignoredProxyHostPatterns = ProxyUtil.getIgnoredProxyHostPatterns(getProxyNoHosts());
        return !ProxyUtil.shouldIgnoreHost(url.getHost(), ignoredProxyHostPatterns);
    }

    public RequestBody createJsonRequestBody(final String content) {
        return createJsonRequestBody("application/json", content);
    }

    public RequestBody createJsonRequestBody(final String mediaType, final String content) {
        return RequestBody.create(MediaType.parse(mediaType), content);
    }

    public RequestBody createEncodedRequestBody(final Map<String, String> content) {
        final FormBody.Builder builder = new FormBody.Builder();
        for (final Entry<String, String> contentEntry : content.entrySet()) {
            builder.addEncoded(contentEntry.getKey(), contentEntry.getValue());
        }
        return builder.build();
    }

    public Request createGetRequest(final HttpUrl httpUrl) {
        return createGetRequest(httpUrl, "application/json");
    }

    public Request createGetRequest(final HttpUrl httpUrl, final String mediaType) {
        final Map<String, String> headers = new HashMap<>();
        headers.put("Accept", mediaType);
        return createGetRequest(httpUrl, headers);
    }

    public Request createGetRequest(final HttpUrl httpUrl, final Map<String, String> headers) {
        final Request.Builder requestBuilder = new Request.Builder();
        for (final Entry<String, String> header : headers.entrySet()) {
            requestBuilder.addHeader(header.getKey(), header.getValue());
        }
        return requestBuilder
                .url(httpUrl).get().build();
    }

    public Request createPostRequest(final HttpUrl httpUrl, final RequestBody body) {
        return new Request.Builder()
                .url(httpUrl)
                .post(body).build();
    }

    public Request createPutRequest(final HttpUrl httpUrl, final RequestBody body) {
        return new Request.Builder()
                .url(httpUrl)
                .put(body).build();
    }

    public Request createDeleteRequest(final HttpUrl httpUrl) {
        return new Request.Builder()
                .url(httpUrl).delete().build();
    }

    public Response handleExecuteClientCall(final Request request) throws IntegrationException {
        return handleExecuteClientCall(request, 0);
    }

    private Response handleExecuteClientCall(final Request request, final int retryCount) throws IntegrationException {
        if (getClient() != null) {
            try {
                logRequestHeaders(request);
                final Response response = getClient().newCall(request).execute();
                if (!response.isSuccessful()) {
                    if (response.code() == 401 && retryCount < 2) {
                        connect();
                        return handleExecuteClientCall(request, retryCount + 1);
                    } else {
                        throw new IntegrationRestException(response.code(), response.message(),
                                String.format("There was a problem trying to %s this item: %s. Error: %s %s",
                                        request.method(), request.url().uri().toString(), response.code(), response.message()));
                    }
                }
                logResponseHeaders(response);
                return response;
            } catch (final IOException e) {
                throw new IntegrationException(e.getMessage(), e);
            }
        } else {
            connect();
            return handleExecuteClientCall(request, retryCount);
        }
    }

    private void logMessage(final LogLevel level, final String txt) {
        if (logger != null) {
            if (level == LogLevel.ERROR) {
                logger.error(txt);
            } else if (level == LogLevel.WARN) {
                logger.warn(txt);
            } else if (level == LogLevel.INFO) {
                logger.info(txt);
            } else if (level == LogLevel.DEBUG) {
                logger.debug(txt);
            } else if (level == LogLevel.TRACE) {
                logger.trace(txt);
            }
        }
    }

    private boolean isDebugLogging() {
        return logger != null && logger.getLogLevel() == LogLevel.TRACE;
    }

    protected void logRequestHeaders(final Request request) {
        if (isDebugLogging()) {
            final String requestName = request.getClass().getSimpleName();
            logMessage(LogLevel.TRACE, requestName + " : " + request.toString());
            logHeaders(requestName, request.headers());
        }
    }

    protected void logResponseHeaders(final Response response) {
        if (isDebugLogging()) {
            final String responseName = response.getClass().getSimpleName();
            logMessage(LogLevel.TRACE, responseName + " : " + response.toString());
            logHeaders(responseName, response.headers());
        }
    }

    private void logHeaders(final String requestOrResponseName, final Headers headers) {
        if (headers != null && headers.size() > 0) {
            logMessage(LogLevel.TRACE, requestOrResponseName + " headers : ");
            for (final Entry<String, List<String>> headerEntry : headers.toMultimap().entrySet()) {
                final String key = headerEntry.getKey();
                String value = "null";
                if (headerEntry.getValue() != null && !headerEntry.getValue().isEmpty()) {
                    value = StringUtils.join(headerEntry.getValue(), System.lineSeparator());
                }
                logMessage(LogLevel.TRACE, String.format("Header %s : %s", key, value));
            }
        } else {
            logMessage(LogLevel.TRACE, requestOrResponseName + " does not have any headers.");
        }
    }

    @Override
    public String toString() {
        return "RestConnection [baseUrl=" + getHubBaseUrl() + "]";
    }

    protected OkHttpClient.Builder getBuilder() {
        return builder;
    }

    public OkHttpClient getClient() {
        return client;
    }

    public void setClient(final OkHttpClient client) {
        this.client = client;
    }

    public URL getHubBaseUrl() {
        return hubBaseUrl;
    }

    public Gson getGson() {
        return gson;
    }

    public JsonParser getJsonParser() {
        return jsonParser;
    }

    public IntLogger getLogger() {
        return logger;
    }

    public void setLogger(final IntLogger logger) {
        this.logger = logger;
    }

    public int getTimeout() {
        return timeout;
    }

    public void setTimeout(final int timeout) {
        if (timeout <= 0) {
            throw new IllegalArgumentException("Timeout must be greater than zero.");
        }
        this.timeout = timeout;
        logMessage(LogLevel.DEBUG, "Setting connectTimeout to: " + timeout + "s on client context");
    }

    public String getProxyHost() {
        return proxyHost;
    }

    public void setProxyHost(final String proxyHost) {
        this.proxyHost = proxyHost;
    }

    public int getProxyPort() {
        return proxyPort;
    }

    public void setProxyPort(final int proxyPort) {
        this.proxyPort = proxyPort;
    }

    public String getProxyNoHosts() {
        return proxyNoHosts;
    }

    public void setProxyNoHosts(final String proxyNoHosts) {
        this.proxyNoHosts = proxyNoHosts;
    }

    public String getProxyUsername() {
        return proxyUsername;
    }

    public void setProxyUsername(final String proxyUsername) {
        this.proxyUsername = proxyUsername;
    }

    public String getProxyPassword() {
        return proxyPassword;
    }

    public void setProxyPassword(final String proxyPassword) {
        this.proxyPassword = proxyPassword;
    }

}
