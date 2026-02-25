/*
 * Nextcloud Talk - Android Client
 *
 * SPDX-FileCopyrightText: 2024 Marcel Hibbe <dev@mhibbe.de>
 * SPDX-FileCopyrightText: 2017-2020 Mario Danic <mario@lovelyhq.com>
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package com.gcc.talk.gccData.source.local.converters

import androidx.room.TypeConverter
import com.bluelinelabs.logansquare.LoganSquare
import com.gcc.talk.gccModels.json.capabilities.ServerVersion

class GccServerVersionConverter {
    @TypeConverter
    fun fromServerVersionToString(serverVersion: ServerVersion?): String =
        if (serverVersion == null) {
            ""
        } else {
            LoganSquare.serialize(serverVersion)
        }

    @TypeConverter
    fun fromStringToServerVersion(value: String): ServerVersion? {
        return if (value.isBlank()) {
            null
        } else {
            return LoganSquare.parse(value, ServerVersion::class.java)
        }
    }
}
