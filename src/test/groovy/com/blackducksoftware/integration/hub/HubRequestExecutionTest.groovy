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

import com.blackducksoftware.integration.hub.request.HubRequest
import com.blackducksoftware.integration.hub.request.HubRequestFactory
import com.blackducksoftware.integration.hub.rest.RestConnection
import com.blackducksoftware.integration.hub.rest.UnauthenticatedRestConnection
import com.blackducksoftware.integration.hub.rest.exception.IntegrationRestException
import com.blackducksoftware.integration.log.LogLevel
import com.blackducksoftware.integration.log.PrintStreamIntLogger

class HubRequestExecutionTest {
    public static final String GOOGLE_URL_STRING = "https://www.google.com/"

    public static final URL GOOGLE_URL = new URL(GOOGLE_URL_STRING)

    public static final int CONNECTION_TIMEOUT = 213

    private RestConnection getRestConnection(){
        new UnauthenticatedRestConnection(new PrintStreamIntLogger(System.out, LogLevel.INFO), GOOGLE_URL, CONNECTION_TIMEOUT)
    }

    @Test
    public void testExecuteGet(){
        HubRequestFactory hubRequestFactory = new HubRequestFactory(getRestConnection())
        HubRequest request = hubRequestFactory.createRequest()
        request.executeGet().withCloseable{ assert 200 == it.code }
    }

    @Test
    public void testExecutePagedGet(){
        HubRequestFactory hubRequestFactory = new HubRequestFactory(getRestConnection())
        HubRequest request = hubRequestFactory.createPagedRequest(GOOGLE_URL_STRING)
        request.executeGet().withCloseable{ assert 200 == it.code }
    }

    @Test
    public void testExecuteEncodedFormPost(){
        HubRequestFactory hubRequestFactory = new HubRequestFactory(getRestConnection())
        def formBody = [name:"hello"]
        HubRequest request = hubRequestFactory.createRequest()
        try{
            request.executeEncodedFormPost(formBody)
        } catch (IntegrationRestException e){
            assert 405 == e.httpStatusCode
        }
    }

    @Test
    public void testExecutePost(){
        HubRequestFactory hubRequestFactory = new HubRequestFactory(getRestConnection())
        HubRequest request = hubRequestFactory.createRequest()
        try{
            request.executePost("hello")
        } catch (IntegrationRestException e){
            assert 405 == e.httpStatusCode
        }
    }

    @Test
    public void testExecutePostWithMediaType(){
        HubRequestFactory hubRequestFactory = new HubRequestFactory(getRestConnection())
        HubRequest request = hubRequestFactory.createRequest()
        try{
            request.executePost("text/plain", "hello")
        } catch (IntegrationRestException e){
            assert 405 == e.httpStatusCode
        }
    }

    @Test
    public void testExecuteEncodedFormPut(){
        HubRequestFactory hubRequestFactory = new HubRequestFactory(getRestConnection())
        def formBody = [name:"hello"]
        HubRequest request = hubRequestFactory.createRequest()
        try{
            request.executeEncodedFormPut(formBody)
        } catch (IntegrationRestException e){
            assert 405 == e.httpStatusCode
        }
    }

    @Test
    public void testExecutePut(){
        HubRequestFactory hubRequestFactory = new HubRequestFactory(getRestConnection())
        HubRequest request = hubRequestFactory.createRequest()
        try{
            request.executePut("hello")
        } catch (IntegrationRestException e){
            assert 405 == e.httpStatusCode
        }
    }

    @Test
    public void testExecutePutWithMediaType(){
        HubRequestFactory hubRequestFactory = new HubRequestFactory(getRestConnection())
        HubRequest request = hubRequestFactory.createRequest()
        try{
            request.executePut("text/plain", "hello")
        } catch (IntegrationRestException e){
            assert 405 == e.httpStatusCode
        }
    }

    @Test
    public void testExecuteDelete(){
        HubRequestFactory hubRequestFactory = new HubRequestFactory(getRestConnection())
        HubRequest request = hubRequestFactory.createRequest()
        try{
            request.executeDelete()
        } catch (IntegrationRestException e){
            assert 405 == e.httpStatusCode
        }
    }
}
