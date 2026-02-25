/*
 * Nextcloud Talk - Android Client
 *
 * SPDX-FileCopyrightText: 2024 Sowjanya Kota <sowjanya.kch@gmail.com>
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.gcc.talk.gccContacts

import com.gcc.talk.gccContacts.apiService.FakeItem
import com.gcc.talk.gccContacts.repository.FakeRepositoryError
import com.gcc.talk.gccContacts.repository.FakeRepositorySuccess
import com.gcc.talk.gccData.user.GccUsersDao
import com.gcc.talk.gccData.user.GccUsersRepository
import com.gcc.talk.gccData.user.GccUsersRepositoryImpl
import com.gcc.talk.gccUsers.GccUserManager
import com.gcc.talk.gccUtils.database.user.GccCurrentUserProviderOld
import com.gcc.talk.gccUtils.database.user.CurrentUserProviderOldImpl
import com.gcc.talk.gccUtils.preview.DummyUserDaoImpl
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ContactsViewModelTest {
    private lateinit var viewModel: GccContactsViewModel
    private val repository: GccContactsRepository = FakeRepositorySuccess()

    val dispatcher: TestDispatcher = UnconfinedTestDispatcher()

    val usersDao: GccUsersDao
        get() = DummyUserDaoImpl()

    val userRepository: GccUsersRepository
        get() = GccUsersRepositoryImpl(usersDao)

    val userManager: GccUserManager
        get() = GccUserManager(userRepository)

    val userProvider: GccCurrentUserProviderOld
        get() = CurrentUserProviderOldImpl(userManager)

    @Before
    fun setup() {
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Before
    fun setUp() {
        viewModel = GccContactsViewModel(repository, userProvider)
    }

    @Test
    fun `fetch contacts`() =
        runTest {
            viewModel = GccContactsViewModel(repository, userProvider)
            viewModel.getContactsFromSearchParams()
            assert(viewModel.contactsViewState.value is GccContactsViewModel.ContactsUiState.Success)
            val successState = viewModel.contactsViewState.value as GccContactsViewModel.ContactsUiState.Success
            assert(successState.contacts == FakeItem.contacts)
        }

    @Test
    fun `test error contacts state`() =
        runTest {
            viewModel = GccContactsViewModel(FakeRepositoryError(), userProvider)
            assert(viewModel.contactsViewState.value is GccContactsViewModel.ContactsUiState.Error)
            val errorState = viewModel.contactsViewState.value as GccContactsViewModel.ContactsUiState.Error
            assert(errorState.message == "unable to fetch contacts")
        }

    @Test
    fun `update search query`() {
        viewModel.updateSearchQuery("Ma")
        assert(viewModel.searchQuery.value == "Ma")
    }

    @Test
    fun `initial search query is empty string`() {
        viewModel.updateSearchQuery("")
        assert(viewModel.searchQuery.value == "")
    }

    @Test
    fun `initial shareType is User`() {
        assert(viewModel.shareTypeList.contains(GccShareType.User.shareType))
    }

    @Test
    fun `update shareTypes`() {
        viewModel.updateShareTypes(listOf(GccShareType.Group.shareType))
        assert(viewModel.shareTypeList.contains(GccShareType.Group.shareType))
    }

    @Test
    fun `initial room state is none`() =
        runTest {
            assert(viewModel.roomViewState.value is GccContactsViewModel.RoomUiState.None)
        }

    @Test
    fun `test success room state`() =
        runTest {
            viewModel.createRoom("1", "users", "s@gmail.com", null)
            assert(viewModel.roomViewState.value is GccContactsViewModel.RoomUiState.Success)
            val successState = viewModel.roomViewState.value as GccContactsViewModel.RoomUiState.Success
            assert(successState.conversation == FakeItem.roomOverall.ocs!!.data)
        }

    @Test
    fun `test failure room state`() =
        runTest {
            viewModel = GccContactsViewModel(FakeRepositoryError(), userProvider)
            viewModel.createRoom("1", "users", "s@gmail.com", null)
            assert(viewModel.roomViewState.value is GccContactsViewModel.RoomUiState.Error)
            val errorState = viewModel.roomViewState.value as GccContactsViewModel.RoomUiState.Error
            assert(errorState.message == "unable to create room")
        }

    @Test
    fun `test image uri`() {
        val expectedImageUri = "https://mydomain.com/index.php/avatar/vidya/512"
        val imageUri = viewModel.getImageUri("vidya", false)
        assert(imageUri == expectedImageUri)
    }

    @Test
    fun `test error image uri`() {
        val expectedImageUri = "https://mydoman.com/index.php/avatar/vidya/512"
        val imageUri = viewModel.getImageUri("vidya", false)
        assert(imageUri != expectedImageUri)
    }
}
