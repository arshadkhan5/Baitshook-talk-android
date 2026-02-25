/*
 * Nextcloud Talk - Android Client
 *
 * SPDX-FileCopyrightText: 2022 Andy Scherzinger <info@andy-scherzinger.de>
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package com.gcc.talk.gccData.storage.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class GccArbitraryStorage(
    var accountIdentifier: Long,
    var key: String,
    var storageObject: String? = null,
    var value: String? = null
) : Parcelable
