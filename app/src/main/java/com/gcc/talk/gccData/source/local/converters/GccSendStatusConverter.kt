/*
 * Nextcloud Talk - Android Client
 *
 * SPDX-FileCopyrightText: 2025 Marcel Hibbe <dev@mhibbe.de>
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.gcc.talk.gccData.source.local.converters

import androidx.room.TypeConverter
import com.gcc.talk.gccData.database.model.GccSendStatus

class GccSendStatusConverter {
    @TypeConverter
    fun fromStatus(value: GccSendStatus): String = value.name

    @TypeConverter
    fun toStatus(value: String): GccSendStatus = GccSendStatus.valueOf(value)
}
