/*
 * Nextcloud Talk - Android Client
 *
 * SPDX-FileCopyrightText: 2022 Marcel Hibbe <dev@mhibbe.de>
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package com.gcc.talk.gccPolls.viewmodels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.gcc.talk.gccData.user.model.GccUser
import com.gcc.talk.gccPolls.model.GccPoll
import com.gcc.talk.gccPolls.repositories.GccPollRepository
import com.gcc.talk.gccUsers.GccUserManager
import com.gcc.talk.gccUtils.GccApiUtils
import com.gcc.talk.gccUtils.database.user.GccCurrentUserProviderOld
import io.reactivex.Observer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

class GccPollMainViewModel @Inject constructor(
    private val repository: GccPollRepository,
    private val currentUserProvider: GccCurrentUserProviderOld
) : ViewModel() {

    @Inject
    lateinit var userManager: GccUserManager

    lateinit var user: GccUser
    lateinit var roomToken: String
    private var isOwnerOrModerator: Boolean = false
    lateinit var pollId: String
    lateinit var pollTitle: String

    private var editVotes: Boolean = false

    sealed interface ViewState
    object InitialState : ViewState
    object DismissDialogState : ViewState
    object LoadingState : ViewState

    open class PollVoteState(
        val poll: GccPoll,
        val showVotersAmount: Boolean,
        val showEndPollButton: Boolean,
        val showDismissEditButton: Boolean
    ) : ViewState

    open class PollResultState(
        val poll: GccPoll,
        val showVotersAmount: Boolean,
        val showEndPollButton: Boolean,
        val showEditButton: Boolean
    ) : ViewState

    private val _viewState: MutableLiveData<ViewState> = MutableLiveData(InitialState)
    val viewState: LiveData<ViewState>
        get() = _viewState

    private var disposable: Disposable? = null

    private val currentUser = currentUserProvider.currentUser.blockingGet()
    private val credentials = GccApiUtils.getCredentials(currentUser.username, currentUser.token)

    fun setData(user: GccUser, roomToken: String, isOwnerOrModerator: Boolean, pollId: String, pollTitle: String) {
        this.user = user
        this.roomToken = roomToken
        this.isOwnerOrModerator = isOwnerOrModerator
        this.pollId = pollId
        this.pollTitle = pollTitle

        loadPoll()
    }

    fun voted() {
        loadPoll()
    }

    fun editVotes() {
        editVotes = true
        loadPoll()
    }

    fun dismissEditVotes() {
        loadPoll()
    }

    private fun loadPoll() {
        _viewState.value = LoadingState

        val url = GccApiUtils.getUrlForPoll(
            currentUser.baseUrl!!,
            roomToken,
            pollId
        )
        repository.getPoll(
            credentials,
            url,
            roomToken,
            pollId
        )
            .doOnSubscribe { disposable = it }
            ?.subscribeOn(Schedulers.io())
            ?.observeOn(AndroidSchedulers.mainThread())
            ?.subscribe(PollObserver())
    }

    fun endPoll() {
        _viewState.value = LoadingState

        val url = GccApiUtils.getUrlForPoll(
            currentUser.baseUrl!!,
            roomToken,
            pollId
        )

        repository.closePoll(
            credentials,
            url,
            roomToken,
            pollId
        )
            .doOnSubscribe { disposable = it }
            ?.subscribeOn(Schedulers.io())
            ?.observeOn(AndroidSchedulers.mainThread())
            ?.subscribe(PollObserver())
    }

    override fun onCleared() {
        super.onCleared()
        disposable?.dispose()
    }

    inner class PollObserver : Observer<GccPoll> {

        lateinit var poll: GccPoll

        override fun onSubscribe(d: Disposable) = Unit

        override fun onNext(response: GccPoll) {
            poll = response
        }

        override fun onError(e: Throwable) {
            Log.e(TAG, "An error occurred: $e")
        }

        override fun onComplete() {
            val showEndPollButton = showEndPollButton(poll)
            val showVotersAmount = showVotersAmount(poll)

            if (votedForOpenHiddenPoll(poll)) {
                _viewState.value = PollVoteState(poll, showVotersAmount, showEndPollButton, false)
            } else if (editVotes && poll.status == GccPoll.STATUS_OPEN) {
                _viewState.value = PollVoteState(poll, false, showEndPollButton, true)
                editVotes = false
            } else if (poll.status == GccPoll.STATUS_CLOSED || poll.votedSelf?.isNotEmpty() == true) {
                val showEditButton = poll.status == GccPoll.STATUS_OPEN && poll.resultMode == GccPoll.RESULT_MODE_PUBLIC
                _viewState.value = PollResultState(poll, showVotersAmount, showEndPollButton, showEditButton)
            } else if (poll.votedSelf.isNullOrEmpty()) {
                _viewState.value = PollVoteState(poll, showVotersAmount, showEndPollButton, false)
            } else {
                Log.w(TAG, "unknown poll state")
            }
        }
    }

    private fun showEndPollButton(poll: GccPoll): Boolean =
        !editVotes && poll.status == GccPoll.STATUS_OPEN && (isPollCreatedByCurrentUser(poll) || isOwnerOrModerator)

    private fun showVotersAmount(poll: GccPoll): Boolean =
        votedForPublicPoll(poll) ||
            poll.status == GccPoll.STATUS_CLOSED ||
            isOwnerOrModerator ||
            isPollCreatedByCurrentUser(poll)

    private fun votedForOpenHiddenPoll(poll: GccPoll): Boolean =
        poll.status == GccPoll.STATUS_OPEN &&
            poll.resultMode == GccPoll.RESULT_MODE_HIDDEN &&
            poll.votedSelf?.isNotEmpty() == true

    private fun votedForPublicPoll(poll: GccPoll): Boolean =
        poll.resultMode == GccPoll.RESULT_MODE_PUBLIC &&
            poll.votedSelf?.isNotEmpty() == true

    private fun isPollCreatedByCurrentUser(poll: GccPoll): Boolean = currentUser.userId == poll.actorId

    fun dismissDialog() {
        _viewState.value = DismissDialogState
    }

    companion object {
        private val TAG = GccPollMainViewModel::class.java.simpleName
    }
}
