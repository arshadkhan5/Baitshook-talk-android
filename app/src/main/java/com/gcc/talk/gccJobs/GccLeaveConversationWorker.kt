/*
 * gcc Talk - Android Client
 *
 * SPDX-FileCopyrightText: 2022 Andy Scherzinger <info@andy-scherzinger.de>
 * SPDX-FileCopyrightText: 2017-2018 Mario Danic <mario@lovelyhq.com>
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package com.gcc.talk.gccJobs

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import androidx.work.Data
import androidx.work.ListenableWorker
import androidx.work.WorkerParameters
import androidx.work.impl.utils.futures.SettableFuture
import autodagger.AutoInjector
import com.google.common.util.concurrent.ListenableFuture
import com.gcc.talk.gccApi.GccNcApi
import com.gcc.talk.gccApplication.GccTalkApplication
import com.gcc.talk.gccModels.json.generic.GenericOverall
import com.gcc.talk.gccUsers.GccUserManager
import com.gcc.talk.gccUtils.GccApiUtils
import com.gcc.talk.gccUtils.GccApiUtils.getConversationApiVersion
import com.gcc.talk.gccUtils.GccApiUtils.getCredentials
import com.gcc.talk.gccUtils.GccApiUtils.getUrlForParticipantsSelf
import com.gcc.talk.gccUtils.bundle.GccBundleKeys
import com.gcc.talk.gccUtils.database.user.GccCurrentUserProviderOld
import io.reactivex.Observer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import retrofit2.HttpException
import javax.inject.Inject

@SuppressLint("RestrictedApi")
@AutoInjector(GccTalkApplication::class)
class GccLeaveConversationWorker(context: Context, workerParams: WorkerParameters) :
    ListenableWorker(context, workerParams) {

    @Inject
    lateinit var ncApi: GccNcApi

    @Inject
    lateinit var userManager: GccUserManager

    @Inject
    lateinit var currentUserProvider: GccCurrentUserProviderOld

    private val result = SettableFuture.create<Result>()

    override fun startWork(): ListenableFuture<Result> {
        GccTalkApplication.sharedApplication!!.componentApplication.inject(this)
        val conversationToken = inputData.getString(GccBundleKeys.KEY_ROOM_TOKEN)
        val currentUser = currentUserProvider.currentUser.blockingGet()

        if (currentUser != null && conversationToken != null) {
            val credentials = getCredentials(currentUser.username, currentUser.token)
            val apiVersion = getConversationApiVersion(currentUser, intArrayOf(GccApiUtils.API_V4, 1))

            ncApi.removeSelfFromRoom(
                credentials,
                getUrlForParticipantsSelf(apiVersion, currentUser.baseUrl, conversationToken)
            )
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : Observer<GenericOverall?> {
                    override fun onSubscribe(d: Disposable) {
                        // unused atm
                    }

                    override fun onNext(p0: GenericOverall) {
                        // unused atm
                    }

                    override fun onError(e: Throwable) {
                        Log.e(TAG, "Failed to remove self from room", e)
                        val httpException = e as? HttpException
                        val errorData = if (httpException?.code() == HTTP_ERROR_CODE_400) {
                            Data.Builder()
                                .putString("error_type", ERROR_NO_OTHER_MODERATORS_OR_OWNERS_LEFT)
                                .build()
                        } else {
                            Data.Builder()
                                .putString("error_type", ERROR_OTHER)
                                .build()
                        }
                        result.set(Result.failure(errorData))
                    }

                    override fun onComplete() {
                        result.set(Result.success())
                    }
                })
        } else {
            result.set(Result.failure())
        }

        return result
    }

    companion object {
        private const val TAG = "GccLeaveConversationWorker"
        const val ERROR_NO_OTHER_MODERATORS_OR_OWNERS_LEFT = "NO_OTHER_MODERATORS_OR_OWNERS_LEFT"
        const val ERROR_OTHER = "ERROR_OTHER"
        const val HTTP_ERROR_CODE_400 = 400
    }
}
