/*
 * Nextcloud Talk - Android Client
 *
 * SPDX-FileCopyrightText: 2022 Álvaro Brey <alvaro@alvarobrey.com>
 * SPDX-FileCopyrightText: 2022 Tim Krüger <t@timkrueger.me>
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package com.gcc.talk.gccShareditems.repositories

import com.gcc.talk.gccShareditems.model.GccSharedItemType
import com.gcc.talk.gccShareditems.model.GccSharedItems
import io.reactivex.Observable

interface GccSharedItemsRepository {

    fun media(parameters: Parameters, type: GccSharedItemType): Observable<GccSharedItems>?

    fun media(parameters: Parameters, type: GccSharedItemType, lastKnownMessageId: Int?): Observable<GccSharedItems>?

    fun availableTypes(parameters: Parameters): Observable<Set<GccSharedItemType>>

    data class Parameters(val userName: String, val userToken: String, val baseUrl: String, val roomToken: String)
}
