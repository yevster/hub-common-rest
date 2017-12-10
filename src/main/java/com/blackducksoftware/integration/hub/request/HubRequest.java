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
package com.blackducksoftware.integration.hub.request;

import static com.blackducksoftware.integration.hub.RestConstants.QUERY_Q;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.RecursiveToStringStyle;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;

import com.blackducksoftware.integration.exception.IntegrationException;
import com.blackducksoftware.integration.hub.rest.RestConnection;

import okhttp3.HttpUrl;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Most usages of the Hub endpoints as of 2016-11-23 (Hub 3.3.1) should use the HubPagedRequest, but there are several REST endpoints that do not consume limit or offset, and those should use this implementation.
 */
public class HubRequest {
    public final RestConnection restConnection;
    public String url;
    public final List<String> urlSegments = new ArrayList<>();
    public final Map<String, String> queryParameters = new HashMap<>();
    public String q;

    public HubRequest(final RestConnection restConnection) {
        this.restConnection = restConnection;
    }

    public Response executeGet() throws IntegrationException {
        final HttpUrl httpUrl = buildHttpUrl();
        final Request request = restConnection.createGetRequest(httpUrl);
        return restConnection.createResponse(request);
    }

    public Response executeGet(final String mediaType) throws IntegrationException {
        final HttpUrl httpUrl = buildHttpUrl();
        final Request request = restConnection.createGetRequest(httpUrl, mediaType);
        return restConnection.createResponse(request);
    }

    public Response executeEncodedFormPost(final Map<String, String> contentMap) throws IntegrationException {
        final HttpUrl httpUrl = buildHttpUrl();
        final Request request = restConnection.createPostRequest(httpUrl, restConnection.createEncodedFormBody(contentMap));
        return restConnection.createResponse(request);
    }

    public Response executePost(final String content) throws IntegrationException {
        final HttpUrl httpUrl = buildHttpUrl();
        final Request request = restConnection.createPostRequest(httpUrl, restConnection.createJsonRequestBody(content));
        return restConnection.createResponse(request);
    }

    public Response executePost(final String mediaType, final String content) throws IntegrationException {
        final HttpUrl httpUrl = buildHttpUrl();
        final Request request = restConnection.createPostRequest(httpUrl, restConnection.createJsonRequestBody(mediaType, content));
        return restConnection.createResponse(request);
    }

    public Response executePost(final String mediaType, final File file) throws IntegrationException {
        final HttpUrl httpUrl = buildHttpUrl();
        final Request request = restConnection.createPostRequest(httpUrl, restConnection.createFileRequestBody(mediaType, file));
        return restConnection.createResponse(request);
    }

    public Response executeEncodedFormPut(final Map<String, String> contentMap) throws IntegrationException {
        final HttpUrl httpUrl = buildHttpUrl();
        final Request request = restConnection.createPutRequest(httpUrl, restConnection.createEncodedFormBody(contentMap));
        return restConnection.createResponse(request);
    }

    public Response executePut(final String content) throws IntegrationException {
        final HttpUrl httpUrl = buildHttpUrl();
        final Request request = restConnection.createPutRequest(httpUrl, restConnection.createJsonRequestBody(content));
        return restConnection.createResponse(request);
    }

    public Response executePut(final String mediaType, final String content) throws IntegrationException {
        final HttpUrl httpUrl = buildHttpUrl();
        final Request request = restConnection.createPutRequest(httpUrl, restConnection.createJsonRequestBody(mediaType, content));
        return restConnection.createResponse(request);
    }

    public void executeDelete() throws IntegrationException {
        final HttpUrl httpUrl = buildHttpUrl();
        final Request request = restConnection.createDeleteRequest(httpUrl);
        try (Response response = restConnection.createResponse(request)) {

        }
    }

    protected void populateQueryParameters() {
        if (StringUtils.isNotBlank(q)) {
            queryParameters.put(QUERY_Q, q);
        }
    }

    private HttpUrl buildHttpUrl() {
        populateQueryParameters();
        if (StringUtils.isBlank(url)) {
            url = restConnection.hubBaseUrl.toString();
        }
        return restConnection.createHttpUrl(url, urlSegments, queryParameters);
    }

    public void addUrlSegment(final String urlSegment) {
        urlSegments.add(urlSegment);
    }

    public void addUrlSegments(final List<String> urlSegment) {
        urlSegments.addAll(urlSegment);
    }

    public HubRequest addQueryParameter(final String queryParameterName, final String queryParameterValue) {
        this.queryParameters.put(queryParameterName, queryParameterValue);
        return this;
    }

    public HubRequest addQueryParameters(final Map<String, String> queryParameters) {
        this.queryParameters.putAll(queryParameters);
        return this;
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this, RecursiveToStringStyle.JSON_STYLE);
    }

}
