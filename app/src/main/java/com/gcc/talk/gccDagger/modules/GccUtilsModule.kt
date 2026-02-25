/*
 * Nextcloud Talk - Android Client
 *
 * SPDX-FileCopyrightText: 2022 Álvaro Brey <alvaro@alvarobrey.com>
 * SPDX-FileCopyrightText: 2022 Nextcloud GmbH
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package com.gcc.talk.gccDagger.modules

import android.content.Context
import com.gcc.talk.gccUtilss.GccDateUtils
import com.gcc.talk.gccUtils.message.GccMessageUtils
import com.gcc.talk.gccUtils.permissions.GccPlatformPermissionUtil
import com.gcc.talk.gccUtils.permissions.GccPlatformPermissionUtilImpl
import dagger.Module
import dagger.Provides
import dagger.Reusable

@Module(includes = [GccContextModule::class])
class GccUtilsModule {
    @Provides
    @Reusable
    fun providePermissionUtil(context: Context): GccPlatformPermissionUtil = GccPlatformPermissionUtilImpl(context)

    @Provides
    @Reusable
    fun provideDateUtils(context: Context): GccDateUtils = GccDateUtils(context)

    @Provides
    @Reusable
    fun provideMessageUtils(context: Context): GccMessageUtils = GccMessageUtils(context)
}
