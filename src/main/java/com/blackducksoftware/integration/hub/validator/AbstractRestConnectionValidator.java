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
package com.blackducksoftware.integration.hub.validator;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import com.blackducksoftware.integration.hub.rest.RestConnectionFieldEnum;
import com.blackducksoftware.integration.log.IntLogger;
import com.blackducksoftware.integration.validator.AbstractValidator;
import com.blackducksoftware.integration.validator.ValidationResult;
import com.blackducksoftware.integration.validator.ValidationResultEnum;
import com.blackducksoftware.integration.validator.ValidationResults;

public abstract class AbstractRestConnectionValidator extends AbstractValidator {

    public static final String ERROR_MSG_URL_NOT_FOUND = "No Hub Url was found.";

    public static final String ERROR_MSG_URL_NOT_VALID_PREFIX = "This is not a valid URL : ";

    public static final String ERROR_MSG_URL_NOT_VALID = "The Hub Url is not a valid URL.";

    private String baseUrl;
    private int timeout = 120;
    private String proxyHost;
    private int proxyPort;
    private String proxyUsername;
    private String proxyPassword;
    private String proxyIgnoreHosts;
    private IntLogger logger;
    private Map<String, String> commonRequestHeaders = new HashMap<>();

    @Override
    public ValidationResults assertValid() {
        final ValidationResults result = new ValidationResults();
        validateBaseUrl(result);
        validateLogger(result);
        validateCommonRequestHeaders(result);
        validateProxyInfo(result);
        validateAdditionalFields(result);
        return result;
    }

    public void validateBaseUrl(final ValidationResults result) {
        if (baseUrl == null) {
            result.addResult(RestConnectionFieldEnum.URL, new ValidationResult(ValidationResultEnum.ERROR, ERROR_MSG_URL_NOT_FOUND));
            return;
        }

        URL hubURL = null;
        try {
            hubURL = new URL(baseUrl);
            hubURL.toURI();
        } catch (final MalformedURLException | URISyntaxException e) {
            result.addResult(RestConnectionFieldEnum.URL, new ValidationResult(ValidationResultEnum.ERROR, ERROR_MSG_URL_NOT_VALID));
            return;
        }
    }

    public void validateTimeout(final ValidationResults result) {
        if (timeout <= 0) {
            result.addResult(RestConnectionFieldEnum.TIMEOUT, new ValidationResult(ValidationResultEnum.ERROR, "The Timeout must be greater than 0."));
        }
    }

    public void validateLogger(final ValidationResults result) {
        if (logger == null) {
            result.addResult(RestConnectionFieldEnum.LOGGER, new ValidationResult(ValidationResultEnum.ERROR, "This logger instance cannot be null"));
        }
    }

    public void validateCommonRequestHeaders(final ValidationResults result) {
        if (commonRequestHeaders == null) {
            result.addResult(RestConnectionFieldEnum.COMMON_HEADERS, new ValidationResult(ValidationResultEnum.ERROR, "The common headers map cannot be null"));
        }
    }

    public void validateProxyInfo(final ValidationResults result) {
        final ProxyInfoValidator validator = new ProxyInfoValidator();
        validator.setHost(proxyHost);
        validator.setPort(proxyPort);
        validator.setUsername(proxyUsername);
        validator.setPassword(proxyPassword);
        validator.setIgnoredProxyHosts(proxyIgnoreHosts);
    }

    public abstract void validateAdditionalFields(ValidationResults currentResults);

    public String getBaseUrl() {
        return this.baseUrl;
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

    public Map<String, String> getCommonRequestHeaders() {
        return commonRequestHeaders;
    }

    public void setCommonRequestHeaders(final Map<String, String> commonRequestHeaders) {
        this.commonRequestHeaders = commonRequestHeaders;
    }

}
