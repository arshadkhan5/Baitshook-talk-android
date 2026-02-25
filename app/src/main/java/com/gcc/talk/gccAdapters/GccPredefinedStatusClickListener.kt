/*
 * Nextcloud Talk - Android Client
 *
 * SPDX-FileCopyrightText: 2022 Tim Krüger <t@timkrueger.me
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package com.gcc.talk.gccAdapters

import com.gcc.talk.gccModels.json.status.predefined.PredefinedStatus

interface GccPredefinedStatusClickListener {
    fun onClick(predefinedStatus: PredefinedStatus)
    fun revertStatus()
}
