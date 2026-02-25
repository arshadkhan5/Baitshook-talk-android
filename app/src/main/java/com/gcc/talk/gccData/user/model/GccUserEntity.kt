/*
 * Nextcloud Talk - Android Client
 *
 * SPDX-FileCopyrightText: 2022 Andy Scherzinger <info@andy-scherzinger.de>
 * SPDX-FileCopyrightText: 2017-2020 Mario Danic <mario@lovelyhq.com>
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package com.gcc.talk.gccData.user.model

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.gcc.talk.gccModels.GccExternalSignalingServer
import com.gcc.talk.gccModels.json.capabilities.Capabilities
import com.gcc.talk.gccModels.json.capabilities.ServerVersion
import com.gcc.talk.gccModels.json.push.PushConfigurationState
import kotlinx.parcelize.Parcelize
import java.lang.Boolean.FALSE

@Parcelize
@Entity(tableName = "GccUser")
data class GccUserEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    var id: Long = 0,

    @ColumnInfo(name = "userId")
    var userId: String? = null,

    @ColumnInfo(name = "username")
    var username: String? = null,

    @ColumnInfo(name = "baseUrl")
    var baseUrl: String? = null,

    @ColumnInfo(name = "token")
    var token: String? = null,

    @ColumnInfo(name = "displayName")
    var displayName: String? = null,

    @ColumnInfo(name = "pushConfigurationState")
    var pushConfigurationState: PushConfigurationState? = null,

    @ColumnInfo(name = "capabilities")
    var capabilities: Capabilities? = null,

    @ColumnInfo(name = "serverVersion", defaultValue = "")
    var serverVersion: ServerVersion? = null,

    @ColumnInfo(name = "clientCertificate")
    var clientCertificate: String? = null,

    @ColumnInfo(name = "externalSignalingServer")
    var externalSignalingServer: GccExternalSignalingServer? = null,

    @ColumnInfo(name = "current")
    var current: Boolean = FALSE,

    @ColumnInfo(name = "scheduledForDeletion")
    var scheduledForDeletion: Boolean = FALSE
) : Parcelable
