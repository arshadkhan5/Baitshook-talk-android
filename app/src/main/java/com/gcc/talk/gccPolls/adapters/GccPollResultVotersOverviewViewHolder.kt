/*
 * Nextcloud Talk - Android Client
 *
 * SPDX-FileCopyrightText: 2022 Tim Krüger <t@timkrueger.me>
 * SPDX-FileCopyrightText: 2022 Marcel Hibbe <dev@mhibbe.de>
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package com.gcc.talk.gccPolls.adapters

import android.annotation.SuppressLint
import android.text.TextUtils
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.gcc.talk.R
import com.gcc.talk.gccApplication.GccTalkApplication
import com.gcc.talk.gccData.user.model.GccUser
import com.gcc.talk.databinding.PollResultVotersOverviewItemBinding
import com.gcc.talk.gccExtensions.loadFederatedUserAvatar
import com.gcc.talk.gccExtensions.loadGuestAvatar
import com.gcc.talk.gccExtensions.loadUserAvatar
import com.gcc.talk.gccModels.json.participants.Participant
import com.gcc.talk.gccPolls.model.GccPollDetails
import com.gcc.talk.gccUtils.GccDisplayUtils

class GccPollResultVotersOverviewViewHolder(
    private val user: GccUser,
    private val roomToken: String,
    override val binding: PollResultVotersOverviewItemBinding
) : GccPollResultViewHolder(binding) {

    @SuppressLint("SetTextI18n")
    override fun bind(pollResultItem: GccPollResultItem, clickListener: GccPollResultItemClickListener) {
        val item = pollResultItem as GccPollResultVotersOverviewItem

        binding.root.setOnClickListener { clickListener.onClick() }

        val layoutParams = LinearLayout.LayoutParams(
            AVATAR_SIZE,
            AVATAR_SIZE
        )

        var avatarsToDisplay = MAX_AVATARS
        if (item.detailsList.size < avatarsToDisplay) {
            avatarsToDisplay = item.detailsList.size
        }
        val shotsDots = item.detailsList.size > avatarsToDisplay

        for (i in 0 until avatarsToDisplay) {
            val pollDetails = item.detailsList[i]
            val avatar = ImageView(binding.root.context)

            layoutParams.marginStart = i * AVATAR_OFFSET
            avatar.layoutParams = layoutParams

            avatar.translationZ = i.toFloat() * -1

            loadAvatar(pollDetails, avatar)

            binding.votersAvatarsOverviewWrapper.addView(avatar)

            if (i == avatarsToDisplay - 1 && shotsDots) {
                val dotsView = TextView(itemView.context)
                layoutParams.marginStart = i * AVATAR_OFFSET + DOTS_OFFSET
                dotsView.layoutParams = layoutParams
                dotsView.text = DOTS_TEXT
                binding.votersAvatarsOverviewWrapper.addView(dotsView)
            }
        }
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

    companion object {
        const val AVATAR_SIZE = 60
        const val MAX_AVATARS = 10
        const val AVATAR_OFFSET = AVATAR_SIZE - 20
        const val DOTS_OFFSET = 70
        const val DOTS_TEXT = "…"
    }
}
