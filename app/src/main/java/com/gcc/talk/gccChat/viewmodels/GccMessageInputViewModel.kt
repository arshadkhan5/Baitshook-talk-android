/*
 * Nextcloud Talk - Android Client
 *
 * SPDX-FileCopyrightText: 2024 Julius Linus juliuslinus1@gmail.com
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.gcc.talk.gccChat.viewmodels

import android.content.Context
import android.util.Log
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import autodagger.AutoInjector
import com.gcc.talk.gccApplication.GccTalkApplication
import com.gcc.talk.gccChat.data.GccChatMessageRepository
import com.gcc.talk.gccChat.data.io.GccAudioFocusRequestManager
import com.gcc.talk.gccChat.data.io.GccAudioRecorderManager
import com.gcc.talk.gccChat.data.io.GccMediaPlayerManager
import com.gcc.talk.gccChat.data.model.GccChatMessage
import com.gcc.talk.gccChat.data.network.GccChatNetworkDataSource
import com.gcc.talk.gccModels.json.chat.ChatOverallSingleMessage
import com.gcc.talk.gccUtils.message.GccSendMessageUtils
import com.stfalcon.chatkit.commons.models.IMessage
import io.reactivex.disposables.Disposable
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@Suppress("Detekt.TooManyFunctions")
@AutoInjector(GccTalkApplication::class)
class GccMessageInputViewModel :
    ViewModel(),
    DefaultLifecycleObserver {

    enum class LifeCycleFlag {
        PAUSED,
        RESUMED,
        STOPPED
    }

    init {
        GccTalkApplication.sharedApplication?.componentApplication?.inject(this)
    }

    @Inject
    lateinit var audioRecorderManager: GccAudioRecorderManager

    @Inject
    lateinit var mediaPlayerManager: GccMediaPlayerManager

    @Inject
    lateinit var audioFocusRequestManager: GccAudioFocusRequestManager

    lateinit var chatRepository: GccChatMessageRepository
    lateinit var chatNetworkDataSource: GccChatNetworkDataSource
    lateinit var currentLifeCycleFlag: LifeCycleFlag
    val disposableSet = mutableSetOf<Disposable>()

    fun setData(chatMessageRepository: GccChatMessageRepository) {
        chatRepository = chatMessageRepository
    }

    override fun onResume(owner: LifecycleOwner) {
        super.onResume(owner)
        currentLifeCycleFlag = LifeCycleFlag.RESUMED
        audioRecorderManager.handleOnResume()
        mediaPlayerManager.handleOnResume()
    }

    override fun onPause(owner: LifecycleOwner) {
        super.onPause(owner)
        currentLifeCycleFlag = LifeCycleFlag.PAUSED
        disposableSet.forEach { disposable -> disposable.dispose() }
        disposableSet.clear()
        audioRecorderManager.handleOnPause()
        mediaPlayerManager.handleOnPause()
    }

    override fun onStop(owner: LifecycleOwner) {
        super.onStop(owner)
        currentLifeCycleFlag = LifeCycleFlag.STOPPED
        audioRecorderManager.handleOnStop()
        mediaPlayerManager.handleOnStop()
    }

    val getAudioFocusChange: LiveData<GccAudioFocusRequestManager.ManagerState>
        get() = audioFocusRequestManager.getManagerState

    private val _getRecordingTime: MutableLiveData<Long> = MutableLiveData(0L)
    val getRecordingTime: LiveData<Long>
        get() = _getRecordingTime

    val micInputAudioObserver: LiveData<Pair<Float, Float>>
        get() = audioRecorderManager.getAudioValues

    val mediaPlayerSeekbarObserver: Flow<Int>
        get() = mediaPlayerManager.mediaPlayerSeekBarPosition

    private val _getEditChatMessage: MutableStateFlow<IMessage?> = MutableStateFlow(null)
    val getEditChatMessage: StateFlow<IMessage?>
        get() = _getEditChatMessage

    private val _getReplyChatMessage: MutableLiveData<GccChatMessage?> = MutableLiveData()
    val getReplyChatMessage: LiveData<GccChatMessage?>
        get() = _getReplyChatMessage

    object CreateThreadStartState : ViewState
    class CreateThreadEditState : ViewState

    private val _createThreadViewState: MutableLiveData<ViewState> = MutableLiveData(CreateThreadStartState)
    val createThreadViewState: LiveData<ViewState>
        get() = _createThreadViewState

    sealed interface ViewState

    object SendChatMessageStartState : ViewState
    class SendChatMessageSuccessState(val message: CharSequence) : ViewState
    class SendChatMessageErrorState(val message: CharSequence) : ViewState

    private val _sendChatMessageViewState: MutableLiveData<ViewState> = MutableLiveData(SendChatMessageStartState)
    val sendChatMessageViewState: LiveData<ViewState>
        get() = _sendChatMessageViewState

    object EditMessageErrorState : ViewState
    class EditMessageSuccessState(val messageEdited: ChatOverallSingleMessage) : ViewState

    private val _editMessageViewState: MutableLiveData<ViewState> = MutableLiveData()
    val editMessageViewState: LiveData<ViewState>
        get() = _editMessageViewState

    private val _isVoicePreviewPlaying: MutableLiveData<Boolean> = MutableLiveData(false)
    val isVoicePreviewPlaying: LiveData<Boolean>
        get() = _isVoicePreviewPlaying

    private val _callStartedFlow: MutableLiveData<Pair<GccChatMessage, Boolean>> = MutableLiveData()
    val callStartedFlow: LiveData<Pair<GccChatMessage, Boolean>>
        get() = _callStartedFlow

    object ScheduleChatMessageStartState : ViewState
    class ScheduleChatMessageSuccessState(val scheduledAt: Long) : ViewState
    object ScheduleChatMessageErrorState : ViewState

    private val _scheduleChatMessageViewState: MutableLiveData<ViewState> =
        MutableLiveData(ScheduleChatMessageStartState)
    val scheduleChatMessageViewState: LiveData<ViewState>
        get() = _scheduleChatMessageViewState

    @Suppress("LongParameterList")
    fun sendChatMessage(
        credentials: String,
        url: String,
        message: String,
        displayName: String,
        replyTo: Int,
        sendWithoutNotification: Boolean,
        threadTitle: String?
    ) {
        val referenceId = GccSendMessageUtils().generateReferenceId()
        Log.d(TAG, "Random SHA-256 Hash: $referenceId")

        viewModelScope.launch {
            chatRepository.addTemporaryMessage(
                message,
                displayName,
                replyTo,
                sendWithoutNotification,
                referenceId
            ).collect { result ->
                if (result.isSuccess) {
                    Log.d(TAG, "temp message ref id: " + (result.getOrNull()?.referenceId ?: "none"))

                    _sendChatMessageViewState.value = SendChatMessageSuccessState(message)
                } else {
                    _sendChatMessageViewState.value = SendChatMessageErrorState(message)
                }
            }
        }

        viewModelScope.launch {
            chatRepository.sendChatMessage(
                credentials,
                url,
                message,
                displayName,
                replyTo,
                sendWithoutNotification,
                referenceId,
                threadTitle
            ).collect { result ->
                if (result.isSuccess) {
                    Log.d(TAG, "received ref id: " + (result.getOrNull()?.referenceId ?: "none"))

                    _sendChatMessageViewState.value = SendChatMessageSuccessState(message)
                } else {
                    _sendChatMessageViewState.value = SendChatMessageErrorState(message)
                }
            }
        }
    }

    fun sendUnsentMessages(credentials: String, url: String) {
        viewModelScope.launch {
            chatRepository.sendUnsentChatMessages(
                credentials,
                url
            )
        }
    }

    fun editChatMessage(credentials: String, url: String, text: String) {
        viewModelScope.launch {
            chatRepository.editChatMessage(
                credentials,
                url,
                text
            ).collect { result ->
                if (result.isSuccess) {
                    _editMessageViewState.value = EditMessageSuccessState(result.getOrNull()!!)
                } else {
                    _editMessageViewState.value = EditMessageErrorState
                }
            }
        }
    }

    fun editTempChatMessage(message: GccChatMessage, editedMessageText: String) {
        viewModelScope.launch {
            chatRepository.editTempChatMessage(
                message,
                editedMessageText
            ).collect {}
        }
    }

    fun reply(message: GccChatMessage?) {
        _getReplyChatMessage.postValue(message)
    }

    fun edit(message: IMessage?) {
        _getEditChatMessage.value = message
    }

    fun startMicInput(context: Context) {
        audioFocusRequestManager.audioFocusRequest(true) {
            audioRecorderManager.start(context)
        }
    }

    fun stopMicInput() {
        audioFocusRequestManager.audioFocusRequest(false) {
            audioRecorderManager.stop()
        }
    }

    fun startMediaPlayer(path: String) {
        audioFocusRequestManager.audioFocusRequest(true) {
            mediaPlayerManager.start(path)
            _isVoicePreviewPlaying.postValue(true)
        }
    }

    fun pauseMediaPlayer() {
        audioFocusRequestManager.audioFocusRequest(false) {
            mediaPlayerManager.pause(false)
            _isVoicePreviewPlaying.postValue(false)
        }
    }

    fun stopMediaPlayer() {
        audioFocusRequestManager.audioFocusRequest(false) {
            mediaPlayerManager.stop()
            _isVoicePreviewPlaying.postValue(false)
        }
    }

    fun seekMediaPlayerTo(progress: Int) {
        mediaPlayerManager.seekTo(progress)
    }

    fun setRecordingTime(time: Long) {
        _getRecordingTime.postValue(time)
    }

    fun showCallStartedIndicator(recent: GccChatMessage, show: Boolean) {
        _callStartedFlow.postValue(Pair(recent, show))
    }

    fun startThreadCreation() {
        _createThreadViewState.postValue(CreateThreadEditState())
    }

    fun stopThreadCreation() {
        _createThreadViewState.postValue(CreateThreadStartState)
    }

    @Suppress("LongParameterList")
    fun scheduleChatMessage(
        credentials: String,
        url: String,
        message: String,
        displayName: String,
        replyTo: Int?,
        sendWithoutNotification: Boolean,
        threadTitle: String?,
        threadId: Long?,
        sendAt: Int?
    ) {
        val referenceId = GccSendMessageUtils().generateReferenceId()
        Log.d(TAG, "Random SHA-256 Hash: $referenceId")

        viewModelScope.launch {
            chatRepository.sendScheduledChatMessage(
                credentials,
                url,
                message,
                displayName,
                referenceId,
                replyTo,
                sendWithoutNotification,
                threadTitle,
                threadId,
                sendAt
            ).collect { result ->
                if (result.isSuccess) {
                    _scheduleChatMessageViewState.value =
                        ScheduleChatMessageSuccessState(sendAt?.toLong() ?: 0L)
                } else {
                    _scheduleChatMessageViewState.value = ScheduleChatMessageErrorState
                }
            }
        }
    }

    companion object {
        private val TAG = GccMessageInputViewModel::class.java.simpleName
    }
}
