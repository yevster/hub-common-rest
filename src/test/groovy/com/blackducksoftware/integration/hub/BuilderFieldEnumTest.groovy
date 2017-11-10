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

import org.junit.Test

import com.blackducksoftware.integration.hub.proxy.ProxyInfoFieldEnum
import com.blackducksoftware.integration.hub.rest.RestConnectionFieldEnum
import com.blackducksoftware.integration.hub.rest.oauth.OauthRestConnectionFieldEnum

class BuilderFieldEnumTest {

    @Test
    public void testRestConnectionFieldEnum() {
        assert "restConnectionUrl" == RestConnectionFieldEnum.URL.getKey()
        assert "restConnectionTimeout" == RestConnectionFieldEnum.TIMEOUT.getKey()
        assert "restConnectionLogger" == RestConnectionFieldEnum.LOGGER.getKey()
        assert "restConnectionHeaders" == RestConnectionFieldEnum.COMMON_HEADERS.getKey()
    }

    @Test
    public void testCredentialsFieldEnum() {
        assert "username" == CredentialsFieldEnum.USERNAME.getKey()
        assert "password" == CredentialsFieldEnum.PASSWORD.getKey()
    }

    @Test
    public void testProxyInfoFieldEnum() {
        ProxyInfoFieldEnum

        assert "proxyHost" == ProxyInfoFieldEnum.PROXYHOST.getKey();
        assert "proxyPort" == ProxyInfoFieldEnum.PROXYPORT.getKey();
        assert "proxyUsername" == ProxyInfoFieldEnum.PROXYUSERNAME.getKey();
        assert "proxyPassword" == ProxyInfoFieldEnum.PROXYPASSWORD.getKey();
        assert "noProxyHosts" == ProxyInfoFieldEnum.NOPROXYHOSTS.getKey();
    }

    @Test
    public void testOAuthRestConnectionFieldEnum() {
        assert "oauthAccessType" == OauthRestConnectionFieldEnum.ACCESSTYPE.getKey()
        assert "oauthTokenManager" == OauthRestConnectionFieldEnum.TOKENMANAGER.getKey()
    }
}
