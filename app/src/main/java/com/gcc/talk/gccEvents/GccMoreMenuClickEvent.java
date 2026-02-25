/*
 * Nextcloud Talk - Android Client
 *
 * SPDX-FileCopyrightText: 2021 Andy Scherzinger <info@andy-scherzinger.de>
 * SPDX-FileCopyrightText: 2017 Mario Danic <mario@lovelyhq.com>
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package com.gcc.talk.gccEvents;

import com.gcc.talk.gccModels.json.conversations.Conversation;

public class GccMoreMenuClickEvent {
    private final Conversation conversation;

    public GccMoreMenuClickEvent(Conversation conversation) {
        this.conversation = conversation;
    }

    public Conversation getConversation() {
        return this.conversation;
    }

    public boolean equals(final Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof GccMoreMenuClickEvent)) {
            return false;
        }
        final GccMoreMenuClickEvent other = (GccMoreMenuClickEvent) o;
        if (!other.canEqual((Object) this)) {
            return false;
        }
        final Object this$conversation = this.getConversation();
        final Object other$conversation = other.getConversation();

        return this$conversation == null ? other$conversation == null : this$conversation.equals(other$conversation);
    }

    protected boolean canEqual(final Object other) {
        return other instanceof GccMoreMenuClickEvent;
    }

    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        final Object $conversation = this.getConversation();
        return result * PRIME + ($conversation == null ? 43 : $conversation.hashCode());
    }

    public String toString() {
        return "GccMoreMenuClickEvent(conversation=" + this.getConversation() + ")";
    }
}
