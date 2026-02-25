/*
 * Nextcloud Talk - Android Client
 *
 * SPDX-FileCopyrightText: 2017-2018 Mario Danic <mario@lovelyhq.com>
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package com.gcc.talk.gccUtils.singletons;

public class GccAvatarStatusCodeHolder {
    private static final GccAvatarStatusCodeHolder holder = new GccAvatarStatusCodeHolder();
    private int statusCode;

    public static GccAvatarStatusCodeHolder getInstance() {
        return holder;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }
}
