/*
 * Nextcloud Talk - Android Client
 *
 * SPDX-FileCopyrightText: 2020 Tobias Kaminsky <tobias@kaminsky.me>
 * SPDX-FileCopyrightText: 2020 Nextcloud GmbH
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package com.gcc.talk.gccAdapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.gcc.talk.databinding.PredefinedStatusBinding
import com.gcc.talk.gccModels.json.status.predefined.PredefinedStatus

class GccPredefinedStatusListAdapter(
    private val clickListener: GccPredefinedStatusClickListener,
    val context: Context,
    var isBackupStatusAvailable: Boolean
) : RecyclerView.Adapter<PredefinedStatusViewHolder>() {
    internal var list: List<PredefinedStatus> = emptyList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PredefinedStatusViewHolder {
        val itemBinding = PredefinedStatusBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PredefinedStatusViewHolder(itemBinding)
    }

    override fun onBindViewHolder(holder: PredefinedStatusViewHolder, position: Int) {
        holder.bind(list[position], clickListener, context, isBackupStatusAvailable)
    }

    override fun getItemCount(): Int = list.size
}
