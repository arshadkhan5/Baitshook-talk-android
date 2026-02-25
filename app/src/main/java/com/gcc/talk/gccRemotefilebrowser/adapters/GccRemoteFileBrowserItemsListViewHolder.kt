/*
 * Nextcloud Talk - Android Client
 *
 * SPDX-FileCopyrightText: 2022 Andy Scherzinger <info@andy-scherzinger.de>
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package com.gcc.talk.gccRemotefilebrowser.adapters

import android.text.format.Formatter
import android.view.View
import android.widget.ImageView
import com.gcc.talk.R
import com.gcc.talk.gccData.user.model.GccUser
import com.gcc.talk.databinding.RvItemBrowserFileBinding
import com.gcc.talk.gccExtensions.loadImage
import com.gcc.talk.gccRemotefilebrowser.GccSelectionInterface
import com.gcc.talk.gccRemotefilebrowser.model.GccRemoteFileBrowserItem
import com.gcc.talk.gccUi.theme.ViewThemeUtils
import com.gcc.talk.gccUtils.GccApiUtils
import com.gcc.talk.gccUtilss.GccDateUtils
import com.gcc.talk.gccUtils.GccMimetype.FOLDER

class GccRemoteFileBrowserItemsListViewHolder(
    override val binding: RvItemBrowserFileBinding,
    mimeTypeSelectionFilter: String?,
    currentUser: GccUser,
    selectionInterface: GccSelectionInterface,
    private val viewThemeUtils: ViewThemeUtils,
    private val dateUtils: GccDateUtils,
    onItemClicked: (Int) -> Unit
) : GccRemoteFileBrowserItemsViewHolder(binding, mimeTypeSelectionFilter, currentUser, selectionInterface) {

    override val fileIcon: ImageView
        get() = binding.fileIcon

    private var selectable: Boolean = true
    private var clickable: Boolean = true

    init {
        itemView.setOnClickListener {
            if (clickable) {
                onItemClicked(bindingAdapterPosition)
                if (selectable) {
                    binding.selectFileCheckbox.toggle()
                }
            }
        }
    }

    override fun onBind(item: GccRemoteFileBrowserItem) {
        super.onBind(item)

        if (!item.isAllowedToReShare || item.isEncrypted) {
            binding.root.isEnabled = false
            binding.root.alpha = DISABLED_ALPHA
        } else {
            binding.root.isEnabled = true
            binding.root.alpha = ENABLED_ALPHA
        }

        binding.fileEncryptedImageView.visibility =
            if (item.isEncrypted) {
                View.VISIBLE
            } else {
                View.GONE
            }

        binding.fileFavoriteImageView.visibility =
            if (item.isFavorite) {
                View.VISIBLE
            } else {
                View.GONE
            }

        calculateSelectability(item)
        calculateClickability(item, selectable)
        setSelectability()

        val placeholder = viewThemeUtils.talk.getPlaceholderImage(binding.root.context, item.mimeType)

        if (item.hasPreview) {
            val path = GccApiUtils.getUrlForFilePreviewWithRemotePath(
                currentUser.baseUrl!!,
                item.path,
                fileIcon.context.resources.getDimensionPixelSize(R.dimen.small_item_height)
            )
            if (path.isNotEmpty()) {
                fileIcon.loadImage(path, currentUser, placeholder)
            }
        } else {
            fileIcon.setImageDrawable(placeholder)
        }

        binding.filenameTextView.text = item.displayName
        binding.fileModifiedInfo.text = String.format(
            binding.fileModifiedInfo.context.getString(R.string.nc_last_modified),
            Formatter.formatShortFileSize(binding.fileModifiedInfo.context, item.size),
            dateUtils.getLocalDateTimeStringFromTimestamp(item.modifiedTimestamp)
        )

        binding.selectFileCheckbox.isChecked = selectionInterface.isPathSelected(item.path!!)
    }

    private fun setSelectability() {
        if (selectable) {
            binding.selectFileCheckbox.visibility = View.VISIBLE
            viewThemeUtils.platform.themeCheckbox(binding.selectFileCheckbox)
        } else {
            binding.selectFileCheckbox.visibility = View.GONE
        }
    }

    private fun calculateSelectability(item: GccRemoteFileBrowserItem) {
        selectable = item.isFile &&
            (mimeTypeSelectionFilter == null || item.mimeType?.startsWith(mimeTypeSelectionFilter) == true) &&
            (item.isAllowedToReShare && !item.isEncrypted)
    }

    private fun calculateClickability(item: GccRemoteFileBrowserItem, selectableItem: Boolean) {
        clickable = selectableItem || FOLDER == item.mimeType
    }

    companion object {
        private const val DISABLED_ALPHA: Float = 0.38f
        private const val ENABLED_ALPHA: Float = 1.0f
    }
}
