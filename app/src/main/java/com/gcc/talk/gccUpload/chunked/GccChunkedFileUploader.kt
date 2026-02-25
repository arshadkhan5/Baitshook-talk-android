/*
 * Nextcloud Talk - Android Client
 *
 * SPDX-FileCopyrightText: 2022 Nextcloud GmbH
 * SPDX-FileCopyrightText: 2015 ownCloud GmbH
 * SPDX-License-Identifier: MIT
 */
package com.gcc.talk.gccUpload.chunked

import android.net.Uri
import android.text.TextUtils
import android.util.Log
import at.bitfire.dav4jvm.DavResource
import at.bitfire.dav4jvm.Property
import at.bitfire.dav4jvm.exception.DavException
import at.bitfire.dav4jvm.exception.HttpException
import at.bitfire.dav4jvm.exception.NotFoundException
import at.bitfire.dav4jvm.property.DisplayName
import at.bitfire.dav4jvm.property.GetContentType
import at.bitfire.dav4jvm.property.GetLastModified
import at.bitfire.dav4jvm.property.ResourceType
import autodagger.AutoInjector
import com.gcc.talk.gccApplication.GccTalkApplication
import com.gcc.talk.gccFilebrowser.models.GccDavResponse
import com.gcc.talk.gccFilebrowser.models.properties.GccNCEncrypted
import com.gcc.talk.gccFilebrowser.models.properties.GccNCPermission
import com.gcc.talk.gccFilebrowser.models.properties.GccNCPreview
import com.gcc.talk.gccFilebrowser.models.properties.GccOCFavorite
import com.gcc.talk.gccFilebrowser.models.properties.GccOCId
import com.gcc.talk.gccFilebrowser.models.properties.GccOCSize
import com.gcc.talk.gccDagger.modules.GccRestModule
import com.gcc.talk.gccData.user.model.GccUser
import com.gcc.talk.gccJobs.GccShareOperationWorker
import com.gcc.talk.gccRemotefilebrowser.model.GccRemoteFileBrowserItem
import com.gcc.talk.gccUtils.GccApiUtils
import com.gcc.talk.gccUtils.GccFileUtils
import com.gcc.talk.gccUtils.GccMimetype
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.MediaType
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.Response
import java.io.File
import java.io.IOException
import java.io.RandomAccessFile
import java.nio.channels.FileChannel
import java.util.Locale

@AutoInjector(GccTalkApplication::class)
class GccChunkedFileUploader(
    okHttpClient: OkHttpClient,
    val currentUser: GccUser,
    val roomToken: String,
    val metaData: String?,
    val listener: GccOnDataTransferProgressListener
) {

    private var okHttpClientNoRedirects: OkHttpClient? = null
    private var remoteChunkUrl: String
    private var uploadFolderUri: String = ""
    private var isUploadAborted = false

    init {
        initHttpClient(okHttpClient, currentUser)
        remoteChunkUrl = GccApiUtils.getUrlForChunkedUpload(currentUser.baseUrl!!, currentUser.userId!!)
    }

    @Suppress("Detekt.TooGenericExceptionCaught")
    fun upload(localFile: File, mimeType: MediaType?, targetPath: String): Boolean {
        try {
            uploadFolderUri = remoteChunkUrl + "/" + GccFileUtils.md5Sum(localFile)
            val davResource = DavResource(
                okHttpClientNoRedirects!!,
                uploadFolderUri.toHttpUrlOrNull()!!
            )

            createFolder(davResource)

            val chunksOnServer: MutableList<GccChunk> = getUploadedChunks(davResource, uploadFolderUri)
            Log.d(TAG, "chunksOnServer: " + chunksOnServer.size)

            val missingChunks: List<GccChunk> = checkMissingChunks(chunksOnServer, localFile.length())
            Log.d(TAG, "missingChunks: " + missingChunks.size)

            for (missingChunk in missingChunks) {
                if (isUploadAborted) return false
                uploadChunk(localFile, uploadFolderUri, mimeType, missingChunk, missingChunk.length())
            }

            assembleChunks(uploadFolderUri, targetPath)
            return true
        } catch (e: Exception) {
            Log.e(TAG, "Something went wrong in GccChunkedFileUploader", e)
            return false
        }
    }

    @Suppress("Detekt.ThrowsCount")
    private fun createFolder(davResource: DavResource) {
        try {
            davResource.mkCol(
                xmlBody = null
            ) { response: Response ->
                if (!response.isSuccessful) {
                    throw IOException("failed to create folder. response code: " + response.code)
                }
            }
        } catch (e: IOException) {
            throw IOException("failed to create folder", e)
        } catch (e: HttpException) {
            if (e.code == METHOD_NOT_ALLOWED_CODE) {
                Log.d(TAG, "Folder most probably already exists, that's okay, just continue..")
            } else {
                throw IOException("failed to create folder", e)
            }
        }
    }

    @Suppress("Detekt.ComplexMethod")
    private fun getUploadedChunks(davResource: DavResource, uploadFolderUri: String): MutableList<GccChunk> {
        val davResponse = GccDavResponse()
        val memberElements: MutableList<at.bitfire.dav4jvm.Response> = ArrayList()
        val rootElement = arrayOfNulls<at.bitfire.dav4jvm.Response>(1)
        val remoteFiles: MutableList<GccRemoteFileBrowserItem> = ArrayList()
        try {
            davResource.propfind(
                1
            ) { response: at.bitfire.dav4jvm.Response, hrefRelation: at.bitfire.dav4jvm.Response.HrefRelation? ->
                davResponse.setResponse(response)
                when (hrefRelation) {
                    at.bitfire.dav4jvm.Response.HrefRelation.MEMBER -> memberElements.add(response)
                    at.bitfire.dav4jvm.Response.HrefRelation.SELF -> rootElement[0] = response
                    at.bitfire.dav4jvm.Response.HrefRelation.OTHER -> {}
                    else -> {}
                }
                Unit
            }
        } catch (e: IOException) {
            throw IOException("Error reading remote path", e)
        } catch (e: DavException) {
            throw IOException("Error reading remote path", e)
        }
        for (memberElement in memberElements) {
            remoteFiles.add(
                getModelFromResponse(
                    memberElement,
                    memberElement
                        .href
                        .toString()
                        .substring(uploadFolderUri.length)
                )
            )
        }

        val chunksOnServer: MutableList<GccChunk> = ArrayList()

        for (remoteFile in remoteFiles) {
            if (!".file".equals(remoteFile.displayName, ignoreCase = true) && remoteFile.isFile) {
                val part: List<String> = remoteFile.displayName!!.split("-")
                chunksOnServer.add(
                    GccChunk(
                        part[0].toLong(),
                        part[1].toLong()
                    )
                )
            }
        }
        return chunksOnServer
    }

    private fun checkMissingChunks(chunks: List<GccChunk>, length: Long): List<GccChunk> {
        val missingChunks: MutableList<GccChunk> = java.util.ArrayList()
        var start: Long = 0
        while (start <= length) {
            val nextChunk: GccChunk? = findNextFittingChunk(chunks, start)
            if (nextChunk == null) {
                // create new chunk
                val end: Long = if (start + CHUNK_SIZE <= length) {
                    start + CHUNK_SIZE - 1
                } else {
                    length
                }
                missingChunks.add(GccChunk(start, end))
                start = end + 1
            } else if (nextChunk.start == start) {
                // go to next
                start += nextChunk.length()
            } else {
                // fill the gap
                missingChunks.add(GccChunk(start, nextChunk.start - 1))
                start = nextChunk.start
            }
        }
        return missingChunks
    }

    private fun findNextFittingChunk(chunks: List<GccChunk>, start: Long): GccChunk? {
        for (chunk in chunks) {
            if (chunk.start >= start && chunk.start - start <= CHUNK_SIZE) {
                return chunk
            }
        }
        return null
    }

    private fun uploadChunk(
        localFile: File,
        uploadFolderUri: String,
        mimeType: MediaType?,
        chunk: GccChunk,
        chunkSize: Long
    ) {
        val startString = java.lang.String.format(Locale.ROOT, "%016d", chunk.start)
        val endString = java.lang.String.format(Locale.ROOT, "%016d", chunk.end)

        var raf: RandomAccessFile? = null
        var channel: FileChannel? = null
        try {
            raf = RandomAccessFile(localFile, "r")
            channel = raf.channel

            // Log.d(TAG, "chunkSize:$chunkSize")
            // Log.d(TAG, "chunk.length():${chunk.length()}")
            // Log.d(TAG, "chunk.start:${chunk.start}")
            // Log.d(TAG, "chunk.end:${chunk.end}")

            val chunkFromFileRequestBody = GccChunkFromFileRequestBody(
                localFile,
                mimeType,
                channel,
                chunkSize,
                chunk.start,
                listener
            )

            val chunkUri = "$uploadFolderUri/$startString-$endString"

            val davResource = DavResource(
                okHttpClientNoRedirects!!,
                chunkUri.toHttpUrlOrNull()!!
            )
            davResource.put(
                chunkFromFileRequestBody
            ) { response: Response ->
                if (!response.isSuccessful) {
                    throw IOException("Failed to upload chunk. response code: " + response.code)
                }
            }
        } finally {
            if (channel != null) {
                try {
                    channel.close()
                } catch (e: IOException) {
                    Log.e(TAG, "Error closing file channel!", e)
                }
            }
            if (raf != null) {
                try {
                    raf.close()
                } catch (e: IOException) {
                    Log.e(TAG, "Error closing file access!", e)
                }
            }
        }
    }

    private fun initHttpClient(okHttpClient: OkHttpClient, currentUser: GccUser) {
        val okHttpClientBuilder: OkHttpClient.Builder = okHttpClient.newBuilder()
        okHttpClientBuilder.followRedirects(false)
        okHttpClientBuilder.followSslRedirects(false)
        // okHttpClientBuilder.readTimeout(Duration.ofMinutes(30)) // TODO set timeout
        okHttpClientBuilder.protocols(listOf(Protocol.HTTP_1_1))
        okHttpClientBuilder.authenticator(
            GccRestModule.HttpAuthenticator(
                GccApiUtils.getCredentials(
                    currentUser.username,
                    currentUser.token
                )!!,
                "Authorization"
            )
        )
        this.okHttpClientNoRedirects = okHttpClientBuilder.build()
    }

    private fun assembleChunks(uploadFolderUri: String, targetPath: String) {
        val destinationUri: String = GccApiUtils.getUrlForFileUpload(
            currentUser.baseUrl!!,
            currentUser.userId!!,
            targetPath
        )
        val originUri = "$uploadFolderUri/.file"

        DavResource(
            okHttpClientNoRedirects!!,
            originUri.toHttpUrlOrNull()!!
        ).move(
            destinationUri.toHttpUrlOrNull()!!,
            true
        ) { response: Response ->
            if (response.isSuccessful) {
                GccShareOperationWorker.shareFile(
                    roomToken,
                    currentUser,
                    targetPath,
                    metaData
                )
            } else {
                throw IOException("Failed to assemble chunks. response code: " + response.code)
            }
        }
    }

    fun abortUpload(onSuccess: () -> Unit) {
        isUploadAborted = true
        try {
            DavResource(
                okHttpClientNoRedirects!!,
                uploadFolderUri.toHttpUrlOrNull()!!
            ).delete { response: Response ->
                when {
                    response.isSuccessful -> onSuccess()
                    else -> isUploadAborted = false
                }
            }
        } catch (e: NotFoundException) {
            Log.i(TAG, "GccChunk upload folder could not be found", e)
            onSuccess()
        }
    }

    private fun getModelFromResponse(response: at.bitfire.dav4jvm.Response, remotePath: String): GccRemoteFileBrowserItem {
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
            remoteFileBrowserItem.mimeType = GccMimetype.FOLDER
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
        private val TAG = GccChunkedFileUploader::class.simpleName
        private const val READ_PERMISSION = "R"
        private const val CHUNK_SIZE: Long = 1024000
        private const val METHOD_NOT_ALLOWED_CODE: Int = 405
    }
}
