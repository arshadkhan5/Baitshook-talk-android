/*
 * Nextcloud Talk - Android Client
 *
 * SPDX-FileCopyrightText: 2024 Marcel Hibbe <dev@mhibbe.de>
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.gcc.talk.gccData.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy.Companion.REPLACE
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.gcc.talk.gccData.database.model.GccConversationEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

@Dao
interface GccConversationsDao {
    @Query("SELECT * FROM Conversations where accountId = :accountId")
    fun getConversationsForUser(accountId: Long): Flow<List<GccConversationEntity>>

    @Query("SELECT * FROM Conversations where accountId = :accountId AND token = :token")
    fun getConversationForUser(accountId: Long, token: String): Flow<GccConversationEntity?>

    @Transaction
    suspend fun upsertConversations(accountId: Long, serverItems: List<GccConversationEntity>) {
        serverItems.forEach { serverItem ->
            val existingItem = getConversationForUser(accountId, serverItem.token).first()
            if (existingItem != null) {
                val mergedItem = serverItem.copy()
                mergedItem.messageDraft = existingItem.messageDraft
                updateConversation(mergedItem)
            } else {
                insertConversation(serverItem)
            }
        }
    }

    /**
     * Deletes rows in the db matching the specified [conversationIds]
     */
    @Query(
        value = """
            DELETE FROM conversations
            WHERE internalId in (:conversationIds)
        """
    )
    fun deleteConversations(conversationIds: List<String>)

    @Update(onConflict = REPLACE)
    fun updateConversation(conversationEntity: GccConversationEntity)

    @Insert(onConflict = REPLACE)
    fun insertConversation(conversation: GccConversationEntity)

    @Query(
        """
        DELETE FROM Conversations
        WHERE accountId = :accountId
        """
    )
    fun clearAllConversationsForUser(accountId: Long)
}
