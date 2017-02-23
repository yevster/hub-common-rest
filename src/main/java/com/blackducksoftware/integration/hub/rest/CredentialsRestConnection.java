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
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.blackducksoftware.integration.exception.IntegrationException;
import com.blackducksoftware.integration.hub.rest.exception.IntegrationRestException;
import com.blackducksoftware.integration.log.IntLogger;

import okhttp3.HttpUrl;
import okhttp3.JavaNetCookieJar;
import okhttp3.Request;
import okhttp3.Response;

public class CredentialsRestConnection extends RestConnection {

    private final String hubUsername;

    private final String hubPassword;

    public CredentialsRestConnection(final IntLogger logger, final URL hubBaseUrl, final String hubUsername, final String hubPassword, final int timeout) {
        super(logger, hubBaseUrl, timeout);
        this.hubUsername = hubUsername;
        this.hubPassword = hubPassword;
    }

    @Override
    public void addBuilderAuthentication() throws IntegrationRestException {
        if (StringUtils.isNotBlank(hubUsername) && StringUtils.isNotBlank(hubPassword)) {
            final CookieManager cookieManager = new CookieManager();
            cookieManager.setCookiePolicy(CookiePolicy.ACCEPT_ALL);
            getBuilder().cookieJar(new JavaNetCookieJar(cookieManager));
        }
    }

    /**
     * Gets the cookie for the Authorized connection to the Hub server. Returns
     * the response code from the connection.
     */
    @Override
    public void clientAuthenticate() throws IntegrationException {
        final ArrayList<String> segments = new ArrayList<>();
        segments.add("j_spring_security_check");
        final HttpUrl httpUrl = createHttpUrl(segments, null);

        final Map<String, String> content = new HashMap<>();
        if (StringUtils.isNotBlank(hubUsername) && StringUtils.isNotBlank(hubPassword)) {

            content.put("j_username", hubUsername);
            content.put("j_password", hubPassword);
            final Request request = createPostRequest(httpUrl, createEncodedRequestBody(content));
            Response response = null;
            try {
                logRequestHeaders(request);
                response = getClient().newCall(request).execute();
                logResponseHeaders(response);
                if (!response.isSuccessful()) {
                    throw new IntegrationRestException(response.code(), response.message(),
                            String.format("Connection Error: %s %s", response.code(), response.message()));
                }
            } catch (final IOException e) {
                throw new IntegrationException(e.getMessage(), e);
            } finally {
                if (response != null) {
                    response.close();
                }
            }
        }
    }

}
