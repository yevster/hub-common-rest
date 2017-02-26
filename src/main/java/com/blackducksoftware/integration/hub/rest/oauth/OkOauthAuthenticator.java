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
package com.blackducksoftware.integration.hub.rest.oauth;

import java.io.IOException;

import com.blackducksoftware.integration.exception.IntegrationException;
import com.blackducksoftware.integration.hub.api.oauth.Token;

import okhttp3.Authenticator;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.Route;

public class OkOauthAuthenticator implements Authenticator {

    public static final String WWW_AUTH_RESP = "Authorization";

    private final TokenManager tokenManager;

    private final AccessType accessType;

    public OkOauthAuthenticator(final TokenManager tokenManager, final AccessType accessType) {
        this.tokenManager = tokenManager;
        this.accessType = accessType;
    }

    @Override
    public Request authenticate(final Route route, final Response response) throws IOException {
        final String token = refreshToken();
        final String credential = createTokenCredential(token);
        return response.request().newBuilder().header(WWW_AUTH_RESP, credential).build();
    }

    private String createTokenCredential(final String token) {
        return String.format("%s %s", "Bearer", token);
    }

    private String refreshToken() throws IOException {
        Token token;
        try {
            token = tokenManager.refreshToken(accessType);
            return token.accessToken;
        } catch (final IntegrationException ex) {
            throw new IOException("Cannot refresh token", ex);
        }
    }
}
