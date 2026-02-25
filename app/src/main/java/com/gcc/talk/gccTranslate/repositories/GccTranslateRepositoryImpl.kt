/*
 * Nextcloud Talk - Android Client
 *
 * SPDX-FileCopyrightText: 2023 Julius Linus1 <juliuslinus1@gmail.com>
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package com.gcc.talk.gccTranslate.repositories

import com.gcc.talk.gccApi.GccNcApi
import com.gcc.talk.gccTranslate.repositories.model.GccLanguage
import io.reactivex.Observable
import javax.inject.Inject

class GccTranslateRepositoryImpl @Inject constructor(private val ncApi: GccNcApi) : GccTranslateRepository {

    override fun translateMessage(
        authorization: String,
        url: String,
        text: String,
        toLanguage: String,
        fromLanguage: String?
    ): Observable<String> =
        ncApi.translateMessage(authorization, url, text, toLanguage, fromLanguage).map {
            it.ocs?.data!!.text
        }

    override fun getLanguages(authorization: String, url: String): Observable<List<GccLanguage>> =
        ncApi.getLanguages(authorization, url).map {
            it.ocs?.data?.languages
        }
}
