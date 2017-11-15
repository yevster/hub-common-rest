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

import com.blackducksoftware.integration.hub.proxy.ProxyInfo

class ProxyInfoTest {

    private static final String VALID_URL = "http://www.google.com"

    @Test
    public void testProxyConstructor() {

        final String username1 = null
        final String password1 = null
        Credentials credentials1 = null
        final String proxyHost1 = null
        final int proxyPort1 = 0
        final String proxyIgnoredHosts1 = null
        ProxyInfo proxyInfo1 = new ProxyInfo(proxyHost1, proxyPort1, credentials1, proxyIgnoredHosts1)
        assert null == proxyInfo1.host
        assert 0 == proxyInfo1.port
        assert null == proxyInfo1.proxyCredentials
        assert null == proxyInfo1.ignoredProxyHosts

        username1 = "username"
        password1 = "password"
        credentials1 = new Credentials(username1, password1);
        proxyHost1 = "proxyHost"
        proxyPort1 = 25
        proxyIgnoredHosts1 = "*"
        proxyInfo1 = new ProxyInfo(proxyHost1, proxyPort1, credentials1, proxyIgnoredHosts1)
        final String maskedPassword = proxyInfo1.getMaskedPassword()
        assert proxyHost1 == proxyInfo1.host
        assert proxyPort1 == proxyInfo1.port
        assert credentials1 == proxyInfo1.proxyCredentials
        assert proxyIgnoredHosts1 == proxyInfo1.ignoredProxyHosts

        assert password1 != proxyInfo1.encryptedPassword
        assert password1.length() == proxyInfo1.actualPasswordLength
        assert maskedPassword.length() == password1.length()
        assert password1 != maskedPassword
        assert StringUtils.containsOnly(maskedPassword, "*")
    }

    @Test
    public void testOpenConnection() {
        final String username1 = "username"
        final String password1 = "password"
        Credentials credentials1 = new Credentials(username1, password1);
        final String proxyHost1 = "proxyHost"
        final int proxyPort1 = 25
        final String proxyIgnoredHosts1 = ".*"
        ProxyInfo proxyInfo1 = new ProxyInfo(proxyHost1, proxyPort1, credentials1, proxyIgnoredHosts1)

        proxyInfo1.openConnection(new URL(VALID_URL))
    }

    @Test
    public void testShouldUseProxy() {
        final String username1 = "username"
        final String password1 = "password"
        Credentials credentials1 = new Credentials(username1, password1);
        final String proxyHost1 = "proxyHost"
        final int proxyPort1 = 25
        String proxyIgnoredHosts1 = ""
        ProxyInfo proxyInfo1 = new ProxyInfo(proxyHost1, proxyPort1, credentials1, proxyIgnoredHosts1)

        assert true == proxyInfo1.shouldUseProxyForUrl(new URL(VALID_URL))

        proxyIgnoredHosts1 = ".*"
        proxyInfo1 = new ProxyInfo(proxyHost1, proxyPort1, credentials1, proxyIgnoredHosts1)
        final boolean result = proxyInfo1.shouldUseProxyForUrl(new URL(VALID_URL))
        assert !result
    }

    @Test
    public void testGetProxy() {
        final String username1 = "username"
        final String password1 = "password"
        Credentials credentials1 = new Credentials(username1, password1);
        final String proxyHost1 = "proxyHost"
        final int proxyPort1 = 25
        String proxyIgnoredHosts1 = ""
        ProxyInfo proxyInfo1 = new ProxyInfo(proxyHost1, proxyPort1, credentials1, proxyIgnoredHosts1)
        assert null != proxyInfo1.getProxy(new URL(VALID_URL))

        proxyIgnoredHosts1 = ".*"
        proxyInfo1 = new ProxyInfo(proxyHost1, proxyPort1, credentials1, proxyIgnoredHosts1)
        assert Proxy.NO_PROXY == proxyInfo1.getProxy(new URL(VALID_URL))
    }

    @Test
    public void testHashCode() {
        final String username1 = "username"
        final String password1 = "password"
        Credentials credentials1 = new Credentials(username1, password1);
        final String proxyHost1 = "proxyHost"
        final int proxyPort1 = 25
        final String proxyIgnoredHosts1 = "*"
        ProxyInfo proxyInfo1 = new ProxyInfo(proxyHost1, proxyPort1, credentials1, proxyIgnoredHosts1)


        final String username2 = "username"
        final String password2 = "password"
        Credentials credentials2 = new Credentials(username1, password1);
        final String proxyHost2 = "proxyHost"
        final int proxyPort2 = 25
        final String proxyIgnoredHosts2 = "*"
        ProxyInfo proxyInfo2 = new ProxyInfo(proxyHost2, proxyPort2, credentials2, proxyIgnoredHosts2)

        assert proxyInfo1.hashCode() == proxyInfo2.hashCode()
    }

    @Test
    public void testEquals() {
        final String username1 = "username"
        final String password1 = "password"
        Credentials credentials1 = new Credentials(username1, password1);
        final String proxyHost1 = "proxyHost"
        final int proxyPort1 = 25
        final String proxyIgnoredHosts1 = "*"
        ProxyInfo proxyInfo1 = new ProxyInfo(proxyHost1, proxyPort1, credentials1, proxyIgnoredHosts1)


        final String username2 = "username"
        final String password2 = "password"
        Credentials credentials2 = new Credentials(username1, password1);
        final String proxyHost2 = "proxyHost"
        final int proxyPort2 = 25
        final String proxyIgnoredHosts2 = "*"
        ProxyInfo proxyInfo2 = new ProxyInfo(proxyHost2, proxyPort2, credentials2, proxyIgnoredHosts2)

        assert proxyInfo1.equals(proxyInfo2)
    }

    @Test
    public void testToString() {
        final String username1 = "username"
        final String password1 = "password"
        Credentials credentials1 = new Credentials(username1, password1);
        final String proxyHost1 = "proxyHost"
        final int proxyPort1 = 25
        final String proxyIgnoredHosts1 = "*"
        ProxyInfo proxyInfo1 = new ProxyInfo(proxyHost1, proxyPort1, credentials1, proxyIgnoredHosts1)


        final String username2 = "username"
        final String password2 = "password"
        Credentials credentials2 = new Credentials(username1, password1);
        final String proxyHost2 = "proxyHost"
        final int proxyPort2 = 25
        final String proxyIgnoredHosts2 = "*"
        ProxyInfo proxyInfo2 = new ProxyInfo(proxyHost2, proxyPort2, credentials2, proxyIgnoredHosts2)

        assert proxyInfo1.toString() == proxyInfo2.toString()
    }
}
