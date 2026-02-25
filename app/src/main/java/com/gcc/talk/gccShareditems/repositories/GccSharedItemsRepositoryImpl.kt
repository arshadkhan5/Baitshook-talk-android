/*
 * Nextcloud Talk - Android Client
 *
 * SPDX-FileCopyrightText: 2022 Álvaro Brey <alvaro@alvarobrey.com>
 * SPDX-FileCopyrightText: 2022 Tim Krüger <t@timkrueger.me>
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package com.gcc.talk.gccShareditems.repositories

import android.util.Log
import androidx.core.net.toUri
import com.gcc.talk.R
import com.gcc.talk.gccApi.GccNcApi
import com.gcc.talk.gccApplication.GccTalkApplication.Companion.sharedApplication
import com.gcc.talk.gccModels.json.chat.ChatShareOverall
import com.gcc.talk.gccShareditems.model.GccSharedDeckCardItem
import com.gcc.talk.gccShareditems.model.GccSharedFileItem
import com.gcc.talk.gccShareditems.model.GccSharedItem
import com.gcc.talk.gccShareditems.model.GccSharedItemType
import com.gcc.talk.gccShareditems.model.GccSharedItems
import com.gcc.talk.gccShareditems.model.GccSharedLocationItem
import com.gcc.talk.gccShareditems.model.GccSharedOtherItem
import com.gcc.talk.gccShareditems.model.GccSharedPinnedItem
import com.gcc.talk.gccShareditems.model.GccSharedPollItem
import com.gcc.talk.gccUtils.GccApiUtils
import com.gcc.talk.gccUtils.GccDateConstants
import com.gcc.talk.gccUtilss.GccDateUtils
import io.reactivex.Observable
import retrofit2.Response
import java.util.Locale
import javax.inject.Inject

class GccSharedItemsRepositoryImpl @Inject constructor(private val ncApi: GccNcApi, private val dateUtils: GccDateUtils) :
    GccSharedItemsRepository {

    override fun media(parameters: GccSharedItemsRepository.Parameters, type: GccSharedItemType): Observable<GccSharedItems>? =
        media(parameters, type, null)

    override fun media(
        parameters: GccSharedItemsRepository.Parameters,
        type: GccSharedItemType,
        lastKnownMessageId: Int?
    ): Observable<GccSharedItems>? {
        val credentials = GccApiUtils.getCredentials(parameters.userName, parameters.userToken)

        return ncApi.getSharedItems(
            credentials,
            GccApiUtils.getUrlForChatSharedItems(1, parameters.baseUrl, parameters.roomToken),
            type.toString().lowercase(Locale.ROOT),
            lastKnownMessageId,
            BATCH_SIZE
        ).map { map(it, parameters, type) }
    }

    private fun map(
        response: Response<ChatShareOverall>,
        parameters: GccSharedItemsRepository.Parameters,
        type: GccSharedItemType
    ): GccSharedItems {
        var chatLastGiven: Int? = null
        val items = mutableMapOf<String, GccSharedItem>()

        if (response.headers()["x-chat-last-given"] != null) {
            chatLastGiven = response.headers()["x-chat-last-given"]!!.toInt()
        }

        val mediaItems = response.body()!!.ocs!!.data
        if (mediaItems != null) {
            for (it in mediaItems) {
                val metaData = it.value.metaData
                val dateTime = dateUtils.getLocalDateTimeStringFromTimestamp(
                    it.value.timestamp * GccDateConstants.SECOND_DIVIDER
                )

                if (metaData != null) {
                    val fileParameters = it.value.messageParameters?.get("file")
                    val message = it.value.message!!
                    val name = fileParameters?.get("name") ?: message

                    val sharedItem = GccSharedPinnedItem(
                        it.value.id.toString(),
                        name,
                        it.value.actorId!!,
                        metaData.pinnedActorDisplayName!!,
                        dateTime
                    )
                    items[it.value.id.toString()] = sharedItem
                } else if (it.value.messageParameters?.containsKey("file") == true) {
                    val fileParameters = it.value.messageParameters!!["file"]!!
                    val actorParameters = it.value.messageParameters!!["actor"]!!

                    val previewAvailable =
                        "yes".equals(fileParameters["preview-available"]!!, ignoreCase = true)

                    items[it.value.id.toString()] = GccSharedFileItem(
                        fileParameters["id"]!!,
                        fileParameters["name"]!!,
                        actorParameters["id"]!!,
                        actorParameters["name"]!!,
                        dateTime,
                        fileParameters["size"]!!.toLong(),
                        fileParameters["path"]!!,
                        fileParameters["link"]!!,
                        fileParameters["mimetype"]!!,
                        previewAvailable,
                        previewLink(fileParameters["id"], parameters.baseUrl!!)
                    )
                } else if (it.value.messageParameters?.containsKey("object") == true) {
                    val objectParameters = it.value.messageParameters!!["object"]!!
                    val actorParameters = it.value.messageParameters!!["actor"]!!
                    items[it.value.id.toString()] = itemFromObject(objectParameters, actorParameters, dateTime)
                } else {
                    Log.w(TAG, "Item contains neither 'file' or 'object'.")
                }
            }
        }

        val sortedMutableItems = items.toSortedMap().values.toList().reversed().toMutableList()
        val moreItemsExisting = items.count() == BATCH_SIZE

        return GccSharedItems(
            sortedMutableItems,
            type,
            chatLastGiven,
            moreItemsExisting
        )
    }

    private fun itemFromObject(
        objectParameters: HashMap<String?, String?>,
        actorParameters: HashMap<String?, String?>,
        dateTime: String
    ): GccSharedItem {
        val returnValue: GccSharedItem
        when (objectParameters["type"]) {
            "talk-poll" -> {
                returnValue = GccSharedPollItem(
                    objectParameters["id"]!!,
                    objectParameters["name"]!!,
                    actorParameters["id"]!!,
                    actorParameters["name"]!!,
                    dateTime
                )
            }

            "geo-location" -> {
                returnValue = GccSharedLocationItem(
                    objectParameters["id"]!!,
                    objectParameters["name"]!!,
                    actorParameters["id"]!!,
                    actorParameters["name"]!!,
                    dateTime,
                    objectParameters["id"]!!.replace("geo:", "geo:0,0?z=11&q=").toUri()
                )
            }

            "deck-card" -> {
                returnValue = GccSharedDeckCardItem(
                    objectParameters["id"]!!,
                    objectParameters["name"]!!,
                    actorParameters["id"]!!,
                    actorParameters["name"]!!,
                    dateTime,
                    objectParameters["link"]!!.toUri()
                )
            }

            else -> {
                returnValue = GccSharedOtherItem(
                    objectParameters["id"]!!,
                    objectParameters["name"]!!,
                    actorParameters["id"]!!,
                    actorParameters["name"]!!,
                    dateTime
                )
            }
        }
        return returnValue
    }

    override fun availableTypes(parameters: GccSharedItemsRepository.Parameters): Observable<Set<GccSharedItemType>> {
        val credentials = GccApiUtils.getCredentials(parameters.userName, parameters.userToken)

        return ncApi.getSharedItemsOverview(
            credentials,
            GccApiUtils.getUrlForChatSharedItemsOverview(1, parameters.baseUrl, parameters.roomToken),
            1
        ).map {
            val types = mutableSetOf<GccSharedItemType>()

            if (it.code() == HTTP_OK) {
                val typeMap = it.body()!!.ocs!!.data!!
                for (t in typeMap) {
                    if (t.value.isNotEmpty()) {
                        try {
                            types += GccSharedItemType.typeFor(t.key)
                        } catch (e: IllegalArgumentException) {
                            Log.w(TAG, "Server responds an unknown shared item type: ${t.key}")
                        }
                    }
                }
            } else {
                Log.e(TAG, "Failed to getSharedItemsOverview")
            }

            types.toSet()
        }
    }

    private fun previewLink(fileId: String?, baseUrl: String): String =
        GccApiUtils.getUrlForFilePreviewWithFileId(
            baseUrl,
            fileId!!,
            sharedApplication!!.resources.getDimensionPixelSize(R.dimen.maximum_file_preview_size)
        )

    companion object {
        const val BATCH_SIZE: Int = 28
        private const val HTTP_OK: Int = 200
        private val TAG = GccSharedItemsRepositoryImpl::class.simpleName
    }
}
