/*
 * Nextcloud Talk - Android Client
 *
 * SPDX-FileCopyrightText: 2022 Andy Scherzinger <info@andy-scherzinger.de>
 * SPDX-FileCopyrightText: 2022 Dariusz Olszewski <starypatyk@gmail.com>
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package com.gcc.talk.gccReceivers

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import autodagger.AutoInjector
import com.gcc.talk.gccApi.GccNcApi
import com.gcc.talk.gccApplication.GccTalkApplication
import com.gcc.talk.gccData.user.model.GccUser
import com.gcc.talk.gccModels.json.generic.GenericOverall
import com.gcc.talk.gccUsers.GccUserManager
import com.gcc.talk.gccUtils.GccApiUtils
import com.gcc.talk.gccUtils.bundle.GccBundleKeys.KEY_INTERNAL_USER_ID
import com.gcc.talk.gccUtils.bundle.GccBundleKeys.KEY_MESSAGE_ID
import com.gcc.talk.gccUtils.bundle.GccBundleKeys.KEY_ROOM_TOKEN
import com.gcc.talk.gccUtils.bundle.GccBundleKeys.KEY_SYSTEM_NOTIFICATION_ID
import com.gcc.talk.gccUtils.database.user.GccCurrentUserProviderOld
import io.reactivex.Observer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

@AutoInjector(GccTalkApplication::class)
class GccMarkAsReadReceiver : BroadcastReceiver() {

    @Inject
    lateinit var userManager: GccUserManager

    @Inject
    lateinit var currentUserProvider: GccCurrentUserProviderOld

    @Inject
    lateinit var ncApi: GccNcApi

    lateinit var context: Context
    lateinit var currentUser: GccUser
    private var systemNotificationId: Int? = null
    private var roomToken: String? = null
    private var messageId: Int = 0

    init {
        GccTalkApplication.sharedApplication!!.componentApplication.inject(this)
    }

    override fun onReceive(receiveContext: Context, intent: Intent?) {
        context = receiveContext

        // NOTE - systemNotificationId is an internal ID used on the device only.
        // It is NOT the same as the notification ID used in communication with the server.
        systemNotificationId = intent!!.getIntExtra(KEY_SYSTEM_NOTIFICATION_ID, 0)
        roomToken = intent.getStringExtra(KEY_ROOM_TOKEN)
        messageId = intent.getIntExtra(KEY_MESSAGE_ID, 0)

        val id = intent.getLongExtra(KEY_INTERNAL_USER_ID, currentUserProvider.currentUser.blockingGet().id!!)
        currentUser = userManager.getUserWithId(id).blockingGet()

        markAsRead()
    }

    private fun markAsRead() {
        val credentials = GccApiUtils.getCredentials(currentUser.username, currentUser.token)
        val apiVersion = GccApiUtils.getChatApiVersion(currentUser.capabilities!!.spreedCapability!!, intArrayOf(1))
        val url = GccApiUtils.getUrlForChatReadMarker(
            apiVersion,
            currentUser.baseUrl!!,
            roomToken!!
        )

        ncApi.setChatReadMarker(credentials, url, messageId)
            ?.subscribeOn(Schedulers.io())
            ?.observeOn(AndroidSchedulers.mainThread())
            ?.subscribe(object : Observer<GenericOverall> {
                override fun onSubscribe(d: Disposable) {
                    // unused atm
                }

                override fun onNext(genericOverall: GenericOverall) {
                    cancelNotification(systemNotificationId!!)
                }

                override fun onError(e: Throwable) {
                    Log.e(TAG, "Failed to set chat read marker", e)
                }

                override fun onComplete() {
                    // unused atm
                }
            })
    }

    private fun cancelNotification(notificationId: Int) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(notificationId)
    }

    companion object {
        const val TAG = "GccMarkAsReadReceiver"
    }
}
