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
package com.blackducksoftware.integration.hub.rest.exception;

import com.blackducksoftware.integration.exception.IntegrationException;

public class IntegrationRestException extends IntegrationException {
    private final int httpStatusCode;

    private final String httpStatusMessage;

    public IntegrationRestException(final int httpStatusCode, final String httpStatusMessage) {
        super();
        this.httpStatusCode = httpStatusCode;
        this.httpStatusMessage = httpStatusMessage;
    }

    public IntegrationRestException(final int httpStatusCode, final String httpStatusMessage, final String message, final Throwable cause,
            final boolean enableSuppression, final boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
        this.httpStatusCode = httpStatusCode;
        this.httpStatusMessage = httpStatusMessage;
    }

    public IntegrationRestException(final int httpStatusCode, final String httpStatusMessage, final String message, final Throwable cause) {
        super(message, cause);
        this.httpStatusCode = httpStatusCode;
        this.httpStatusMessage = httpStatusMessage;
    }

    public IntegrationRestException(final int httpStatusCode, final String httpStatusMessage, final String message) {
        super(message);
        this.httpStatusCode = httpStatusCode;
        this.httpStatusMessage = httpStatusMessage;
    }

    public IntegrationRestException(final int httpStatusCode, final String httpStatusMessage, final Throwable cause) {
        super(cause);
        this.httpStatusCode = httpStatusCode;
        this.httpStatusMessage = httpStatusMessage;
    }

    public int getHttpStatusCode() {
        return httpStatusCode;
    }

    public String getHttpStatusMessage() {
        return httpStatusMessage;
    }

}
