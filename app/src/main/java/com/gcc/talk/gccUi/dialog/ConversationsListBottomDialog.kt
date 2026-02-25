/*
 * Nextcloud Talk - Android Client
 *
 * SPDX-FileCopyrightText: 2022 Marcel Hibbe <dev@mhibbe.de>
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package com.gcc.talk.gccUi.dialog

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import androidx.work.Data
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkInfo
import androidx.work.WorkManager
import autodagger.AutoInjector
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.gcc.talk.R
import com.gcc.talk.gccActivities.GccMainActivity
import com.gcc.talk.gccApi.GccNcApi
import com.gcc.talk.gccApi.GccNcApiCoroutines
import com.gcc.talk.gccApplication.GccTalkApplication
import com.gcc.talk.gccConversation.GccRenameConversationDialogFragment
import com.gcc.talk.gccConversationinfo.viewmodel.GccConversationInfoViewModel
import com.gcc.talk.gccConversationlist.GccConversationsListActivity
import com.gcc.talk.gccData.user.model.GccUser
import com.gcc.talk.databinding.DialogConversationOperationsBinding
import com.gcc.talk.gccJobs.GccLeaveConversationWorker
import com.gcc.talk.gccModels.domain.GccConversationModel
import com.gcc.talk.gccModels.json.generic.GenericOverall
import com.gcc.talk.gccUi.theme.ViewThemeUtils
import com.gcc.talk.gccUsers.GccUserManager
import com.gcc.talk.gccUtils.GccApiUtils
import com.gcc.talk.gccUtils.CapabilitiesUtil
import com.gcc.talk.gccUtils.GccConversationUtils
import com.gcc.talk.gccUtils.GccShareUtils
import com.gcc.talk.gccUtils.SpreedFeatures
import com.gcc.talk.gccUtils.bundle.GccBundleKeys.KEY_INTERNAL_USER_ID
import com.gcc.talk.gccUtils.bundle.GccBundleKeys.KEY_ROOM_TOKEN
import com.gcc.talk.gccUtils.database.user.GccCurrentUserProviderOld
import io.reactivex.Observer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@AutoInjector(GccTalkApplication::class)
class ConversationsListBottomDialog(
    val activity: GccConversationsListActivity,
    val currentUser: GccUser,
    val conversation: GccConversationModel
) : BottomSheetDialog(activity) {

    private lateinit var binding: DialogConversationOperationsBinding

    @Inject
    lateinit var ncApi: GccNcApi

    @Inject
    lateinit var ncApiCoroutines: GccNcApiCoroutines

    @Inject
    lateinit var viewThemeUtils: ViewThemeUtils

    @Inject
    lateinit var conversationInfoViewModel: GccConversationInfoViewModel

    @Inject
    lateinit var userManager: GccUserManager

    @Inject
    lateinit var currentUserProvider: GccCurrentUserProviderOld

    lateinit var credentials: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        GccTalkApplication.sharedApplication?.componentApplication?.inject(this)

        binding = DialogConversationOperationsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)

        viewThemeUtils.material.colorBottomSheetBackground(binding.root)
        viewThemeUtils.material.colorBottomSheetDragHandle(binding.bottomSheetDragHandle)
        initHeaderDescription()
        initItemsVisibility()
        initClickListeners()

        credentials = GccApiUtils.getCredentials(currentUser.username, currentUser.token)!!
    }

    override fun onStart() {
        super.onStart()
        val bottomSheet = findViewById<View>(R.id.design_bottom_sheet)
        val behavior = BottomSheetBehavior.from(bottomSheet as View)
        behavior.state = BottomSheetBehavior.STATE_COLLAPSED
    }

    private fun initHeaderDescription() {
        if (!TextUtils.isEmpty(conversation.displayName)) {
            binding.conversationOperationHeader.text = conversation.displayName
        } else if (!TextUtils.isEmpty(conversation.name)) {
            binding.conversationOperationHeader.text = conversation.name
        }
    }

    private fun initItemsVisibility() {
        val hasFavoritesCapability = CapabilitiesUtil.hasSpreedFeatureCapability(
            currentUser.capabilities?.spreedCapability!!,
            SpreedFeatures.FAVORITES
        )

        binding.conversationRemoveFromFavorites.visibility = setVisibleIf(
            hasFavoritesCapability && conversation.favorite
        )
        binding.conversationAddToFavorites.visibility = setVisibleIf(
            hasFavoritesCapability && !conversation.favorite
        )

        binding.conversationMarkAsRead.visibility = setVisibleIf(
            conversation.unreadMessages > 0 &&
                CapabilitiesUtil.hasSpreedFeatureCapability(
                    currentUser.capabilities?.spreedCapability!!,
                    SpreedFeatures.CHAT_READ_MARKER
                )
        )

        binding.conversationMarkAsUnread.visibility = setVisibleIf(
            conversation.unreadMessages <= 0 &&
                CapabilitiesUtil.hasSpreedFeatureCapability(
                    currentUser.capabilities?.spreedCapability!!,
                    SpreedFeatures.CHAT_UNREAD
                )
        )

        binding.conversationOperationRename.visibility = setVisibleIf(
            GccConversationUtils.isNameEditable(conversation, currentUser.capabilities!!.spreedCapability!!)
        )
        binding.conversationLinkShare.visibility = setVisibleIf(
            !GccConversationUtils.isNoteToSelfConversation(conversation)
        )

        binding.conversationOperationDelete.visibility = setVisibleIf(
            conversation.canDeleteConversation
        )

        binding.conversationOperationLeave.visibility = setVisibleIf(
            conversation.canLeaveConversation
        )
    }

    private fun setVisibleIf(boolean: Boolean): Int =
        if (boolean) {
            View.VISIBLE
        } else {
            View.GONE
        }

    private fun initClickListeners() {
        binding.conversationAddToFavorites.setOnClickListener {
            addConversationToFavorites()
        }

        binding.conversationRemoveFromFavorites.setOnClickListener {
            removeConversationFromFavorites()
        }

        binding.conversationMarkAsRead.setOnClickListener {
            markConversationAsRead()
        }

        binding.conversationMarkAsUnread.setOnClickListener {
            markConversationAsUnread()
        }

        binding.conversationLinkShare.setOnClickListener {
            val canGeneratePrettyURL = CapabilitiesUtil.canGeneratePrettyURL(currentUser)
            GccShareUtils.shareConversationLink(
                activity,
                currentUser.baseUrl,
                conversation.token,
                conversation.name,
                canGeneratePrettyURL
            )
            dismiss()
        }

        binding.conversationArchiveText.text = if (conversation.hasArchived) {
            this.activity.resources.getString(R.string.unarchive_conversation)
        } else {
            this.activity.resources.getString(R.string.archive_conversation)
        }

        binding.conversationArchive.setOnClickListener {
            handleArchiving()
        }

        binding.conversationOperationRename.setOnClickListener {
            renameConversation()
        }

        binding.conversationOperationLeave.setOnClickListener {
            leaveConversation()
        }

        binding.conversationOperationDelete.setOnClickListener {
            deleteConversation()
        }
    }

    private fun handleArchiving() {
        val currentUser = currentUserProvider.currentUser.blockingGet()
        val token = conversation.token
        lifecycleScope.launch {
            if (conversation.hasArchived) {
                conversationInfoViewModel.unarchiveConversation(currentUser, token)
                activity.showSnackbar(
                    String.format(
                        context.resources.getString(R.string.unarchived_conversation),
                        conversation.displayName
                    )
                )
                dismiss()
            } else {
                conversationInfoViewModel.archiveConversation(currentUser, token)
                activity.showSnackbar(
                    String.format(
                        context.resources.getString(R.string.archived_conversation),
                        conversation.displayName
                    )
                )
                dismiss()
            }
        }
        activity.fetchRooms()
    }

    @Suppress("Detekt.TooGenericExceptionCaught")
    @SuppressLint("StringFormatInvalid", "TooGenericExceptionCaught")
    private fun addConversationToFavorites() {
        val apiVersion = GccApiUtils.getConversationApiVersion(currentUser, intArrayOf(GccApiUtils.API_V4, GccApiUtils.API_V1))
        val url = GccApiUtils.getUrlForRoomFavorite(apiVersion, currentUser.baseUrl!!, conversation.token)
        lifecycleScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    ncApiCoroutines.addConversationToFavorites(credentials, url)
                }
                activity.fetchRooms()
                activity.showSnackbar(
                    String.format(
                        context.resources.getString(R.string.added_to_favorites),
                        conversation.displayName
                    )
                )
                dismiss()
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    activity.showSnackbar(context.resources.getString(R.string.nc_common_error_sorry))
                    dismiss()
                }
            }
        }
    }

    @Suppress("Detekt.TooGenericExceptionCaught")
    @SuppressLint("StringFormatInvalid", "TooGenericExceptionCaught")
    private fun removeConversationFromFavorites() {
        val apiVersion = GccApiUtils.getConversationApiVersion(currentUser, intArrayOf(GccApiUtils.API_V4, GccApiUtils.API_V1))
        val url = GccApiUtils.getUrlForRoomFavorite(apiVersion, currentUser.baseUrl!!, conversation.token)
        lifecycleScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    ncApiCoroutines.removeConversationFromFavorites(credentials, url)
                }
                activity.fetchRooms()
                activity.showSnackbar(
                    String.format(
                        context.resources.getString(R.string.removed_from_favorites),
                        conversation.displayName
                    )
                )
                dismiss()
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    activity.showSnackbar(context.resources.getString(R.string.nc_common_error_sorry))
                    dismiss()
                }
            }
        }
    }

    private fun markConversationAsUnread() {
        ncApi.markRoomAsUnread(
            credentials,
            GccApiUtils.getUrlForChatReadMarker(
                chatApiVersion(),
                currentUser.baseUrl!!,
                conversation.token!!
            )
        )
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .retry(1)
            .subscribe(object : Observer<GenericOverall> {
                override fun onSubscribe(d: Disposable) {
                    // unused atm
                }

                @SuppressLint("StringFormatInvalid")
                override fun onNext(genericOverall: GenericOverall) {
                    activity.fetchRooms()
                    activity.showSnackbar(
                        String.format(
                            context.resources.getString(R.string.marked_as_unread),
                            conversation.displayName
                        )
                    )
                    dismiss()
                }

                override fun onError(e: Throwable) {
                    activity.showSnackbar(context.resources.getString(R.string.nc_common_error_sorry))
                    dismiss()
                }

                override fun onComplete() {
                    // unused atm
                }
            })
    }

    private fun markConversationAsRead() {
        val messageId = if (conversation.remoteServer.isNullOrEmpty()) {
            conversation.lastMessage?.id
        } else {
            null
        }

        ncApi.setChatReadMarker(
            credentials,
            GccApiUtils.getUrlForChatReadMarker(
                chatApiVersion(),
                currentUser.baseUrl!!,
                conversation.token!!
            ),
            messageId?.toInt()
        )
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .retry(1)
            .subscribe(object : Observer<GenericOverall> {
                override fun onSubscribe(d: Disposable) {
                    // unused atm
                }

                @SuppressLint("StringFormatInvalid")
                override fun onNext(genericOverall: GenericOverall) {
                    activity.fetchRooms()
                    activity.showSnackbar(
                        String.format(
                            context.resources.getString(R.string.marked_as_read),
                            conversation.displayName
                        )
                    )
                    dismiss()
                }

                override fun onError(e: Throwable) {
                    activity.showSnackbar(context.resources.getString(R.string.nc_common_error_sorry))
                    dismiss()
                }

                override fun onComplete() {
                    // unused atm
                }
            })
    }

    private fun renameConversation() {
        if (!TextUtils.isEmpty(conversation.token)) {
            dismiss()
            val conversationDialog = GccRenameConversationDialogFragment.newInstance(
                conversation.token!!,
                conversation.displayName!!
            )
            conversationDialog.show(
                activity.supportFragmentManager,
                TAG
            )
        }
    }

    @SuppressLint("StringFormatInvalid")
    private fun leaveConversation() {
        val dataBuilder = Data.Builder()
        dataBuilder.putString(KEY_ROOM_TOKEN, conversation.token)
        dataBuilder.putLong(KEY_INTERNAL_USER_ID, currentUser.id!!)
        val data = dataBuilder.build()

        val leaveConversationWorker =
            OneTimeWorkRequest.Builder(GccLeaveConversationWorker::class.java).setInputData(
                data
            ).build()
        WorkManager.getInstance().enqueue(leaveConversationWorker)

        WorkManager.getInstance(context).getWorkInfoByIdLiveData(leaveConversationWorker.id)
            .observeForever { workInfo: WorkInfo? ->
                if (workInfo != null) {
                    when (workInfo.state) {
                        WorkInfo.State.SUCCEEDED -> {
                            activity.showSnackbar(
                                String.format(
                                    context.resources.getString(R.string.left_conversation),
                                    conversation.displayName
                                )
                            )
                            val intent = Intent(context, GccMainActivity::class.java)
                            context.startActivity(intent)
                        }

                        WorkInfo.State.FAILED -> {
                            activity.showSnackbar(context.resources.getString(R.string.nc_common_error_sorry))
                        }

                        else -> {
                        }
                    }
                }
            }

        dismiss()
    }

    private fun deleteConversation() {
        if (!TextUtils.isEmpty(conversation.token)) {
            activity.showDeleteConversationDialog(conversation)
        }

        dismiss()
    }

    private fun chatApiVersion(): Int =
        GccApiUtils.getChatApiVersion(currentUser.capabilities!!.spreedCapability!!, intArrayOf(GccApiUtils.API_V1))

    companion object {
        val TAG = ConversationsListBottomDialog::class.simpleName
    }
}
