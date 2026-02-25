/*
 * Nextcloud Talk - Android Client
 *
 * SPDX-FileCopyrightText: 2022 Álvaro Brey <alvaro@alvarobrey.com>
 * SPDX-FileCopyrightText: 2022 Nextcloud GmbH
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package com.gcc.talk.gccUtils.database.user

import com.gcc.talk.gccData.user.model.GccUser
import io.reactivex.Maybe

@Deprecated("Use GccCurrentUserProvider instead")
interface GccCurrentUserProviderOld {
    val currentUser: Maybe<GccUser>
}
