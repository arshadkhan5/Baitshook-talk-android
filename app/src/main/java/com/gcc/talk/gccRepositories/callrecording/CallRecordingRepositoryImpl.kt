/*
 * Nextcloud Talk - Android Client
 *
 * SPDX-FileCopyrightText: 2022 Marcel Hibbe <dev@mhibbe.de>
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package com.gcc.talk.gccRepositories.callrecording

import com.gcc.talk.gccApi.GccNcApi
import com.gcc.talk.gccModels.domain.GccStartCallRecordingModel
import com.gcc.talk.gccModels.domain.GccStopCallRecordingModel
import com.gcc.talk.gccModels.json.generic.GenericMeta
import io.reactivex.Observable

class CallRecordingRepositoryImpl(private val ncApi: GccNcApi) : GccCallRecordingRepository {

    override fun startRecording(
        credentials: String?,
        url: String,
        roomToken: String
    ): Observable<GccStartCallRecordingModel> =
        ncApi.startRecording(
            credentials,
            url,
            1
        ).map { mapToStartCallRecordingModel(it.ocs?.meta!!) }

    override fun stopRecording(
        credentials: String?,
        url: String,
        roomToken: String
    ): Observable<GccStopCallRecordingModel> =
        ncApi.stopRecording(
            credentials,
            url
        ).map { mapToStopCallRecordingModel(it.ocs?.meta!!) }

    private fun mapToStartCallRecordingModel(response: GenericMeta): GccStartCallRecordingModel {
        val success = response.statusCode == HTTP_OK
        return GccStartCallRecordingModel(
            success
        )
    }

    private fun mapToStopCallRecordingModel(response: GenericMeta): GccStopCallRecordingModel {
        val success = response.statusCode == HTTP_OK
        return GccStopCallRecordingModel(
            success
        )
    }

    companion object {
        private const val HTTP_OK: Int = 200
    }
}
