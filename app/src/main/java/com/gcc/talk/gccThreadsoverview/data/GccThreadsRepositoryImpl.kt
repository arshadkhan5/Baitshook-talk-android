/*
 * Nextcloud Talk - Android Client
 *
 * SPDX-FileCopyrightText: 2025 Marcel Hibbe <dev@mhibbe.de>
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.gcc.talk.gccThreadsoverview.data

import com.gcc.talk.gccApi.GccNcApiCoroutines
import com.gcc.talk.gccModels.json.threads.ThreadOverall
import com.gcc.talk.gccModels.json.threads.ThreadsOverall
import javax.inject.Inject

class GccThreadsRepositoryImpl @Inject constructor(private val ncApiCoroutines: GccNcApiCoroutines) : GccThreadsRepository {

    override suspend fun getThreads(credentials: String, url: String, limit: Int?): ThreadsOverall =
        ncApiCoroutines.getThreads(credentials, url, limit)

    override suspend fun getThread(credentials: String, url: String): ThreadOverall =
        ncApiCoroutines.getThread(credentials, url)

    override suspend fun setThreadNotificationLevel(credentials: String, url: String, level: Int): ThreadOverall =
        ncApiCoroutines.setThreadNotificationLevel(credentials, url, level)

    companion object {
        val TAG = GccThreadsRepositoryImpl::class.simpleName
    }
}
