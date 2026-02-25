/*
 * Nextcloud Talk - Android Client
 *
 * SPDX-FileCopyrightText: 2021-2022 Tim Krüger <t@timkrueger.me>
 * SPDX-FileCopyrightText: 2021 Andy Scherzinger <info@andy-scherzinger.de>
 * SPDX-FileCopyrightText: 2021 Marcel Hibbe <dev@mhibbe.de>
 * SPDX-FileCopyrightText: 2017-2018 Mario Danic <mario@lovelyhq.com>
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package com.gcc.talk.gccAdapters.messages

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Handler
import android.util.Base64
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.emoji2.widget.EmojiTextView
import autodagger.AutoInjector
import com.google.android.material.card.MaterialCardView
import com.nextcloud.android.common.ui.theme.utils.ColorRole
import com.gcc.talk.R
import com.gcc.talk.gccApplication.GccTalkApplication
import com.gcc.talk.gccApplication.GccTalkApplication.Companion.sharedApplication
import com.gcc.talk.gccChat.GccChatActivity
import com.gcc.talk.gccChat.data.model.GccChatMessage
import com.gcc.talk.gccData.user.model.GccUser
import com.gcc.talk.databinding.ItemThreadTitleBinding
import com.gcc.talk.databinding.ReactionsInsideMessageBinding
import com.gcc.talk.gccExtensions.loadChangelogBotAvatar
import com.gcc.talk.gccExtensions.loadFederatedUserAvatar
import com.gcc.talk.gccFilebrowser.models.GccBrowserFile
import com.gcc.talk.gccFilebrowser.webdav.GccReadFilesystemOperation
import com.gcc.talk.gccUi.theme.ViewThemeUtils
import com.gcc.talk.gccUsers.GccUserManager
import com.gcc.talk.gccUtilss.GccDateUtils
import com.gcc.talk.gccUtils.GccDisplayUtils
import com.gcc.talk.gccUtils.GccDrawableUtils.getDrawableResourceIdForMimeType
import com.gcc.talk.gccUtils.GccFileViewerUtils
import com.gcc.talk.gccUtils.GccFileViewerUtils.ProgressUi
import com.gcc.talk.gccUtils.message.GccMessageUtils
import com.stfalcon.chatkit.messages.MessageHolders.IncomingImageMessageViewHolder
import io.reactivex.Single
import io.reactivex.SingleObserver
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import okhttp3.OkHttpClient
import java.io.ByteArrayInputStream
import java.io.IOException
import javax.inject.Inject

@AutoInjector(GccTalkApplication::class)
abstract class GccPreviewMessageViewHolder(itemView: View?, payload: Any?) :
    IncomingImageMessageViewHolder<GccChatMessage>(itemView, payload) {
    @JvmField
    @Inject
    var context: Context? = null

    @JvmField
    @Inject
    var viewThemeUtils: ViewThemeUtils? = null

    @Inject
    lateinit var dateUtils: GccDateUtils

    @Inject
    lateinit var messageUtils: GccMessageUtils

    @Inject
    lateinit var userManager: GccUserManager

    @JvmField
    @Inject
    var okHttpClient: OkHttpClient? = null
    open var progressBar: ProgressBar? = null
    open var reactionsBinding: ReactionsInsideMessageBinding? = null
    open var threadsBinding: ItemThreadTitleBinding? = null
    var fileViewerUtils: GccFileViewerUtils? = null
    var clickView: View? = null

    lateinit var commonMessageInterface: CommonMessageInterface
    var previewMessageInterface: GccPreviewMessageInterface? = null

    private var placeholder: Drawable? = null

    init {
        sharedApplication!!.componentApplication.inject(this)
    }

    @SuppressLint("SetTextI18n")
    @Suppress("NestedBlockDepth", "ComplexMethod", "LongMethod")
    override fun onBind(message: GccChatMessage) {
        super.onBind(message)
        image.minimumHeight = GccDisplayUtils.convertDpToPixel(MIN_IMAGE_HEIGHT, context!!).toInt()

        if (message.lastEditTimestamp != 0L && !message.isDeleted) {
            time.text = dateUtils.getLocalTimeStringFromTimestamp(message.lastEditTimestamp!!)
            messageEditIndicator.visibility = View.VISIBLE
        } else {
            time.text = dateUtils.getLocalTimeStringFromTimestamp(message.timestamp)
            messageEditIndicator.visibility = View.GONE
        }

        viewThemeUtils!!.platform.colorCircularProgressBar(progressBar!!, ColorRole.PRIMARY)
        clickView = image
        messageText.visibility = View.VISIBLE
        if (message.getCalculateMessageType() === GccChatMessage.MessageType.SINGLE_NC_ATTACHMENT_MESSAGE) {
            fileViewerUtils = GccFileViewerUtils(context!!, message.activeUser!!)
            val fileName = message.selectedIndividualHashMap!![KEY_NAME]

            messageText.text = fileName

            if (message.activeUser != null &&
                message.activeUser!!.username != null &&
                message.activeUser!!.baseUrl != null
            ) {
                clickView!!.setOnClickListener { v: View? ->
                    fileViewerUtils!!.openFile(
                        message,
                        ProgressUi(progressBar, messageText, image)
                    )
                }
                clickView!!.setOnLongClickListener {
                    previewMessageInterface!!.onPreviewMessageLongClick(message)
                    true
                }
            } else {
                Log.e(TAG, "failed to set click listener because activeUser, username or baseUrl were null")
            }
            fileViewerUtils!!.resumeToUpdateViewsByProgress(
                message.selectedIndividualHashMap!![KEY_NAME]!!,
                message.selectedIndividualHashMap!![KEY_ID]!!,
                message.selectedIndividualHashMap!![KEY_MIMETYPE],
                message.openWhenDownloaded,
                ProgressUi(progressBar, messageText, image)
            )
        } else if (message.getCalculateMessageType() === GccChatMessage.MessageType.SINGLE_LINK_GIPHY_MESSAGE) {
            messageText.text = "GIPHY"
            GccDisplayUtils.setClickableString("GIPHY", "https://giphy.com", messageText)
        } else if (message.getCalculateMessageType() === GccChatMessage.MessageType.SINGLE_LINK_TENOR_MESSAGE) {
            messageText.text = "Tenor"
            GccDisplayUtils.setClickableString("Tenor", "https://tenor.com", messageText)
        } else {
            if (message.messageType == GccChatMessage.MessageType.SINGLE_LINK_IMAGE_MESSAGE.name) {
                clickView!!.setOnClickListener {
                    val browserIntent = Intent(Intent.ACTION_VIEW, message.imageUrl!!.toUri())
                    browserIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    context!!.startActivity(browserIntent)
                }
            } else {
                clickView!!.setOnClickListener(null)
            }
            messageText.text = ""
        }
        itemView.setTag(R.string.replyable_message_view_tag, message.replyable)

        val chatActivity = commonMessageInterface as GccChatActivity
        GccThread().showThreadPreview(
            chatActivity,
            message,
            threadBinding = threadsBinding!!,
            reactionsBinding = reactionsBinding!!,
            openThread = { openThread(message) }
        )

        val paddingSide = GccDisplayUtils.convertDpToPixel(HORIZONTAL_REACTION_PADDING, context!!).toInt()
        GccReaction().showReactions(
            message,
            ::clickOnReaction,
            ::longClickOnReaction,
            reactionsBinding!!,
            messageText.context,
            true,
            viewThemeUtils!!,
            hasBubbleBackground(message)
        )
        reactionsBinding!!.reactionsEmojiWrapper.setPadding(paddingSide, 0, paddingSide, 0)

        if (userAvatar != null) {
            if (message.isGrouped || message.isOneToOneConversation) {
                if (message.isOneToOneConversation) {
                    userAvatar.visibility = View.GONE
                } else {
                    userAvatar.visibility = View.INVISIBLE
                }
            } else {
                userAvatar.visibility = View.VISIBLE
                userAvatar.setOnClickListener { v: View ->
                    if (payload is GccMessagePayload) {
                        (payload as GccMessagePayload).profileBottomSheet.showFor(
                            message,
                            v.context
                        )
                    }
                }
                if (ACTOR_TYPE_BOTS == message.actorType && ACTOR_ID_CHANGELOG == message.actorId) {
                    userAvatar.loadChangelogBotAvatar()
                } else if (message.actorType == "federated_users") {
                    userAvatar.loadFederatedUserAvatar(message)
                }
            }
        }

        messageCaption.setOnClickListener(null)
        messageCaption.setOnLongClickListener {
            previewMessageInterface!!.onPreviewMessageLongClick(message)
            true
        }
    }

    private fun longClickOnReaction(chatMessage: GccChatMessage) {
        commonMessageInterface.onLongClickReactions(chatMessage)
    }

    private fun clickOnReaction(chatMessage: GccChatMessage, emoji: String) {
        commonMessageInterface.onClickReaction(chatMessage, emoji)
    }

    private fun openThread(chatMessage: GccChatMessage) {
        commonMessageInterface.openThread(chatMessage)
    }

    override fun getPayloadForImageLoader(message: GccChatMessage?): Any? {
        if (message!!.selectedIndividualHashMap!!.containsKey(KEY_CONTACT_NAME)) {
            previewContainer.visibility = View.GONE
            previewContactContainer.visibility = View.VISIBLE
            previewContactName.text = message.selectedIndividualHashMap!![KEY_CONTACT_NAME]
            progressBar = previewContactProgressBar
            messageText.visibility = View.INVISIBLE
            clickView = previewContactContainer
            viewThemeUtils!!.talk.colorContactChatItemBackground(previewContactContainer)
            viewThemeUtils!!.talk.colorContactChatItemName(previewContactName)
            viewThemeUtils!!.platform.colorCircularProgressBar(
                previewContactProgressBar!!,
                ColorRole.ON_PRIMARY_CONTAINER
            )

            if (message.selectedIndividualHashMap!!.containsKey(KEY_CONTACT_PHOTO)) {
                image = previewContactPhoto
                placeholder = getDrawableFromContactDetails(
                    context,
                    message.selectedIndividualHashMap!![KEY_CONTACT_PHOTO]
                )
            } else {
                image = previewContactPhoto
                image.setImageDrawable(ContextCompat.getDrawable(context!!, R.drawable.ic_mimetype_text_vcard))
            }
        } else {
            previewContainer.visibility = View.VISIBLE
            previewContactContainer.visibility = View.GONE
        }

        if (message.selectedIndividualHashMap!!.containsKey(KEY_MIMETYPE)) {
            val mimetype = message.selectedIndividualHashMap!![KEY_MIMETYPE]
            val drawableResourceId = getDrawableResourceIdForMimeType(mimetype)
            var drawable = ContextCompat.getDrawable(context!!, drawableResourceId)
            if (drawable != null &&
                (
                    drawableResourceId == R.drawable.ic_mimetype_folder ||
                        drawableResourceId == R.drawable.ic_mimetype_package_x_generic
                    )
            ) {
                drawable = viewThemeUtils?.platform?.tintDrawable(context!!, drawable)
            }
            placeholder = drawable
        } else {
            fetchFileInformation(
                "/" + message.selectedIndividualHashMap!![KEY_PATH],
                message.activeUser
            )
        }

        return placeholder
    }

    private fun getDrawableFromContactDetails(context: Context?, base64: String?): Drawable? {
        var drawable: Drawable? = null
        if (base64 != "") {
            val inputStream = ByteArrayInputStream(
                Base64.decode(base64!!.toByteArray(), Base64.DEFAULT)
            )
            drawable = Drawable.createFromResourceStream(
                context!!.resources,
                null,
                inputStream,
                null,
                null
            )
            try {
                inputStream.close()
            } catch (e: IOException) {
                Log.e(TAG, "failed to close stream in getDrawableFromContactDetails", e)
            }
        }
        if (drawable == null) {
            drawable = ContextCompat.getDrawable(context!!, R.drawable.ic_mimetype_text_vcard)
        }
        return drawable
    }

    private fun fetchFileInformation(url: String, activeUser: GccUser?) {
        Single.fromCallable { GccReadFilesystemOperation(okHttpClient, activeUser, url, 0) }
            .observeOn(Schedulers.io())
            .subscribe(object : SingleObserver<GccReadFilesystemOperation> {
                override fun onSubscribe(d: Disposable) {
                    // unused atm
                }

                override fun onSuccess(readFilesystemOperation: GccReadFilesystemOperation) {
                    val davResponse = readFilesystemOperation.readRemotePath()
                    if (davResponse.data != null) {
                        val browserFileList = davResponse.data as List<GccBrowserFile>
                        if (browserFileList.isNotEmpty()) {
                            Handler(context!!.mainLooper).post {
                                val resourceId = getDrawableResourceIdForMimeType(browserFileList[0].mimeType)
                                placeholder = ContextCompat.getDrawable(context!!, resourceId)
                            }
                        }
                    }
                }

                override fun onError(e: Throwable) {
                    Log.e(TAG, "Error reading file information", e)
                }
            })
    }

    fun assignCommonMessageInterface(commonMessageInterface: CommonMessageInterface) {
        this.commonMessageInterface = commonMessageInterface
    }

    fun assignPreviewMessageInterface(previewMessageInterface: GccPreviewMessageInterface?) {
        this.previewMessageInterface = previewMessageInterface
    }

    fun hasBubbleBackground(message: GccChatMessage): Boolean = !message.isVoiceMessage && message.message != "{file}"

    abstract val messageText: EmojiTextView
    abstract val messageCaption: EmojiTextView
    abstract val previewContainer: View
    abstract val previewContactContainer: MaterialCardView
    abstract val previewContactPhoto: ImageView
    abstract val previewContactName: EmojiTextView
    abstract val previewContactProgressBar: ProgressBar?
    abstract val messageEditIndicator: TextView

    companion object {
        private const val TAG = "PreviewMsgViewHolder"
        const val KEY_CONTACT_NAME = "contact-name"
        const val KEY_CONTACT_PHOTO = "contact-photo"
        const val KEY_MIMETYPE = "mimetype"
        const val KEY_ID = "id"
        const val KEY_PATH = "path"
        const val ACTOR_TYPE_BOTS = "bots"
        const val ACTOR_ID_CHANGELOG = "changelog"
        const val KEY_NAME = "name"
        const val MIN_IMAGE_HEIGHT = 100F
        const val HORIZONTAL_REACTION_PADDING = 8.0F
    }
}
