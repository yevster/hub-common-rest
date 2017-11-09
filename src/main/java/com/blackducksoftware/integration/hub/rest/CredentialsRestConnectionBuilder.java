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

import java.net.URL;

import com.blackducksoftware.integration.exception.EncryptionException;
import com.blackducksoftware.integration.hub.Credentials;
import com.blackducksoftware.integration.hub.proxy.ProxyInfo;
import com.blackducksoftware.integration.log.IntLogger;

public class CredentialsRestConnectionBuilder extends AbstractRestConnectionBuilder<CredentialsRestConnectionBuilder, CredentialsRestConnection> {
    private Credentials credentials;

    public CredentialsRestConnectionBuilder applyCredentials(final Credentials credentials) {
        this.credentials = credentials;
        return this;
    }

    @Override
    public CredentialsRestConnection buildConnection(final IntLogger logger, final URL baseURL, final int timeout, final ProxyInfo proxyInfo) {
        try {
            return new CredentialsRestConnection(logger, baseURL, credentials.getUsername(), credentials.getDecryptedPassword(), timeout, proxyInfo);
        } catch (final EncryptionException ex) {
            throw new IllegalStateException(ex);
        }
    }

}
