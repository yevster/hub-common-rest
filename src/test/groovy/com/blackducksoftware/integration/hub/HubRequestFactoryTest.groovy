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

import com.blackducksoftware.integration.hub.request.HubPagedRequest
import com.blackducksoftware.integration.hub.request.HubRequest
import com.blackducksoftware.integration.hub.request.HubRequestFactory
import com.blackducksoftware.integration.hub.rest.RestConnection
import com.blackducksoftware.integration.hub.rest.UnauthenticatedRestConnection
import com.blackducksoftware.integration.log.LogLevel
import com.blackducksoftware.integration.log.PrintStreamIntLogger

class HubRequestFactoryTest {
    public static final String GOOGLE_URL_STRING = "https://www.google.com/"

    public static final URL GOOGLE_URL = new URL(GOOGLE_URL_STRING)

    public static final int CONNECTION_TIMEOUT = 213

    private RestConnection getRestConnection(){
        new UnauthenticatedRestConnection(new PrintStreamIntLogger(System.out, LogLevel.INFO), GOOGLE_URL, CONNECTION_TIMEOUT)
    }

    @Test
    public void testCreatePagedRequest(){
        HubRequestFactory hubRequestFactory = new HubRequestFactory(getRestConnection())
        def urlSegments = ["hello", "test"]
        HubPagedRequest pagedRequest = hubRequestFactory.createPagedRequest(urlSegments)
        assert 100 == pagedRequest.limit
        urlSegments.each {
            assert pagedRequest.urlSegments.contains(it)
        }
        assert null == pagedRequest.url
        assert null == pagedRequest.q
        assert pagedRequest.queryParameters.isEmpty()

        pagedRequest = hubRequestFactory.createPagedRequest(51, urlSegments)
        assert 51 == pagedRequest.limit
        urlSegments.each {
            assert pagedRequest.urlSegments.contains(it)
        }
        assert null == pagedRequest.url
        assert null == pagedRequest.q
        assert pagedRequest.queryParameters.isEmpty()

        String q = "testQuery"
        pagedRequest = hubRequestFactory.createPagedRequest(urlSegments, q)
        assert 100 == pagedRequest.limit
        urlSegments.each {
            assert pagedRequest.urlSegments.contains(it)
        }
        assert q.equals(pagedRequest.q)
        assert null == pagedRequest.url
        assert pagedRequest.queryParameters.isEmpty()

        q = "testQuery"
        pagedRequest = hubRequestFactory.createPagedRequest(53, urlSegments, q)
        assert 53 == pagedRequest.limit
        urlSegments.each {
            assert pagedRequest.urlSegments.contains(it)
        }
        assert q.equals(pagedRequest.q)
        assert null == pagedRequest.url
        assert pagedRequest.queryParameters.isEmpty()

        pagedRequest = hubRequestFactory.createPagedRequest(GOOGLE_URL_STRING)
        assert 100 == pagedRequest.limit
        assert pagedRequest.urlSegments.isEmpty()
        assert null == pagedRequest.q
        assert GOOGLE_URL_STRING.equals(pagedRequest.url)
        assert pagedRequest.queryParameters.isEmpty()

        pagedRequest = hubRequestFactory.createPagedRequest(21, GOOGLE_URL_STRING)
        assert 21 == pagedRequest.limit
        assert pagedRequest.urlSegments.isEmpty()
        assert null == pagedRequest.q
        assert GOOGLE_URL_STRING.equals(pagedRequest.url)
        assert pagedRequest.queryParameters.isEmpty()

        pagedRequest = hubRequestFactory.createPagedRequest(GOOGLE_URL_STRING, q)
        assert 100 == pagedRequest.limit
        assert pagedRequest.urlSegments.isEmpty()
        assert q.equals(pagedRequest.q)
        assert GOOGLE_URL_STRING.equals(pagedRequest.url)
        assert pagedRequest.queryParameters.isEmpty()

        pagedRequest = hubRequestFactory.createPagedRequest(34, GOOGLE_URL_STRING, q)
        assert 34 == pagedRequest.limit
        assert pagedRequest.urlSegments.isEmpty()
        assert q.equals(pagedRequest.q)
        assert GOOGLE_URL_STRING.equals(pagedRequest.url)
        assert pagedRequest.queryParameters.isEmpty()
    }

    @Test
    public void testCreateRequest(){
        HubRequestFactory hubRequestFactory = new HubRequestFactory(getRestConnection())
        def urlSegments = ["hello", "test"]
        HubRequest request = hubRequestFactory.createRequest(urlSegments)
        urlSegments.each {
            assert request.urlSegments.contains(it)
        }
        assert null == request.url
        assert null == request.q
        assert request.queryParameters.isEmpty()

        request = hubRequestFactory.createRequest(GOOGLE_URL_STRING)
        assert request.urlSegments.isEmpty()
        assert GOOGLE_URL_STRING.equals(request.url)
        assert null == request.q
        assert request.queryParameters.isEmpty()

        request = hubRequestFactory.createRequest()
        assert request.urlSegments.isEmpty()
        assert GOOGLE_URL_STRING.equals(request.url)
        assert null == request.q
        assert request.queryParameters.isEmpty()
    }
}
