/*
 * Nextcloud Talk - Android Client
 *
 * SPDX-FileCopyrightText: 2022 Andy Scherzinger <info@andy-scherzinger.de>
 * SPDX-FileCopyrightText: 2017 Mario Danic <mario@lovelyhq.com>
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package com.gcc.talk.gccDagger.modules;

import android.content.Context;

import com.gcc.talk.gccData.network.GccNetworkMonitor;
import com.gcc.talk.gccData.network.GccNetworkMonitorImpl;
import com.gcc.talk.gccData.source.local.GccTalkDatabase;
import com.gcc.talk.gccUtils.preferences.GccAppPreferences;
import com.gcc.talk.gccUtils.preferences.GccAppPreferencesImpl;

import javax.inject.Singleton;

import androidx.annotation.NonNull;
import androidx.annotation.OptIn;
import dagger.Module;
import dagger.Provides;
import kotlinx.coroutines.ExperimentalCoroutinesApi;

@Module
@OptIn(markerClass = ExperimentalCoroutinesApi.class)
public class GccDatabaseModule {
    @Provides
    @Singleton
    public GccAppPreferences providePreferences(@NonNull final Context poContext) {
        GccAppPreferences preferences = new GccAppPreferencesImpl(poContext);
        preferences.removeLinkPreviews();
        return preferences;
    }

    @Provides
    @Singleton
    public GccAppPreferencesImpl providePreferencesImpl(@NonNull final Context poContext) {
        return new GccAppPreferencesImpl(poContext);
    }

    @Provides
    @Singleton
    public GccTalkDatabase provideTalkDatabase(@NonNull final Context context) {
        return GccTalkDatabase.getInstance(context);
    }

    @Provides
    @Singleton
    public GccNetworkMonitor provideNetworkMonitor(@NonNull final Context poContext) {
        return new GccNetworkMonitorImpl(poContext);
    }
}
