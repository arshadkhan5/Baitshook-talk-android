/*
 * Nextcloud Talk - Android Client
 *
 * SPDX-FileCopyrightText: 2024 Marcel Hibbe <dev@mhibbe.de>
 * SPDX-FileCopyrightText: 2022 Andy Scherzinger <info@andy-scherzinger.de>
 * SPDX-FileCopyrightText: 2017-2020 Mario Danic <mario@lovelyhq.com>
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package com.gcc.talk.gccData.user.model

import android.os.Parcelable
import android.util.Log
import com.gcc.talk.gccModels.GccExternalSignalingServer
import com.gcc.talk.gccModels.json.capabilities.Capabilities
import com.gcc.talk.gccModels.json.capabilities.ServerVersion
import com.gcc.talk.gccModels.json.push.PushConfigurationState
import com.gcc.talk.gccUtils.GccApiUtils
import kotlinx.parcelize.Parcelize
import java.lang.Boolean.FALSE

@Parcelize
data class GccUser(
    var id: Long? = null,
    var userId: String? = null,
    var username: String? = null,
    var baseUrl: String? = null,
    var token: String? = null,
    var displayName: String? = null,
    var pushConfigurationState: PushConfigurationState? = null,
    var capabilities: Capabilities? = null,
    var serverVersion: ServerVersion? = null,
    var clientCertificate: String? = null,
    var externalSignalingServer: GccExternalSignalingServer? = null,
    var current: Boolean = FALSE,
    var scheduledForDeletion: Boolean = FALSE
) : Parcelable {

    fun getCredentials(): String = GccApiUtils.getCredentials(username, token)!!

    fun hasSpreedFeatureCapability(capabilityName: String): Boolean {
        if (capabilities == null) {
            Log.e(TAG, "Capabilities are null in hasSpreedFeatureCapability. false is returned for capability check")
        }
        return capabilities?.spreedCapability?.features?.contains(capabilityName) ?: false
    }

    companion object {
        private val TAG = GccUser::class.simpleName
    }
}
