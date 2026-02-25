/*
 * Nextcloud Talk - Android Client
 *
 * SPDX-FileCopyrightText: 2024 Marcel Hibbe <dev@mhibbe.de>
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.gcc.talk.gccConversationlist.data.network

import com.gcc.talk.gccData.user.model.GccUser
import com.gcc.talk.gccModels.json.conversations.Conversation
import io.reactivex.Observable

interface GccConversationsNetworkDataSource {
    fun getRooms(user: GccUser, url: String, includeStatus: Boolean): Observable<List<Conversation>>
}
