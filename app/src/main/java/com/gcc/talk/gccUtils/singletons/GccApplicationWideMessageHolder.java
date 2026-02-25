/*
 * Nextcloud Talk - Android Client
 *
 * SPDX-FileCopyrightText: 2017-2018 Mario Danic <mario@lovelyhq.com>
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package com.gcc.talk.gccUtils.singletons;

import androidx.annotation.Nullable;

public class GccApplicationWideMessageHolder {
    private static final GccApplicationWideMessageHolder holder =
        new GccApplicationWideMessageHolder();
    private MessageType messageType;

    public static GccApplicationWideMessageHolder getInstance() {
        return holder;
    }

    public MessageType getMessageType() {
        return messageType;
    }

    public void setMessageType(@Nullable MessageType messageType) {
        this.messageType = messageType;
    }

    public enum MessageType {
        WRONG_ACCOUNT, ACCOUNT_UPDATED_NOT_ADDED, SERVER_WITHOUT_TALK,
        FAILED_TO_IMPORT_ACCOUNT, ACCOUNT_WAS_IMPORTED, CALL_PASSWORD_WRONG
    }


}
