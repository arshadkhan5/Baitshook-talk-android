/*
 * Nextcloud Talk - Android Client
 *
 * SPDX-FileCopyrightText: 2022 Álvaro Brey <alvaro@alvarobrey.com>
 * SPDX-FileCopyrightText: 2022 Tim Krüger <t@timkrueger.me>
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package com.gcc.talk.gccShareditems.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.gcc.talk.gccData.user.model.GccUser
import com.gcc.talk.databinding.SharedItemGridBinding
import com.gcc.talk.databinding.SharedItemListBinding
import com.gcc.talk.gccPolls.ui.GccPollMainDialogFragment
import com.gcc.talk.gccShareditems.activities.GccSharedItemsActivity
import com.gcc.talk.gccShareditems.model.GccSharedDeckCardItem
import com.gcc.talk.gccShareditems.model.GccSharedFileItem
import com.gcc.talk.gccShareditems.model.GccSharedItem
import com.gcc.talk.gccShareditems.model.GccSharedLocationItem
import com.gcc.talk.gccShareditems.model.GccSharedOtherItem
import com.gcc.talk.gccShareditems.model.GccSharedPinnedItem
import com.gcc.talk.gccShareditems.model.GccSharedPollItem
import com.gcc.talk.gccUi.theme.ViewThemeUtils
import com.gcc.talk.gccUtils.GccApiUtils
import java.util.Collections.emptyList

class GccSharedItemsAdapter(
    private val showGrid: Boolean,
    private val user: GccUser,
    private val roomToken: String,
    private val isUserConversationOwnerOrModerator: Boolean,
    private val isOne2One: Boolean,
    private val viewThemeUtils: ViewThemeUtils
) : RecyclerView.Adapter<GccSharedItemsViewHolder>() {

    var items: MutableList<GccSharedItem> = emptyList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GccSharedItemsViewHolder =
        if (showGrid) {
            GccSharedItemsGridViewHolder(
                SharedItemGridBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                ),
                user,
                viewThemeUtils
            )
        } else {
            GccSharedItemsListViewHolder(
                SharedItemListBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                ),
                user,
                viewThemeUtils
            )
        }

    override fun onBindViewHolder(holder: GccSharedItemsViewHolder, position: Int) {
        when (val item = items[position]) {
            is GccSharedPollItem -> holder.onBind(item, ::showPoll)
            is GccSharedFileItem -> holder.onBind(item)
            is GccSharedLocationItem -> holder.onBind(item)
            is GccSharedOtherItem -> holder.onBind(item)
            is GccSharedDeckCardItem -> holder.onBind(item)
            is GccSharedPinnedItem -> holder.onBind(item, ::openMessage, ::unpinMessage)
        }
    }

    override fun getItemCount(): Int = items.size

    private fun showPoll(item: GccSharedItem, context: Context) {
        val pollVoteDialog = GccPollMainDialogFragment.newInstance(
            user,
            roomToken,
            isUserConversationOwnerOrModerator,
            item.id,
            item.name
        )
        pollVoteDialog.show(
            (context as GccSharedItemsActivity).supportFragmentManager,
            TAG
        )
    }

    private fun unpinMessage(item: GccSharedItem, context: Context) {
        val credentials = GccApiUtils.getCredentials(user.username, user.token)
        val url = GccApiUtils.getUrlForChatMessagePinning(1, user.baseUrl, roomToken, item.id)

        val canPin = isOne2One || isUserConversationOwnerOrModerator
        if (canPin) {
            credentials?.let {
                (context as GccSharedItemsActivity).chatViewModel.unPinMessage(credentials, url)
                val index = items.indexOf(item)
                items.remove(item)
                this.notifyItemRemoved(index)
            }
        }
    }

    private fun openMessage(item: GccSharedItem, context: Context) {
        val credentials = GccApiUtils.getCredentials(user.username, user.token)
        val baseUrl = user.baseUrl
        (context as GccSharedItemsActivity).startContextChatWindowForMessage(
            credentials,
            baseUrl,
            roomToken,
            item.id,
            null
        )
    }

    companion object {
        private val TAG = GccSharedItemsAdapter::class.simpleName
    }
}
