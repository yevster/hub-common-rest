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
package com.blackducksoftware.integration.hub.certificate;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStore.PasswordProtection;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.commons.lang3.StringUtils;

import com.blackducksoftware.integration.exception.IntegrationCertificateException;
import com.blackducksoftware.integration.exception.IntegrationException;
import com.blackducksoftware.integration.hub.rest.TLSSocketFactory;
import com.blackducksoftware.integration.log.IntLogger;
import com.blackducksoftware.integration.util.proxy.ProxyUtil;

import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class CertificateHandler {

    public final IntLogger logger;

    private File javaHomeOverride;

    public int timeout = 120;

    public String proxyHost;

    public int proxyPort;

    public String proxyNoHosts;

    public String proxyUsername;

    public String proxyPassword;

    public CertificateHandler(final IntLogger intLogger) {
        logger = intLogger;
    }

    public CertificateHandler(final IntLogger intLogger, final File javaHomeOverride) {
        this(intLogger);
        this.javaHomeOverride = javaHomeOverride;
    }

    public void retrieveAndImportHttpsCertificate(final URL url) throws IntegrationException {
        if (url == null || !url.getProtocol().startsWith("https")) {
            return;
        }
        try {
            final Certificate certificate = retrieveHttpsCertificateFromURL(url);
            if (certificate == null) {
                throw new IntegrationCertificateException(String.format("Could not retrieve the Certificate from %s", url));
            }
            importHttpsCertificate(url, certificate);
        } catch (final IntegrationException e) {
            throw e;
        } catch (final Exception e) {
            throw new IntegrationException(e.getMessage(), e);
        }
    }

    public Certificate retrieveHttpsCertificateFromURL(final URL url) throws IntegrationException {
        if (url == null || !url.getProtocol().startsWith("https")) {
            return null;
        }
        logger.info(String.format("Retrieving the certificate from %s", url));

        Certificate certificate = null;
        try {
            final OkHttpClient client = getOkHttpClient(url);

            final HttpUrl.Builder urlBuilder = HttpUrl.get(url).newBuilder();
            final HttpUrl httpUrl = urlBuilder.build();

            final Request.Builder requestBuilder = new Request.Builder();
            final Request request = requestBuilder.url(httpUrl).get().build();

            final Response response = client.newCall(request).execute();

            final List<Certificate> certificates = response.handshake().peerCertificates();

            certificate = certificates.get(0);
        } catch (final Exception e) {
            throw new IntegrationException(e);
        }
        return certificate;
    }

    protected OkHttpClient getOkHttpClient(final URL url) throws IntegrationException {
        final CertTrustManager trustManager = new CertTrustManager();
        final OkHttpClient.Builder clientBuilder = new OkHttpClient.Builder();

        clientBuilder.connectTimeout(timeout, TimeUnit.SECONDS);
        clientBuilder.writeTimeout(timeout, TimeUnit.SECONDS);
        clientBuilder.readTimeout(timeout, TimeUnit.SECONDS);

        if (shouldUseProxyForUrl(url)) {
            clientBuilder.proxy(getProxy(url));
            clientBuilder.proxyAuthenticator(new com.blackducksoftware.integration.hub.proxy.OkAuthenticator(proxyUsername, proxyPassword));
        }

        final String version = System.getProperty("java.version");
        if (url.getProtocol().equalsIgnoreCase("https") && version.startsWith("1.7") || version.startsWith("1.6")) {
            // We do not need to do this for Java 8+
            try {
                // Java 7 does not enable TLS1.2 so we use our TLSSocketFactory to enable all protocols
                clientBuilder.sslSocketFactory(new TLSSocketFactory(trustManager), trustManager);
            } catch (KeyManagementException | NoSuchAlgorithmException e) {
                throw new IntegrationException(e);
            }
        } else {
            clientBuilder.sslSocketFactory(systemDefaultSslSocketFactory(trustManager), trustManager);
        }
        return clientBuilder.build();
    }

    public Certificate retrieveHttpsCertificateFromTrustStore(final URL url) throws IntegrationException {
        final File trustStore = getTrustStore();
        final String trustStorePath = trustStore.getAbsolutePath();
        logger.info(String.format("Removing the certificate from %s", trustStorePath));
        try {
            final KeyStore keyStore = getKeyStore(trustStore);
            if (keyStore.containsAlias(url.getHost())) {
                return keyStore.getCertificate(url.getHost());
            }
        } catch (final Exception e) {
            throw new IntegrationException(e);
        }
        return null;
    }

    public void importHttpsCertificate(final URL url, final Certificate certificate) throws IntegrationException {
        final File trustStore = getTrustStore();
        final String trustStorePath = trustStore.getAbsolutePath();
        logger.info(String.format("Importing the certificate from %s into keystore %s", url.getHost(), trustStorePath));
        try {
            final KeyStore keyStore = getKeyStore(trustStore);
            keyStore.setCertificateEntry(url.getHost(), certificate);
            try (OutputStream stream = new BufferedOutputStream(new FileOutputStream(trustStore))) {
                keyStore.store(stream, getKeyStorePassword());
            }
        } catch (final Exception e) {
            throw new IntegrationException(e);
        }
    }

    public void removeHttpsCertificate(final URL url) throws IntegrationException {
        final File trustStore = getTrustStore();
        final String trustStorePath = trustStore.getAbsolutePath();
        logger.info(String.format("Removing the certificate from %s", trustStorePath));
        try {
            final KeyStore keyStore = getKeyStore(trustStore);
            if (keyStore.containsAlias(url.getHost())) {
                keyStore.deleteEntry(url.getHost());
                try (OutputStream stream = new BufferedOutputStream(new FileOutputStream(trustStore))) {
                    keyStore.store(stream, getKeyStorePassword());
                }
            }
        } catch (final Exception e) {
            throw new IntegrationException(e);
        }
    }

    public boolean isCertificateInTrustStore(final URL url) throws IntegrationException {
        final File trustStore = getTrustStore();
        if (!trustStore.isFile()) {
            return false;
        }
        final String jssecacertsPath = trustStore.getAbsolutePath();
        logger.info(String.format("Checking for alias %s in keystore %s", url.getHost(), jssecacertsPath));
        try {
            final KeyStore keyStore = getKeyStore(trustStore);
            return keyStore.containsAlias(url.getHost());
        } catch (final Exception e) {
            throw new IntegrationException(e);
        }
    }

    public KeyStore getKeyStore(final File trustStore) throws KeyStoreException, NoSuchAlgorithmException, CertificateException, IOException {
        // trustStore must be an existing file and it must not be empty,
        // otherwise we create a new empty keystore
        if (trustStore.isFile() && trustStore.length() > 0) {
            final PasswordProtection protection = new PasswordProtection(getKeyStorePassword());
            return KeyStore.Builder.newInstance(getTrustStoreType(), null, trustStore, protection).getKeyStore();
        }
        final KeyStore keyStore = KeyStore.getInstance(getTrustStoreType());
        keyStore.load(null, null);
        try (OutputStream stream = new BufferedOutputStream(new FileOutputStream(trustStore))) {
            // to create a valid empty keystore file
            keyStore.store(stream, getKeyStorePassword());
        }
        return keyStore;
    }

    private String getTrustStoreType() {
        return System.getProperty("javax.net.ssl.trustStoreType", KeyStore.getDefaultType());
    }

    private char[] getKeyStorePassword() {
        return System.getProperty("javax.net.ssl.trustStorePassword", "changeit").toCharArray();
    }

    public File getTrustStore() {
        File trustStore;
        if (javaHomeOverride != null) {
            trustStore = resolveTrustStoreFile(javaHomeOverride);
        } else {
            trustStore = new File(System.getProperty("javax.net.ssl.trustStore", ""));
            if (!trustStore.isFile()) {
                final File javaHome = new File(System.getProperty("java.home"));
                trustStore = resolveTrustStoreFile(javaHome);
            }
        }

        return trustStore;
    }

    private File resolveTrustStoreFile(final File javaHome) {
        // first check for jssecacerts
        File trustStoreFile = new File(javaHome, "lib");
        trustStoreFile = new File(trustStoreFile, "security");
        trustStoreFile = new File(trustStoreFile, "jssecacerts");

        // if we can't find jssecacerts, look for cacerts
        if (!trustStoreFile.isFile()) {
            trustStoreFile = new File(javaHome, "lib");
            trustStoreFile = new File(trustStoreFile, "security");
            trustStoreFile = new File(trustStoreFile, "cacerts");
        }

        return trustStoreFile;
    }

    private SSLSocketFactory systemDefaultSslSocketFactory(final X509TrustManager trustManager) throws IntegrationException {
        try {
            final SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, new TrustManager[] { trustManager }, null);
            return sslContext.getSocketFactory();
        } catch (final GeneralSecurityException e) {
            throw new IntegrationException(e); // The system has no TLS. Just give up.
        }
    }

    private Proxy getProxy(final URL hubUrl) {
        final Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxyHost, proxyPort));
        return proxy;
    }

    private boolean shouldUseProxyForUrl(final URL url) {
        if (StringUtils.isBlank(proxyHost) || proxyPort <= 0) {
            return false;
        }
        final List<Pattern> ignoredProxyHostPatterns = ProxyUtil.getIgnoredProxyHostPatterns(proxyNoHosts);
        return !ProxyUtil.shouldIgnoreHost(url.getHost(), ignoredProxyHostPatterns);
    }

}
