/*
 * Nextcloud Talk - Android Client
 *
 * SPDX-FileCopyrightText: 2022 Andy Scherzinger <info@andy-scherzinger.de>
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package com.gcc.talk.gccAdapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.gcc.talk.gccData.user.model.GccUser
import com.gcc.talk.databinding.ReactionItemBinding

class GccReactionsAdapter(private val clickListener: GccReactionItemClickListener, private val user: GccUser?) :
    RecyclerView.Adapter<GccReactionsViewHolder>() {
    internal var list: MutableList<GccReactionItem> = ArrayList<GccReactionItem>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GccReactionsViewHolder {
        val itemBinding = ReactionItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return GccReactionsViewHolder(itemBinding, user)
    }

    override fun onBindViewHolder(holder: GccReactionsViewHolder, position: Int) {
        holder.bind(list[position], clickListener)
    }

    override fun getItemCount(): Int = list.size
}
