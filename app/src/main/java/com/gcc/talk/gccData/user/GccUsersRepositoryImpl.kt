/*
 * Nextcloud Talk - Android Client
 *
 * SPDX-FileCopyrightText: 2022 Andy Scherzinger <info@andy-scherzinger.de>
 * SPDX-FileCopyrightText: 2017-2020 Mario Danic <mario@lovelyhq.com>
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package com.gcc.talk.gccData.user

import android.util.Log
import com.gcc.talk.gccData.user.model.GccUser
import com.gcc.talk.gccModels.json.push.PushConfigurationState
import io.reactivex.Maybe
import io.reactivex.Observable
import io.reactivex.Single

@Suppress("TooManyFunctions")
class GccUsersRepositoryImpl(private val usersDao: GccUsersDao) : GccUsersRepository {

    override fun getActiveUser(): Maybe<GccUser> {
        val user = usersDao.getActiveUser()
            .map {
                setUserAsActiveWithId(it.id)
                GccUserMapper.toModel(it)!!
            }
        return user
    }

    override fun getActiveUserObservable(): Observable<GccUser> =
        usersDao.getActiveUserObservable().map {
            GccUserMapper.toModel(it)
        }

    override fun getUsers(): Single<List<GccUser>> = usersDao.getUsers().map { GccUserMapper.toModel(it) }

    override fun getUserWithId(id: Long): Maybe<GccUser> = usersDao.getUserWithId(id).map { GccUserMapper.toModel(it) }

    override fun getUserWithIdNotScheduledForDeletion(id: Long): Maybe<GccUser> =
        usersDao.getUserWithIdNotScheduledForDeletion(id).map {
            GccUserMapper.toModel(it)
        }

    override fun getUserWithUserId(userId: String): Maybe<GccUser> =
        usersDao.getUserWithUserId(userId).map {
            GccUserMapper.toModel(it)
        }

    override fun getUsersScheduledForDeletion(): Single<List<GccUser>> =
        usersDao.getUsersScheduledForDeletion().map {
            GccUserMapper.toModel(it)
        }

    override fun getUsersNotScheduledForDeletion(): Single<List<GccUser>> =
        usersDao.getUsersNotScheduledForDeletion().map {
            GccUserMapper.toModel(it)
        }

    override fun getUserWithUsernameAndServer(username: String, server: String): Maybe<GccUser> =
        usersDao.getUserWithUsernameAndServer(username, server).map {
            GccUserMapper.toModel(it)
        }

    override fun updateUser(user: GccUser): Int = usersDao.updateUser(GccUserMapper.toEntity(user))

    override fun insertUser(user: GccUser): Long = usersDao.saveUser(GccUserMapper.toEntity(user))

    override fun setUserAsActiveWithId(id: Long): Single<Boolean> {
        val amountUpdated = usersDao.setUserAsActiveWithId(id)
        Log.d(TAG, "setUserAsActiveWithId. amountUpdated: $amountUpdated")
        return if (amountUpdated > 0) {
            Single.just(true)
        } else {
            Single.just(false)
        }
    }

    override fun deleteUser(user: GccUser): Int = usersDao.deleteUser(GccUserMapper.toEntity(user))

    override fun updatePushState(id: Long, state: PushConfigurationState): Single<Int> =
        usersDao.updatePushState(id, state)

    companion object {
        private val TAG = GccUsersRepositoryImpl::class.simpleName
    }
}
