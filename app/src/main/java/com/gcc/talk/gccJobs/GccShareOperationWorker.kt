/*
 * Nextcloud Talk - Android Client
 *
 * SPDX-FileCopyrightText: 2022 Marcel Hibbe <dev@mhibbe.de>
 * SPDX-FileCopyrightText: 2017-2018 Mario Danic <mario@lovelyhq.com>
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package com.gcc.talk.gccJobs

import android.content.Context
import android.util.Log
import androidx.work.Data
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import androidx.work.Worker
import androidx.work.WorkerParameters
import autodagger.AutoInjector
import com.gcc.talk.gccApi.GccNcApi
import com.gcc.talk.gccApplication.GccTalkApplication
import com.gcc.talk.gccApplication.GccTalkApplication.Companion.sharedApplication
import com.gcc.talk.gccData.user.model.GccUser
import com.gcc.talk.gccUsers.GccUserManager
import com.gcc.talk.gccUtils.GccApiUtils
import com.gcc.talk.gccUtils.bundle.GccBundleKeys.KEY_FILE_PATHS
import com.gcc.talk.gccUtils.bundle.GccBundleKeys.KEY_INTERNAL_USER_ID
import com.gcc.talk.gccUtils.bundle.GccBundleKeys.KEY_META_DATA
import com.gcc.talk.gccUtils.bundle.GccBundleKeys.KEY_ROOM_TOKEN
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

@AutoInjector(GccTalkApplication::class)
class GccShareOperationWorker(context: Context, workerParams: WorkerParameters) : Worker(context, workerParams) {

    @Inject
    lateinit var userManager: GccUserManager

    @Inject
    lateinit var ncApi: GccNcApi

    private val userId: Long
    private val roomToken: String?
    private val filesArray: MutableList<String?> = ArrayList()
    private val credentials: String
    private val baseUrl: String?
    private val metaData: String?

    override fun doWork(): Result {
        for (filePath in filesArray) {
            ncApi.createRemoteShare(
                credentials,
                GccApiUtils.getSharingUrl(baseUrl!!),
                filePath,
                roomToken,
                "10",
                metaData
            )
                .subscribeOn(Schedulers.io())
                .blockingSubscribe(
                    {},
                    { e -> Log.w(TAG, "error while creating RemoteShare", e) }
                )
        }
        return Result.success()
    }

    init {
        sharedApplication!!.componentApplication.inject(this)
        val data = workerParams.inputData
        userId = data.getLong(KEY_INTERNAL_USER_ID, 0)
        roomToken = data.getString(KEY_ROOM_TOKEN)
        metaData = data.getString(KEY_META_DATA)
        data.getStringArray(KEY_FILE_PATHS)?.let { filesArray.addAll(it.toList()) }

        val operationsUser = userManager.getUserWithId(userId).blockingGet()
        baseUrl = operationsUser.baseUrl
        credentials = GccApiUtils.getCredentials(operationsUser.username, operationsUser.token)!!
    }

    companion object {
        private val TAG = GccShareOperationWorker::class.simpleName

        fun shareFile(roomToken: String?, currentUser: GccUser, remotePath: String, metaData: String?) {
            val paths: MutableList<String> = ArrayList()
            paths.add(remotePath)

            val data = Data.Builder()
                .putLong(KEY_INTERNAL_USER_ID, currentUser.id!!)
                .putString(KEY_ROOM_TOKEN, roomToken)
                .putStringArray(KEY_FILE_PATHS, paths.toTypedArray())
                .putString(KEY_META_DATA, metaData)
                .build()
            val shareWorker = OneTimeWorkRequest.Builder(GccShareOperationWorker::class.java)
                .setInputData(data)
                .build()
            WorkManager.getInstance().enqueue(shareWorker)
        }
    }
}
