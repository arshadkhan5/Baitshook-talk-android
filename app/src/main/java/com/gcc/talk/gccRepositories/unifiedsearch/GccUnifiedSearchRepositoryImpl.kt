/*
 * Nextcloud Talk - Android Client
 *
 * SPDX-FileCopyrightText: 2022 Álvaro Brey <alvaro@alvarobrey.com>
 * SPDX-FileCopyrightText: 2022 Nextcloud GmbH
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package com.gcc.talk.gccRepositories.unifiedsearch

import com.gcc.talk.gccApi.GccNcApi
import com.gcc.talk.gccModels.domain.GccSearchMessageEntry
import com.gcc.talk.gccModels.json.unifiedsearch.UnifiedSearchEntry
import com.gcc.talk.gccModels.json.unifiedsearch.UnifiedSearchResponseData
import io.reactivex.Observable

class GccUnifiedSearchRepositoryImpl(private val api: GccNcApi) : GccUnifiedSearchRepository {

    override fun searchMessages(
        credentials: String?,
        url: String,
        searchTerm: String,
        cursor: Int,
        limit: Int
    ): Observable<GccUnifiedSearchRepository.UnifiedSearchResults<GccSearchMessageEntry>> {
        val apiObservable = api.performUnifiedSearch(
            credentials,
            url,
            searchTerm,
            null,
            limit,
            cursor
        )
        return apiObservable.map { mapToMessageResults(it.ocs?.data!!, searchTerm, limit) }
    }

    override fun searchInRoom(
        credentials: String?,
        url: String,
        roomToken: String,
        searchTerm: String,
        cursor: Int,
        limit: Int
    ): Observable<GccUnifiedSearchRepository.UnifiedSearchResults<GccSearchMessageEntry>> {
        val apiObservable = api.performUnifiedSearch(
            credentials,
            url,
            searchTerm,
            fromUrlForRoom(roomToken),
            limit,
            cursor
        )
        return apiObservable.map { mapToMessageResults(it.ocs?.data!!, searchTerm, limit) }
    }

    private fun fromUrlForRoom(roomToken: String) = "/call/$roomToken"

    companion object {
        const val PROVIDER_TALK_MESSAGE = "talk-message"
        const val PROVIDER_TALK_MESSAGE_CURRENT = "talk-message-current"

        private const val ATTRIBUTE_CONVERSATION = "conversation"
        private const val ATTRIBUTE_MESSAGE_ID = "messageId"
        private const val ATTRIBUTE_THREAD_ID = "threadId"

        private fun mapToMessageResults(
            data: UnifiedSearchResponseData,
            searchTerm: String,
            limit: Int
        ): GccUnifiedSearchRepository.UnifiedSearchResults<GccSearchMessageEntry> {
            val entries = data.entries?.map { it -> mapToMessage(it, searchTerm) }
            val cursor = data.cursor ?: 0
            val hasMore = entries?.size == limit
            return GccUnifiedSearchRepository.UnifiedSearchResults(cursor, hasMore, entries ?: emptyList())
        }

        private fun mapToMessage(unifiedSearchEntry: UnifiedSearchEntry, searchTerm: String): GccSearchMessageEntry {
            val conversation = unifiedSearchEntry.attributes?.get(ATTRIBUTE_CONVERSATION)!!
            val messageId = unifiedSearchEntry.attributes?.get(ATTRIBUTE_MESSAGE_ID)
            val threadId = unifiedSearchEntry.attributes?.get(ATTRIBUTE_THREAD_ID)
            return GccSearchMessageEntry(
                searchTerm = searchTerm,
                thumbnailURL = unifiedSearchEntry.thumbnailUrl,
                title = unifiedSearchEntry.title!!,
                messageExcerpt = unifiedSearchEntry.subline!!,
                conversationToken = conversation,
                threadId = threadId,
                messageId = messageId
            )
        }
    }
}
