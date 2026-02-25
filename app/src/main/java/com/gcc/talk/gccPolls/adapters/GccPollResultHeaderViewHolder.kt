/*
 * Nextcloud Talk - Android Client
 *
 * SPDX-FileCopyrightText: 2022 Marcel Hibbe <dev@mhibbe.de>
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package com.gcc.talk.gccPolls.adapters

import android.annotation.SuppressLint
import android.graphics.Typeface
import com.gcc.talk.databinding.PollResultHeaderItemBinding
import com.gcc.talk.gccUi.theme.ViewThemeUtils

class GccPollResultHeaderViewHolder(
    override val binding: PollResultHeaderItemBinding,
    private val viewThemeUtils: ViewThemeUtils
) : GccPollResultViewHolder(binding) {

    @SuppressLint("SetTextI18n")
    override fun bind(pollResultItem: GccPollResultItem, clickListener: GccPollResultItemClickListener) {
        val item = pollResultItem as GccPollResultHeaderItem

        viewThemeUtils.material.colorProgressBar(binding.pollOptionBar)

        binding.root.setOnClickListener { clickListener.onClick() }

        binding.pollOptionText.text = item.name
        binding.pollOptionPercentText.text = "${item.percent}%"

        viewThemeUtils.dialog.colorDialogSupportingText(binding.pollOptionText)
        viewThemeUtils.dialog.colorDialogSupportingText(binding.pollOptionPercentText)

        if (item.selfVoted) {
            binding.pollOptionText.setTypeface(null, Typeface.BOLD)
            binding.pollOptionPercentText.setTypeface(null, Typeface.BOLD)
        }

        binding.pollOptionBar.progress = item.percent
    }
}
