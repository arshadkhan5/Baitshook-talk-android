/*
 * Nextcloud Talk - Android Client
 *
 * SPDX-FileCopyrightText: 2024 Julius Linus juliuslinus1@gmail.com
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.gcc.talk.gccChat

import android.os.Bundle
import android.os.SystemClock
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import autodagger.AutoInjector
import com.nextcloud.android.common.ui.theme.utils.ColorRole
import com.gcc.talk.R
import com.gcc.talk.gccApplication.GccTalkApplication
import com.gcc.talk.gccApplication.GccTalkApplication.Companion.sharedApplication
import com.gcc.talk.gccChat.data.io.GccAudioFocusRequestManager
import com.gcc.talk.gccChat.viewmodels.GccMessageInputViewModel
import com.gcc.talk.databinding.FragmentMessageInputVoiceRecordingBinding
import com.gcc.talk.gccUi.theme.ViewThemeUtils
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@AutoInjector(GccTalkApplication::class)
class GccMessageInputVoiceRecordingFragment : Fragment() {
    companion object {
        val TAG: String = GccMessageInputVoiceRecordingFragment::class.java.simpleName
        private const val SEEK_LIMIT = 98

        @JvmStatic
        fun newInstance() = GccMessageInputVoiceRecordingFragment()
    }

    @Inject
    lateinit var viewThemeUtils: ViewThemeUtils

    private val messageInputViewModel: GccMessageInputViewModel by activityViewModels()

    lateinit var binding: FragmentMessageInputVoiceRecordingBinding
    private lateinit var chatActivity: GccChatActivity
    private var pause = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedApplication!!.componentApplication.inject(this)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentMessageInputVoiceRecordingBinding.inflate(inflater)
        chatActivity = (requireActivity() as GccChatActivity)
        themeVoiceRecordingView()
        initVoiceRecordingView()
        initObservers()
        this.lifecycle.addObserver(messageInputViewModel)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        messageInputViewModel.stopMediaPlayer() // if it wasn't stopped already
        this.lifecycle.removeObserver(messageInputViewModel)
    }

    private fun initObservers() {
        messageInputViewModel.startMicInput(requireContext())
        messageInputViewModel.micInputAudioObserver.observe(viewLifecycleOwner) {
            binding.micInputCloud.setRotationSpeed(it.first, it.second)
        }

        lifecycleScope.launch {
            messageInputViewModel.mediaPlayerSeekbarObserver.onEach { progress ->
                if (progress >= SEEK_LIMIT) {
                    togglePausePlay()
                    binding.seekbar.progress = 0
                } else if (!pause && messageInputViewModel.isVoicePreviewPlaying.value == true) {
                    binding.seekbar.progress = progress
                }
            }.collect()
        }

        messageInputViewModel.getAudioFocusChange.observe(viewLifecycleOwner) { state ->
            when (state) {
                GccAudioFocusRequestManager.ManagerState.AUDIO_FOCUS_CHANGE_LOSS -> {
                    if (messageInputViewModel.isVoicePreviewPlaying.value == true) {
                        messageInputViewModel.stopMediaPlayer()
                    }
                }
                GccAudioFocusRequestManager.ManagerState.AUDIO_FOCUS_CHANGE_LOSS_TRANSIENT -> {
                    if (messageInputViewModel.isVoicePreviewPlaying.value == true) {
                        messageInputViewModel.pauseMediaPlayer()
                    }
                }
                GccAudioFocusRequestManager.ManagerState.BROADCAST_RECEIVED -> {
                    if (messageInputViewModel.isVoicePreviewPlaying.value == true) {
                        messageInputViewModel.pauseMediaPlayer()
                    }
                }
            }
        }
    }

    private fun initVoiceRecordingView() {
        binding.deleteVoiceRecording.setOnClickListener {
            chatActivity.chatViewModel.stopAndDiscardAudioRecording()
            clear()
        }

        binding.sendVoiceRecording.setOnClickListener {
            chatActivity.chatViewModel.stopAndSendAudioRecording(
                roomToken = chatActivity.roomToken,
                replyToMessageId = chatActivity.getReplyToMessageId(),
                displayName = chatActivity.currentConversation!!.displayName
            )
            clear()
        }

        binding.micInputCloud.setOnClickListener {
            togglePreviewVisibility()
        }

        binding.playPauseBtn.setOnClickListener {
            togglePausePlay()
        }

        binding.audioRecordDuration.base = messageInputViewModel.getRecordingTime.value ?: 0L
        binding.audioRecordDuration.start()

        binding.seekbar.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
            override fun onProgressChanged(seekbar: SeekBar, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    messageInputViewModel.seekMediaPlayerTo(progress)
                }
            }

            override fun onStartTrackingTouch(p0: SeekBar) {
                pause = true
            }

            override fun onStopTrackingTouch(p0: SeekBar) {
                pause = false
            }
        })
    }

    private fun clear() {
        chatActivity.chatViewModel.setVoiceRecordingLocked(false)
        messageInputViewModel.stopMicInput()
        chatActivity.chatViewModel.stopAudioRecording()
        messageInputViewModel.stopMediaPlayer()
        binding.audioRecordDuration.stop()
        binding.audioRecordDuration.clearAnimation()
    }

    private fun togglePreviewVisibility() {
        val visibility = binding.voicePreviewContainer.visibility
        binding.voicePreviewContainer.visibility = if (visibility == View.VISIBLE) {
            messageInputViewModel.stopMediaPlayer()
            binding.playPauseBtn.icon = ContextCompat.getDrawable(
                requireContext(),
                R.drawable.ic_baseline_play_arrow_voice_message_24
            )
            pause = true
            messageInputViewModel.startMicInput(requireContext())
            chatActivity.chatViewModel.startAudioRecording(requireContext(), chatActivity.currentConversation!!)
            binding.audioRecordDuration.visibility = View.VISIBLE
            binding.audioRecordDuration.base = SystemClock.elapsedRealtime()
            binding.audioRecordDuration.start()
            View.GONE
        } else {
            pause = false
            binding.seekbar.progress = 0
            messageInputViewModel.stopMicInput()
            chatActivity.chatViewModel.stopAudioRecording()
            binding.audioRecordDuration.visibility = View.GONE
            binding.audioRecordDuration.stop()
            View.VISIBLE
        }
    }

    private fun togglePausePlay() {
        val path = chatActivity.chatViewModel.getCurrentVoiceRecordFile()
        if (messageInputViewModel.isVoicePreviewPlaying.value == true) {
            binding.playPauseBtn.icon = ContextCompat.getDrawable(
                requireContext(),
                R.drawable.ic_baseline_play_arrow_voice_message_24
            )
            messageInputViewModel.stopMediaPlayer()
        } else {
            binding.playPauseBtn.icon = ContextCompat.getDrawable(
                requireContext(),
                R.drawable.ic_baseline_pause_voice_message_24
            )
            messageInputViewModel.startMediaPlayer(path)
        }
    }

    private fun themeVoiceRecordingView() {
        binding.playPauseBtn.let {
            viewThemeUtils.material.colorMaterialButtonText(it)
        }

        binding.seekbar.let {
            viewThemeUtils.platform.themeHorizontalSeekBar(it)
        }

        binding.deleteVoiceRecording.let {
            viewThemeUtils.platform.colorImageView(it, ColorRole.PRIMARY)
        }
        binding.sendVoiceRecording.let {
            viewThemeUtils.platform.colorImageView(it, ColorRole.PRIMARY)
        }

        binding.voicePreviewContainer.let {
            viewThemeUtils.talk.themeOutgoingMessageBubble(it, true, false)
        }

        binding.micInputCloud.let {
            viewThemeUtils.talk.themeMicInputCloud(it)
        }
    }
}
