/**
 * Hub Common Rest
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
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;
import java.net.URLEncoder;
import java.security.GeneralSecurityException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

import org.apache.commons.lang3.StringUtils;

import com.blackducksoftware.integration.exception.EncryptionException;
import com.blackducksoftware.integration.exception.IntegrationException;
import com.blackducksoftware.integration.hub.certificate.CertTrustManager;
import com.blackducksoftware.integration.hub.proxy.ProxyInfo;
import com.blackducksoftware.integration.hub.rest.exception.IntegrationRestException;
import com.blackducksoftware.integration.log.IntLogger;
import com.blackducksoftware.integration.log.LogLevel;
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
    private static final String ERROR_MSG_PROXY_INFO_NULL = "A RestConnection's proxy information cannot be null";

    public static final String JSON_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSSX";

    public final Gson gson = new GsonBuilder().setDateFormat(JSON_DATE_FORMAT).create();
    public final JsonParser jsonParser = new JsonParser();
    public final OkHttpClient.Builder builder = new OkHttpClient.Builder();
    public final Map<String, String> commonRequestHeaders = new HashMap<>();
    public final URL hubBaseUrl;
    public int timeout = 120;
    private final ProxyInfo proxyInfo;
    public boolean alwaysTrustServerCertificate;
    public IntLogger logger;

    private OkHttpClient client;

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

    public RestConnection(final IntLogger logger, final URL hubBaseUrl, final int timeout, final ProxyInfo proxyInfo) {
        this.logger = logger;
        this.hubBaseUrl = hubBaseUrl;
        this.timeout = timeout;
        this.proxyInfo = proxyInfo;
    }

    public void connect() throws IntegrationException {
        addBuilderConnectionTimes();
        addBuilderProxyInformation();
        addBuilderAuthentication();
        addTlsConnectionInfo();
        setClient(builder.build());
        clientAuthenticate();
    }

    public void addTlsConnectionInfo() throws IntegrationException {
        if (hubBaseUrl.getProtocol().equalsIgnoreCase("https")) {
            X509TrustManager trustManager = null;
            if (alwaysTrustServerCertificate) {
                trustManager = new CertTrustManager();
            } else {
                trustManager = systemDefaultTrustManager();
            }
            final String version = System.getProperty("java.version");
            SSLSocketFactory sSLSocketFactory = null;
            if (version.startsWith("1.7") || version.startsWith("1.6")) {
                // We do not need to do this for Java 8+
                try {
                    // Java 7 does not enable TLS1.2 so we use our TLSSocketFactory to enable all protocols
                    sSLSocketFactory = new TLSSocketFactory(trustManager);
                } catch (KeyManagementException | NoSuchAlgorithmException e) {
                    throw new IntegrationException(e);
                }
            } else {
                sSLSocketFactory = systemDefaultSslSocketFactory(trustManager);
            }
            builder.sslSocketFactory(sSLSocketFactory, trustManager);
        }
    }

    private X509TrustManager systemDefaultTrustManager() throws IntegrationException {
        try {
            final TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            trustManagerFactory.init((KeyStore) null);
            final TrustManager[] trustManagers = trustManagerFactory.getTrustManagers();
            if (trustManagers.length != 1 || !(trustManagers[0] instanceof X509TrustManager)) {
                throw new IllegalStateException("Unexpected default trust managers:" + Arrays.toString(trustManagers));
            }
            return (X509TrustManager) trustManagers[0];
        } catch (final GeneralSecurityException e) {
            // The system has no TLS. Just give up.
            throw new IntegrationException();
        }
    }

    private SSLSocketFactory systemDefaultSslSocketFactory(final X509TrustManager trustManager) throws IntegrationException {
        try {
            final SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, new TrustManager[] { trustManager }, null);
            return sslContext.getSocketFactory();
        } catch (final GeneralSecurityException e) {
            // The system has no TLS. Just give up.
            throw new IntegrationException(e);
        }
    }

    public abstract void addBuilderAuthentication() throws IntegrationException;

    public abstract void clientAuthenticate() throws IntegrationException;

    private void addBuilderConnectionTimes() {
        builder.connectTimeout(timeout, TimeUnit.SECONDS);
        builder.writeTimeout(timeout, TimeUnit.SECONDS);
        builder.readTimeout(timeout, TimeUnit.SECONDS);
    }

    private void addBuilderProxyInformation() throws IntegrationException {
        if (shouldUseProxyForUrl(hubBaseUrl)) {
            builder.proxy(getProxy(hubBaseUrl));
            try {
                builder.proxyAuthenticator(new com.blackducksoftware.integration.hub.proxy.OkAuthenticator(this.proxyInfo.getUsername(), this.proxyInfo.getDecryptedPassword()));
            } catch (IllegalArgumentException | EncryptionException ex) {
                throw new IntegrationException(ex);
            }
        }
    }

    private Proxy getProxy(final URL hubUrl) {
        final Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(this.proxyInfo.getHost(), this.proxyInfo.getPort()));
        return proxy;
    }

    private boolean shouldUseProxyForUrl(final URL url) {
        if (this.proxyInfo == null) {
            throw new IllegalStateException(ERROR_MSG_PROXY_INFO_NULL);
        }
        return this.proxyInfo.shouldUseProxyForUrl(url);
    }

    public HttpUrl createHttpUrl() {
        return HttpUrl.get(hubBaseUrl).newBuilder().build();
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

    public HttpUrl createHttpUrl(final List<String> urlSegments, final Map<String, String> queryParameters) {
        return createHttpUrl(hubBaseUrl.toString(), urlSegments, queryParameters);
    }

    public HttpUrl createHttpUrl(final String providedUrl, final List<String> urlSegments, final Map<String, String> queryParameters) {
        final HttpUrl.Builder urlBuilder = HttpUrl.parse(providedUrl).newBuilder();
        if (urlSegments != null) {
            for (final String urlSegment : urlSegments) {
                urlBuilder.addPathSegment(urlSegment);
            }
        }
        if (queryParameters != null) {
            for (final Entry<String, String> queryParameter : queryParameters.entrySet()) {
                try {
                    // As of okhttp 3.8.0 the escaped characters are space, ", ', <, >, #, &, and =, so we need to
                    // encode on our own
                    // see HttpUrl.java, QUERY_COMPONENT_ENCODE_SET
                    final String encodedKey = URLEncoder.encode(queryParameter.getKey(), "UTF-8");
                    final String encodedVal = URLEncoder.encode(queryParameter.getValue(), "UTF-8");
                    urlBuilder.addEncodedQueryParameter(encodedKey, encodedVal);
                } catch (final UnsupportedEncodingException e) {
                    if (logger != null) {
                        logger.error(e);
                    }
                }
            }
        }
        return urlBuilder.build();
    }

    public RequestBody createJsonRequestBody(final String content) {
        return createJsonRequestBody("application/json", content);
    }

    public RequestBody createJsonRequestBody(final String mediaType, final String content) {
        return RequestBody.create(MediaType.parse(mediaType), content);
    }

    public RequestBody createEncodedFormBody(final Map<String, String> content) {
        final FormBody.Builder builder = new FormBody.Builder();
        for (final Entry<String, String> contentEntry : content.entrySet()) {
            builder.add(contentEntry.getKey(), contentEntry.getValue());
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
        return getRequestBuilder(headers).url(httpUrl).get().build();
    }

    public Request createPostRequest(final HttpUrl httpUrl, final RequestBody body) {
        return getRequestBuilder().url(httpUrl).post(body).build();
    }

    public Request createPostRequest(final HttpUrl httpUrl, final Map<String, String> headers, final RequestBody body) {
        return getRequestBuilder(headers).url(httpUrl).post(body).build();
    }

    public Request createPutRequest(final HttpUrl httpUrl, final RequestBody body) {
        return getRequestBuilder().url(httpUrl).put(body).build();
    }

    public Request createDeleteRequest(final HttpUrl httpUrl) {
        return getRequestBuilder().url(httpUrl).delete().build();
    }

    private Request.Builder getRequestBuilder() {
        return getRequestBuilder(null);
    }

    private Request.Builder getRequestBuilder(final Map<String, String> headers) {
        final Request.Builder builder = new Request.Builder();
        final Map<String, String> requestHeaders = new HashMap<>();
        requestHeaders.putAll(commonRequestHeaders);
        if (headers != null) {
            requestHeaders.putAll(headers);
        }
        for (final Entry<String, String> header : requestHeaders.entrySet()) {
            builder.addHeader(header.getKey(), header.getValue());
        }
        return builder;
    }

    private Request createNewRequest(final Request request) {
        final Request.Builder builder = request.newBuilder();
        for (final Map.Entry<String, String> entry : commonRequestHeaders.entrySet()) {
            builder.header(entry.getKey(), entry.getValue());
        }

        return builder.build();
    }

    public Response handleExecuteClientCall(final Request request) throws IntegrationException {
        final long start = System.currentTimeMillis();
        logMessage(LogLevel.TRACE, "starting request: " + request.url());
        try {
            return handleExecuteClientCall(request, 0);
        } finally {
            final long end = System.currentTimeMillis();
            logMessage(LogLevel.TRACE, String.format("completed request: %s (%d ms)", request.url(), end - start));
        }
    }

    private Response handleExecuteClientCall(final Request request, final int retryCount) throws IntegrationException {
        if (client != null) {
            try {
                final URL url = request.url().url();
                final String urlString = request.url().uri().toString();
                if (alwaysTrustServerCertificate && url.getProtocol().equalsIgnoreCase("https") && logger != null) {
                    logger.debug("Automatically trusting the certificate for " + urlString);
                }
                logRequestHeaders(request);
                final Response response = client.newCall(request).execute();
                if (!response.isSuccessful()) {
                    try {
                        if (response.code() == 401 && retryCount < 2) {
                            connect();
                            final Request newRequest = createNewRequest(request);
                            return handleExecuteClientCall(newRequest, retryCount + 1);
                        } else {
                            throw new IntegrationRestException(response.code(), response.message(),
                                    String.format("There was a problem trying to %s this item: %s. Error: %s %s", request.method(), urlString, response.code(), response.message()));
                        }
                    } finally {
                        // request was un-successful make sure the response is closed to close the body
                        response.close();
                    }
                }
                logResponseHeaders(response);
                return response;
            } catch (final IOException e) {
                throw new IntegrationException(e.getMessage(), e);
            }
        } else {
            connect();
            final Request newRequest = createNewRequest(request);
            return handleExecuteClientCall(newRequest, retryCount);
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
        return "RestConnection [baseUrl=" + hubBaseUrl + "]";
    }

    public OkHttpClient getClient() {
        return client;
    }

    public void setClient(final OkHttpClient client) {
        this.client = client;
    }

    public ProxyInfo getProxyInfo() {
        return proxyInfo;
    }
}
