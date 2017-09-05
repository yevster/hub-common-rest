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
package com.blackducksoftware.integration.hub.rest;

import java.io.File;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.commons.lang3.StringUtils;

import com.blackducksoftware.integration.certificate.CertificateHandler;
import com.blackducksoftware.integration.exception.IntegrationException;
import com.blackducksoftware.integration.log.IntLogger;
import com.blackducksoftware.integration.util.proxy.ProxyUtil;

import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class RestCertificateHandler extends CertificateHandler {

    public int timeout = 120;

    public String proxyHost;

    public int proxyPort;

    public String proxyNoHosts;

    public String proxyUsername;

    public String proxyPassword;

    public RestCertificateHandler(final IntLogger intLogger) {
        super(intLogger);
    }

    public RestCertificateHandler(final IntLogger intLogger, final File javaHomeOverride) {
        super(intLogger, javaHomeOverride);
    }

    @Override
    public Certificate retrieveHttpsCertificateFromURL(final URL url) throws IntegrationException {
        if (url == null || !url.getProtocol().startsWith("https")) {
            return null;
        }
        logger.info(String.format("Retrieving the certificate from %s", url));

        final CertTrustManager trustManager = new CertTrustManager();
        Certificate certificate = null;
        try {
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

            clientBuilder.hostnameVerifier(new HostnameVerifier() {
                @Override
                public boolean verify(final String hostname, final SSLSession session) {
                    return true;
                }
            });

            final OkHttpClient client = clientBuilder.build();

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
