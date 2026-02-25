/*
 * Nextcloud Talk - Android Client
 *
 * SPDX-FileCopyrightText: 2022 Nextcloud GmbH
 * SPDX-FileCopyrightText: 2022 Álvaro Brey <alvaro@alvarobrey.com>
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package com.gcc.talk.test.fakes

import com.gcc.talk.gccModels.domain.GccStartCallRecordingModel
import com.gcc.talk.gccModels.domain.GccStopCallRecordingModel
import com.gcc.talk.gccRepositories.callrecording.GccCallRecordingRepository
import io.reactivex.Observable

class FakeCallRecordingRepository : GccCallRecordingRepository {

    override fun startRecording(credentials: String?, url: String, roomToken: String) =
        Observable.just(GccStartCallRecordingModel(true))

    override fun stopRecording(credentials: String?, url: String, roomToken: String) =
        Observable.just(GccStopCallRecordingModel(true))
}
