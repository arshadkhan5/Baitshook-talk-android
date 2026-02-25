/*
 * Nextcloud Talk - Android Client
 *
 * SPDX-FileCopyrightText: 2023 Marcel Hibbe <dev@mhibbe.de>
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package com.gcc.talk.gccInvitation

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.core.graphics.drawable.toDrawable
import androidx.lifecycle.ViewModelProvider
import autodagger.AutoInjector
import com.google.android.material.snackbar.Snackbar
import com.gcc.talk.R
import com.gcc.talk.gccActivities.GccBaseActivity
import com.gcc.talk.gccApi.GccNcApi
import com.gcc.talk.gccApplication.GccTalkApplication
import com.gcc.talk.gccChat.GccChatActivity
import com.gcc.talk.gccConversationlist.GccConversationsListActivity
import com.gcc.talk.gccData.user.model.GccUser
import com.gcc.talk.databinding.ActivityInvitationsBinding
import com.gcc.talk.gccInvitation.adapters.InvitationsAdapter
import com.gcc.talk.gccInvitation.data.ActionEnum
import com.gcc.talk.gccInvitation.data.GccInvitation
import com.gcc.talk.gccInvitation.viewmodels.GccInvitationsViewModel
import com.gcc.talk.gccUtils.bundle.GccBundleKeys
import javax.inject.Inject

@AutoInjector(GccTalkApplication::class)
class GccInvitationsActivity : GccBaseActivity() {

    private lateinit var binding: ActivityInvitationsBinding

    @Inject
    lateinit var ncApi: GccNcApi

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    lateinit var invitationsViewModel: GccInvitationsViewModel

    lateinit var adapter: InvitationsAdapter

    private lateinit var currentUser: GccUser

    private val onBackPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            val intent = Intent(this@GccInvitationsActivity, GccConversationsListActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        GccTalkApplication.sharedApplication!!.componentApplication.inject(this)

        invitationsViewModel = ViewModelProvider(this, viewModelFactory)[GccInvitationsViewModel::class.java]

        currentUser = currentUserProviderOld.currentUser.blockingGet()
        invitationsViewModel.fetchInvitations(currentUser)

        binding = ActivityInvitationsBinding.inflate(layoutInflater)
        setupActionBar()
        setContentView(binding.root)
        initSystemBars()

        adapter = InvitationsAdapter(currentUser) { invitation, action ->
            handleInvitation(invitation, action)
        }

        binding.invitationsRecyclerView.adapter = adapter

        initObservers()

        onBackPressedDispatcher.addCallback(this, onBackPressedCallback)
    }

    enum class InvitationAction {
        ACCEPT,
        REJECT
    }

    private fun handleInvitation(invitation: GccInvitation, action: InvitationAction) {
        when (action) {
            InvitationAction.ACCEPT -> {
                invitationsViewModel.acceptInvitation(currentUser, invitation)
            }

            InvitationAction.REJECT -> {
                invitationsViewModel.rejectInvitation(currentUser, invitation)
            }
        }
    }

    private fun initObservers() {
        invitationsViewModel.fetchInvitationsViewState.observe(this) { state ->
            when (state) {
                is GccInvitationsViewModel.FetchInvitationsStartState -> {
                    binding.invitationsRecyclerView.visibility = View.GONE
                    binding.progressBarWrapper.visibility = View.VISIBLE
                }

                is GccInvitationsViewModel.FetchInvitationsSuccessState -> {
                    binding.invitationsRecyclerView.visibility = View.VISIBLE
                    binding.progressBarWrapper.visibility = View.GONE
                    adapter.submitList(state.invitations)
                }

                is GccInvitationsViewModel.FetchInvitationsEmptyState -> {
                    binding.invitationsRecyclerView.visibility = View.GONE
                    binding.progressBarWrapper.visibility = View.GONE

                    binding.emptyList.emptyListView.visibility = View.VISIBLE
                    binding.emptyList.emptyListViewHeadline.text = getString(R.string.nc_federation_no_invitations)
                    binding.emptyList.emptyListIcon.setImageResource(R.drawable.baseline_info_24)
                    binding.emptyList.emptyListIcon.visibility = View.VISIBLE
                    binding.emptyList.emptyListViewText.visibility = View.VISIBLE
                }

                is GccInvitationsViewModel.FetchInvitationsErrorState -> {
                    Snackbar.make(binding.root, R.string.nc_common_error_sorry, Snackbar.LENGTH_LONG).show()
                }

                else -> {}
            }
        }

        invitationsViewModel.invitationActionViewState.observe(this) { state ->
            when (state) {
                is GccInvitationsViewModel.InvitationActionStartState -> {
                }

                is GccInvitationsViewModel.InvitationActionSuccessState -> {
                    if (state.action == ActionEnum.ACCEPT) {
                        val bundle = Bundle()
                        bundle.putString(GccBundleKeys.KEY_ROOM_TOKEN, state.invitation.localToken)
                        val chatIntent = Intent(context, GccChatActivity::class.java)
                        chatIntent.putExtras(bundle)
                        chatIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                        startActivity(chatIntent)
                    } else {
                        // adapter.currentList.remove(state.invitation)
                        // adapter.notifyDataSetChanged()  // leads to UnsupportedOperationException ?!

                        // Just reload activity as lazy workaround to not deal with adapter for now.
                        // Might be fine until switching to jetpack compose.
                        finish()
                        startActivity(intent)
                    }
                }

                is GccInvitationsViewModel.InvitationActionErrorState -> {
                    Snackbar.make(binding.root, R.string.nc_common_error_sorry, Snackbar.LENGTH_LONG).show()
                }

                else -> {}
            }
        }
    }

    private fun setupActionBar() {
        setSupportActionBar(binding.invitationsToolbar)
        binding.invitationsToolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.setIcon(resources!!.getColor(R.color.transparent, null).toDrawable())
        viewThemeUtils.material.themeToolbar(binding.invitationsToolbar)
    }
}
