/*
 * Nextcloud Talk - Android Client
 *
 * SPDX-FileCopyrightText: 2022 Andy Scherzinger <info@andy-scherzinger.de>
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package com.gcc.talk.gccUtils

import com.gcc.talk.gccData.user.model.GccUser

object GccUserIdUtils {
    const val NO_ID: Long = -1

    fun getIdForUser(user: GccUser?): Long =
        if (user?.id != null) {
            user.id!!
        } else {
            NO_ID
        }
}
