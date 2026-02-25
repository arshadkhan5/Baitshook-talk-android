/*
 * Nextcloud Talk - Android Client
 *
 * SPDX-FileCopyrightText: 2024 Julius Linus <juliuslinus1@gmail.com>
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.gcc.talk.gccData.source.local.converters

import android.util.Log
import androidx.room.TypeConverter
import com.bluelinelabs.logansquare.LoganSquare

class GccArrayListConverter {

    @Suppress("Detekt.TooGenericExceptionCaught")
    @TypeConverter
    fun arrayListToString(list: ArrayList<String>?): String? {
        return if (list == null) {
            null
        } else {
            return try {
                LoganSquare.serialize(list)
            } catch (e: Exception) {
                Log.e("GccArrayListConverter", "Error parsing array list $list to String $e")
                ""
            }
        }
    }

    @TypeConverter
    fun stringToArrayList(value: String?): ArrayList<String>? {
        if (value.isNullOrEmpty()) {
            return null
        }

        return LoganSquare.parseList(value, String::class.java) as ArrayList<String>?
    }
}
