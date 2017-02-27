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
package com.blackducksoftware.integration.hub.service;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.blackducksoftware.integration.exception.IntegrationException;
import com.blackducksoftware.integration.hub.api.oauth.Token;
import com.blackducksoftware.integration.hub.rest.RestConnection;

import okhttp3.HttpUrl;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class HubOAuthTokenService extends HubRequestService {

    public HubOAuthTokenService(final RestConnection restConnection) {
        super(restConnection);
    }

    public Token requestUserToken(final String clientId, final String authCode, final String redirectUri)
            throws IntegrationException {
        return requestUserToken(clientId, null, authCode, redirectUri);
    }

    public Token requestUserToken(final String clientId, final String clientSecret, final String authCode, final String redirectUri)
            throws IntegrationException {
        Token token = null;
        Response response = null;
        try {
            final Map<String, String> formDataMap = new LinkedHashMap<>();
            formDataMap.put("grant_type", "authorization_code");
            formDataMap.put("redirect_uri", redirectUri);
            formDataMap.put("client_id", clientId);
            formDataMap.put("code", authCode);

            if (StringUtils.isNotBlank(clientSecret)) {
                formDataMap.put("client_secret", clientSecret);
            }

            final RequestBody requestBody = getRestConnection().createEncodedRequestBody(formDataMap);
            final HttpUrl httpUrl = getRestConnection().createHttpUrl();
            final Request request = getRestConnection().createPostRequest(httpUrl, requestBody);

            response = getRestConnection().handleExecuteClientCall(request);
            final String jsonToken = response.body().string();
            token = getRestConnection().getGson().fromJson(jsonToken, Token.class);
        } catch (final IOException e) {
            throw new IntegrationException(e);
        } finally {
            if (response != null) {
                response.close();
            }
        }
        return token;
    }

    public Token refreshClientToken(final String clientId) throws IntegrationException {
        return refreshClientToken(clientId, null);
    }

    public Token refreshClientToken(final String clientId, final String clientSecret) throws IntegrationException {
        Token token = null;
        Response response = null;
        try {
            final Map<String, String> formDataMap = new LinkedHashMap<>();
            formDataMap.put("grant_type", "client_credentials");
            formDataMap.put("scope", "read write");
            formDataMap.put("client_id", clientId);

            if (StringUtils.isNotBlank(clientSecret)) {
                formDataMap.put("client_secret", clientSecret);
            }

            final RequestBody requestBody = getRestConnection().createEncodedRequestBody(formDataMap);
            final HttpUrl httpUrl = getRestConnection().createHttpUrl();
            final Request request = getRestConnection().createPostRequest(httpUrl, requestBody);
            response = getRestConnection().handleExecuteClientCall(request);
            final String jsonToken = response.body().string();
            token = getRestConnection().getGson().fromJson(jsonToken, Token.class);
        } catch (final IOException e) {
            throw new IntegrationException(e);
        } finally {
            if (response != null) {
                response.close();
            }
        }
        return token;
    }

    public Token refreshUserToken(final String clientId, final String refreshToken) throws IntegrationException {
        return refreshUserToken(clientId, null, refreshToken);
    }

    public Token refreshUserToken(final String clientId, final String clientSecret, final String refreshToken) throws IntegrationException {
        Token token = null;
        Response response = null;
        try {
            final Map<String, String> formDataMap = new LinkedHashMap<>();
            formDataMap.put("grant_type", "refresh_token");
            formDataMap.put("refresh_token", refreshToken);
            formDataMap.put("client_id", clientId);

            if (StringUtils.isNotBlank(clientSecret)) {
                formDataMap.put("client_secret", clientSecret);
            }

            final RequestBody requestBody = getRestConnection().createEncodedRequestBody(formDataMap);
            final HttpUrl httpUrl = getRestConnection().createHttpUrl();
            final Request request = getRestConnection().createPostRequest(httpUrl, requestBody);
            response = getRestConnection().handleExecuteClientCall(request);
            final String jsonToken = response.body().string();
            token = getRestConnection().getGson().fromJson(jsonToken, Token.class);
        } catch (final IOException e) {
            throw new IntegrationException(e);
        } finally {
            if (response != null) {
                response.close();
            }
        }
        return token;
    }

}
