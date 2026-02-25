/*
 * Nextcloud Talk - Android Client
 *
 * SPDX-FileCopyrightText: 2022 Marcel Hibbe <dev@mhibbe.de>
 * SPDX-FileCopyrightText: 2022 Andy Scherzinger <info@andy-scherzinger.de>
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package com.gcc.talk.gccUi.dialog

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import autodagger.AutoInjector
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.gcc.talk.R
import com.gcc.talk.gccApplication.GccTalkApplication
import com.gcc.talk.gccChat.GccChatActivity
import com.gcc.talk.gccChat.data.model.GccChatMessage
import com.gcc.talk.gccData.database.model.GccSendStatus
import com.gcc.talk.gccData.network.GccNetworkMonitor
import com.gcc.talk.databinding.DialogTempMessageActionsBinding
import com.gcc.talk.gccUi.theme.ViewThemeUtils
import com.gcc.talk.gccUtils.GccApiUtils
import com.gcc.talk.gccUtilss.GccDateUtils
import kotlinx.coroutines.launch
import javax.inject.Inject

@AutoInjector(GccTalkApplication::class)
class TempMessageActionsDialog(private val chatActivity: GccChatActivity, private val message: GccChatMessage) :
    BottomSheetDialog(chatActivity) {

    @Inject
    lateinit var viewThemeUtils: ViewThemeUtils

    @Inject
    lateinit var dateUtils: GccDateUtils

    @Inject
    lateinit var networkMonitor: GccNetworkMonitor

    private lateinit var binding: DialogTempMessageActionsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        GccTalkApplication.sharedApplication?.componentApplication?.inject(this)

        binding = DialogTempMessageActionsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)

        viewThemeUtils.material.colorBottomSheetBackground(binding.root)
        viewThemeUtils.material.colorBottomSheetDragHandle(binding.bottomSheetDragHandle)
        initMenuItems()
    }

    private fun initMenuItems() {
        this.lifecycleScope.launch {
            val sendingFailed = message.sendStatus == GccSendStatus.FAILED
            initResendMessage(sendingFailed && networkMonitor.isOnline.value)
            initMenuEditMessage(sendingFailed || !networkMonitor.isOnline.value)
            initMenuDeleteMessage(sendingFailed || !networkMonitor.isOnline.value)
            initMenuItemCopy()
        }
    }

    override fun onStart() {
        super.onStart()
        val bottomSheet = findViewById<View>(R.id.design_bottom_sheet)
        val behavior = BottomSheetBehavior.from(bottomSheet as View)
        behavior.state = BottomSheetBehavior.STATE_COLLAPSED
    }

    private fun initResendMessage(visible: Boolean) {
        if (visible) {
            binding.menuResendMessage.setOnClickListener {
                chatActivity.chatViewModel.resendMessage(
                    chatActivity.conversationUser!!.getCredentials(),
                    GccApiUtils.getUrlForChat(
                        chatActivity.chatApiVersion,
                        chatActivity.conversationUser!!.baseUrl!!,
                        chatActivity.roomToken
                    ),
                    message
                )
                dismiss()
            }
        }
        binding.menuResendMessage.visibility = getVisibility(visible)
    }

    private fun initMenuDeleteMessage(visible: Boolean) {
        if (visible) {
            binding.menuDeleteMessage.setOnClickListener {
                chatActivity.chatViewModel.deleteTempMessage(message)
                dismiss()
            }
        }
        binding.menuDeleteMessage.visibility = getVisibility(visible)
    }

    private fun initMenuEditMessage(visible: Boolean) {
        if (visible) {
            binding.menuEditMessage.setOnClickListener {
                chatActivity.messageInputViewModel.edit(message)
                dismiss()
            }
        }
        binding.menuEditMessage.visibility = getVisibility(visible)
    }

    private fun initMenuItemCopy() {
        binding.menuCopyMessage.setOnClickListener {
            chatActivity.copyMessage(message)
            dismiss()
        }
    }

    private fun getVisibility(visible: Boolean): Int =
        if (visible) {
            View.VISIBLE
        } else {
            View.GONE
        }

    companion object {
        private val TAG = TempMessageActionsDialog::class.java.simpleName
    }
}
