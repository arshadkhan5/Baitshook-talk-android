/*
 * Nextcloud Talk - Android Client
 *
 * SPDX-FileCopyrightText: 2024 Christian Reiner <foss@christian-reiner.info>
 * SPDX-FileCopyrightText: 2021 Andy Scherzinger <info@andy-scherzinger.de>
 * SPDX-FileCopyrightText: 2021 Marcel Hibbe <dev@mhibbe.de>
 * SPDX-FileCopyrightText: 2017-2018 Mario Danic <mario@lovelyhq.com>
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package com.gcc.talk.gccAdapters.messages

import android.content.Context
import android.util.Log
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import androidx.core.content.res.ResourcesCompat
import androidx.lifecycle.lifecycleScope
import autodagger.AutoInjector
import coil.load
import com.google.android.flexbox.FlexboxLayout
import com.google.android.material.snackbar.Snackbar
import com.nextcloud.android.common.ui.theme.utils.ColorRole
import com.gcc.talk.R
import com.gcc.talk.gccApplication.GccTalkApplication
import com.gcc.talk.gccApplication.GccTalkApplication.Companion.sharedApplication
import com.gcc.talk.gccChat.GccChatActivity
import com.gcc.talk.gccChat.data.GccChatMessageRepository
import com.gcc.talk.gccChat.data.model.GccChatMessage
import com.gcc.talk.gccData.database.model.GccSendStatus
import com.gcc.talk.gccData.network.GccNetworkMonitor
import com.gcc.talk.gccData.user.model.GccUser
import com.gcc.talk.databinding.ItemCustomOutcomingTextMessageBinding
import com.gcc.talk.gccModels.json.chat.ChatUtils
import com.gcc.talk.gccModels.json.chat.ReadStatus
import com.gcc.talk.gccModels.json.conversations.ConversationEnums
import com.gcc.talk.gccUi.theme.ViewThemeUtils
import com.gcc.talk.gccUtils.GccApiUtils
import com.gcc.talk.gccUtils.CapabilitiesUtil.hasSpreedFeatureCapability
import com.gcc.talk.gccUtilss.GccDateUtils
import com.gcc.talk.gccUtils.SpreedFeatures
import com.gcc.talk.gccUtils.GccTextMatchers
import com.gcc.talk.gccUtils.database.user.GccCurrentUserProviderOld
import com.gcc.talk.gccUtils.message.GccMessageCheckboxUtils.addCheckboxLine
import com.gcc.talk.gccUtils.message.GccMessageCheckboxUtils.addPlainTextLine
import com.gcc.talk.gccUtils.message.GccMessageCheckboxUtils.matchCheckbox
import com.gcc.talk.gccUtils.message.GccMessageCheckboxUtils.updateMessageWithCheckboxStates
import com.gcc.talk.gccUtils.message.GccMessageUtils
import com.stfalcon.chatkit.messages.MessageHolders.OutcomingTextMessageViewHolder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Date
import javax.inject.Inject

@AutoInjector(GccTalkApplication::class)
class GccOutcomingTextMessageViewHolder(itemView: View) :
    OutcomingTextMessageViewHolder<GccChatMessage>(itemView),
    GccAdjustableMessageHolderInterface {

    override val binding: ItemCustomOutcomingTextMessageBinding = ItemCustomOutcomingTextMessageBinding.bind(itemView)
    private val realView: View = itemView

    @Inject
    lateinit var context: Context

    @Inject
    lateinit var viewThemeUtils: ViewThemeUtils

    @Inject
    lateinit var messageUtils: GccMessageUtils

    @Inject
    lateinit var dateUtils: GccDateUtils

    @Inject
    lateinit var networkMonitor: GccNetworkMonitor

    lateinit var commonMessageInterface: CommonMessageInterface

    @Inject
    lateinit var chatRepository: GccChatMessageRepository

    @Inject
    lateinit var currentUserProvider: GccCurrentUserProviderOld

    private var job: Job? = null

    @Suppress("Detekt.LongMethod")
    override fun onBind(message: GccChatMessage) {
        super.onBind(message)
        sharedApplication!!.componentApplication.inject(this)
        val user = currentUserProvider.currentUser.blockingGet()
        val hasCheckboxes = processCheckboxes(
            message,
            user
        )
        processMessage(message, hasCheckboxes)
    }

    @Suppress("Detekt.LongMethod")
    private fun processMessage(message: GccChatMessage, hasCheckboxes: Boolean) {
        var isBubbled = true
        val layoutParams = binding.messageTime.layoutParams as FlexboxLayout.LayoutParams
        var textSize = context.resources.getDimension(R.dimen.chat_text_size)
        if (!hasCheckboxes) {
            realView.isSelected = false
            layoutParams.isWrapBefore = false

            binding.messageText.visibility = View.VISIBLE
            binding.checkboxContainer.visibility = View.GONE

            var processedMessageText = messageUtils.enrichChatMessageText(
                binding.messageText.context,
                message,
                false,
                viewThemeUtils
            )

            val spansFromString: Array<Any> = processedMessageText!!.getSpans(
                0,
                processedMessageText.length,
                Any::class.java
            )

            if (spansFromString.isNotEmpty()) {
                binding.bubble.layoutParams.apply {
                    width = FlexboxLayout.LayoutParams.MATCH_PARENT
                }
                binding.messageText.layoutParams.apply {
                    width = FlexboxLayout.LayoutParams.MATCH_PARENT
                }
            } else {
                binding.bubble.layoutParams.apply {
                    width = FlexboxLayout.LayoutParams.WRAP_CONTENT
                }
                binding.messageText.layoutParams.apply {
                    width = FlexboxLayout.LayoutParams.WRAP_CONTENT
                }
            }

            processedMessageText = messageUtils.processMessageParameters(
                binding.messageText.context,
                viewThemeUtils,
                processedMessageText,
                message,
                itemView
            )

            if (
                (message.messageParameters == null || message.messageParameters!!.size <= 0) &&
                GccTextMatchers.isMessageWithSingleEmoticonOnly(message.text)
            ) {
                textSize = (textSize * TEXT_SIZE_MULTIPLIER).toFloat()
                layoutParams.isWrapBefore = true
                realView.isSelected = true
                isBubbled = false
            }

            binding.messageTime.layoutParams = layoutParams
            viewThemeUtils.platform.colorTextView(binding.messageText, ColorRole.ON_SURFACE_VARIANT)
            binding.messageText.text = processedMessageText
            // just for debugging:
            // binding.messageText.text =
            //     SpannableStringBuilder(processedMessageText).append(" (" + message.jsonMessageId + ")")
        } else {
            binding.messageText.visibility = View.GONE
            binding.checkboxContainer.visibility = View.VISIBLE
        }
        binding.messageText.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize)
        if (message.lastEditTimestamp != 0L && !message.isDeleted) {
            binding.messageEditIndicator.visibility = View.VISIBLE
            binding.messageTime.text = dateUtils.getLocalTimeStringFromTimestamp(message.lastEditTimestamp!!)
        } else {
            binding.messageEditIndicator.visibility = View.GONE
            binding.messageTime.text = dateUtils.getLocalTimeStringFromTimestamp(message.timestamp)
        }
        viewThemeUtils.platform.colorTextView(binding.messageTime, ColorRole.ON_SURFACE_VARIANT)
        setBubbleOnChatMessage(message)

        // parent message handling
        val chatActivity = commonMessageInterface as? GccChatActivity
        binding.messageQuote.quotedChatMessageView.visibility =
            if (chatActivity != null &&
                !message.isDeleted &&
                message.parentMessageId != null &&
                message.parentMessageId != chatActivity.conversationThreadId
            ) {
                processParentMessage(message)
                View.VISIBLE
            } else {
                View.GONE
            }

        binding.messageQuote.quotedChatMessageView.setOnLongClickListener { l: View? ->
            commonMessageInterface.onOpenMessageActionsDialog(message)
            true
        }

        binding.checkMark.visibility = View.INVISIBLE
        binding.sendingProgress.visibility = View.GONE

        if (message.sendStatus == GccSendStatus.FAILED) {
            updateStatus(R.drawable.baseline_error_outline_24, context.resources?.getString(R.string.nc_message_failed))
        } else if (message.isTemporary) {
            updateStatus(R.drawable.baseline_schedule_24, context.resources?.getString(R.string.nc_message_sending))
        } else if (message.readStatus == ReadStatus.READ) {
            updateStatus(R.drawable.ic_check_all, context.resources?.getString(R.string.nc_message_read))
        } else if (message.readStatus == ReadStatus.SENT) {
            updateStatus(R.drawable.ic_check, context.resources?.getString(R.string.nc_message_sent))
        }

        chatActivity?.lifecycleScope?.launch {
            if (message.isTemporary && !networkMonitor.isOnline.value) {
                updateStatus(
                    R.drawable.ic_signal_wifi_off_white_24dp,
                    context.resources?.getString(R.string.nc_message_offline)
                )
            }
        }

        itemView.setTag(R.string.replyable_message_view_tag, message.replyable)

        if (chatActivity != null) {
            GccThread().showThreadPreview(
                chatActivity,
                message,
                threadBinding = binding.threadTitleWrapper,
                reactionsBinding = binding.reactions,
                openThread = { openThread(message) }
            )
        }

        GccReaction().showReactions(
            message,
            ::clickOnReaction,
            ::longClickOnReaction,
            binding.reactions,
            context,
            true,
            viewThemeUtils,
            isBubbled
        )
    }

    private fun processCheckboxes(chatMessage: GccChatMessage, user: GccUser): Boolean {
        val chatActivity = commonMessageInterface as GccChatActivity
        val message = chatMessage.message ?: return false
        val checkBoxContainer = binding.checkboxContainer

        val isOlderThanTwentyFourHours = chatMessage
            .createdAt
            .before(
                Date(System.currentTimeMillis() - AGE_THRESHOLD_FOR_EDIT_MESSAGE)
            )
        val messageIsEditable = hasSpreedFeatureCapability(
            user.capabilities?.spreedCapability!!,
            SpreedFeatures.EDIT_MESSAGES
        ) &&
            !isOlderThanTwentyFourHours

        val isNoLimitOnNoteToSelf = hasSpreedFeatureCapability(
            user.capabilities?.spreedCapability!!,
            SpreedFeatures.EDIT_MESSAGES_NOTE_TO_SELF
        ) &&
            chatActivity.currentConversation?.type == ConversationEnums.ConversationType.NOTE_TO_SELF

        checkBoxContainer.removeAllViews()
        return renderCheckboxLines(
            chatMessage,
            user,
            message,
            messageIsEditable || isNoLimitOnNoteToSelf,
            checkBoxContainer
        )
    }

    private fun renderCheckboxLines(
        chatMessage: GccChatMessage,
        user: GccUser,
        message: String,
        editable: Boolean,
        checkBoxContainer: ViewGroup
    ): Boolean {
        val checkboxList = mutableListOf<CheckBox>()
        val chatActivity = commonMessageInterface as GccChatActivity
        var hasCheckbox = false
        val marginPx = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            PADDING_FOUR_FLOAT,
            context.resources.displayMetrics
        ).toInt()

        message.lines().forEach { line ->
            val trimmed = line.trimEnd()
            val match = matchCheckbox(trimmed)
            if (match != null) {
                hasCheckbox = true
                val isChecked = match.groupValues[CHECKED_GROUP_INDEX].equals("X", true)
                val taskText = match.groupValues[TASK_TEXT_GROUP_INDEX].trim()
                val checkBox = addCheckboxLine(
                    context = chatActivity,
                    container = checkBoxContainer,
                    chatMessage = chatMessage,
                    taskText = taskText,
                    isChecked = isChecked,
                    isEnabled = editable,
                    isIncomingMessage = false,
                    messageUtils = messageUtils,
                    viewThemeUtils = viewThemeUtils,
                    linkColorRes = R.color.no_emphasis_text,
                    paddingPx = marginPx
                ) { _, _ ->
                    updateCheckboxStates(chatMessage, user, checkboxList)
                }
                checkboxList.add(checkBox)
            } else if (trimmed.isNotBlank()) {
                addPlainTextLine(
                    context = checkBoxContainer.context,
                    container = checkBoxContainer,
                    chatMessage = chatMessage,
                    text = trimmed,
                    isIncomingMessage = false,
                    messageUtils = messageUtils,
                    viewThemeUtils = viewThemeUtils,
                    linkColorRes = R.color.no_emphasis_text,
                    paddingPx = marginPx
                )
            }
        }
        return hasCheckbox
    }

    private fun updateCheckboxStates(chatMessage: GccChatMessage, user: GccUser, checkboxes: List<CheckBox>) {
        job = CoroutineScope(Dispatchers.Main).launch {
            withContext(Dispatchers.IO) {
                val apiVersion: Int = GccApiUtils.getChatApiVersion(
                    user.capabilities?.spreedCapability!!,
                    intArrayOf(1)
                )
                val updatedMessage = updateMessageWithCheckboxStates(chatMessage.message!!, checkboxes)
                val messageParameters = chatMessage.messageParameters
                val messageToSend = if (!messageParameters.isNullOrEmpty()) {
                    val parsedMessage = ChatUtils.getParsedMessage(updatedMessage, messageParameters) ?: updatedMessage
                    messageUtils.processEditMessageParameters(
                        messageParameters,
                        chatMessage,
                        parsedMessage
                    ).toString()
                } else {
                    updatedMessage
                }

                chatRepository.editChatMessage(
                    user.getCredentials(),
                    GccApiUtils.getUrlForChatMessage(apiVersion, user.baseUrl!!, chatMessage.token!!, chatMessage.id),
                    messageToSend
                ).collect { result ->
                    withContext(Dispatchers.Main) {
                        if (result.isSuccess) {
                            val editedMessage = result.getOrNull()?.ocs?.data!!.parentMessage!!
                            Log.d(TAG, "EditedMessage: $editedMessage")
                            binding.messageEditIndicator.apply {
                                visibility = View.VISIBLE
                            }
                            binding.messageTime.text =
                                dateUtils.getLocalTimeStringFromTimestamp(editedMessage.lastEditTimestamp!!)
                        } else {
                            Snackbar.make(binding.root, R.string.nc_common_error_sorry, Snackbar.LENGTH_LONG).show()
                        }
                    }
                }
            }
        }
    }

    private fun updateStatus(readStatusDrawableInt: Int, description: String?) {
        binding.sendingProgress.visibility = View.GONE
        binding.checkMark.visibility = View.VISIBLE
        readStatusDrawableInt.let { drawableInt ->
            ResourcesCompat.getDrawable(context.resources, drawableInt, null)?.let {
                binding.checkMark.setImageDrawable(it)
                viewThemeUtils.talk.themeMessageCheckMark(binding.checkMark)
            }
        }
        binding.checkMark.contentDescription = description
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

    @Suppress("Detekt.TooGenericExceptionCaught")
    private fun processParentMessage(message: GccChatMessage) {
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

                    binding.messageQuote.quotedChatMessageView.setOnClickListener {
                        chatActivity.jumpToQuotedMessage(parentChatMessage)
                    }
                } catch (e: Exception) {
                    Log.d(TAG, "Error when processing parent message in view holder", e)
                }
            }
        }
    }

    private fun setBubbleOnChatMessage(message: GccChatMessage) {
        viewThemeUtils.talk.themeOutgoingMessageBubble(bubble, message.isGrouped, message.isDeleted)
    }

    fun assignCommonMessageInterface(commonMessageInterface: CommonMessageInterface) {
        this.commonMessageInterface = commonMessageInterface
    }

    override fun viewDetached() {
        super.viewDetached()
        job?.cancel()
    }

    companion object {
        const val TEXT_SIZE_MULTIPLIER = 2.5
        private val TAG = OutcomingTextMessageViewHolder::class.java.simpleName
        private const val AGE_THRESHOLD_FOR_EDIT_MESSAGE: Long = 86400000
        private const val CHECKED_GROUP_INDEX = 1
        private const val TASK_TEXT_GROUP_INDEX = 2
        private const val PADDING_FOUR_FLOAT = 4f
    }
}
