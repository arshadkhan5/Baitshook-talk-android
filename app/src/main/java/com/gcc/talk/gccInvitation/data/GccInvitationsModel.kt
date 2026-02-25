/*
 * Nextcloud Talk - Android Client
 *
 * SPDX-FileCopyrightText: 2023 Marcel Hibbe <dev@mhibbe.de>
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package com.gcc.talk.gccInvitation.data

import com.gcc.talk.gccData.user.model.GccUser

data class GccInvitationsModel(var user: GccUser, var invitations: List<GccInvitation>)
