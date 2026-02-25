/*
 * Nextcloud Talk - Android Client
 *
 * SPDX-FileCopyrightText: 2022 Tim Krüger <t@timkrueger.me>
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package com.gcc.talk.gccShareditems.model

interface GccSharedItem {
    val id: String
    val name: String
    val actorId: String
    val actorName: String
    val dateTime: String
}
