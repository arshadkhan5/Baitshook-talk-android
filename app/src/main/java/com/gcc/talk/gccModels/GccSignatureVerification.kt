/*
 * Nextcloud Talk - Android Client
 *
 * SPDX-FileCopyrightText: 2022 Andy Scherzinger <info@andy-scherzinger.de>
 * SPDX-FileCopyrightText: 2017 Mario Danic <mario@lovelyhq.com>
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package com.gcc.talk.gccModels;

import android.os.Parcelable
import com.gcc.talk.gccData.user.model.GccUser
import kotlinx.parcelize.Parcelize

@Parcelize
data class GccSignatureVerification(var signatureValid: Boolean = false, var user: GccUser? = null) : Parcelable
