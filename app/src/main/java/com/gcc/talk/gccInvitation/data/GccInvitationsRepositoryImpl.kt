/*
 * Nextcloud Talk - Android Client
 *
 * SPDX-FileCopyrightText: 2023 Marcel Hibbe <dev@mhibbe.de>
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package com.gcc.talk.gccInvitation.data

import com.gcc.talk.gccApi.GccNcApi
import com.gcc.talk.gccApi.GccNcApiCoroutines
import com.gcc.talk.gccData.user.model.GccUser
import com.gcc.talk.gccModels.json.invitation.Invitation
import com.gcc.talk.gccUtils.GccApiUtils
import io.reactivex.Observable

class GccInvitationsRepositoryImpl(private val ncApi: GccNcApi, private val ncApiCoroutines: GccNcApiCoroutines) :
    GccInvitationsRepository {

    override fun fetchInvitations(user: GccUser): Observable<GccInvitationsModel> {
        val credentials: String = GccApiUtils.getCredentials(user.username, user.token)!!

        return ncApi.getInvitations(
            credentials,
            GccApiUtils.getUrlForInvitation(user.baseUrl!!)
        ).map { mapToInvitationsModel(user, it.ocs?.data!!) }
    }

    override fun acceptInvitation(user: GccUser, invitation: GccInvitation): Observable<InvitationActionModel> {
        val credentials: String = GccApiUtils.getCredentials(user.username, user.token)!!

        return ncApi.acceptInvitation(
            credentials,
            GccApiUtils.getUrlForInvitationAccept(user.baseUrl!!, invitation.id)
        ).map { InvitationActionModel(ActionEnum.ACCEPT, it.ocs?.meta?.statusCode!!, invitation) }
    }

    override fun rejectInvitation(user: GccUser, invitation: GccInvitation): Observable<InvitationActionModel> {
        val credentials: String = GccApiUtils.getCredentials(user.username, user.token)!!

        return ncApi.rejectInvitation(
            credentials,
            GccApiUtils.getUrlForInvitationReject(user.baseUrl!!, invitation.id)
        ).map { InvitationActionModel(ActionEnum.REJECT, it.ocs?.meta?.statusCode!!, invitation) }
    }

    override suspend fun getInvitations(user: GccUser): GccInvitationsModel {
        val credentials: String = GccApiUtils.getCredentials(user.username, user.token)!!

        val invitationsOverall = ncApiCoroutines.getInvitations(
            credentials,
            GccApiUtils.getUrlForInvitation(user.baseUrl!!)
        )
        return mapToInvitationsModel(user, invitationsOverall.ocs?.data!!)
    }

    private fun mapToInvitationsModel(
        user: GccUser,
        invitations: List<Invitation>
    ): GccInvitationsModel {
        val filteredInvitations = invitations.filter { it.state == OPEN_PENDING_INVITATION }

        return GccInvitationsModel(
            user,
            filteredInvitations.map { invitation ->
                GccInvitation(
                    invitation.id,
                    invitation.state,
                    invitation.localCloudId!!,
                    invitation.localToken!!,
                    invitation.remoteAttendeeId,
                    invitation.remoteServerUrl!!,
                    invitation.remoteToken!!,
                    invitation.roomName!!,
                    invitation.userId!!,
                    invitation.inviterCloudId!!,
                    invitation.inviterDisplayName!!
                )
            }
        )
    }

    companion object {
        private const val OPEN_PENDING_INVITATION = 0
    }
}
