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
import com.gcc.talk.databinding.PollCreateOptionsItemBinding
import com.gcc.talk.gccUi.theme.ViewThemeUtils

class GccPollCreateOptionsAdapter(
    private val clickListener: GccPollCreateOptionsItemListener,
    private val viewThemeUtils: ViewThemeUtils
) : RecyclerView.Adapter<GccPollCreateOptionViewHolder>() {

    internal var list: ArrayList<GccPollCreateOptionItem> = ArrayList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GccPollCreateOptionViewHolder {
        val itemBinding = PollCreateOptionsItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)

        return GccPollCreateOptionViewHolder(itemBinding, viewThemeUtils)
    }

    override fun onBindViewHolder(holder: GccPollCreateOptionViewHolder, position: Int) {
        val currentItem = list[position]
        var focus = false

        if (list.size - 1 == position && currentItem.pollOption.isBlank()) {
            focus = true
        }

        holder.bind(currentItem, clickListener, position, focus)
    }

    override fun getItemCount(): Int = list.size

    fun updateOptionsList(optionsList: ArrayList<GccPollCreateOptionItem>) {
        list = optionsList
        notifyDataSetChanged()
    }
}
