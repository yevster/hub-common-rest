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
package com.blackducksoftware.integration.hub.rest.oauth;

import java.io.IOException;

import com.blackducksoftware.integration.exception.IntegrationException;
import com.blackducksoftware.integration.hub.rest.RestConnection;

import okhttp3.Authenticator;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.Route;

public class OkOauthAuthenticator implements Authenticator {
    private final TokenManager tokenManager;
    private final AccessType accessType;
    private final RestConnection restConnection;

    public OkOauthAuthenticator(final TokenManager tokenManager, final AccessType accessType, final RestConnection restConnection) {
        this.tokenManager = tokenManager;
        this.accessType = accessType;
        this.restConnection = restConnection;
    }

    @Override
    public Request authenticate(final Route route, final Response response) throws IOException {
        if (response.priorResponse() != null) {
            return null;
        } else {
            String credential;
            try {
                credential = tokenManager.createTokenCredential(tokenManager.getToken(accessType).accessToken);
            } catch (final IntegrationException e) {
                throw new IOException("Cannot refresh token", e);
            }
            restConnection.commonRequestHeaders.put(TokenManager.WWW_AUTH_RESP, credential);
            return response.request().newBuilder().header(TokenManager.WWW_AUTH_RESP, credential).build();
        }
    }

}
