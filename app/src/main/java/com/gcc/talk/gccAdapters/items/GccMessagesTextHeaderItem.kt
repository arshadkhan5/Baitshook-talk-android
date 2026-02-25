/*
 * Nextcloud Talk - Android Client
 *
 * SPDX-FileCopyrightText: 2022 Álvaro Brey <alvaro@alvarobrey.com>
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package com.gcc.talk.gccAdapters.items

import android.content.Context
import com.gcc.talk.R
import com.gcc.talk.gccUi.theme.ViewThemeUtils

class GccMessagesTextHeaderItem(context: Context, viewThemeUtils: ViewThemeUtils) :
    GccGenericTextHeaderItem(context.getString(R.string.messages), viewThemeUtils) {
    companion object {
        const val VIEW_TYPE = GccFlexibleItemViewType.MESSAGES_TEXT_HEADER_ITEM
    }

    override fun getItemViewType(): Int = VIEW_TYPE
}
