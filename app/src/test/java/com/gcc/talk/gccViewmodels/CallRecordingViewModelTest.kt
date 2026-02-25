/*
 * Nextcloud Talk - Android Client
 *
 * SPDX-FileCopyrightText: 2023 Marcel Hibbe <dev@mhibbe.de>
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package com.gcc.talk.gccViewmodels

import com.gcc.talk.gccData.user.GccUsersDao
import com.gcc.talk.gccData.user.GccUsersRepository
import com.gcc.talk.gccData.user.GccUsersRepositoryImpl
import com.gcc.talk.test.fakes.FakeCallRecordingRepository
import com.gcc.talk.gccUsers.GccUserManager
import com.gcc.talk.gccUtils.database.user.GccCurrentUserProviderOld
import com.gcc.talk.gccUtils.database.user.CurrentUserProviderOldImpl
import com.gcc.talk.gccUtils.preview.DummyUserDaoImpl
import com.vividsolutions.jts.util.Assert
import org.junit.Before
import org.junit.Test
import org.mockito.MockitoAnnotations

class CallRecordingViewModelTest : AbstractViewModelTest() {

    private val repository = FakeCallRecordingRepository()

    val usersDao: GccUsersDao
        get() = DummyUserDaoImpl()

    val userRepository: GccUsersRepository
        get() = GccUsersRepositoryImpl(usersDao)

    val userManager: GccUserManager
        get() = GccUserManager(userRepository)

    val userProvider: GccCurrentUserProviderOld
        get() = CurrentUserProviderOldImpl(userManager)

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
    }

    @Test
    fun testCallRecordingViewModel_clickStartRecord() {
        val viewModel = GccCallRecordingViewModel(repository, userProvider)
        viewModel.setData("foo")
        viewModel.clickRecordButton()

        Assert.isTrue(viewModel.viewState.value is GccCallRecordingViewModel.RecordingStartingState)

        // fake to execute setRecordingState which would be triggered by signaling message
        viewModel.setRecordingState(GccCallRecordingViewModel.RECORDING_STARTED_VIDEO_CODE)

        Assert.isTrue(viewModel.viewState.value is GccCallRecordingViewModel.RecordingStartedState)
    }

    @Test
    fun testCallRecordingViewModel_clickStopRecord() {
        val viewModel = GccCallRecordingViewModel(repository, userProvider)
        viewModel.setData("foo")
        viewModel.setRecordingState(GccCallRecordingViewModel.RECORDING_STARTED_VIDEO_CODE)

        Assert.equals(true, (viewModel.viewState.value as GccCallRecordingViewModel.RecordingStartedState).showStartedInfo)

        viewModel.clickRecordButton()

        Assert.isTrue(viewModel.viewState.value is GccCallRecordingViewModel.RecordingConfirmStopState)

        viewModel.stopRecording()

        Assert.isTrue(viewModel.viewState.value is GccCallRecordingViewModel.RecordingStoppedState)
    }

    @Test
    fun testCallRecordingViewModel_keepConfirmState() {
        val viewModel = GccCallRecordingViewModel(repository, userProvider)
        viewModel.setData("foo")
        viewModel.setRecordingState(GccCallRecordingViewModel.RECORDING_STARTED_VIDEO_CODE)

        Assert.equals(true, (viewModel.viewState.value as GccCallRecordingViewModel.RecordingStartedState).showStartedInfo)

        viewModel.clickRecordButton()

        Assert.isTrue(viewModel.viewState.value is GccCallRecordingViewModel.RecordingConfirmStopState)

        viewModel.clickRecordButton()

        Assert.isTrue(viewModel.viewState.value is GccCallRecordingViewModel.RecordingConfirmStopState)
    }

    @Test
    fun testCallRecordingViewModel_continueRecordingWhenDismissStopDialog() {
        val viewModel = GccCallRecordingViewModel(repository, userProvider)
        viewModel.setData("foo")
        viewModel.setRecordingState(GccCallRecordingViewModel.RECORDING_STARTED_VIDEO_CODE)
        viewModel.clickRecordButton()

        Assert.isTrue(viewModel.viewState.value is GccCallRecordingViewModel.RecordingConfirmStopState)

        viewModel.dismissStopRecording()

        Assert.isTrue(viewModel.viewState.value is GccCallRecordingViewModel.RecordingStartedState)

        Assert.equals(
            false,
            (viewModel.viewState.value as GccCallRecordingViewModel.RecordingStartedState).showStartedInfo
        )
    }

    @Test
    fun testSetRecordingStateDirectly() {
        val viewModel = GccCallRecordingViewModel(repository, userProvider)
        viewModel.setData("foo")

        viewModel.setRecordingState(GccCallRecordingViewModel.RECORDING_STOPPED_CODE)
        Assert.isTrue(viewModel.viewState.value is GccCallRecordingViewModel.RecordingStoppedState)

        viewModel.setRecordingState(GccCallRecordingViewModel.RECORDING_STARTED_AUDIO_CODE)
        Assert.isTrue(viewModel.viewState.value is GccCallRecordingViewModel.RecordingStartedState)

        viewModel.setRecordingState(GccCallRecordingViewModel.RECORDING_STARTED_VIDEO_CODE)
        Assert.isTrue(viewModel.viewState.value is GccCallRecordingViewModel.RecordingStartedState)

        viewModel.setRecordingState(GccCallRecordingViewModel.RECORDING_STARTING_AUDIO_CODE)
        Assert.isTrue(viewModel.viewState.value is GccCallRecordingViewModel.RecordingStartingState)

        viewModel.setRecordingState(GccCallRecordingViewModel.RECORDING_STARTING_VIDEO_CODE)
        Assert.isTrue(viewModel.viewState.value is GccCallRecordingViewModel.RecordingStartingState)

        viewModel.setRecordingState(GccCallRecordingViewModel.RECORDING_FAILED_CODE)
        Assert.isTrue(viewModel.viewState.value is GccCallRecordingViewModel.RecordingErrorState)
    }
}
