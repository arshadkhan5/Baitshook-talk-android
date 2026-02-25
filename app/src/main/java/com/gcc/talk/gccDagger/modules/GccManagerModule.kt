/*
 * Nextcloud Talk - Android Client
 *
 * SPDX-FileCopyrightText: 2024 Julius Linus <juliuslinus1@gmail.com>
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.gcc.talk.gccDagger.modules

import android.content.Context
import com.gcc.talk.gccChat.data.io.GccAudioFocusRequestManager
import com.gcc.talk.gccChat.data.io.GccAudioRecorderManager
import com.gcc.talk.gccChat.data.io.GccMediaPlayerManager
import com.gcc.talk.gccChat.data.io.GccMediaRecorderManager
import com.gcc.talk.gccUtils.preferences.GccAppPreferences
import dagger.Module
import dagger.Provides

@Module
class GccManagerModule {

    @Provides
    fun provideMediaRecorderManager(): GccMediaRecorderManager = GccMediaRecorderManager()

    @Provides
    fun provideAudioRecorderManager(): GccAudioRecorderManager = GccAudioRecorderManager()

    @Provides
    fun provideMediaPlayerManager(preferences: GccAppPreferences): GccMediaPlayerManager =
        GccMediaPlayerManager().apply {
            appPreferences = preferences
        }

    @Provides
    fun provideAudioFocusManager(context: Context): GccAudioFocusRequestManager = GccAudioFocusRequestManager(context)
}
