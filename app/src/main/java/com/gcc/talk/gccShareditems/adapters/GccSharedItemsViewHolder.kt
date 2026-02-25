/*
 * Nextcloud Talk - Android Client
 *
 * SPDX-FileCopyrightText: 2022 Álvaro Brey <alvaro@alvarobrey.com>
 * SPDX-FileCopyrightText: 2022 Tim Krüger <t@timkrueger.me>
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package com.gcc.talk.gccShareditems.adapters

import android.content.Context
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.gcc.talk.gccData.user.model.GccUser
import com.gcc.talk.gccExtensions.loadImage
import com.gcc.talk.gccShareditems.model.GccSharedDeckCardItem
import com.gcc.talk.gccShareditems.model.GccSharedFileItem
import com.gcc.talk.gccShareditems.model.GccSharedItem
import com.gcc.talk.gccShareditems.model.GccSharedLocationItem
import com.gcc.talk.gccShareditems.model.GccSharedOtherItem
import com.gcc.talk.gccShareditems.model.GccSharedPinnedItem
import com.gcc.talk.gccShareditems.model.GccSharedPollItem
import com.gcc.talk.gccUi.theme.ViewThemeUtils
import com.gcc.talk.gccUtils.GccFileViewerUtils

abstract class GccSharedItemsViewHolder(
    open val binding: ViewBinding,
    internal val user: GccUser,
    private val viewThemeUtils: ViewThemeUtils
) : RecyclerView.ViewHolder(binding.root) {

    companion object {
        private val TAG = GccSharedItemsViewHolder::class.simpleName
    }

    abstract val image: ImageView
    abstract val clickTarget: View
    abstract val progressBar: ProgressBar

    open fun onBind(item: GccSharedFileItem) {
        val placeholder = viewThemeUtils.talk.getPlaceholderImage(image.context, item.mimeType)
        if (item.previewAvailable) {
            image.loadImage(
                item.previewLink,
                user,
                placeholder
            )
        } else {
            image.setImageDrawable(placeholder)
        }

        /*
        The GccFileViewerUtils forces us to do things at this points which should be done separated in the activity and
        the view model.

        This should be done after a refactoring of GccFileViewerUtils.
         */
        val fileViewerUtils = GccFileViewerUtils(image.context, user)

        clickTarget.setOnClickListener {
            fileViewerUtils.openFile(
                GccFileViewerUtils.FileInfo(item.id, item.name, item.fileSize),
                item.path,
                item.link,
                item.mimeType,
                GccFileViewerUtils.ProgressUi(
                    progressBar,
                    null,
                    image
                ),
                true
            )
        }

        fileViewerUtils.resumeToUpdateViewsByProgress(
            item.name,
            item.id,
            item.mimeType,
            true,
            GccFileViewerUtils.ProgressUi(progressBar, null, image)
        )
    }

    open fun onBind(item: GccSharedPollItem, showPoll: (item: GccSharedItem, context: Context) -> Unit) {}

    open fun onBind(item: GccSharedLocationItem) {}

    open fun onBind(item: GccSharedOtherItem) {}

    open fun onBind(item: GccSharedDeckCardItem) {}

    open fun onBind(
        item: GccSharedPinnedItem,
        openMessage: (item: GccSharedItem, context: Context) -> Unit,
        unpinMessage: (item: GccSharedItem, context: Context) -> Unit
    ) {}
}
