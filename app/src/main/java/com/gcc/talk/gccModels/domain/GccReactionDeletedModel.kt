/*
 * Nextcloud Talk - Android Client
 *
 * SPDX-FileCopyrightText: 2022 Marcel Hibbe <dev@mhibbe.de>
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package com.gcc.talk.gccModels.domain

import com.gcc.talk.gccChat.data.model.GccChatMessage

data class GccReactionDeletedModel(var chatMessage: GccChatMessage, var emoji: String, var success: Boolean)
