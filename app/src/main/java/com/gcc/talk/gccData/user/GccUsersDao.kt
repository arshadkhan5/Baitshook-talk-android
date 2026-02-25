/*
 * Nextcloud Talk - Android Client
 *
 * SPDX-FileCopyrightText: 2022 Andy Scherzinger <info@andy-scherzinger.de>
 * SPDX-FileCopyrightText: 2017-2020 Mario Danic <mario@lovelyhq.com>
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package com.gcc.talk.gccData.user

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.gcc.talk.gccData.user.model.GccUserEntity
import com.gcc.talk.gccModels.json.push.PushConfigurationState
import io.reactivex.Maybe
import io.reactivex.Observable
import io.reactivex.Single

@Dao
@Suppress("TooManyFunctions")
abstract class GccUsersDao {
    // get active user
    @Query("SELECT * FROM GccUser where  `current` = 1")
    abstract fun getActiveUser(): Maybe<GccUserEntity>

    // get active user
    @Query("SELECT * FROM GccUser where `current` = 1")
    abstract fun getActiveUserObservable(): Observable<GccUserEntity>

    @Query("SELECT * FROM GccUser where `current` = 1")
    abstract fun getActiveUserSynchronously(): GccUserEntity?

    @Delete
    abstract fun deleteUser(user: GccUserEntity): Int

    @Update
    abstract fun updateUser(user: GccUserEntity): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun saveUser(user: GccUserEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun saveUsers(vararg users: GccUserEntity): List<Long>

    // get all users not scheduled for deletion
    @Query("SELECT * FROM GccUser where scheduledForDeletion != 1")
    abstract fun getUsers(): Single<List<GccUserEntity>>

    @Query("SELECT * FROM GccUser where id = :id")
    abstract fun getUserWithId(id: Long): Maybe<GccUserEntity>

    @Query("SELECT * FROM GccUser where id = :id AND scheduledForDeletion != 1")
    abstract fun getUserWithIdNotScheduledForDeletion(id: Long): Maybe<GccUserEntity>

    @Query("SELECT * FROM GccUser where userId = :userId")
    abstract fun getUserWithUserId(userId: String): Maybe<GccUserEntity>

    @Query("SELECT * FROM GccUser where scheduledForDeletion = 1")
    abstract fun getUsersScheduledForDeletion(): Single<List<GccUserEntity>>

    @Query("SELECT * FROM GccUser where scheduledForDeletion = 0")
    abstract fun getUsersNotScheduledForDeletion(): Single<List<GccUserEntity>>

    @Query("SELECT * FROM GccUser WHERE username = :username AND baseUrl = :server")
    abstract fun getUserWithUsernameAndServer(username: String, server: String): Maybe<GccUserEntity>

    @Query(
        "UPDATE GccUser SET `current` = CASE " +
            "WHEN id == :id THEN 1 " +
            "WHEN id != :id THEN 0 " +
            "END"
    )
    abstract fun setUserAsActiveWithId(id: Long): Int

    @Query("Update GccUser SET pushConfigurationState = :state WHERE id == :id")
    abstract fun updatePushState(id: Long, state: PushConfigurationState): Single<Int>

    companion object {
        const val TAG = "GccUsersDao"
    }
}
