/*
 * Nextcloud Talk - Android Client
 *
 * SPDX-FileCopyrightText: 2023 Marcel Hibbe <dev@mhibbe.de>
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package com.gcc.talk.gccChat.data.network

import com.gcc.talk.gccData.user.model.GccUser
import com.gcc.talk.gccModels.domain.GccConversationModel
import com.gcc.talk.gccModels.json.capabilities.SpreedCapability
import com.gcc.talk.gccModels.json.chat.ChatMessageJson
import com.gcc.talk.gccModels.json.chat.ChatOverall
import com.gcc.talk.gccModels.json.chat.ChatOverallSingleMessage
import com.gcc.talk.gccModels.json.conversations.RoomOverall
import com.gcc.talk.gccModels.json.generic.GenericOverall
import com.gcc.talk.gccModels.json.opengraph.Reference
import com.gcc.talk.gccModels.json.reminder.Reminder
import com.gcc.talk.gccModels.json.userAbsence.UserAbsenceOverall
import io.reactivex.Observable
import retrofit2.Response

@Suppress("LongParameterList", "TooManyFunctions")
interface GccChatNetworkDataSource {
    fun getRoom(user: GccUser, roomToken: String): Observable<GccConversationModel>
    fun getCapabilities(user: GccUser, roomToken: String): Observable<SpreedCapability>
    fun joinRoom(user: GccUser, roomToken: String, roomPassword: String): Observable<GccConversationModel>
    fun setReminder(
        user: GccUser,
        roomToken: String,
        messageId: String,
        timeStamp: Int,
        chatApiVersion: Int
    ): Observable<Reminder>

    fun getReminder(user: GccUser, roomToken: String, messageId: String, apiVersion: Int): Observable<Reminder>
    fun deleteReminder(user: GccUser, roomToken: String, messageId: String, apiVersion: Int): Observable<GenericOverall>
    fun shareToNotes(
        credentials: String,
        url: String,
        message: String,
        displayName: String
    ): Observable<ChatOverallSingleMessage>

    suspend fun checkForNoteToSelf(credentials: String, url: String): RoomOverall

    fun shareLocationToNotes(
        credentials: String,
        url: String,
        objectType: String,
        objectId: String,
        metadata: String
    ): Observable<GenericOverall>

    fun leaveRoom(credentials: String, url: String): Observable<GenericOverall>
    suspend fun sendChatMessage(
        credentials: String,
        url: String,
        message: String,
        displayName: String,
        replyTo: Int,
        sendWithoutNotification: Boolean,
        referenceId: String,
        threadTitle: String?
    ): ChatOverallSingleMessage

    fun pullChatMessages(credentials: String, url: String, fieldMap: HashMap<String, Int>): Observable<Response<*>>
    fun deleteChatMessage(credentials: String, url: String): Observable<ChatOverallSingleMessage>
    fun createRoom(credentials: String, url: String, map: Map<String, String>): Observable<RoomOverall>
    fun setChatReadMarker(credentials: String, url: String, previousMessageId: Int): Observable<GenericOverall>
    suspend fun editChatMessage(credentials: String, url: String, text: String): ChatOverallSingleMessage
    suspend fun getOutOfOfficeStatusForUser(credentials: String, baseUrl: String, userId: String): UserAbsenceOverall
    suspend fun getContextForChatMessage(
        credentials: String,
        baseUrl: String,
        token: String,
        messageId: String,
        limit: Int,
        threadId: Int?
    ): List<ChatMessageJson>
    suspend fun getOpenGraph(credentials: String, baseUrl: String, extractedLinkToPreview: String): Reference?
    suspend fun unbindRoom(credentials: String, baseUrl: String, roomToken: String): GenericOverall

    suspend fun sendScheduledChatMessage(
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
    ): ChatOverallSingleMessage

    suspend fun updateScheduledMessage(
        credentials: String,
        url: String,
        message: String,
        sendAt: Int?,
        replyTo: Int?,
        sendWithoutNotification: Boolean,
        threadTitle: String?,
        threadId: Long?
    ): ChatOverallSingleMessage

    suspend fun deleteScheduledMessage(credentials: String, url: String): GenericOverall

    suspend fun getScheduledMessages(credentials: String, url: String): ChatOverall

    suspend fun pinMessage(credentials: String, url: String, pinUntil: Int): ChatOverallSingleMessage

    suspend fun unPinMessage(credentials: String, url: String): ChatOverallSingleMessage

    suspend fun hidePinnedMessage(credentials: String, url: String): GenericOverall
}
