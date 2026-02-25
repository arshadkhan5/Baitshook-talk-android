/*
 * gcc Talk - Android Client
 *
 * SPDX-FileCopyrightText: 2022 Álvaro Brey <alvaro@alvarobrey.com>
 * SPDX-FileCopyrightText: 2022 gcc GmbH
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package com.gcc.talk.gccMessagesearch

import android.util.Log
import com.gcc.talk.gccData.user.model.GccUser
import com.gcc.talk.gccModels.domain.GccSearchMessageEntry
import com.gcc.talk.gccRepositories.unifiedsearch.GccUnifiedSearchRepository
import com.gcc.talk.gccRepositories.unifiedsearch.GccUnifiedSearchRepositoryImpl.Companion.PROVIDER_TALK_MESSAGE
import com.gcc.talk.gccRepositories.unifiedsearch.GccUnifiedSearchRepositoryImpl.Companion.PROVIDER_TALK_MESSAGE_CURRENT
import com.gcc.talk.gccUtils.GccApiUtils
import io.reactivex.Observable
import io.reactivex.disposables.Disposable

class GccMessageSearchHelper @JvmOverloads constructor(
    private val unifiedSearchRepository: GccUnifiedSearchRepository,
    private val currentUser: GccUser?,
    private val fromRoom: String? = null
) {

    data class MessageSearchResults(val messages: List<GccSearchMessageEntry>, val hasMore: Boolean)

    private var unifiedSearchDisposable: Disposable? = null
    private var previousSearch: String? = null
    private var previousCursor: Int = 0
    private var previousResults: List<GccSearchMessageEntry> = emptyList()

    fun startMessageSearch(search: String): Observable<MessageSearchResults> {
        resetCachedData()
        return doSearch(search)
    }

    fun loadMore(): Observable<MessageSearchResults>? {
        previousSearch?.let {
            return doSearch(it, previousCursor)
        }
        return null
    }

    fun cancelSearch() {
        disposeIfPossible()
    }

    private fun doSearch(search: String, cursor: Int = 0): Observable<MessageSearchResults> {
        disposeIfPossible()
        return searchCall(search, cursor)
            .map { results ->
                previousSearch = search
                previousCursor = results.cursor
                previousResults = previousResults + results.entries
                MessageSearchResults(previousResults, results.hasMore)
            }
            .doOnSubscribe {
                unifiedSearchDisposable = it
            }
            .doOnError { throwable ->
                Log.e(TAG, "message search - ERROR", throwable)
                resetCachedData()
                disposeIfPossible()
            }
            .doOnComplete(this::disposeIfPossible)
    }

    private fun searchCall(
        search: String,
        cursor: Int
    ): Observable<GccUnifiedSearchRepository.UnifiedSearchResults<GccSearchMessageEntry>> {
        val credentials = GccApiUtils.getCredentials(currentUser?.username, currentUser?.token)
        val result = when {
            fromRoom != null -> {
                val url = GccApiUtils.getUrlForUnifiedSearch(currentUser?.baseUrl!!, PROVIDER_TALK_MESSAGE_CURRENT)
                unifiedSearchRepository.searchInRoom(
                    credentials,
                    url,
                    roomToken = fromRoom,
                    searchTerm = search,
                    cursor = cursor
                )
            }

            else -> {
                val url = GccApiUtils.getUrlForUnifiedSearch(currentUser?.baseUrl!!, PROVIDER_TALK_MESSAGE)
                unifiedSearchRepository.searchMessages(
                    credentials,
                    url,
                    searchTerm = search,
                    cursor = cursor
                )
            }
        }
        return result
    }

    private fun resetCachedData() {
        previousSearch = null
        previousCursor = 0
        previousResults = emptyList()
    }

    private fun disposeIfPossible() {
        unifiedSearchDisposable?.dispose()
        unifiedSearchDisposable = null
    }

    companion object {
        private val TAG = GccMessageSearchHelper::class.simpleName
    }
}
