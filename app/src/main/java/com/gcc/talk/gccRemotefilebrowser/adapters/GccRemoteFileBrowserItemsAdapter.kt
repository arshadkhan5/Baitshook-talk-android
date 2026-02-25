/*
 * Nextcloud Talk - Android Client
 *
 * SPDX-FileCopyrightText: 2022 Andy Scherzinger <info@andy-scherzinger.de>
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package com.gcc.talk.gccRemotefilebrowser.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.gcc.talk.gccData.user.model.GccUser
import com.gcc.talk.databinding.RvItemBrowserFileBinding
import com.gcc.talk.gccRemotefilebrowser.GccSelectionInterface
import com.gcc.talk.gccRemotefilebrowser.model.GccRemoteFileBrowserItem
import com.gcc.talk.gccUi.theme.ViewThemeUtils
import com.gcc.talk.gccUtilss.GccDateUtils

class GccRemoteFileBrowserItemsAdapter(
    private val showGrid: Boolean = false,
    private val mimeTypeSelectionFilter: String? = null,
    private val user: GccUser,
    private val selectionInterface: GccSelectionInterface,
    private val viewThemeUtils: ViewThemeUtils,
    private val dateUtils: GccDateUtils,
    private val onItemClicked: (GccRemoteFileBrowserItem) -> Unit
) : RecyclerView.Adapter<GccRemoteFileBrowserItemsViewHolder>() {

    var items: List<GccRemoteFileBrowserItem> = emptyList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GccRemoteFileBrowserItemsViewHolder =
        if (showGrid) {
            GccRemoteFileBrowserItemsListViewHolder(
                RvItemBrowserFileBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                ),
                mimeTypeSelectionFilter,
                user,
                selectionInterface,
                viewThemeUtils,
                dateUtils
            ) {
                onItemClicked(items[it])
            }
        } else {
            GccRemoteFileBrowserItemsListViewHolder(
                RvItemBrowserFileBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                ),
                mimeTypeSelectionFilter,
                user,
                selectionInterface,
                viewThemeUtils,
                dateUtils
            ) {
                onItemClicked(items[it])
            }
        }

    override fun onBindViewHolder(holder: GccRemoteFileBrowserItemsViewHolder, position: Int) {
        holder.onBind(items[position])
    }

    override fun getItemCount(): Int = items.size

    @SuppressLint("NotifyDataSetChanged")
    fun updateDataSet(browserItems: List<GccRemoteFileBrowserItem>) {
        items = browserItems
        notifyDataSetChanged()
    }
}
