/*
 * Nextcloud Talk - Android Client
 *
 * SPDX-FileCopyrightText: 2022 Marcel Hibbe <dev@mhibbe.de>
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package com.gcc.talk.gccAdapters.messages

import android.view.View
import com.gcc.talk.R
import com.gcc.talk.gccChat.GccChatActivity
import com.gcc.talk.databinding.ReactionsInsideMessageBinding
import com.gcc.talk.gccChat.data.model.GccChatMessage
import com.gcc.talk.databinding.ItemThreadTitleBinding

class GccThread {

    fun showThreadPreview(
        chatActivity: GccChatActivity,
        message: GccChatMessage,
        threadBinding: ItemThreadTitleBinding,
        reactionsBinding: ReactionsInsideMessageBinding,
        openThread: (message: GccChatMessage) -> Unit
    ) {
        val isFirstMessageOfThreadInNormalChat = chatActivity.conversationThreadId == null && message.isThread
        if (isFirstMessageOfThreadInNormalChat) {
            threadBinding.threadTitleLayout.visibility = View.VISIBLE

            threadBinding.threadTitleLayout.findViewById<androidx.emoji2.widget.EmojiTextView>(R.id.threadTitle).text =
                message.threadTitle

            reactionsBinding.threadButton.visibility = View.VISIBLE

            reactionsBinding.threadButton.setContent {
                ThreadButtonComposable(
                    message.threadReplies ?: 0,
                    onButtonClick = { openThread(message) }
                )
            }
        } else {
            threadBinding.threadTitleLayout.visibility = View.GONE
            reactionsBinding.threadButton.visibility = View.GONE
        }
    }
}
