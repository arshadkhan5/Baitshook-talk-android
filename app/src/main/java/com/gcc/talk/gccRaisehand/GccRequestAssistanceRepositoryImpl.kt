/*
 * Nextcloud Talk - Android Client
 *
 * SPDX-FileCopyrightText: 2023 Marcel Hibbe <dev@mhibbe.de>
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package com.gcc.talk.gccRaisehand

import com.gcc.talk.gccApi.GccNcApi
import com.gcc.talk.gccModels.json.generic.GenericMeta
import io.reactivex.Observable

class GccRequestAssistanceRepositoryImpl(private val ncApi: GccNcApi) : GccRequestAssistanceRepository {

    override fun requestAssistance(
        credentials: String,
        url: String,
        roomToken: String
    ): Observable<GccRequestAssistanceModel> =
        ncApi.requestAssistance(
            credentials,
            url
        ).map { mapToRequestAssistanceModel(it.ocs?.meta!!) }

    override fun withdrawRequestAssistance(
        credentials: String,
        url: String,
        roomToken: String
    ): Observable<GccWithdrawRequestAssistanceModel> =
        ncApi.withdrawRequestAssistance(
            credentials,
            url
        ).map { mapToWithdrawRequestAssistanceModel(it.ocs?.meta!!) }

    private fun mapToRequestAssistanceModel(response: GenericMeta): GccRequestAssistanceModel {
        val success = response.statusCode == HTTP_OK
        return GccRequestAssistanceModel(
            success
        )
    }

    private fun mapToWithdrawRequestAssistanceModel(response: GenericMeta): GccWithdrawRequestAssistanceModel {
        val success = response.statusCode == HTTP_OK
        return GccWithdrawRequestAssistanceModel(
            success
        )
    }

    companion object {
        private const val HTTP_OK: Int = 200
    }
}
