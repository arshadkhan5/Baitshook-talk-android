/*
 * Nextcloud Talk - Android Client
 *
 * SPDX-FileCopyrightText: 2022 Álvaro Brey <alvaro@alvarobrey.com>
 * SPDX-FileCopyrightText: 2022 Nextcloud GmbH
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package com.gcc.talk.gccUtils.database.user

import com.gcc.talk.gccData.user.model.GccUser
import com.gcc.talk.gccUsers.GccUserManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.rx2.asFlow
import kotlinx.coroutines.withTimeoutOrNull
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GccCurrentUserProviderImpl @Inject constructor(private val userManager: GccUserManager) : GccCurrentUserProvider {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val currentUser: StateFlow<GccUser?> = userManager.currentUserObservable
        .asFlow()
        .stateIn(
            scope = scope,
            started = SharingStarted.Eagerly,
            initialValue = null
        )

    // only emit non-null users
    val currentUserFlow: Flow<GccUser> = currentUser.filterNotNull()

    // function for safe one-shot access
    override suspend fun getCurrentUser(timeout: Long): Result<GccUser> {
        val user = withTimeoutOrNull(timeout) {
            currentUserFlow.first()
        }

        return if (user != null) {
            Result.success(user)
        } else {
            Result.failure(IllegalStateException("No current user available"))
        }
    }
}
