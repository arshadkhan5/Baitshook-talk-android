/*
 * Nextcloud Talk - Android Client
 *
 * SPDX-FileCopyrightText: 2022 Andy Scherzinger <info@andy-scherzinger.de>
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package com.gcc.talk.gccRemotefilebrowser.repositories

import com.gcc.talk.gccData.user.model.GccUser
import com.gcc.talk.gccRemotefilebrowser.model.GccRemoteFileBrowserItem
import io.reactivex.Observable

interface GccRemoteFileBrowserItemsRepository {

    fun listFolder(user: GccUser, path: String): Observable<List<GccRemoteFileBrowserItem>>
}
