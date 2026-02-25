/*
 * Nextcloud Talk - Android Client
 *
 * SPDX-FileCopyrightText: 2017 Mario Danic <mario@lovelyhq.com>
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package com.gcc.talk.gccDagger.modules;

import android.content.Context;

import androidx.annotation.NonNull;
import dagger.Module;
import dagger.Provides;

@Module
public class GccContextModule {
    private final Context context;

    public GccContextModule(@NonNull final Context context) {
        this.context = context;
    }

    @Provides
    public Context provideContext() {
        return context;
    }
}
