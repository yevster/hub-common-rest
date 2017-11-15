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

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import com.blackducksoftware.integration.builder.AbstractBuilder;
import com.blackducksoftware.integration.exception.EncryptionException;
import com.blackducksoftware.integration.hub.proxy.ProxyInfo;
import com.blackducksoftware.integration.hub.proxy.ProxyInfoBuilder;
import com.blackducksoftware.integration.log.IntLogger;

public abstract class AbstractRestConnectionBuilder<C extends RestConnection> extends AbstractBuilder<C> {

    private String baseUrl;
    private int timeout = 120;
    private String proxyHost;
    private int proxyPort;
    private String proxyUsername;
    private String proxyPassword;
    private String proxyIgnoreHosts;
    private IntLogger logger;
    private boolean alwaysTrustServerCertificate;
    private Map<String, String> commonRequestHeaders = new HashMap<>();

    @Override
    public C buildObject() {
        final ProxyInfo proxyInfo = getProxyInfo();
        final C connection = createConnection(proxyInfo);
        connection.alwaysTrustServerCertificate = alwaysTrustServerCertificate;
        if (!this.commonRequestHeaders.isEmpty()) {
            connection.commonRequestHeaders.putAll(this.commonRequestHeaders);
        }
        return connection;
    }

    public abstract C createConnection(ProxyInfo proxyInfo);

    private ProxyInfo getProxyInfo() {
        final ProxyInfoBuilder builder = new ProxyInfoBuilder();
        builder.setHost(proxyHost);
        builder.setPort(proxyPort);
        builder.setUsername(proxyUsername);
        builder.setPassword(proxyPassword);
        builder.setIgnoredProxyHosts(proxyIgnoreHosts);
        return builder.buildObject();
    }

    public void applyHeader(final String headerName, final String headerValue) {
        commonRequestHeaders.put(headerName, headerValue);
    }

    public void applyProxyInfo(final ProxyInfo proxyInfo) {
        try {
            setProxyHost(proxyInfo.getHost());
            setProxyPort(proxyInfo.getPort());
            setProxyUsername(proxyInfo.getUsername());
            setProxyPassword(proxyInfo.getDecryptedPassword());
            setProxyIgnoreHosts(proxyInfo.getIgnoredProxyHosts());
        } catch (IllegalArgumentException | EncryptionException ex) {
            throw new IllegalArgumentException(ex);
        }
    }

    public URL getBaseConnectionUrl() {
        try {
            return new URL(baseUrl);
        } catch (final MalformedURLException e) {
            return null;
        }
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(final String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public int getTimeout() {
        return timeout;
    }

    public void setTimeout(final int timeout) {
        this.timeout = timeout;
    }

    public String getProxyHost() {
        return proxyHost;
    }

    public void setProxyHost(final String proxyHost) {
        this.proxyHost = proxyHost;
    }

    public int getProxyPort() {
        return proxyPort;
    }

    public void setProxyPort(final int proxyPort) {
        this.proxyPort = proxyPort;
    }

    public String getProxyUsername() {
        return proxyUsername;
    }

    public void setProxyUsername(final String proxyUsername) {
        this.proxyUsername = proxyUsername;
    }

    public String getProxyPassword() {
        return proxyPassword;
    }

    public void setProxyPassword(final String proxyPassword) {
        this.proxyPassword = proxyPassword;
    }

    public String getProxyIgnoreHosts() {
        return proxyIgnoreHosts;
    }

    public void setProxyIgnoreHosts(final String proxyIgnoreHosts) {
        this.proxyIgnoreHosts = proxyIgnoreHosts;
    }

    public IntLogger getLogger() {
        return logger;
    }

    public void setLogger(final IntLogger logger) {
        this.logger = logger;
    }

    public boolean isAlwaysTrustServerCertificate() {
        return alwaysTrustServerCertificate;
    }

    public void setAlwaysTrustServerCertificate(final boolean alwaysTrustServerCertificate) {
        this.alwaysTrustServerCertificate = alwaysTrustServerCertificate;
    }

    public Map<String, String> getCommonRequestHeaders() {
        return commonRequestHeaders;
    }

    public void setCommonRequestHeaders(final Map<String, String> commonRequestHeaders) {
        this.commonRequestHeaders = commonRequestHeaders;
    }
}
