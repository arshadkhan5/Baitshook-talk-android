/*
 * Nextcloud Talk - Android Client
 *
 * SPDX-FileCopyrightText: 2022 Marcel Hibbe <dev@mhibbe.de>
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package com.gcc.talk.gccRepositories.reactions

import com.gcc.talk.gccApi.GccNcApi
import com.gcc.talk.gccChat.data.model.GccChatMessage
import com.gcc.talk.gccData.database.dao.GccChatMessagesDao
import com.gcc.talk.gccModels.domain.GccReactionAddedModel
import com.gcc.talk.gccModels.domain.GccReactionDeletedModel
import com.gcc.talk.gccModels.json.generic.GenericMeta
import io.reactivex.Observable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

class GccReactionsRepositoryImpl @Inject constructor(private val ncApi: GccNcApi, private val dao: GccChatMessagesDao) :
    GccReactionsRepository {

    override fun addReaction(
        credentials: String?,
        userId: Long,
        url: String,
        roomToken: String,
        message: GccChatMessage,
        emoji: String
    ): Observable<GccReactionAddedModel> {
        return ncApi.sendReaction(
            credentials,
            url,
            emoji
        ).map {
            val model = mapToReactionAddedModel(message, emoji, it.ocs?.meta!!)
            persistAddedModel(
                userId,
                model,
                roomToken
            )
            return@map model
        }
    }

    override fun deleteReaction(
        credentials: String?,
        userId: Long,
        url: String,
        roomToken: String,
        message: GccChatMessage,
        emoji: String
    ): Observable<GccReactionDeletedModel> {
        return ncApi.deleteReaction(
            credentials,
            url,
            emoji
        ).map {
            val model = mapToReactionDeletedModel(message, emoji, it.ocs?.meta!!)
            persistDeletedModel(
                userId,
                model,
                roomToken
            )
            return@map model
        }
    }

    private fun mapToReactionAddedModel(
        message: GccChatMessage,
        emoji: String,
        reactionResponse: GenericMeta
    ): GccReactionAddedModel {
        val success = reactionResponse.statusCode == HTTP_CREATED
        return GccReactionAddedModel(
            message,
            emoji,
            success
        )
    }

    private fun mapToReactionDeletedModel(
        message: GccChatMessage,
        emoji: String,
        reactionResponse: GenericMeta
    ): GccReactionDeletedModel {
        val success = reactionResponse.statusCode == HTTP_OK
        return GccReactionDeletedModel(
            message,
            emoji,
            success
        )
    }

    private fun persistAddedModel(userId: Long, model: GccReactionAddedModel, roomToken: String) =
        CoroutineScope(Dispatchers.IO).launch {
            // 1. Call DAO, Get a singular GccChatMessageEntity with model.chatMessage.{PARAM}
            val id = model.chatMessage.jsonMessageId.toLong()
            val internalConversationId = "$userId@$roomToken"
            val emoji = model.emoji

            val message = dao.getChatMessageForConversation(
                internalConversationId,
                id
            ).first()

            // 2. Check state of entity, create params as needed
            if (message.reactions == null) {
                message.reactions = LinkedHashMap()
            }

            if (message.reactionsSelf == null) {
                message.reactionsSelf = ArrayList()
            }

            var amount = message.reactions!![emoji]
            if (amount == null) {
                amount = 0
            }
            message.reactions!![emoji] = amount + 1
            message.reactionsSelf!!.add(emoji)

            // 3. Call DAO again, to update the singular GccChatMessageEntity with params
            dao.updateChatMessage(message)
        }

    private fun persistDeletedModel(userId: Long, model: GccReactionDeletedModel, roomToken: String) =
        CoroutineScope(Dispatchers.IO).launch {
            // 1. Call DAO, Get a singular GccChatMessageEntity with model.chatMessage.{PARAM}
            val id = model.chatMessage.jsonMessageId.toLong()
            val internalConversationId = "$userId@$roomToken"
            val emoji = model.emoji

            val message = dao.getChatMessageForConversation(internalConversationId, id).first()

            // 2. Check state of entity, create params as needed
            if (message.reactions == null) {
                message.reactions = LinkedHashMap()
            }

            if (message.reactionsSelf == null) {
                message.reactionsSelf = ArrayList()
            }

            var amount = message.reactions!![emoji]
            if (amount == null) {
                amount = 0
            }
            message.reactions!![emoji] = amount - 1
            message.reactionsSelf!!.remove(emoji)

            // 3. Call DAO again, to update the singular GccChatMessageEntity with params
            dao.updateChatMessage(message)
        }

    companion object {
        private const val HTTP_OK: Int = 200
        private const val HTTP_CREATED: Int = 201
    }
}
