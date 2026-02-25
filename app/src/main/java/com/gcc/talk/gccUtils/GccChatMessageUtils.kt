/*
 * Nextcloud Talk - Android Client
 *
 * SPDX-FileCopyrightText: 2024 Marcel Hibbe <dev@mhibbe.de>
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package com.gcc.talk.gccUtils

import android.view.View
import android.widget.ImageView
import com.gcc.talk.gccChat.data.model.GccChatMessage
import com.gcc.talk.gccExtensions.loadBotsAvatar
import com.gcc.talk.gccExtensions.loadChangelogBotAvatar
import com.gcc.talk.gccExtensions.loadDefaultAvatar
import com.gcc.talk.gccExtensions.loadFederatedUserAvatar
import com.gcc.talk.gccExtensions.loadFirstLetterAvatar
import com.gcc.talk.gccUi.theme.ViewThemeUtils

class GccChatMessageUtils {

    fun setAvatarOnMessage(view: ImageView, message: GccChatMessage, viewThemeUtils: ViewThemeUtils) {
        view.visibility = View.VISIBLE
        if (message.actorType == "guests" || message.actorType == "emails") {
            val actorName = message.actorDisplayName
            if (!actorName.isNullOrBlank()) {
                view.loadFirstLetterAvatar(actorName)
            } else {
                view.loadDefaultAvatar(viewThemeUtils)
            }
        } else if (message.actorType == "bots" && (message.actorId == "changelog" || message.actorId == "sample")) {
            view.loadChangelogBotAvatar()
        } else if (message.actorType == "bots") {
            view.loadBotsAvatar()
        } else if (message.actorType == "federated_users") {
            view.loadFederatedUserAvatar(message)
        }
    }
}
