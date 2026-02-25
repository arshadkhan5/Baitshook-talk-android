/*
 * Nextcloud Talk - Android Client
 *
 * SPDX-FileCopyrightText: 2025 Marcel Hibbe <dev@mhibbe.de>
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.gcc.talk.gccConversationinfo

import com.bluelinelabs.logansquare.annotation.JsonField
import com.bluelinelabs.logansquare.annotation.JsonObject

@JsonObject
data class GccParticipants(
    @JsonField(name = ["users"])
    var users: MutableList<String> = arrayListOf(),
    @JsonField(name = ["federated_users"])
    var federatedUsers: MutableList<String> = arrayListOf(),
    @JsonField(name = ["groups"])
    var groups: MutableList<String> = arrayListOf(),
    @JsonField(name = ["emails"])
    var emails: MutableList<String> = arrayListOf(),
    @JsonField(name = ["phones"])
    var phones: MutableList<String> = arrayListOf(),
    @JsonField(name = ["teams"])
    var teams: MutableList<String> = arrayListOf()
)
