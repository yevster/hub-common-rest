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

import com.blackducksoftware.integration.hub.api.oauth.Token
import com.blackducksoftware.integration.hub.rest.RestConnection
import com.blackducksoftware.integration.hub.rest.UnauthenticatedRestConnection
import com.blackducksoftware.integration.hub.rest.exception.IntegrationRestException
import com.blackducksoftware.integration.hub.service.HubOAuthTokenService
import com.blackducksoftware.integration.log.LogLevel
import com.blackducksoftware.integration.log.PrintStreamIntLogger

import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest

class HubOAuthTokenServiceTest {
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

    private RestConnection getRestConnection(MockResponse mockResponse){
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

        new UnauthenticatedRestConnection(new PrintStreamIntLogger(System.out, LogLevel.TRACE),server.url("/").url(), CONNECTION_TIMEOUT)
    }

    @Test
    public void testRequestUserToken(){
        String clientId = "ClientId"
        String clientSecret = "ClientSecret"
        String authCode = "AuthCode"
        String redirectUri = "RedirectUri"
        HubOAuthTokenService tokenService = new HubOAuthTokenService(getRestConnection())
        Token token = tokenService.requestUserToken(clientId, authCode, redirectUri)
        assert null != token
        assert null != token.accessToken
        assert null != token.refreshToken
        assert null != token.tokenType
        assert null != token.expiresIn
        assert null != token.scope
        assert null != token.jti

        token = tokenService.requestUserToken(clientId, clientSecret, authCode, redirectUri)
        assert null != token
        assert null != token.accessToken
        assert null != token.refreshToken
        assert null != token.tokenType
        assert null != token.expiresIn
        assert null != token.scope
        assert null != token.jti

        tokenService = new HubOAuthTokenService(getRestConnection(new MockResponse().setResponseCode(404)))
        try{
            tokenService.requestUserToken(clientId, clientSecret, authCode, redirectUri)
            fail('Should have thrown exception')
        } catch (IntegrationRestException e){
            assert 404 == e.httpStatusCode
        }
    }

    @Test
    public void testRefreshClientToken(){
        String clientId = "ClientId"
        String clientSecret = "ClientSecret"
        HubOAuthTokenService tokenService = new HubOAuthTokenService(getRestConnection())
        Token token = tokenService.refreshClientToken(clientId)
        assert null != token
        assert null != token.accessToken
        assert null == token.refreshToken
        assert null != token.tokenType
        assert null != token.expiresIn
        assert null != token.scope
        assert null != token.jti

        token = tokenService.refreshClientToken(clientId, clientSecret)
        assert null != token
        assert null != token.accessToken
        assert null == token.refreshToken
        assert null != token.tokenType
        assert null != token.expiresIn
        assert null != token.scope
        assert null != token.jti

        tokenService = new HubOAuthTokenService(getRestConnection(new MockResponse().setResponseCode(404)))
        try{
            tokenService.refreshClientToken(clientId, clientSecret)
            fail('Should have thrown exception')
        } catch (IntegrationRestException e){
            assert 404 == e.httpStatusCode
        }
    }

    @Test
    public void testRefreshUserToken(){
        String clientId = "ClientId"
        String clientSecret = "ClientSecret"
        String refreshToken = "RefreshToken"
        HubOAuthTokenService tokenService = new HubOAuthTokenService(getRestConnection())
        Token token = tokenService.refreshUserToken(clientId, refreshToken)
        assert null != token
        assert null != token.accessToken
        assert null != token.refreshToken
        assert null != token.tokenType
        assert null != token.expiresIn
        assert null != token.scope
        assert null != token.jti

        token = tokenService.refreshUserToken(clientId, clientSecret, refreshToken)
        assert null != token
        assert null != token.accessToken
        assert null != token.refreshToken
        assert null != token.tokenType
        assert null != token.expiresIn
        assert null != token.scope
        assert null != token.jti

        tokenService = new HubOAuthTokenService(getRestConnection(new MockResponse().setResponseCode(404)))
        try{
            tokenService.refreshUserToken(clientId, clientSecret, refreshToken)
            fail('Should have thrown exception')
        } catch (IntegrationRestException e){
            assert 404 == e.httpStatusCode
        }
    }
}
