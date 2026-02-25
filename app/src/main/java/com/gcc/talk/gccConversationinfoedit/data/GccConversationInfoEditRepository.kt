/*
 * Nextcloud Talk - Android Client
 *
 * SPDX-FileCopyrightText: 2023 Marcel Hibbe <dev@mhibbe.de>
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package com.gcc.talk.gccConversationinfoedit.data

import com.gcc.talk.gccData.user.model.GccUser
import com.gcc.talk.gccModels.domain.GccConversationModel
import com.gcc.talk.gccModels.json.generic.GenericOverall
import io.reactivex.Observable
import java.io.File

interface GccConversationInfoEditRepository {

    fun uploadConversationAvatar(
        credentials: String?,
        url: String,
        user: GccUser,
        file: File,
        roomToken: String
    ): Observable<GccConversationModel>

    fun deleteConversationAvatar(
        credentials: String?,
        url: String,
        user: GccUser,
        roomToken: String
    ): Observable<GccConversationModel>

    suspend fun renameConversation(
        credentials: String?,
        url: String,
        roomToken: String,
        newRoomName: String
    ): GenericOverall

    suspend fun setConversationDescription(
        credentials: String?,
        url: String,
        roomToken: String,
        conversationDescription: String?
    ): GenericOverall
}
