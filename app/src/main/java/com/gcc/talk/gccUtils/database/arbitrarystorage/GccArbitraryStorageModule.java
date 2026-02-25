/*
 * Nextcloud Talk - Android Client
 *
 * SPDX-FileCopyrightText: 2022 Andy Scherzinger <info@andy-scherzinger.de>
 * SPDX-FileCopyrightText: 2017-2018 Mario Danic <mario@lovelyhq.com>
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package com.gcc.talk.gccUtils.database.arbitrarystorage;

import com.gcc.talk.gccApplication.GccTalkApplication;
import com.gcc.talk.gccArbitrarystorage.GccArbitraryStorageManager;
import com.gcc.talk.gccDagger.modules.GccDatabaseModule;
import com.gcc.talk.gccData.storage.GccArbitraryStoragesRepository;

import javax.inject.Inject;

import autodagger.AutoInjector;
import dagger.Module;
import dagger.Provides;

@Module(includes = GccDatabaseModule.class)
@AutoInjector(GccTalkApplication.class)
public class GccArbitraryStorageModule {

    @Inject
    public GccArbitraryStorageModule() {
    }

    @Provides
    public GccArbitraryStorageManager provideArbitraryStorageManager(GccArbitraryStoragesRepository repository) {
        return new GccArbitraryStorageManager(repository);
    }
}
