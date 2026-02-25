/*
 * Nextcloud Talk - Android Client
 *
 * SPDX-FileCopyrightText: 2023 Marcel Hibbe <dev@mhibbe.de>
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package com.gcc.talk.gccOpenconversations.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.gcc.talk.gccData.user.model.GccUser
import com.gcc.talk.databinding.RvItemOpenConversationBinding
import com.gcc.talk.gccExtensions.loadConversationAvatar
import com.gcc.talk.gccModels.domain.GccConversationModel
import com.gcc.talk.gccModels.json.conversations.Conversation
import com.gcc.talk.gccUi.theme.ViewThemeUtils

class OpenConversationsAdapter(
    val user: GccUser,
    val viewThemeUtils: ViewThemeUtils,
    private val onClick: (Conversation) -> Unit
) : ListAdapter<Conversation, OpenConversationsAdapter.OpenConversationsViewHolder>(ConversationsCallback) {

    inner class OpenConversationsViewHolder(val itemBinding: RvItemOpenConversationBinding) :
        RecyclerView.ViewHolder(itemBinding.root) {

        var currentConversation: Conversation? = null

        init {
            itemBinding.root.setOnClickListener {
                currentConversation?.let {
                    onClick(it)
                }
            }
        }

        fun bindItem(conversation: Conversation) {
            val nameTextLayoutParams: RelativeLayout.LayoutParams = itemBinding.nameText.layoutParams as
                RelativeLayout.LayoutParams
            currentConversation = conversation
            val currentConversationModel = GccConversationModel.mapToConversationModel(conversation, user)
            itemBinding.nameText.text = conversation.displayName
            if (conversation.description == "") {
                itemBinding.descriptionText.visibility = View.GONE
                nameTextLayoutParams.addRule(RelativeLayout.CENTER_VERTICAL)
            } else {
                itemBinding.descriptionText.text = conversation.description
            }

            itemBinding.avatarView.loadConversationAvatar(
                user,
                currentConversationModel,
                false,
                viewThemeUtils
            )
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OpenConversationsViewHolder =
        OpenConversationsViewHolder(
            RvItemOpenConversationBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )

    override fun onBindViewHolder(holder: OpenConversationsViewHolder, position: Int) {
        val conversation = getItem(position)
        holder.bindItem(conversation)
    }
}

object ConversationsCallback : DiffUtil.ItemCallback<Conversation>() {
    override fun areItemsTheSame(oldItem: Conversation, newItem: Conversation): Boolean = oldItem == newItem

    override fun areContentsTheSame(oldItem: Conversation, newItem: Conversation): Boolean =
        oldItem.token == newItem.token
}
