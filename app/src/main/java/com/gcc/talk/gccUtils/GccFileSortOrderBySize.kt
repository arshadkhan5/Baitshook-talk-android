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

class GccFileSortOrderBySize internal constructor(name: String, ascending: Boolean) : GccFileSortOrder(name, ascending) {
    /**
     * Sorts list by Size.
     *
     * @param files list of files to sort
     */
    override fun sortCloudFiles(files: List<GccRemoteFileBrowserItem>): List<GccRemoteFileBrowserItem> {
        Collections.sort(files, RemoteFileBrowserItemSizeComparator(multiplier))
        return super.sortCloudFiles(files)
    }

    /**
     * Comparator for RemoteFileBrowserItems, sorts by name.
     */
    class RemoteFileBrowserItemSizeComparator(private val multiplier: Int) : Comparator<GccRemoteFileBrowserItem> {

        override fun compare(left: GccRemoteFileBrowserItem, right: GccRemoteFileBrowserItem): Int {
            return if (!left.isFile && !right.isFile) {
                return multiplier * left.size.compareTo(right.size)
            } else if (!left.isFile) {
                -1
            } else if (!right.isFile) {
                1
            } else {
                multiplier * left.size.compareTo(right.size)
            }
        }
    }
}
