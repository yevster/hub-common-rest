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
package com.blackducksoftware.integration.hub.proxy;

import java.io.IOException;
import java.net.Proxy;
import java.util.List;

import okhttp3.Authenticator;
import okhttp3.Challenge;
import okhttp3.Credentials;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.Route;

public class OkAuthenticator implements Authenticator {
    public static final String PROXY_AUTH = "Proxy-Authenticate";
    public static final String PROXY_AUTH_RESP = "Proxy-Authorization";
    public static final String WWW_AUTH = "WWW-Authenticate";
    public static final String WWW_AUTH_RESP = "Authorization";

    private final String username;
    private final String password;
    private boolean proxy;
    private boolean basicAuth;

    public OkAuthenticator(final String username, final String password) {
        this.username = username;
        this.password = password;
    }

    @Override
    public Request authenticate(final Route route, final Response response) throws IOException {
        if (route.proxy() != null && route.proxy() != Proxy.NO_PROXY) {
            this.proxy = true;
        }
        checkAuthScheme(response);
        if (basicAuth) {
            if (response.priorResponse() != null) {
                // Should not attempt authentication again if the response has a previous response
                // because that means we have already tried to authenticate
                return null;
            }
            return authenticateBasic(response);
        }
        return null;
    }

    private void checkAuthScheme(final Response response) {
        final List<Challenge> challenges = response.challenges();
        for (final Challenge challenge : challenges) {
            if ("Basic".equalsIgnoreCase(challenge.scheme())) {
                this.basicAuth = true;
            }
        }
    }

    private Request authenticateBasic(final Response response) throws IOException {
        String headerKey;
        if (proxy) {
            headerKey = PROXY_AUTH_RESP;
        } else {
            headerKey = WWW_AUTH_RESP;
        }
        final String credential = Credentials.basic(username, password);
        return response.request().newBuilder().header(headerKey, credential).build();
    }

}
