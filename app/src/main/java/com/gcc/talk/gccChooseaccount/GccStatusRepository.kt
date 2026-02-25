/*
 * Nextcloud Talk - Android Client
 *
 * SPDX-FileCopyrightText: 2025 Sowjanya Kota <sowjanya.kch@gmail.com>
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.gcc.talk.gccChooseaccount

import com.gcc.talk.gccModels.json.status.StatusOverall

interface GccStatusRepository {
    suspend fun setStatus(credentials: String, url: String): StatusOverall
}
