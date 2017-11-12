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

import org.apache.commons.lang3.StringUtils
import org.junit.Test

import com.blackducksoftware.integration.encryption.PasswordEncrypter

class CredentialsBuilderTest {

    @Test
    public void testBuildObject() {
        final String username = "username"
        final String password = "password"
        CredentialsBuilder builder = new CredentialsBuilder()
        builder.username = username
        builder.password = password
        Credentials credentials = builder.build()
        final String maskedPassword = credentials.getMaskedPassword()
        assert username == credentials.username
        assert password == credentials.decryptedPassword
        assert password != credentials.encryptedPassword
        assert password.length() == credentials.actualPasswordLength
        assert maskedPassword.length() == password.length()
        assert password != maskedPassword
        assert StringUtils.containsOnly(maskedPassword, "*")
    }

    @Test
    public void testEncryptedPassword() {
        final String username = "username"
        final String password = "password"
        final String encryptedPassword = PasswordEncrypter.encrypt(password);
        CredentialsBuilder builder = new CredentialsBuilder()
        builder.username = username
        builder.password = encryptedPassword
        builder.passwordLength = password.length()
        Credentials credentials = builder.build()
        final String maskedPassword = credentials.getMaskedPassword()
        assert password.length() == builder.passwordLength
        assert username == credentials.username
        assert password == credentials.decryptedPassword
        assert password != credentials.encryptedPassword
        assert password.length() == credentials.actualPasswordLength
        assert maskedPassword.length() == password.length()
        assert password != maskedPassword
        assert StringUtils.containsOnly(maskedPassword, "*")
    }
}
