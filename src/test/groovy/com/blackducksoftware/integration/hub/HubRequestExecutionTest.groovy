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

import com.blackducksoftware.integration.hub.request.HubPagedRequest
import com.blackducksoftware.integration.hub.request.HubRequest
import com.blackducksoftware.integration.hub.request.HubRequestFactory
import com.blackducksoftware.integration.hub.rest.RestConnection
import com.blackducksoftware.integration.hub.rest.UnauthenticatedRestConnection
import com.blackducksoftware.integration.hub.rest.exception.IntegrationRestException
import com.blackducksoftware.integration.log.LogLevel
import com.blackducksoftware.integration.log.PrintStreamIntLogger

import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest

class HubRequestExecutionTest {
    public static final int CONNECTION_TIMEOUT = 213

    private final MockWebServer server = new MockWebServer();

    @Before public void setUp() throws Exception {
        server.start();
    }

    @After public void tearDown() throws Exception {
        server.shutdown();
    }

    private RestConnection getRestConnection(){
        getRestConnection(new MockResponse().setResponseCode(200))
    }

    private RestConnection getRestConnection(MockResponse response){
        if(null != response){
            final Dispatcher dispatcher = new Dispatcher() {
                        @Override
                        public MockResponse dispatch(RecordedRequest request) throws InterruptedException {
                            response
                        }
                    };
            server.setDispatcher(dispatcher);
        }
        new UnauthenticatedRestConnection(new PrintStreamIntLogger(System.out, LogLevel.TRACE), server.url("/").url(), CONNECTION_TIMEOUT)
    }

    @Test
    public void testExecuteGet(){
        HubRequestFactory hubRequestFactory = new HubRequestFactory(getRestConnection())
        HubRequest request = hubRequestFactory.createRequest()
        request.executeGet().withCloseable{ assert 200 == it.code }
    }

    @Test
    public void testExecuteGetWithQuery(){
        HubRequestFactory hubRequestFactory = new HubRequestFactory(getRestConnection())
        HubRequest request = hubRequestFactory.createRequest()
        request.q = "query"
        request.executeGet().withCloseable{ assert 200 == it.code }
    }

    @Test
    public void testExecutePagedGet(){
        HubRequestFactory hubRequestFactory = new HubRequestFactory(getRestConnection())
        HubPagedRequest request = hubRequestFactory.createPagedRequest(server.url("/").toString())
        request.executeGet().withCloseable{ assert 200 == it.code }

        request = hubRequestFactory.createPagedRequest(-245, server.url("/").toString())
        request.executeGet().withCloseable{
            assert 200 == it.code
            assert it.request.url.url.contains('limit=10')
        }
    }

    @Test
    public void testExecuteEncodedFormPost(){
        HubRequestFactory hubRequestFactory = new HubRequestFactory(getRestConnection())
        def formBody = [name:"hello"]
        HubRequest request = hubRequestFactory.createRequest()
        request.executeEncodedFormPost(formBody).withCloseable{ assert 200 == it.code }
    }

    @Test
    public void testExecutePost(){
        HubRequestFactory hubRequestFactory = new HubRequestFactory(getRestConnection())
        HubRequest request = hubRequestFactory.createRequest()
        request.executePost("hello").withCloseable{ assert 200 == it.code }
    }

    @Test
    public void testExecutePostWithMediaType(){
        HubRequestFactory hubRequestFactory = new HubRequestFactory(getRestConnection())
        HubRequest request = hubRequestFactory.createRequest()
        request.executePost("text/plain", "hello").withCloseable{ assert 200 == it.code }
    }

    @Test
    public void testExecuteEncodedFormPut(){
        HubRequestFactory hubRequestFactory = new HubRequestFactory(getRestConnection())
        def formBody = [name:"hello"]
        HubRequest request = hubRequestFactory.createRequest()
        request.executeEncodedFormPut(formBody).withCloseable{ assert 200 == it.code }
    }

    @Test
    public void testExecutePut(){
        HubRequestFactory hubRequestFactory = new HubRequestFactory(getRestConnection())
        HubRequest request = hubRequestFactory.createRequest()
        request.executePut("hello").withCloseable{ assert 200 == it.code }
    }

    @Test
    public void testExecutePutWithMediaType(){
        HubRequestFactory hubRequestFactory = new HubRequestFactory(getRestConnection())
        HubRequest request = hubRequestFactory.createRequest()
        request.executePut("text/plain", "hello").withCloseable{ assert 200 == it.code }
    }

    @Test
    public void testExecuteDelete(){
        HubRequestFactory hubRequestFactory = new HubRequestFactory(getRestConnection())
        HubRequest request = hubRequestFactory.createRequest()
        request.executeDelete()
    }

    @Test
    public void testExecuteDeleteNotAllowed(){
        MockResponse response = new MockResponse().setResponseCode(405)
        HubRequestFactory hubRequestFactory = new HubRequestFactory(getRestConnection(response))
        HubRequest request = hubRequestFactory.createRequest()
        try{
            request.executeDelete()
            fail('Should have thrown exception')
        } catch (IntegrationRestException e){
            assert 405 == e.httpStatusCode
        }
    }
}
