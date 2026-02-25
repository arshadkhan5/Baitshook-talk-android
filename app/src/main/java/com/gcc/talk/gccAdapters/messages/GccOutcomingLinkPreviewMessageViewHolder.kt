/*
 * Nextcloud Talk - Android Client
 *
 * SPDX-FileCopyrightText: 2024 Christian Reiner <foss@christian-reiner.info>
 * SPDX-FileCopyrightText: 2022 Marcel Hibbe <dev@mhibbe.de>
 * SPDX-FileCopyrightText: 2017-2019 Mario Danic <mario@lovelyhq.com>
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package com.gcc.talk.gccAdapters.messages

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import android.view.View
import androidx.appcompat.content.res.AppCompatResources
import autodagger.AutoInjector
import coil.load
import com.nextcloud.android.common.ui.theme.utils.ColorRole
import com.gcc.talk.R
import com.gcc.talk.gccApi.GccNcApi
import com.gcc.talk.gccApplication.GccTalkApplication
import com.gcc.talk.gccApplication.GccTalkApplication.Companion.sharedApplication
import com.gcc.talk.gccChat.GccChatActivity
import com.gcc.talk.gccChat.data.model.GccChatMessage
import com.gcc.talk.databinding.ItemCustomOutcomingLinkPreviewMessageBinding
import com.gcc.talk.gccModels.json.chat.ReadStatus
import com.gcc.talk.gccUi.theme.ViewThemeUtils
import com.gcc.talk.gccUtils.GccApiUtils
import com.gcc.talk.gccUtilss.GccDateUtils
import com.gcc.talk.gccUtils.message.GccMessageUtils
import com.gcc.talk.gccUtils.preferences.GccAppPreferences
import com.stfalcon.chatkit.messages.MessageHolders
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@AutoInjector(GccTalkApplication::class)
class GccOutcomingLinkPreviewMessageViewHolder(outcomingView: View, payload: Any) :
    MessageHolders.OutcomingTextMessageViewHolder<GccChatMessage>(outcomingView, payload),
    GccAdjustableMessageHolderInterface {

    override val binding: ItemCustomOutcomingLinkPreviewMessageBinding =
        ItemCustomOutcomingLinkPreviewMessageBinding.bind(itemView)

    @Inject
    lateinit var context: Context

    @Inject
    lateinit var viewThemeUtils: ViewThemeUtils

    @Inject
    lateinit var messageUtils: GccMessageUtils

    @Inject
    lateinit var dateUtils: GccDateUtils

    @Inject
    lateinit var appPreferences: GccAppPreferences

    @Inject
    lateinit var ncApi: GccNcApi

    lateinit var message: GccChatMessage

    lateinit var commonMessageInterface: CommonMessageInterface

    @SuppressLint("SetTextI18n")
    override fun onBind(message: GccChatMessage) {
        super.onBind(message)
        this.message = message
        sharedApplication!!.componentApplication.inject(this)
        viewThemeUtils.platform.colorTextView(binding.messageTime, ColorRole.ON_SURFACE_VARIANT)
        binding.messageTime.text = dateUtils.getLocalTimeStringFromTimestamp(message.timestamp)

        colorizeMessageBubble(message)
        var processedMessageText =
            messageUtils.enrichChatMessageText(binding.messageText.context, message, false, viewThemeUtils)
        processedMessageText = messageUtils.processMessageParameters(
            binding.messageText.context,
            viewThemeUtils,
            processedMessageText!!,
            message,
            itemView
        )

        binding.messageText.text = processedMessageText

        itemView.isSelected = false

        // parent message handling
        setParentMessageDataOnMessageItem(message)

        val readStatusDrawableInt = when (message.readStatus) {
            ReadStatus.READ -> R.drawable.ic_check_all
            ReadStatus.SENT -> R.drawable.ic_check
            else -> null
        }

        val readStatusContentDescriptionString = when (message.readStatus) {
            ReadStatus.READ -> context.resources?.getString(R.string.nc_message_read)
            ReadStatus.SENT -> context.resources?.getString(R.string.nc_message_sent)
            else -> null
        }

        readStatusDrawableInt?.let { drawableInt ->
            AppCompatResources.getDrawable(context, drawableInt)?.let {
                binding.checkMark.setImageDrawable(it)
                viewThemeUtils.talk.themeMessageCheckMark(binding.checkMark)
            }
        }

        binding.checkMark.contentDescription = readStatusContentDescriptionString

        GccLinkPreview().showLink(
            message,
            ncApi,
            binding.referenceInclude,
            itemView.context
        )
        binding.referenceInclude.referenceWrapper.setOnLongClickListener { l: View? ->
            commonMessageInterface.onOpenMessageActionsDialog(message)
            true
        }

        itemView.setTag(R.string.replyable_message_view_tag, message.replyable)

        val chatActivity = commonMessageInterface as GccChatActivity
        val showThreadButton = chatActivity.conversationThreadId == null && message.isThread
        if (showThreadButton) {
            binding.reactions.threadButton.visibility = View.VISIBLE
            binding.reactions.threadButton.setContent {
                ThreadButtonComposable(
                    onButtonClick = { openThread(message) }
                )
            }
        } else {
            binding.reactions.threadButton.visibility = View.GONE
        }

        GccReaction().showReactions(
            message,
            ::clickOnReaction,
            ::longClickOnReaction,
            binding.reactions,
            binding.messageTime.context,
            true,
            viewThemeUtils
        )
    }

    private fun longClickOnReaction(chatMessage: GccChatMessage) {
        commonMessageInterface.onLongClickReactions(chatMessage)
    }

    private fun clickOnReaction(chatMessage: GccChatMessage, emoji: String) {
        commonMessageInterface.onClickReaction(chatMessage, emoji)
    }

    private fun openThread(chatMessage: GccChatMessage) {
        commonMessageInterface.openThread(chatMessage)
    }

    @Suppress("Detekt.TooGenericExceptionCaught", "Detekt.LongMethod")
    private fun setParentMessageDataOnMessageItem(message: GccChatMessage) {
        if (message.parentMessageId != null && !message.isDeleted) {
            CoroutineScope(Dispatchers.Main).launch {
                try {
                    val chatActivity = commonMessageInterface as GccChatActivity
                    val urlForChatting = GccApiUtils.getUrlForChat(
                        chatActivity.chatApiVersion,
                        chatActivity.conversationUser?.baseUrl,
                        chatActivity.roomToken
                    )

                    val parentChatMessage = withContext(Dispatchers.IO) {
                        chatActivity.chatViewModel.getMessageById(
                            urlForChatting,
                            chatActivity.currentConversation!!,
                            message.parentMessageId!!
                        ).first()
                    }
                    parentChatMessage.activeUser = message.activeUser
                    parentChatMessage.imageUrl?.let {
                        binding.messageQuote.quotedMessageImage.visibility = View.VISIBLE
                        binding.messageQuote.quotedMessageImage.load(it) {
                            addHeader(
                                "Authorization",
                                GccApiUtils.getCredentials(message.activeUser!!.username, message.activeUser!!.token)!!
                            )
                        }
                    } ?: run {
                        binding.messageQuote.quotedMessageImage.visibility = View.GONE
                    }
                    binding.messageQuote.quotedMessageAuthor.text = parentChatMessage.actorDisplayName
                        ?: context.getText(R.string.nc_nick_guest)
                    binding.messageQuote.quotedMessage.text = messageUtils
                        .enrichChatReplyMessageText(
                            binding.messageQuote.quotedMessage.context,
                            parentChatMessage,
                            false,
                            viewThemeUtils
                        )
                    viewThemeUtils.talk.colorOutgoingQuoteText(binding.messageQuote.quotedMessage)
                    viewThemeUtils.talk.colorOutgoingQuoteAuthorText(binding.messageQuote.quotedMessageAuthor)
                    viewThemeUtils.talk.themeParentMessage(
                        parentChatMessage,
                        message,
                        binding.messageQuote.quotedChatMessageView
                    )

                    binding.messageQuote.quotedChatMessageView.visibility =
                        if (!message.isDeleted &&
                            message.parentMessageId != null &&
                            message.parentMessageId != chatActivity.conversationThreadId
                        ) {
                            View.VISIBLE
                        } else {
                            View.GONE
                        }
                } catch (e: Exception) {
                    Log.d(TAG, "Error when processing parent message in view holder", e)
                }
            }
        } else {
            binding.messageQuote.quotedChatMessageView.visibility = View.GONE
        }
    }

    private fun colorizeMessageBubble(message: GccChatMessage) {
        viewThemeUtils.talk.themeOutgoingMessageBubble(bubble, message.isGrouped, message.isDeleted)
    }

    fun assignCommonMessageInterface(commonMessageInterface: CommonMessageInterface) {
        this.commonMessageInterface = commonMessageInterface
    }

    companion object {
        private val TAG = GccOutcomingLinkPreviewMessageViewHolder::class.java.simpleName
    }
}
