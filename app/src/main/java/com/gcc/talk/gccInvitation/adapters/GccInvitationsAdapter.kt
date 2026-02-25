/*
 * Nextcloud Talk - Android Client
 *
 * SPDX-FileCopyrightText: 2023 Marcel Hibbe <dev@mhibbe.de>
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package com.gcc.talk.gccInvitation.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import autodagger.AutoInjector
import com.gcc.talk.R
import com.gcc.talk.gccApplication.GccTalkApplication
import com.gcc.talk.gccData.user.model.GccUser
import com.gcc.talk.databinding.RvItemInvitationBinding
import com.gcc.talk.gccInvitation.GccInvitationsActivity
import com.gcc.talk.gccInvitation.data.GccInvitation
import com.gcc.talk.gccUi.theme.ViewThemeUtils
import javax.inject.Inject

@AutoInjector(GccTalkApplication::class)
class InvitationsAdapter(
    val user: GccUser,
    private val handleInvitation: (GccInvitation, GccInvitationsActivity.InvitationAction) -> Unit
) : ListAdapter<GccInvitation, InvitationsAdapter.InvitationsViewHolder>(InvitationsCallback) {

    @Inject
    lateinit var viewThemeUtils: ViewThemeUtils

    inner class InvitationsViewHolder(private val itemBinding: RvItemInvitationBinding) :
        RecyclerView.ViewHolder(itemBinding.root) {

        private var currentInvitation: GccInvitation? = null

        fun bindItem(invitation: GccInvitation) {
            currentInvitation = invitation

            itemBinding.title.text = invitation.roomName
            itemBinding.subject.text = String.format(
                itemBinding.root.context.resources.getString(R.string.nc_federation_invited_to_room),
                invitation.inviterDisplayName,
                invitation.remoteServerUrl
            )

            itemBinding.acceptInvitation.setOnClickListener {
                currentInvitation?.let {
                    handleInvitation(it, GccInvitationsActivity.InvitationAction.ACCEPT)
                }
            }

            itemBinding.rejectInvitation.setOnClickListener {
                currentInvitation?.let {
                    handleInvitation(it, GccInvitationsActivity.InvitationAction.REJECT)
                }
            }

            viewThemeUtils.material.colorMaterialButtonPrimaryBorderless(itemBinding.rejectInvitation)
            viewThemeUtils.material.colorMaterialButtonPrimaryTonal(itemBinding.acceptInvitation)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): InvitationsViewHolder {
        GccTalkApplication.sharedApplication!!.componentApplication.inject(this)
        return InvitationsViewHolder(
            RvItemInvitationBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: InvitationsViewHolder, position: Int) {
        val invitation = getItem(position)
        holder.bindItem(invitation)
    }
}

object InvitationsCallback : DiffUtil.ItemCallback<GccInvitation>() {
    override fun areItemsTheSame(oldItem: GccInvitation, newItem: GccInvitation): Boolean = oldItem == newItem

    override fun areContentsTheSame(oldItem: GccInvitation, newItem: GccInvitation): Boolean = oldItem.id == newItem.id
}
