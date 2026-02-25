/*
 * Nextcloud Talk - Android Client
 *
 * SPDX-FileCopyrightText: 2022 Andy Scherzinger <info@andy-scherzinger.de>
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package com.gcc.talk.gccRemotefilebrowser.adapters

import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.gcc.talk.gccData.user.model.GccUser
import com.gcc.talk.gccRemotefilebrowser.GccSelectionInterface
import com.gcc.talk.gccRemotefilebrowser.model.GccRemoteFileBrowserItem
import com.gcc.talk.gccUtils.GccDrawableUtils

abstract class GccRemoteFileBrowserItemsViewHolder(
    open val binding: ViewBinding,
    val mimeTypeSelectionFilter: String? = null,
    val currentUser: GccUser,
    val selectionInterface: GccSelectionInterface
) : RecyclerView.ViewHolder(binding.root) {

    abstract val fileIcon: ImageView

    open fun onBind(item: GccRemoteFileBrowserItem) {
        fileIcon.setImageResource(GccDrawableUtils.getDrawableResourceIdForMimeType(item.mimeType))
    }
}
