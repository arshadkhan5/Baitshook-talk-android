/*
 * Nextcloud Talk - Android Client
 *
 * SPDX-FileCopyrightText: 2022 Marcel Hibbe <dev@mhibbe.de>
 * SPDX-FileCopyrightText: 2022 Álvaro Brey <alvaro@alvarobrey.com>
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package com.gcc.talk.gccPolls.repositories

import com.gcc.talk.gccPolls.model.GccPoll
import io.reactivex.Observable

interface GccPollRepository {

    fun createPoll(
        credentials: String?,
        url: String,
        roomToken: String,
        question: String,
        options: List<String>,
        resultMode: Int,
        maxVotes: Int
    ): Observable<GccPoll>

    fun getPoll(credentials: String?, url: String, roomToken: String, pollId: String): Observable<GccPoll>

    fun vote(credentials: String?, url: String, roomToken: String, pollId: String, options: List<Int>): Observable<GccPoll>

    fun closePoll(credentials: String?, url: String, roomToken: String, pollId: String): Observable<GccPoll>
}
