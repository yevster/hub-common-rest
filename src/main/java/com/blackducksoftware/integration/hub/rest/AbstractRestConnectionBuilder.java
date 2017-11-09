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
import java.util.HashMap;
import java.util.Map;

import com.blackducksoftware.integration.hub.proxy.ProxyInfo;
import com.blackducksoftware.integration.log.IntLogger;

public abstract class AbstractRestConnectionBuilder<B extends AbstractRestConnectionBuilder<B, C>, C extends RestConnection> {

    private URL baseUrl;
    private int timeout = 120;
    private ProxyInfo proxyInfo;
    private IntLogger logger;
    private boolean alwaysTrustServerCertificate;
    private Map<String, String> commonRequestHeaders = new HashMap<>();

    public B applyBaseUrl(final URL baseUrl) {
        this.baseUrl = baseUrl;
        return (B) this;
    }

    public B applyTimeout(final int timeout) {
        this.timeout = timeout;
        return (B) this;
    }

    public B applyLogger(final IntLogger logger) {
        this.logger = logger;
        return (B) this;
    }

    public B applyAlwaysTrustServerCertificate(final ProxyInfo proxyInfo) {
        this.proxyInfo = proxyInfo;
        return (B) this;
    }

    public B applyProxyInfo(final ProxyInfo proxyInfo) {
        this.proxyInfo = proxyInfo;
        return (B) this;
    }

    public B applyHeader(final String headerName, final String headerValue) {
        commonRequestHeaders.put(headerName, headerValue);
        return (B) this;
    }

    public B applyHeaders(final Map<String, String> commonRequestHeaders) {
        this.commonRequestHeaders = commonRequestHeaders;
        return (B) this;
    }

    public C build() {
        if (proxyInfo == null) {
            // this.proxyInfo = new NoProxyInfo(null, null, null, null);
        }
        final C connection = buildConnection(logger, baseUrl, timeout, proxyInfo);
        connection.alwaysTrustServerCertificate = this.alwaysTrustServerCertificate;
        if (!this.commonRequestHeaders.isEmpty()) {
            connection.commonRequestHeaders.putAll(this.commonRequestHeaders);
        }
        return connection;
    }

    public abstract C buildConnection(final IntLogger logger, final URL baseURL, final int timeout, final ProxyInfo proxyInfo);
}
