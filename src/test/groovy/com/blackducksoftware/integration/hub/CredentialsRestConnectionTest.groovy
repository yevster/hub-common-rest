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

import com.blackducksoftware.integration.hub.rest.CredentialsRestConnection
import com.blackducksoftware.integration.hub.rest.RestConnection
import com.blackducksoftware.integration.hub.rest.exception.IntegrationRestException
import com.blackducksoftware.integration.log.LogLevel
import com.blackducksoftware.integration.log.PrintStreamIntLogger

import okhttp3.HttpUrl
import okhttp3.Request
import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest

class CredentialsRestConnectionTest {
    public static final int CONNECTION_TIMEOUT = 213

    private final MockWebServer server = new MockWebServer();

    @Before public void setUp() throws Exception {
        server.start();
    }

    @After public void tearDown() throws Exception {
        server.shutdown();
    }

    private RestConnection getRestConnection(MockResponse response){
        final Dispatcher dispatcher = new Dispatcher() {
                    @Override
                    public MockResponse dispatch(RecordedRequest request) throws InterruptedException {
                        response
                    }
                };
        server.setDispatcher(dispatcher);
        new CredentialsRestConnection(new PrintStreamIntLogger(System.out, LogLevel.TRACE), server.url("/").url(), 'TestUser', 'Password', CONNECTION_TIMEOUT)
    }

    private MockResponse getSuccessResponse(){
        new MockResponse()
                .addHeader("Content-Type", "text/plain")
                .setBody("Hello").setResponseCode(200);
    }

    private MockResponse getUnauthorizedResponse(){
        new MockResponse()
                .addHeader("Content-Type", "text/plain")
                .setBody("Hello").setResponseCode(401);
    }

    private MockResponse getFailureResponse(){
        new MockResponse()
                .addHeader("Content-Type", "text/plain")
                .setBody("Hello").setResponseCode(404);
    }

    @Test
    public void testFollowRedirect(){
        RestConnection restConnection = getRestConnection(getSuccessResponse())
        HttpUrl httpUrl = restConnection.createHttpUrl()
        Request request = restConnection.createGetRequest(httpUrl)
        restConnection.handleExecuteClientCall(request).withCloseable{ assert 200 == it.code }
        assert !restConnection.followRedirects
        assert !restConnection.client.followRedirects

        restConnection.followRedirects = true
        restConnection.handleExecuteClientCall(request).withCloseable{ assert 200 == it.code }
        assert restConnection.followRedirects
        assert !restConnection.client.followRedirects

        restConnection.connect()
        assert restConnection.followRedirects
        assert restConnection.client.followRedirects

        restConnection.followRedirects = false
        restConnection.handleExecuteClientCall(request).withCloseable{ assert 200 == it.code }
        assert !restConnection.followRedirects
        assert restConnection.client.followRedirects

        restConnection.connect()
        assert !restConnection.followRedirects
        assert !restConnection.client.followRedirects
    }

    @Test
    public void testHandleExecuteClientCallSuccessful(){
        RestConnection restConnection = getRestConnection(getSuccessResponse())
        HttpUrl httpUrl = restConnection.createHttpUrl()
        Request request = restConnection.createGetRequest(httpUrl)
        restConnection.handleExecuteClientCall(request).withCloseable{ assert 200 == it.code }
        assert null != restConnection.client.cookieJar
    }

    @Test
    public void testHandleExecuteClientCallUnauthorized(){
        RestConnection restConnection = getRestConnection(getUnauthorizedResponse())
        HttpUrl httpUrl = restConnection.createHttpUrl()
        Request request = restConnection.createGetRequest(httpUrl)
        try{
            restConnection.handleExecuteClientCall(request)
            fail('Should have thrown exception')
        } catch (IntegrationRestException e) {
            assert 401 == e.httpStatusCode
        }
    }


    @Test
    public void testHandleExecuteClientCallFail(){
        RestConnection restConnection = getRestConnection(getFailureResponse())
        HttpUrl httpUrl = restConnection.createHttpUrl()
        Request request = restConnection.createGetRequest(httpUrl)
        try{
            restConnection.handleExecuteClientCall(request)
            fail('Should have thrown exception')
        } catch (IntegrationRestException e) {
            assert 404 == e.httpStatusCode
        }
    }
}