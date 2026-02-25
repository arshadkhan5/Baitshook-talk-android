/*
 * Nextcloud Talk - Android Client
 *
 * SPDX-FileCopyrightText: 2023 Marcel Hibbe <dev@mhibbe.de>
 * SPDX-FileCopyrightText: 2023 Ezhil Shanmugham <ezhil56x.contact@gmail.com>
 * SPDX-FileCopyrightText: 2022 Álvaro Brey <alvaro@alvarobrey.com>
 * SPDX-FileCopyrightText: 2022 Tim Krüger <t@timkrueger.me>
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package com.gcc.talk.gccShareditems.activities

import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import autodagger.AutoInjector
import com.google.android.material.tabs.TabLayout
import com.gcc.talk.R
import com.gcc.talk.gccActivities.GccBaseActivity
import com.gcc.talk.gccApplication.GccTalkApplication
import com.gcc.talk.gccChat.viewmodels.GccChatViewModel
import com.gcc.talk.gccContextchat.ContextChatView
import com.gcc.talk.gccContextchat.GccContextChatViewModel
import com.gcc.talk.gccData.user.model.GccUser
import com.gcc.talk.databinding.ActivitySharedItemsBinding
import com.gcc.talk.gccShareditems.adapters.GccSharedItemsAdapter
import com.gcc.talk.gccShareditems.model.GccSharedItemType
import com.gcc.talk.gccShareditems.viewmodels.GccSharedItemsViewModel
import com.gcc.talk.gccUtils.bundle.GccBundleKeys.KEY_CONVERSATION_NAME
import com.gcc.talk.gccUtils.bundle.GccBundleKeys.KEY_ROOM_TOKEN
import javax.inject.Inject

@AutoInjector(GccTalkApplication::class)
class GccSharedItemsActivity : GccBaseActivity() {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    @Inject
    lateinit var chatViewModel: GccChatViewModel

    @Inject
    lateinit var contextChatViewModel: GccContextChatViewModel

    private lateinit var binding: ActivitySharedItemsBinding
    private lateinit var viewModel: GccSharedItemsViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        GccTalkApplication.sharedApplication!!.componentApplication.inject(this)

        val roomToken = intent.getStringExtra(KEY_ROOM_TOKEN)!!
        val conversationName = intent.getStringExtra(KEY_CONVERSATION_NAME)

        val user = currentUserProviderOld.currentUser.blockingGet()

        val isUserConversationOwnerOrModerator = intent.getBooleanExtra(KEY_USER_IS_OWNER_OR_MODERATOR, false)
        val isOne2One = intent.getBooleanExtra(KEY_IS_ONE_2_ONE, false)

        binding = ActivitySharedItemsBinding.inflate(layoutInflater)
        setSupportActionBar(binding.sharedItemsToolbar)
        setContentView(binding.root)

        initSystemBars()

        viewThemeUtils.material.themeToolbar(binding.sharedItemsToolbar)
        viewThemeUtils.material.themeTabLayoutOnSurface(binding.sharedItemsTabs)

        supportActionBar?.title = conversationName
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        viewModel = ViewModelProvider(this, viewModelFactory)[GccSharedItemsViewModel::class.java]

        viewModel.viewState.observe(this) { state ->
            handleModelChange(state, user, roomToken, isUserConversationOwnerOrModerator, isOne2One)
        }

        binding.imageRecycler.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (!recyclerView.canScrollVertically(1) && newState == RecyclerView.SCROLL_STATE_IDLE) {
                    viewModel.loadNextItems()
                }
            }
        })

        viewModel.initialize(user, roomToken)
    }

    private fun handleModelChange(
        state: GccSharedItemsViewModel.ViewState?,
        user: GccUser,
        roomToken: String,
        isUserConversationOwnerOrModerator: Boolean,
        isOne2One: Boolean
    ) {
        clearEmptyLoading()
        when (state) {
            is GccSharedItemsViewModel.LoadingItemsState, GccSharedItemsViewModel.InitialState -> {
                showLoading()
            }
            is GccSharedItemsViewModel.NoSharedItemsState -> {
                showEmpty()
            }
            is GccSharedItemsViewModel.LoadedState -> {
                val sharedMediaItems = state.items
                Log.d(TAG, "Items received: $sharedMediaItems")

                val showGrid = state.selectedType == GccSharedItemType.MEDIA
                val layoutManager = if (showGrid) {
                    GridLayoutManager(this, SPAN_COUNT)
                } else {
                    LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
                }

                val adapter = GccSharedItemsAdapter(
                    showGrid,
                    user,
                    roomToken,
                    isUserConversationOwnerOrModerator,
                    isOne2One,
                    viewThemeUtils
                ).apply {
                    items = sharedMediaItems.items.toMutableList()
                }
                binding.imageRecycler.adapter = adapter
                binding.imageRecycler.layoutManager = layoutManager
            }
            is GccSharedItemsViewModel.TypesLoadedState -> {
                initTabs(state.types)
            }
            else -> {}
        }

        viewThemeUtils.material.themeTabLayoutOnSurface(binding.sharedItemsTabs)
    }

    fun startContextChatWindowForMessage(
        credentials: String?,
        baseUrl: String?,
        roomToken: String,
        messageId: String?,
        threadId: String?
    ) {
        binding.genericComposeView.apply {
            setContent {
                contextChatViewModel.getContextForChatMessages(
                    credentials = credentials!!,
                    baseUrl = baseUrl!!,
                    token = roomToken,
                    threadId = threadId,
                    messageId = messageId!!,
                    title = ""
                )
                ContextChatView(context, contextChatViewModel)
            }
        }
        Log.d(TAG, "Should open something else")
    }

    private fun clearEmptyLoading() {
        binding.sharedItemsTabs.visibility = View.VISIBLE
        binding.emptyContainer.emptyListView.visibility = View.GONE
    }

    private fun showLoading() {
        binding.emptyContainer.emptyListViewHeadline.text = getString(R.string.file_list_loading)
        binding.emptyContainer.emptyListView.visibility = View.VISIBLE
    }

    private fun showEmpty() {
        binding.emptyContainer.emptyListViewHeadline.text = getString(R.string.nc_shared_items_empty)
        binding.emptyContainer.emptyListView.visibility = View.VISIBLE
        binding.sharedItemsTabs.visibility = View.GONE
    }

    private fun initTabs(sharedItemTypes: Set<GccSharedItemType>) {
        binding.sharedItemsTabs.removeAllTabs()

        if (sharedItemTypes.contains(GccSharedItemType.MEDIA)) {
            val tabMedia: TabLayout.Tab = binding.sharedItemsTabs.newTab()
            tabMedia.tag = GccSharedItemType.MEDIA
            tabMedia.setText(R.string.shared_items_media)
            binding.sharedItemsTabs.addTab(tabMedia)
        }

        if (sharedItemTypes.contains(GccSharedItemType.FILE)) {
            val tabFile: TabLayout.Tab = binding.sharedItemsTabs.newTab()
            tabFile.tag = GccSharedItemType.FILE
            tabFile.setText(R.string.shared_items_file)
            binding.sharedItemsTabs.addTab(tabFile)
        }

        if (sharedItemTypes.contains(GccSharedItemType.AUDIO)) {
            val tabAudio: TabLayout.Tab = binding.sharedItemsTabs.newTab()
            tabAudio.tag = GccSharedItemType.AUDIO
            tabAudio.setText(R.string.shared_items_audio)
            binding.sharedItemsTabs.addTab(tabAudio)
        }

        if (sharedItemTypes.contains(GccSharedItemType.RECORDING)) {
            val tabRecording: TabLayout.Tab = binding.sharedItemsTabs.newTab()
            tabRecording.tag = GccSharedItemType.RECORDING
            tabRecording.setText(R.string.shared_items_recording)
            binding.sharedItemsTabs.addTab(tabRecording)
        }

        if (sharedItemTypes.contains(GccSharedItemType.VOICE)) {
            val tabVoice: TabLayout.Tab = binding.sharedItemsTabs.newTab()
            tabVoice.tag = GccSharedItemType.VOICE
            tabVoice.setText(R.string.shared_items_voice)
            binding.sharedItemsTabs.addTab(tabVoice)
        }

        if (sharedItemTypes.contains(GccSharedItemType.POLL)) {
            val tabVoice: TabLayout.Tab = binding.sharedItemsTabs.newTab()
            tabVoice.tag = GccSharedItemType.POLL
            tabVoice.setText(R.string.shared_items_poll)
            binding.sharedItemsTabs.addTab(tabVoice)
        }

        if (sharedItemTypes.contains(GccSharedItemType.PINNED)) {
            val tabPinned: TabLayout.Tab = binding.sharedItemsTabs.newTab()
            tabPinned.tag = GccSharedItemType.PINNED
            tabPinned.setText(R.string.pinned)
            binding.sharedItemsTabs.addTab(tabPinned)
        }

        if (sharedItemTypes.contains(GccSharedItemType.LOCATION)) {
            val tabLocation: TabLayout.Tab = binding.sharedItemsTabs.newTab()
            tabLocation.tag = GccSharedItemType.LOCATION
            tabLocation.setText(R.string.nc_shared_items_location)
            binding.sharedItemsTabs.addTab(tabLocation)
        }

        if (sharedItemTypes.contains(GccSharedItemType.DECKCARD)) {
            val tabDeckCard: TabLayout.Tab = binding.sharedItemsTabs.newTab()
            tabDeckCard.tag = GccSharedItemType.DECKCARD
            tabDeckCard.setText(R.string.nc_shared_items_deck_card)
            binding.sharedItemsTabs.addTab(tabDeckCard)
        }

        if (sharedItemTypes.contains(GccSharedItemType.OTHER)) {
            val tabOther: TabLayout.Tab = binding.sharedItemsTabs.newTab()
            tabOther.tag = GccSharedItemType.OTHER
            tabOther.setText(R.string.shared_items_other)
            binding.sharedItemsTabs.addTab(tabOther)
        }

        binding.sharedItemsTabs.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                viewModel.initialLoadItems(tab.tag as GccSharedItemType)
            }

            override fun onTabUnselected(tab: TabLayout.Tab) = Unit

            override fun onTabReselected(tab: TabLayout.Tab) = Unit
        })
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean =
        if (item.itemId == android.R.id.home) {
            onBackPressedDispatcher.onBackPressed()
            true
        } else {
            super.onOptionsItemSelected(item)
        }

    companion object {
        private val TAG = GccSharedItemsActivity::class.simpleName
        const val SPAN_COUNT: Int = 4
        const val KEY_USER_IS_OWNER_OR_MODERATOR = "userIsOwnerOrModerator"
        const val KEY_IS_ONE_2_ONE = "KEY_IS_ONE_2_ONE"
    }
}
