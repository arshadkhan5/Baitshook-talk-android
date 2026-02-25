/*
 * Nextcloud Talk - Android Client
 *
 * SPDX-FileCopyrightText: 2022 Marcel Hibbe <dev@mhibbe.de>
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package com.gcc.talk.gccAdapters.messages

import com.gcc.talk.gccChat.data.model.GccChatMessage

interface GccPreviewMessageInterface {
    fun onPreviewMessageLongClick(chatMessage: GccChatMessage)
}
