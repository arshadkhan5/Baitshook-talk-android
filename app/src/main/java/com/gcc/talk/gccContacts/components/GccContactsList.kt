/*
 * Nextcloud Talk - Android Client
 *
 * SPDX-FileCopyrightText: 2024 Sowjanya Kota <sowjanya.kch@gmail.com>
 * SPDX-FileCopyrightText: 2025 Marcel Hibbe <dev@mhibbe.de>
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.gcc.talk.gccContacts.components

import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.gcc.talk.gccContacts.CompanionClass
import com.gcc.talk.gccContacts.GccContactsViewModel

@Composable
fun ContactsList(contactsUiState: GccContactsViewModel.ContactsUiState, contactsViewModel: GccContactsViewModel) {
    val context = LocalContext.current
    when (contactsUiState) {
        is GccContactsViewModel.ContactsUiState.None -> {
        }

        is GccContactsViewModel.ContactsUiState.Loading -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }

        is GccContactsViewModel.ContactsUiState.Success -> {
            val contacts = contactsUiState.contacts
            Log.d(CompanionClass.TAG, "Contacts:$contacts")
            if (contacts != null) {
                ContactsItem(contacts, contactsViewModel, context)
            }
        }

        is GccContactsViewModel.ContactsUiState.Error -> {
            val errorMessage = contactsUiState.message
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "Error: $errorMessage", color = Color.Red)
            }
        }
    }
}
