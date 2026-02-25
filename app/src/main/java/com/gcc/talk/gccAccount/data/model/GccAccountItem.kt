/*
 * Nextcloud Talk - Android Client
 *
 * SPDX-FileCopyrightText: 2025 Sowjanya Kota <sowjanya.kch@gmail.com>
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.gcc.talk.gccAccount.data.model

import com.gcc.talk.gccData.user.model.GccUser

data class GccAccountItem(val user: GccUser, val pendingInvitation: Int)
