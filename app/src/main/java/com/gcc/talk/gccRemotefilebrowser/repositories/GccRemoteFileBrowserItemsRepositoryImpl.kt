/*
 * Nextcloud Talk - Android Client
 *
 * SPDX-FileCopyrightText: 2022 Andy Scherzinger <info@andy-scherzinger.de>
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package com.gcc.talk.gccRemotefilebrowser.repositories

import com.gcc.talk.gccFilebrowser.webdav.GccReadFolderListingOperation
import com.gcc.talk.gccData.user.model.GccUser
import com.gcc.talk.gccRemotefilebrowser.model.GccRemoteFileBrowserItem
import io.reactivex.Observable
import okhttp3.OkHttpClient
import javax.inject.Inject

class GccRemoteFileBrowserItemsRepositoryImpl @Inject constructor(private val okHttpClient: OkHttpClient) :
    GccRemoteFileBrowserItemsRepository {

    override fun listFolder(user: GccUser, path: String): Observable<List<GccRemoteFileBrowserItem>> {
        return Observable.fromCallable {
            val operation =
                GccReadFolderListingOperation(
                    okHttpClient,
                    user,
                    path,
                    1
                )
            val davResponse = operation.readRemotePath()
            if (davResponse.getData() != null) {
                return@fromCallable davResponse.getData() as List<GccRemoteFileBrowserItem>
            }
            return@fromCallable emptyList()
        }
    }
}
