/*
 * Nextcloud Talk - Android Client
 *
 * SPDX-FileCopyrightText: 2022 Marcel Hibbe <dev@mhibbe.de>
 * SPDX-FileCopyrightText: 2017-2019 Mario Danic <mario@lovelyhq.com>
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package com.gcc.talk.gccAdapters.messages;

import android.view.View;

import com.gcc.talk.gccChat.data.model.GccChatMessage;
import com.stfalcon.chatkit.messages.MessageHolders;

public class GccUnreadNoticeMessageViewHolder extends MessageHolders.SystemMessageViewHolder<GccChatMessage> {

    public GccUnreadNoticeMessageViewHolder(View itemView) {
        super(itemView);
    }

    public GccUnreadNoticeMessageViewHolder(View itemView, Object payload) {
        super(itemView, payload);
    }

    @Override
    public void viewDetached() {
    }

    @Override
    public void viewAttached() {
    }

    @Override
    public void viewRecycled() {

    }
}
