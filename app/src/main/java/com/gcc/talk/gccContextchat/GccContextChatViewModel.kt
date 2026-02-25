/*
 * Nextcloud Talk - Android Client
 *
 * SPDX-FileCopyrightText: 2025 Marcel Hibbe <dev@mhibbe.de>
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.gcc.talk.gccContextchat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import autodagger.AutoInjector
import com.gcc.talk.gccApplication.GccTalkApplication
import com.gcc.talk.gccChat.data.network.GccChatNetworkDataSource
import com.gcc.talk.gccChat.viewmodels.GccChatViewModel
import com.gcc.talk.gccModels.json.chat.ChatMessageJson
import com.gcc.talk.gccUsers.GccUserManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@AutoInjector(GccTalkApplication::class)
class GccContextChatViewModel @Inject constructor(private val chatNetworkDataSource: GccChatNetworkDataSource) :
    ViewModel() {

    @Inject
    lateinit var chatViewModel: GccChatViewModel

    @Inject
    lateinit var userManager: GccUserManager

    var threadId: String? = null

    private val _getContextChatMessagesState =
        MutableStateFlow<ContextChatRetrieveUiState>(ContextChatRetrieveUiState.None)
    val getContextChatMessagesState: StateFlow<ContextChatRetrieveUiState> = _getContextChatMessagesState

    @Suppress("LongParameterList")
    fun getContextForChatMessages(
        credentials: String,
        baseUrl: String,
        token: String,
        threadId: String?,
        messageId: String,
        title: String
    ) {
        viewModelScope.launch {
            val user = userManager.currentUser.blockingGet()

            if (!user.hasSpreedFeatureCapability("chat-get-context") ||
                !user.hasSpreedFeatureCapability("federation-v1")
            ) {
                _getContextChatMessagesState.value = ContextChatRetrieveUiState.Error
            }

            var messages = chatNetworkDataSource.getContextForChatMessage(
                credentials = credentials,
                baseUrl = baseUrl,
                token = token,
                messageId = messageId,
                limit = LIMIT,
                threadId = threadId?.toIntOrNull()
            )

            if (threadId.isNullOrEmpty()) {
                messages = messages.filter { !isThreadChildMessage(it) }
            }

            val subTitle = if (threadId?.isNotEmpty() == true) {
                messages.firstOrNull()?.threadTitle
            } else {
                ""
            }

            _getContextChatMessagesState.value = ContextChatRetrieveUiState.Success(
                messageId = messageId,
                threadId = threadId,
                messages = messages,
                title = title,
                subTitle = subTitle
            )
        }
    }

    fun isThreadChildMessage(currentMessage: ChatMessageJson): Boolean =
        currentMessage.hasThread &&
            currentMessage.threadId != currentMessage.id

    fun clearContextChatState() {
        _getContextChatMessagesState.value = ContextChatRetrieveUiState.None
    }

    sealed class ContextChatRetrieveUiState {
        data object None : ContextChatRetrieveUiState()
        data class Success(
            val messageId: String,
            val threadId: String?,
            val messages: List<ChatMessageJson>,
            val title: String?,
            val subTitle: String?
        ) : ContextChatRetrieveUiState()
        data object Error : ContextChatRetrieveUiState()
    }

    companion object {
        private const val LIMIT = 50
    }
}
