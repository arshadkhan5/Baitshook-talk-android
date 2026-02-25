/*
 * Nextcloud Talk - Android Client
 *
 * SPDX-FileCopyrightText: 2026 Sowjanya Kota <sowjanya.kch@gmail.com>
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.gcc.talk.gccChat.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gcc.talk.gccChat.data.GccChatMessageRepository
import com.gcc.talk.gccChat.data.model.GccChatMessage
import com.gcc.talk.gccData.user.model.GccUser
import com.gcc.talk.gccUtils.GccApiUtils
import com.gcc.talk.gccUtils.database.user.GccCurrentUserProvider
import com.gcc.talk.gccUtils.message.GccSendMessageUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

class GccScheduledMessagesViewModel @Inject constructor(
    private val chatRepository: GccChatMessageRepository,
    private val currentUserProvider: GccCurrentUserProvider
) : ViewModel() {

    sealed interface GetScheduledMessagesState
    object GetScheduledMessagesIdleState : GetScheduledMessagesState
    object GetScheduledMessagesLoadingState : GetScheduledMessagesState
    data class GetScheduledMessagesSuccessState(val messages: List<GccChatMessage>) : GetScheduledMessagesState
    data class GetScheduledMessagesErrorState(val error: Throwable? = null) : GetScheduledMessagesState

    private val _getScheduledMessagesState: MutableStateFlow<GetScheduledMessagesState> =
        MutableStateFlow(GetScheduledMessagesIdleState)
    val getScheduledMessagesState: StateFlow<GetScheduledMessagesState>
        get() = _getScheduledMessagesState

    sealed interface ScheduledMessageActionState
    object ScheduledMessageActionIdleState : ScheduledMessageActionState
    object ScheduledMessageActionLoadingState : ScheduledMessageActionState
    data class ScheduledMessageActionSuccessState(val response: GccChatMessage? = null) :
        ScheduledMessageActionState

    data class ScheduledMessageErrorState(val error: Throwable? = null) : ScheduledMessageActionState

    private val _currentUserState = MutableStateFlow<GccUser?>(null)
    val currentUserState: StateFlow<GccUser?> = _currentUserState

    sealed interface SendNowMessageState
    object SendNowMessageIdleState : SendNowMessageState
    object SendNowMessageLoadingState : SendNowMessageState
    data class SendNowMessageSuccessState(val message: GccChatMessage? = null) : SendNowMessageState
    data class SendNowMessageErrorState(val error: Throwable? = null) : SendNowMessageState

    private val _sendNowState: MutableStateFlow<SendNowMessageState> =
        MutableStateFlow(SendNowMessageIdleState)
    val sendNowState: StateFlow<SendNowMessageState>
        get() = _sendNowState

    private val _rescheduleState: MutableStateFlow<ScheduledMessageActionState> =
        MutableStateFlow(ScheduledMessageActionIdleState)
    val rescheduleState: StateFlow<ScheduledMessageActionState>
        get() = _rescheduleState

    private val _editState: MutableStateFlow<ScheduledMessageActionState> =
        MutableStateFlow(ScheduledMessageActionIdleState)
    val editState: StateFlow<ScheduledMessageActionState>
        get() = _editState

    private val _deleteState: MutableStateFlow<ScheduledMessageActionState> =
        MutableStateFlow(ScheduledMessageActionIdleState)
    val deleteState: StateFlow<ScheduledMessageActionState>
        get() = _deleteState

    private val _parentMessages =
        MutableStateFlow<Map<Long, GccChatMessage>>(emptyMap())

    val parentMessages: StateFlow<Map<Long, GccChatMessage>> =
        _parentMessages.asStateFlow()

    fun loadScheduledMessages(credentials: String, url: String) {
        _getScheduledMessagesState.value = GetScheduledMessagesLoadingState
        viewModelScope.launch {
            chatRepository.getScheduledChatMessages(credentials, url).collect { result ->
                if (result.isSuccess) {
                    _getScheduledMessagesState.value =
                        GetScheduledMessagesSuccessState(result.getOrNull().orEmpty())
                } else {
                    _getScheduledMessagesState.value = GetScheduledMessagesErrorState(result.exceptionOrNull())
                }
            }
        }
    }

    fun loadCurrentUser() {
        viewModelScope.launch {
            val user = currentUserProvider.getCurrentUser().getOrNull()
            _currentUserState.value = user
        }
    }

    @Suppress("LongParameterList")
    fun sendNow(
        credentials: String,
        sendUrl: String,
        message: String,
        displayName: String,
        replyTo: Int,
        sendWithoutNotification: Boolean,
        threadTitle: String?,
        deleteUrl: String
    ) {
        val referenceId = GccSendMessageUtils().generateReferenceId()
        _sendNowState.value = SendNowMessageLoadingState
        viewModelScope.launch {
            chatRepository.sendChatMessage(
                credentials = credentials,
                url = sendUrl,
                message = message,
                displayName = displayName,
                replyTo = replyTo,
                sendWithoutNotification = sendWithoutNotification,
                referenceId = referenceId,
                threadTitle = threadTitle
            ).collect { result ->
                if (result.isSuccess) {
                    SendNowMessageSuccessState(result.getOrNull())
                    deleteScheduledMessage(credentials, deleteUrl)
                } else {
                    SendNowMessageErrorState(result.exceptionOrNull())
                }
            }
        }
    }

    @Suppress("LongParameterList")
    fun reschedule(
        credentials: String,
        url: String,
        message: String,
        sendAt: Int?,
        replyTo: Int?,
        sendWithoutNotification: Boolean,
        threadTitle: String?,
        threadId: Long?
    ) {
        _rescheduleState.value = ScheduledMessageActionLoadingState
        viewModelScope.launch {
            chatRepository.updateScheduledChatMessage(
                credentials,
                url,
                message,
                sendAt,
                replyTo,
                sendWithoutNotification,
                threadTitle,
                threadId
            ).collect { result ->
                if (result.isSuccess) {
                    _rescheduleState.value =
                        ScheduledMessageActionSuccessState(result.getOrNull())
                } else {
                    _rescheduleState.value =
                        ScheduledMessageErrorState(result.exceptionOrNull())
                }
            }
        }
    }

    fun requestParentMessage(token: String, parentMessageId: Long, threadId: Long?) {
        if (_parentMessages.value.containsKey(parentMessageId)) return
        viewModelScope.launch {
            val parent = getParentMessageById(token, parentMessageId, threadId)
            parent?.let {
                _parentMessages.update { old ->
                    old + (parentMessageId to it)
                }
            }
        }
    }

    private suspend fun getParentMessageById(token: String, parentMessageId: Long, threadId: Long?): GccChatMessage? =
        withContext(Dispatchers.IO) {
            val userResult = currentUserProvider.getCurrentUser()
            val user = userResult.getOrElse { return@withContext null }

            val credentials = user.getCredentials()

            val apiVersion = GccApiUtils.getChatApiVersion(
                user.capabilities?.spreedCapability ?: return@withContext null,
                intArrayOf(GccApiUtils.API_V1)
            )

            val chatUrl = GccApiUtils.getUrlForChat(apiVersion, user.baseUrl, token)

            chatRepository.initData(
                user,
                credentials,
                chatUrl,
                token,
                threadId
            )

            chatRepository.getParentMessageById(parentMessageId).firstOrNull()
        }

    @Suppress("LongParameterList")
    fun edit(
        credentials: String,
        url: String,
        message: String,
        sendAt: Int?,
        replyTo: Int?,
        sendWithoutNotification: Boolean,
        threadTitle: String?,
        threadId: Long?
    ) {
        _editState.value = ScheduledMessageActionLoadingState
        viewModelScope.launch {
            chatRepository.updateScheduledChatMessage(
                credentials,
                url,
                message,
                sendAt,
                replyTo,
                sendWithoutNotification,
                threadTitle,
                threadId
            ).collect { result ->
                if (result.isSuccess) {
                    _editState.value =
                        ScheduledMessageActionSuccessState(result.getOrNull())
                } else {
                    _editState.value =
                        ScheduledMessageErrorState(result.exceptionOrNull())
                }
            }
        }
    }

    fun deleteScheduledMessage(credentials: String, url: String) {
        _deleteState.value = ScheduledMessageActionLoadingState
        viewModelScope.launch {
            chatRepository.deleteScheduledChatMessage(credentials, url).collect { result ->
                if (result.isSuccess) {
                    _deleteState.value =
                        ScheduledMessageActionSuccessState()
                } else {
                    _deleteState.value =
                        ScheduledMessageErrorState(result.exceptionOrNull())
                }
            }
        }
    }
}
