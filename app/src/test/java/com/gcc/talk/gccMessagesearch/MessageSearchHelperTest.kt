/*
 * Nextcloud Talk - Android Client
 *
 * SPDX-FileCopyrightText: 2022 Nextcloud GmbH
 * SPDX-FileCopyrightText: 2022 Álvaro Brey <alvaro@alvarobrey.com>
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package com.gcc.talk.gccMessagesearch

import com.gcc.talk.gccData.user.GccUsersDao
import com.gcc.talk.gccData.user.GccUsersRepository
import com.gcc.talk.gccData.user.GccUsersRepositoryImpl
import com.gcc.talk.gccModels.domain.GccSearchMessageEntry
import com.gcc.talk.gccRepositories.unifiedsearch.GccUnifiedSearchRepository
import com.gcc.talk.test.fakes.FakeUnifiedSearchRepository
import com.gcc.talk.gccUsers.GccUserManager
import com.gcc.talk.gccUtils.database.user.GccCurrentUserProviderOld
import com.gcc.talk.gccUtils.database.user.CurrentUserProviderOldImpl
import com.gcc.talk.gccUtils.preview.DummyUserDaoImpl
import io.reactivex.Observable
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.mockito.MockitoAnnotations

class MessageSearchHelperTest {

    val repository = FakeUnifiedSearchRepository()

    val usersDao: GccUsersDao
        get() = DummyUserDaoImpl()

    val userRepository: GccUsersRepository
        get() = GccUsersRepositoryImpl(usersDao)

    val userManager: GccUserManager
        get() = GccUserManager(userRepository)

    val userProvider: GccCurrentUserProviderOld
        get() = CurrentUserProviderOldImpl(userManager)

    @Suppress("LongParameterList")
    private fun createMessageEntry(
        searchTerm: String = "foo",
        thumbnailURL: String = "foo",
        title: String = "foo",
        messageExcerpt: String = "foo",
        conversationToken: String = "foo",
        messageId: String? = "foo",
        threadId: String? = "foo"
    ) = GccSearchMessageEntry(searchTerm, thumbnailURL, title, messageExcerpt, conversationToken, threadId, messageId)

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
    }

    @Test
    fun emptySearch() {
        repository.response = GccUnifiedSearchRepository.UnifiedSearchResults(0, false, emptyList())

        val sut = GccMessageSearchHelper(
            repository,
            currentUser = userProvider.currentUser.blockingGet()
        )

        val testObserver = sut.startMessageSearch("foo").test()
        testObserver.assertComplete()
        testObserver.assertValueCount(1)
        val expected = GccMessageSearchHelper.MessageSearchResults(emptyList(), false)
        testObserver.assertValue(expected)
    }

    @Test
    fun nonEmptySearch_withMoreResults() {
        val entries = (1..5).map { createMessageEntry() }
        repository.response = GccUnifiedSearchRepository.UnifiedSearchResults(5, true, entries)

        val sut = GccMessageSearchHelper(
            repository,
            currentUser = userProvider.currentUser.blockingGet()
        )

        val observable = sut.startMessageSearch("foo")
        val expected = GccMessageSearchHelper.MessageSearchResults(entries, true)
        testCall(observable, expected)
    }

    @Test
    fun nonEmptySearch_withNoMoreResults() {
        val entries = (1..2).map { createMessageEntry() }
        repository.response = GccUnifiedSearchRepository.UnifiedSearchResults(2, false, entries)

        val sut = GccMessageSearchHelper(
            repository,
            currentUser = userProvider.currentUser.blockingGet()
        )

        val observable = sut.startMessageSearch("foo")
        val expected = GccMessageSearchHelper.MessageSearchResults(entries, false)
        testCall(observable, expected)
    }

    @Test
    fun nonEmptySearch_consecutiveSearches_sameResult() {
        val entries = (1..2).map { createMessageEntry() }
        repository.response = GccUnifiedSearchRepository.UnifiedSearchResults(2, false, entries)

        val sut = GccMessageSearchHelper(
            repository,
            currentUser = userProvider.currentUser.blockingGet()
        )

        repeat(5) {
            val observable = sut.startMessageSearch("foo")
            val expected = GccMessageSearchHelper.MessageSearchResults(entries, false)
            testCall(observable, expected)
        }
    }

    @Test
    fun loadMore_noPreviousResults() {
        val sut = GccMessageSearchHelper(
            repository,
            currentUser = userProvider.currentUser.blockingGet()
        )
        Assert.assertEquals(null, sut.loadMore())
    }

    @Test
    fun loadMore_previousResults_sameSearch() {
        val sut = GccMessageSearchHelper(
            repository,
            currentUser = userProvider.currentUser.blockingGet()
        )

        val firstPageEntries = (1..5).map { createMessageEntry() }
        repository.response = GccUnifiedSearchRepository.UnifiedSearchResults(5, true, firstPageEntries)

        val firstPageObservable = sut.startMessageSearch("foo")
        Assert.assertEquals(0, repository.lastRequestedCursor)
        val firstPageExpected = GccMessageSearchHelper.MessageSearchResults(firstPageEntries, true)
        testCall(firstPageObservable, firstPageExpected)

        val secondPageEntries = (1..5).map { createMessageEntry(title = "bar") }
        repository.response = GccUnifiedSearchRepository.UnifiedSearchResults(10, false, secondPageEntries)

        val secondPageObservable = sut.loadMore()
        Assert.assertEquals(5, repository.lastRequestedCursor)
        Assert.assertNotNull(secondPageObservable)
        val secondPageExpected = GccMessageSearchHelper.MessageSearchResults(firstPageEntries + secondPageEntries, false)
        testCall(secondPageObservable!!, secondPageExpected)
    }

    private fun testCall(
        searchCall: Observable<GccMessageSearchHelper.MessageSearchResults>,
        expectedResult: GccMessageSearchHelper.MessageSearchResults
    ) {
        val testObserver = searchCall.test()
        testObserver.assertComplete()
        testObserver.assertValueCount(1)
        testObserver.assertValue(expectedResult)
        testObserver.dispose()
    }
}
