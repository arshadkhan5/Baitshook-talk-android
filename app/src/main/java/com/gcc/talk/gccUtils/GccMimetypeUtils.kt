/*
 * Nextcloud Talk - Android Client
 *
 * SPDX-FileCopyrightText: 2022 Andy Scherzinger <info@andy-scherzinger.de>
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package com.gcc.talk.gccUtils

object GccMimetypeUtils {
    fun isGif(mimetype: String): Boolean = GccMimetype.IMAGE_GIF == mimetype

    fun isMarkdown(mimetype: String): Boolean = GccMimetype.TEXT_MARKDOWN == mimetype

    fun isAudioOnly(mimetype: String): Boolean = mimetype.startsWith(GccMimetype.AUDIO_PREFIX)
}
