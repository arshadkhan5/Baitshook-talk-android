/*
 * Nextcloud Talk - Android Client
 *
 * SPDX-FileCopyrightText: 2024 Sowjanya Kota <sowjanya.kota@gmail.com>
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.gcc.talk.gccContacts.repository

import com.gcc.talk.gccContacts.GccContactsRepository
import com.gcc.talk.gccData.user.model.GccUser
import com.gcc.talk.gccModels.json.autocomplete.AutocompleteOverall
import com.gcc.talk.gccModels.json.conversations.RoomOverall

class FakeRepositoryError : GccContactsRepository {
    @Suppress("Detekt.TooGenericExceptionThrown")
    override suspend fun getContacts(user: GccUser, searchQuery: String?, shareTypes: List<String>): AutocompleteOverall =
        throw Exception("unable to fetch contacts")

    @Suppress("Detekt.TooGenericExceptionThrown")
    override suspend fun createRoom(
        user: GccUser,
        roomType: String,
        sourceType: String?,
        userId: String,
        conversationName: String?
    ): RoomOverall = throw Exception("unable to create room")

    override fun getImageUri(user: GccUser, avatarId: String, requestBigSize: Boolean) =
        "https://mydoman.com/index.php/avatar/$avatarId/512"
}
