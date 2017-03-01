/**
 * Hub Rest Common
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

import com.blackducksoftware.integration.hub.rest.RestConnection
import com.blackducksoftware.integration.hub.rest.UnauthenticatedRestConnection
import com.blackducksoftware.integration.log.IntLogger
import com.blackducksoftware.integration.log.LogLevel
import com.blackducksoftware.integration.log.PrintStreamIntLogger

import okhttp3.OkHttpClient
import okhttp3.RequestBody
import okhttp3.FormBody
import okhttp3.HttpUrl
import okhttp3.Request

class RestConnectionTest {
    public static final String GOOGLE_URL_STRING = "https://www.google.com/"
    public static final URL GOOGLE_URL = new URL(GOOGLE_URL_STRING)

    //    private Response getSuccessfulResponse(){
    //        Response.Builder builder = Response.newBuilder()
    //        builder.code(404)
    //        new Response(builder)
    //    }
    //
    //    private OkHttpClient getClient(Response mockResponse){
    //        def call =  [execute: { -> mockResponse }] as okhttp3.Call
    //        [newCall: { Request request -> call }] as okhttp3.Call
    //    }

    private RestConnection getRestConnection(){
        IntLogger logger = new PrintStreamIntLogger(System.out, LogLevel.INFO)
        new UnauthenticatedRestConnection(logger, GOOGLE_URL, 213)
    }

    @Test
    public void testClientBuilding(){
        IntLogger logger = new PrintStreamIntLogger(System.out, LogLevel.INFO)
        int timeoutSeconds = 213
        int timeoutMilliSeconds = timeoutSeconds * 1000
        RestConnection restConnection = new UnauthenticatedRestConnection(logger, GOOGLE_URL, timeoutSeconds)
        OkHttpClient realClient = restConnection.client
        assert null == realClient
        restConnection.connect()
        realClient = restConnection.client
        assert timeoutMilliSeconds == realClient.connectTimeout
        assert timeoutMilliSeconds == realClient.writeTimeout
        assert timeoutMilliSeconds == realClient.readTimeout
        assert null == realClient.proxy
        assert okhttp3.Authenticator.NONE == realClient.proxyAuthenticator

        restConnection = new UnauthenticatedRestConnection(logger, GOOGLE_URL, timeoutSeconds)
        String proxyHost = "ProxyHost"
        int proxyPort = 3128
        String proxyIgnoredHosts = "IgnoredHost"
        String proxyUser = "testUser"
        String proxyPassword = "password"

        restConnection.setProxyHost(proxyHost)
        restConnection.setProxyPort(proxyPort)
        restConnection.setProxyNoHosts(proxyIgnoredHosts)
        restConnection.setProxyUsername(proxyUser)
        restConnection.setProxyPassword(proxyPassword)

        restConnection.connect()
        realClient = restConnection.client
        assert null != realClient.proxy
        assert okhttp3.Authenticator.NONE != realClient.proxyAuthenticator

        restConnection = new UnauthenticatedRestConnection(logger, GOOGLE_URL, timeoutSeconds)
        proxyIgnoredHosts = ".*"
        restConnection.setProxyHost(proxyHost)
        restConnection.setProxyPort(proxyPort)
        restConnection.setProxyNoHosts(proxyIgnoredHosts)
        restConnection.setProxyUsername(proxyUser)
        restConnection.setProxyPassword(proxyPassword)

        restConnection.connect()
        realClient = restConnection.client
        assert null == realClient.proxy
        assert okhttp3.Authenticator.NONE == realClient.proxyAuthenticator
    }

    @Test
    public void testCreatingHttpUrl(){
        RestConnection restConnection = getRestConnection()
        assert GOOGLE_URL_STRING == restConnection.createHttpUrl().url
        assert GOOGLE_URL_STRING == restConnection.createHttpUrl(GOOGLE_URL_STRING).url
        assert GOOGLE_URL_STRING+'test/whatsUp' == restConnection.createHttpUrl(["test", "whatsUp"]).url
        assert GOOGLE_URL_STRING+'test/whatsUp?name=hello&question=who' == restConnection.createHttpUrl(["test", "whatsUp"], [name:'hello', question:'who']).url
        assert GOOGLE_URL_STRING+'test/whatsUp?name=hello&question=who' == restConnection.createHttpUrl(GOOGLE_URL_STRING,["test", "whatsUp"], [name:'hello', question:'who']).url
    }
    
    @Test
    public void testCreatingBody(){
        RestConnection restConnection = getRestConnection()
        String content = "hello"
        RequestBody requestBody = restConnection.createJsonRequestBody(content)
        assert "utf-8".equals(requestBody.contentType().charset)
        assert "application".equals(requestBody.contentType().type)
        assert "json".equals(requestBody.contentType().subtype)
        assert content.length() == requestBody.contentLength()
        
        requestBody =restConnection.createJsonRequestBody("text/plain",content)
        assert "utf-8".equals(requestBody.contentType().charset)
        assert "text".equals(requestBody.contentType().type)
        assert "plain".equals(requestBody.contentType().subtype)
        assert content.length() == requestBody.contentLength()
        
        FormBody formBody =restConnection.createEncodedFormBody([name:'hello', question:'who'])
        assert null == formBody.contentType().charset
        assert "application".equals(formBody.contentType().type)
        assert "x-www-form-urlencoded".equals(formBody.contentType().subtype)
        assert formBody.encodedNames.contains('name')
        assert formBody.encodedNames.contains('question')
        assert formBody.encodedValues.contains('hello')
        assert formBody.encodedValues.contains('who')
    }
    
    @Test
    public void testCreatingGetRequest(){
        RestConnection restConnection = getRestConnection()
        restConnection.getCommonRequestHeaders().put("Common", "Header")
        HttpUrl httpUrl = restConnection.createHttpUrl()
        Request request = restConnection.createGetRequest(httpUrl)
        assert "GET".equals(request.method)
        assert httpUrl == request.url
        assert "application/json".equals(request.header("Accept"))
        assert "Header".equals(request.header("Common"))
        
        String mediaType = "text/plain"
        request = restConnection.createGetRequest(httpUrl, mediaType)
        assert "GET".equals(request.method)
        assert httpUrl == request.url
        assert mediaType.equals(request.header("Accept"))
        assert "Header".equals(request.header("Common"))
        
        restConnection.getCommonRequestHeaders().remove("Common")
        request = restConnection.createGetRequest(httpUrl, [name:'hello', question:'who'])
        assert "GET".equals(request.method)
        assert httpUrl == request.url
        assert "hello".equals(request.header("name"))
        assert "who".equals(request.header("question"))
        assert null == request.header("Common")
    }
    
    @Test
    public void testCreatingPostRequest(){
        RestConnection restConnection = getRestConnection()
        restConnection.getCommonRequestHeaders().put("Common", "Header")
        HttpUrl httpUrl = restConnection.createHttpUrl()
        RequestBody requestBody = restConnection.createJsonRequestBody("hello")
        Request request = restConnection.createPostRequest(httpUrl,requestBody)
        assert "POST".equals(request.method)
        assert httpUrl == request.url
        assert "Header".equals(request.header("Common"))
        assert requestBody == request.body
        
        restConnection.getCommonRequestHeaders().remove("Common")
        request = restConnection.createPostRequest(httpUrl,requestBody)
        assert "POST".equals(request.method)
        assert httpUrl == request.url
        assert null == request.header("Common")
        assert requestBody == request.body
    }
    
    @Test
    public void testCreatingPutRequest(){
        RestConnection restConnection = getRestConnection()
        restConnection.getCommonRequestHeaders().put("Common", "Header")
        HttpUrl httpUrl = restConnection.createHttpUrl()
        RequestBody requestBody = restConnection.createJsonRequestBody("hello")
        Request request = restConnection.createPutRequest(httpUrl,requestBody)
        assert "PUT".equals(request.method)
        assert httpUrl == request.url
        assert "Header".equals(request.header("Common"))
        assert requestBody == request.body
        
        restConnection.getCommonRequestHeaders().remove("Common")
        request = restConnection.createPutRequest(httpUrl,requestBody)
        assert "PUT".equals(request.method)
        assert httpUrl == request.url
        assert null == request.header("Common")
        assert requestBody == request.body
    }
    
    @Test
    public void testCreatingDeleteRequest(){
        RestConnection restConnection = getRestConnection()
        restConnection.getCommonRequestHeaders().put("Common", "Header")
        HttpUrl httpUrl = restConnection.createHttpUrl()
        Request request = restConnection.createDeleteRequest(httpUrl)
        assert "DELETE".equals(request.method)
        assert httpUrl == request.url
        assert "Header".equals(request.header("Common"))
        
        restConnection.getCommonRequestHeaders().remove("Common")
        request = restConnection.createDeleteRequest(httpUrl)
        assert "DELETE".equals(request.method)
        assert httpUrl == request.url
        assert null == request.header("Common")
    }

}
