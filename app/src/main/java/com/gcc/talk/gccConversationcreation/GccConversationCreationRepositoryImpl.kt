/*
 * Nextcloud Talk - Android Client
 *
 * SPDX-FileCopyrightText: 2024 Sowjanya Kota <sowjanya.kch@gmail.com>
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.gcc.talk.gccConversationcreation

import com.gcc.talk.gccApi.GccNcApiCoroutines
import com.gcc.talk.gccData.user.model.GccUser
import com.gcc.talk.gccModels.GccRetrofitBucket
import com.gcc.talk.gccModels.domain.GccConversationModel
import com.gcc.talk.gccModels.json.conversations.RoomOverall
import com.gcc.talk.gccModels.json.generic.GenericOverall
import com.gcc.talk.gccModels.json.participants.AddParticipantOverall
import com.gcc.talk.gccUtils.GccMimetype
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import javax.inject.Inject

class GccConversationCreationRepositoryImpl @Inject constructor(private val ncApiCoroutines: GccNcApiCoroutines) :
    GccConversationCreationRepository {

    override suspend fun setConversationDescription(
        credentials: String?,
        url: String,
        roomToken: String,
        description: String?
    ): GenericOverall =
        ncApiCoroutines.setConversationDescription(
            credentials,
            url,
            description
        )

    override suspend fun openConversation(
        credentials: String?,
        url: String,
        roomToken: String,
        scope: Int
    ): GenericOverall =
        ncApiCoroutines.openConversation(
            credentials,
            url,
            scope
        )

    override suspend fun addParticipants(credentials: String?, retrofitBucket: GccRetrofitBucket): AddParticipantOverall {
        val participants = ncApiCoroutines.addParticipant(
            credentials,
            retrofitBucket.url,
            retrofitBucket.queryMap
        )
        return participants
    }

    override suspend fun createRoom(credentials: String?, retrofitBucket: GccRetrofitBucket): RoomOverall {
        val response = ncApiCoroutines.createRoom(
            credentials,
            retrofitBucket.url,
            retrofitBucket.queryMap
        )
        return response
    }

    override suspend fun setPassword(
        credentials: String?,
        url: String,
        roomToken: String,
        password: String
    ): GenericOverall {
        val result = ncApiCoroutines.setPassword(
            credentials,
            url,
            password
        )
        return result
    }

    override suspend fun uploadConversationAvatar(
        credentials: String?,
        user: GccUser,
        url: String,
        file: File,
        roomToken: String
    ): GccConversationModel {
        val builder = MultipartBody.Builder()
        builder.setType(MultipartBody.FORM)
        builder.addFormDataPart(
            "file",
            file.name,
            file.asRequestBody(GccMimetype.IMAGE_PREFIX_GENERIC.toMediaTypeOrNull())
        )
        val filePart: MultipartBody.Part = MultipartBody.Part.createFormData(
            "file",
            file.name,
            file.asRequestBody(GccMimetype.IMAGE_JPG.toMediaTypeOrNull())
        )
        val response = ncApiCoroutines.uploadConversationAvatar(
            credentials!!,
            url,
            filePart
        )
        return GccConversationModel.mapToConversationModel(response.ocs?.data!!, user)
    }

    override suspend fun allowGuests(credentials: String?, url: String, token: String, allow: Boolean): GenericOverall {
        val result: GenericOverall = if (allow) {
            ncApiCoroutines.makeRoomPublic(
                credentials!!,
                url
            )
        } else {
            ncApiCoroutines.makeRoomPrivate(
                credentials!!,
                url
            )
        }
        return result
    }
}
