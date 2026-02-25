/*
 * Nextcloud Talk - Android Client
 *
 * SPDX-FileCopyrightText: 2024 Marcel Hibbe <dev@mhibbe.de>
 * SPDX-FileCopyrightText: 2023 Julius Linus <juliuslinus1@gmail.com>
 * SPDX-FileCopyrightText: 2022 Tim Krüger <t@timkrueger.me>
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package com.gcc.talk.gccConversationinfo

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.LifecycleOwner
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.gcc.talk.R
import com.gcc.talk.gccConversationinfo.viewmodel.GccConversationInfoViewModel
import com.gcc.talk.gccData.user.model.GccUser
import com.gcc.talk.databinding.ActivityConversationInfoBinding
import com.gcc.talk.databinding.DialogPasswordBinding
import com.gcc.talk.gccModels.domain.GccConversationModel
import com.gcc.talk.gccModels.json.capabilities.SpreedCapability
import com.gcc.talk.gccModels.json.conversations.ConversationEnums
import com.gcc.talk.gccRepositories.conversations.GccConversationsRepository
import com.gcc.talk.gccUtils.GccApiUtils
import com.gcc.talk.gccUtils.GccConversationUtils
import io.reactivex.Observer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers

class GccGuestAccessHelper(
    private val activity: GccConversationInfoActivity,
    private val binding: ActivityConversationInfoBinding,
    private val conversation: GccConversationModel,
    private val spreedCapabilities: SpreedCapability,
    private val conversationUser: GccUser,
    private val viewModel: GccConversationInfoViewModel,
    private val lifecycleOwner: LifecycleOwner
) {
    private val conversationsRepository = activity.conversationsRepository
    private val viewThemeUtils = activity.viewThemeUtils
    private val context = activity.context

    fun setupGuestAccess() {
        if (GccConversationUtils.canModerate(conversation, spreedCapabilities)) {
            binding.guestAccessView.guestAccessSettings.visibility = View.VISIBLE
        } else {
            binding.guestAccessView.guestAccessSettings.visibility = View.GONE
        }

        if (conversation.type == ConversationEnums.ConversationType.ROOM_PUBLIC_CALL) {
            binding.guestAccessView.allowGuestsSwitch.isChecked = true
            showAllOptions()
            if (conversation.hasPassword) {
                binding.guestAccessView.passwordProtectionSwitch.isChecked = true
            }
        } else {
            binding.guestAccessView.allowGuestsSwitch.isChecked = false
            hideAllOptions()
        }

        binding.guestAccessView.guestAccessSettingsAllowGuest.setOnClickListener {
            val isChecked = binding.guestAccessView.allowGuestsSwitch.isChecked
            binding.guestAccessView.allowGuestsSwitch.isChecked = !isChecked
            viewModel.allowGuests(
                conversationUser,
                conversation.token,
                !isChecked
            )
            viewModel.allowGuestsViewState.observe(lifecycleOwner) { uiState ->
                when (uiState) {
                    is GccConversationInfoViewModel.AllowGuestsUIState.Success -> {
                        binding.guestAccessView.allowGuestsSwitch.isChecked = uiState.allow
                        if (uiState.allow) {
                            showAllOptions()
                        } else {
                            hideAllOptions()
                        }
                    }
                    is GccConversationInfoViewModel.AllowGuestsUIState.Error -> {
                        val exception = uiState.exception
                        val message = context.getString(R.string.nc_guest_access_allow_failed)
                        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show()
                        Log.e(TAG, message, exception)
                    }
                    GccConversationInfoViewModel.AllowGuestsUIState.None -> {
                    }
                }
            }
        }

        binding.guestAccessView.guestAccessSettingsPasswordProtection.setOnClickListener {
            val isChecked = binding.guestAccessView.passwordProtectionSwitch.isChecked
            binding.guestAccessView.passwordProtectionSwitch.isChecked = !isChecked
            if (isChecked) {
                val apiVersion = GccApiUtils.getConversationApiVersion(
                    conversationUser,
                    intArrayOf(GccApiUtils.API_V4, GccApiUtils.API_V1)
                )
                val url = GccApiUtils.getUrlForRoomPassword(
                    apiVersion,
                    conversationUser.baseUrl!!,
                    conversation.token
                )
                viewModel.setPassword(
                    user = conversationUser,
                    url = url,
                    password = ""
                )
                passwordObserver()
            } else {
                showPasswordDialog()
            }
        }

        binding.guestAccessView.resendInvitationsButton.setOnClickListener {
            val apiVersion = GccApiUtils.getConversationApiVersion(conversationUser, intArrayOf(GccApiUtils.API_V4))
            val url = GccApiUtils.getUrlForParticipantsResendInvitations(
                apiVersion,
                conversationUser.baseUrl!!,
                conversation.token
            )

            conversationsRepository.resendInvitations(
                user = conversationUser,
                url = url
            ).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread()).subscribe(ResendInvitationsObserver())
        }
    }

    private fun passwordObserver() {
        viewModel.passwordViewState.observe(lifecycleOwner) { uiState ->
            when (uiState) {
                is GccConversationInfoViewModel.PasswordUiState.Success -> {
                    // unused atm
                }
                is GccConversationInfoViewModel.PasswordUiState.Error -> {
                    val exception = uiState.exception
                    val message = context.getString(R.string.nc_guest_access_password_failed)
                    Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show()
                    Log.e(TAG, message, exception)
                }
                is GccConversationInfoViewModel.PasswordUiState.None -> {
                    // unused atm
                }
            }
        }
    }

    private fun showPasswordDialog() {
        val builder = MaterialAlertDialogBuilder(activity)
        builder.apply {
            val dialogPassword = DialogPasswordBinding.inflate(LayoutInflater.from(context))
            viewThemeUtils.platform.colorEditText(dialogPassword.password)
            setView(dialogPassword.root)
            setTitle(R.string.nc_guest_access_password_dialog_title)
            setPositiveButton(R.string.nc_ok) { _, _ ->
                val apiVersion = GccApiUtils.getConversationApiVersion(
                    conversationUser,
                    intArrayOf(GccApiUtils.API_V4, GccApiUtils.API_V1)
                )
                val url = GccApiUtils.getUrlForRoomPassword(
                    apiVersion,
                    conversationUser.baseUrl!!,
                    conversation.token
                )
                val password = dialogPassword.password.text.toString()
                viewModel.setPassword(
                    user = conversationUser,
                    url = url,
                    password = password
                )
            }
            setNegativeButton(R.string.nc_cancel) { _, _ ->
                binding.guestAccessView.passwordProtectionSwitch.isChecked = false
            }
        }
        createDialog(builder)
        passwordObserver()
    }

    private fun createDialog(builder: MaterialAlertDialogBuilder) {
        builder.create()
        viewThemeUtils.dialog.colorMaterialAlertDialogBackground(binding.conversationInfoName.context, builder)
        val dialog = builder.show()
        viewThemeUtils.platform.colorTextButtons(
            dialog.getButton(AlertDialog.BUTTON_POSITIVE),
            dialog.getButton(AlertDialog.BUTTON_NEGATIVE)
        )
    }

    inner class ResendInvitationsObserver : Observer<GccConversationsRepository.ResendInvitationsResult> {

        private lateinit var resendInvitationsResult: GccConversationsRepository.ResendInvitationsResult

        override fun onSubscribe(d: Disposable) = Unit

        override fun onNext(t: GccConversationsRepository.ResendInvitationsResult) {
            resendInvitationsResult = t
        }

        override fun onError(e: Throwable) {
            val message = context.getString(R.string.nc_guest_access_resend_invitations_failed)
            Snackbar.make(binding.root, message, Snackbar.LENGTH_SHORT).show()
            Log.e(TAG, message, e)
        }

        override fun onComplete() {
            if (resendInvitationsResult.successful) {
                Snackbar.make(
                    binding.root,
                    R.string.nc_guest_access_resend_invitations_successful,
                    Snackbar.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun showAllOptions() {
        binding.guestAccessView.guestAccessSettingsPasswordProtection.visibility = View.VISIBLE
        if (conversationUser.capabilities?.spreedCapability?.features?.contains("sip-support") == true) {
            binding.guestAccessView.resendInvitationsButton.visibility = View.VISIBLE
        }
    }

    private fun hideAllOptions() {
        binding.guestAccessView.guestAccessSettingsPasswordProtection.visibility = View.GONE
        binding.guestAccessView.resendInvitationsButton.visibility = View.GONE
    }

    companion object {
        private val TAG = GccGuestAccessHelper::class.simpleName
    }
}
