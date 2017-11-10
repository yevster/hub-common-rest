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

import org.junit.After
import org.junit.Before
import org.junit.Test

import com.blackducksoftware.integration.hub.api.oauth.OAuthConfiguration
import com.blackducksoftware.integration.hub.proxy.ProxyInfo
import com.blackducksoftware.integration.hub.rest.RestConnection
import com.blackducksoftware.integration.hub.rest.UnauthenticatedRestConnection
import com.blackducksoftware.integration.hub.rest.oauth.AccessType
import com.blackducksoftware.integration.hub.rest.oauth.OkOauthAuthenticator
import com.blackducksoftware.integration.hub.rest.oauth.TokenManager
import com.blackducksoftware.integration.log.LogLevel
import com.blackducksoftware.integration.log.PrintStreamIntLogger

import okhttp3.Address
import okhttp3.Authenticator
import okhttp3.ConnectionSpec
import okhttp3.Dns
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest

class OkOauthAuthenticatorTest {
    public static final int CONNECTION_TIMEOUT = 213

    private final MockWebServer server = new MockWebServer();

    @Before public void setUp() throws Exception {
        server.start();
    }

    @After public void tearDown() throws Exception {
        server.shutdown();
    }

    private String getClientTokenJson(){
        getJsonFileContent('ClientToken.json')
    }

    private String getUserTokenJson(){
        getJsonFileContent('UserToken.json')
    }

    private String getJsonFileContent(String fileName){
        getClass().getResource("/$fileName").text
    }

    private RestConnection getRestConnection(){
        getRestConnection(null)
    }

    private RestConnection getRestConnection(final MockResponse mockResponse){
        final Dispatcher dispatcher = new Dispatcher() {
                    @Override
                    public MockResponse dispatch(RecordedRequest request) throws InterruptedException {
                        MockResponse response = null
                        if(null != mockResponse){
                            response = mockResponse
                        } else{
                            String body = request.getBody().readUtf8()
                            if(body.contains("grant_type=authorization_code")){
                                response = new MockResponse().setResponseCode(200).setBody(getUserTokenJson())
                            } else  if(body.contains("grant_type=client_credentials")){
                                response = new MockResponse().setResponseCode(200).setBody(getClientTokenJson())
                            } else  if(body.contains("grant_type=refresh_token")){
                                response = new MockResponse().setResponseCode(200).setBody(getUserTokenJson())
                            } else {
                                response = new MockResponse().setResponseCode(200)
                            }
                        }
                        response
                    }
                };
        server.setDispatcher(dispatcher);
        new UnauthenticatedRestConnection(new PrintStreamIntLogger(System.out, LogLevel.TRACE), server.url("/").url(), CONNECTION_TIMEOUT, ProxyInfo.NO_PROXY_INFO)
    }

    private TokenManager getTokenManager(){
        OAuthConfiguration oAuthConfig = new OAuthConfiguration()
        oAuthConfig.clientId = 'ClientId'
        oAuthConfig.authorizeUri = server.url("/authorize/").toString()
        oAuthConfig.tokenUri = server.url("/token/").toString()
        oAuthConfig.callbackUrl = server.url("/callback/").toString()

        TokenManager tokenManager = new TokenManager(new PrintStreamIntLogger(System.out, LogLevel.TRACE), CONNECTION_TIMEOUT)
        tokenManager.setConfiguration(oAuthConfig)
        tokenManager
    }

    private Route mockRoute() {
        Address address = new Address('a', 1, Dns.SYSTEM, SocketFactory.getDefault(), null, null, null,
                Authenticator.NONE, null, Collections.<Protocol>emptyList(),
                Collections.<ConnectionSpec>emptyList(),
                ProxySelector.getDefault());

        new Route(address, Proxy.NO_PROXY,
                InetSocketAddress.createUnresolved(address.url().host(), address.url().port()));
    }


    @Test
    public void testAuthenticatePriorAttempt(){
        TokenManager tokenManager = getTokenManager()
        AccessType accessType = AccessType.CLIENT
        RestConnection restConnection = getRestConnection()
        OkOauthAuthenticator authenticator = new OkOauthAuthenticator(tokenManager,accessType, restConnection)
        def route = mockRoute()
        Response.Builder previousResponseBuilder = new Response.Builder()
        Request.Builder initialRequest = new Request.Builder()
        initialRequest.url(server.url("/").toString())
        previousResponseBuilder.request(initialRequest.build())
        previousResponseBuilder.protocol(Protocol.HTTP_1_1)
        previousResponseBuilder.code(200)

        Response previousResponse = previousResponseBuilder.build()

        Response.Builder currentResponse = new Response.Builder()
        currentResponse.priorResponse(previousResponse)
        currentResponse.request(initialRequest.build())
        currentResponse.protocol(Protocol.HTTP_1_1)
        currentResponse.code(200)

        Request request = authenticator.authenticate(route, currentResponse.build())
        assert null == request
    }

    @Test
    public void testAuthenticate(){
        TokenManager tokenManager = getTokenManager()
        AccessType accessType = AccessType.CLIENT
        RestConnection restConnection = getRestConnection()
        OkOauthAuthenticator authenticator = new OkOauthAuthenticator(tokenManager,accessType, restConnection)
        def route = mockRoute()
        Response.Builder response = new Response.Builder()
        Request.Builder initialRequest = new Request.Builder()
        initialRequest.url(server.url("/").toString())
        response.request(initialRequest.build())
        response.protocol(Protocol.HTTP_1_1)
        response.code(401)
        Request request = authenticator.authenticate(route, response.build())
        assert null != request
    }

    @Test
    public void testAuthenticateFail(){
        TokenManager tokenManager = getTokenManager()
        AccessType accessType = AccessType.CLIENT
        RestConnection restConnection = getRestConnection(new MockResponse().setResponseCode(404))
        OkOauthAuthenticator authenticator = new OkOauthAuthenticator(tokenManager,accessType, restConnection)
        def route = mockRoute()
        try{
            Response.Builder response = new Response.Builder()
            Request.Builder initialRequest = new Request.Builder()
            initialRequest.url(server.url("/").toString())
            response.request(initialRequest.build())
            response.protocol(Protocol.HTTP_1_1)
            response.code(401)

            authenticator.authenticate(route, response.build())
            fail('Should have thrown exception')
        } catch (Exception e){
            assert null != e
            assert 'Cannot refresh token'.equals(e.getMessage())
        }
    }
}
