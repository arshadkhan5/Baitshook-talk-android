/*
 * Nextcloud Talk - Android Client
 *
 * SPDX-FileCopyrightText: 2025 Marcel Hibbe <dev@mhibbe.de>
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.gcc.talk.gccThreadsoverview.data

import com.gcc.talk.gccModels.json.threads.ThreadOverall
import com.gcc.talk.gccModels.json.threads.ThreadsOverall

interface GccThreadsRepository {

    suspend fun getThreads(credentials: String, url: String, limit: Int?): ThreadsOverall

    suspend fun getThread(credentials: String, url: String): ThreadOverall

    suspend fun setThreadNotificationLevel(credentials: String, url: String, level: Int): ThreadOverall
}
