/*
 * Nextcloud Talk - Android Client
 *
 * SPDX-FileCopyrightText: 2023 Marcel Hibbe <dev@mhibbe.de>
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package com.gcc.talk.gccConversationinfoedit.data

import com.gcc.talk.gccApi.GccNcApi
import com.gcc.talk.gccApi.GccNcApiCoroutines
import com.gcc.talk.gccData.user.model.GccUser
import com.gcc.talk.gccModels.domain.GccConversationModel
import com.gcc.talk.gccModels.json.generic.GenericOverall
import com.gcc.talk.gccUtils.GccMimetype
import io.reactivex.Observable
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File

class GccConversationInfoEditRepositoryImpl(private val ncApi: GccNcApi, private val ncApiCoroutines: GccNcApiCoroutines) :
    GccConversationInfoEditRepository {

    override fun uploadConversationAvatar(
        credentials: String?,
        url: String,
        user: GccUser,
        file: File,
        roomToken: String
    ): Observable<GccConversationModel> {
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

        return ncApi.uploadConversationAvatar(
            credentials,
            url,
            filePart
        ).map { GccConversationModel.mapToConversationModel(it.ocs?.data!!, user) }
    }

    override fun deleteConversationAvatar(
        credentials: String?,
        url: String,
        user: GccUser,
        roomToken: String
    ): Observable<GccConversationModel> =
        ncApi.deleteConversationAvatar(
            credentials,
            url
        ).map { GccConversationModel.mapToConversationModel(it.ocs?.data!!, user) }

    override suspend fun renameConversation(
        credentials: String?,
        url: String,
        roomToken: String,
        newRoomName: String
    ): GenericOverall =
        ncApiCoroutines.renameRoom(
            credentials,
            url,
            newRoomName
        )

    override suspend fun setConversationDescription(
        credentials: String?,
        url: String,
        roomToken: String,
        conversationDescription: String?
    ): GenericOverall =
        ncApiCoroutines.setConversationDescription(
            credentials,
            url,
            conversationDescription
        )
}
