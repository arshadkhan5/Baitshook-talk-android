/*
 * Nextcloud Talk - Android Client
 *
 * SPDX-FileCopyrightText: 2023 Marcel Hibbe <dev@mhibbe.de>
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package com.gcc.talk.gccOpenconversations.data

import com.gcc.talk.gccApi.GccNcApiCoroutines
import com.gcc.talk.gccData.user.model.GccUser
import com.gcc.talk.gccModels.json.conversations.Conversation
import com.gcc.talk.gccUtils.GccApiUtils

class GccOpenConversationsRepositoryImpl(private val ncApiCoroutines: GccNcApiCoroutines) : GccOpenConversationsRepository {
    override suspend fun fetchConversations(user: GccUser, url: String, searchTerm: String): Result<List<Conversation>> =
        runCatching {
            val credentials: String = GccApiUtils.getCredentials(user.username, user.token)!!

            val roomOverall = ncApiCoroutines.getOpenConversations(
                credentials,
                url,
                searchTerm
            )
            roomOverall.ocs?.data.orEmpty()
        }
}
