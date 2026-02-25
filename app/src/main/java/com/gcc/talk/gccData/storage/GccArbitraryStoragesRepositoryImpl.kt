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

class GccArbitraryStoragesRepositoryImpl(private val arbitraryStoragesDao: GccArbitraryStoragesDao) :
    GccArbitraryStoragesRepository {
    override fun getStorageSetting(
        accountIdentifier: Long,
        key: String,
        objectString: String
    ): Maybe<GccArbitraryStorage> =
        arbitraryStoragesDao
            .getStorageSetting(accountIdentifier, key, objectString)
            .map { GccArbitraryStorageMapper.toModel(it) }

    override fun getAll(): Maybe<List<GccArbitraryStorageEntity>> = arbitraryStoragesDao.getAll()

    override fun deleteArbitraryStorage(accountIdentifier: Long): Int =
        arbitraryStoragesDao.deleteArbitraryStorage(accountIdentifier)

    override fun saveArbitraryStorage(arbitraryStorage: GccArbitraryStorage): Long =
        arbitraryStoragesDao.saveArbitraryStorage(GccArbitraryStorageMapper.toEntity(arbitraryStorage))
}
