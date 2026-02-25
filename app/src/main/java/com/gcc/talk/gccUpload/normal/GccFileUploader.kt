/*
 * Nextcloud Talk - Android Client
 *
 * SPDX-FileCopyrightText: 2022-2024 Marcel Hibbe <dev@mhibbe.de>
 * SPDX-FileCopyrightText: 2023 Andy Scherzinger <info@andy-scherzinger.de>
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package com.gcc.talk.gccUpload.normal

import android.content.Context
import android.net.Uri
import android.util.Log
import at.bitfire.dav4jvm.DavResource
import at.bitfire.dav4jvm.exception.HttpException
import com.gcc.talk.gccApi.GccNcApi
import com.gcc.talk.gccDagger.modules.GccRestModule
import com.gcc.talk.gccData.user.model.GccUser
import com.gcc.talk.gccJobs.GccShareOperationWorker
import com.gcc.talk.gccUtils.GccApiUtils
import com.gcc.talk.gccUtils.GccFileUtils
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.RequestBody
import okhttp3.Response
import java.io.File
import java.io.IOException
import java.io.InputStream

class GccFileUploader(
    okHttpClient: OkHttpClient,
    val context: Context,
    val currentUser: GccUser,
    val roomToken: String,
    val ncApi: GccNcApi,
    val file: File
) {

    private var okHttpClientNoRedirects: OkHttpClient? = null
    private var okhttpClient: OkHttpClient = okHttpClient

    init {
        initHttpClient(okHttpClient, currentUser)
    }

    fun upload(sourceFileUri: Uri, fileName: String, remotePath: String, metaData: String?): Observable<Boolean> =
        ncApi.uploadFile(
            GccApiUtils.getCredentials(
                currentUser.username,
                currentUser.token
            ),
            GccApiUtils.getUrlForFileUpload(
                currentUser.baseUrl!!,
                currentUser.userId!!,
                remotePath
            ),
            createRequestBody(sourceFileUri)
        )
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .flatMap { response ->
                if (response.isSuccessful) {
                    GccShareOperationWorker.shareFile(
                        roomToken,
                        currentUser,
                        remotePath,
                        metaData
                    )
                    GccFileUtils.copyFileToCache(context, sourceFileUri, fileName)
                    Observable.just(true)
                } else {
                    if (response.code() == HTTP_CODE_NOT_FOUND ||
                        response.code() == HTTP_CODE_CONFLICT
                    ) {
                        createDavResource(sourceFileUri, fileName, remotePath, metaData)
                    } else {
                        Observable.just(false)
                    }
                }
            }

    private fun createDavResource(
        sourceFileUri: Uri,
        fileName: String,
        remotePath: String,
        metaData: String?
    ): Observable<Boolean> =
        Observable.fromCallable {
            val userFileUploadPath = GccApiUtils.userFileUploadPath(
                currentUser.baseUrl!!,
                currentUser.userId!!
            )
            val userTalkAttachmentsUploadPath = GccApiUtils.userTalkAttachmentsUploadPath(
                currentUser.baseUrl!!,
                currentUser.userId!!
            )

            var davResource = DavResource(
                okHttpClientNoRedirects!!,
                userFileUploadPath.toHttpUrlOrNull()!!
            )
            createFolder(davResource)
            initHttpClient(okHttpClient = okhttpClient, currentUser)
            davResource = DavResource(
                okHttpClientNoRedirects!!,
                userTalkAttachmentsUploadPath.toHttpUrlOrNull()!!
            )
            createFolder(davResource)
            true
        }
            .subscribeOn(Schedulers.io())
            .flatMap { upload(sourceFileUri, fileName, remotePath, metaData) }

    @Suppress("Detekt.TooGenericExceptionCaught")
    private fun createRequestBody(sourceFileUri: Uri): RequestBody? {
        var requestBody: RequestBody? = null
        try {
            val input: InputStream = context.contentResolver.openInputStream(sourceFileUri)!!
            input.use {
                val buf = ByteArray(input.available())
                while (it.read(buf) != -1) {
                    requestBody = RequestBody.create("application/octet-stream".toMediaTypeOrNull(), buf)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "failed to create RequestBody for $sourceFileUri", e)
        }
        return requestBody
    }

    private fun initHttpClient(okHttpClient: OkHttpClient, currentUser: GccUser) {
        val okHttpClientBuilder: OkHttpClient.Builder = okHttpClient.newBuilder()
        okHttpClientBuilder.followRedirects(false)
        okHttpClientBuilder.followSslRedirects(false)
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

    companion object {
        private val TAG = GccFileUploader::class.simpleName
        private const val METHOD_NOT_ALLOWED_CODE: Int = 405
        private const val HTTP_CODE_NOT_FOUND: Int = 404
        private const val HTTP_CODE_CONFLICT: Int = 409
    }
}
