/*
 * Nextcloud Talk - Android Client
 *
 * SPDX-FileCopyrightText: 2021-2023 Marcel Hibbe <dev@mhibbe.de>
 * SPDX-FileCopyrightText: 2021 Andy Scherzinger <info@andy-scherzinger.de>
 * SPDX-FileCopyrightText: 2021 Tim Krüger <t@timkrueger.me>
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
import com.gcc.talk.gccData.user.model.GccUser
import com.gcc.talk.databinding.ItemCustomIncomingTextMessageBinding
import com.gcc.talk.gccModels.json.chat.ChatUtils
import com.gcc.talk.gccUi.theme.ViewThemeUtils
import com.gcc.talk.gccUtils.GccApiUtils
import com.gcc.talk.gccUtils.CapabilitiesUtil.hasSpreedFeatureCapability
import com.gcc.talk.gccUtils.GccChatMessageUtils
import com.gcc.talk.gccUtilss.GccDateUtils
import com.gcc.talk.gccUtils.SpreedFeatures
import com.gcc.talk.gccUtils.GccTextMatchers
import com.gcc.talk.gccUtils.database.user.GccCurrentUserProviderOld
import com.gcc.talk.gccUtils.message.GccMessageCheckboxUtils.addCheckboxLine
import com.gcc.talk.gccUtils.message.GccMessageCheckboxUtils.addPlainTextLine
import com.gcc.talk.gccUtils.message.GccMessageCheckboxUtils.matchCheckbox
import com.gcc.talk.gccUtils.message.GccMessageCheckboxUtils.updateMessageWithCheckboxStates
import com.gcc.talk.gccUtils.message.GccMessageUtils
import com.gcc.talk.gccUtils.preferences.GccAppPreferences
import com.stfalcon.chatkit.messages.MessageHolders
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Date
import javax.inject.Inject

@AutoInjector(GccTalkApplication::class)
class GccIncomingTextMessageViewHolder(itemView: View, payload: Any) :
    MessageHolders.IncomingTextMessageViewHolder<GccChatMessage>(itemView, payload) {

    private val binding: ItemCustomIncomingTextMessageBinding = ItemCustomIncomingTextMessageBinding.bind(itemView)

    @Inject
    lateinit var context: Context

    @Inject
    lateinit var viewThemeUtils: ViewThemeUtils

    @Inject
    lateinit var messageUtils: GccMessageUtils

    @Inject
    lateinit var appPreferences: GccAppPreferences

    @Inject
    lateinit var dateUtils: GccDateUtils

    @Inject
    lateinit var currentUserProvider: GccCurrentUserProviderOld

    lateinit var commonMessageInterface: CommonMessageInterface

    @Inject
    lateinit var chatRepository: GccChatMessageRepository

    private var job: Job? = null

    override fun onBind(message: GccChatMessage) {
        super.onBind(message)
        sharedApplication!!.componentApplication.inject(this)
        setAvatarAndAuthorOnMessageItem(message)
        colorizeMessageBubble(message)
        itemView.isSelected = false
        val user = currentUserProvider.currentUser.blockingGet()
        val hasCheckboxes = processCheckboxes(
            message,
            user
        )
        processMessage(message, hasCheckboxes)
    }

    @Suppress("LongMethod")
    private fun processMessage(message: GccChatMessage, hasCheckboxes: Boolean) {
        var textSize = context.resources!!.getDimension(R.dimen.chat_text_size)
        if (!hasCheckboxes) {
            binding.messageText.visibility = View.VISIBLE
            binding.checkboxContainer.visibility = View.GONE
            var processedMessageText = messageUtils.enrichChatMessageText(
                binding.messageText.context,
                message,
                true,
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
            val messageParameters = message.messageParameters
            if (
                (messageParameters == null || messageParameters.size <= 0) &&
                GccTextMatchers.isMessageWithSingleEmoticonOnly(message.text)
            ) {
                textSize = (textSize * TEXT_SIZE_MULTIPLIER).toFloat()
                itemView.isSelected = true
                binding.messageAuthor.visibility = View.GONE
            }
            binding.messageText.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize)
            binding.messageText.text = processedMessageText
            // just for debugging:
            // binding.messageText.text =
            //     SpannableStringBuilder(processedMessageText).append(" (" + message.jsonMessageId + ")")
        } else {
            binding.checkboxContainer.visibility = View.VISIBLE
            binding.messageText.visibility = View.GONE
        }

        if (message.lastEditTimestamp != 0L && !message.isDeleted) {
            binding.messageEditIndicator.visibility = View.VISIBLE
            binding.messageTime.text = dateUtils.getLocalTimeStringFromTimestamp(message.lastEditTimestamp!!)
        } else {
            binding.messageEditIndicator.visibility = View.GONE
            binding.messageTime.text = dateUtils.getLocalTimeStringFromTimestamp(message.timestamp)
        }
        viewThemeUtils.platform.colorTextView(binding.messageTime, ColorRole.ON_SURFACE_VARIANT)

        // parent message handling
        val chatActivity = commonMessageInterface as GccChatActivity
        binding.messageQuote.quotedChatMessageView.visibility =
            if (!message.isDeleted &&
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

        itemView.setTag(R.string.replyable_message_view_tag, message.replyable)

        GccThread().showThreadPreview(
            chatActivity,
            message,
            threadBinding = binding.threadTitleWrapper,
            reactionsBinding = binding.reactions,
            openThread = { openThread(message) }
        )

        GccReaction().showReactions(
            message,
            ::clickOnReaction,
            ::longClickOnReaction,
            binding.reactions,
            binding.messageText.context,
            false,
            viewThemeUtils
        )
    }

    private fun processCheckboxes(chatMessage: GccChatMessage, user: GccUser): Boolean {
        val chatActivity = commonMessageInterface as GccChatActivity
        val message = chatMessage.message ?: return false
        val checkBoxContainer = binding.checkboxContainer
        checkBoxContainer.removeAllViews()

        val isEditable = hasSpreedFeatureCapability(
            user.capabilities?.spreedCapability!!,
            SpreedFeatures.EDIT_MESSAGES
        ) &&
            !chatMessage.createdAt.before(Date(System.currentTimeMillis() - AGE_THRESHOLD_FOR_EDIT_MESSAGE))

        val checkboxList = mutableListOf<CheckBox>()
        val spaceInPx = getBottomPaddingPx()
        var hasCheckbox = false

        message.lines().forEach { line ->
            if (addCheckboxOrTextView(
                    line.trimEnd(),
                    chatMessage,
                    user,
                    isEditable,
                    checkBoxContainer,
                    checkboxList,
                    spaceInPx,
                    chatActivity
                )
            ) {
                hasCheckbox = true
            }
        }
        return hasCheckbox
    }

    fun getBottomPaddingPx(): Int =
        TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            PADDING_FOUR.toFloat(),
            context.resources.displayMetrics
        ).toInt()

    @Suppress("LongParameterList")
    private fun addCheckboxOrTextView(
        line: String,
        chatMessage: GccChatMessage,
        user: GccUser,
        isEditable: Boolean,
        container: ViewGroup,
        checkboxList: MutableList<CheckBox>,
        spaceInPx: Int,
        chatActivity: GccChatActivity
    ): Boolean {
        val match = matchCheckbox(line)
        return if (match != null) {
            val isChecked = match.groupValues[CHECKED_GROUP_INDEX].equals("X", true)
            val taskText = match.groupValues[TASK_TEXT_GROUP_INDEX].trim()
            val checkBox = addCheckboxLine(
                context = chatActivity,
                container = container,
                chatMessage = chatMessage,
                taskText = taskText,
                isChecked = isChecked,
                isEnabled = (
                    chatMessage.actorType == "bots" ||
                        chatActivity.userAllowedByPrivilages(chatMessage)
                    ) &&
                    isEditable,
                isIncomingMessage = true,
                messageUtils = messageUtils,
                viewThemeUtils = viewThemeUtils,
                linkColorRes = R.color.no_emphasis_text,
                paddingPx = spaceInPx
            ) { _, _ ->
                updateCheckboxStates(chatMessage, user, checkboxList)
            }
            checkboxList.add(checkBox)
            true
        } else if (line.isNotBlank()) {
            addPlainTextLine(
                context = container.context,
                container = container,
                chatMessage = chatMessage,
                text = line,
                isIncomingMessage = true,
                messageUtils = messageUtils,
                viewThemeUtils = viewThemeUtils,
                linkColorRes = R.color.no_emphasis_text,
                paddingPx = spaceInPx
            )
            false
        } else {
            false
        }
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

    private fun longClickOnReaction(chatMessage: GccChatMessage) {
        commonMessageInterface.onLongClickReactions(chatMessage)
    }

    private fun clickOnReaction(chatMessage: GccChatMessage, emoji: String) {
        commonMessageInterface.onClickReaction(chatMessage, emoji)
    }

    private fun openThread(chatMessage: GccChatMessage) {
        commonMessageInterface.openThread(chatMessage)
    }

    private fun setAvatarAndAuthorOnMessageItem(message: GccChatMessage) {
        val actorName = message.actorDisplayName
        if (!actorName.isNullOrBlank()) {
            binding.messageAuthor.visibility = View.VISIBLE
            binding.messageAuthor.text = actorName
            binding.messageUserAvatar.setOnClickListener {
                (payload as? GccMessagePayload)?.profileBottomSheet?.showFor(message, itemView.context)
            }
        } else {
            binding.messageAuthor.setText(R.string.nc_nick_guest)
        }

        if (!message.isGrouped && !message.isOneToOneConversation && !message.isFormerOneToOneConversation) {
            GccChatMessageUtils().setAvatarOnMessage(binding.messageUserAvatar, message, viewThemeUtils)
        } else {
            if (message.isOneToOneConversation || message.isFormerOneToOneConversation) {
                binding.messageUserAvatar.visibility = View.GONE
            } else {
                binding.messageUserAvatar.visibility = View.INVISIBLE
            }
            binding.messageAuthor.visibility = View.GONE
        }
    }

    private fun colorizeMessageBubble(message: GccChatMessage) {
        viewThemeUtils.talk.themeIncomingMessageBubble(bubble, message.isGrouped, message.isDeleted)
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
                    binding.messageQuote.quotedMessageAuthor.text =
                        if (parentChatMessage.actorDisplayName.isNullOrEmpty()) {
                            context.getText(R.string.nc_nick_guest)
                        } else {
                            parentChatMessage.actorDisplayName
                        }

                    binding.messageQuote.quotedMessage.text = messageUtils
                        .enrichChatReplyMessageText(
                            binding.messageQuote.quotedMessage.context,
                            parentChatMessage,
                            true,
                            viewThemeUtils
                        )

                    viewThemeUtils.talk.themeParentMessage(
                        parentChatMessage,
                        message,
                        binding.messageQuote.quotedChatMessageView,
                        R.color.high_emphasis_text
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

    fun assignCommonMessageInterface(commonMessageInterface: CommonMessageInterface) {
        this.commonMessageInterface = commonMessageInterface
    }

    override fun viewDetached() {
        super.viewDetached()
        job?.cancel()
    }

    companion object {
        const val TEXT_SIZE_MULTIPLIER = 2.5
        private val TAG = GccIncomingTextMessageViewHolder::class.java.simpleName
        private const val AGE_THRESHOLD_FOR_EDIT_MESSAGE: Long = 86400000
        private const val CHECKED_GROUP_INDEX = 1
        private const val TASK_TEXT_GROUP_INDEX = 2
        private const val PADDING_FOUR = 4
    }
}
