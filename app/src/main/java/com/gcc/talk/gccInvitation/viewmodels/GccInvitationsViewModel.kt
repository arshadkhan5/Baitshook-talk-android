/*
 * Nextcloud Talk - Android Client
 *
 * SPDX-FileCopyrightText: 2023 Marcel Hibbe <dev@mhibbe.de>
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package com.gcc.talk.gccInvitation.viewmodels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gcc.talk.gccData.user.model.GccUser
import com.gcc.talk.gccInvitation.data.ActionEnum
import com.gcc.talk.gccInvitation.data.GccInvitation
import com.gcc.talk.gccInvitation.data.InvitationActionModel
import com.gcc.talk.gccInvitation.data.GccInvitationsModel
import com.gcc.talk.gccInvitation.data.GccInvitationsRepository
import io.reactivex.Observer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

class GccInvitationsViewModel @Inject constructor(private val repository: GccInvitationsRepository) : ViewModel() {

    sealed interface ViewState

    object FetchInvitationsStartState : ViewState
    object FetchInvitationsEmptyState : ViewState
    object FetchInvitationsErrorState : ViewState
    open class FetchInvitationsSuccessState(val invitations: List<GccInvitation>) : ViewState

    private val _fetchInvitationsViewState: MutableLiveData<ViewState> = MutableLiveData(FetchInvitationsStartState)
    val fetchInvitationsViewState: LiveData<ViewState>
        get() = _fetchInvitationsViewState

    object GetInvitationsStartState : ViewState
    object GetInvitationsEmptyState : ViewState
    open class GetInvitationsErrorState(val error: Exception) : ViewState
    open class GetInvitationsSuccessState(val invitations: List<GccInvitation>) : ViewState

    private val _getInvitationsViewState = MutableStateFlow<ViewState>(GetInvitationsStartState)
    val getInvitationsViewState: StateFlow<ViewState> = _getInvitationsViewState

    object InvitationActionStartState : ViewState
    object InvitationActionErrorState : ViewState

    private val _invitationActionViewState: MutableLiveData<ViewState> = MutableLiveData(InvitationActionStartState)

    open class InvitationActionSuccessState(val action: ActionEnum, val invitation: GccInvitation) : ViewState

    val invitationActionViewState: LiveData<ViewState>
        get() = _invitationActionViewState

    fun fetchInvitations(user: GccUser) {
        _fetchInvitationsViewState.value = FetchInvitationsStartState
        repository.fetchInvitations(user)
            .subscribeOn(Schedulers.io())
            ?.observeOn(AndroidSchedulers.mainThread())
            ?.subscribe(FetchInvitationsObserver())
    }

    @Suppress("TooGenericExceptionCaught")
    fun getInvitations(user: GccUser) {
        viewModelScope.launch {
            try {
                val invitationsModel = repository.getInvitations(user)
                if (invitationsModel.invitations.isEmpty()) {
                    _getInvitationsViewState.value = GetInvitationsEmptyState
                } else {
                    _getInvitationsViewState.value = GetInvitationsSuccessState(invitationsModel.invitations)
                }
            } catch (e: Exception) {
                _getInvitationsViewState.value = GetInvitationsErrorState(e)
            }
        }
    }

    fun acceptInvitation(user: GccUser, invitation: GccInvitation) {
        repository.acceptInvitation(user, invitation)
            .subscribeOn(Schedulers.io())
            ?.observeOn(AndroidSchedulers.mainThread())
            ?.subscribe(InvitationActionObserver())
    }

    fun rejectInvitation(user: GccUser, invitation: GccInvitation) {
        repository.rejectInvitation(user, invitation)
            .subscribeOn(Schedulers.io())
            ?.observeOn(AndroidSchedulers.mainThread())
            ?.subscribe(InvitationActionObserver())
    }

    inner class FetchInvitationsObserver : Observer<GccInvitationsModel> {
        override fun onSubscribe(d: Disposable) {
            // unused atm
        }

        override fun onNext(model: GccInvitationsModel) {
            if (model.invitations.isEmpty()) {
                _fetchInvitationsViewState.value = FetchInvitationsEmptyState
            } else {
                _fetchInvitationsViewState.value = FetchInvitationsSuccessState(model.invitations)
            }
        }

        override fun onError(e: Throwable) {
            Log.e(TAG, "Error when fetching invitations")
            _fetchInvitationsViewState.value = FetchInvitationsErrorState
        }

        override fun onComplete() {
            // unused atm
        }
    }

    inner class InvitationActionObserver : Observer<InvitationActionModel> {
        override fun onSubscribe(d: Disposable) {
            // unused atm
        }

        override fun onNext(model: InvitationActionModel) {
            if (model.statusCode == HTTP_OK) {
                _invitationActionViewState.value = InvitationActionSuccessState(model.action, model.invitation)
            } else {
                _invitationActionViewState.value = InvitationActionErrorState
            }
        }

        override fun onError(e: Throwable) {
            Log.e(TAG, "Error when handling invitation")
            _invitationActionViewState.value = InvitationActionErrorState
        }

        override fun onComplete() {
            // unused atm
        }
    }

    companion object {
        private val TAG = GccInvitationsViewModel::class.simpleName
        private const val OPEN_PENDING_INVITATION = "0"
        private const val HTTP_OK = 200
    }
}
