/*
 * Nextcloud Talk - Android Client
 *
 * SPDX-FileCopyrightText: 2023 Ezhil Shanmugham <ezhil56x.contact@gmail.com>
 * SPDX-FileCopyrightText: 2022 Andy Scherzinger <info@andy-scherzinger.de>
 * SPDX-FileCopyrightText: 2022 Álvaro Brey <alvaro.brey@nextcloud.com>
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package com.gcc.talk.gccRemotefilebrowser.activities

import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import autodagger.AutoInjector
import com.gcc.talk.R
import com.gcc.talk.gccApplication.GccTalkApplication
import com.gcc.talk.databinding.ActivityRemoteFileBrowserBinding
import com.gcc.talk.gccRemotefilebrowser.GccSelectionInterface
import com.gcc.talk.gccRemotefilebrowser.adapters.GccRemoteFileBrowserItemsAdapter
import com.gcc.talk.gccRemotefilebrowser.viewmodels.GccRemoteFileBrowserItemsViewModel
import com.gcc.talk.gccUi.dialog.SortingOrderDialogFragment
import com.gcc.talk.gccUi.theme.ViewThemeUtils
import com.gcc.talk.gccUtilss.GccDateUtils
import com.gcc.talk.gccUtils.GccDisplayUtils
import com.gcc.talk.gccUtils.GccFileSortOrder
import com.gcc.talk.gccUtils.bundle.GccBundleKeys.KEY_MIME_TYPE_FILTER
import com.gcc.talk.gccUtils.database.user.GccCurrentUserProviderOld
import javax.inject.Inject

@AutoInjector(GccTalkApplication::class)
class GccRemoteFileBrowserActivity :
    AppCompatActivity(),
    GccSelectionInterface,
    SwipeRefreshLayout.OnRefreshListener {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    @Inject
    lateinit var currentUserProvider: GccCurrentUserProviderOld

    @Inject
    lateinit var viewThemeUtils: ViewThemeUtils

    @Inject
    lateinit var dateUtils: GccDateUtils

    private lateinit var binding: ActivityRemoteFileBrowserBinding
    private lateinit var viewModel: GccRemoteFileBrowserItemsViewModel

    private var filesSelectionDoneMenuItem: MenuItem? = null

    private val onBackPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            setResult(Activity.RESULT_CANCELED)
            finish()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        GccTalkApplication.sharedApplication!!.componentApplication.inject(this)

        binding = ActivityRemoteFileBrowserBinding.inflate(layoutInflater)
        setSupportActionBar(binding.remoteFileBrowserItemsToolbar)
        viewThemeUtils.material.themeToolbar(binding.remoteFileBrowserItemsToolbar)
        viewThemeUtils.talk.themeSortListButtonGroup(binding.sortListButtonGroup)
        viewThemeUtils.talk.themeSortButton(binding.sortButton)
        viewThemeUtils.material.colorMaterialTextButton(binding.sortButton)
        viewThemeUtils.talk.themePathNavigationButton(binding.pathNavigationBackButton)
        viewThemeUtils.material.colorMaterialTextButton(binding.pathNavigationBackButton)
        viewThemeUtils.platform.themeStatusBar(this)
        setContentView(binding.root)
        initSystemBars()

        GccDisplayUtils.applyColorToNavigationBar(
            this.window,
            ResourcesCompat.getColor(resources, R.color.bg_default, null)
        )

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val extras = intent.extras
        val mimeTypeSelectionFilter = extras?.getString(KEY_MIME_TYPE_FILTER, null)

        initViewModel(mimeTypeSelectionFilter)

        binding.swipeRefreshList.setOnRefreshListener(this)
        viewThemeUtils.androidx.themeSwipeRefreshLayout(binding.swipeRefreshList)

        binding.pathNavigationBackButton.setOnClickListener { viewModel.navigateUp() }
        binding.sortButton.setOnClickListener { changeSorting() }

        viewModel.loadItems()

        onBackPressedDispatcher.addCallback(this, onBackPressedCallback)
    }

    private fun initViewModel(mimeTypeSelectionFilter: String?) {
        viewModel = ViewModelProvider(this, viewModelFactory)[GccRemoteFileBrowserItemsViewModel::class.java]

        viewModel.viewState.observe(this) { state ->
            clearEmptyLoading()
            when (state) {
                is GccRemoteFileBrowserItemsViewModel.LoadingItemsState, GccRemoteFileBrowserItemsViewModel.InitialState -> {
                    showLoading()
                }
                is GccRemoteFileBrowserItemsViewModel.NoRemoteFileItemsState -> {
                    showEmpty()
                }
                is GccRemoteFileBrowserItemsViewModel.LoadedState -> {
                    loadList(state, mimeTypeSelectionFilter)
                }
                is GccRemoteFileBrowserItemsViewModel.FinishState -> {
                    finishWithResult(state.selectedPaths)
                }

                else -> {}
            }
        }

        viewModel.fileSortOrder.observe(this) { sortOrder ->
            if (sortOrder != null) {
                binding.sortButton.setText(GccDisplayUtils.getSortOrderStringId(sortOrder))
            }
        }

        viewModel.currentPath.observe(this) { path ->
            if (path != null) {
                supportActionBar?.title = path
            }
        }

        viewModel.selectedPaths.observe(this) { selectedPaths ->
            filesSelectionDoneMenuItem?.isVisible = !selectedPaths.isNullOrEmpty()
        }
    }

    private fun loadList(state: GccRemoteFileBrowserItemsViewModel.LoadedState, mimeTypeSelectionFilter: String?) {
        val remoteFileBrowserItems = state.items
        Log.d(TAG, "Items received: $remoteFileBrowserItems")

        // TODO make showGrid based on preferences (when available)
        val showGrid = false
        val layoutManager = if (showGrid) {
            GridLayoutManager(this, SPAN_COUNT)
        } else {
            LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        }

        // TODO do not needlessly recreate adapter if it can be reused
        val adapter = GccRemoteFileBrowserItemsAdapter(
            showGrid = showGrid,
            mimeTypeSelectionFilter = mimeTypeSelectionFilter,
            user = currentUserProvider.currentUser.blockingGet(),
            selectionInterface = this,
            viewThemeUtils = viewThemeUtils,
            dateUtils = dateUtils,
            onItemClicked = viewModel::onItemClicked
        )
        adapter.items = remoteFileBrowserItems

        binding.recyclerView.adapter = adapter
        binding.recyclerView.layoutManager = layoutManager
        binding.recyclerView.setHasFixedSize(true)

        showList()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        super.onCreateOptionsMenu(menu)
        menuInflater.inflate(R.menu.menu_share_files, menu)
        filesSelectionDoneMenuItem = menu.findItem(R.id.files_selection_done)
        return true
    }

    override fun onResume() {
        super.onResume()
        refreshCurrentPath()
    }

    private fun changeSorting() {
        val newFragment: DialogFragment = SortingOrderDialogFragment
            .newInstance(GccFileSortOrder.getFileSortOrder(viewModel.fileSortOrder.value!!.name))
        newFragment.show(
            supportFragmentManager,
            SortingOrderDialogFragment.SORTING_ORDER_FRAGMENT
        )
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressedDispatcher.onBackPressed()
                true
            }
            R.id.files_selection_done -> {
                viewModel.onSelectionDone()
                true
            }
            else -> {
                return super.onOptionsItemSelected(item)
            }
        }
    }

    private fun finishWithResult(selectedPaths: Set<String>) {
        val data = Intent()
        data.putStringArrayListExtra(EXTRA_SELECTED_PATHS, ArrayList(selectedPaths))
        setResult(Activity.RESULT_OK, data)
        finish()
    }

    private fun clearEmptyLoading() {
        binding.emptyContainer.emptyListView.visibility = View.GONE
    }

    private fun showLoading() {
        binding.emptyContainer.emptyListViewHeadline.text = getString(R.string.file_list_loading)
        binding.emptyContainer.emptyListView.visibility = View.VISIBLE
        binding.recyclerView.visibility = View.GONE
    }

    private fun showEmpty() {
        binding.emptyContainer.emptyListViewHeadline.text = getString(R.string.nc_shared_items_empty)
        binding.emptyContainer.emptyListView.visibility = View.VISIBLE
        binding.recyclerView.visibility = View.GONE
    }

    private fun showList() {
        binding.recyclerView.visibility = View.VISIBLE
    }

    fun initSystemBars() {
        val decorView = window.decorView
        decorView.setOnApplyWindowInsetsListener { view, insets ->
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM) {
                val systemBars = insets.getInsets(
                    WindowInsetsCompat.Type.systemBars() or
                        WindowInsetsCompat.Type.displayCutout()
                )
                view.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            }
            insets
        }
        ViewCompat.requestApplyInsets(decorView)
    }

    override fun onRefresh() {
        refreshCurrentPath()
    }

    private fun refreshCurrentPath() {
        viewModel.loadItems()
    }

    override fun isPathSelected(path: String): Boolean = viewModel.isPathSelected(path)

    companion object {
        private val TAG = GccRemoteFileBrowserActivity::class.simpleName
        const val SPAN_COUNT: Int = 4
        const val EXTRA_SELECTED_PATHS = "EXTRA_SELECTED_PATH"
    }
}
