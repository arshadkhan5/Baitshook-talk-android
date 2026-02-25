/*
 * Nextcloud Talk - Android Client
 *
 * SPDX-FileCopyrightText: 2022 Andy Scherzinger <info@andy-scherzinger.de>
 * SPDX-FileCopyrightText: 2017 Mario Danic <mario@lovelyhq.com>
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package com.gcc.talk.gccModels

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class GccRetrofitBucket(var url: String? = null, var queryMap: MutableMap<String, String>? = null) : Parcelable
