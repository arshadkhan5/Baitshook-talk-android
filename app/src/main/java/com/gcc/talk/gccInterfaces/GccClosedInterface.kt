/*
 * Nextcloud Talk - Android Client
 *
 * SPDX-FileCopyrightText: 2022 Marcel Hibbe <dev@mhibbe.de>
 * SPDX-FileCopyrightText: 2017-2018 Mario Danic <mario@lovelyhq.com>
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package com.gcc.talk.gccInterfaces

interface GccClosedInterface {

    val isGooglePlayServicesAvailable: Boolean
    fun providerInstallerInstallIfNeededAsync()
    fun setUpPushTokenRegistration()
}
