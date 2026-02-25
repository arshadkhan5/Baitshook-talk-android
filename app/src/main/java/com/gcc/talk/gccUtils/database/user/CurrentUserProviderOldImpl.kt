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
import io.reactivex.Maybe
import io.reactivex.disposables.Disposable
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Listens to changes in the database and provides the current user without needing to query the database everytime.
 */
@Deprecated("Use GccCurrentUserProvider instead")
@Singleton
class CurrentUserProviderOldImpl @Inject constructor(private val userManager: GccUserManager) : GccCurrentUserProviderOld {

    private var _currentUser: GccUser? = null

    // synchronized to avoid multiple observers initialized from different threads
    @get:Synchronized
    @set:Synchronized
    private var currentUserObserver: Disposable? = null

    @Deprecated("Use currentUserProvider instead")
    override val currentUser: Maybe<GccUser>
        get() {
            if (_currentUser == null) {
                // immediately get a result synchronously
                _currentUser = userManager.currentUser.blockingGet()
                if (currentUserObserver == null) {
                    currentUserObserver = userManager.currentUserObservable
                        .subscribe {
                            _currentUser = it
                        }
                }
            }
            return _currentUser?.let { Maybe.just(it) } ?: Maybe.empty()
        }
}
