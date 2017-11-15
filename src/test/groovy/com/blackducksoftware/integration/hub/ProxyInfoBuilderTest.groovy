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

import com.blackducksoftware.integration.encryption.PasswordEncrypter
import com.blackducksoftware.integration.hub.proxy.ProxyInfo
import com.blackducksoftware.integration.hub.proxy.ProxyInfoBuilder

class ProxyInfoBuilderTest {

    @Test
    public void testBuilder() {
        final String username = "username"
        final String password = "password"
        final String proxyHost = "proxyHost"
        final int proxyPort = 25
        final String proxyIgnoredHosts = ".*"
        final ProxyInfoBuilder builder = new ProxyInfoBuilder()
        builder.host = proxyHost
        builder.port = proxyPort
        builder.username = username
        builder.password = password
        builder.ignoredProxyHosts = proxyIgnoredHosts
        final ProxyInfo proxyInfo1 = builder.build()
        final String maskedPassword = proxyInfo1.getMaskedPassword()
        assert proxyHost == proxyInfo1.host
        assert proxyPort == proxyInfo1.port
        assert proxyIgnoredHosts == proxyInfo1.ignoredProxyHosts

        assert maskedPassword.length() == password.length()
        assert password != maskedPassword
        assert StringUtils.containsOnly(maskedPassword, "*")

        assert builder.hasProxySettings()
    }

    @Test
    public void testEncryptedPasswordBuilder() {
        final String username = "username"
        final String password = "password"
        final String proxyHost = "proxyHost"
        final int proxyPort = 25
        final String proxyIgnoredHosts = ".*"
        final String encryptedPassword = PasswordEncrypter.encrypt(password)
        final ProxyInfoBuilder builder = new ProxyInfoBuilder()
        builder.host = proxyHost
        builder.port = proxyPort
        builder.username = username
        builder.password = encryptedPassword
        builder.passwordLength = password.length()
        builder.ignoredProxyHosts = proxyIgnoredHosts
        final ProxyInfo proxyInfo1 = builder.build()
        final String maskedPassword = proxyInfo1.getMaskedPassword()
        assert proxyHost == proxyInfo1.host
        assert proxyPort == proxyInfo1.port
        assert proxyIgnoredHosts == proxyInfo1.ignoredProxyHosts

        assert password != builder.password
        assert password.length() == builder.passwordLength
        assert maskedPassword.length() == password.length()
        assert password != maskedPassword
        assert StringUtils.containsOnly(maskedPassword, "*")

        assert builder.hasProxySettings()
    }

    @Test
    public void testUnauthenticatedBuilder() {
        final String proxyHost = "proxyHost"
        final int proxyPort = 25
        final String proxyIgnoredHosts = ".*"
        final ProxyInfoBuilder builder = new ProxyInfoBuilder()
        builder.host = proxyHost
        builder.port = proxyPort
        builder.ignoredProxyHosts = proxyIgnoredHosts
        final ProxyInfo proxyInfo1 = builder.build()
        final String maskedPassword = proxyInfo1.getMaskedPassword()
        assert proxyHost == proxyInfo1.host
        assert proxyPort == proxyInfo1.port
        assert proxyIgnoredHosts == proxyInfo1.ignoredProxyHosts
        assert builder.hasProxySettings()
    }
}
