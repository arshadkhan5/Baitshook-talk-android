/*
 * Nextcloud Talk - Android Client
 *
 * SPDX-FileCopyrightText: 2023 Marcel Hibbe <dev@mhibbe.de>
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package com.gcc.talk.gccAdapters.messages

import com.gcc.talk.gccChat.data.model.GccChatMessage

interface GccSystemMessageInterface {
    fun expandSystemMessage(chatMessage: GccChatMessage)
    fun collapseSystemMessages()
}
