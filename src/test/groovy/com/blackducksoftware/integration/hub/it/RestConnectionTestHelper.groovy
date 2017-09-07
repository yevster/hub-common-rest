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
package com.blackducksoftware.integration.hub.it

import java.util.logging.Level
import java.util.logging.Logger

import org.junit.Assert;

import com.blackducksoftware.integration.hub.rest.CredentialsRestConnection
import com.blackducksoftware.integration.log.LogLevel
import com.blackducksoftware.integration.log.PrintStreamIntLogger

import okhttp3.OkHttpClient

public class RestConnectionTestHelper {
    private Properties testProperties;

    private final String hubServerUrl;

    public RestConnectionTestHelper() {
        initProperties()
        this.hubServerUrl = getProperty(TestingPropertyKey.TEST_HUB_SERVER_URL)
    }

    public RestConnectionTestHelper(final String hubServerUrlPropertyName) {
        initProperties()
        this.hubServerUrl = testProperties.getProperty(hubServerUrlPropertyName)
    }

    private void initProperties() {
        Logger.getLogger(OkHttpClient.class.getName()).setLevel(Level.FINE)
        testProperties = new Properties()
        final ClassLoader classLoader = Thread.currentThread().getContextClassLoader()
        InputStream is = null;
        try  {
            is = classLoader.getResourceAsStream("test.properties")
            testProperties.load(is)
        } catch (final Exception e) {
            System.err.println("reading test.properties failed!")
        } finally {
            if(is != null) {
                is.close()
            }
        }

        if (testProperties.isEmpty()) {
            try {
                loadOverrideProperties(TestingPropertyKey.values())
            } catch (final Exception e) {
                System.err.println("reading properties from the environment failed")
            }
        }
    }

    private void loadOverrideProperties(final TestingPropertyKey[] keys) {
        for (final TestingPropertyKey key : keys) {
            final String prop = System.getenv(key.toString())
            if (prop != null && !prop.isEmpty()) {
                testProperties.setProperty(key.toString(), prop)
            }
        }
    }

    public String getProperty(final TestingPropertyKey key) {
        return getProperty(key.toString())
    }

    public String getProperty(final String key) {
        return testProperties.getProperty(key)
    }

    public String getIntegrationHubServerUrlString() {
        return hubServerUrl
    }

    public URL getIntegrationHubServerUrl() {
        URL url
        try {
            url = new URL(getIntegrationHubServerUrlString())
        } catch (final MalformedURLException e) {
            throw new RuntimeException(e)
        }
        return url
    }

    public String getTestUsername() {
        return getProperty(TestingPropertyKey.TEST_USERNAME)
    }

    public String getTestPassword() {
        return getProperty(TestingPropertyKey.TEST_PASSWORD)
    }

    public CredentialsRestConnection getRestConnection() {
        return getRestConnection(LogLevel.TRACE);
    }

    public CredentialsRestConnection getRestConnection(final LogLevel logLevel) {
        final CredentialsRestConnection restConnection = new CredentialsRestConnection(new PrintStreamIntLogger(System.out, logLevel),
                getIntegrationHubServerUrl(), getTestUsername(), getTestPassword(), 120);

        return restConnection;
    }
    
    public File getFile(final String classpathResource) {
        try {
            final URL url = Thread.currentThread().getContextClassLoader().getResource(classpathResource)
            final File file = new File(url.toURI().getPath())
            return file
        } catch (final Exception e) {
            Assert.fail("Could not get file: " + e.getMessage())
            return null
        }
    }
}
