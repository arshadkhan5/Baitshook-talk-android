/*
 * Nextcloud Talk - Android Client
 *
 * SPDX-FileCopyrightText: 2022 Marcel Hibbe <dev@mhibbe.de>
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package com.gcc.talk.gccPolls.adapters

import com.gcc.talk.gccAdapters.items.GccFlexibleItemViewType
import com.gcc.talk.gccPolls.model.GccPollDetails

data class GccPollResultVotersOverviewItem(val detailsList: List<GccPollDetails>) : GccPollResultItem {

    override fun getViewType(): Int = VIEW_TYPE

    companion object {
        // layout is used as view type for uniqueness
        const val VIEW_TYPE = GccFlexibleItemViewType.POLL_RESULT_VOTERS_OVERVIEW_ITEM
    }
}
