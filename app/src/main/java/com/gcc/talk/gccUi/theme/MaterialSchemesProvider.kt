/*
 * Nextcloud Talk - Android Client
 *
 * SPDX-FileCopyrightText: 2022 Álvaro Brey <alvaro@alvarobrey.com>
 * SPDX-FileCopyrightText: 2022 Nextcloud GmbH
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package com.gcc.talk.gccUi.theme

import com.nextcloud.android.common.ui.theme.MaterialSchemes
import com.gcc.talk.gccData.user.model.GccUser
import com.gcc.talk.gccModels.json.capabilities.Capabilities

interface MaterialSchemesProvider {
    fun getMaterialSchemesForUser(user: GccUser?): MaterialSchemes
    fun getMaterialSchemesForCapabilities(capabilities: Capabilities?): MaterialSchemes
    fun getMaterialSchemesForCurrentUser(): MaterialSchemes
}
