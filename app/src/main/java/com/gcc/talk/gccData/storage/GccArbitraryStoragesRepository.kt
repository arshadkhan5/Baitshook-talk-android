/*
 * Nextcloud Talk - Android Client
 *
 * SPDX-FileCopyrightText: 2022 Andy Scherzinger <info@andy-scherzinger.de>
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package com.gcc.talk.gccData.storage

import com.gcc.talk.gccData.storage.model.GccArbitraryStorage
import com.gcc.talk.gccData.storage.model.GccArbitraryStorageEntity
import io.reactivex.Maybe

interface GccArbitraryStoragesRepository {
    fun getStorageSetting(accountIdentifier: Long, key: String, objectString: String): Maybe<GccArbitraryStorage>
    fun deleteArbitraryStorage(accountIdentifier: Long): Int
    fun saveArbitraryStorage(arbitraryStorage: GccArbitraryStorage): Long
    fun getAll(): Maybe<List<GccArbitraryStorageEntity>>
}
