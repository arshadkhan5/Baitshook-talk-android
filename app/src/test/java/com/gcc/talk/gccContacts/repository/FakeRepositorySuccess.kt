/*
 * Nextcloud Talk - Android Client
 *
 * SPDX-FileCopyrightText: 2024 Sowjanya Kota <sowjanya.kch@email.com>
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.gcc.talk.gccContacts.repository

import com.gcc.talk.gccContacts.GccContactsRepository
import com.gcc.talk.gccContacts.apiService.FakeItem
import com.gcc.talk.gccData.user.model.GccUser

class FakeRepositorySuccess : GccContactsRepository {
    override suspend fun getContacts(user: GccUser, searchQuery: String?, shareTypes: List<String>) =
        FakeItem.contactsOverall

    override suspend fun createRoom(
        user: GccUser,
        roomType: String,
        sourceType: String?,
        userId: String,
        conversationName: String?
    ) = FakeItem.roomOverall

    override fun getImageUri(user: GccUser, avatarId: String, requestBigSize: Boolean) =
        "https://mydomain.com/index.php/avatar/$avatarId/512"
}
