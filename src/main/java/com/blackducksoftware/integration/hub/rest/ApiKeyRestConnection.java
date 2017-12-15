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
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.blackducksoftware.integration.exception.IntegrationException;
import com.blackducksoftware.integration.hub.proxy.ProxyInfo;
import com.blackducksoftware.integration.hub.rest.exception.IntegrationRestException;
import com.blackducksoftware.integration.log.IntLogger;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import okhttp3.HttpUrl;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Connection to the Hub application which authenticates using the API key feature (added in Hub 4.4.0)
 *
 * @author romeara
 */
public class ApiKeyRestConnection extends RestConnection {

    private static final String AUTHORIZATION_HEADER = "Authorization";

    private static final String X_CSRF_TOKEN = "X-CSRF-TOKEN";

    private final String hubApiKey;

    public ApiKeyRestConnection(final IntLogger logger, final URL hubBaseUrl, final String hubApiKey, final int timeout, final ProxyInfo proxyInfo) {
        super(logger, hubBaseUrl, timeout, proxyInfo);
        this.hubApiKey = hubApiKey;
    }

    @Override
    public void addBuilderAuthentication() throws IntegrationRestException {
    }

    /**
     * Gets the cookie for the Authorized connection to the Hub server. Returns the response code from the connection.
     */
    @Override
    public void clientAuthenticate() throws IntegrationException {
        final ArrayList<String> segments = new ArrayList<>();
        segments.add("api");
        segments.add("tokens");
        segments.add("authenticate");
        final HttpUrl httpUrl = createHttpUrl(segments, null);

        final Map<String, String> content = new HashMap<>();
        if (StringUtils.isNotBlank(hubApiKey)) {
            final Request request = createPostRequest(httpUrl, getRequestHeaders(), createEncodedFormBody(content));

            try (Response response = getClient().newCall(request).execute()) {
                // We don't log the headers here, as they contain a secret (the API key)

                logResponseHeaders(response);
                if (!response.isSuccessful()) {
                    throw new IntegrationRestException(response.code(), response.message(),
                            String.format("Connection Error: %s %s", response.code(), response.message()));
                } else {
                    // Extract the bearer token and apply to headers
                    commonRequestHeaders.put(AUTHORIZATION_HEADER, "Bearer " + readBearerToken(response));

                    // get the CSRF token
                    final String csrfToken = response.header(X_CSRF_TOKEN);
                    if (StringUtils.isNotBlank(csrfToken)) {
                        commonRequestHeaders.put(X_CSRF_TOKEN, csrfToken);
                    }
                }
            } catch (final IOException e) {
                throw new IntegrationException(e.getMessage(), e);
            }
        }
    }

    private Map<String, String> getRequestHeaders() {
        Map<String, String> headers = new HashMap<>();
        headers.put(AUTHORIZATION_HEADER, "token " + hubApiKey);

        return headers;
    }

    private String readBearerToken(Response response) throws IOException {
        JsonParser jsonParser = new JsonParser();

        JsonObject bearerResponse = jsonParser.parse(response.body().string()).getAsJsonObject();
        return bearerResponse.get("bearerToken").getAsString();
    }

}
