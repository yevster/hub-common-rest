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

import com.blackducksoftware.integration.hub.proxy.ProxyInfo;
import com.blackducksoftware.integration.hub.validator.UnauthenticatedRestConnectionValidator;
import com.blackducksoftware.integration.validator.AbstractValidator;

public class UnauthenticatedRestConnectionBuilder extends AbstractRestConnectionBuilder<UnauthenticatedRestConnection> {

    @Override
    public AbstractValidator createValidator() {
        final UnauthenticatedRestConnectionValidator validator = new UnauthenticatedRestConnectionValidator();
        validator.setBaseUrl(getBaseUrl());
        validator.setTimeout(getTimeout());
        validator.setProxyHost(getProxyHost());
        validator.setProxyPort(getProxyPort());
        validator.setProxyUsername(getProxyUsername());
        validator.setProxyPassword(getProxyPassword());
        validator.setProxyIgnoreHosts(getProxyIgnoreHosts());
        validator.setLogger(getLogger());
        validator.setCommonRequestHeaders(getCommonRequestHeaders());
        return validator;
    }

    @Override
    public UnauthenticatedRestConnection createConnection(final ProxyInfo proxyInfo) {
        return new UnauthenticatedRestConnection(getLogger(), getBaseConnectionUrl(), getTimeout(), proxyInfo);
    }
}
