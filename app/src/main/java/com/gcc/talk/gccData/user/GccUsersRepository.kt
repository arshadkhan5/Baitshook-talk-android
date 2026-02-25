/*
 * Nextcloud Talk - Android Client
 *
 * SPDX-FileCopyrightText: 2022 Andy Scherzinger <info@andy-scherzinger.de>
 * SPDX-FileCopyrightText: 2017-2020 Mario Danic <mario@lovelyhq.com>
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package com.gcc.talk.gccData.user

import com.gcc.talk.gccData.user.model.GccUser
import com.gcc.talk.gccModels.json.push.PushConfigurationState
import io.reactivex.Maybe
import io.reactivex.Observable
import io.reactivex.Single

@Suppress("TooManyFunctions")
interface GccUsersRepository {
    fun getActiveUser(): Maybe<GccUser>
    fun getActiveUserObservable(): Observable<GccUser>
    fun getUsers(): Single<List<GccUser>>
    fun getUserWithId(id: Long): Maybe<GccUser>
    fun getUserWithIdNotScheduledForDeletion(id: Long): Maybe<GccUser>
    fun getUserWithUserId(userId: String): Maybe<GccUser>
    fun getUsersScheduledForDeletion(): Single<List<GccUser>>
    fun getUsersNotScheduledForDeletion(): Single<List<GccUser>>
    fun getUserWithUsernameAndServer(username: String, server: String): Maybe<GccUser>
    fun updateUser(user: GccUser): Int
    fun insertUser(user: GccUser): Long
    fun setUserAsActiveWithId(id: Long): Single<Boolean>
    fun deleteUser(user: GccUser): Int
    fun updatePushState(id: Long, state: PushConfigurationState): Single<Int>
}
