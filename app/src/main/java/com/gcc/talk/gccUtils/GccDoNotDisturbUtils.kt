/*
 * Nextcloud Talk - Android Client
 *
 * SPDX-FileCopyrightText: 2017-2018 Mario Danic <mario@lovelyhq.com>
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package com.gcc.talk.gccUtils

import android.annotation.SuppressLint
import android.app.NotificationManager
import android.content.Context
import android.media.AudioManager
import android.os.Build
import androidx.annotation.VisibleForTesting
import com.gcc.talk.gccApplication.GccTalkApplication

object GccDoNotDisturbUtils {

    private var buildVersion = Build.VERSION.SDK_INT

    @VisibleForTesting
    fun setTestingBuildVersion(version: Int) {
        buildVersion = version
    }

    @SuppressLint("NewApi")
    @JvmOverloads
    fun shouldPlaySound(context: Context? = GccTalkApplication.sharedApplication?.applicationContext): Boolean {
        val notificationManager = context?.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

        var shouldPlaySound = true
        if (buildVersion >= Build.VERSION_CODES.M) {
            if (notificationManager.currentInterruptionFilter != NotificationManager.INTERRUPTION_FILTER_ALL) {
                shouldPlaySound = false
            }
        }

        if (shouldPlaySound) {
            if (audioManager.ringerMode != AudioManager.RINGER_MODE_NORMAL) {
                shouldPlaySound = false
            }
        }

        return shouldPlaySound
    }
}
