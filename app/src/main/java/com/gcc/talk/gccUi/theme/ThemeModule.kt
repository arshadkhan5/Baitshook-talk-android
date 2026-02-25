/*
 * Nextcloud Talk - Android Client
 *
 * SPDX-FileCopyrightText: 2022 Álvaro Brey <alvaro@alvarobrey.com>
 * SPDX-FileCopyrightText: 2022 Nextcloud GmbH
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package com.gcc.talk.gccUi.theme

import com.nextcloud.android.common.ui.theme.MaterialSchemes
import com.gcc.talk.gccDagger.modules.GccContextModule
import com.gcc.talk.gccUtils.database.user.GccUserModule
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.Reusable

@Module(includes = [GccContextModule::class, GccUserModule::class])
internal abstract class ThemeModule {

    @Binds
    @Reusable
    abstract fun bindMaterialSchemesProvider(provider: MaterialSchemesProviderImpl): MaterialSchemesProvider

    companion object {
        @Provides
        fun provideCurrentMaterialSchemes(schemesProvider: MaterialSchemesProvider): MaterialSchemes =
            schemesProvider.getMaterialSchemesForCurrentUser()
    }
}
