/*
 * Nextcloud Talk - Android Client
 *
 * SPDX-FileCopyrightText: 2024 Sowjanya Kota <sowjanya.kch@gmail.com>
 * SPDX-FileCopyrightText: 2025 Marcel Hibbe <dev@mhibbe.de>
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.gcc.talk.gccContacts

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.remember
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import autodagger.AutoInjector
import com.gcc.talk.gccActivities.GccBaseActivity
import com.gcc.talk.gccApplication.GccTalkApplication
import com.gcc.talk.gccComponents.ColoredStatusBar
import com.gcc.talk.gccContacts.CompanionClass.Companion.KEY_HIDE_ALREADY_EXISTING_PARTICIPANTS
import com.gcc.talk.gccExtensions.getParcelableArrayListExtraProvider
import com.gcc.talk.gccModels.json.autocomplete.AutocompleteUser
import com.gcc.talk.gccUtils.bundle.GccBundleKeys
import javax.inject.Inject

@AutoInjector(GccTalkApplication::class)
class ContactsActivity : GccBaseActivity() {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    private lateinit var contactsViewModel: GccContactsViewModel

    @SuppressLint("UnrememberedMutableState")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        GccTalkApplication.sharedApplication!!.componentApplication.inject(this)
        contactsViewModel = ViewModelProvider(this, viewModelFactory)[GccContactsViewModel::class.java]
        setContent {
            val isAddParticipants = intent.getBooleanExtra(GccBundleKeys.KEY_ADD_PARTICIPANTS, false)
            val hideAlreadyAddedParticipants = intent.getBooleanExtra(KEY_HIDE_ALREADY_EXISTING_PARTICIPANTS, false)
            contactsViewModel.updateIsAddParticipants(isAddParticipants)
            contactsViewModel.hideAlreadyAddedParticipants(hideAlreadyAddedParticipants)
            if (isAddParticipants) {
                contactsViewModel.updateShareTypes(
                    listOf(
                        GccShareType.Group.shareType,
                        GccShareType.Email.shareType,
                        GccShareType.Circle.shareType
                    )
                )
                contactsViewModel.getContactsFromSearchParams()
            }
            val colorScheme = viewThemeUtils.getColorScheme(this)
            val uiState = contactsViewModel.contactsViewState.collectAsStateWithLifecycle()

            val selectedParticipants = remember {
                intent?.getParcelableArrayListExtraProvider<AutocompleteUser>("selectedParticipants")
                    ?: emptyList()
            }.toSet().toMutableList()
            contactsViewModel.updateSelectedParticipants(selectedParticipants)

            MaterialTheme(
                colorScheme = colorScheme
            ) {
                ColoredStatusBar()
                ContactsScreen(
                    contactsViewModel = contactsViewModel,
                    uiState = uiState.value
                )
            }
        }
    }
}

class CompanionClass {
    companion object {
        internal val TAG = ContactsActivity::class.simpleName
        internal const val ROOM_TYPE_ONE_ONE = "1"
        const val KEY_HIDE_ALREADY_EXISTING_PARTICIPANTS: String = "KEY_HIDE_ALREADY_EXISTING_PARTICIPANTS"
    }
}
