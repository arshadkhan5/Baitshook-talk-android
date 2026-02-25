/*
 * Nextcloud Talk - Android Client
 *
 * SPDX-FileCopyrightText: 2022 Andy Scherzinger <info@andy-scherzinger.de>
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package com.gcc.talk.gccArbitrarystorage

import com.gcc.talk.gccData.storage.GccArbitraryStoragesRepository
import com.gcc.talk.gccData.storage.model.GccArbitraryStorage
import io.reactivex.Maybe

class GccArbitraryStorageManager(private val arbitraryStoragesRepository: GccArbitraryStoragesRepository) {
    fun storeStorageSetting(accountIdentifier: Long, key: String, value: String?, objectString: String?) {
        arbitraryStoragesRepository.saveArbitraryStorage(
            GccArbitraryStorage(
                accountIdentifier,
                key,
                objectString,
                value
            )
        )
    }

    fun getStorageSetting(accountIdentifier: Long, key: String, objectString: String): Maybe<GccArbitraryStorage> =
        arbitraryStoragesRepository.getStorageSetting(accountIdentifier, key, objectString)

    fun deleteAllEntriesForAccountIdentifier(accountIdentifier: Long): Int =
        arbitraryStoragesRepository.deleteArbitraryStorage(accountIdentifier)
}
