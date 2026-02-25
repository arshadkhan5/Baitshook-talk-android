/*
 * Nextcloud Talk - Android Client
 *
 * SPDX-FileCopyrightText: 2022 Marcel Hibbe <dev@mhibbe.de>
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package com.gcc.talk.gccAdapters.messages

import com.gcc.talk.gccUi.bottom.sheet.ProfileBottomSheet

data class GccMessagePayload(
    var roomToken: String,
    val isOwnerOrModerator: Boolean?,
    val profileBottomSheet: ProfileBottomSheet
)
