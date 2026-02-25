/*
 * Nextcloud Talk - Android Client
 *
 * SPDX-FileCopyrightText: 2022 Andy Scherzinger <info@andy-scherzinger.de>
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package com.gcc.talk.gccData.storage

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.gcc.talk.gccData.storage.model.GccArbitraryStorageEntity
import io.reactivex.Maybe

@Dao
abstract class GccArbitraryStoragesDao {
    @Query(
        "SELECT * FROM GccArbitraryStorage WHERE " +
            "accountIdentifier = :accountIdentifier AND " +
            "\"key\" = :key AND " +
            "object = :objectString"
    )
    abstract fun getStorageSetting(
        accountIdentifier: Long,
        key: String,
        objectString: String
    ): Maybe<GccArbitraryStorageEntity>

    @Query(
        "SELECT * FROM GccArbitraryStorage"
    )
    abstract fun getAll(): Maybe<List<GccArbitraryStorageEntity>>

    @Query("DELETE FROM GccArbitraryStorage WHERE accountIdentifier = :accountIdentifier")
    abstract fun deleteArbitraryStorage(accountIdentifier: Long): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun saveArbitraryStorage(arbitraryStorage: GccArbitraryStorageEntity): Long
}
