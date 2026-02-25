/*
 * Nextcloud Talk - Android Client
 *
 * SPDX-FileCopyrightText: 2017 Mario Danic <mario@lovelyhq.com>
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package com.gcc.talk.gccEvents;

import android.webkit.SslErrorHandler;

import com.gcc.talk.gccUtils.ssl.GccTrustManager;

import java.security.cert.X509Certificate;

import androidx.annotation.Nullable;

public class GccCertificateEvent {
    private final X509Certificate x509Certificate;
    private final GccTrustManager trustManager;
    @Nullable
    private final SslErrorHandler sslErrorHandler;

    public GccCertificateEvent(X509Certificate x509Certificate, GccTrustManager trustManager,
                               @Nullable SslErrorHandler sslErrorHandler) {
        this.x509Certificate = x509Certificate;
        this.trustManager = trustManager;
        this.sslErrorHandler = sslErrorHandler;
    }

    @Nullable
    public SslErrorHandler getSslErrorHandler() {
        return sslErrorHandler;
    }

    public X509Certificate getX509Certificate() {
        return x509Certificate;
    }

    public GccTrustManager getTrustManager() {
        return trustManager;
    }
}
