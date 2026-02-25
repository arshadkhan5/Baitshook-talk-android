/*
 * Nextcloud Talk - Android Client
 *
 * SPDX-FileCopyrightText: 2025 Marcel Hibbe <dev@mhibbe.de>
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.gcc.talk.gccThreadsoverview.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gcc.talk.gccArbitrarystorage.GccArbitraryStorageManager
import com.gcc.talk.gccConversationlist.viewmodels.GccConversationsListViewModel.Companion.FOLLOWED_THREADS_EXIST
import com.gcc.talk.gccConversationlist.viewmodels.GccConversationsListViewModel.Companion.FOLLOWED_THREADS_EXIST_LAST_CHECK
import com.gcc.talk.gccData.user.model.GccUser
import com.gcc.talk.gccModels.json.threads.ThreadInfo
import com.gcc.talk.gccModels.json.threads.ThreadsOverall
import com.gcc.talk.gccThreadsoverview.data.GccThreadsRepository
import com.gcc.talk.gccUtils.GccApiUtils
import com.gcc.talk.gccUtils.GccUserIdUtils
import com.gcc.talk.gccUtils.database.user.GccCurrentUserProviderOld
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@Suppress("TooGenericExceptionCaught")
class GccThreadsOverviewViewModel @Inject constructor(
    private val threadsRepository: GccThreadsRepository,
    private val currentUserProvider: GccCurrentUserProviderOld,
    private val arbitraryStorageManager: GccArbitraryStorageManager
) : ViewModel() {
    private val _currentUser = currentUserProvider.currentUser.blockingGet()
    val currentUser: GccUser = _currentUser
    val credentials = GccApiUtils.getCredentials(_currentUser.username, _currentUser.token) ?: ""

    private val _threadsListState = MutableStateFlow<ThreadsListUiState>(ThreadsListUiState.None)
    val threadsListState: StateFlow<ThreadsListUiState> = _threadsListState

    fun init(url: String) {
        getThreads(credentials, url)
    }

    fun getThreads(credentials: String, url: String) {
        viewModelScope.launch {
            try {
                val threads = threadsRepository.getThreads(credentials, url, null)
                _threadsListState.value = ThreadsListUiState.Success(threads.ocs?.data)
                updateFollowedThreadsIndicator(url, threads)
            } catch (exception: Exception) {
                _threadsListState.value = ThreadsListUiState.Error(exception)
            }
        }
    }

    private fun updateFollowedThreadsIndicator(url: String, threads: ThreadsOverall) {
        val subscribedThreadsEndpoint = "subscribed-threads"
        if (url.contains(subscribedThreadsEndpoint) && threads.ocs?.data?.isEmpty() == true) {
            val accountId = GccUserIdUtils.getIdForUser(currentUserProvider.currentUser.blockingGet())
            arbitraryStorageManager.storeStorageSetting(
                accountId,
                FOLLOWED_THREADS_EXIST,
                false.toString(),
                ""
            )
            arbitraryStorageManager.storeStorageSetting(
                accountId,
                FOLLOWED_THREADS_EXIST_LAST_CHECK,
                null,
                ""
            )
        }
    }

    sealed class ThreadsListUiState {
        data object None : ThreadsListUiState()
        data class Success(val threadsList: List<ThreadInfo>?) : ThreadsListUiState()
        data class Error(val exception: Exception) : ThreadsListUiState()
    }
}
