/*
 * Nextcloud Talk - Android Client
 *
 * SPDX-FileCopyrightText: 2022 Andy Scherzinger <info@andy-scherzinger.de>
 * SPDX-FileCopyrightText: 2022 Álvaro Brey <alvaro.brey@nextcloud.com>
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package com.gcc.talk.gccRemotefilebrowser.viewmodels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.gcc.talk.R
import com.gcc.talk.gccRemotefilebrowser.model.GccRemoteFileBrowserItem
import com.gcc.talk.gccRemotefilebrowser.repositories.GccRemoteFileBrowserItemsRepository
import com.gcc.talk.gccUtils.GccFileSortOrder
import com.gcc.talk.gccUtils.GccMimetype.FOLDER
import com.gcc.talk.gccUtils.database.user.GccCurrentUserProviderOld
import com.gcc.talk.gccUtils.preferences.GccAppPreferencesImpl
import io.reactivex.Observer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

/**
 * @startuml
 * hide empty description
 * [*] --> InitialState
 * InitialState --> LoadingItemsState
 * LoadingItemsState --> NoRemoteFileItemsState
 * NoRemoteFileItemsState --> LoadingItemsState
 * LoadingItemsState --> LoadedState
 * LoadedState --> LoadingItemsState
 * LoadedState --> FinishState
 * FinishState --> [*]
 * @enduml
 */
@OptIn(ExperimentalCoroutinesApi::class)
class GccRemoteFileBrowserItemsViewModel
@Inject
constructor(
    private val repository: GccRemoteFileBrowserItemsRepository,
    private val appPreferences: GccAppPreferencesImpl,
    private val currentUserProvider: GccCurrentUserProviderOld
) : ViewModel() {

    sealed interface ViewState
    object InitialState : ViewState
    object NoRemoteFileItemsState : ViewState
    object LoadingItemsState : ViewState
    class LoadedState(val items: List<GccRemoteFileBrowserItem>) : ViewState
    class FinishState(val selectedPaths: Set<String>) : ViewState

    private val currentUser = currentUserProvider.currentUser.blockingGet()

    private val initialSortOrder = GccFileSortOrder.getFileSortOrder(appPreferences.sorting)

    private var sortingFlow: Flow<String>

    private val _viewState: MutableLiveData<ViewState> = MutableLiveData(InitialState)
    val viewState: LiveData<ViewState>
        get() = _viewState

    // TODO incorporate into view state object?
    private val _fileSortOrder: MutableLiveData<GccFileSortOrder> = MutableLiveData(initialSortOrder)
    val fileSortOrder: LiveData<GccFileSortOrder>
        get() = _fileSortOrder

    private val _currentPath: MutableLiveData<String> = MutableLiveData(ROOT_PATH)
    val currentPath: LiveData<String>
        get() = _currentPath

    private val _selectedPaths: MutableLiveData<Set<String>> = MutableLiveData(emptySet())
    val selectedPaths: LiveData<Set<String>>
        get() = _selectedPaths

    init {
        val key = appPreferences.context.resources.getString(R.string.nc_file_browser_sort_by_key)
        sortingFlow = appPreferences.readString(key)
        CoroutineScope(Dispatchers.Main).launch {
            var state = appPreferences.sorting
            sortingFlow.collect { newString ->
                if (newString != state) {
                    state = newString
                    onSelectSortOrder(newString)
                }
            }
        }
    }

    fun loadItems() {
        _viewState.value = LoadingItemsState
        repository.listFolder(
            currentUser,
            currentPath.value!!
        ).subscribeOn(Schedulers.io())
            ?.observeOn(AndroidSchedulers.mainThread())
            ?.subscribe(RemoteFileBrowserItemsObserver())
    }

    inner class RemoteFileBrowserItemsObserver : Observer<List<GccRemoteFileBrowserItem>> {

        var newRemoteFileBrowserItems: List<GccRemoteFileBrowserItem>? = null

        override fun onSubscribe(d: Disposable) = Unit

        override fun onNext(response: List<GccRemoteFileBrowserItem>) {
            newRemoteFileBrowserItems = fileSortOrder.value!!.sortCloudFiles(response)
        }

        override fun onError(e: Throwable) {
            Log.d(TAG, "An error occurred: $e")
        }

        override fun onComplete() {
            if (newRemoteFileBrowserItems.isNullOrEmpty()) {
                this@GccRemoteFileBrowserItemsViewModel._viewState.value = NoRemoteFileItemsState
            } else {
                setCurrentState(newRemoteFileBrowserItems!!)
            }
        }

        private fun setCurrentState(items: List<GccRemoteFileBrowserItem>) {
            when (this@GccRemoteFileBrowserItemsViewModel._viewState.value) {
                is LoadedState, LoadingItemsState -> {
                    this@GccRemoteFileBrowserItemsViewModel._viewState.value = LoadedState(items)
                }
                else -> return
            }
        }
    }

    private fun onSelectSortOrder(newSortOrderString: String) {
        val newSortOrder = GccFileSortOrder.getFileSortOrder(newSortOrderString)
        if (newSortOrder.name != fileSortOrder.value?.name) {
            _fileSortOrder.value = newSortOrder
            val currentState = viewState.value
            if (currentState is LoadedState) {
                val sortedItems = newSortOrder.sortCloudFiles(currentState.items)
                _viewState.value = LoadedState(sortedItems)
            }
        }
    }

    private fun changePath(path: String) {
        _currentPath.value = path
        loadItems()
    }

    fun navigateUp() {
        val path = _currentPath.value
        if (path!! != ROOT_PATH) {
            _currentPath.value = File(path).parent!!
            loadItems()
        }
    }

    fun onSelectionDone() {
        val selection = selectedPaths.value
        if (!selection.isNullOrEmpty()) {
            _viewState.value = FinishState(selection)
        }
    }

    fun onItemClicked(remoteFileBrowserItem: GccRemoteFileBrowserItem) {
        if (remoteFileBrowserItem.mimeType == FOLDER) {
            changePath(remoteFileBrowserItem.path!!)
        } else {
            toggleBrowserItemSelection(remoteFileBrowserItem.path!!)
        }
    }

    private fun toggleBrowserItemSelection(path: String) {
        val paths = selectedPaths.value!!.toMutableSet()
        if (paths.contains(path) || shouldPathBeSelectedDueToParent(path)) {
            checkAndRemoveAnySelectedParents(path)
        } else {
            // TODO if it's a folder, remove all the children we added manually
            paths.add(path)
            _selectedPaths.value = paths
        }
    }

    private fun checkAndRemoveAnySelectedParents(currentPath: String) {
        var file = File(currentPath)
        val paths = selectedPaths.value!!.toMutableSet()
        paths.remove(currentPath)
        while (file.parent != null) {
            paths.remove(file.parent!! + File.pathSeparator)
            file = File(file.parent!!)
        }
        _selectedPaths.value = paths
    }

    private fun shouldPathBeSelectedDueToParent(currentPath: String): Boolean {
        var file = File(currentPath)
        val paths = selectedPaths.value!!
        if (paths.isNotEmpty() && file.parent != ROOT_PATH) {
            while (file.parent != null) {
                var parent = file.parent!!
                if (File(file.parent!!).parent != null) {
                    parent += File.pathSeparator
                }
                if (paths.contains(parent)) {
                    return true
                }
                file = File(file.parent!!)
            }
        }
        return false
    }

    fun isPathSelected(path: String): Boolean =
        selectedPaths.value?.contains(path) == true || shouldPathBeSelectedDueToParent(path)

    companion object {
        private val TAG = GccRemoteFileBrowserItemsViewModel::class.simpleName
        private const val ROOT_PATH = "/"
    }
}
