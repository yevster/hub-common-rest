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

import com.blackducksoftware.integration.exception.IntegrationException
import com.blackducksoftware.integration.hub.api.oauth.OAuthConfiguration
import com.blackducksoftware.integration.hub.api.oauth.Token
import com.blackducksoftware.integration.hub.rest.exception.IntegrationRestException
import com.blackducksoftware.integration.hub.rest.oauth.AccessType
import com.blackducksoftware.integration.hub.rest.oauth.TokenManager
import com.blackducksoftware.integration.log.LogLevel
import com.blackducksoftware.integration.log.PrintStreamIntLogger

import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest

class TokenManagerTest {
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

    @Test
    public void testCreateTokenCredential(){
        TokenManager tokenManager = getTokenManager()
        assert null != tokenManager.getLogger()
        assert null != tokenManager.getTimeout()
        assert null != tokenManager.getConfiguration()
        String token = 'test'
        String output = tokenManager.createTokenCredential(token)
        assert null != output
        String expected = "Bearer $token"
        assert expected.equals(output)
    }

    @Test
    public void testExchangeForUserToken(){
        TokenManager tokenManager = getTokenManager()
        String authCode = 'AuthCode'
        Token token = tokenManager.exchangeForUserToken(authCode)
        assert null != token
        assert null != token.accessToken
        assert null != token.refreshToken
        assert null != token.tokenType
        assert null != token.expiresIn
        assert null != token.scope
        assert null != token.jti

        tokenManager = getTokenManager(new MockResponse().setResponseCode(404))
        try{
            tokenManager.exchangeForUserToken(authCode)
            fail('Should have thrown exception')
        } catch (IntegrationException e){
            assert 'Error refreshing client token'.equals(e.getMessage())
            IntegrationRestException restException = e.getCause()
            assert 404 == restException.httpStatusCode
        }
    }

    @Test
    public void testRefreshToken(){
        TokenManager tokenManager = getTokenManager()
        try{
            tokenManager.refreshToken(AccessType.USER)
            fail('Should have thrown exception')
        } catch (IntegrationException e){
            assert "No token present to refresh".equals(e.getMessage())
        }
        String refreshToken = 'RefreshToken'
        tokenManager = getTokenManager(refreshToken)
        Token token = tokenManager.refreshToken(AccessType.USER)
        assert null != token
        assert null != token
        assert null != token.accessToken
        assert null != token.refreshToken
        assert null != token.tokenType
        assert null != token.expiresIn
        assert null != token.scope
        assert null != token.jti

        assert null ==  tokenManager.clientToken

        token = tokenManager.refreshToken(AccessType.CLIENT)
        Token storedClientToken = token
        assert null != token
        assert null != token
        assert null != token.accessToken
        assert null == token.refreshToken
        assert null != token.tokenType
        assert null != token.expiresIn
        assert null != token.scope
        assert null != token.jti

        assert null !=  tokenManager.clientToken

        token = tokenManager.refreshToken(AccessType.CLIENT)
        assert null != token
        assert storedClientToken != token

        tokenManager = getTokenManager(new MockResponse().setResponseCode(404))
        try{
            tokenManager.refreshToken(AccessType.CLIENT)
            fail('Should have thrown exception')
        } catch (IntegrationException e){
            assert 'Error refreshing client token'.equals(e.getMessage())
            IntegrationRestException restException = e.getCause()
            assert 404 == restException.httpStatusCode
        }
        tokenManager = getTokenManager(new MockResponse().setResponseCode(404), refreshToken)
        try{
            tokenManager.refreshToken(AccessType.USER)
            fail('Should have thrown exception')
        } catch (IntegrationException e){
            assert 'Error refreshing user token'.equals(e.getMessage())
            IntegrationRestException restException = e.getCause()
            assert 404 == restException.httpStatusCode
        }
    }

    @Test
    public void testGetToken(){
        TokenManager tokenManager = getTokenManager()
        try{
            tokenManager.getToken(AccessType.USER)
            fail('Should have thrown exception')
        } catch (IntegrationException e){
            assert "No token present to refresh".equals(e.getMessage())
        }
        String refreshToken = 'RefreshToken'
        tokenManager = getTokenManager(refreshToken)
        Token token = tokenManager.getToken(AccessType.USER)
        assert null != token
        assert null != token
        assert null != token.accessToken
        assert null != token.refreshToken
        assert null != token.tokenType
        assert null != token.expiresIn
        assert null != token.scope
        assert null != token.jti

        assert null ==  tokenManager.clientToken

        token = tokenManager.getToken(AccessType.CLIENT)
        Token storedClientToken = token
        assert null != token
        assert null != token
        assert null != token.accessToken
        assert null == token.refreshToken
        assert null != token.tokenType
        assert null != token.expiresIn
        assert null != token.scope
        assert null != token.jti

        assert null !=  tokenManager.clientToken

        token = tokenManager.getToken(AccessType.CLIENT)
        assert null != token
        assert storedClientToken == token

        tokenManager = getTokenManager(new MockResponse().setResponseCode(404))
        try{
            tokenManager.getToken(AccessType.CLIENT)
            fail('Should have thrown exception')
        } catch (IntegrationException e){
            assert 'Error refreshing client token'.equals(e.getMessage())
            IntegrationRestException restException = e.getCause()
            assert 404 == restException.httpStatusCode
        }
    }
}
