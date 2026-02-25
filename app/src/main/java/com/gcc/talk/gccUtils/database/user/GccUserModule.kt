/*
 * Nextcloud Talk - Android Client
 *
 * SPDX-FileCopyrightText: 2022 Andy Scherzinger <info@andy-scherzinger.de>
 * SPDX-FileCopyrightText: 2022 Álvaro Brey <alvaro.brey@nextcloud.com>
 * SPDX-FileCopyrightText: 2017 Mario Danic <mario@lovelyhq.com>
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package com.gcc.talk.gccUtils.database.user

import com.gcc.talk.gccDagger.modules.GccDatabaseModule
import com.gcc.talk.gccData.user.GccUsersRepository
import com.gcc.talk.gccUsers.GccUserManager
import dagger.Binds
import dagger.Module
import dagger.Provides

@Module(includes = [GccDatabaseModule::class])
abstract class GccUserModule {

    @Binds
    abstract fun bindCurrentUserProviderOld(
        currentUserProviderOldImpl: CurrentUserProviderOldImpl
    ): GccCurrentUserProviderOld

    @Binds
    abstract fun bindCurrentUserProvider(currentUserProviderImpl: GccCurrentUserProviderImpl): GccCurrentUserProvider

    companion object {
        @Provides
        fun provideUserManager(userRepository: GccUsersRepository): GccUserManager = GccUserManager(userRepository)
    }
}
