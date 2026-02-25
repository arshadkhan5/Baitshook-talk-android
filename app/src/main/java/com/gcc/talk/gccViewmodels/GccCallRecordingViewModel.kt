/*
 * Nextcloud Talk - Android Client
 *
 * SPDX-FileCopyrightText: 2022 Marcel Hibbe <dev@mhibbe.de>
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package com.gcc.talk.gccViewmodels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.gcc.talk.gccData.user.model.GccUser
import com.gcc.talk.gccModels.domain.GccStartCallRecordingModel
import com.gcc.talk.gccModels.domain.GccStopCallRecordingModel
import com.gcc.talk.gccRepositories.callrecording.GccCallRecordingRepository
import com.gcc.talk.gccUtils.GccApiUtils
import com.gcc.talk.gccUtils.database.user.GccCurrentUserProviderOld
import io.reactivex.Observer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

class GccCallRecordingViewModel @Inject constructor(
    private val repository: GccCallRecordingRepository,
    private val currentUserProvider: GccCurrentUserProviderOld
) : ViewModel() {

    lateinit var roomToken: String

    sealed interface ViewState
    open class RecordingStartedState(val hasVideo: Boolean, val showStartedInfo: Boolean) : ViewState

    object RecordingStoppedState : ViewState
    open class RecordingStartingState(val hasVideo: Boolean) : ViewState
    object RecordingStoppingState : ViewState
    object RecordingConfirmStopState : ViewState
    object RecordingErrorState : ViewState

    private val _viewState: MutableLiveData<ViewState> = MutableLiveData(RecordingStoppedState)
    val viewState: LiveData<ViewState>
        get() = _viewState

    private var disposable: Disposable? = null

    private var currentUser: GccUser = currentUserProvider.currentUser.blockingGet()
    val credentials: String = GccApiUtils.getCredentials(currentUser.username, currentUser.token)!!

    fun clickRecordButton() {
        when (viewState.value) {
            is RecordingStartedState -> {
                _viewState.value = RecordingConfirmStopState
            }
            RecordingStoppedState -> {
                startRecording()
            }
            RecordingConfirmStopState -> {
                // confirm dialog to stop recording might have been dismissed without to click an action.
                // just show it again.
                _viewState.value = RecordingConfirmStopState
            }
            is RecordingStartingState -> {
                stopRecording()
            }
            RecordingErrorState -> {
                stopRecording()
            }
            else -> {}
        }
    }

    private fun startRecording() {
        _viewState.value = RecordingStartingState(true)
        val apiVersion = 1
        val url = GccApiUtils.getUrlForRecording(
            apiVersion,
            currentUser.baseUrl!!,
            roomToken
        )

        repository.startRecording(
            credentials,
            url,
            roomToken
        )
            .subscribeOn(Schedulers.io())
            ?.observeOn(AndroidSchedulers.mainThread())
            ?.subscribe(CallStartRecordingObserver())
    }

    fun stopRecording() {
        _viewState.value = RecordingStoppingState
        val apiVersion = 1
        val url = GccApiUtils.getUrlForRecording(
            apiVersion,
            currentUser.baseUrl!!,
            roomToken
        )

        repository.stopRecording(
            credentials,
            url,
            roomToken
        )
            .subscribeOn(Schedulers.io())
            ?.observeOn(AndroidSchedulers.mainThread())
            ?.subscribe(CallStopRecordingObserver())
    }

    fun dismissStopRecording() {
        _viewState.value = RecordingStartedState(true, false)
    }

    override fun onCleared() {
        super.onCleared()
        disposable?.dispose()
    }

    fun setData(roomToken: String) {
        this.roomToken = roomToken
    }

    // https://nextcloud-talk.readthedocs.io/en/latest/constants/#call-recording-status
    fun setRecordingState(state: Int) {
        when (state) {
            RECORDING_STOPPED_CODE -> _viewState.value = RecordingStoppedState
            RECORDING_STARTED_VIDEO_CODE -> _viewState.value = RecordingStartedState(true, true)
            RECORDING_STARTED_AUDIO_CODE -> _viewState.value = RecordingStartedState(false, true)
            RECORDING_STARTING_VIDEO_CODE -> _viewState.value = RecordingStartingState(true)
            RECORDING_STARTING_AUDIO_CODE -> _viewState.value = RecordingStartingState(false)
            RECORDING_FAILED_CODE -> _viewState.value = RecordingErrorState
            else -> {}
        }
    }

    inner class CallStartRecordingObserver : Observer<GccStartCallRecordingModel> {
        override fun onSubscribe(d: Disposable) {
            // unused atm
        }

        override fun onNext(startCallRecordingModel: GccStartCallRecordingModel) {
            // unused atm. RecordingStartedState is set via setRecordingState which is triggered by signaling message.
        }

        override fun onError(e: Throwable) {
            Log.e(TAG, "failure in CallStartRecordingObserver", e)
            _viewState.value = RecordingErrorState
        }

        override fun onComplete() {
            // dismiss()
        }
    }

    inner class CallStopRecordingObserver : Observer<GccStopCallRecordingModel> {
        override fun onSubscribe(d: Disposable) {
            // unused atm
        }

        override fun onNext(stopCallRecordingModel: GccStopCallRecordingModel) {
            if (stopCallRecordingModel.success) {
                _viewState.value = RecordingStoppedState
            }
        }

        override fun onError(e: Throwable) {
            Log.e(TAG, "failure in CallStopRecordingObserver", e)
            _viewState.value = RecordingErrorState
        }

        override fun onComplete() {
            // dismiss()
        }
    }

    companion object {
        private val TAG = GccCallRecordingViewModel::class.java.simpleName
        const val RECORDING_STOPPED_CODE = 0
        const val RECORDING_STARTED_VIDEO_CODE = 1
        const val RECORDING_STARTED_AUDIO_CODE = 2
        const val RECORDING_STARTING_VIDEO_CODE = 3
        const val RECORDING_STARTING_AUDIO_CODE = 4
        const val RECORDING_FAILED_CODE = 5
    }
}
