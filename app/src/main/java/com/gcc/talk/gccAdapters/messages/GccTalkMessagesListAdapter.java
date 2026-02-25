/*
 * Nextcloud Talk - Android Client
 *
 * SPDX-FileCopyrightText: 2024 Christian Reiner <foss@christian-reiner.info>
 * SPDX-FileCopyrightText: 2020 Tobias Kaminsky <tobias.kaminsky@nextcloud.com>
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package com.gcc.talk.gccAdapters.messages;

import com.gcc.talk.gccChat.GccChatActivity;
import com.stfalcon.chatkit.commons.ImageLoader;
import com.stfalcon.chatkit.commons.ViewHolder;
import com.stfalcon.chatkit.commons.models.IMessage;
import com.stfalcon.chatkit.messages.MessageHolders;
import com.stfalcon.chatkit.messages.MessagesListAdapter;

import java.util.List;

public class GccTalkMessagesListAdapter<M extends IMessage> extends MessagesListAdapter<M> {
    private final GccChatActivity chatActivity;

    public GccTalkMessagesListAdapter(
        String senderId,
        MessageHolders holders,
        ImageLoader imageLoader,
        GccChatActivity chatActivity) {
        super(senderId, holders, imageLoader);
        this.chatActivity = chatActivity;
    }
    
    public List<MessagesListAdapter.Wrapper> getItems() {
        return items;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {

        if (holder instanceof GccIncomingTextMessageViewHolder holderInstance) {
            holderInstance.assignCommonMessageInterface(chatActivity);
        } else if (holder instanceof GccOutcomingTextMessageViewHolder holderInstance) {
            holderInstance.assignCommonMessageInterface(chatActivity);
            holderInstance.adjustIfNoteToSelf(chatActivity.getCurrentConversation());

        } else if (holder instanceof GccIncomingLocationMessageViewHolder holderInstance) {
            holderInstance.assignCommonMessageInterface(chatActivity);
        } else if (holder instanceof GccOutcomingLocationMessageViewHolder holderInstance) {
            holderInstance.assignCommonMessageInterface(chatActivity);
            holderInstance.adjustIfNoteToSelf(chatActivity.getCurrentConversation());

        } else if (holder instanceof GccIncomingLinkPreviewMessageViewHolder holderInstance) {
            holderInstance.assignCommonMessageInterface(chatActivity);
        } else if (holder instanceof GccOutcomingLinkPreviewMessageViewHolder holderInstance) {
            holderInstance.assignCommonMessageInterface(chatActivity);
            holderInstance.adjustIfNoteToSelf(chatActivity.getCurrentConversation());

        } else if (holder instanceof GccIncomingVoiceMessageViewHolder holderInstance) {
            holderInstance.assignVoiceMessageInterface(chatActivity);
            holderInstance.assignCommonMessageInterface(chatActivity);
        } else if (holder instanceof GccOutcomingVoiceMessageViewHolder holderInstance) {
            holderInstance.assignVoiceMessageInterface(chatActivity);
            holderInstance.assignCommonMessageInterface(chatActivity);
            holderInstance.adjustIfNoteToSelf(chatActivity.getCurrentConversation());

        } else if (holder instanceof GccPreviewMessageViewHolder holderInstance) {
            holderInstance.assignPreviewMessageInterface(chatActivity);
            holderInstance.assignCommonMessageInterface(chatActivity);

        } else if (holder instanceof GccSystemMessageViewHolder holderInstance) {
            holderInstance.assignSystemMessageInterface(chatActivity);

        } else if (holder instanceof GccIncomingDeckCardViewHolder holderInstance) {
            holderInstance.assignCommonMessageInterface(chatActivity);
        } else if (holder instanceof GccOutcomingDeckCardViewHolder holderInstance) {
            holderInstance.assignCommonMessageInterface(chatActivity);
            holderInstance.adjustIfNoteToSelf(chatActivity.getCurrentConversation());

        } else if (holder instanceof GccIncomingPollMessageViewHolder holderInstance) {
            holderInstance.assignCommonMessageInterface(chatActivity);
        } else if (holder instanceof GccOutcomingPollMessageViewHolder holderInstance) {
            holderInstance.assignCommonMessageInterface(chatActivity);
            holderInstance.adjustIfNoteToSelf(chatActivity.getCurrentConversation());
        }

        super.onBindViewHolder(holder, position);
    }
}
