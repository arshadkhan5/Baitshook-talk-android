/*
 * Nextcloud Talk - Android Client
 *
 * SPDX-FileCopyrightText: 2023-2024 Julius Linus <juliuslinus1@gmail.com>
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package com.gcc.talk.gccTranslate.repositories

import com.gcc.talk.gccTranslate.repositories.model.GccLanguage
import io.reactivex.Observable

interface GccTranslateRepository {

    fun translateMessage(
        authorization: String,
        url: String,
        text: String,
        toLanguage: String,
        fromLanguage: String?
    ): Observable<String>

    fun getLanguages(authorization: String, url: String): Observable<List<GccLanguage>>
}
