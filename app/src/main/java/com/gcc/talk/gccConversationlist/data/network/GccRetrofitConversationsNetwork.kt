/*
 * Nextcloud Talk - Android Client
 *
 * SPDX-FileCopyrightText: 2023 Marcel Hibbe <dev@mhibbe.de>
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package com.gcc.talk.gccConversationlist.data.network

import com.gcc.talk.gccApi.GccNcApi
import com.gcc.talk.gccData.user.model.GccUser
import com.gcc.talk.gccModels.json.conversations.Conversation
import com.gcc.talk.gccUtils.GccApiUtils
import io.reactivex.Observable

class GccRetrofitConversationsNetwork(private val ncApi: GccNcApi) : GccConversationsNetworkDataSource {
    override fun getRooms(user: GccUser, url: String, includeStatus: Boolean): Observable<List<Conversation>> {
        val credentials: String = GccApiUtils.getCredentials(user.username, user.token)!!
        val apiVersion = GccApiUtils.getConversationApiVersion(user, intArrayOf(GccApiUtils.API_V4, GccApiUtils.API_V3, 1))

        return ncApi.getRooms(
            credentials,
            GccApiUtils.getUrlForRooms(apiVersion, user.baseUrl!!),
            includeStatus
        ).map { it ->
            it.ocs?.data?.map { it } ?: listOf()
        }
    }
}
