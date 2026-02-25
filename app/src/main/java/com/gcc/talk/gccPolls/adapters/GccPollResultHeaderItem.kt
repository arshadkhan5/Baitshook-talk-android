/*
 * Nextcloud Talk - Android Client
 *
 * SPDX-FileCopyrightText: 2022 Marcel Hibbe <dev@mhibbe.de>
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package com.gcc.talk.gccPolls.adapters
import com.gcc.talk.gccAdapters.items.GccFlexibleItemViewType

data class GccPollResultHeaderItem(val name: String, val percent: Int, val selfVoted: Boolean) : GccPollResultItem {

    override fun getViewType(): Int = VIEW_TYPE

    companion object {
        const val VIEW_TYPE = GccFlexibleItemViewType.POLL_RESULT_HEADER_ITEM
    }
}
