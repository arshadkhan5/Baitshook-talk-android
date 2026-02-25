/*
 * Nextcloud Talk - Android Client
 *
 * SPDX-FileCopyrightText: 2022 Andy Scherzinger <infoi@andy-scherzinger.de>
 * SPDX-FileCopyrightText: 2017 Mario Danic <mario@lovelyhq.com>
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package com.gcc.talk.gccUsers

import android.text.TextUtils
import android.util.Log
import com.bluelinelabs.logansquare.LoganSquare
import com.gcc.talk.gccData.user.GccUsersRepository
import com.gcc.talk.gccData.user.model.GccUser
import com.gcc.talk.gccModels.GccExternalSignalingServer
import com.gcc.talk.gccModels.json.capabilities.Capabilities
import com.gcc.talk.gccModels.json.capabilities.ServerVersion
import com.gcc.talk.gccModels.json.push.PushConfigurationState
import io.reactivex.Maybe
import io.reactivex.Observable
import io.reactivex.Single

@Suppress("TooManyFunctions")
class GccUserManager internal constructor(private val userRepository: GccUsersRepository) {
    val users: Single<List<GccUser>>
        get() = userRepository.getUsers()

    val usersScheduledForDeletion: Single<List<GccUser>>
        get() = userRepository.getUsersScheduledForDeletion()

    val currentUser: Maybe<GccUser>
        get() {
            return userRepository.getActiveUser()
                .switchIfEmpty(Maybe.defer { getAnyUserAndSetAsActive() })
        }

    val currentUserObservable: Observable<GccUser>
        get() {
            return userRepository.getActiveUserObservable()
        }

    fun deleteUser(internalId: Long): Int =
        userRepository.deleteUser(userRepository.getUserWithId(internalId).blockingGet())

    fun getUserWithId(id: Long): Maybe<GccUser> = userRepository.getUserWithId(id)

    fun checkIfUserIsScheduledForDeletion(username: String, server: String): Single<Boolean> =
        userRepository
            .getUserWithUsernameAndServer(username, server)
            .map { it.scheduledForDeletion }
            .switchIfEmpty(Single.just(false))

    fun getUserWithInternalId(id: Long): Maybe<GccUser> = userRepository.getUserWithIdNotScheduledForDeletion(id)

    fun checkIfUserExists(username: String, server: String): Single<Boolean> =
        userRepository
            .getUserWithUsernameAndServer(username, server)
            .map { true }
            .switchIfEmpty(Single.just(false))

    /**
     * Don't ask
     *
     * @return `true` if the user was updated **AND** there is another user to set as active, `false` otherwise
     */
    fun scheduleUserForDeletionWithId(id: Long): Single<Boolean> =
        userRepository.getUserWithId(id)
            .map { user ->
                user.scheduledForDeletion = true
                user.current = false
                userRepository.updateUser(user)
            }
            .flatMap { getAnyUserAndSetAsActive() }
            .map { true }
            .switchIfEmpty(Single.just(false))

    private fun getAnyUserAndSetAsActive(): Maybe<GccUser> {
        val results = userRepository.getUsersNotScheduledForDeletion()

        return results
            .flatMapMaybe {
                if (it.isNotEmpty()) {
                    val user = it.first()
                    if (setUserAsActive(user).blockingGet()) {
                        userRepository.getActiveUser()
                    } else {
                        Maybe.empty()
                    }
                } else {
                    Maybe.empty()
                }
            }
    }

    fun updateExternalSignalingServer(id: Long, externalSignalingServer: GccExternalSignalingServer): Single<Int> =
        userRepository.getUserWithId(id).map { user ->
            user.externalSignalingServer = externalSignalingServer
            userRepository.updateUser(user)
        }.toSingle()

    fun updateOrCreateUser(user: GccUser): Single<Int> =
        Single.fromCallable {
            when (user.id) {
                null -> userRepository.insertUser(user).toInt()
                else -> userRepository.updateUser(user)
            }
        }

    fun saveUser(user: GccUser): Single<Int> =
        Single.fromCallable {
            userRepository.updateUser(user)
        }

    fun setUserAsActive(user: GccUser): Single<Boolean> {
        Log.d(TAG, "setUserAsActive:" + user.id!!)
        return userRepository.setUserAsActiveWithId(user.id!!)
    }

    fun storeProfile(username: String?, userAttributes: UserAttributes): Maybe<GccUser> =
        findUser(userAttributes)
            .map { user: GccUser? ->
                when (user) {
                    null -> createUser(
                        username,
                        userAttributes
                    )
                    else -> {
                        user.token = userAttributes.token
                        user.baseUrl = userAttributes.serverUrl
                        user.current = userAttributes.currentUser
                        user.userId = userAttributes.userId
                        user.token = userAttributes.token
                        user.displayName = userAttributes.displayName
                        user.clientCertificate = userAttributes.certificateAlias

                        updateUserData(
                            user,
                            userAttributes
                        )

                        user
                    }
                }
            }
            .switchIfEmpty(Maybe.just(createUser(username, userAttributes)))
            .map { user ->
                userRepository.insertUser(user)
            }
            .flatMap { id ->
                userRepository.getUserWithId(id)
            }

    private fun findUser(userAttributes: UserAttributes): Maybe<GccUser> =
        if (userAttributes.id != null) {
            userRepository.getUserWithId(userAttributes.id)
        } else {
            Maybe.empty()
        }

    private fun updateUserData(user: GccUser, userAttributes: UserAttributes) {
        user.userId = userAttributes.userId
        user.token = userAttributes.token
        user.displayName = userAttributes.displayName
        if (userAttributes.pushConfigurationState != null) {
            user.pushConfigurationState = LoganSquare
                .parse(userAttributes.pushConfigurationState, PushConfigurationState::class.java)
        }
        if (userAttributes.capabilities != null) {
            user.capabilities = LoganSquare
                .parse(userAttributes.capabilities, Capabilities::class.java)
        }
        if (userAttributes.serverVersion != null) {
            user.serverVersion = LoganSquare
                .parse(userAttributes.serverVersion, ServerVersion::class.java)
        }
        user.clientCertificate = userAttributes.certificateAlias
        if (userAttributes.externalSignalingServer != null) {
            user.externalSignalingServer = LoganSquare
                .parse(userAttributes.externalSignalingServer, GccExternalSignalingServer::class.java)
        }
        user.current = userAttributes.currentUser == true
    }

    private fun createUser(username: String?, userAttributes: UserAttributes): GccUser {
        val user = GccUser()
        user.baseUrl = userAttributes.serverUrl
        user.username = username
        user.token = userAttributes.token
        if (!TextUtils.isEmpty(userAttributes.displayName)) {
            user.displayName = userAttributes.displayName
        }
        if (userAttributes.pushConfigurationState != null) {
            user.pushConfigurationState = LoganSquare
                .parse(userAttributes.pushConfigurationState, PushConfigurationState::class.java)
        }
        if (!TextUtils.isEmpty(userAttributes.userId)) {
            user.userId = userAttributes.userId
        }
        if (!TextUtils.isEmpty(userAttributes.capabilities)) {
            user.capabilities = LoganSquare.parse(userAttributes.capabilities, Capabilities::class.java)
        }
        if (!TextUtils.isEmpty(userAttributes.serverVersion)) {
            user.serverVersion = LoganSquare.parse(userAttributes.serverVersion, ServerVersion::class.java)
        }
        if (!TextUtils.isEmpty(userAttributes.certificateAlias)) {
            user.clientCertificate = userAttributes.certificateAlias
        }
        if (!TextUtils.isEmpty(userAttributes.externalSignalingServer)) {
            user.externalSignalingServer = LoganSquare
                .parse(userAttributes.externalSignalingServer, GccExternalSignalingServer::class.java)
        }
        user.current = userAttributes.currentUser == true
        return user
    }

    fun updatePushState(id: Long, state: PushConfigurationState): Single<Int> =
        userRepository.updatePushState(id, state)

    companion object {
        const val TAG = "GccUserManager"
    }

    data class UserAttributes(
        val id: Long?,
        val serverUrl: String?,
        val currentUser: Boolean,
        val userId: String?,
        val token: String?,
        val displayName: String?,
        val pushConfigurationState: String?,
        val capabilities: String?,
        val serverVersion: String?,
        val certificateAlias: String?,
        val externalSignalingServer: String?
    )
}
