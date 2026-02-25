/*
 * Nextcloud Talk - Android Client
 *
 * SPDX-FileCopyrightText: 2022 Marcel Hibbe <dev@mhibbe.de>
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package com.gcc.talk.gccPolls.repositories.model

import android.os.Parcelable
import com.bluelinelabs.logansquare.annotation.JsonField
import com.bluelinelabs.logansquare.annotation.JsonObject
import com.gcc.talk.gccModels.json.converters.EnumActorTypeConverter
import com.gcc.talk.gccModels.json.participants.Participant
import kotlinx.parcelize.Parcelize

@Parcelize
@JsonObject
data class GccPollDetailsResponse(
    @JsonField(name = ["actorType"], typeConverter = EnumActorTypeConverter::class)
    var actorType: Participant.ActorType? = null,

    @JsonField(name = ["actorId"])
    var actorId: String,

    @JsonField(name = ["actorDisplayName"])
    var actorDisplayName: String,

    @JsonField(name = ["optionId"])
    var optionId: Int
) : Parcelable {
    // This constructor is added to work with the 'com.bluelinelabs.logansquare.annotation.JsonObject'
    constructor() : this(null, "", "", 0)
}
