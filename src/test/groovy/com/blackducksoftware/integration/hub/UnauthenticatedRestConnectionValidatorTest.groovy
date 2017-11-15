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
package com.blackducksoftware.integration.hub

import org.apache.commons.lang3.StringUtils
import org.junit.Test

import com.blackducksoftware.integration.hub.rest.RestConnectionFieldEnum
import com.blackducksoftware.integration.hub.validator.UnauthenticatedRestConnectionValidator
import com.blackducksoftware.integration.log.IntLogger
import com.blackducksoftware.integration.log.LogLevel
import com.blackducksoftware.integration.log.PrintStreamIntLogger
import com.blackducksoftware.integration.validator.ValidationResults

class UnauthenticatedRestConnectionValidatorTest {

    @Test
    public void testValid() {
        UnauthenticatedRestConnectionValidator validator = new UnauthenticatedRestConnectionValidator()
        validator.baseUrl = "http://www.google.com"
        validator.setTimeout(120)
        validator.setLogger(new PrintStreamIntLogger(System.out, LogLevel.INFO))
        validator.setCommonRequestHeaders(new HashMap<>())
        assert validator.assertValid().success
    }

    @Test
    public void testInvalid() {
        UnauthenticatedRestConnectionValidator validator = new UnauthenticatedRestConnectionValidator()
        validator.setTimeout(-1)
        validator.setLogger(null)
        validator.setCommonRequestHeaders(null)
        ValidationResults result = validator.assertValid()
        assert !result.success
    }

    @Test
    public void testBaseUrlValid() {
        UnauthenticatedRestConnectionValidator validator = new UnauthenticatedRestConnectionValidator()
        String baseUrl = "http://www.google.com"
        validator.setBaseUrl(baseUrl)
        ValidationResults result = new ValidationResults()
        validator.validateBaseUrl(result)
        final String resultString = result.getResultString(RestConnectionFieldEnum.URL)
        assert baseUrl == validator.getBaseUrl()
        assert result.success
        assert StringUtils.isBlank(resultString)
    }

    @Test
    public void testBaseUrlInvalid() {
        UnauthenticatedRestConnectionValidator validator = new UnauthenticatedRestConnectionValidator()
        String baseUrl = null
        validator.setBaseUrl(baseUrl)
        ValidationResults result = new ValidationResults()
        validator.validateBaseUrl(result)
        final String resultString = result.getResultString(RestConnectionFieldEnum.URL)
        assert null == validator.getBaseUrl()
        assert result.hasErrors()
        assert StringUtils.isNotBlank(resultString)
        assert resultString.contains(UnauthenticatedRestConnectionValidator.ERROR_MSG_URL_NOT_FOUND)

        baseUrl = "htp:/a.bad.domain"
        validator.setBaseUrl(baseUrl)
        result = new ValidationResults()
        validator.validateBaseUrl(result)
        resultString = result.getResultString(RestConnectionFieldEnum.URL)
        assert baseUrl == validator.getBaseUrl()
        assert result.hasErrors()
        assert StringUtils.isNotBlank(resultString)
        assert resultString.contains(UnauthenticatedRestConnectionValidator.ERROR_MSG_URL_NOT_VALID)
    }

    @Test
    public void testLoggerValid() {
        UnauthenticatedRestConnectionValidator validator = new UnauthenticatedRestConnectionValidator()
        IntLogger logger = new PrintStreamIntLogger(System.out, LogLevel.INFO)
        validator.setLogger(logger)
        ValidationResults result = new ValidationResults()
        validator.validateLogger(result)
        final String resultString = result.getResultString(RestConnectionFieldEnum.LOGGER)
        assert logger == validator.getLogger()
        assert result.success
        assert StringUtils.isBlank(resultString)
    }

    @Test
    public void testLoggerInvalid() {
        UnauthenticatedRestConnectionValidator validator = new UnauthenticatedRestConnectionValidator()
        validator.setLogger(null)
        ValidationResults result = new ValidationResults()
        validator.validateLogger(result)
        final String resultString = result.getResultString(RestConnectionFieldEnum.LOGGER)
        assert null == validator.getLogger()
        assert result.hasErrors()
        assert StringUtils.isNotBlank(resultString)
        assert resultString.contains(UnauthenticatedRestConnectionValidator.ERROR_MSG_LOGGER_NOT_VALID)
    }

    @Test
    public void testTimeoutValid() {
        UnauthenticatedRestConnectionValidator validator = new UnauthenticatedRestConnectionValidator()
        int timeout = 120
        validator.setTimeout(timeout)
        ValidationResults result = new ValidationResults()
        validator.validateTimeout(result)
        final String resultString = result.getResultString(RestConnectionFieldEnum.TIMEOUT)
        assert timeout == validator.getTimeout()
        assert result.success
        assert StringUtils.isBlank(resultString)
    }

    @Test
    public void testTimeoutInvalid() {
        UnauthenticatedRestConnectionValidator validator = new UnauthenticatedRestConnectionValidator()
        validator.setTimeout(-1)
        ValidationResults result = new ValidationResults()
        validator.validateTimeout(result)
        final String resultString = result.getResultString(RestConnectionFieldEnum.TIMEOUT)
        assert -1 == validator.getTimeout()
        assert result.hasErrors()
        assert StringUtils.isNotBlank(resultString)
        assert resultString.contains(UnauthenticatedRestConnectionValidator.ERROR_MSG_TIMEOUT_NOT_VALID)
    }

    @Test
    public void testHeadersValid() {
        UnauthenticatedRestConnectionValidator validator = new UnauthenticatedRestConnectionValidator()
        Map<String,String> headers = new HashMap<>();
        validator.setCommonRequestHeaders(headers)
        ValidationResults result = new ValidationResults()
        validator.validateCommonRequestHeaders(result)
        final String resultString = result.getResultString(RestConnectionFieldEnum.COMMON_HEADERS)
        assert headers == validator.getCommonRequestHeaders()
        assert result.success
        assert StringUtils.isBlank(resultString)
    }

    @Test
    public void testHeadersInvalid() {
        UnauthenticatedRestConnectionValidator validator = new UnauthenticatedRestConnectionValidator()
        validator.setCommonRequestHeaders(null)
        ValidationResults result = new ValidationResults()
        validator.validateCommonRequestHeaders(result)
        final String resultString = result.getResultString(RestConnectionFieldEnum.COMMON_HEADERS)
        assert null == validator.getCommonRequestHeaders()
        assert result.hasErrors()
        assert StringUtils.isNotBlank(resultString)
        assert resultString.contains(UnauthenticatedRestConnectionValidator.ERROR_MSG_COMMON_HEADERS_NOT_VALID)
    }

    @Test
    public void testProxyValid() {
        UnauthenticatedRestConnectionValidator validator = new UnauthenticatedRestConnectionValidator()
        ValidationResults result = new ValidationResults()
        validator.validateProxyInfo(result)
        assert result.success

        result = new ValidationResults()
        String proxyHost = "proxyhost"
        int proxyPort = 25
        validator.proxyHost = proxyHost
        validator.proxyPort = proxyPort
        validator.validateProxyInfo(result)
        assert result.success

        result = new ValidationResults()
        proxyHost = "proxyhost"
        proxyPort = 25
        String username = "proxyUser"
        String password = "proxyPassword"
        String ignoredHost = ".*"
        validator.proxyHost = proxyHost
        validator.proxyPort = proxyPort
        validator.proxyUsername = username
        validator.proxyPassword = password
        validator.proxyIgnoreHosts = ignoredHost
        validator.validateProxyInfo(result)
        assert proxyHost == validator.proxyHost
        assert proxyPort == validator.proxyPort
        assert username == validator.proxyUsername
        assert password == validator.proxyPassword
        assert ignoredHost == validator.proxyIgnoreHosts
        assert result.success
    }

    @Test
    public void testProxyInvalid() {
        UnauthenticatedRestConnectionValidator validator = new UnauthenticatedRestConnectionValidator()
        ValidationResults result = new ValidationResults()
        String proxyHost = "proxyhost"
        int proxyPort = -1
        validator.proxyHost = proxyHost
        validator.proxyPort = proxyPort
        validator.validateProxyInfo(result)
        assert result.hasErrors()

        result = new ValidationResults()
        proxyHost = "proxyhost"
        proxyPort = 25
        String username = "proxyUser"
        String password = null
        String ignoredHost = ".*"
        validator.proxyHost = proxyHost
        validator.proxyPort = proxyPort
        validator.proxyUsername = username
        validator.proxyPassword = password
        validator.proxyIgnoreHosts = ignoredHost
        validator.validateProxyInfo(result)
        assert result.hasErrors()

        result = new ValidationResults()
        proxyHost = "proxyhost"
        proxyPort = 25
        username = null
        password = "proxyPassword"
        ignoredHost = ".*"
        validator.proxyHost = proxyHost
        validator.proxyPort = proxyPort
        validator.proxyUsername = username
        validator.proxyPassword = password
        validator.proxyIgnoreHosts = ignoredHost
        validator.validateProxyInfo(result)
        assert result.hasErrors()

        result = new ValidationResults()
        proxyHost = "proxyhost"
        proxyPort = 25
        username = "proxyUser"
        password = "proxyPassword"
        ignoredHost = ".asdfajdflkjaf{ ])(faslkfj"
        validator.proxyHost = proxyHost
        validator.proxyPort = proxyPort
        validator.proxyUsername = username
        validator.proxyPassword = password
        validator.proxyIgnoreHosts = ignoredHost
        validator.validateProxyInfo(result)
        assert result.hasErrors()
    }
}
