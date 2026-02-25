/*
 * Nextcloud Talk - Android Client
 *
 * SPDX-FileCopyrightText: 2022 Nextcloud GmbH
 * SPDX-FileCopyrightText: 2022 Álvaro Brey <alvaro@alvarobrey.com>
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package com.gcc.talk.test.fakes

import com.gcc.talk.gccModels.domain.GccSearchMessageEntry
import com.gcc.talk.gccRepositories.unifiedsearch.GccUnifiedSearchRepository
import io.reactivex.Observable

class FakeUnifiedSearchRepository : GccUnifiedSearchRepository {

    lateinit var response: GccUnifiedSearchRepository.UnifiedSearchResults<GccSearchMessageEntry>
    var lastRequestedCursor = -1

    override fun searchMessages(
        credentials: String?,
        url: String,
        searchTerm: String,
        cursor: Int,
        limit: Int
    ): Observable<GccUnifiedSearchRepository.UnifiedSearchResults<GccSearchMessageEntry>> {
        lastRequestedCursor = cursor
        return Observable.just(response)
    }

    override fun searchInRoom(
        credentials: String?,
        url: String,
        roomToken: String,
        searchTerm: String,
        cursor: Int,
        limit: Int
    ): Observable<GccUnifiedSearchRepository.UnifiedSearchResults<GccSearchMessageEntry>> {
        lastRequestedCursor = cursor
        return Observable.just(response)
    }
}
