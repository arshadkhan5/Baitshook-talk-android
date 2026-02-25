/*
 * Nextcloud Talk - Android Client
 *
 * SPDX-FileCopyrightText: 2022 Marcel Hibbe <dev@mhibbe.de>
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package com.gcc.talk.gccRepositories.reactions

import com.gcc.talk.gccModels.domain.GccReactionAddedModel
import com.gcc.talk.gccModels.domain.GccReactionDeletedModel
import com.gcc.talk.gccChat.data.model.GccChatMessage
import io.reactivex.Observable

interface GccReactionsRepository {

    @Suppress("LongParameterList")
    fun addReaction(
        credentials: String?,
        userId: Long,
        url: String,
        roomToken: String,
        message: GccChatMessage,
        emoji: String
    ): Observable<GccReactionAddedModel>

    @Suppress("LongParameterList")
    fun deleteReaction(
        credentials: String?,
        userId: Long,
        url: String,
        roomToken: String,
        message: GccChatMessage,
        emoji: String
    ): Observable<GccReactionDeletedModel>
}
