/*
 * Nextcloud Talk - Android Client
 *
 * SPDX-FileCopyrightText: 2024 Marcel Hibbe <dev@mhibbe.de>
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.gcc.talk.gccDagger.modules;

import com.gcc.talk.gccData.database.dao.GccChatBlocksDao
import com.gcc.talk.gccData.database.dao.GccChatMessagesDao
import com.gcc.talk.gccData.database.dao.GccConversationsDao
import com.gcc.talk.gccData.source.local.GccTalkDatabase
import dagger.Module
import dagger.Provides

@Module
internal object GccDaosModule {
    @Provides
    fun providesConversationsDao(database: GccTalkDatabase): GccConversationsDao = database.conversationsDao()

    @Provides
    fun providesChatDao(database: GccTalkDatabase): GccChatMessagesDao = database.chatMessagesDao()

    @Provides
    fun providesChatBlocksDao(database: GccTalkDatabase): GccChatBlocksDao = database.chatBlocksDao()
}
