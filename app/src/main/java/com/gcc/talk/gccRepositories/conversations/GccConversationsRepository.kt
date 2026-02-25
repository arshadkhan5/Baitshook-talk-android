/*
 * Nextcloud Talk - Android Client
 *
 * SPDX-FileCopyrightText: 2022 Tim Krüger <t@timkrueger.me>
 * SPDX-FileCopyrightText: 2022 Nextcloud GmbH
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package com.gcc.talk.gccRepositories.conversations

import com.gcc.talk.gccConversationinfo.GccCreateRoomRequest
import com.gcc.talk.gccData.user.model.GccUser
import com.gcc.talk.gccModels.json.conversations.RoomOverall
import com.gcc.talk.gccModels.json.generic.GenericOverall
import com.gcc.talk.gccModels.json.participants.TalkBan
import com.gcc.talk.gccModels.json.profile.Profile
import io.reactivex.Observable

interface GccConversationsRepository {

    suspend fun allowGuests(user: GccUser, url: String, token: String, allow: Boolean): GenericOverall

    data class ResendInvitationsResult(val successful: Boolean)
    fun resendInvitations(user: GccUser, url: String): Observable<ResendInvitationsResult>

    suspend fun archiveConversation(credentials: String, url: String): GenericOverall

    suspend fun unarchiveConversation(credentials: String, url: String): GenericOverall

    suspend fun banActor(
        credentials: String,
        url: String,
        actorType: String,
        actorId: String,
        internalNote: String
    ): TalkBan

    suspend fun listBans(credentials: String, url: String): List<TalkBan>
    suspend fun unbanActor(credentials: String, url: String): GenericOverall

    suspend fun setPassword(user: GccUser, url: String, password: String): GenericOverall

    suspend fun setConversationReadOnly(user: GccUser, url: String, state: Int): GenericOverall

    suspend fun clearChatHistory(user: GccUser, url: String): GenericOverall

    suspend fun createRoom(credentials: String, url: String, body: GccCreateRoomRequest): RoomOverall

    suspend fun getProfile(credentials: String, url: String): Profile?

    suspend fun markConversationAsSensitive(credentials: String, baseUrl: String, roomToken: String): GenericOverall

    suspend fun markConversationAsInsensitive(credentials: String, baseUrl: String, roomToken: String): GenericOverall

    suspend fun markConversationAsImportant(credentials: String, baseUrl: String, roomToken: String): GenericOverall

    suspend fun markConversationAsUnImportant(credentials: String, baseUrl: String, roomToken: String): GenericOverall
}
