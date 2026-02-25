/*
 * Nextcloud Talk - Android Client
 *
 * SPDX-FileCopyrightText: 2022 Álvaro Brey <alvaro@alvarobrey.com>
 * SPDX-FileCopyrightText: 2022 Tim Krüger <t@timkrueger.me>
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package com.gcc.talk.gccShareditems.viewmodels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.gcc.talk.gccData.user.model.GccUser
import com.gcc.talk.gccShareditems.model.GccSharedItemType
import com.gcc.talk.gccShareditems.model.GccSharedItems
import com.gcc.talk.gccShareditems.repositories.GccSharedItemsRepository
import io.reactivex.Observer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

class GccSharedItemsViewModel @Inject constructor(private val repository: GccSharedItemsRepository) : ViewModel() {

    private lateinit var repositoryParameters: GccSharedItemsRepository.Parameters

    sealed interface ViewState
    object InitialState : ViewState
    object NoSharedItemsState : ViewState
    open class TypesLoadedState(val types: Set<GccSharedItemType>, val selectedType: GccSharedItemType) : ViewState
    class LoadingItemsState(types: Set<GccSharedItemType>, selectedType: GccSharedItemType) :
        TypesLoadedState(types, selectedType)

    class LoadedState(types: Set<GccSharedItemType>, selectedType: GccSharedItemType, val items: GccSharedItems) :
        TypesLoadedState(types, selectedType)

    private val _viewState: MutableLiveData<ViewState> = MutableLiveData(InitialState)
    val viewState: LiveData<ViewState>
        get() = _viewState

    fun initialize(user: GccUser, roomToken: String) {
        repositoryParameters = GccSharedItemsRepository.Parameters(
            user.userId!!,
            user.token!!,
            user.baseUrl!!,
            roomToken
        )
        loadAvailableTypes()
    }

    private fun loadAvailableTypes() {
        repository.availableTypes(repositoryParameters).subscribeOn(Schedulers.io())
            ?.observeOn(AndroidSchedulers.mainThread())
            ?.subscribe(object : Observer<Set<GccSharedItemType>> {

                var types: Set<GccSharedItemType>? = null

                override fun onSubscribe(d: Disposable) = Unit

                override fun onNext(types: Set<GccSharedItemType>) {
                    this.types = types
                }

                override fun onError(e: Throwable) {
                    Log.e(TAG, "An error occurred when loading available types", e)
                }

                override fun onComplete() {
                    val newTypes = this.types
                    if (newTypes.isNullOrEmpty()) {
                        this@GccSharedItemsViewModel._viewState.value = NoSharedItemsState
                    } else {
                        val selectedType = chooseInitialType(newTypes)
                        this@GccSharedItemsViewModel._viewState.value =
                            TypesLoadedState(newTypes, selectedType)
                        initialLoadItems(selectedType)
                    }
                }
            })
    }

    private fun chooseInitialType(newTypes: Set<GccSharedItemType>): GccSharedItemType =
        when {
            newTypes.contains(GccSharedItemType.MEDIA) -> GccSharedItemType.MEDIA
            else -> newTypes.toList().first()
        }

    fun initialLoadItems(type: GccSharedItemType) {
        val state = _viewState.value
        if (state is TypesLoadedState) {
            _viewState.value = LoadingItemsState(state.types, type)
            repository.media(repositoryParameters, type)?.subscribeOn(Schedulers.io())
                ?.observeOn(AndroidSchedulers.mainThread())
                ?.subscribe(SharedMediaItemsObserver())
        }
    }

    fun loadNextItems() {
        when (val currentState = _viewState.value) {
            is LoadedState -> {
                val currentSharedItems = currentState.items
                if (currentSharedItems.moreItemsExisting) {
                    repository.media(repositoryParameters, currentState.selectedType, currentSharedItems.lastSeenId)
                        ?.subscribeOn(Schedulers.io())
                        ?.observeOn(AndroidSchedulers.mainThread())
                        ?.subscribe(SharedMediaItemsObserver())
                }
            }
            else -> return
        }
    }

    inner class SharedMediaItemsObserver : Observer<GccSharedItems> {

        var newSharedItems: GccSharedItems? = null

        override fun onSubscribe(d: Disposable) = Unit

        override fun onNext(response: GccSharedItems) {
            newSharedItems = response
        }

        override fun onError(e: Throwable) {
            Log.d(TAG, "An error occurred: $e")
        }

        override fun onComplete() {
            val items = newSharedItems!!
            val state = this@GccSharedItemsViewModel._viewState.value
            if (state is LoadedState) {
                val oldItems = state.items.items
                val newItems =
                    GccSharedItems(
                        oldItems + newSharedItems!!.items,
                        state.items.type,
                        newSharedItems!!.lastSeenId,
                        newSharedItems!!.moreItemsExisting
                    )
                setCurrentState(newItems)
            } else {
                setCurrentState(items)
            }
        }

        private fun setCurrentState(items: GccSharedItems) {
            when (val state = this@GccSharedItemsViewModel._viewState.value) {
                is TypesLoadedState -> {
                    this@GccSharedItemsViewModel._viewState.value = LoadedState(
                        state.types,
                        state.selectedType,
                        items
                    )
                }
                else -> return
            }
        }
    }

    companion object {
        private val TAG = GccSharedItemsViewModel::class.simpleName
    }
}
