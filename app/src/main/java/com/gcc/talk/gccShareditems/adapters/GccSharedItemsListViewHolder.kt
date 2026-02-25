/*
 * Nextcloud Talk - Android Client
 *
 * SPDX-FileCopyrightText: 2022 Álvaro Brey <alvaro@alvarobrey.com>
 * SPDX-FileCopyrightText: 2022 Tim Krüger <t@timkrueger.me>
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package com.gcc.talk.gccShareditems.adapters

import android.content.Context
import android.content.Intent
import android.text.format.Formatter
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import androidx.core.content.ContextCompat
import coil.load
import com.gcc.talk.R
import com.gcc.talk.gccData.user.model.GccUser
import com.gcc.talk.databinding.SharedItemListBinding
import com.gcc.talk.gccShareditems.model.GccSharedDeckCardItem
import com.gcc.talk.gccShareditems.model.GccSharedFileItem
import com.gcc.talk.gccShareditems.model.GccSharedItem
import com.gcc.talk.gccShareditems.model.GccSharedLocationItem
import com.gcc.talk.gccShareditems.model.GccSharedOtherItem
import com.gcc.talk.gccShareditems.model.GccSharedPinnedItem
import com.gcc.talk.gccShareditems.model.GccSharedPollItem
import com.gcc.talk.gccUi.theme.ViewThemeUtils

class GccSharedItemsListViewHolder(
    override val binding: SharedItemListBinding,
    user: GccUser,
    viewThemeUtils: ViewThemeUtils
) : GccSharedItemsViewHolder(binding, user, viewThemeUtils) {

    override val image: ImageView
        get() = binding.fileImage
    override val clickTarget: View
        get() = binding.fileItem
    override val progressBar: ProgressBar
        get() = binding.progressBar

    override fun onBind(item: GccSharedFileItem) {
        super.onBind(item)

        binding.fileName.text = item.name
        binding.fileSize.text = item.fileSize.let {
            Formatter.formatShortFileSize(
                binding.fileSize.context,
                it
            )
        }
        binding.fileDate.text = item.dateTime
        binding.actor.text = item.actorName
    }

    override fun onBind(item: GccSharedPollItem, showPoll: (item: GccSharedItem, context: Context) -> Unit) {
        super.onBind(item, showPoll)

        binding.fileName.text = item.name
        binding.fileSize.visibility = View.GONE
        binding.separator1.visibility = View.GONE
        binding.fileDate.text = item.dateTime
        binding.actor.text = item.actorName
        image.load(R.drawable.ic_baseline_bar_chart_24)
        image.setColorFilter(
            ContextCompat.getColor(image.context, R.color.high_emphasis_menu_icon),
            android.graphics.PorterDuff.Mode.SRC_IN
        )
        clickTarget.setOnClickListener {
            showPoll(item, it.context)
        }
    }

    override fun onBind(item: GccSharedLocationItem) {
        super.onBind(item)

        binding.fileName.text = item.name
        binding.fileSize.visibility = View.GONE
        binding.separator1.visibility = View.GONE
        binding.fileDate.text = item.dateTime
        binding.actor.text = item.actorName
        image.load(R.drawable.ic_baseline_location_on_24)
        image.setColorFilter(
            ContextCompat.getColor(image.context, R.color.high_emphasis_menu_icon),
            android.graphics.PorterDuff.Mode.SRC_IN
        )

        clickTarget.setOnClickListener {
            val browserIntent = Intent(Intent.ACTION_VIEW, item.geoUri)
            browserIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            it.context.startActivity(browserIntent)
        }
    }

    override fun onBind(item: GccSharedOtherItem) {
        super.onBind(item)

        binding.fileName.text = item.name
        binding.fileSize.visibility = View.GONE
        binding.separator1.visibility = View.GONE
        binding.fileDate.text = item.dateTime
        binding.actor.text = item.actorName
        image.load(R.drawable.ic_mimetype_file)
        image.setColorFilter(
            ContextCompat.getColor(image.context, R.color.high_emphasis_menu_icon),
            android.graphics.PorterDuff.Mode.SRC_IN
        )
    }

    override fun onBind(item: GccSharedDeckCardItem) {
        super.onBind(item)

        binding.fileName.text = item.name
        binding.fileSize.visibility = View.GONE
        binding.separator1.visibility = View.GONE
        binding.fileDate.text = item.dateTime
        binding.actor.text = item.actorName
        image.load(R.drawable.ic_baseline_deck_24)
        image.setColorFilter(
            ContextCompat.getColor(image.context, R.color.high_emphasis_menu_icon),
            android.graphics.PorterDuff.Mode.SRC_IN
        )

        clickTarget.setOnClickListener {
            val browserIntent = Intent(Intent.ACTION_VIEW, item.link)
            browserIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            it.context.startActivity(browserIntent)
        }
    }

    override fun onBind(
        item: GccSharedPinnedItem,
        openMessage: (item: GccSharedItem, context: Context) -> Unit,
        unpinMessage: (item: GccSharedItem, context: Context) -> Unit
    ) {
        super.onBind(item, openMessage, unpinMessage)
        binding.fileName.text = item.name // actually the message of the chat item
        binding.fileSize.visibility = View.GONE
        binding.separator1.visibility = View.GONE
        binding.fileDate.text = item.dateTime
        binding.actor.text = item.actorName

        image.load(R.drawable.keep_off_24px)
        image.setColorFilter(
            ContextCompat.getColor(image.context, R.color.high_emphasis_menu_icon),
            android.graphics.PorterDuff.Mode.SRC_IN
        )

        image.setOnClickListener {
            unpinMessage(item, it.context)
        }

        clickTarget.setOnClickListener {
            openMessage(item, it.context)
        }
    }
}
