/*
 * Nextcloud Talk - Android Client
 *
 * SPDX-FileCopyrightText: 2024 Sowjanya Kota <sowjanya.kch@gmail.com>
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.gcc.talk.gccContacts

import com.gcc.talk.gccApi.GccNcApiCoroutines
import com.gcc.talk.gccData.user.model.GccUser
import com.gcc.talk.gccModels.GccRetrofitBucket
import com.gcc.talk.gccModels.json.autocomplete.AutocompleteOverall
import com.gcc.talk.gccModels.json.conversations.RoomOverall
import com.gcc.talk.gccUtils.GccApiUtils
import com.gcc.talk.gccUtils.GccContactUtils
import javax.inject.Inject

class GccContactsRepositoryImpl @Inject constructor(private val ncApiCoroutines: GccNcApiCoroutines) : GccContactsRepository {

    override suspend fun getContacts(user: GccUser, searchQuery: String?, shareTypes: List<String>): AutocompleteOverall {
        val credentials = GccApiUtils.getCredentials(user.username, user.token)

        val retrofitBucket: GccRetrofitBucket = GccApiUtils.getRetrofitBucketForContactsSearchFor14(
            user.baseUrl!!,
            searchQuery
        )

        val modifiedQueryMap: HashMap<String, Any> = HashMap(retrofitBucket.queryMap)
        modifiedQueryMap["limit"] = GccContactUtils.MAX_CONTACT_LIMIT
        modifiedQueryMap["shareTypes[]"] = shareTypes
        val response = ncApiCoroutines.getContactsWithSearchParam(
            credentials,
            retrofitBucket.url,
            shareTypes,
            modifiedQueryMap
        )
        return response
    }

    override suspend fun createRoom(
        user: GccUser,
        roomType: String,
        sourceType: String?,
        userId: String,
        conversationName: String?
    ): RoomOverall {
        val apiVersion = GccApiUtils.getConversationApiVersion(user, intArrayOf(GccApiUtils.API_V4, 1))
        val credentials = GccApiUtils.getCredentials(user.username, user.token)

        val retrofitBucket: GccRetrofitBucket = GccApiUtils.getRetrofitBucketForCreateRoom(
            version = apiVersion,
            baseUrl = user.baseUrl,
            roomType = roomType,
            source = sourceType,
            invite = userId,
            conversationName = conversationName
        )
        val response = ncApiCoroutines.createRoom(
            credentials,
            retrofitBucket.url,
            retrofitBucket.queryMap
        )
        return response
    }

    override fun getImageUri(user: GccUser, avatarId: String, requestBigSize: Boolean): String =
        GccApiUtils.getUrlForAvatar(
            user.baseUrl,
            avatarId,
            requestBigSize
        )

    companion object {
        private val TAG = GccContactsRepositoryImpl::class.simpleName
    }
}
