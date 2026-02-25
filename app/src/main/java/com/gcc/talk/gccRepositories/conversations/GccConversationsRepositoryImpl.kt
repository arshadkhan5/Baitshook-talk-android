/*
 * Nextcloud Talk - Android Client
 *
 * SPDX-FileCopyrightText: 2022 Tim Krüger <t@timkrueger.me>
 * SPDX-FileCopyrightText: 2022 Nextcloud GmbH
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package com.gcc.talk.gccRepositories.conversations

import com.gcc.talk.gccApi.GccNcApi
import com.gcc.talk.gccApi.GccNcApiCoroutines
import com.gcc.talk.gccConversationinfo.GccCreateRoomRequest
import com.gcc.talk.gccData.user.model.GccUser
import com.gcc.talk.gccModels.json.conversations.RoomOverall
import com.gcc.talk.gccModels.json.generic.GenericOverall
import com.gcc.talk.gccModels.json.participants.TalkBan
import com.gcc.talk.gccModels.json.profile.Profile
import com.gcc.talk.gccRepositories.conversations.GccConversationsRepository.ResendInvitationsResult
import com.gcc.talk.gccUtils.GccApiUtils
import io.reactivex.Observable

class GccConversationsRepositoryImpl(private val api: GccNcApi, private val coroutineApi: GccNcApiCoroutines) :
    GccConversationsRepository {
    override suspend fun allowGuests(user: GccUser, url: String, token: String, allow: Boolean): GenericOverall {
        val credentials = GccApiUtils.getCredentials(user.username, user.token)!!
        val result: GenericOverall = if (allow) {
            coroutineApi.makeRoomPublic(
                credentials,
                url
            )
        } else {
            coroutineApi.makeRoomPrivate(
                credentials,
                url
            )
        }
        return result
    }

    override fun resendInvitations(user: GccUser, url: String): Observable<ResendInvitationsResult> {
        val credentials = GccApiUtils.getCredentials(user.username, user.token)!!
        val apiObservable = api.resendParticipantInvitations(
            credentials,
            url
        )
        return apiObservable.map {
            ResendInvitationsResult(true)
        }
    }

    override suspend fun archiveConversation(credentials: String, url: String): GenericOverall =
        coroutineApi.archiveConversation(credentials, url)

    override suspend fun unarchiveConversation(credentials: String, url: String): GenericOverall =
        coroutineApi.unarchiveConversation(credentials, url)

    override suspend fun setConversationReadOnly(user: GccUser, url: String, state: Int): GenericOverall {
        val credentials = GccApiUtils.getCredentials(user.username, user.token)!!
        val result = coroutineApi.setConversationReadOnly(
            credentials,
            url,
            state
        )
        return result
    }

    override suspend fun setPassword(user: GccUser, url: String, password: String): GenericOverall {
        val credentials = GccApiUtils.getCredentials(user.username, user.token)!!
        val result = coroutineApi.setPassword(
            credentials,
            url,
            password
        )
        return result
    }

    override suspend fun clearChatHistory(user: GccUser, url: String): GenericOverall {
        val credentials = GccApiUtils.getCredentials(user.username, user.token)!!
        val result = coroutineApi.clearChatHistory(
            credentials,
            url
        )
        return result
    }

    override suspend fun createRoom(credentials: String, url: String, body: GccCreateRoomRequest): RoomOverall {
        val response = coroutineApi.createRoomWithBody(
            credentials,
            url,
            body
        )
        return response
    }

    override suspend fun getProfile(credentials: String, url: String): Profile? =
        coroutineApi.getProfile(credentials, url).ocs?.data

    override suspend fun markConversationAsSensitive(
        credentials: String,
        baseUrl: String,
        roomToken: String
    ): GenericOverall {
        val url = GccApiUtils.getUrlForSensitiveConversation(baseUrl, roomToken)
        return coroutineApi.markConversationAsSensitive(credentials, url)
    }

    override suspend fun markConversationAsInsensitive(
        credentials: String,
        baseUrl: String,
        roomToken: String
    ): GenericOverall {
        val url = GccApiUtils.getUrlForSensitiveConversation(baseUrl, roomToken)
        return coroutineApi.markConversationAsInsensitive(credentials, url)
    }

    override suspend fun markConversationAsImportant(
        credentials: String,
        baseUrl: String,
        roomToken: String
    ): GenericOverall {
        val url = GccApiUtils.getUrlForImportantConversation(baseUrl, roomToken)
        return coroutineApi.markConversationAsImportant(credentials, url)
    }

    override suspend fun markConversationAsUnImportant(
        credentials: String,
        baseUrl: String,
        roomToken: String
    ): GenericOverall {
        val url = GccApiUtils.getUrlForImportantConversation(baseUrl, roomToken)
        return coroutineApi.markConversationAsUnimportant(credentials, url)
    }

    override suspend fun banActor(
        credentials: String,
        url: String,
        actorType: String,
        actorId: String,
        internalNote: String
    ): TalkBan = coroutineApi.banActor(credentials, url, actorType, actorId, internalNote)

    override suspend fun listBans(credentials: String, url: String): List<TalkBan> {
        val talkBanOverall = coroutineApi.listBans(credentials, url)
        return talkBanOverall.ocs?.data!!
    }

    override suspend fun unbanActor(credentials: String, url: String): GenericOverall =
        coroutineApi.unbanActor(credentials, url)

    companion object {
        const val STATUS_CODE_OK = 200
    }
}
