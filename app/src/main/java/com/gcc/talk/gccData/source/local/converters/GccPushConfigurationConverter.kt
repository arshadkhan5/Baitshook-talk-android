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
import com.gcc.talk.gccModels.json.push.PushConfigurationState

class GccPushConfigurationConverter {

    @TypeConverter
    fun fromPushConfigurationToString(pushConfiguration: PushConfigurationState?): String =
        if (pushConfiguration == null) {
            ""
        } else {
            LoganSquare.serialize(pushConfiguration)
        }

    @TypeConverter
    fun fromStringToPushConfiguration(value: String?): PushConfigurationState? {
        return if (value.isNullOrBlank()) {
            null
        } else {
            return LoganSquare.parse(value, PushConfigurationState::class.java)
        }
    }
}
