/*
 * Nextcloud Talk - Android Client
 *
 * SPDX-FileCopyrightText: 2022 Andy Scherzinger <info@andy-scherzinger.de>
 * SPDX-FileCopyrightText: 2017-2020 Mario Danic <mario@lovelyhq.com>
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package com.gcc.talk.gccData.source.local.converters

import androidx.room.TypeConverter
import com.bluelinelabs.logansquare.LoganSquare
import com.gcc.talk.gccModels.json.capabilities.Capabilities

class GccCapabilitiesConverter {
    @TypeConverter
    fun fromCapabilitiesToString(capabilities: Capabilities?): String =
        if (capabilities == null) {
            ""
        } else {
            LoganSquare.serialize(capabilities)
        }

    @TypeConverter
    fun fromStringToCapabilities(value: String): Capabilities? {
        return if (value.isBlank()) {
            null
        } else {
            return LoganSquare.parse(value, Capabilities::class.java)
        }
    }
}
