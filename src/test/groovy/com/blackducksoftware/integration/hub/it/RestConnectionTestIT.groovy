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
package com.blackducksoftware.integration.hub.it

import static org.junit.Assert.assertEquals
import static org.junit.Assert.assertFalse
import static org.junit.Assert.assertTrue
import static org.junit.Assert.fail

import org.apache.commons.lang3.math.NumberUtils
import org.junit.Rule
import org.junit.Test
import org.junit.rules.ExpectedException
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import com.blackducksoftware.integration.exception.IntegrationException
import com.blackducksoftware.integration.hub.certificate.CertificateHandler
import com.blackducksoftware.integration.hub.request.HubRequest
import com.blackducksoftware.integration.hub.rest.CredentialsRestConnection
import com.blackducksoftware.integration.hub.rest.RestConnection
import com.blackducksoftware.integration.log.LogLevel
import com.blackducksoftware.integration.log.PrintStreamIntLogger

class RestConnectionTestIT {

    private final Logger logger = LoggerFactory.getLogger(RestConnectionTestIT.class)

    private static RestConnectionTestHelper restConnectionTestHelper = new RestConnectionTestHelper()

    @Rule
    public ExpectedException exception = ExpectedException.none()

    @Test
    public void testTimeoutSet() {
        final RestConnection restConnection = restConnectionTestHelper.getRestConnection()
        restConnection.timeout = 459
        assertEquals(459, restConnection.timeout)
    }

    @Test
    public void testBasicProxyWithHttp() {
        try {
            final CredentialsRestConnection restConnection = restConnectionTestHelper.getRestConnection()
            restConnection.proxyHost = restConnectionTestHelper.getProperty("TEST_PROXY_HOST_BASIC")
            restConnection.proxyPort = NumberUtils.toInt(restConnectionTestHelper.getProperty("TEST_PROXY_PORT_BASIC"))
            restConnection.proxyUsername = restConnectionTestHelper.getProperty("TEST_PROXY_USER_BASIC")
            restConnection.proxyPassword = restConnectionTestHelper.getProperty("TEST_PROXY_PASSWORD_BASIC")

            restConnection.connect()
        } catch (final Exception e) {
            fail("No exception should be thrown with a valid config: " + e.getMessage())
        }
    }

    @Test
    public void testBasicProxyFailsWithoutCredentialsWithHttp() {
        try {
            final CredentialsRestConnection restConnection = restConnectionTestHelper.getRestConnection()
            restConnection.proxyHost = restConnectionTestHelper.getProperty("TEST_PROXY_HOST_BASIC")
            restConnection.proxyPort = NumberUtils.toInt(restConnectionTestHelper.getProperty("TEST_PROXY_PORT_BASIC"))
            restConnection.connect()
            fail("An exception should be thrown")
        } catch (final Exception e) {
            assertFalse(e.getMessage(), e.getMessage().contains("Can not reach this server"))
            assertTrue(e.getMessage(), e.getMessage().contains("Proxy Authentication Required"))
        }
    }

    @Test
    public void testPassthroughProxyWithHttp() {
        try {
            final CredentialsRestConnection restConnection = restConnectionTestHelper.getRestConnection()
            restConnection.proxyHost = restConnectionTestHelper.getProperty("TEST_PROXY_HOST_PASSTHROUGH")
            restConnection.proxyPort = NumberUtils.toInt(restConnectionTestHelper.getProperty("TEST_PROXY_PORT_PASSTHROUGH"))
            restConnection.connect()
        } catch (final Exception e) {
            fail("No exception should be thrown with a valid config: " + e.getMessage())
        }
    }

    @Test
    public void testUnauthorizedGet() throws Exception {
        final URL url = new URL(restConnectionTestHelper.getProperty("TEST_HUB_SERVER_URL"))
        final CredentialsRestConnection restConnection = new CredentialsRestConnection(new PrintStreamIntLogger(System.out, LogLevel.INFO), url, "notavalidusername", "notavalidpassword", 120)

        final HubRequest hubRequest = new HubRequest(restConnection)
        hubRequest.url = url.toString() + "/api/notifications?offset=0&endDate=2017-01-25T18:43:46.685Z&limit=100&startDate=2017-01-17T21:19:33.311Z"
        System.out.println("Executing: " + hubRequest.toString())
        try {
            hubRequest.executeGet()
            fail("Expected Unauthorized Exception")
        } catch (final Exception e) {
            assertTrue(e.getMessage().contains("Unauthorized"))
        }
    }

    @Test
    public void testTLS() throws Exception {
        final URL url = new URL(restConnectionTestHelper.getProperty("TEST_HTTPS_HUB_SERVER_URL"))
        final CertificateHandler handler = new CertificateHandler(new PrintStreamIntLogger(System.out, LogLevel.DEBUG))
        boolean skipTest = true
        if (!handler.isCertificateInTrustStore(url)) {
            try {
                handler.retrieveAndImportHttpsCertificate(url)
                skipTest = false
            } catch (final IntegrationException e) {
                logger.warn("The certificate can not be auto imported: " + e.getMessage(), e)
            }
        }
        if (skipTest) {
            return
        }

        final CredentialsRestConnection restConnection = new CredentialsRestConnection(new PrintStreamIntLogger(System.out, LogLevel.INFO), url, "sysadmin", "blackduck", 120)

        final HubRequest hubRequest = new HubRequest(restConnection)
        hubRequest.url = url.toString() + "/api/projects"
        System.out.println("Executing: " + hubRequest.toString())
        hubRequest.executeGet()
    }
}
