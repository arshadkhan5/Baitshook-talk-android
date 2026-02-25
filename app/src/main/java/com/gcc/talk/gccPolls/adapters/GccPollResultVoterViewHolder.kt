/*
 * Nextcloud Talk - Android Client
 *
 * SPDX-FileCopyrightText: 2022 Marcel Hibbe <dev@mhibbe.de>
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package com.gcc.talk.gccPolls.adapters

import android.annotation.SuppressLint
import android.text.TextUtils
import android.widget.ImageView
import com.gcc.talk.R
import com.gcc.talk.gccApplication.GccTalkApplication
import com.gcc.talk.gccData.user.model.GccUser
import com.gcc.talk.databinding.PollResultVoterItemBinding
import com.gcc.talk.gccExtensions.loadFederatedUserAvatar
import com.gcc.talk.gccExtensions.loadGuestAvatar
import com.gcc.talk.gccExtensions.loadUserAvatar
import com.gcc.talk.gccModels.json.participants.Participant
import com.gcc.talk.gccPolls.model.GccPollDetails
import com.gcc.talk.gccUi.theme.ViewThemeUtils
import com.gcc.talk.gccUtils.GccDisplayUtils

class GccPollResultVoterViewHolder(
    private val user: GccUser,
    private val roomToken: String,
    override val binding: PollResultVoterItemBinding,
    private val viewThemeUtils: ViewThemeUtils
) : GccPollResultViewHolder(binding) {

    @SuppressLint("SetTextI18n")
    override fun bind(pollResultItem: GccPollResultItem, clickListener: GccPollResultItemClickListener) {
        val item = pollResultItem as GccPollResultVoterItem

        binding.root.setOnClickListener { clickListener.onClick() }

        binding.pollVoterName.text = item.details.actorDisplayName
        loadAvatar(item.details, binding.pollVoterAvatar)
        viewThemeUtils.dialog.colorDialogSupportingText(binding.pollVoterName)
    }

    private fun loadAvatar(pollDetail: GccPollDetails, avatar: ImageView) {
        when (pollDetail.actorType) {
            Participant.ActorType.GUESTS -> {
                var displayName = GccTalkApplication.sharedApplication?.resources?.getString(R.string.nc_guest)
                if (!TextUtils.isEmpty(pollDetail.actorDisplayName)) {
                    displayName = pollDetail.actorDisplayName!!
                }
                avatar.loadGuestAvatar(user, displayName!!, false)
            }

            Participant.ActorType.FEDERATED -> {
                val darkTheme = if (GccDisplayUtils.isDarkModeOn(binding.root.context)) 1 else 0
                avatar.loadFederatedUserAvatar(
                    user,
                    user.baseUrl!!,
                    roomToken,
                    pollDetail.actorId!!,
                    darkTheme,
                    false,
                    false
                )
            }

            else -> {
                avatar.loadUserAvatar(user, pollDetail.actorId!!, false, false)
            }
        }
    }
}
