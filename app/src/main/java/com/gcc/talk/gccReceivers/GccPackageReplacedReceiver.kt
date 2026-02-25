/*
 * Nextcloud Talk - Android Client
 *
 * SPDX-FileCopyrightText: 2017-2018 Mario Danic <mario@lovelyhq.com>
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package com.gcc.talk.gccReceivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.gcc.talk.gccUtils.GccNotificationUtils

class GccPackageReplacedReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        if (intent != null &&
            intent.action != null &&
            intent.action == "android.intent.action.MY_PACKAGE_REPLACED"
        ) {
            GccNotificationUtils.removeOldNotificationChannels(context)
        }
    }
}
