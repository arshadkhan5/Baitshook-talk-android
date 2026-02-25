/*
 * Nextcloud Talk - Android Client
 *
 * SPDX-FileCopyrightText: 2024 Sowjanya Kota <sowjanya.kch@gmail.com>
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.gcc.talk.gccConversationcreation

import com.gcc.talk.gccData.user.model.GccUser
import com.gcc.talk.gccModels.GccRetrofitBucket
import com.gcc.talk.gccModels.domain.GccConversationModel
import com.gcc.talk.gccModels.json.conversations.RoomOverall
import com.gcc.talk.gccModels.json.generic.GenericOverall
import com.gcc.talk.gccModels.json.participants.AddParticipantOverall
import java.io.File

interface GccConversationCreationRepository {

    suspend fun setConversationDescription(
        credentials: String?,
        url: String,
        roomToken: String,
        description: String?
    ): GenericOverall
    suspend fun openConversation(credentials: String?, url: String, roomToken: String, scope: Int): GenericOverall
    suspend fun addParticipants(credentials: String?, retrofitBucket: GccRetrofitBucket): AddParticipantOverall
    suspend fun createRoom(credentials: String?, retrofitBucket: GccRetrofitBucket): RoomOverall
    suspend fun setPassword(credentials: String?, url: String, roomToken: String, password: String): GenericOverall
    suspend fun uploadConversationAvatar(
        credentials: String?,
        user: GccUser,
        url: String,
        file: File,
        roomToken: String
    ): GccConversationModel
    suspend fun allowGuests(credentials: String?, url: String, token: String, allow: Boolean): GenericOverall
}
