/*
 * Nextcloud Talk - Android Client
 *
 * SPDX-FileCopyrightText: 2021 Tim Krüger <t@timkrueger.me>
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package com.gcc.talk.gccUi.bottom.sheet

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.core.net.toUri
import com.afollestad.materialdialogs.LayoutMode
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.bottomsheets.BottomSheet
import com.gcc.talk.R
import com.gcc.talk.gccApi.GccNcApi
import com.gcc.talk.gccBottomsheet.items.BasicListItemWithImage
import com.gcc.talk.gccBottomsheet.items.listItemsWithImage
import com.gcc.talk.gccChat.GccChatActivity
import com.gcc.talk.gccChat.data.model.GccChatMessage
import com.gcc.talk.gccData.user.model.GccUser
import com.gcc.talk.gccModels.json.conversations.RoomOverall
import com.gcc.talk.gccModels.json.hovercard.HoverCardAction
import com.gcc.talk.gccModels.json.hovercard.HoverCardOverall
import com.gcc.talk.gccModels.json.participants.Participant
import com.gcc.talk.gccUi.bottom.sheet.ProfileBottomSheet.AllowedAppIds.EMAIL
import com.gcc.talk.gccUi.bottom.sheet.ProfileBottomSheet.AllowedAppIds.PROFILE
import com.gcc.talk.gccUi.bottom.sheet.ProfileBottomSheet.AllowedAppIds.SPREED
import com.gcc.talk.gccUi.theme.ViewThemeUtils
import com.gcc.talk.gccUtils.GccApiUtils
import com.gcc.talk.gccUtils.bundle.GccBundleKeys
import io.reactivex.Observer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers

private const val TAG = "ProfileBottomSheet"

class ProfileBottomSheet(val ncApi: GccNcApi, val userModel: GccUser, val viewThemeUtils: ViewThemeUtils) {

    private val allowedAppIds = listOf(SPREED.stringValue, PROFILE.stringValue, EMAIL.stringValue)

    fun showFor(message: GccChatMessage, context: Context) {
        if (message.actorType == Participant.ActorType.FEDERATED.toString()) {
            Log.d(TAG, "no actions for federated users are shown")
            return
        }

        ncApi.hoverCard(
            GccApiUtils.getCredentials(userModel.username, userModel.token),
            GccApiUtils.getUrlForHoverCard(userModel.baseUrl!!, message.actorId!!)
        ).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : Observer<HoverCardOverall> {
                override fun onSubscribe(d: Disposable) {
                    // unused atm
                }

                override fun onNext(hoverCardOverall: HoverCardOverall) {
                    bottomSheet(
                        hoverCardOverall.ocs!!.data!!.actions!!,
                        hoverCardOverall.ocs!!.data!!.displayName!!,
                        message.actorId!!,
                        context
                    )
                }

                override fun onError(e: Throwable) {
                    Log.e(TAG, "Failed to get hover card for user " + message.actorId, e)
                }

                override fun onComplete() {
                    // unused atm
                }
            })
    }

    @SuppressLint("CheckResult")
    private fun bottomSheet(actions: List<HoverCardAction>, displayName: String, userId: String, context: Context) {
        val filteredActions = actions.filter { allowedAppIds.contains(it.appId) }
        val items = filteredActions.map { configureActionListItem(it) }

        MaterialDialog(context, BottomSheet(LayoutMode.WRAP_CONTENT)).show {
            cornerRadius(res = R.dimen.corner_radius)
            viewThemeUtils.material.colorBottomSheetBackground(this.view)

            title(text = displayName)
            listItemsWithImage(items = items) { _, index, _ ->

                val action = filteredActions[index]

                when (AllowedAppIds.createFor(action)) {
                    PROFILE -> openProfile(action.hyperlink!!, context)
                    EMAIL -> composeEmail(action.title!!, context)
                    SPREED -> talkTo(userId, context)
                }
            }
        }
    }

    private fun configureActionListItem(action: HoverCardAction): BasicListItemWithImage {
        val drawable = when (AllowedAppIds.createFor(action)) {
            PROFILE -> R.drawable.ic_user
            EMAIL -> R.drawable.ic_email
            SPREED -> R.drawable.ic_talk
        }

        return BasicListItemWithImage(
            drawable,
            action.title!!
        )
    }

    private fun talkTo(userId: String, context: Context) {
        val apiVersion =
            GccApiUtils.getConversationApiVersion(userModel, intArrayOf(GccApiUtils.API_V4, 1))
        val retrofitBucket = GccApiUtils.getRetrofitBucketForCreateRoom(
            version = apiVersion,
            baseUrl = userModel.baseUrl!!,
            roomType = "1",
            invite = userId
        )
        val credentials = GccApiUtils.getCredentials(userModel.username, userModel.token)
        ncApi.createRoom(
            credentials,
            retrofitBucket.url,
            retrofitBucket.queryMap
        )
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : Observer<RoomOverall> {
                override fun onSubscribe(d: Disposable) {
                    // unused atm
                }

                override fun onNext(roomOverall: RoomOverall) {
                    val bundle = Bundle()
                    bundle.putString(GccBundleKeys.KEY_ROOM_TOKEN, roomOverall.ocs!!.data!!.token)
                    // bundle.putString(GccBundleKeys.KEY_ROOM_ID, roomOverall.ocs!!.data!!.roomId)

                    val chatIntent = Intent(context, GccChatActivity::class.java)
                    chatIntent.putExtras(bundle)
                    chatIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    context.startActivity(chatIntent)
                }

                override fun onError(e: Throwable) {
                    Log.e(TAG, e.message, e)
                }

                override fun onComplete() {
                    // unused atm
                }
            })
    }

    private fun composeEmail(address: String, context: Context) {
        val addresses = arrayListOf(address)
        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = "mailto:".toUri() // only email apps should handle this
            putExtra(Intent.EXTRA_EMAIL, addresses)
        }
        context.startActivity(intent)
    }

    private fun openProfile(hyperlink: String, context: Context) {
        val webpage: Uri = hyperlink.toUri()
        val intent = Intent(Intent.ACTION_VIEW, webpage)
        context.startActivity(intent)
    }

    enum class AllowedAppIds(val stringValue: String) {
        SPREED("spreed"),
        PROFILE("profile"),
        EMAIL("email");

        companion object {
            fun createFor(action: HoverCardAction): AllowedAppIds = valueOf(action.appId!!.uppercase())
        }
    }
}
