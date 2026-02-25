/*
 * Nextcloud Talk - Android Client
 *
 * SPDX-FileCopyrightText: 2022 Marcel Hibbe <dev@mhibbe.de>
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package com.gcc.talk.gccPolls.model

import com.gcc.talk.gccModels.json.participants.Participant

data class GccPollDetails(
    val actorType: Participant.ActorType?,
    val actorId: String?,
    val actorDisplayName: String?,
    val optionId: Int
)
