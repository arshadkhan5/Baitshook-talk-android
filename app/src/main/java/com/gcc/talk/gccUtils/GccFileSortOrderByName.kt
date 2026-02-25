/*
 * Nextcloud Talk - Android Client
 *
 * SPDX-FileCopyrightText: 2021-2022 Andy Scherzinger <info@andy-scherzinger.de>
 * SPDX-FileCopyrightText: 2017 Sven R. Kunze
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package com.gcc.talk.gccUtils

import com.gcc.talk.gccRemotefilebrowser.model.GccRemoteFileBrowserItem
import third.parties.daveKoeller.AlphanumComparator
import java.util.Collections

class GccFileSortOrderByName internal constructor(name: String, ascending: Boolean) : GccFileSortOrder(name, ascending) {
    /**
     * Sorts list by Name.
     *
     * @param files files to sort
     */
    override fun sortCloudFiles(files: List<GccRemoteFileBrowserItem>): List<GccRemoteFileBrowserItem> {
        Collections.sort(files, RemoteFileBrowserItemNameComparator(multiplier))
        return super.sortCloudFiles(files)
    }

    /**
     * Comparator for RemoteFileBrowserItems, sorts by name.
     */
    class RemoteFileBrowserItemNameComparator(private val multiplier: Int) : Comparator<GccRemoteFileBrowserItem> {
        private val alphanumComparator =
            AlphanumComparator<GccRemoteFileBrowserItem>()

        override fun compare(left: GccRemoteFileBrowserItem, right: GccRemoteFileBrowserItem): Int {
            return if (!left.isFile && !right.isFile) {
                return multiplier * alphanumComparator.compare(left.path, right.path)
            } else if (!left.isFile) {
                -1
            } else if (!right.isFile) {
                1
            } else {
                multiplier * alphanumComparator.compare(left.path, right.path)
            }
        }
    }
}
