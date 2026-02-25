/*
 * Nextcloud Talk - Android Client
 *
 * SPDX-FileCopyrightText: 2022 Álvaro Brey <alvaro@alvarobrey.com>
 * SPDX-FileCopyrightText: 2022 Nextcloud GmbH
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package com.gcc.talk.gccDagger.modules

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.gcc.talk.gccAccount.viewmodels.GccBrowserLoginActivityViewModel
import com.gcc.talk.gccActivities.GccCallViewModel
import com.gcc.talk.gccChat.viewmodels.GccChatViewModel
import com.gcc.talk.gccChat.viewmodels.GccScheduledMessagesViewModel
import com.gcc.talk.gccChooseaccount.StatusViewModel
import com.gcc.talk.gccContacts.GccContactsViewModel
import com.gcc.talk.gccContextchat.GccContextChatViewModel
import com.gcc.talk.gccConversationcreation.ConversationCreationViewModel
import com.gcc.talk.gccConversationinfo.viewmodel.GccConversationInfoViewModel
import com.gcc.talk.gccConversationinfoedit.viewmodel.GccConversationInfoEditViewModel
import com.gcc.talk.gccConversationlist.viewmodels.GccConversationsListViewModel
import com.gcc.talk.gccDiagnose.DiagnoseViewModel
import com.gcc.talk.gccInvitation.viewmodels.GccInvitationsViewModel
import com.gcc.talk.gccMessagesearch.GccMessageSearchViewModel
import com.gcc.talk.gccOpenconversations.viewmodels.GccOpenConversationsViewModel
import com.gcc.talk.gccPolls.viewmodels.GccPollCreateViewModel
import com.gcc.talk.gccPolls.viewmodels.GccPollMainViewModel
import com.gcc.talk.gccPolls.viewmodels.GccPollResultsViewModel
import com.gcc.talk.gccPolls.viewmodels.GccPollVoteViewModel
import com.gcc.talk.gccRaisehand.viewmodel.GccRaiseHandViewModel
import com.gcc.talk.gccRemotefilebrowser.viewmodels.GccRemoteFileBrowserItemsViewModel
import com.gcc.talk.gccShareditems.viewmodels.GccSharedItemsViewModel
import com.gcc.talk.gccThreadsoverview.viewmodels.GccThreadsOverviewViewModel
import com.gcc.talk.gccTranslate.viewmodels.GccTranslateViewModel
import com.gcc.talk.gccViewmodels.GccCallRecordingViewModel
import dagger.Binds
import dagger.MapKey
import dagger.Module
import dagger.multibindings.IntoMap
import javax.inject.Inject
import javax.inject.Provider
import kotlin.reflect.KClass

class ViewModelFactory @Inject constructor(
    private val viewModels: MutableMap<Class<out ViewModel>, Provider<ViewModel>>
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T = viewModels[modelClass]?.get() as T
}

@Target(AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY_GETTER, AnnotationTarget.PROPERTY_SETTER)
@Retention(AnnotationRetention.RUNTIME)
@MapKey
internal annotation class ViewModelKey(val value: KClass<out ViewModel>)

@Module
@Suppress("TooManyFunctions")
abstract class ViewModelModule {

    @Binds
    abstract fun bindViewModelFactory(factory: ViewModelFactory): ViewModelProvider.Factory

    @Binds
    @IntoMap
    @ViewModelKey(GccSharedItemsViewModel::class)
    abstract fun sharedItemsViewModel(viewModel: GccSharedItemsViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(GccMessageSearchViewModel::class)
    abstract fun messageSearchViewModel(viewModel: GccMessageSearchViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(GccPollMainViewModel::class)
    abstract fun pollViewModel(viewModel: GccPollMainViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(GccPollVoteViewModel::class)
    abstract fun pollVoteViewModel(viewModel: GccPollVoteViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(GccPollResultsViewModel::class)
    abstract fun pollResultsViewModel(viewModel: GccPollResultsViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(GccPollCreateViewModel::class)
    abstract fun pollCreateViewModel(viewModel: GccPollCreateViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(GccRemoteFileBrowserItemsViewModel::class)
    abstract fun remoteFileBrowserItemsViewModel(viewModel: GccRemoteFileBrowserItemsViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(GccCallRecordingViewModel::class)
    abstract fun callRecordingViewModel(viewModel: GccCallRecordingViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(GccRaiseHandViewModel::class)
    abstract fun raiseHandViewModel(viewModel: GccRaiseHandViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(GccTranslateViewModel::class)
    abstract fun translateViewModel(viewModel: GccTranslateViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(GccOpenConversationsViewModel::class)
    abstract fun openConversationsViewModel(viewModel: GccOpenConversationsViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(GccConversationsListViewModel::class)
    abstract fun conversationsListViewModel(viewModel: GccConversationsListViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(GccChatViewModel::class)
    abstract fun chatViewModel(viewModel: GccChatViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(GccConversationInfoViewModel::class)
    abstract fun conversationInfoViewModel(viewModel: GccConversationInfoViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(GccConversationInfoEditViewModel::class)
    abstract fun conversationInfoEditViewModel(viewModel: GccConversationInfoEditViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(GccInvitationsViewModel::class)
    abstract fun invitationsViewModel(viewModel: GccInvitationsViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(GccContactsViewModel::class)
    abstract fun contactsViewModel(viewModel: GccContactsViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(ConversationCreationViewModel::class)
    abstract fun conversationCreationViewModel(viewModel: ConversationCreationViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(DiagnoseViewModel::class)
    abstract fun diagnoseViewModel(viewModel: DiagnoseViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(GccThreadsOverviewViewModel::class)
    abstract fun threadsOverviewViewModel(viewModel: GccThreadsOverviewViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(GccBrowserLoginActivityViewModel::class)
    abstract fun browserLoginActivityViewModel(viewModel: GccBrowserLoginActivityViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(GccContextChatViewModel::class)
    abstract fun contextChatViewModel(viewModel: GccContextChatViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(GccCallViewModel::class)
    abstract fun callViewModel(viewModel: GccCallViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(StatusViewModel::class)
    abstract fun statusRepositoryViewModel(viewModel: StatusViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(GccScheduledMessagesViewModel::class)
    abstract fun scheduledMessagesViewModel(viewModel: GccScheduledMessagesViewModel): ViewModel
}
