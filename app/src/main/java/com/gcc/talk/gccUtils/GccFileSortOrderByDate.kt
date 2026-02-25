/*
 * Nextcloud Talk - Android Client
 *
 * SPDX-FileCopyrightText: 2021-2022 Andy Scherzinger <info@andy-scherzinger.de>
 * SPDX-FileCopyrightText: 2017 Sven R. Kunze
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package com.gcc.talk.gccUtils

import com.gcc.talk.gccRemotefilebrowser.model.GccRemoteFileBrowserItem
import java.util.Collections

class GccFileSortOrderByDate internal constructor(name: String, ascending: Boolean) : GccFileSortOrder(name, ascending) {
    /**
     * Sorts list by Date.
     *
     * @param files list of files to sort
     */
    override fun sortCloudFiles(files: List<GccRemoteFileBrowserItem>): List<GccRemoteFileBrowserItem> {
        Collections.sort(files, RemoteFileBrowserItemDateComparator(multiplier))
        return super.sortCloudFiles(files)
    }

    /**
     * Comparator for RemoteFileBrowserItems, sorts by modified timestamp.
     */
    class RemoteFileBrowserItemDateComparator(private val multiplier: Int) : Comparator<GccRemoteFileBrowserItem> {

        override fun compare(left: GccRemoteFileBrowserItem, right: GccRemoteFileBrowserItem): Int =
            multiplier * left.modifiedTimestamp.compareTo(right.modifiedTimestamp)
    }
}
