/*
 * Nextcloud Talk - Android Client
 *
 * SPDX-FileCopyrightText: 2022 Álvaro Brey <alvaro@alvarobrey.com>
 * SPDX-FileCopyrightText: 2022 Tim Krüger <t@timkrueger.me>
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package com.gcc.talk.gccShareditems.adapters

import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import com.gcc.talk.gccData.user.model.GccUser
import com.gcc.talk.databinding.SharedItemGridBinding
import com.gcc.talk.gccUi.theme.ViewThemeUtils

class GccSharedItemsGridViewHolder(
    override val binding: SharedItemGridBinding,
    user: GccUser,
    viewThemeUtils: ViewThemeUtils
) : GccSharedItemsViewHolder(binding, user, viewThemeUtils) {

    override val image: ImageView
        get() = binding.image
    override val clickTarget: View
        get() = binding.image
    override val progressBar: ProgressBar
        get() = binding.progressBar
}
