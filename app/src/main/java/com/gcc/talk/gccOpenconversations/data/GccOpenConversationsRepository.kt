/*
 * Nextcloud Talk - Android Client
 *
 * SPDX-FileCopyrightText: 2023 Marcel Hibbe <dev@mhibbe.de>
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package com.gcc.talk.gccOpenconversations.data

import com.gcc.talk.gccData.user.model.GccUser
import com.gcc.talk.gccModels.json.conversations.Conversation

interface GccOpenConversationsRepository {

    suspend fun fetchConversations(user: GccUser, url: String, searchTerm: String): Result<List<Conversation>>
}
