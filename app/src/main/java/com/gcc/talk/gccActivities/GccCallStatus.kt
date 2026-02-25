/*
 * Nextcloud Talk - Android Client
 *
 * SPDX-FileCopyrightText: 2022 Andy Scherzinger <info@andy-scherzinger.de>
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package com.gcc.talk.gccActivities

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
enum class GccCallStatus : Parcelable {
    CONNECTING,
    CALLING_TIMEOUT,
    JOINED,
    IN_CONVERSATION,
    RECONNECTING,
    OFFLINE,
    LEAVING,
    PUBLISHER_FAILED
}
