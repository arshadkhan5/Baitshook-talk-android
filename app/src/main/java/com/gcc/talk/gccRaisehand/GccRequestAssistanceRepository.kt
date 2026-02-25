/*
 * Nextcloud Talk - Android Client
 *
 * SPDX-FileCopyrightText: 2023 Marcel Hibbe <dev@mhibbe.de>
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package com.gcc.talk.gccRaisehand

import io.reactivex.Observable

interface GccRequestAssistanceRepository {

    fun requestAssistance(credentials: String, url: String, roomToken: String): Observable<GccRequestAssistanceModel>

    fun withdrawRequestAssistance(
        credentials: String,
        url: String,
        roomToken: String
    ): Observable<GccWithdrawRequestAssistanceModel>
}
