/*
 * Nextcloud Talk - Android Client
 *
 * SPDX-FileCopyrightText: 2023 Marcel Hibbe <dev@mhibbe.de>
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package com.gcc.talk.gccChat.data.network

import com.gcc.talk.gccApi.GccNcApi
import com.gcc.talk.gccApi.GccNcApiCoroutines
import com.gcc.talk.gccData.user.model.GccUser
import com.gcc.talk.gccModels.domain.GccConversationModel
import com.gcc.talk.gccModels.json.capabilities.SpreedCapability
import com.gcc.talk.gccModels.json.chat.ChatMessageJson
import com.gcc.talk.gccModels.json.chat.ChatOverallSingleMessage
import com.gcc.talk.gccModels.json.conversations.RoomOverall
import com.gcc.talk.gccModels.json.generic.GenericOverall
import com.gcc.talk.gccModels.json.opengraph.Reference
import com.gcc.talk.gccModels.json.reminder.Reminder
import com.gcc.talk.gccModels.json.userAbsence.UserAbsenceOverall
import com.gcc.talk.gccUtils.GccApiUtils
import com.gcc.talk.gccUtils.message.GccSendMessageUtils
import io.reactivex.Observable
import retrofit2.Response
import com.gcc.talk.gccModels.json.chat.ChatOverall

class GccRetrofitChatNetwork(private val ncApi: GccNcApi, private val ncApiCoroutines: GccNcApiCoroutines) :
    GccChatNetworkDataSource {
    override fun getRoom(user: GccUser, roomToken: String): Observable<GccConversationModel> {
        val credentials: String = GccApiUtils.getCredentials(user.username, user.token)!!
        val apiVersion = GccApiUtils.getConversationApiVersion(user, intArrayOf(GccApiUtils.API_V4, GccApiUtils.API_V3, 1))

        return ncApi.getRoom(
            credentials,
            GccApiUtils.getUrlForRoom(apiVersion, user.baseUrl!!, roomToken)
        ).map { GccConversationModel.mapToConversationModel(it.ocs?.data!!, user) }
    }

    override fun getCapabilities(user: GccUser, roomToken: String): Observable<SpreedCapability> {
        val credentials: String = GccApiUtils.getCredentials(user.username, user.token)!!
        val apiVersion = GccApiUtils.getConversationApiVersion(user, intArrayOf(GccApiUtils.API_V4, GccApiUtils.API_V3, 1))

        return ncApi.getRoomCapabilities(
            credentials,
            GccApiUtils.getUrlForRoomCapabilities(apiVersion, user.baseUrl!!, roomToken)
        ).map { it.ocs?.data }
    }

    override fun joinRoom(user: GccUser, roomToken: String, roomPassword: String): Observable<GccConversationModel> {
        val credentials: String = GccApiUtils.getCredentials(user.username, user.token)!!
        val apiVersion = GccApiUtils.getConversationApiVersion(user, intArrayOf(GccApiUtils.API_V4, 1))

        return ncApi.joinRoom(
            credentials,
            GccApiUtils.getUrlForParticipantsActive(apiVersion, user.baseUrl!!, roomToken),
            roomPassword
        ).map { GccConversationModel.mapToConversationModel(it.ocs?.data!!, user) }
    }

    override fun setReminder(
        user: GccUser,
        roomToken: String,
        messageId: String,
        timeStamp: Int,
        chatApiVersion: Int
    ): Observable<Reminder> {
        val credentials: String = GccApiUtils.getCredentials(user.username, user.token)!!
        return ncApi.setReminder(
            credentials,
            GccApiUtils.getUrlForReminder(user, roomToken, messageId, chatApiVersion),
            timeStamp
        ).map {
            it.ocs!!.data
        }
    }

    override fun getReminder(
        user: GccUser,
        roomToken: String,
        messageId: String,
        chatApiVersion: Int
    ): Observable<Reminder> {
        val credentials: String = GccApiUtils.getCredentials(user.username, user.token)!!
        return ncApi.getReminder(
            credentials,
            GccApiUtils.getUrlForReminder(user, roomToken, messageId, chatApiVersion)
        ).map {
            it.ocs!!.data
        }
    }

    override fun deleteReminder(
        user: GccUser,
        roomToken: String,
        messageId: String,
        chatApiVersion: Int
    ): Observable<GenericOverall> {
        val credentials: String = GccApiUtils.getCredentials(user.username, user.token)!!
        return ncApi.deleteReminder(
            credentials,
            GccApiUtils.getUrlForReminder(user, roomToken, messageId, chatApiVersion)
        ).map {
            it
        }
    }

    override fun shareToNotes(
        credentials: String,
        url: String,
        message: String,
        displayName: String
    ): Observable<ChatOverallSingleMessage> =
        ncApi.sendChatMessage(
            credentials,
            url,
            message,
            displayName,
            null,
            false,
            GccSendMessageUtils().generateReferenceId()
        ).map {
            it
        }

    override suspend fun checkForNoteToSelf(credentials: String, url: String): RoomOverall =
        ncApiCoroutines.getNoteToSelfRoom(credentials, url)

    override fun shareLocationToNotes(
        credentials: String,
        url: String,
        objectType: String,
        objectId: String,
        metadata: String
    ): Observable<GenericOverall> = ncApi.sendLocation(credentials, url, objectType, objectId, metadata).map { it }

    override fun leaveRoom(credentials: String, url: String): Observable<GenericOverall> =
        ncApi.leaveRoom(credentials, url).map {
            it
        }

    override suspend fun sendChatMessage(
        credentials: String,
        url: String,
        message: String,
        displayName: String,
        replyTo: Int,
        sendWithoutNotification: Boolean,
        referenceId: String,
        threadTitle: String?
    ): ChatOverallSingleMessage =
        ncApiCoroutines.sendChatMessage(
            credentials,
            url,
            message,
            displayName,
            replyTo,
            sendWithoutNotification,
            referenceId,
            threadTitle
        )

    override fun pullChatMessages(
        credentials: String,
        url: String,
        fieldMap: HashMap<String, Int>
    ): Observable<Response<*>> = ncApi.pullChatMessages(credentials, url, fieldMap).map { it }

    override fun deleteChatMessage(credentials: String, url: String): Observable<ChatOverallSingleMessage> =
        ncApi.deleteChatMessage(credentials, url).map {
            it
        }

    override fun createRoom(credentials: String, url: String, map: Map<String, String>): Observable<RoomOverall> =
        ncApi.createRoom(credentials, url, map).map {
            it
        }

    override fun setChatReadMarker(
        credentials: String,
        url: String,
        previousMessageId: Int
    ): Observable<GenericOverall> = ncApi.setChatReadMarker(credentials, url, previousMessageId).map { it }

    override suspend fun editChatMessage(credentials: String, url: String, text: String): ChatOverallSingleMessage =
        ncApiCoroutines.editChatMessage(credentials, url, text)

    override suspend fun getOutOfOfficeStatusForUser(
        credentials: String,
        baseUrl: String,
        userId: String
    ): UserAbsenceOverall =
        ncApiCoroutines.getOutOfOfficeStatusForUser(
            credentials,
            GccApiUtils.getUrlForOutOfOffice(baseUrl, userId)
        )

    override suspend fun getContextForChatMessage(
        credentials: String,
        baseUrl: String,
        token: String,
        messageId: String,
        limit: Int,
        threadId: Int?
    ): List<ChatMessageJson> {
        val url = GccApiUtils.getUrlForChatMessageContext(baseUrl, token, messageId)
        return ncApiCoroutines.getContextOfChatMessage(credentials, url, limit, threadId).ocs?.data ?: listOf()
    }

    override suspend fun getOpenGraph(
        credentials: String,
        baseUrl: String,
        extractedLinkToPreview: String
    ): Reference? {
        val openGraphLink = GccApiUtils.getUrlForOpenGraph(baseUrl)
        return ncApi.getOpenGraph(
            credentials,
            openGraphLink,
            extractedLinkToPreview
        ).blockingFirst().ocs?.data?.references?.entries?.iterator()?.next()?.value
    }

    override suspend fun unbindRoom(credentials: String, baseUrl: String, roomToken: String): GenericOverall {
        val url = GccApiUtils.getUrlForUnbindingRoom(baseUrl, roomToken)
        return ncApiCoroutines.unbindRoom(credentials, url)
    }

    override suspend fun sendScheduledChatMessage(
        credentials: String,
        url: String,
        message: String,
        displayName: String,
        referenceId: String,
        replyTo: Int?,
        sendWithoutNotification: Boolean,
        threadTitle: String?,
        threadId: Long?,
        sendAt: Int?
    ): ChatOverallSingleMessage =
        ncApiCoroutines.sendScheduleChatMessage(
            credentials,
            url,
            message,
            displayName,
            referenceId,
            replyTo,
            sendWithoutNotification,
            threadTitle,
            threadId,
            sendAt
        )

    override suspend fun updateScheduledMessage(
        credentials: String,
        url: String,
        message: String,
        sendAt: Int?,
        replyTo: Int?,
        sendWithoutNotification: Boolean,
        threadTitle: String?,
        threadId: Long?
    ): ChatOverallSingleMessage =
        ncApiCoroutines.updateScheduledMessage(
            credentials,
            url,
            message,
            sendAt,
            replyTo,
            sendWithoutNotification,
            threadTitle,
            threadId
        )

    override suspend fun deleteScheduledMessage(credentials: String, url: String): GenericOverall =
        ncApiCoroutines.deleteScheduleMessage(credentials, url)

    override suspend fun getScheduledMessages(credentials: String, url: String): ChatOverall =
        ncApiCoroutines.getScheduledMessage(credentials, url)

    override suspend fun pinMessage(credentials: String, url: String, pinUntil: Int): ChatOverallSingleMessage =
        ncApiCoroutines.pinMessage(credentials, url, pinUntil)

    override suspend fun unPinMessage(credentials: String, url: String): ChatOverallSingleMessage =
        ncApiCoroutines.unPinMessage(credentials, url)

    override suspend fun hidePinnedMessage(credentials: String, url: String): GenericOverall =
        ncApiCoroutines.hidePinnedMessage(credentials, url)
}
