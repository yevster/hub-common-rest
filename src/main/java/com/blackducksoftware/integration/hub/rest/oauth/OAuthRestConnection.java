/*******************************************************************************
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
 *******************************************************************************/
package com.blackducksoftware.integration.hub.rest.oauth;

import java.net.URL;

import com.blackducksoftware.integration.exception.IntegrationException;
import com.blackducksoftware.integration.hub.rest.RestConnection;
import com.blackducksoftware.integration.log.IntLogger;

public class OAuthRestConnection extends RestConnection {

    private final TokenManager tokenManager;

    private final AccessType accessType;

    public OAuthRestConnection(final IntLogger logger, final URL hubBaseUrl, final int timeout, final TokenManager tokenManager, final AccessType accessType) {
        super(logger, hubBaseUrl, timeout);
        this.tokenManager = tokenManager;
        this.accessType = accessType;
    }

    @Override
    public void addBuilderAuthentication() throws IntegrationException {
        getBuilder().authenticator(new OkOauthAuthenticator(tokenManager, accessType));
    }

    @Override
    public void clientAuthenticate() throws IntegrationException {
        tokenManager.refreshToken(accessType);
    }
}
