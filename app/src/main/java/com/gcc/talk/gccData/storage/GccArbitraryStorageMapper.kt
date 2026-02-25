/*
 * Nextcloud Talk - Android Client
 *
 * SPDX-FileCopyrightText: 2022 Andy Scherzinger <info@andy-scherzinger.de>
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package com.gcc.talk.gccData.storage

import com.gcc.talk.gccData.storage.model.GccArbitraryStorage
import com.gcc.talk.gccData.storage.model.GccArbitraryStorageEntity

object GccArbitraryStorageMapper {
    fun toModel(entity: GccArbitraryStorageEntity?): GccArbitraryStorage? =
        entity?.let {
            GccArbitraryStorage(
                it.accountIdentifier,
                it.key,
                it.storageObject,
                it.value
            )
        }

    fun toEntity(model: GccArbitraryStorage): GccArbitraryStorageEntity =
        GccArbitraryStorageEntity(
            accountIdentifier = model.accountIdentifier,
            key = model.key,
            storageObject = model.storageObject,
            value = model.value
        )
}
