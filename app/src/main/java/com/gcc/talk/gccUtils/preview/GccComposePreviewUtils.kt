/*
 * Nextcloud Talk - Android Client
 *
 * SPDX-FileCopyrightText: 2025 Julius Linus <juliuslinus1@gmail.com>
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.gcc.talk.gccUtils.preview

import android.content.Context
import com.github.aurae.retrofit2.LoganSquareConverterFactory
import com.nextcloud.android.common.ui.color.ColorUtil
import com.nextcloud.android.common.ui.theme.MaterialSchemes
import com.nextcloud.android.common.ui.theme.utils.AndroidViewThemeUtils
import com.nextcloud.android.common.ui.theme.utils.AndroidXViewThemeUtils
import com.nextcloud.android.common.ui.theme.utils.DialogViewThemeUtils
import com.nextcloud.android.common.ui.theme.utils.MaterialViewThemeUtils
import com.gcc.talk.gccApi.GccNcApi
import com.gcc.talk.gccApi.GccNcApiCoroutines
import com.gcc.talk.gccChat.data.GccChatMessageRepository
import com.gcc.talk.gccChat.data.io.GccAudioFocusRequestManager
import com.gcc.talk.gccChat.data.io.GccMediaRecorderManager
import com.gcc.talk.gccChat.data.network.GccChatNetworkDataSource
import com.gcc.talk.gccChat.data.network.GccOfflineFirstChatRepository
import com.gcc.talk.gccChat.data.network.GccRetrofitChatNetwork
import com.gcc.talk.gccChat.viewmodels.GccChatViewModel
import com.gcc.talk.gccContacts.GccContactsRepository
import com.gcc.talk.gccContacts.GccContactsRepositoryImpl
import com.gcc.talk.gccContacts.GccContactsViewModel
import com.gcc.talk.gccConversationlist.data.GccOfflineConversationsRepository
import com.gcc.talk.gccConversationlist.data.network.GccConversationsNetworkDataSource
import com.gcc.talk.gccConversationlist.data.network.GccOfflineFirstConversationsRepository
import com.gcc.talk.gccConversationlist.data.network.GccRetrofitConversationsNetwork
import com.gcc.talk.gccData.database.dao.GccChatBlocksDao
import com.gcc.talk.gccData.database.dao.GccChatMessagesDao
import com.gcc.talk.gccData.database.dao.GccConversationsDao
import com.gcc.talk.gccData.network.GccNetworkMonitor
import com.gcc.talk.gccData.network.GccNetworkMonitorImpl
import com.gcc.talk.gccData.user.GccUsersDao
import com.gcc.talk.gccData.user.GccUsersRepository
import com.gcc.talk.gccData.user.GccUsersRepositoryImpl
import com.gcc.talk.gccRepositories.reactions.GccReactionsRepository
import com.gcc.talk.gccRepositories.reactions.GccReactionsRepositoryImpl
import com.gcc.talk.gccThreadsoverview.data.GccThreadsRepository
import com.gcc.talk.gccThreadsoverview.data.GccThreadsRepositoryImpl
import com.gcc.talk.gccUi.theme.MaterialSchemesProviderImpl
import com.gcc.talk.gccUi.theme.TalkSpecificViewThemeUtils
import com.gcc.talk.gccUi.theme.ViewThemeUtils
import com.gcc.talk.gccUsers.GccUserManager
import com.gcc.talk.gccUtils.database.user.CurrentUserProviderOldImpl
import com.gcc.talk.gccUtils.database.user.GccCurrentUserProviderOld
import com.gcc.talk.gccUtils.message.GccMessageUtils
import com.gcc.talk.gccUtils.preferences.GccAppPreferences
import com.gcc.talk.gccUtils.preferences.GccAppPreferencesImpl
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory

/**
 * TODO - basically a reimplementation of common dependencies for use in Previewing Advanced Compose Views
 * It's a hard coded Dependency Injector
 *
 */
class GccComposePreviewUtils private constructor(context: Context) {
    private val mContext = context

    companion object {
        fun getInstance(context: Context) = GccComposePreviewUtils(context)
        val TAG: String = GccComposePreviewUtils::class.java.simpleName
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val appPreferences: GccAppPreferences
        get() = GccAppPreferencesImpl(mContext)

    val context: Context = mContext

    val userRepository: GccUsersRepository
        get() = GccUsersRepositoryImpl(usersDao)

    val userManager: GccUserManager
        get() = GccUserManager(userRepository)

    val userProvider: GccCurrentUserProviderOld
        get() = CurrentUserProviderOldImpl(userManager)

    val colorUtil: ColorUtil
        get() = ColorUtil(mContext)

    val materialScheme: MaterialSchemes
        get() = MaterialSchemesProviderImpl(userProvider, colorUtil).getMaterialSchemesForCurrentUser()

    val viewThemeUtils: ViewThemeUtils
        get() {
            val android = AndroidViewThemeUtils(materialScheme, colorUtil)
            val material = MaterialViewThemeUtils(materialScheme, colorUtil)
            val androidx = AndroidXViewThemeUtils(materialScheme, android)
            val talk = TalkSpecificViewThemeUtils(materialScheme, androidx)
            val dialog = DialogViewThemeUtils(materialScheme)
            return ViewThemeUtils(materialScheme, android, material, androidx, talk, dialog)
        }

    val messageUtils: GccMessageUtils
        get() = GccMessageUtils(mContext)

    val retrofit: Retrofit
        get() {
            val retrofitBuilder = Retrofit.Builder()
                .client(OkHttpClient.Builder().build())
                .baseUrl("https://nextcloud.com")
                .addCallAdapterFactory(RxJava2CallAdapterFactory.createWithScheduler(Schedulers.io()))
                .addConverterFactory(LoganSquareConverterFactory.create())

            return retrofitBuilder.build()
        }

    val ncApi: GccNcApi
        get() = retrofit.create(GccNcApi::class.java)

    val ncApiCoroutines: GccNcApiCoroutines
        get() = retrofit.create(GccNcApiCoroutines::class.java)

    val chatNetworkDataSource: GccChatNetworkDataSource
        get() = GccRetrofitChatNetwork(ncApi, ncApiCoroutines)

    val usersDao: GccUsersDao
        get() = DummyUserDaoImpl()

    val chatMessagesDao: GccChatMessagesDao
        get() = DummyChatMessagesDaoImpl()

    val chatBlocksDao: GccChatBlocksDao
        get() = DummyChatBlocksDaoImpl()

    val conversationsDao: GccConversationsDao
        get() = DummyConversationDaoImpl()

    val networkMonitor: GccNetworkMonitor
        get() = GccNetworkMonitorImpl(mContext)

    val chatRepository: GccChatMessageRepository
        get() = GccOfflineFirstChatRepository(
            chatMessagesDao,
            chatBlocksDao,
            chatNetworkDataSource,
            networkMonitor
        )

    val threadsRepository: GccThreadsRepository
        get() = GccThreadsRepositoryImpl(ncApiCoroutines)

    val conversationNetworkDataSource: GccConversationsNetworkDataSource
        get() = GccRetrofitConversationsNetwork(ncApi)

    val conversationRepository: GccOfflineConversationsRepository
        get() = GccOfflineFirstConversationsRepository(
            conversationsDao,
            conversationNetworkDataSource,
            chatNetworkDataSource,
            networkMonitor
        )

    val reactionsRepository: GccReactionsRepository
        get() = GccReactionsRepositoryImpl(ncApi, chatMessagesDao)

    val mediaRecorderManager: GccMediaRecorderManager
        get() = GccMediaRecorderManager()

    val audioFocusRequestManager: GccAudioFocusRequestManager
        get() = GccAudioFocusRequestManager(mContext)

    val chatViewModel: GccChatViewModel
        get() = GccChatViewModel(
            appPreferences,
            chatNetworkDataSource,
            chatRepository,
            threadsRepository,
            conversationRepository,
            reactionsRepository,
            mediaRecorderManager,
            audioFocusRequestManager
        )

    val contactsRepository: GccContactsRepository
        get() = GccContactsRepositoryImpl(ncApiCoroutines)

    val contactsViewModel: GccContactsViewModel
        get() = GccContactsViewModel(contactsRepository, userProvider)
}
