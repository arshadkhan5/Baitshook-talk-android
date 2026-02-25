/*
 * Nextcloud Talk - Android Client
 *
 * SPDX-FileCopyrightText: 2022 Dariusz Olszewski <starypatyk@gmail.com>
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package com.gcc.talk.gccReceivers

import android.Manifest
import android.app.Notification
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.Person
import androidx.core.app.RemoteInput
import autodagger.AutoInjector
import com.gcc.talk.R
import com.gcc.talk.gccApi.GccNcApi
import com.gcc.talk.gccApplication.GccTalkApplication
import com.gcc.talk.gccData.user.model.GccUser
import com.gcc.talk.gccModels.json.chat.ChatOverallSingleMessage
import com.gcc.talk.gccUsers.GccUserManager
import com.gcc.talk.gccUtils.GccApiUtils
import com.gcc.talk.gccUtils.GccNotificationUtils
import com.gcc.talk.gccUtils.bundle.GccBundleKeys.KEY_INTERNAL_USER_ID
import com.gcc.talk.gccUtils.bundle.GccBundleKeys.KEY_ROOM_TOKEN
import com.gcc.talk.gccUtils.bundle.GccBundleKeys.KEY_SYSTEM_NOTIFICATION_ID
import com.gcc.talk.gccUtils.database.user.GccCurrentUserProviderOld
import com.gcc.talk.gccUtils.message.GccSendMessageUtils
import io.reactivex.Observer
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

@AutoInjector(GccTalkApplication::class)
class GccDirectReplyReceiver : BroadcastReceiver() {

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
    private var replyMessage: CharSequence? = null

    init {
        GccTalkApplication.sharedApplication!!.componentApplication.inject(this)
    }

    override fun onReceive(receiveContext: Context, intent: Intent?) {
        context = receiveContext

        // NOTE - systemNotificationId is an internal ID used on the device only.
        // It is NOT the same as the notification ID used in communication with the server.
        systemNotificationId = intent!!.getIntExtra(KEY_SYSTEM_NOTIFICATION_ID, 0)
        roomToken = intent.getStringExtra(KEY_ROOM_TOKEN)

        val id = intent.getLongExtra(KEY_INTERNAL_USER_ID, currentUserProvider.currentUser.blockingGet().id!!)
        currentUser = userManager.getUserWithId(id).blockingGet()

        replyMessage = getMessageText(intent)
        sendDirectReply()
    }

    private fun getMessageText(intent: Intent): CharSequence? =
        RemoteInput.getResultsFromIntent(intent)?.getCharSequence(GccNotificationUtils.KEY_DIRECT_REPLY)

    private fun sendDirectReply() {
        val credentials = GccApiUtils.getCredentials(currentUser.username, currentUser.token)
        val apiVersion = GccApiUtils.getChatApiVersion(currentUser.capabilities!!.spreedCapability!!, intArrayOf(1))
        val url = GccApiUtils.getUrlForChat(apiVersion, currentUser.baseUrl!!, roomToken!!)

        ncApi.sendChatMessage(
            credentials,
            url,
            replyMessage,
            currentUser.displayName,
            null,
            false,
            GccSendMessageUtils().generateReferenceId()
        )
            ?.subscribeOn(Schedulers.io())
            ?.observeOn(AndroidSchedulers.mainThread())
            ?.subscribe(object : Observer<ChatOverallSingleMessage> {
                override fun onSubscribe(d: Disposable) {
                    // unused atm
                }

                override fun onNext(message: ChatOverallSingleMessage) {
                    confirmReplySent()
                }

                override fun onError(e: Throwable) {
                    Log.e(TAG, "Failed to send reply", e)
                    informReplyFailed()
                }

                override fun onComplete() {
                    // unused atm
                }
            })
    }

    private fun confirmReplySent() {
        appendMessageToNotification(replyMessage!!)
    }

    private fun informReplyFailed() {
        val errorColor = ForegroundColorSpan(context.resources.getColor(R.color.medium_emphasis_text, context.theme))
        val errorMessageHeader = context.resources.getString(R.string.nc_message_failed_to_send)
        val errorMessage = SpannableStringBuilder().append("$errorMessageHeader\n$replyMessage", errorColor, 0)
        appendMessageToNotification(errorMessage)
    }

    private fun findActiveNotification(notificationId: Int): Notification? {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        return notificationManager.activeNotifications.find { it.id == notificationId }?.notification
    }

    private fun appendMessageToNotification(reply: CharSequence) {
        // Implementation inspired by the SO question and article below:
        // https://stackoverflow.com/questions/51549456/android-o-notification-for-direct-reply-message
        // https://medium.com/@sidorovroman3/android-how-to-use-messagingstyle-for-notifications-without-caching-messages-c414ef2b816c
        //
        // Tries to follow "Best practices for messaging apps" described here:
        // https://developer.android.com/training/notify-user/build-notification#messaging-best-practices

        // Find the original (active) notification
        val previousNotification = findActiveNotification(systemNotificationId!!) ?: return

        // Recreate builder based on the active notification
        val previousBuilder = NotificationCompat.Builder(context, previousNotification)

        // Extract MessagingStyle from the active notification
        val previousStyle = NotificationCompat.MessagingStyle
            .extractMessagingStyleFromNotification(previousNotification)

        // Add reply
        Single.fromCallable {
            val avatarUrl = GccApiUtils.getUrlForAvatar(currentUser.baseUrl!!, currentUser.userId, false)
            val me = Person.Builder()
                .setName(currentUser.displayName)
                .setIcon(GccNotificationUtils.loadAvatarSync(avatarUrl, context))
                .build()
            val message = NotificationCompat.MessagingStyle.Message(reply, System.currentTimeMillis(), me)
            previousStyle?.addMessage(message)

            // Set the updated style
            previousBuilder.setStyle(previousStyle)

            if (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                // Check if notification still exists
                if (findActiveNotification(systemNotificationId!!) != null) {
                    NotificationManagerCompat.from(context).notify(systemNotificationId!!, previousBuilder.build())
                }
            }
        }
            .subscribeOn(Schedulers.io())
            .subscribe()
    }

    companion object {
        const val TAG = "GccDirectReplyReceiver"
    }
}
