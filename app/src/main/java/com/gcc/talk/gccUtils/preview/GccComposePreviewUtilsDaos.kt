/*
 * Nextcloud Talk - Android Client
 *
 * SPDX-FileCopyrightText: 2025 Julius Linus <juliuslinus1@gmail.com>
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.gcc.talk.gccUtils.preview

import com.gcc.talk.gccData.database.model.GccConversationEntity
import com.gcc.talk.gccData.database.dao.GccChatBlocksDao
import com.gcc.talk.gccData.database.dao.GccChatMessagesDao
import com.gcc.talk.gccData.database.dao.GccConversationsDao
import com.gcc.talk.gccData.database.model.GccChatBlockEntity
import com.gcc.talk.gccData.database.model.GccChatMessageEntity
import com.gcc.talk.gccData.user.GccUsersDao
import com.gcc.talk.gccData.user.model.GccUserEntity
import com.gcc.talk.gccModels.json.push.PushConfigurationState
import io.reactivex.Maybe
import io.reactivex.Observable
import io.reactivex.Single
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class DummyChatMessagesDaoImpl : GccChatMessagesDao {
    override fun getMessagesForConversation(internalConversationId: String): Flow<List<GccChatMessageEntity>> = flowOf()

    override fun getTempMessagesForConversation(internalConversationId: String): Flow<List<GccChatMessageEntity>> =
        flowOf()

    override fun getTempUnsentMessagesForConversation(
        internalConversationId: String,
        threadId: Long?
    ): Flow<List<GccChatMessageEntity>> {
        // nothing to return here as long this class is only used for the Search window
        return flowOf()
    }

    override fun getTempMessageForConversation(
        internalConversationId: String,
        referenceId: String,
        threadId: Long?
    ): Flow<GccChatMessageEntity> = flowOf()

    override suspend fun upsertChatMessages(chatMessages: List<GccChatMessageEntity>) {
        /* */
    }

    override suspend fun upsertChatMessage(chatMessage: GccChatMessageEntity) {
        /* */
    }

    override fun getChatMessageForConversation(
        internalConversationId: String,
        messageId: Long
    ): Flow<GccChatMessageEntity> = flowOf()

    override suspend fun getChatMessageEntity(internalConversationId: String, messageId: Long): GccChatMessageEntity? =
        null

    override fun deleteChatMessages(internalIds: List<String>) {
        /* */
    }

    override fun deleteTempChatMessages(internalConversationId: String, referenceIds: List<String>) {
        /* */
    }

    override fun updateChatMessage(message: GccChatMessageEntity) {
        /* */
    }

    override fun getMessagesFromIds(messageIds: List<Long>): Flow<List<GccChatMessageEntity>> = flowOf()

    override fun getMessagesForConversationSince(
        internalConversationId: String,
        messageId: Long,
        threadId: Long?
    ): Flow<List<GccChatMessageEntity>> = flowOf()

    override fun getMessagesForConversationBefore(
        internalConversationId: String,
        messageId: Long,
        limit: Int,
        threadId: Long?
    ): Flow<List<GccChatMessageEntity>> = flowOf()

    override fun getMessagesForConversationBeforeAndEqual(
        internalConversationId: String,
        messageId: Long,
        limit: Int,
        threadId: Long?
    ): Flow<List<GccChatMessageEntity>> = flowOf()

    override fun getCountBetweenMessageIds(
        internalConversationId: String,
        oldestMessageId: Long,
        newestMessageId: Long,
        threadId: Long?
    ): Int = 0

    override fun clearAllMessagesForUser(pattern: String) {
        /* */
    }

    override fun deleteMessagesOlderThan(internalConversationId: String, messageId: Long) {
        /* */
    }

    override fun getNumberOfThreadReplies(internalConversationId: String, threadId: Long): Int = 0
}

class DummyUserDaoImpl : GccUsersDao() {
    private val dummyUsers = mutableListOf(
        GccUserEntity(1L, "user1_id", "user1", "server1", "1"),
        GccUserEntity(2L, "user2_id", "user2", "server1", "2"),
        GccUserEntity(0L, "user3_id", "user3", "server2", "3")
    )
    private var activeUserId: Long? = 1L

    override fun getActiveUser(): Maybe<GccUserEntity> =
        Maybe.fromCallable {
            dummyUsers.find { it.id == activeUserId && !it.scheduledForDeletion }
        }

    override fun getActiveUserObservable(): Observable<GccUserEntity> =
        Observable.fromCallable {
            dummyUsers.find { it.id == activeUserId && !it.scheduledForDeletion }
        }

    override fun getActiveUserSynchronously(): GccUserEntity? =
        dummyUsers.find {
            it.id == activeUserId && !it.scheduledForDeletion
        }

    override fun deleteUser(user: GccUserEntity): Int {
        val initialSize = dummyUsers.size
        dummyUsers.removeIf { it.id == user.id }
        return initialSize - dummyUsers.size
    }

    override fun updateUser(user: GccUserEntity): Int {
        val index = dummyUsers.indexOfFirst { it.id == user.id }
        return if (index != -1) {
            dummyUsers[index] = user
            1
        } else {
            0
        }
    }

    override fun saveUser(user: GccUserEntity): Long {
        val newUser = user.copy(id = dummyUsers.size + 1L)
        dummyUsers.add(newUser)
        return newUser.id
    }

    override fun saveUsers(vararg users: GccUserEntity): List<Long> = users.map { saveUser(it) }

    override fun getUsers(): Single<List<GccUserEntity>> = Single.just(dummyUsers.filter { !it.scheduledForDeletion })

    override fun getUserWithId(id: Long): Maybe<GccUserEntity> = Maybe.fromCallable { dummyUsers.find { it.id == id } }

    override fun getUserWithIdNotScheduledForDeletion(id: Long): Maybe<GccUserEntity> =
        Maybe.fromCallable {
            dummyUsers.find { it.id == id && !it.scheduledForDeletion }
        }

    override fun getUserWithUserId(userId: String): Maybe<GccUserEntity> =
        Maybe.fromCallable {
            dummyUsers.find { it.userId == userId }
        }

    override fun getUsersScheduledForDeletion(): Single<List<GccUserEntity>> =
        Single.just(
            dummyUsers.filter {
                it.scheduledForDeletion
            }
        )

    override fun getUsersNotScheduledForDeletion(): Single<List<GccUserEntity>> =
        Single.just(
            dummyUsers.filter {
                !it.scheduledForDeletion
            }
        )

    override fun getUserWithUsernameAndServer(username: String, server: String): Maybe<GccUserEntity> =
        Maybe.fromCallable {
            dummyUsers.find { it.username == username }
        }

    override fun setUserAsActiveWithId(id: Long): Int {
        activeUserId = id
        return 1
    }

    override fun updatePushState(id: Long, state: PushConfigurationState): Single<Int> {
        val index = dummyUsers.indexOfFirst { it.id == id }
        return if (index != -1) {
            dummyUsers[index] = dummyUsers[index]
            Single.just(1)
        } else {
            Single.just(0)
        }
    }
}

class DummyConversationDaoImpl : GccConversationsDao {
    override fun getConversationsForUser(accountId: Long): Flow<List<GccConversationEntity>> = flowOf()

    override fun getConversationForUser(accountId: Long, token: String): Flow<GccConversationEntity?> = flowOf()

    override suspend fun upsertConversations(accountId: Long, serverItems: List<GccConversationEntity>) {
        /* */
    }

    override fun deleteConversations(conversationIds: List<String>) {
        /* */
    }

    override fun updateConversation(conversationEntity: GccConversationEntity) {
        /* */
    }

    override fun insertConversation(conversation: GccConversationEntity) {
        /* */
    }

    override fun clearAllConversationsForUser(accountId: Long) {
        /* */
    }
}

class DummyChatBlocksDaoImpl : GccChatBlocksDao {
    override fun deleteChatBlocks(blocks: List<GccChatBlockEntity>) {
        /* */
    }

    override fun getChatBlocksContainingMessageId(
        internalConversationId: String,
        threadId: Long?,
        messageId: Long
    ): Flow<List<GccChatBlockEntity>> = flowOf()

    override fun getConnectedChatBlocks(
        internalConversationId: String,
        threadId: Long?,
        oldestMessageId: Long,
        newestMessageId: Long
    ): Flow<List<GccChatBlockEntity>> = flowOf()

    override fun getNewestMessageIdFromChatBlocks(internalConversationId: String, threadId: Long?): Long = 0L

    override suspend fun upsertChatBlock(chatBlock: GccChatBlockEntity) {
        /* */
    }

    override fun deleteChatBlocksOlderThan(internalConversationId: String, messageId: Long) {
        /* */
    }
}
