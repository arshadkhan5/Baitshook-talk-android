/*
 * Nextcloud Talk - Android Client
 *
 * SPDX-FileCopyrightText: 2022 Álvaro Brey <alvaro@alvarobrey.com>
 * SPDX-FileCopyrightText: 2022 Nextcloud GmbH
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package com.gcc.talk.gccUtils.database.user

import com.gcc.talk.gccData.user.model.GccUser

interface GccCurrentUserProvider {
    suspend fun getCurrentUser(timeout: Long = 5000L): Result<GccUser>
}
