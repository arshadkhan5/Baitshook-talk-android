/*
 * Nextcloud Talk - Android Client
 *
 * SPDX-FileCopyrightText: 2022 Andy Scherzinger <info@andy-scherzinger.de>
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package com.gcc.talk.gccAdapters

import android.text.TextUtils
import androidx.recyclerview.widget.RecyclerView
import com.gcc.talk.R
import com.gcc.talk.gccApplication.GccTalkApplication.Companion.sharedApplication
import com.gcc.talk.gccData.user.model.GccUser
import com.gcc.talk.databinding.ReactionItemBinding
import com.gcc.talk.gccExtensions.loadGuestAvatar
import com.gcc.talk.gccExtensions.loadUserAvatar
import com.gcc.talk.gccModels.json.reactions.ReactionVoter

class GccReactionsViewHolder(private val binding: ReactionItemBinding, private val user: GccUser?) :
    RecyclerView.ViewHolder(binding.root) {

    fun bind(reactionItem: GccReactionItem, clickListener: GccReactionItemClickListener) {
        binding.root.setOnClickListener { clickListener.onClick(reactionItem) }
        binding.reaction.text = reactionItem.reaction
        binding.name.text = reactionItem.reactionVoter.actorDisplayName

        if (user != null && user.baseUrl?.isNotEmpty() == true) {
            loadAvatar(reactionItem)
        }
    }

    private fun loadAvatar(reactionItem: GccReactionItem) {
        if (reactionItem.reactionVoter.actorType == ReactionVoter.ReactionActorType.GUESTS) {
            var displayName = sharedApplication?.resources?.getString(R.string.nc_guest)
            if (!TextUtils.isEmpty(reactionItem.reactionVoter.actorDisplayName)) {
                displayName = reactionItem.reactionVoter.actorDisplayName!!
            }
            binding.avatar.loadGuestAvatar(user!!.baseUrl!!, displayName!!, false)
        } else if (reactionItem.reactionVoter.actorType == ReactionVoter.ReactionActorType.USERS) {
            binding.avatar.loadUserAvatar(
                user!!,
                reactionItem.reactionVoter.actorId!!,
                false,
                false
            )
        }
    }
}
