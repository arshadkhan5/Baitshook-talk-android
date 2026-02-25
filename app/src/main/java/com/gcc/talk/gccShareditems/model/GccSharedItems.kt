/*
 * Nextcloud Talk - Android Client
 *
 * SPDX-FileCopyrightText: 2022 Álvaro Brey <alvaro@alvarobrey.com>
 * SPDX-FileCopyrightText: 2022 Tim Krüger <t@timkrueger.me>
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package com.gcc.talk.gccShareditems.model

class GccSharedItems(
    val items: List<GccSharedItem>,
    val type: GccSharedItemType,
    var lastSeenId: Int?,
    var moreItemsExisting: Boolean
)
