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

import com.blackducksoftware.integration.hub.rest.CredentialsRestConnection
import com.blackducksoftware.integration.hub.rest.RestConnection
import com.blackducksoftware.integration.hub.rest.exception.IntegrationRestException
import com.blackducksoftware.integration.log.LogLevel
import com.blackducksoftware.integration.log.PrintStreamIntLogger

import okhttp3.HttpUrl
import okhttp3.MediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody

class CredentialsRestConnectionTest {
    public static final String GOOGLE_URL_STRING = "https://www.google.com/"

    public static final URL GOOGLE_URL = new URL(GOOGLE_URL_STRING)

    public static final int CONNECTION_TIMEOUT = 213

    private RestConnection getRestConnection(OkHttpClient mockClient){
        new CredentialsRestConnection(new PrintStreamIntLogger(System.out, LogLevel.INFO), GOOGLE_URL, 'TestUser', 'Password', CONNECTION_TIMEOUT){
                    @Override
                    public void setClient(final OkHttpClient client) {
                        super.setClient(mockClient)
                    }
                }
    }


    private Response getSuccessResponse(){
        Response.Builder builder = new Response.Builder()
        builder.code(200)
        builder.body(ResponseBody.create(MediaType.parse("text/plain"), "Hello"))
        new Response(builder)
    }

    private Response getUnauthorizedResponse(){
        Response.Builder builder = new Response.Builder()
        builder.code(401)
        builder.body(ResponseBody.create(MediaType.parse("text/plain"), "Hello"))
        new Response(builder)
    }

    private Response getFailureResponse(){
        Response.Builder builder = new Response.Builder()
        builder.code(404)
        builder.body(ResponseBody.create(MediaType.parse("text/plain"), "Hello"))
        new Response(builder)
    }

    private OkHttpClient getClient(Response mockResponse){
        def call = [execute: { -> mockResponse }, ] as okhttp3.Call
        [newCall: { Request request -> call }] as okhttp3.OkHttpClient
    }

    @Test
    public void testHandleExecuteClientCallSuccessful(){
        RestConnection restConnection = getRestConnection(getClient(getSuccessResponse()))
        HttpUrl httpUrl = restConnection.createHttpUrl()
        Request request = restConnection.createGetRequest(httpUrl)
        restConnection.handleExecuteClientCall(request).withCloseable{ assert 200 == it.code }
    }

    @Test
    public void testHandleExecuteClientCallUnauthorized(){
        RestConnection restConnection = getRestConnection(getClient(getUnauthorizedResponse()))
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
        RestConnection restConnection = getRestConnection(getClient(getFailureResponse()))
        HttpUrl httpUrl = restConnection.createHttpUrl()
        Request request = restConnection.createGetRequest(httpUrl)
        try{
            restConnection.handleExecuteClientCall(request)
            fail('Should have thrown exception')
        } catch (IntegrationRestException e) {
            assert 404 == e.httpStatusCode
        }
    }

    @Test
    public void testClientForCookieJar(){
        RestConnection restConnection = new CredentialsRestConnection(new PrintStreamIntLogger(System.out, LogLevel.INFO), GOOGLE_URL, null, null, 213)
        HttpUrl httpUrl = restConnection.createHttpUrl()
        Request request = restConnection.createGetRequest(httpUrl)
        restConnection.handleExecuteClientCall(request)
        assert null != restConnection.client.cookieJar
    }
}
