/*
 * Nextcloud Talk - Android Client
 *
 * SPDX-FileCopyrightText: 2022 Álvaro Brey <alvaro@alvarobrey.com>
 * SPDX-FileCopyrightText: 2022 Tim Krüger <t@timkrueger.me>
 * SPDX-FileCopyrightText: 2021 Andy Scherzinger <infoi@andy-scherzinger.de>
 * SPDX-FileCopyrightText: 2017-2019 Mario Danic <mario@lovelyhq.com>
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package com.gcc.talk.gccAdapters.items

import android.content.Context
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.gcc.talk.R
import com.gcc.talk.gccData.user.model.GccUser
import com.gcc.talk.databinding.RvItemSearchMessageBinding
import com.gcc.talk.gccExtensions.loadThumbnail
import com.gcc.talk.gccModels.domain.GccSearchMessageEntry
import com.gcc.talk.gccUi.theme.ViewThemeUtils
import eu.davidea.flexibleadapter.FlexibleAdapter
import eu.davidea.flexibleadapter.items.AbstractFlexibleItem
import eu.davidea.flexibleadapter.items.IFilterable
import eu.davidea.flexibleadapter.items.IFlexible
import eu.davidea.flexibleadapter.items.ISectionable
import eu.davidea.viewholders.FlexibleViewHolder

data class GccMessageResultItem(
    private val context: Context,
    private val currentUser: GccUser,
    val messageEntry: GccSearchMessageEntry,
    var showHeader: Boolean = false,
    private val viewThemeUtils: ViewThemeUtils
) : AbstractFlexibleItem<GccMessageResultItem.ViewHolder>(),
    IFilterable<String>,
    ISectionable<GccMessageResultItem.ViewHolder, GccGenericTextHeaderItem> {

    class ViewHolder(view: View, adapter: FlexibleAdapter<*>) : FlexibleViewHolder(view, adapter) {
        var binding: RvItemSearchMessageBinding

        init {
            binding = RvItemSearchMessageBinding.bind(view)
        }
    }

    override fun getLayoutRes(): Int = R.layout.rv_item_search_message

    override fun createViewHolder(
        view: View,
        adapter: FlexibleAdapter<IFlexible<RecyclerView.ViewHolder>>
    ): ViewHolder = ViewHolder(view, adapter)

    override fun bindViewHolder(
        adapter: FlexibleAdapter<IFlexible<RecyclerView.ViewHolder>>,
        holder: ViewHolder,
        position: Int,
        payloads: MutableList<Any>?
    ) {
        holder.binding.conversationTitle.text = messageEntry.title
        bindMessageExcerpt(holder)
        messageEntry.thumbnailURL?.let { holder.binding.thumbnail.loadThumbnail(it, currentUser) }
    }

    private fun bindMessageExcerpt(holder: ViewHolder) {
        viewThemeUtils.platform.highlightText(
            holder.binding.messageExcerpt,
            messageEntry.messageExcerpt,
            messageEntry.searchTerm
        )
    }

    override fun filter(constraint: String?): Boolean = true

    override fun getItemViewType(): Int = VIEW_TYPE

    companion object {
        const val VIEW_TYPE = GccFlexibleItemViewType.MESSAGE_RESULT_ITEM
    }

    override fun getHeader(): GccGenericTextHeaderItem =
        GccMessagesTextHeaderItem(context, viewThemeUtils)
            .apply {
                isHidden = showHeader // FlexibleAdapter needs this hack for some reason
            }

    override fun setHeader(header: GccGenericTextHeaderItem?) {
        // nothing, header is always the same
    }
}
