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

import org.junit.After
import org.junit.Before
import org.junit.Test

import com.blackducksoftware.integration.hub.api.oauth.OAuthConfiguration
import com.blackducksoftware.integration.hub.rest.RestConnection
import com.blackducksoftware.integration.hub.rest.oauth.AccessType
import com.blackducksoftware.integration.hub.rest.oauth.OAuthRestConnection
import com.blackducksoftware.integration.hub.rest.oauth.OkOauthAuthenticator
import com.blackducksoftware.integration.hub.rest.oauth.TokenManager
import com.blackducksoftware.integration.log.LogLevel
import com.blackducksoftware.integration.log.PrintStreamIntLogger

import okhttp3.HttpUrl
import okhttp3.Request
import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest

class OAuthRestConnectionTest {
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

    private TokenManager getTokenManager(){
        getTokenManager(null, null)
    }

    private TokenManager getTokenManager(String refreshToken){
        getTokenManager(null, refreshToken)
    }

    private TokenManager getTokenManager(MockResponse mockResponse){
        getTokenManager(mockResponse, null)
    }

    private TokenManager getTokenManager(MockResponse mockResponse, String refreshToken){
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
        OAuthConfiguration oAuthConfig = new OAuthConfiguration()
        oAuthConfig.clientId = 'ClientId'
        oAuthConfig.authorizeUri = server.url("/authorize/").toString()
        oAuthConfig.tokenUri = server.url("/token/").toString()
        oAuthConfig.callbackUrl = server.url("/callback/").toString()
        oAuthConfig.refreshToken = refreshToken

        TokenManager tokenManager = new TokenManager(new PrintStreamIntLogger(System.out, LogLevel.TRACE), CONNECTION_TIMEOUT)
        tokenManager.setConfiguration(oAuthConfig)
        tokenManager
    }

    private RestConnection getRestConnection(TokenManager tokenManager, AccessType accessType){
        new OAuthRestConnection(new PrintStreamIntLogger(System.out, LogLevel.TRACE), server.url("/").url(), CONNECTION_TIMEOUT, tokenManager, accessType)
    }


    @Test
    public void testHandleExecuteClientCallSuccessful(){
        OAuthRestConnection restConnection = getRestConnection(getTokenManager(), AccessType.CLIENT)
        HttpUrl httpUrl = restConnection.createHttpUrl()
        Request request = restConnection.createGetRequest(httpUrl)
        restConnection.handleExecuteClientCall(request).withCloseable{ assert 200 == it.code }
        assert null != restConnection.client.authenticator
        assert OkOauthAuthenticator.class == restConnection.client.authenticator.getClass()
    }
}
