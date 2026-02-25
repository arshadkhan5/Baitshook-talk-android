/*
 * Nextcloud Talk - Android Client
 *
 * SPDX-FileCopyrightText: 2023 Marcel Hibbe <dev@mhibbe.de>
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package com.gcc.talk.gccInvitation.data

import com.gcc.talk.gccData.user.model.GccUser
import io.reactivex.Observable

interface GccInvitationsRepository {
    fun fetchInvitations(user: GccUser): Observable<GccInvitationsModel>
    fun acceptInvitation(user: GccUser, invitation: GccInvitation): Observable<InvitationActionModel>
    fun rejectInvitation(user: GccUser, invitation: GccInvitation): Observable<InvitationActionModel>
    suspend fun getInvitations(user: GccUser): GccInvitationsModel
}
