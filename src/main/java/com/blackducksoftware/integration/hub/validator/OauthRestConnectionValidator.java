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
package com.blackducksoftware.integration.hub.validator;

import com.blackducksoftware.integration.hub.rest.oauth.AccessType;
import com.blackducksoftware.integration.hub.rest.oauth.OauthRestConnectionFieldEnum;
import com.blackducksoftware.integration.hub.rest.oauth.TokenManager;
import com.blackducksoftware.integration.validator.ValidationResult;
import com.blackducksoftware.integration.validator.ValidationResultEnum;
import com.blackducksoftware.integration.validator.ValidationResults;

public class OauthRestConnectionValidator extends AbstractRestConnectionValidator {

    public static final String ERROR_MSG_ACCESS_TYPE_NULL = "The access type cannot be null";
    public static final String ERROR_MSG_TOKEN_MANAGER_NULL = "The Token Manager cannot be null";
    private TokenManager tokenManager;
    private AccessType accessType;

    public TokenManager getTokenManager() {
        return tokenManager;
    }

    public AccessType getAccessType() {
        return accessType;
    }

    public void setTokenManager(final TokenManager tokenManager) {
        this.tokenManager = tokenManager;
    }

    public void setAccessType(final AccessType accessType) {
        this.accessType = accessType;
    }

    @Override
    public void validateAdditionalFields(final ValidationResults currentResults) {
        validateAccessType(currentResults);
        validateTokenManager(currentResults);
    }

    public void validateAccessType(final ValidationResults result) {
        if (accessType == null) {
            result.addResult(OauthRestConnectionFieldEnum.ACCESSTYPE, new ValidationResult(ValidationResultEnum.ERROR, ERROR_MSG_ACCESS_TYPE_NULL));
        }
    }

    public void validateTokenManager(final ValidationResults result) {
        if (tokenManager == null) {
            result.addResult(OauthRestConnectionFieldEnum.TOKENMANAGER, new ValidationResult(ValidationResultEnum.ERROR, ERROR_MSG_TOKEN_MANAGER_NULL));
        }
    }
}
