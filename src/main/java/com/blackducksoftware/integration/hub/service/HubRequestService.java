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
package com.blackducksoftware.integration.hub.service;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import com.blackducksoftware.integration.exception.IntegrationException;
import com.blackducksoftware.integration.hub.api.item.HubPagedResponse;
import com.blackducksoftware.integration.hub.request.HubPagedRequest;
import com.blackducksoftware.integration.hub.request.HubRequest;
import com.blackducksoftware.integration.hub.request.HubRequestFactory;
import com.blackducksoftware.integration.hub.rest.RestConnection;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class HubRequestService {
    private final RestConnection restConnection;

    private final HubRequestFactory hubRequestFactory;

    public HubRequestService(final RestConnection restConnection) {
        this.restConnection = restConnection;
        this.hubRequestFactory = new HubRequestFactory(restConnection);
    }

    public String getString(final List<String> urlSegments) throws IntegrationException {
        final HubRequest hubRequest = getHubRequestFactory().createRequest(urlSegments);
        final String s = hubRequest.executeGetForResponseString();
        return s;
    }

    public JsonObject getJsonObject(final List<String> urlSegments) throws IntegrationException {
        final HubRequest hubRequest = getHubRequestFactory().createRequest(urlSegments);
        final JsonObject jsonObject = hubRequest.executeGetForResponseJson();
        return jsonObject;
    }

    public <T> T getItem(final String url, final Class<T> clazz) throws IntegrationException {
        final HubRequest hubRequest = getHubRequestFactory().createRequest(url);
        final String response = hubRequest.executeGetForResponseString();
        final T item = getRestConnection().getGson().fromJson(response, clazz);
        return item;
    }

    public <T> T getItem(final JsonObject jsonObject, final Class<T> clazz) {
        final T item = getRestConnection().getGson().fromJson(jsonObject, clazz);
        return item;
    }

    public <T> T getItem(final JsonElement jsonElement, final Class<T> clazz) {
        final T item = getRestConnection().getGson().fromJson(jsonElement, clazz);
        return item;
    }

    public <T> List<T> getItems(final HubPagedRequest hubPagedRequest, final Class<T> clazz) throws IntegrationException {
        final JsonObject jsonObject = hubPagedRequest.executeGetForResponseJson();
        final List<T> items = getItems(jsonObject, clazz);
        return items;
    }

    /**
     * This method can be overridden by subclasses to provide special treatment for extracting the items from the
     * jsonObject.
     */
    public <T> List<T> getItems(final JsonObject jsonObject, final Class<T> clazz) {
        final LinkedList<T> itemList = new LinkedList<>();
        final JsonElement itemsElement = jsonObject.get("items");
        final JsonArray itemsArray = itemsElement.getAsJsonArray();
        final int count = itemsArray.size();
        for (int index = 0; index < count; index++) {
            final JsonElement element = itemsArray.get(index);
            final T item = getItem(element, clazz);
            itemList.add(item);
        }
        return itemList;
    }

    public <T> List<T> getAllItems(final HubPagedRequest hubPagedRequest, final Class<T> clazz) throws IntegrationException {
        final List<T> allItems = new ArrayList<>();

        final HubPagedResponse<T> firstPage = getPagedResponse(hubPagedRequest, clazz);
        final int totalCount = firstPage.getTotalCount();
        final List<T> items = firstPage.getItems();
        allItems.addAll(items);

        while (allItems.size() < totalCount) {
            final int currentOffset = hubPagedRequest.getOffset();
            final int increasedOffset = currentOffset + items.size();

            hubPagedRequest.setOffset(increasedOffset);
            final HubPagedResponse<T> nextPage = getPagedResponse(hubPagedRequest, clazz);
            allItems.addAll(nextPage.getItems());
        }

        return allItems;
    }

    private <T> HubPagedResponse<T> getPagedResponse(final HubPagedRequest hubPagedRequest, final Class<T> clazz) throws IntegrationException {
        final JsonObject jsonObject = hubPagedRequest.executeGetForResponseJson();
        final int totalCount = jsonObject.get("totalCount").getAsInt();
        final List<T> items = getItems(jsonObject, clazz);
        return new HubPagedResponse<>(totalCount, items);
    }

    public <T> List<T> getAllItems(final List<String> urlSegments, final Class<T> clazz) throws IntegrationException {
        final HubPagedRequest hubPagedRequest = getHubRequestFactory().createPagedRequest(urlSegments);
        return getAllItems(hubPagedRequest, clazz);
    }

    public <T> List<T> getAllItems(final String url, final Class<T> clazz) throws IntegrationException {
        final HubPagedRequest hubPagedRequest = getHubRequestFactory().createPagedRequest(url);
        return getAllItems(hubPagedRequest, clazz);
    }

    public void deleteItem(final String url) throws IntegrationException {
        final HubRequest hubRequest = hubRequestFactory.createRequest(url);
        hubRequest.executeDelete();
    }

    public RestConnection getRestConnection() {
        return restConnection;
    }

    public HubRequestFactory getHubRequestFactory() {
        return hubRequestFactory;
    }

}
