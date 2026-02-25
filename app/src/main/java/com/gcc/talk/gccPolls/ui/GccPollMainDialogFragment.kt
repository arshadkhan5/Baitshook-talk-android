/*
 * Nextcloud Talk - Android Client
 *
 * SPDX-FileCopyrightText: 2022 Marcel Hibbe <dev@mhibbe.de>
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package com.gcc.talk.gccPolls.ui

import android.annotation.SuppressLint
import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import autodagger.AutoInjector
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.gcc.talk.R
import com.gcc.talk.gccApplication.GccTalkApplication
import com.gcc.talk.gccData.user.model.GccUser
import com.gcc.talk.databinding.DialogPollMainBinding
import com.gcc.talk.gccPolls.viewmodels.GccPollMainViewModel
import com.gcc.talk.gccUi.theme.ViewThemeUtils
import com.gcc.talk.gccUtils.database.user.GccCurrentUserProviderOld
import javax.inject.Inject

@AutoInjector(GccTalkApplication::class)
class GccPollMainDialogFragment : DialogFragment() {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    @Inject
    lateinit var viewThemeUtils: ViewThemeUtils

    var currentUserProvider: GccCurrentUserProviderOld? = null
        @Inject set

    private lateinit var binding: DialogPollMainBinding
    private lateinit var viewModel: GccPollMainViewModel

    lateinit var user: GccUser

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        GccTalkApplication.sharedApplication!!.componentApplication.inject(this)

        viewModel = ViewModelProvider(this, viewModelFactory)[GccPollMainViewModel::class.java]

        user = currentUserProvider?.currentUser?.blockingGet()!!

        val roomToken = arguments?.getString(KEY_ROOM_TOKEN)!!
        val isOwnerOrModerator = arguments?.getBoolean(KEY_OWNER_OR_MODERATOR)!!
        val pollId = arguments?.getString(KEY_POLL_ID)!!
        val pollTitle = arguments?.getString(KEY_POLL_TITLE)!!

        viewModel.setData(user, roomToken, isOwnerOrModerator, pollId, pollTitle)
    }

    @SuppressLint("InflateParams")
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        binding = DialogPollMainBinding.inflate(layoutInflater)

        val dialogBuilder = MaterialAlertDialogBuilder(binding.root.context).setView(binding.root)

        viewThemeUtils.dialog.colorMaterialAlertDialogBackground(binding.root.context, dialogBuilder)

        val dialog = dialogBuilder.create()

        binding.messagePollTitle.text = viewModel.pollTitle
        viewThemeUtils.dialog.colorDialogHeadline(binding.messagePollTitle)
        viewThemeUtils.dialog.colorDialogIcon(binding.messagePollIcon)

        return dialog
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View =
        binding.root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.viewState.observe(viewLifecycleOwner) { state ->
            when (state) {
                GccPollMainViewModel.InitialState -> {}
                is GccPollMainViewModel.PollVoteState -> {
                    initVotersAmount(state.showVotersAmount, state.poll.numVoters, false)
                    showVoteScreen()
                }

                is GccPollMainViewModel.PollResultState -> {
                    initVotersAmount(state.showVotersAmount, state.poll.numVoters, true)
                    showResultsScreen()
                }

                is GccPollMainViewModel.LoadingState -> {
                    showLoadingScreen()
                }

                is GccPollMainViewModel.DismissDialogState -> {
                    dismiss()
                }

                else -> {}
            }
        }
    }

    private fun showLoadingScreen() {
        val contentFragment = GccPollLoadingFragment.newInstance()
        val transaction = childFragmentManager.beginTransaction()
        transaction.replace(binding.messagePollContentFragment.id, contentFragment)
        transaction.commit()
    }

    private fun showVoteScreen() {
        val contentFragment = GccPollVoteFragment.newInstance()

        val transaction = childFragmentManager.beginTransaction()
        transaction.replace(binding.messagePollContentFragment.id, contentFragment)
        transaction.commit()
    }

    private fun showResultsScreen() {
        val contentFragment = GccPollResultsFragment.newInstance()

        val transaction = childFragmentManager.beginTransaction()
        transaction.replace(binding.messagePollContentFragment.id, contentFragment)
        transaction.commit()
    }

    private fun initVotersAmount(showVotersAmount: Boolean, numVoters: Int, showResultSubtitle: Boolean) {
        if (showVotersAmount) {
            viewThemeUtils.dialog.colorDialogSupportingText(binding.pollVotesAmount)
            binding.pollVotesAmount.visibility = View.VISIBLE
            binding.pollVotesAmount.text = resources.getQuantityString(
                R.plurals.polls_amount_voters,
                numVoters,
                numVoters
            )
        } else {
            binding.pollVotesAmount.visibility = View.GONE
        }

        if (showResultSubtitle) {
            viewThemeUtils.dialog.colorDialogSupportingText(binding.pollResultsSubtitle)
            binding.pollResultsSubtitle.visibility = View.VISIBLE
            binding.pollResultsSubtitleSeperator.visibility = View.VISIBLE
        } else {
            binding.pollResultsSubtitle.visibility = View.GONE
            binding.pollResultsSubtitleSeperator.visibility = View.GONE
        }
    }

    /**
     * Fragment creator
     */
    companion object {
        private const val KEY_USER_ENTITY = "keyUserEntity"
        private const val KEY_ROOM_TOKEN = "keyRoomToken"
        private const val KEY_OWNER_OR_MODERATOR = "keyIsOwnerOrModerator"
        private const val KEY_POLL_ID = "keyPollId"
        private const val KEY_POLL_TITLE = "keyPollTitle"

        @JvmStatic
        fun newInstance(
            user: GccUser,
            roomTokenParam: String,
            isOwnerOrModerator: Boolean,
            pollId: String,
            name: String
        ): GccPollMainDialogFragment {
            val args = bundleOf(
                KEY_USER_ENTITY to user,
                KEY_ROOM_TOKEN to roomTokenParam,
                KEY_OWNER_OR_MODERATOR to isOwnerOrModerator,
                KEY_POLL_ID to pollId,
                KEY_POLL_TITLE to name
            )

            val fragment = GccPollMainDialogFragment()
            fragment.arguments = args
            return fragment
        }
    }
}
