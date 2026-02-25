/*
 * Nextcloud Talk - Android Client
 *
 * SPDX-FileCopyrightText: 2022 Andy Scherzinger <info@andy-scherzinger.de>
 * SPDX-FileCopyrightText: 2017-2019 Mario Danic <mario@lovelyhq.com>
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package com.gcc.talk.gccFilebrowser.webdav

import android.net.Uri
import android.text.TextUtils
import android.util.Log
import at.bitfire.dav4jvm.DavResource
import at.bitfire.dav4jvm.Property
import at.bitfire.dav4jvm.Response
import at.bitfire.dav4jvm.Response.HrefRelation
import at.bitfire.dav4jvm.exception.DavException
import at.bitfire.dav4jvm.property.DisplayName
import at.bitfire.dav4jvm.property.GetContentType
import at.bitfire.dav4jvm.property.GetLastModified
import at.bitfire.dav4jvm.property.ResourceType
import com.gcc.talk.gccFilebrowser.models.GccDavResponse
import com.gcc.talk.gccFilebrowser.models.properties.GccNCEncrypted
import com.gcc.talk.gccFilebrowser.models.properties.GccNCPermission
import com.gcc.talk.gccFilebrowser.models.properties.GccNCPreview
import com.gcc.talk.gccFilebrowser.models.properties.GccOCFavorite
import com.gcc.talk.gccFilebrowser.models.properties.GccOCId
import com.gcc.talk.gccFilebrowser.models.properties.GccOCSize
import com.gcc.talk.gccDagger.modules.GccRestModule.HttpAuthenticator
import com.gcc.talk.gccData.user.model.GccUser
import com.gcc.talk.gccRemotefilebrowser.model.GccRemoteFileBrowserItem
import com.gcc.talk.gccUtils.GccApiUtils
import com.gcc.talk.gccUtils.GccMimetype.FOLDER
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.OkHttpClient
import java.io.File
import java.io.IOException

class GccReadFolderListingOperation(okHttpClient: OkHttpClient, currentUser: GccUser, path: String, depth: Int) {
    private val okHttpClient: OkHttpClient
    private val url: String
    private val depth: Int
    private val basePath: String

    init {
        val okHttpClientBuilder: OkHttpClient.Builder = okHttpClient.newBuilder()
        okHttpClientBuilder.followRedirects(false)
        okHttpClientBuilder.followSslRedirects(false)
        okHttpClientBuilder.authenticator(
            HttpAuthenticator(
                GccApiUtils.getCredentials(
                    currentUser.username,
                    currentUser.token
                )!!,
                "Authorization"
            )
        )
        this.okHttpClient = okHttpClientBuilder.build()
        basePath = currentUser.baseUrl + GccDavUtils.DAV_PATH + currentUser.userId
        url = basePath + path
        this.depth = depth
    }

    fun readRemotePath(): GccDavResponse {
        val davResponse = GccDavResponse()
        val memberElements: MutableList<Response> = ArrayList()
        val rootElement = arrayOfNulls<Response>(1)
        val remoteFiles: MutableList<GccRemoteFileBrowserItem> = ArrayList()
        try {
            DavResource(
                okHttpClient,
                url.toHttpUrlOrNull()!!
            ).propfind(
                depth = depth,
                reqProp = GccDavUtils.getAllPropSet()
            ) { response: Response, hrefRelation: HrefRelation? ->
                davResponse.setResponse(response)
                when (hrefRelation) {
                    HrefRelation.MEMBER -> memberElements.add(response)
                    HrefRelation.SELF -> rootElement[0] = response
                    HrefRelation.OTHER -> {}
                    else -> {}
                }
                Unit
            }
        } catch (e: IOException) {
            Log.w(TAG, "Error reading remote path")
        } catch (e: DavException) {
            Log.w(TAG, "Error reading remote path")
        }
        for (memberElement in memberElements) {
            remoteFiles.add(
                getModelFromResponse(
                    memberElement,
                    memberElement
                        .href
                        .toString()
                        .substring(basePath.length)
                )
            )
        }
        davResponse.setData(remoteFiles)
        return davResponse
    }

    private fun getModelFromResponse(response: Response, remotePath: String): GccRemoteFileBrowserItem {
        val remoteFileBrowserItem = GccRemoteFileBrowserItem()
        remoteFileBrowserItem.path = Uri.decode(remotePath)
        remoteFileBrowserItem.displayName = Uri.decode(File(remotePath).name)
        val properties = response.properties
        for (property in properties) {
            mapPropertyToBrowserFile(property, remoteFileBrowserItem)
        }
        if (remoteFileBrowserItem.permissions != null &&
            remoteFileBrowserItem.permissions!!.contains(READ_PERMISSION)
        ) {
            remoteFileBrowserItem.isAllowedToReShare = true
        }
        if (TextUtils.isEmpty(remoteFileBrowserItem.mimeType) && !remoteFileBrowserItem.isFile) {
            remoteFileBrowserItem.mimeType = FOLDER
        }

        return remoteFileBrowserItem
    }

    @Suppress("Detekt.ComplexMethod")
    private fun mapPropertyToBrowserFile(property: Property, remoteFileBrowserItem: GccRemoteFileBrowserItem) {
        when (property) {
            is GccOCId -> {
                remoteFileBrowserItem.remoteId = property.ocId
            }
            is ResourceType -> {
                remoteFileBrowserItem.isFile = !property.types.contains(ResourceType.COLLECTION)
            }
            is GetLastModified -> {
                remoteFileBrowserItem.modifiedTimestamp = property.lastModified
            }
            is GetContentType -> {
                remoteFileBrowserItem.mimeType = property.type
            }
            is GccOCSize -> {
                remoteFileBrowserItem.size = property.ocSize
            }
            is GccNCPreview -> {
                remoteFileBrowserItem.hasPreview = property.isNcPreview
            }
            is GccOCFavorite -> {
                remoteFileBrowserItem.isFavorite = property.isOcFavorite
            }
            is DisplayName -> {
                remoteFileBrowserItem.displayName = property.displayName
            }
            is GccNCEncrypted -> {
                remoteFileBrowserItem.isEncrypted = property.isNcEncrypted
            }
            is GccNCPermission -> {
                remoteFileBrowserItem.permissions = property.ncPermission
            }
        }
    }

    companion object {
        private const val TAG = "GccReadFilesystemOperation"
        private const val READ_PERMISSION = "R"
    }
}
