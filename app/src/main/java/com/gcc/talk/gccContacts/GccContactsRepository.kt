/*
 * Nextcloud Talk - Android Client
 *
 * SPDX-FileCopyrightText: 2024 Sowjanya Kota <sowjanya.kch@gmail.com>
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.gcc.talk.gccContacts

import com.gcc.talk.gccData.user.model.GccUser
import com.gcc.talk.gccModels.json.autocomplete.AutocompleteOverall
import com.gcc.talk.gccModels.json.conversations.RoomOverall

interface GccContactsRepository {
    suspend fun getContacts(user: GccUser, searchQuery: String?, shareTypes: List<String>): AutocompleteOverall

    suspend fun createRoom(
        user: GccUser,
        roomType: String,
        sourceType: String?,
        userId: String,
        conversationName: String?
    ): RoomOverall

    fun getImageUri(user: GccUser, avatarId: String, requestBigSize: Boolean): String
}
