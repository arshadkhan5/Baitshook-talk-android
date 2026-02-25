/*
 * Nextcloud Talk - Android Client
 *
 * SPDX-FileCopyrightText: 2025 Sowjanya Kota <sowjanya.kch@gmail.com>
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.gcc.talk.gccDiagnose

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gcc.talk.gccApi.GccNcApiCoroutines
import com.gcc.talk.gccUtils.GccApiUtils
import com.gcc.talk.gccData.user.model.GccUser
import com.gcc.talk.gccUtils.database.user.GccCurrentUserProviderOld
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@Suppress("TooGenericExceptionCaught")
class DiagnoseViewModel @Inject constructor(
    private val ncApiCoroutines: GccNcApiCoroutines,
    private val currentUserProvider: GccCurrentUserProviderOld
) : ViewModel() {
    private val _currentUser = currentUserProvider.currentUser.blockingGet()
    val currentUser: GccUser = _currentUser
    val credentials = GccApiUtils.getCredentials(_currentUser.username, _currentUser.token) ?: ""

    private val _notificationViewState = MutableStateFlow<NotificationUiState>(NotificationUiState.None)
    val notificationViewState: StateFlow<NotificationUiState> = _notificationViewState

    private val _isLoading = mutableStateOf(false)
    val isLoading = _isLoading

    private val _showDialog = mutableStateOf(false)
    val showDialog = _showDialog

    fun fetchTestPushResult() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val response = ncApiCoroutines.testPushNotifications(
                    credentials,
                    GccApiUtils
                        .getUrlForTestPushNotifications(_currentUser.baseUrl ?: "")
                )
                val notificationMessage = response.ocs?.data?.message
                _notificationViewState.value = NotificationUiState.Success(notificationMessage)
            } catch (e: Exception) {
                _notificationViewState.value = NotificationUiState.Error(e.message ?: "")
            } finally {
                _isLoading.value = false
                _showDialog.value = true
            }
        }
    }

    fun dismissDialog() {
        _showDialog.value = false
    }
}

sealed class NotificationUiState {
    data object None : NotificationUiState()
    data class Success(val testNotification: String?) : NotificationUiState()
    data class Error(val message: String) : NotificationUiState()
}
