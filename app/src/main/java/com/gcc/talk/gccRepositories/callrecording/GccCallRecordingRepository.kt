/*
 * Nextcloud Talk - Android Client
 *
 * SPDX-FileCopyrightText: 2022 Marcel Hibbe <dev@mhibbe.de>
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package com.gcc.talk.gccRepositories.callrecording

import com.gcc.talk.gccModels.domain.GccStartCallRecordingModel
import com.gcc.talk.gccModels.domain.GccStopCallRecordingModel
import io.reactivex.Observable

interface GccCallRecordingRepository {

    fun startRecording(credentials: String?, url: String, roomToken: String): Observable<GccStartCallRecordingModel>

    fun stopRecording(credentials: String?, url: String, roomToken: String): Observable<GccStopCallRecordingModel>
}
