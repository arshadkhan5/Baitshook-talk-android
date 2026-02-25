/*
 * Nextcloud Talk - Android Client
 *
 * SPDX-FileCopyrightText: 2024 Julius Linus <juliuslinus1@gmail.com>
 * SPDX-FileCopyrightText: 2024 Marcel Hibbe <dev@mhibbe.de>
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.gcc.talk.gccConversationlist.data.network

import android.util.Log
import com.gcc.talk.gccChat.data.network.GccChatNetworkDataSource
import com.gcc.talk.gccConversationlist.data.GccOfflineConversationsRepository
import com.gcc.talk.gccData.database.model.GccConversationEntity
import com.gcc.talk.gccData.database.dao.GccConversationsDao
import com.gcc.talk.gccData.database.mappers.asEntity
import com.gcc.talk.gccData.database.mappers.asModel
import com.gcc.talk.gccData.network.GccNetworkMonitor
import com.gcc.talk.gccData.user.model.GccUser
import com.gcc.talk.gccModels.domain.GccConversationModel
import com.gcc.talk.gccUtils.CapabilitiesUtil.isUserStatusAvailable
import io.reactivex.Observer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

class GccOfflineFirstConversationsRepository @Inject constructor(
    private val dao: GccConversationsDao,
    private val network: GccConversationsNetworkDataSource,
    private val chatNetworkDataSource: GccChatNetworkDataSource,
    private val networkMonitor: GccNetworkMonitor
) : GccOfflineConversationsRepository {
    override val roomListFlow: Flow<List<GccConversationModel>>
        get() = _roomListFlow
    private val _roomListFlow: MutableSharedFlow<List<GccConversationModel>> = MutableSharedFlow()

    override val conversationFlow: Flow<GccConversationModel>
        get() = _conversationFlow
    private val _conversationFlow: MutableSharedFlow<GccConversationModel> = MutableSharedFlow()

    private val scope = CoroutineScope(Dispatchers.IO)

    override fun getRooms(user: GccUser): Job =
        scope.launch {
            val initialConversationModels = getListOfConversations(user.id!!)
            _roomListFlow.emit(initialConversationModels)

            if (networkMonitor.isOnline.value) {
                val conversationEntitiesFromSync = getRoomsFromServer(user)
                if (!conversationEntitiesFromSync.isNullOrEmpty()) {
                    val conversationModelsFromSync = conversationEntitiesFromSync.map(GccConversationEntity::asModel)
                    _roomListFlow.emit(conversationModelsFromSync)
                }
            }
        }

    override fun getRoom(user: GccUser, roomToken: String): Job =
        scope.launch {
            chatNetworkDataSource.getRoom(user, roomToken)
                .subscribeOn(Schedulers.io())
                ?.observeOn(AndroidSchedulers.mainThread())
                ?.subscribe(object : Observer<GccConversationModel> {
                    override fun onSubscribe(p0: Disposable) {
                        // unused atm
                    }

                    override fun onError(e: Throwable) {
                        runBlocking {
                            // In case network is offline or call fails
                            val id = user.id!!
                            val model = getConversation(id, roomToken)
                            if (model != null) {
                                _conversationFlow.emit(model)
                            } else {
                                Log.e(TAG, "Conversation model not found on device database")
                            }
                        }
                    }

                    override fun onComplete() {
                        // unused atm
                    }

                    override fun onNext(model: GccConversationModel) {
                        runBlocking {
                            _conversationFlow.emit(model)
                            val entityList = listOf(model.asEntity())
                            dao.upsertConversations(user.id!!, entityList)
                        }
                    }
                })
        }

    override suspend fun updateConversation(conversationModel: GccConversationModel) {
        val entity = conversationModel.asEntity()
        dao.updateConversation(entity)
    }

    override suspend fun getLocallyStoredConversation(user: GccUser, roomToken: String): GccConversationModel? {
        val id = user.id!!
        return getConversation(id, roomToken)
    }

    @Suppress("Detekt.TooGenericExceptionCaught")
    private suspend fun getRoomsFromServer(user: GccUser): List<GccConversationEntity>? {
        var conversationsFromSync: List<GccConversationEntity>? = null

        if (!networkMonitor.isOnline.value) {
            Log.d(TAG, "Device is offline, can't load conversations from server")
            return null
        }

        val includeStatus = isUserStatusAvailable(user)

        try {
            val conversationsList = network.getRooms(user, user.baseUrl!!, includeStatus)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .blockingSingle()

            conversationsFromSync = conversationsList.map {
                it.asEntity(user.id!!)
            }

            deleteLeftConversations(
                user,
                conversationsFromSync
            )
            dao.upsertConversations(user.id!!, conversationsFromSync)
        } catch (e: Exception) {
            Log.e(TAG, "Something went wrong when fetching conversations", e)
        }
        return conversationsFromSync
    }

    private suspend fun deleteLeftConversations(user: GccUser, conversationsFromSync: List<GccConversationEntity>) {
        val conversationsFromSyncIds = conversationsFromSync.map { it.internalId }.toSet()
        val oldConversationsFromDb = dao.getConversationsForUser(user.id!!).first()

        val conversationIdsToDelete = oldConversationsFromDb
            .map { it.internalId }
            .filterNot { it in conversationsFromSyncIds }

        dao.deleteConversations(conversationIdsToDelete)
    }

    private suspend fun getListOfConversations(accountId: Long): List<GccConversationModel> =
        dao.getConversationsForUser(accountId).map {
            it.map(GccConversationEntity::asModel)
        }.first()

    private suspend fun getConversation(accountId: Long, token: String): GccConversationModel? {
        val entity = dao.getConversationForUser(accountId, token).first()
        return entity?.asModel()
    }

    companion object {
        val TAG = GccOfflineFirstConversationsRepository::class.simpleName
    }
}
