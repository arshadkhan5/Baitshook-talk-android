/*
 * Nextcloud Talk - Android Client
 *
 * SPDX-FileCopyrightText: 2023 Marcel Hibbe <dev@mhibbe.de>
 * SPDX-FileCopyrightText: 2022 Álvaro Brey <alvaro@alvarobrey.com>
 * SPDX-FileCopyrightText: 2022 Tim Krüger <t@timkrueger.me>
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package com.gcc.talk.gccShareditems.model

import java.util.Locale

enum class GccSharedItemType {

    AUDIO,
    FILE,
    MEDIA,
    RECORDING,
    VOICE,
    LOCATION,
    DECKCARD,
    OTHER,
    PINNED,
    POLL;

    companion object {
        fun typeFor(name: String) = valueOf(name.uppercase(Locale.ROOT))
    }
}
