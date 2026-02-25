/*
 * Nextcloud Talk - Android Client
 *
 * SPDX-FileCopyrightText: 2024 Christian Reiner <foss@christian-reiner.info>
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.gcc.talk.gccAdapters.messages

import android.widget.RelativeLayout
import androidx.viewbinding.ViewBinding
import com.gcc.talk.databinding.ItemCustomOutcomingDeckCardMessageBinding
import com.gcc.talk.databinding.ItemCustomOutcomingLinkPreviewMessageBinding
import com.gcc.talk.databinding.ItemCustomOutcomingLocationMessageBinding
import com.gcc.talk.databinding.ItemCustomOutcomingPollMessageBinding
import com.gcc.talk.databinding.ItemCustomOutcomingTextMessageBinding
import com.gcc.talk.databinding.ItemCustomOutcomingVoiceMessageBinding
import com.gcc.talk.gccModels.domain.GccConversationModel
import com.gcc.talk.gccModels.json.conversations.ConversationEnums.ConversationType

interface GccAdjustableMessageHolderInterface {

    val binding: ViewBinding

    fun adjustIfNoteToSelf(currentConversation: GccConversationModel?) {
        if (currentConversation?.type == ConversationType.NOTE_TO_SELF) {
            when (this.binding.javaClass) {
                ItemCustomOutcomingTextMessageBinding::class.java ->
                    (this.binding as ItemCustomOutcomingTextMessageBinding).bubble
                ItemCustomOutcomingDeckCardMessageBinding::class.java ->
                    (this.binding as ItemCustomOutcomingDeckCardMessageBinding).bubble
                ItemCustomOutcomingLinkPreviewMessageBinding::class.java ->
                    (this.binding as ItemCustomOutcomingLinkPreviewMessageBinding).bubble
                ItemCustomOutcomingPollMessageBinding::class.java ->
                    (this.binding as ItemCustomOutcomingPollMessageBinding).bubble
                ItemCustomOutcomingVoiceMessageBinding::class.java ->
                    (this.binding as ItemCustomOutcomingVoiceMessageBinding).bubble
                ItemCustomOutcomingLocationMessageBinding::class.java ->
                    (this.binding as ItemCustomOutcomingLocationMessageBinding).bubble
                else -> null
            }?.let {
                RelativeLayout.LayoutParams(binding.root.layoutParams).apply {
                    marginStart = 0
                    marginEnd = 0
                }.run {
                    it.layoutParams = this
                }
            }
        }
    }
}
