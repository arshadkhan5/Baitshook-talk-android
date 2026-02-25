/*
 * Nextcloud Talk - Android Client
 *
 * SPDX-FileCopyrightText: 2022 Andy Scherzinger <info@andy-scherzinger.de>
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package com.gcc.talk.gccData.source.local.converters

import androidx.room.TypeConverter
import com.bluelinelabs.logansquare.LoganSquare
import com.gcc.talk.gccModels.GccExternalSignalingServer

class GccExternalSignalingServerConverter {

    @TypeConverter
    fun fromExternalSignalingServerToString(externalSignalingServer: GccExternalSignalingServer?): String =
        if (externalSignalingServer == null) {
            ""
        } else {
            LoganSquare.serialize(externalSignalingServer)
        }

    @TypeConverter
    fun fromStringToExternalSignalingServer(value: String): GccExternalSignalingServer? {
        return if (value.isBlank()) {
            null
        } else {
            return LoganSquare.parse(value, GccExternalSignalingServer::class.java)
        }
    }
}
