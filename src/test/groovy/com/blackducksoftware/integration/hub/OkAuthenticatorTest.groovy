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

import javax.net.SocketFactory

import org.junit.Test

import com.blackducksoftware.integration.hub.proxy.OkAuthenticator

import okhttp3.Address
import okhttp3.Authenticator
import okhttp3.ConnectionSpec
import okhttp3.Dns
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route

class OkAuthenticatorTest {
    public static final String GOOGLE_URL_STRING = "https://www.google.com/"

    private Route mockRoute(java.net.Proxy proxy) {
        Address address = new Address('a', 1, Dns.SYSTEM, SocketFactory.getDefault(), null, null, null,
                Authenticator.NONE, null, Collections.<Protocol>emptyList(),
                Collections.<ConnectionSpec>emptyList(),
                ProxySelector.getDefault());

        new Route(address, proxy,
                InetSocketAddress.createUnresolved(address.url().host(), address.url().port()));
    }

    @Test
    public void testAuthenticateProxyBasic(){
        String proxyUser = "TestUser"
        String proxyPassword = "Password"
        OkAuthenticator authenticator = new OkAuthenticator(proxyUser,proxyPassword)
        Proxy proxy = new Proxy()
        def route = mockRoute(proxy)
        Response.Builder response = new Response.Builder()
        Request.Builder initialRequest = new Request.Builder()
        initialRequest.url(GOOGLE_URL_STRING)
        response.request(initialRequest.build())
        response.protocol(Protocol.HTTP_1_1)
        response.code(407)
        response.addHeader("Proxy-Authenticate", 'Basic realm="test proxy authentication"')
        Request request = authenticator.authenticate(route, response.build())
        assert null != request
        assert null != request.header(OkAuthenticator.PROXY_AUTH_RESP)
    }

    @Test
    public void testAuthenticateProxyBasicPriorAttempt(){
        String proxyUser = "TestUser"
        String proxyPassword = "Password"
        OkAuthenticator authenticator = new OkAuthenticator(proxyUser,proxyPassword)
        Proxy proxy = new Proxy()
        def route = mockRoute(proxy)
        Response.Builder previousResponseBuilder = new Response.Builder()
        Request.Builder initialRequest = new Request.Builder()
        initialRequest.url(GOOGLE_URL_STRING)
        previousResponseBuilder.request(initialRequest.build())
        previousResponseBuilder.protocol(Protocol.HTTP_1_1)
        previousResponseBuilder.code(407)
        previousResponseBuilder.addHeader("Proxy-Authenticate", 'Basic realm="test proxy authentication"')

        Response previousResponse = previousResponseBuilder.build()

        Response.Builder currentResponse = new Response.Builder()
        currentResponse.priorResponse(previousResponse)
        currentResponse.request(initialRequest.build())
        currentResponse.protocol(Protocol.HTTP_1_1)
        currentResponse.code(407)
        currentResponse.addHeader("Proxy-Authenticate", 'Basic realm="test proxy authentication"')

        Request request = authenticator.authenticate(route, currentResponse.build())
        assert null == request
    }

    @Test
    public void testAuthenticateDirectBasic(){
        String proxyUser = "TestUser"
        String proxyPassword = "Password"
        OkAuthenticator authenticator = new OkAuthenticator(proxyUser,proxyPassword)
        Proxy proxy = Proxy.NO_PROXY
        def route = mockRoute(proxy)
        Response.Builder response = new Response.Builder()
        Request.Builder initialRequest = new Request.Builder()
        initialRequest.url(GOOGLE_URL_STRING)
        response.request(initialRequest.build())
        response.protocol(Protocol.HTTP_1_1)
        response.code(401)
        response.addHeader("WWW-Authenticate", 'Basic realm="test authentication"')
        Request request = authenticator.authenticate(route, response.build())
        assert null != request
        assert null != request.header(OkAuthenticator.WWW_AUTH_RESP)
    }

    @Test
    public void testAuthenticateResponseSuccess(){
        String proxyUser = "TestUser"
        String proxyPassword = "Password"
        OkAuthenticator authenticator = new OkAuthenticator(proxyUser,proxyPassword)
        Proxy proxy = new Proxy()
        def route = mockRoute(proxy)
        Response.Builder response = new Response.Builder()
        Request.Builder initialRequest = new Request.Builder()
        initialRequest.url(GOOGLE_URL_STRING)
        response.request(initialRequest.build())
        response.protocol(Protocol.HTTP_1_1)
        response.code(200)
        Request request = authenticator.authenticate(route, response.build())
        assert null == request
    }

    @Test
    public void testAuthenticateResponseNoChallenge(){
        String proxyUser = "TestUser"
        String proxyPassword = "Password"
        OkAuthenticator authenticator = new OkAuthenticator(proxyUser,proxyPassword)
        Proxy proxy = new Proxy()
        def route = mockRoute(proxy)
        Response.Builder response = new Response.Builder()
        Request.Builder initialRequest = new Request.Builder()
        initialRequest.url(GOOGLE_URL_STRING)
        response.request(initialRequest.build())
        response.protocol(Protocol.HTTP_1_1)
        response.code(407)
        Request request = authenticator.authenticate(route, response.build())
        assert null == request
    }
}
