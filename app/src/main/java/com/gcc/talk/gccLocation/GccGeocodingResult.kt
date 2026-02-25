/*
 * Nextcloud Talk - Android Client
 *
 * SPDX-FileCopyrightText: 2023 Marcel Hibbe <dev@mhibbe.de>
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package com.gcc.talk.gccLocation;

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class GccGeocodingResult(val lat: Double, val lon: Double, var displayName: String) : Parcelable
