/*
 * Nextcloud Talk - Android Client
 *
 * SPDX-FileCopyrightText: 2022 Marcel Hibbe <dev@mhibbe.de>
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package com.gcc.talk.gccPolls.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.gcc.talk.gccData.user.model.GccUser
import com.gcc.talk.databinding.PollResultHeaderItemBinding
import com.gcc.talk.databinding.PollResultVoterItemBinding
import com.gcc.talk.databinding.PollResultVotersOverviewItemBinding
import com.gcc.talk.gccUi.theme.ViewThemeUtils

class GccPollResultsAdapter(
    private val user: GccUser,
    private val roomToken: String,
    private val clickListener: GccPollResultItemClickListener,
    private val viewThemeUtils: ViewThemeUtils
) : RecyclerView.Adapter<GccPollResultViewHolder>() {
    internal var list: MutableList<GccPollResultItem> = ArrayList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GccPollResultViewHolder {
        var viewHolder: GccPollResultViewHolder? = null

        when (viewType) {
            GccPollResultHeaderItem.VIEW_TYPE -> {
                val itemBinding = PollResultHeaderItemBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
                viewHolder = GccPollResultHeaderViewHolder(itemBinding, viewThemeUtils)
            }
            GccPollResultVoterItem.VIEW_TYPE -> {
                val itemBinding = PollResultVoterItemBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
                viewHolder = GccPollResultVoterViewHolder(user, roomToken, itemBinding, viewThemeUtils)
            }
            GccPollResultVotersOverviewItem.VIEW_TYPE -> {
                val itemBinding = PollResultVotersOverviewItemBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
                viewHolder = GccPollResultVotersOverviewViewHolder(user, roomToken, itemBinding)
            }
        }
        return viewHolder!!
    }

    override fun onBindViewHolder(holder: GccPollResultViewHolder, position: Int) {
        when (holder.itemViewType) {
            GccPollResultHeaderItem.VIEW_TYPE -> {
                val pollResultItem = list[position]
                holder.bind(pollResultItem as GccPollResultHeaderItem, clickListener)
            }
            GccPollResultVoterItem.VIEW_TYPE -> {
                val pollResultItem = list[position]
                holder.bind(pollResultItem as GccPollResultVoterItem, clickListener)
            }
            GccPollResultVotersOverviewItem.VIEW_TYPE -> {
                val pollResultItem = list[position]
                holder.bind(pollResultItem as GccPollResultVotersOverviewItem, clickListener)
            }
        }
    }

    override fun getItemCount(): Int = list.size

    override fun getItemViewType(position: Int): Int = list[position].getViewType()
}
