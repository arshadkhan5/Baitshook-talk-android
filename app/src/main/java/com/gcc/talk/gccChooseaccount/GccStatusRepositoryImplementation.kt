/*
 * Nextcloud Talk - Android Client
 *
 * SPDX-FileCopyrightText: 2025 Sowjanya Kota <sowjanya.kch@gmail.com>
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.gcc.talk.gccChooseaccount

import com.gcc.talk.gccApi.GccNcApiCoroutines
import com.gcc.talk.gccModels.json.status.StatusOverall
import javax.inject.Inject

class GccStatusRepositoryImplementation @Inject constructor(private val ncApiCoroutines: GccNcApiCoroutines) :
    GccStatusRepository {

    override suspend fun setStatus(credentials: String, url: String): StatusOverall {
        val statusOverall = ncApiCoroutines.status(credentials, url)
        return statusOverall
    }
}
