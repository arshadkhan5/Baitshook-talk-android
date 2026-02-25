/*
 * Nextcloud Talk - Android Client
 *
 * SPDX-FileCopyrightText: 2022 Álvaro Brey <alvaro@alvarobrey.com>
 * SPDX-FileCopyrightText: 2022 Tim Krüger <t@timkrueger.me>
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package com.gcc.talk.gccShareditems.model

data class GccSharedFileItem(
    override val id: String,
    override val name: String,
    override val actorId: String,
    override val actorName: String,
    override val dateTime: String,
    val fileSize: Long,
    val path: String,
    val link: String,
    val mimeType: String,
    val previewAvailable: Boolean = false,
    val previewLink: String
) : GccSharedItem
