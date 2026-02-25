/*
 * Nextcloud Talk - Android Client
 *
 * SPDX-FileCopyrightText: 2023 Marcel Hibbe <dev@mhibbe.de>
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package com.gcc.talk.gccConversationinfoedit.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gcc.talk.gccChat.data.network.GccChatNetworkDataSource
import com.gcc.talk.gccConversationinfoedit.data.GccConversationInfoEditRepository
import com.gcc.talk.gccData.user.model.GccUser
import com.gcc.talk.gccModels.domain.GccConversationModel
import com.gcc.talk.gccUtils.GccApiUtils
import com.gcc.talk.gccUtils.database.user.GccCurrentUserProviderOld
import io.reactivex.Observer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

class GccConversationInfoEditViewModel @Inject constructor(
    private val repository: GccChatNetworkDataSource,
    private val conversationInfoEditRepository: GccConversationInfoEditRepository,
    private val currentUserProvider: GccCurrentUserProviderOld
) : ViewModel() {

    sealed interface ViewState

    object GetRoomStartState : ViewState
    object GetRoomErrorState : ViewState
    open class GetRoomSuccessState(val conversationModel: GccConversationModel) : ViewState

    object UploadAvatarErrorState : ViewState
    open class UploadAvatarSuccessState(val conversationModel: GccConversationModel) : ViewState

    object DeleteAvatarErrorState : ViewState
    open class DeleteAvatarSuccessState(val conversationModel: GccConversationModel) : ViewState

    private val _viewState: MutableLiveData<ViewState> = MutableLiveData(GetRoomStartState)
    val viewState: LiveData<ViewState>
        get() = _viewState

    private val _renameRoomUiState = MutableLiveData<RenameRoomUiState>(RenameRoomUiState.None)
    val renameRoomUiState: LiveData<RenameRoomUiState>
        get() = _renameRoomUiState

    private val _setConversationDescriptionUiState =
        MutableLiveData<SetConversationDescriptionUiState>(SetConversationDescriptionUiState.None)
    val setConversationDescriptionUiState: LiveData<SetConversationDescriptionUiState>
        get() = _setConversationDescriptionUiState

    private val currentUser = currentUserProvider.currentUser.blockingGet()

    val credentials = GccApiUtils.getCredentials(currentUser.username, currentUser.token)

    fun getRoom(user: GccUser, token: String) {
        _viewState.value = GetRoomStartState
        repository.getRoom(user, token)
            .subscribeOn(Schedulers.io())
            ?.observeOn(AndroidSchedulers.mainThread())
            ?.subscribe(GetRoomObserver())
    }

    fun uploadConversationAvatar(user: GccUser, file: File, roomToken: String) {
        val url = GccApiUtils.getUrlForConversationAvatar(1, user.baseUrl!!, roomToken)

        conversationInfoEditRepository.uploadConversationAvatar(
            credentials,
            url,
            user,
            file,
            roomToken
        )
            .subscribeOn(Schedulers.io())
            ?.observeOn(AndroidSchedulers.mainThread())
            ?.subscribe(UploadConversationAvatarObserver())
    }

    fun deleteConversationAvatar(user: GccUser, roomToken: String) {
        val url = GccApiUtils.getUrlForConversationAvatar(1, user.baseUrl!!, roomToken)

        conversationInfoEditRepository.deleteConversationAvatar(
            credentials,
            url,
            user,
            roomToken
        )
            .subscribeOn(Schedulers.io())
            ?.observeOn(AndroidSchedulers.mainThread())
            ?.subscribe(DeleteConversationAvatarObserver())
    }

    @Suppress("Detekt.TooGenericExceptionCaught")
    fun renameRoom(roomToken: String, newRoomName: String) {
        viewModelScope.launch {
            try {
                val apiVersion = GccApiUtils.getConversationApiVersion(
                    currentUser,
                    intArrayOf(GccApiUtils.API_V4, GccApiUtils.API_V1)
                )
                val url = GccApiUtils.getUrlForRoom(
                    apiVersion,
                    currentUser.baseUrl!!,
                    roomToken
                )

                conversationInfoEditRepository.renameConversation(
                    credentials,
                    url,
                    roomToken,
                    newRoomName
                )
                _renameRoomUiState.value = RenameRoomUiState.Success
            } catch (exception: Exception) {
                _renameRoomUiState.value = RenameRoomUiState.Error(exception)
            }
        }
    }

    @Suppress("Detekt.TooGenericExceptionCaught")
    fun setConversationDescription(roomToken: String, conversationDescription: String?) {
        viewModelScope.launch {
            try {
                val apiVersion = GccApiUtils.getConversationApiVersion(
                    currentUser,
                    intArrayOf(GccApiUtils.API_V4, GccApiUtils.API_V1)
                )
                val url = GccApiUtils.getUrlForConversationDescription(
                    apiVersion,
                    currentUser.baseUrl!!,
                    roomToken
                )

                conversationInfoEditRepository.setConversationDescription(
                    credentials,
                    url,
                    roomToken,
                    conversationDescription
                )

                _setConversationDescriptionUiState.value = SetConversationDescriptionUiState.Success
            } catch (exception: Exception) {
                _setConversationDescriptionUiState.value = SetConversationDescriptionUiState.Error(exception)
            }
        }
    }

    inner class GetRoomObserver : Observer<GccConversationModel> {
        override fun onSubscribe(d: Disposable) {
            // unused atm
        }

        override fun onNext(conversationModel: GccConversationModel) {
            _viewState.value = GetRoomSuccessState(conversationModel)
        }

        override fun onError(e: Throwable) {
            Log.e(TAG, "Error when fetching room")
            _viewState.value = GetRoomErrorState
        }

        override fun onComplete() {
            // unused atm
        }
    }

    inner class UploadConversationAvatarObserver : Observer<GccConversationModel> {
        override fun onSubscribe(d: Disposable) {
            // unused atm
        }

        override fun onNext(conversationModel: GccConversationModel) {
            _viewState.value = UploadAvatarSuccessState(conversationModel)
        }

        override fun onError(e: Throwable) {
            Log.e(TAG, "Error when uploading avatar")
            _viewState.value = UploadAvatarErrorState
        }

        override fun onComplete() {
            // unused atm
        }
    }

    inner class DeleteConversationAvatarObserver : Observer<GccConversationModel> {
        override fun onSubscribe(d: Disposable) {
            // unused atm
        }

        override fun onNext(conversationModel: GccConversationModel) {
            _viewState.value = DeleteAvatarSuccessState(conversationModel)
        }

        override fun onError(e: Throwable) {
            Log.e(TAG, "Error when deleting avatar")
            _viewState.value = DeleteAvatarErrorState
        }

        override fun onComplete() {
            // unused atm
        }
    }

    companion object {
        private val TAG = GccConversationInfoEditViewModel::class.simpleName
    }

    sealed class RenameRoomUiState {
        data object None : RenameRoomUiState()
        data object Success : RenameRoomUiState()
        data class Error(val exception: Exception) : RenameRoomUiState()
    }

    sealed class SetConversationDescriptionUiState {
        data object None : SetConversationDescriptionUiState()
        data object Success : SetConversationDescriptionUiState()
        data class Error(val exception: Exception) : SetConversationDescriptionUiState()
    }
}
