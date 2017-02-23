/*
 * Copyright (C) 2017 Black Duck Software Inc.
 * http://www.blackducksoftware.com/
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Black Duck Software ("Confidential Information"). You shall not
 * disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Black Duck Software.
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
