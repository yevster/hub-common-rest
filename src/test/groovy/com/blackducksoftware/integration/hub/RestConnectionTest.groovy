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

import com.blackducksoftware.integration.hub.proxy.ProxyInfo
import com.blackducksoftware.integration.hub.rest.CredentialsRestConnection
import com.blackducksoftware.integration.hub.rest.RestConnection
import com.blackducksoftware.integration.hub.rest.UnauthenticatedRestConnection
import com.blackducksoftware.integration.hub.rest.exception.IntegrationRestException
import com.blackducksoftware.integration.log.IntLogger
import com.blackducksoftware.integration.log.LogLevel
import com.blackducksoftware.integration.log.PrintStreamIntLogger

import okhttp3.FormBody
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest

class RestConnectionTest {
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
        final Dispatcher dispatcher = new Dispatcher() {
                    @Override
                    public MockResponse dispatch(RecordedRequest request) throws InterruptedException {
                        response
                    }
                };
        server.setDispatcher(dispatcher);
        new CredentialsRestConnection(new PrintStreamIntLogger(System.out, LogLevel.TRACE), server.url("/").url(), 'TestUser', 'Password', CONNECTION_TIMEOUT, ProxyInfo.NO_PROXY_INFO)
    }

    @Test
    public void testClientBuilding(){
        IntLogger logger = new PrintStreamIntLogger(System.out, LogLevel.INFO)
        int timeoutSeconds = 213
        int timeoutMilliSeconds = timeoutSeconds * 1000
        RestConnection restConnection = new UnauthenticatedRestConnection(logger, server.url("/").url(), timeoutSeconds, ProxyInfo.NO_PROXY_INFO)
        OkHttpClient realClient = restConnection.client
        assert null == realClient
        restConnection.connect()
        realClient = restConnection.client
        assert timeoutMilliSeconds == realClient.connectTimeout
        assert timeoutMilliSeconds == realClient.writeTimeout
        assert timeoutMilliSeconds == realClient.readTimeout
        assert null == realClient.proxy
        assert okhttp3.Authenticator.NONE == realClient.proxyAuthenticator

        String proxyHost = "ProxyHost"
        int proxyPort = 3128
        String proxyIgnoredHosts = "IgnoredHost"
        String proxyUser = "testUser"
        String proxyPassword = "password"
        Credentials proxyCredentials = new Credentials(proxyUser,proxyPassword)
        ProxyInfo proxyInfo = new ProxyInfo(proxyHost, proxyPort, proxyCredentials, proxyIgnoredHosts)
        restConnection = new UnauthenticatedRestConnection(logger, server.url("/").url(), timeoutSeconds, proxyInfo)

        restConnection.connect()
        realClient = restConnection.client
        assert null != realClient.proxy
        assert okhttp3.Authenticator.NONE != realClient.proxyAuthenticator

        restConnection = new UnauthenticatedRestConnection(logger, server.url("/").url(), timeoutSeconds, ProxyInfo.NO_PROXY_INFO)
        proxyIgnoredHosts = ".*"
        proxyCredentials = new Credentials(proxyUser,proxyPassword)
        proxyInfo = new ProxyInfo(proxyHost, proxyPort, proxyCredentials, proxyIgnoredHosts)
        restConnection = new UnauthenticatedRestConnection(logger, server.url("/").url(), timeoutSeconds, proxyInfo)

        restConnection.connect()
        realClient = restConnection.client
        assert null == realClient.proxy
        assert okhttp3.Authenticator.NONE == realClient.proxyAuthenticator
    }

    @Test
    public void testToString(){
        RestConnection restConnection = getRestConnection()
        String s  = "RestConnection [baseUrl=${server.url("/").toString()}]"
        assert s.equals(restConnection.toString())
    }

    @Test
    public void testCreatingHttpUrl(){
        RestConnection restConnection = getRestConnection()
        assert server.url("/").toString() == restConnection.createHttpUrl().url
        assert server.url("/").toString() == restConnection.createHttpUrl(server.url("/").url()).url
        assert server.url("/").toString() == restConnection.createHttpUrl(server.url("/").toString()).url
        assert server.url("/").toString()+'test/whatsUp' == restConnection.createHttpUrl(["test", "whatsUp"]).url
        assert server.url("/").toString()+'test/whatsUp?name=hello&question=who' == restConnection.createHttpUrl(["test", "whatsUp"], [name:'hello', question:'who']).url
        assert server.url("/").toString()+'test/whatsUp?name=hello&question=who' == restConnection.createHttpUrl(server.url("/").toString(),["test", "whatsUp"], [name:'hello', question:'who']).url
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
        restConnection.commonRequestHeaders.put("Common", "Header")
        HttpUrl httpUrl = restConnection.createHttpUrl()
        Request request = restConnection.createGetRequest(httpUrl)
        assert "GET".equals(request.method)
        assert httpUrl == request.url
        assert "application/json".equals(request.header("Accept"))
        assert "Header".equals(request.header("Common"))
        assert !restConnection.commonRequestHeaders.isEmpty()

        String mediaType = "text/plain"
        request = restConnection.createGetRequest(httpUrl, mediaType)
        assert "GET".equals(request.method)
        assert httpUrl == request.url
        assert mediaType.equals(request.header("Accept"))
        assert "Header".equals(request.header("Common"))
        assert !restConnection.commonRequestHeaders.isEmpty()

        restConnection.commonRequestHeaders.remove("Common")
        request = restConnection.createGetRequest(httpUrl, [name:'hello', question:'who'])
        assert "GET".equals(request.method)
        assert httpUrl == request.url
        assert "hello".equals(request.header("name"))
        assert "who".equals(request.header("question"))
        assert null == request.header("Common")
        assert restConnection.commonRequestHeaders.isEmpty()
    }

    @Test
    public void testCreatingPostRequest(){
        RestConnection restConnection = getRestConnection()
        restConnection.commonRequestHeaders.put("Common", "Header")
        HttpUrl httpUrl = restConnection.createHttpUrl()
        RequestBody requestBody = restConnection.createJsonRequestBody("hello")
        Request request = restConnection.createPostRequest(httpUrl,requestBody)
        assert "POST".equals(request.method)
        assert httpUrl == request.url
        assert "Header".equals(request.header("Common"))
        assert requestBody == request.body

        restConnection.commonRequestHeaders.remove("Common")
        request = restConnection.createPostRequest(httpUrl,requestBody)
        assert "POST".equals(request.method)
        assert httpUrl == request.url
        assert null == request.header("Common")
        assert requestBody == request.body
    }

    @Test
    public void testCreatingPutRequest(){
        RestConnection restConnection = getRestConnection()
        restConnection.commonRequestHeaders.put("Common", "Header")
        HttpUrl httpUrl = restConnection.createHttpUrl()
        RequestBody requestBody = restConnection.createJsonRequestBody("hello")
        Request request = restConnection.createPutRequest(httpUrl,requestBody)
        assert "PUT".equals(request.method)
        assert httpUrl == request.url
        assert "Header".equals(request.header("Common"))
        assert requestBody == request.body

        restConnection.commonRequestHeaders.remove("Common")
        request = restConnection.createPutRequest(httpUrl,requestBody)
        assert "PUT".equals(request.method)
        assert httpUrl == request.url
        assert null == request.header("Common")
        assert requestBody == request.body
    }

    @Test
    public void testCreatingDeleteRequest(){
        RestConnection restConnection = getRestConnection()
        restConnection.commonRequestHeaders.put("Common", "Header")
        HttpUrl httpUrl = restConnection.createHttpUrl()
        Request request = restConnection.createDeleteRequest(httpUrl)
        assert "DELETE".equals(request.method)
        assert httpUrl == request.url
        assert "Header".equals(request.header("Common"))

        restConnection.commonRequestHeaders.remove("Common")
        request = restConnection.createDeleteRequest(httpUrl)
        assert "DELETE".equals(request.method)
        assert httpUrl == request.url
        assert null == request.header("Common")
    }

    @Test
    public void testHandleExecuteClientCallSuccessful(){
        RestConnection restConnection = getRestConnection()
        HttpUrl httpUrl = restConnection.createHttpUrl()
        Request request = restConnection.createGetRequest(httpUrl)
        restConnection.handleExecuteClientCall(request).withCloseable{  assert 200 == it.code }
    }

    @Test
    public void testHandleExecuteClientCallFail(){
        RestConnection restConnection = getRestConnection()
        HttpUrl httpUrl = restConnection.createHttpUrl()
        Request request = restConnection.createGetRequest(httpUrl)
        restConnection.connect()

        restConnection = getRestConnection(new MockResponse().setResponseCode(404))
        try{
            restConnection.handleExecuteClientCall(request)
            fail('Should have thrown exception')
        } catch (IntegrationRestException e) {
            assert 404 == e.httpStatusCode
        }

        restConnection = getRestConnection(new MockResponse().setResponseCode(401))
        try{
            restConnection.handleExecuteClientCall(request)
            fail('Should have thrown exception')
        } catch (IntegrationRestException e) {
            assert 401 == e.httpStatusCode
        }
    }

    @Test
    public void testParsingDate(){
        String dateString = '2017-03-02T03:35:23.456Z'
        Date date = RestConnection.parseDateString(dateString)
        assert dateString.equals(RestConnection.formatDate(date))
    }
}
