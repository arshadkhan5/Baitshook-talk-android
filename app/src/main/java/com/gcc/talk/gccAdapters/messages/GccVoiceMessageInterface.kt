/*
 * Nextcloud Talk - Android Client
 *
 * SPDX-FileCopyrightText: 2024 Christian Reiner <foss@christian-reiner.info>
 * SPDX-FileCopyrightText: 2021 Marcel Hibbe <dev@mhibbe.de>
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package com.gcc.talk.gccAdapters.messages

import com.gcc.talk.gccChat.data.model.GccChatMessage
import com.gcc.talk.gccUi.PlaybackSpeed

interface GccVoiceMessageInterface {
    fun updateMediaPlayerProgressBySlider(message: GccChatMessage, progress: Int)
    fun registerMessageToObservePlaybackSpeedPreferences(userId: String, listener: (speed: PlaybackSpeed) -> Unit)
}
