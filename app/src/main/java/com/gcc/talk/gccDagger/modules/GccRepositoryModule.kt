/*
 * Nextcloud Talk - Android Client
 *
 * SPDX-FileCopyrightText: 2022-2024 Marcel Hibbe <dev@mhibbe.de>
 * SPDX-FileCopyrightText: 2022 Andy Scherzinger <info@andy-scherzinger.de>
 * SPDX-FileCopyrightText: 2022 Álvaro Brey <alvaro@alvarobrey.com>
 * SPDX-FileCopyrightText: 2022 Nextcloud GmbH
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package com.gcc.talk.gccDagger.modules

import android.content.Context
import com.gcc.talk.gccAccount.data.GccLoginRepository
import com.gcc.talk.gccAccount.data.io.GccLocalLoginDataSource
import com.gcc.talk.gccAccount.data.network.GccNetworkLoginDataSource
import com.gcc.talk.gccApi.GccNcApi
import com.gcc.talk.gccApi.GccNcApiCoroutines
import com.gcc.talk.gccChat.data.GccChatMessageRepository
import com.gcc.talk.gccChat.data.network.GccChatNetworkDataSource
import com.gcc.talk.gccChat.data.network.GccOfflineFirstChatRepository
import com.gcc.talk.gccChat.data.network.GccRetrofitChatNetwork
import com.gcc.talk.gccChooseaccount.GccStatusRepository
import com.gcc.talk.gccChooseaccount.GccStatusRepositoryImplementation
import com.gcc.talk.gccContacts.GccContactsRepository
import com.gcc.talk.gccContacts.GccContactsRepositoryImpl
import com.gcc.talk.gccConversationcreation.GccConversationCreationRepository
import com.gcc.talk.gccConversationcreation.GccConversationCreationRepositoryImpl
import com.gcc.talk.gccConversationinfoedit.data.GccConversationInfoEditRepository
import com.gcc.talk.gccConversationinfoedit.data.GccConversationInfoEditRepositoryImpl
import com.gcc.talk.gccConversationlist.data.GccOfflineConversationsRepository
import com.gcc.talk.gccConversationlist.data.network.GccConversationsNetworkDataSource
import com.gcc.talk.gccConversationlist.data.network.GccOfflineFirstConversationsRepository
import com.gcc.talk.gccConversationlist.data.network.GccRetrofitConversationsNetwork
import com.gcc.talk.gccData.database.dao.GccChatBlocksDao
import com.gcc.talk.gccData.database.dao.GccChatMessagesDao
import com.gcc.talk.gccData.database.dao.GccConversationsDao
import com.gcc.talk.gccData.network.GccNetworkMonitor
import com.gcc.talk.gccData.source.local.GccTalkDatabase
import com.gcc.talk.gccData.storage.GccArbitraryStoragesRepository
import com.gcc.talk.gccData.storage.GccArbitraryStoragesRepositoryImpl
import com.gcc.talk.gccData.user.GccUsersRepository
import com.gcc.talk.gccData.user.GccUsersRepositoryImpl
import com.gcc.talk.gccInvitation.data.GccInvitationsRepository
import com.gcc.talk.gccInvitation.data.GccInvitationsRepositoryImpl
import com.gcc.talk.gccOpenconversations.data.GccOpenConversationsRepository
import com.gcc.talk.gccOpenconversations.data.GccOpenConversationsRepositoryImpl
import com.gcc.talk.gccPolls.repositories.GccPollRepository
import com.gcc.talk.gccPolls.repositories.GccPollRepositoryImpl
import com.gcc.talk.gccRaisehand.GccRequestAssistanceRepository
import com.gcc.talk.gccRaisehand.GccRequestAssistanceRepositoryImpl
import com.gcc.talk.gccRemotefilebrowser.repositories.GccRemoteFileBrowserItemsRepository
import com.gcc.talk.gccRemotefilebrowser.repositories.GccRemoteFileBrowserItemsRepositoryImpl
import com.gcc.talk.gccRepositories.callrecording.GccCallRecordingRepository
import com.gcc.talk.gccRepositories.callrecording.CallRecordingRepositoryImpl
import com.gcc.talk.gccRepositories.conversations.GccConversationsRepository
import com.gcc.talk.gccRepositories.conversations.GccConversationsRepositoryImpl
import com.gcc.talk.gccRepositories.reactions.GccReactionsRepository
import com.gcc.talk.gccRepositories.reactions.GccReactionsRepositoryImpl
import com.gcc.talk.gccRepositories.unifiedsearch.GccUnifiedSearchRepository
import com.gcc.talk.gccRepositories.unifiedsearch.GccUnifiedSearchRepositoryImpl
import com.gcc.talk.gccShareditems.repositories.GccSharedItemsRepository
import com.gcc.talk.gccShareditems.repositories.GccSharedItemsRepositoryImpl
import com.gcc.talk.gccThreadsoverview.data.GccThreadsRepository
import com.gcc.talk.gccThreadsoverview.data.GccThreadsRepositoryImpl
import com.gcc.talk.gccTranslate.repositories.GccTranslateRepository
import com.gcc.talk.gccTranslate.repositories.GccTranslateRepositoryImpl
import com.gcc.talk.gccUsers.GccUserManager
import com.gcc.talk.gccUtilss.GccDateUtils
import com.gcc.talk.gccUtils.preferences.GccAppPreferences
import dagger.Module
import dagger.Provides
import okhttp3.OkHttpClient

@Suppress("TooManyFunctions")
@Module
class GccRepositoryModule {

    @Provides
    fun provideConversationsRepository(ncApi: GccNcApi, ncApiCoroutines: GccNcApiCoroutines): GccConversationsRepository =
        GccConversationsRepositoryImpl(ncApi, ncApiCoroutines)

    @Provides
    fun provideSharedItemsRepository(ncApi: GccNcApi, dateUtils: GccDateUtils): GccSharedItemsRepository =
        GccSharedItemsRepositoryImpl(ncApi, dateUtils)

    @Provides
    fun provideUnifiedSearchRepository(ncApi: GccNcApi): GccUnifiedSearchRepository = GccUnifiedSearchRepositoryImpl(ncApi)

    @Provides
    fun provideDialogPollRepository(ncApi: GccNcApi): GccPollRepository = GccPollRepositoryImpl(ncApi)

    @Provides
    fun provideRemoteFileBrowserItemsRepository(okHttpClient: OkHttpClient): GccRemoteFileBrowserItemsRepository =
        GccRemoteFileBrowserItemsRepositoryImpl(okHttpClient)

    @Provides
    fun provideUsersRepository(database: GccTalkDatabase): GccUsersRepository = GccUsersRepositoryImpl(database.usersDao())

    @Provides
    fun provideArbitraryStoragesRepository(database: GccTalkDatabase): GccArbitraryStoragesRepository =
        GccArbitraryStoragesRepositoryImpl(database.arbitraryStoragesDao())

    @Provides
    fun provideReactionsRepository(ncApi: GccNcApi, dao: GccChatMessagesDao): GccReactionsRepository =
        GccReactionsRepositoryImpl(ncApi, dao)

    @Provides
    fun provideCallRecordingRepository(ncApi: GccNcApi): GccCallRecordingRepository = CallRecordingRepositoryImpl(ncApi)

    @Provides
    fun provideRequestAssistanceRepository(ncApi: GccNcApi): GccRequestAssistanceRepository =
        GccRequestAssistanceRepositoryImpl(ncApi)

    @Provides
    fun provideOpenConversationsRepository(ncApiCoroutines: GccNcApiCoroutines): GccOpenConversationsRepository =
        GccOpenConversationsRepositoryImpl(ncApiCoroutines)

    @Provides
    fun translateRepository(ncApi: GccNcApi): GccTranslateRepository = GccTranslateRepositoryImpl(ncApi)

    @Provides
    fun provideChatNetworkDataSource(ncApi: GccNcApi, ncApiCoroutines: GccNcApiCoroutines): GccChatNetworkDataSource =
        GccRetrofitChatNetwork(ncApi, ncApiCoroutines)

    @Provides
    fun provideConversationsNetworkDataSource(ncApi: GccNcApi): GccConversationsNetworkDataSource =
        GccRetrofitConversationsNetwork(ncApi)

    @Provides
    fun provideConversationInfoEditRepository(
        ncApi: GccNcApi,
        ncApiCoroutines: GccNcApiCoroutines
    ): GccConversationInfoEditRepository = GccConversationInfoEditRepositoryImpl(ncApi, ncApiCoroutines)

    @Provides
    fun provideInvitationsRepository(ncApi: GccNcApi, ncApiCoroutines: GccNcApiCoroutines): GccInvitationsRepository =
        GccInvitationsRepositoryImpl(ncApi, ncApiCoroutines)

    @Provides
    fun provideOfflineFirstChatRepository(
        chatMessagesDao: GccChatMessagesDao,
        chatBlocksDao: GccChatBlocksDao,
        dataSource: GccChatNetworkDataSource,
        networkMonitor: GccNetworkMonitor
    ): GccChatMessageRepository =
        GccOfflineFirstChatRepository(
            chatMessagesDao,
            chatBlocksDao,
            dataSource,
            networkMonitor
        )

    @Provides
    fun provideOfflineFirstConversationsRepository(
        dao: GccConversationsDao,
        dataSource: GccConversationsNetworkDataSource,
        chatNetworkDataSource: GccChatNetworkDataSource,
        networkMonitor: GccNetworkMonitor
    ): GccOfflineConversationsRepository =
        GccOfflineFirstConversationsRepository(
            dao,
            dataSource,
            chatNetworkDataSource,
            networkMonitor
        )

    @Provides
    fun provideContactsRepository(ncApiCoroutines: GccNcApiCoroutines): GccContactsRepository =
        GccContactsRepositoryImpl(ncApiCoroutines)

    @Provides
    fun provideConversationCreationRepository(ncApiCoroutines: GccNcApiCoroutines): GccConversationCreationRepository =
        GccConversationCreationRepositoryImpl(ncApiCoroutines)

    @Provides
    fun provideThreadsRepository(ncApiCoroutines: GccNcApiCoroutines): GccThreadsRepository =
        GccThreadsRepositoryImpl(ncApiCoroutines)

    @Provides
    fun provideNetworkDataSource(okHttpClient: OkHttpClient): GccNetworkLoginDataSource =
        GccNetworkLoginDataSource(okHttpClient)

    @Provides
    fun providesLocalDataSource(
        userManager: GccUserManager,
        appPreferences: GccAppPreferences,
        context: Context
    ): GccLocalLoginDataSource = GccLocalLoginDataSource(userManager, appPreferences, context)

    @Provides
    fun provideLoginRepository(
        networkLoginDataSource: GccNetworkLoginDataSource,
        localLoginDataSource: GccLocalLoginDataSource
    ): GccLoginRepository = GccLoginRepository(networkLoginDataSource, localLoginDataSource)

    @Provides
    fun provideStatusRepository(ncApiCoroutines: GccNcApiCoroutines): GccStatusRepository =
        GccStatusRepositoryImplementation(ncApiCoroutines)
}
