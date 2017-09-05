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

import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.X509TrustManager;

public class CertTrustManager implements X509TrustManager {
    public X509Certificate[] certificates = new X509Certificate[] {};

    @Override
    public void checkClientTrusted(final X509Certificate[] certificateChain, final String authType) throws CertificateException {
    }

    @Override
    public void checkServerTrusted(final X509Certificate[] certificateChain, final String authType) throws CertificateException {
        this.certificates = certificateChain;
    }

    @Override
    public X509Certificate[] getAcceptedIssuers() {
        return new X509Certificate[] {};
    }

}
