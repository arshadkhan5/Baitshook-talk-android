/*
 * Nextcloud Talk - Android Client
 *
 * SPDX-FileCopyrightText: 2025 Julius Linus <juliuslinus1@gmail.com>
 * SPDX-FileCopyrightText: 2023 Marcel Hibbe <dev@mhibbe.de>
 * SPDX-FileCopyrightText: 2022 Tim Krüger <t@timkrueger.me>
 * SPDX-FileCopyrightText: 2017-2018 Mario Danic <mario@lovelyhq.com>
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package com.gcc.talk.gccActivities

import android.Manifest
import android.annotation.SuppressLint
import android.app.PendingIntent
import android.app.RemoteAction
import android.content.BroadcastReceiver
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.IntentFilter
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.Icon
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.text.TextUtils
import android.text.format.DateUtils
import android.util.Log
import android.view.MotionEvent
import android.view.OrientationEventListener
import android.view.View
import android.view.View.OnTouchListener
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.DrawableRes
import androidx.appcompat.app.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.graphics.drawable.DrawableCompat
import androidx.core.graphics.toColorInt
import androidx.core.net.toUri
import androidx.lifecycle.ViewModelProvider
import autodagger.AutoInjector
import com.bluelinelabs.logansquare.LoganSquare
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.gcc.talk.R
import com.gcc.talk.gccApi.GccNcApi
import com.gcc.talk.gccApplication.GccTalkApplication
import com.gcc.talk.gccApplication.GccTalkApplication.Companion.sharedApplication
import com.gcc.talk.gccCall.GccCallParticipantList
import com.gcc.talk.gccCall.GccLocalStateBroadcaster
import com.gcc.talk.gccCall.GccLocalStateBroadcasterMcu
import com.gcc.talk.gccCall.GccLocalStateBroadcasterNoMcu
import com.gcc.talk.gccCall.GccMediaConstraintsHelper
import com.gcc.talk.gccCall.GccMessageSender
import com.gcc.talk.gccCall.GccMessageSenderMcu
import com.gcc.talk.gccCall.GccMessageSenderNoMcu
import com.gcc.talk.gccCall.GccMutableLocalCallParticipantModel
import com.gcc.talk.gccCall.ReactionAnimator
import com.gcc.talk.gccCall.components.ParticipantGrid
import com.gcc.talk.gccCall.components.SelfVideoView
import com.gcc.talk.gccCall.components.screenshare.ScreenShareComponent
import com.gcc.talk.gccChat.GccChatActivity
import com.gcc.talk.gccData.user.model.GccUser
import com.gcc.talk.databinding.CallActivityBinding
import com.gcc.talk.gccEvents.GccConfigurationChangeEvent
import com.gcc.talk.gccEvents.GccNetworkEvent
import com.gcc.talk.gccEvents.GccProximitySensorEvent
import com.gcc.talk.gccEvents.GccWebSocketCommunicationEvent
import com.gcc.talk.gccModels.GccExternalSignalingServer
import com.gcc.talk.gccModels.json.capabilities.CapabilitiesOverall
import com.gcc.talk.gccModels.json.conversations.Conversation
import com.gcc.talk.gccModels.json.conversations.RoomOverall
import com.gcc.talk.gccModels.json.generic.GenericOverall
import com.gcc.talk.gccModels.json.participants.Participant
import com.gcc.talk.gccModels.json.signaling.DataChannelMessage
import com.gcc.talk.gccModels.json.signaling.NCSignalingMessage
import com.gcc.talk.gccModels.json.signaling.Signaling
import com.gcc.talk.gccModels.json.signaling.SignalingOverall
import com.gcc.talk.gccModels.json.signaling.settings.SignalingSettingsOverall
import com.gcc.talk.gccRaisehand.viewmodel.GccRaiseHandViewModel
import com.gcc.talk.gccRaisehand.viewmodel.GccRaiseHandViewModel.LoweredHandState
import com.gcc.talk.gccRaisehand.viewmodel.GccRaiseHandViewModel.RaisedHandState
import com.gcc.talk.gccServices.GccCallForegroundService
import com.gcc.talk.gccSignaling.GccSignalingMessageReceiver
import com.gcc.talk.gccSignaling.GccSignalingMessageReceiver.CallParticipantMessageListener
import com.gcc.talk.gccSignaling.GccSignalingMessageReceiver.LocalParticipantMessageListener
import com.gcc.talk.gccSignaling.GccSignalingMessageReceiver.OfferMessageListener
import com.gcc.talk.gccSignaling.GccSignalingMessageSender
import com.gcc.talk.gccUi.dialog.AudioOutputDialog
import com.gcc.talk.gccUi.dialog.MoreCallActionsDialog
import com.gcc.talk.gccUsers.GccUserManager
import com.gcc.talk.gccUtils.GccApiUtils
import com.gcc.talk.gccUtils.CapabilitiesUtil
import com.gcc.talk.gccUtils.CapabilitiesUtil.hasSpreedFeatureCapability
import com.gcc.talk.gccUtils.CapabilitiesUtil.isCallRecordingAvailable
import com.gcc.talk.gccUtils.GccNotificationUtils.cancelExistingNotificationsForRoom
import com.gcc.talk.gccUtils.GccNotificationUtils.getCallRingtoneUri
import com.gcc.talk.gccUtils.GccReceiverFlag
import com.gcc.talk.gccUtils.SpreedFeatures
import com.gcc.talk.gccUtils.GccVibrationUtils.vibrateShort
import com.gcc.talk.gccUtils.animations.GccPulseAnimation
import com.gcc.talk.gccUtils.bundle.GccBundleKeys.KEY_CALL_VOICE_ONLY
import com.gcc.talk.gccUtils.bundle.GccBundleKeys.KEY_CALL_WITHOUT_NOTIFICATION
import com.gcc.talk.gccUtils.bundle.GccBundleKeys.KEY_CONVERSATION_NAME
import com.gcc.talk.gccUtils.bundle.GccBundleKeys.KEY_CONVERSATION_PASSWORD
import com.gcc.talk.gccUtils.bundle.GccBundleKeys.KEY_FROM_NOTIFICATION_START_CALL
import com.gcc.talk.gccUtils.bundle.GccBundleKeys.KEY_IS_BREAKOUT_ROOM
import com.gcc.talk.gccUtils.bundle.GccBundleKeys.KEY_IS_MODERATOR
import com.gcc.talk.gccUtils.bundle.GccBundleKeys.KEY_MODIFIED_BASE_URL
import com.gcc.talk.gccUtils.bundle.GccBundleKeys.KEY_PARTICIPANT_PERMISSION_CAN_PUBLISH_AUDIO
import com.gcc.talk.gccUtils.bundle.GccBundleKeys.KEY_PARTICIPANT_PERMISSION_CAN_PUBLISH_VIDEO
import com.gcc.talk.gccUtils.bundle.GccBundleKeys.KEY_RECORDING_STATE
import com.gcc.talk.gccUtils.bundle.GccBundleKeys.KEY_ROOM_ID
import com.gcc.talk.gccUtils.bundle.GccBundleKeys.KEY_ROOM_ONE_TO_ONE
import com.gcc.talk.gccUtils.bundle.GccBundleKeys.KEY_ROOM_TOKEN
import com.gcc.talk.gccUtils.bundle.GccBundleKeys.KEY_START_CALL_AFTER_ROOM_SWITCH
import com.gcc.talk.gccUtils.bundle.GccBundleKeys.KEY_SWITCH_TO_ROOM
import com.gcc.talk.gccUtils.permissions.GccPlatformPermissionUtil
import com.gcc.talk.gccUtils.power.GccPowerManagerUtils
import com.gcc.talk.gccUtils.registerPermissionHandlerBroadcastReceiver
import com.gcc.talk.gccUtils.singletons.GccApplicationWideCurrentRoomHolder
import com.gcc.talk.gccViewmodels.GccCallRecordingViewModel
import com.gcc.talk.gccViewmodels.GccCallRecordingViewModel.RecordingConfirmStopState
import com.gcc.talk.gccViewmodels.GccCallRecordingViewModel.RecordingErrorState
import com.gcc.talk.gccViewmodels.GccCallRecordingViewModel.RecordingStartedState
import com.gcc.talk.gccViewmodels.GccCallRecordingViewModel.RecordingStartingState
import com.gcc.talk.gccWebrtc.GccPeerConnectionWrapper
import com.gcc.talk.gccWebrtc.GccPeerConnectionWrapper.PeerConnectionObserver
import com.gcc.talk.gccWebrtc.GccWebRTCUtils
import com.gcc.talk.gccWebrtc.GccWebRtcAudioManager
import com.gcc.talk.gccWebrtc.GccWebRtcAudioManager.AudioDevice
import com.gcc.talk.gccWebrtc.GccWebSocketConnectionHelper
import com.gcc.talk.gccWebrtc.GccWebSocketInstance
import com.wooplr.spotlight.SpotlightView
import io.reactivex.Observable
import io.reactivex.Observer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.Cache
import org.apache.commons.lang3.StringEscapeUtils
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.webrtc.AudioSource
import org.webrtc.AudioTrack
import org.webrtc.Camera1Enumerator
import org.webrtc.Camera2Enumerator
import org.webrtc.CameraEnumerator
import org.webrtc.CameraVideoCapturer
import org.webrtc.CameraVideoCapturer.CameraSwitchHandler
import org.webrtc.DefaultVideoDecoderFactory
import org.webrtc.DefaultVideoEncoderFactory
import org.webrtc.EglBase
import org.webrtc.Logging
import org.webrtc.MediaConstraints
import org.webrtc.MediaStream
import org.webrtc.PeerConnection
import org.webrtc.PeerConnection.IceConnectionState
import org.webrtc.PeerConnectionFactory
import org.webrtc.RendererCommon
import org.webrtc.SoftwareVideoDecoderFactory
import org.webrtc.SoftwareVideoEncoderFactory
import org.webrtc.SurfaceTextureHelper
import org.webrtc.VideoCapturer
import org.webrtc.VideoSource
import org.webrtc.VideoTrack
import java.io.IOException
import java.util.Objects
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger
import javax.inject.Inject
import kotlin.String
import kotlin.math.abs

@AutoInjector(GccTalkApplication::class)
@Suppress("TooManyFunctions", "ReturnCount", "LargeClass")
class GccCallActivity : GccCallBaseActivity() {
    @JvmField
    @Inject
    var ncApi: GccNcApi? = null

    @JvmField
    @Inject
    var userManager: GccUserManager? = null

    @JvmField
    @Inject
    var cache: Cache? = null

    @JvmField
    @Inject
    var permissionUtil: GccPlatformPermissionUtil? = null

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    lateinit var callViewModel: GccCallViewModel

    var audioManager: GccWebRtcAudioManager? = null
    var callRecordingViewModel: GccCallRecordingViewModel? = null
    var raiseHandViewModel: GccRaiseHandViewModel? = null
    private var mReceiver: BroadcastReceiver? = null
    private var peerConnectionFactory: PeerConnectionFactory? = null
    private var screenSharePeerConnectionFactory: PeerConnectionFactory? = null
    private var audioConstraints: MediaConstraints? = null
    private var videoConstraints: MediaConstraints? = null
    private var sdpConstraints: MediaConstraints? = null
    private var sdpConstraintsForMCUPublisher: MediaConstraints? = null
    private var videoSource: VideoSource? = null
    private var localVideoTrack: VideoTrack? = null
    private var audioSource: AudioSource? = null
    private var localAudioTrack: AudioTrack? = null
    private var videoCapturer: VideoCapturer? = null
    private var rootEglBase: EglBase? = null
    private var signalingDisposable: Disposable? = null
    private var iceServers: MutableList<PeerConnection.IceServer>? = null
    private var cameraEnumerator: CameraEnumerator? = null
    private var roomToken: String? = null
    lateinit var conversationUser: GccUser
    private var conversationName: String? = null
    private var callSession: String? = null
    private var localStream: MediaStream? = null
    private var credentials: String? = null
    private val peerConnectionWrapperList: MutableList<GccPeerConnectionWrapper> = ArrayList()
    private var videoOn = false
    private var microphoneOn = false
    var isVoiceOnlyCall = false
    private var isCallWithoutNotification = false
    private var isIncomingCallFromNotification = false
    private val callControlHandler = Handler()
    private val callInfosHandler = Handler()
    private val cameraSwitchHandler = Handler()

    private val callTimeHandler = Handler(Looper.getMainLooper())

    // push to talk
    private var isPushToTalkActive = false
    private var pulseAnimation: GccPulseAnimation? = null
    private var baseUrl: String? = null
    private var roomId: String? = null
    private var spotlightView: SpotlightView? = null
    private val internalSignalingMessageReceiver = InternalSignalingMessageReceiver()
    private var signalingMessageReceiver: GccSignalingMessageReceiver? = null
    private val internalSignalingMessageSender = InternalSignalingMessageSender()
    private var signalingMessageSender: GccSignalingMessageSender? = null
    private var messageSender: GccMessageSender? = null
    private val localCallParticipantModel: GccMutableLocalCallParticipantModel = GccMutableLocalCallParticipantModel()
    private var localStateBroadcaster: GccLocalStateBroadcaster? = null
    private val offerAnswerNickProviders: MutableMap<String?, OfferAnswerNickProvider?> = HashMap()
    private val callParticipantMessageListeners: MutableMap<String?, CallParticipantMessageListener> = HashMap()
    private val selfPeerConnectionObserver: PeerConnectionObserver = CallActivitySelfPeerConnectionObserver()

    private val callParticipantListObserver: GccCallParticipantList.Observer = object : GccCallParticipantList.Observer {

        override fun onCallParticipantsChanged(
            joined: Collection<Participant>,
            updated: Collection<Participant>,
            left: Collection<Participant>,
            unchanged: Collection<Participant>
        ) {
            handleCallParticipantsChanged(joined, updated, left, unchanged)
        }

        override fun onCallEndedForAll() {
            Log.d(TAG, "A moderator ended the call for all.")
            hangup(true, false)
        }
    }
    private var callParticipantList: GccCallParticipantList? = null
    private var switchToRoomToken = ""
    private var isBreakoutRoom = false
    private val localParticipantMessageListener = LocalParticipantMessageListener { token ->
        switchToRoomToken = token
        hangup(true, false)
    }
    private val offerMessageListener = OfferMessageListener { sessionId, roomType, sdp, nick ->
        getOrCreatePeerConnectionWrapperForSessionIdAndType(
            sessionId,
            roomType,
            false
        )
    }
    private var externalSignalingServer: GccExternalSignalingServer? = null
    private var webSocketClient: GccWebSocketInstance? = null
    private var webSocketConnectionHelper: GccWebSocketConnectionHelper? = null
    private var hasMCU = false
    private var hasExternalSignalingServer = false
    private var conversationPassword: String? = null
    private var powerManagerUtils: GccPowerManagerUtils? = null
    private var handler: Handler? = null
    private var currentCallStatus: GccCallStatus? = null
    private var mediaPlayer: MediaPlayer? = null

    private var binding: CallActivityBinding? = null
    private var audioOutputDialog: AudioOutputDialog? = null
    private var moreCallActionsDialog: MoreCallActionsDialog? = null
    private var elapsedSeconds: Long = 0

    private var requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissionMap: Map<String, Boolean> ->
        val rationaleList: MutableList<String> = ArrayList()
        val audioPermission = permissionMap[Manifest.permission.RECORD_AUDIO]
        if (audioPermission != null) {
            if (java.lang.Boolean.TRUE == audioPermission) {
                Log.d(TAG, "Microphone permission was granted")
            } else {
                rationaleList.add(resources.getString(R.string.nc_microphone_permission_hint))
            }
        }
        val cameraPermission = permissionMap[Manifest.permission.CAMERA]
        if (cameraPermission != null) {
            if (java.lang.Boolean.TRUE == cameraPermission) {
                Log.d(TAG, "Camera permission was granted")
            } else {
                rationaleList.add(resources.getString(R.string.nc_camera_permission_hint))
            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val bluetoothPermission = permissionMap[Manifest.permission.BLUETOOTH_CONNECT]
            if (bluetoothPermission != null) {
                if (java.lang.Boolean.TRUE == bluetoothPermission) {
                    enableBluetoothManager()
                } else {
                    // Only ask for bluetooth when already asking to grant microphone or camera access. Asking
                    // for bluetooth solely is not important enough here and would most likely annoy the user.
                    if (rationaleList.isNotEmpty()) {
                        rationaleList.add(resources.getString(R.string.nc_bluetooth_permission_hint))
                    }
                }
            }
        }
        if (rationaleList.isNotEmpty()) {
            showRationaleDialogForSettings(rationaleList)
        }

        if (!isConnectionEstablished) {
            prepareCall()
        }
    }
    private var canPublishAudioStream = false
    private var canPublishVideoStream = false
    private var isModerator = false
    private var reactionAnimator: ReactionAnimator? = null
    private var othersInCall = false
    private var isOneToOneConversation = false

    private lateinit var micInputAudioRecorder: AudioRecord
    private var micInputAudioRecordThread: Thread? = null
    private var isMicInputAudioThreadRunning: Boolean = false
    private val bufferSize = AudioRecord.getMinBufferSize(
        SAMPLE_RATE,
        AudioFormat.CHANNEL_IN_MONO,
        AudioFormat.ENCODING_PCM_16BIT
    )

    private var recordingConsentGiven = false

    private var isFrontCamera by mutableStateOf(true)

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d(TAG, "onCreate")
        super.onCreate(savedInstanceState)
        sharedApplication!!.componentApplication.inject(this)

        callViewModel = ViewModelProvider(this, viewModelFactory)[GccCallViewModel::class.java]

        rootEglBase = EglBase.create()
        binding = CallActivityBinding.inflate(layoutInflater)
        setContentView(binding!!.root)

        binding!!.screenShareFullscreenView.setContent {
            MaterialTheme {
                val screenShareParticipantUiState by callViewModel.activeScreenShareSession.collectAsState()
                if (screenShareParticipantUiState != null) {
                    binding!!.selfVideoViewWrapper.visibility = View.GONE
                    ScreenShareComponent(
                        participantUiState = screenShareParticipantUiState!!,
                        eglBase = rootEglBase!!,
                        onCloseIconClick = {
                            callViewModel.setActiveScreenShareSession(null)
                            initViews()
                        }
                    )
                }
            }
        }

        binding!!.composeParticipantGrid.setContent {
            MaterialTheme {
                val screenShareParticipantUiState by callViewModel.activeScreenShareSession.collectAsState()
                val participantUiStates by callViewModel.participants.collectAsState(initial = emptyList())

                LaunchedEffect(participantUiStates) {
                    participantUiStates.forEach {
                        Log.d(TAG, "Participant: ${it.nick} (${it.sessionKey})")
                    }
                }

                if (screenShareParticipantUiState == null) {
                    ParticipantGrid(
                        participantUiStates = participantUiStates,
                        eglBase = rootEglBase!!,
                        isVoiceOnlyCall = isVoiceOnlyCall,
                        onClick = {},
                        onScreenShareIconClick = {
                            callViewModel.setActiveScreenShareSession(it)
                        }
                    )
                }
            }
        }

        hideNavigationIfNoPipAvailable()
        val extras = intent.extras
        if (extras != null) {
            processExtras(extras)
        } else {
            Log.d(TAG, "extras is null")
            finish()
            return
        }
        processExtras(intent.extras!!)
        conversationUser = currentUserProviderOld.currentUser.blockingGet()

        credentials = GccApiUtils.getCredentials(conversationUser!!.username, conversationUser!!.token)
        if (TextUtils.isEmpty(baseUrl)) {
            baseUrl = conversationUser!!.baseUrl
        }
        powerManagerUtils = GccPowerManagerUtils()

        setCallState(GccCallStatus.CONNECTING)

        initRaiseHandViewModel()
        initCallRecordingViewModel(extras.getInt(KEY_RECORDING_STATE, 0))

        initClickListeners(isModerator, isOneToOneConversation)
        binding!!.microphoneButton.setOnTouchListener(MicrophoneButtonTouchListener())
        pulseAnimation = GccPulseAnimation.create().with(binding!!.microphoneButton)
            .setDuration(PULSE_ANIMATION_DURATION)
            .setRepeatCount(GccPulseAnimation.INFINITE)
            .setRepeatMode(GccPulseAnimation.REVERSE)
        reactionAnimator = ReactionAnimator(context, binding!!.reactionAnimationWrapper, viewThemeUtils)

        checkInitialDevicePermissions()
    }

    private fun initCallRecordingViewModel(recordingState: Int) {
        callRecordingViewModel = ViewModelProvider(this, viewModelFactory).get(
            GccCallRecordingViewModel::class.java
        )
        callRecordingViewModel!!.setData(roomToken!!)
        callRecordingViewModel!!.setRecordingState(recordingState)
        callRecordingViewModel!!.viewState.observe(this) { viewState: GccCallRecordingViewModel.ViewState? ->
            if (viewState is RecordingStartedState) {
                binding!!.callRecordingIndicator.setImageResource(R.drawable.record_stop)
                binding!!.callRecordingIndicator.visibility = View.VISIBLE
                if (viewState.showStartedInfo) {
                    vibrateShort(context)
                    Snackbar.make(
                        binding!!.root,
                        context.resources.getString(R.string.record_active_info),
                        Snackbar.LENGTH_LONG
                    ).show()
                }
            } else if (viewState is RecordingStartingState) {
                if (isAllowedToStartOrStopRecording) {
                    binding!!.callRecordingIndicator.setImageResource(R.drawable.record_starting)
                    binding!!.callRecordingIndicator.visibility = View.VISIBLE
                } else {
                    binding!!.callRecordingIndicator.visibility = View.GONE
                }
            } else if (viewState is RecordingConfirmStopState) {
                if (isAllowedToStartOrStopRecording) {
                    val dialogBuilder = MaterialAlertDialogBuilder(this)
                        .setTitle(R.string.record_stop_confirm_title)
                        .setMessage(R.string.record_stop_confirm_message)
                        .setPositiveButton(R.string.record_stop_description) { _: DialogInterface?, _: Int ->
                            callRecordingViewModel!!.stopRecording()
                        }
                        .setNegativeButton(R.string.nc_common_dismiss) { _: DialogInterface?, _: Int ->
                            callRecordingViewModel!!.dismissStopRecording()
                        }
                    viewThemeUtils.dialog.colorMaterialAlertDialogBackground(this, dialogBuilder)
                    val dialog = dialogBuilder.show()
                    viewThemeUtils.platform.colorTextButtons(
                        dialog.getButton(AlertDialog.BUTTON_POSITIVE),
                        dialog.getButton(AlertDialog.BUTTON_NEGATIVE)
                    )
                } else {
                    Log.e(TAG, "Being in RecordingConfirmStopState as non moderator. This should not happen!")
                }
            } else if (viewState is RecordingErrorState) {
                if (isAllowedToStartOrStopRecording) {
                    Snackbar.make(
                        binding!!.root,
                        context.resources.getString(R.string.record_failed_info),
                        Snackbar.LENGTH_LONG
                    ).show()
                }
                binding!!.callRecordingIndicator.visibility = View.GONE
            } else {
                binding!!.callRecordingIndicator.visibility = View.GONE
            }
        }
    }

    private fun initRaiseHandViewModel() {
        raiseHandViewModel = ViewModelProvider(this, viewModelFactory).get(GccRaiseHandViewModel::class.java)
        raiseHandViewModel!!.setData(roomToken!!, isBreakoutRoom)
        raiseHandViewModel!!.viewState.observe(this) { viewState: GccRaiseHandViewModel.ViewState? ->
            var raised = false
            if (viewState is RaisedHandState) {
                binding!!.lowerHandButton.visibility = View.VISIBLE
                raised = true
            } else if (viewState is LoweredHandState) {
                binding!!.lowerHandButton.visibility = View.GONE
                raised = false
            }
            if (isConnectionEstablished) {
                for (peerConnectionWrapper in peerConnectionWrapperList) {
                    peerConnectionWrapper.raiseHand(raised)
                }
            }
        }
    }

    private fun processExtras(extras: Bundle) {
        roomId = extras.getString(KEY_ROOM_ID, "")
        roomToken = extras.getString(KEY_ROOM_TOKEN, "")
        conversationPassword = extras.getString(KEY_CONVERSATION_PASSWORD, "")
        conversationName = extras.getString(KEY_CONVERSATION_NAME, "")
        isVoiceOnlyCall = extras.getBoolean(KEY_CALL_VOICE_ONLY, false)
        isCallWithoutNotification = extras.getBoolean(KEY_CALL_WITHOUT_NOTIFICATION, false)
        canPublishAudioStream = extras.getBoolean(KEY_PARTICIPANT_PERMISSION_CAN_PUBLISH_AUDIO)
        canPublishVideoStream = extras.getBoolean(KEY_PARTICIPANT_PERMISSION_CAN_PUBLISH_VIDEO)
        isModerator = extras.getBoolean(KEY_IS_MODERATOR, false)
        isOneToOneConversation = extras.getBoolean(KEY_ROOM_ONE_TO_ONE, false)

        if (extras.containsKey(KEY_FROM_NOTIFICATION_START_CALL)) {
            isIncomingCallFromNotification = extras.getBoolean(KEY_FROM_NOTIFICATION_START_CALL)
        }
        if (extras.containsKey(KEY_IS_BREAKOUT_ROOM)) {
            isBreakoutRoom = extras.getBoolean(KEY_IS_BREAKOUT_ROOM)
        }

        baseUrl = extras.getString(KEY_MODIFIED_BASE_URL, "")
    }

    private fun checkRecordingConsentAndInitiateCall() {
        fun askForRecordingConsent() {
            val materialAlertDialogBuilder = MaterialAlertDialogBuilder(this)
                .setTitle(R.string.recording_consent_title)
                .setMessage(R.string.recording_consent_description)
                .setCancelable(false)
                .setPositiveButton(R.string.nc_yes) { _, _ ->
                    recordingConsentGiven = true
                    initiateCall()
                }
                .setNegativeButton(R.string.nc_no) { _, _ ->
                    recordingConsentGiven = false
                    hangup(true, false)
                }

            viewThemeUtils.dialog.colorMaterialAlertDialogBackground(this, materialAlertDialogBuilder)
            val dialog = materialAlertDialogBuilder.show()
            viewThemeUtils.platform.colorTextButtons(
                dialog.getButton(AlertDialog.BUTTON_POSITIVE),
                dialog.getButton(AlertDialog.BUTTON_NEGATIVE)
            )
        }

        when (CapabilitiesUtil.getRecordingConsentType(conversationUser.capabilities!!.spreedCapability!!)) {
            CapabilitiesUtil.RECORDING_CONSENT_NOT_REQUIRED -> initiateCall()
            CapabilitiesUtil.RECORDING_CONSENT_REQUIRED -> askForRecordingConsent()
            CapabilitiesUtil.RECORDING_CONSENT_DEPEND_ON_CONVERSATION -> {
                val getRoomApiVersion = GccApiUtils.getConversationApiVersion(
                    conversationUser,
                    intArrayOf(GccApiUtils.API_V4, 1)
                )
                ncApi!!.getRoom(credentials, GccApiUtils.getUrlForRoom(getRoomApiVersion, baseUrl, roomToken))
                    .retry(API_RETRIES)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(object : Observer<RoomOverall> {
                        override fun onSubscribe(d: Disposable) {
                            // unused atm
                        }

                        override fun onNext(roomOverall: RoomOverall) {
                            val conversation = roomOverall.ocs!!.data
                            if (conversation?.recordingConsentRequired == 1) {
                                askForRecordingConsent()
                            } else {
                                initiateCall()
                            }
                        }

                        override fun onError(e: Throwable) {
                            Log.e(TAG, "Failed to get room", e)
                            Snackbar.make(binding!!.root, R.string.nc_common_error_sorry, Snackbar.LENGTH_LONG).show()
                        }

                        override fun onComplete() {
                            // unused atm
                        }
                    })
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (hasSpreedFeatureCapability(
                conversationUser.capabilities!!.spreedCapability!!,
                SpreedFeatures.RECORDING_V1
            ) &&
            othersInCall &&
            elapsedSeconds.toInt() >= CALL_TIME_ONE_HOUR
        ) {
            showCallRunningSinceOneHourOrMoreInfo()
        }
    }

    fun sendReaction(emoji: String?) {
        addReactionForAnimation(emoji, conversationUser!!.displayName)
        if (isConnectionEstablished) {
            for (peerConnectionWrapper in peerConnectionWrapperList) {
                peerConnectionWrapper.sendReaction(emoji)
            }
        }
    }

    override fun onStart() {
        super.onStart()
        active = true
        initFeaturesVisibility()
        try {
            cache!!.evictAll()
        } catch (e: IOException) {
            Log.e(TAG, "Failed to evict cache")
        }
    }

    override fun onStop() {
        super.onStop()
        active = false

        if (isMicInputAudioThreadRunning) {
            stopMicInputDetection()
        }
    }

    private fun stopMicInputDetection() {
        if (micInputAudioRecordThread != null) {
            micInputAudioRecorder.stop()
            micInputAudioRecorder.release()
            isMicInputAudioThreadRunning = false
            micInputAudioRecordThread = null
        }
    }

    private fun enableBluetoothManager() {
        if (audioManager != null) {
            audioManager!!.startBluetoothManager()
        }
    }

    private fun initFeaturesVisibility() {
        if (isAllowedToStartOrStopRecording || isAllowedToRaiseHand) {
            binding!!.moreCallActions.visibility = View.VISIBLE
        } else {
            binding!!.moreCallActions.visibility = View.GONE
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun initClickListeners(isModerator: Boolean, isOneToOneConversation: Boolean) {
        initCallActionClickListeners(isModerator, isOneToOneConversation)

        if (canPublishAudioStream) {
            binding!!.microphoneButton.setOnClickListener { onMicrophoneClick() }
            binding!!.microphoneButton.setOnLongClickListener {
                if (!microphoneOn) {
                    callControlHandler.removeCallbacksAndMessages(null)
                    callInfosHandler.removeCallbacksAndMessages(null)
                    cameraSwitchHandler.removeCallbacksAndMessages(null)
                    isPushToTalkActive = true
                    binding!!.callControls.visibility = View.VISIBLE
                }
                onMicrophoneClick()
                true
            }
        } else {
            binding!!.microphoneButton.setOnClickListener {
                Snackbar.make(binding!!.root, R.string.nc_not_allowed_to_activate_audio, Snackbar.LENGTH_SHORT).show()
            }
        }

        if (canPublishVideoStream) {
            binding!!.cameraButton.setOnClickListener { onCameraClick() }
        } else {
            binding!!.cameraButton.setOnClickListener {
                Snackbar.make(binding!!.root, R.string.nc_not_allowed_to_activate_video, Snackbar.LENGTH_SHORT).show()
            }
        }

        binding!!.callStates.callStateRelativeLayout.setOnClickListener {
            if (currentCallStatus === GccCallStatus.CALLING_TIMEOUT) {
                setCallState(GccCallStatus.RECONNECTING)
                hangupNetworkCalls(shutDownView = false, endCallForAll = false)
            }
        }
        binding!!.callRecordingIndicator.setOnClickListener {
            if (isAllowedToStartOrStopRecording) {
                if (callRecordingViewModel!!.viewState.value is RecordingStartingState) {
                    if (moreCallActionsDialog == null) {
                        moreCallActionsDialog = MoreCallActionsDialog(this)
                    }
                    moreCallActionsDialog!!.show()
                } else {
                    callRecordingViewModel!!.clickRecordButton()
                }
            } else {
                Snackbar.make(
                    binding!!.root,
                    context.resources.getString(R.string.record_active_info),
                    Snackbar.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun initCallActionClickListeners(isModerator: Boolean, isOneToOneConversation: Boolean) {
        binding!!.audioOutputButton.setOnClickListener {
            audioOutputDialog = AudioOutputDialog(this)
            audioOutputDialog!!.show()
        }

        binding!!.moreCallActions.setOnClickListener {
            moreCallActionsDialog = MoreCallActionsDialog(this)
            moreCallActionsDialog!!.show()
        }

        if (isOneToOneConversation) {
            binding!!.hangupButton.setOnLongClickListener {
                showLeaveCallPopupMenu()
                true
            }
            binding!!.hangupButton.setOnClickListener {
                hangup(shutDownView = true, endCallForAll = true)
            }
            binding!!.endCallPopupMenu.setOnClickListener {
                hangup(shutDownView = true, endCallForAll = true)
                binding!!.endCallPopupMenu.visibility = View.GONE
            }
        } else {
            if (isModerator) {
                binding!!.hangupButton.setOnLongClickListener {
                    showEndCallForAllPopupMenu()
                    true
                }
            }
            binding!!.hangupButton.setOnClickListener {
                hangup(shutDownView = true, endCallForAll = false)
            }
            binding!!.endCallPopupMenu.setOnClickListener {
                hangup(shutDownView = true, endCallForAll = false)
                binding!!.endCallPopupMenu.visibility = View.GONE
            }
        }

        binding!!.lowerHandButton.setOnClickListener { l: View? -> raiseHandViewModel!!.lowerHand() }
        binding!!.pictureInPictureButton.setOnClickListener { enterPipMode() }
    }

    private fun showEndCallForAllPopupMenu() {
        binding!!.endCallPopupMenu.visibility = View.VISIBLE
        binding!!.endCallPopupMenu.text = context.getString(R.string.end_call_for_everyone)
    }

    private fun showLeaveCallPopupMenu() {
        binding!!.endCallPopupMenu.visibility = View.VISIBLE
        binding!!.endCallPopupMenu.text = context.getString(R.string.leave_call)
    }

    @Suppress("Detekt.TooGenericExceptionCaught")
    private fun createCameraEnumerator() {
        var camera2EnumeratorIsSupported = false
        try {
            camera2EnumeratorIsSupported = Camera2Enumerator.isSupported(this)
        } catch (t: Throwable) {
            Log.w(TAG, "Camera2Enumerator threw an error", t)
        }
        cameraEnumerator = if (camera2EnumeratorIsSupported) {
            Camera2Enumerator(this)
        } else {
            Camera1Enumerator(GccWebRTCUtils.shouldEnableVideoHardwareAcceleration())
        }
    }

    private fun basicInitialization() {
        createCameraEnumerator()

        // Create a new PeerConnectionFactory instance.
        val options = PeerConnectionFactory.Options()
        val defaultVideoEncoderFactory = DefaultVideoEncoderFactory(
            rootEglBase!!.eglBaseContext,
            true,
            true
        )
        val defaultVideoDecoderFactory = DefaultVideoDecoderFactory(
            rootEglBase!!.eglBaseContext
        )
        peerConnectionFactory = PeerConnectionFactory.builder()
            .setOptions(options)
            .setVideoEncoderFactory(defaultVideoEncoderFactory)
            .setVideoDecoderFactory(defaultVideoDecoderFactory)
            .createPeerConnectionFactory()

        // Create MediaConstraints - Will be useful for specifying video and audio constraints.
        audioConstraints = MediaConstraints()
        videoConstraints = MediaConstraints()
        localStream = peerConnectionFactory!!.createLocalMediaStream("NCMS")

        // Create and audio manager that will take care of audio routing,
        // audio modes, audio device enumeration etc.
        audioManager = GccWebRtcAudioManager.create(applicationContext, isVoiceOnlyCall)
        // Store existing audio settings and change audio mode to
        // MODE_IN_COMMUNICATION for best possible VoIP performance.
        Log.d(TAG, "Starting the audio manager...")
        audioManager!!.start { currentDevice: AudioDevice, availableDevices: Set<AudioDevice> ->
            onAudioManagerDevicesChanged(
                currentDevice,
                availableDevices
            )
        }
        if (isVoiceOnlyCall) {
            setDefaultAudioOutputChannel(AudioDevice.EARPIECE)
        } else {
            setDefaultAudioOutputChannel(AudioDevice.SPEAKER_PHONE)
        }
        iceServers = ArrayList()

        // create sdpConstraints
        sdpConstraints = MediaConstraints()
        sdpConstraintsForMCUPublisher = MediaConstraints()
        sdpConstraints!!.mandatory.add(MediaConstraints.KeyValuePair("OfferToReceiveAudio", "true"))
        var offerToReceiveVideoString = "true"
        if (isVoiceOnlyCall) {
            offerToReceiveVideoString = "false"
        }
        sdpConstraints!!.mandatory.add(
            MediaConstraints.KeyValuePair("OfferToReceiveVideo", offerToReceiveVideoString)
        )
        sdpConstraintsForMCUPublisher!!.mandatory.add(MediaConstraints.KeyValuePair("OfferToReceiveAudio", "false"))
        sdpConstraintsForMCUPublisher!!.mandatory.add(MediaConstraints.KeyValuePair("OfferToReceiveVideo", "false"))
        sdpConstraintsForMCUPublisher!!.optional.add(MediaConstraints.KeyValuePair("internalSctpDataChannels", "true"))
        sdpConstraintsForMCUPublisher!!.optional.add(MediaConstraints.KeyValuePair("DtlsSrtpKeyAgreement", "true"))
        sdpConstraints!!.optional.add(MediaConstraints.KeyValuePair("internalSctpDataChannels", "true"))
        sdpConstraints!!.optional.add(MediaConstraints.KeyValuePair("DtlsSrtpKeyAgreement", "true"))
        if (!isVoiceOnlyCall) {
            cameraInitialization()
        }
        microphoneInitialization()
    }

    fun setDefaultAudioOutputChannel(selectedAudioDevice: AudioDevice?) {
        if (audioManager != null) {
            audioManager!!.setDefaultAudioDevice(selectedAudioDevice)
            updateAudioOutputButton(audioManager!!.currentAudioDevice)
        }
    }

    fun setAudioOutputChannel(selectedAudioDevice: AudioDevice?) {
        if (audioManager != null) {
            audioManager!!.selectAudioDevice(selectedAudioDevice)
            updateAudioOutputButton(audioManager!!.currentAudioDevice)
        }
    }

    private fun updateAudioOutputButton(activeAudioDevice: AudioDevice) {
        when (activeAudioDevice) {
            AudioDevice.BLUETOOTH -> binding!!.audioOutputButton.setImageResource(
                R.drawable.ic_baseline_bluetooth_audio_24
            )

            AudioDevice.SPEAKER_PHONE -> binding!!.audioOutputButton.setImageResource(
                R.drawable.ic_volume_up_white_24dp
            )

            AudioDevice.EARPIECE -> binding!!.audioOutputButton.setImageResource(
                R.drawable.ic_baseline_phone_in_talk_24
            )

            AudioDevice.WIRED_HEADSET -> binding!!.audioOutputButton.setImageResource(
                R.drawable.ic_baseline_headset_mic_24
            )

            else -> Log.e(TAG, "Icon for audio output not available")
        }
        DrawableCompat.setTint(binding!!.audioOutputButton.drawable, Color.WHITE)
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun initViews() {
        Log.d(TAG, "initViews")
        if (!isPipModePossible) {
            binding!!.pictureInPictureButton.visibility = View.GONE
        }

        if (isVoiceOnlyCall) {
            binding!!.cameraButton.visibility = View.GONE
            binding!!.selfVideoViewWrapper.visibility = View.GONE
        } else {
            initSelfVideoViewForNormalMode()
        }
        binding!!.composeParticipantGrid.setOnTouchListener { _, me ->
            val action = me.actionMasked
            if (action == MotionEvent.ACTION_DOWN) {
                binding!!.endCallPopupMenu.visibility = View.GONE
            }
            false
        }
        binding!!.conversationRelativeLayout.setOnTouchListener { _, me ->
            val action = me.actionMasked
            if (action == MotionEvent.ACTION_DOWN) {
                binding!!.endCallPopupMenu.visibility = View.GONE
            }
            false
        }
        initPipMode()
        binding!!.composeParticipantGrid.z = 0f
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun initSelfVideoViewForNormalMode() {
        binding!!.selfVideoViewWrapper.visibility = View.VISIBLE

        binding!!.selfVideoComposeView.setContent {
            SelfVideoView(
                eglBase = rootEglBase!!.eglBaseContext,
                videoTrack = localVideoTrack,
                isFrontCamera = isFrontCamera,
                onSwitchCamera = { switchCamera() }
            )
        }

        binding!!.pipSelfVideoRenderer.clearImage()
        binding!!.pipSelfVideoRenderer.release()
    }

    private fun initPipMode() {
        if (isInPipMode) {
            updateUiForPipMode()
        }
    }

    private fun checkInitialDevicePermissions() {
        val permissionsToRequest: MutableList<String> = ArrayList()
        val rationaleList: MutableList<String> = ArrayList()
        if (permissionUtil!!.isMicrophonePermissionGranted()) {
            Log.d(TAG, "Microphone permission already granted")
        } else if (shouldShowRequestPermissionRationale(Manifest.permission.RECORD_AUDIO)) {
            permissionsToRequest.add(Manifest.permission.RECORD_AUDIO)
            rationaleList.add(resources.getString(R.string.nc_microphone_permission_hint))
        } else {
            permissionsToRequest.add(Manifest.permission.RECORD_AUDIO)
        }

        if (!isVoiceOnlyCall) {
            if (permissionUtil!!.isCameraPermissionGranted()) {
                Log.d(TAG, "Camera permission already granted")
            } else if (shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)) {
                permissionsToRequest.add(Manifest.permission.CAMERA)
                rationaleList.add(resources.getString(R.string.nc_camera_permission_hint))
            } else {
                permissionsToRequest.add(Manifest.permission.CAMERA)
            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (permissionUtil!!.isBluetoothPermissionGranted()) {
                enableBluetoothManager()
            } else if (shouldShowRequestPermissionRationale(Manifest.permission.BLUETOOTH_CONNECT)) {
                permissionsToRequest.add(Manifest.permission.BLUETOOTH_CONNECT)
                rationaleList.add(resources.getString(R.string.nc_bluetooth_permission_hint))
            } else {
                permissionsToRequest.add(Manifest.permission.BLUETOOTH_CONNECT)
            }
        }

        if (permissionsToRequest.isNotEmpty()) {
            if (rationaleList.isNotEmpty()) {
                showRationaleDialog(permissionsToRequest, rationaleList)
            } else {
                requestPermissionLauncher.launch(permissionsToRequest.toTypedArray())
            }
        } else if (!isConnectionEstablished) {
            prepareCall()
        }
    }

    private fun prepareCall() {
        basicInitialization()
        initViews()
        // updateSelfVideoViewPosition(true)
        checkRecordingConsentAndInitiateCall()

        if (permissionUtil!!.isMicrophonePermissionGranted()) {
            GccCallForegroundService.start(applicationContext, conversationName, intent.extras)
            if (!microphoneOn) {
                onMicrophoneClick()
            }
        }

        if (isVoiceOnlyCall) {
            binding!!.selfVideoViewWrapper.visibility = View.GONE
        } else if (permissionUtil!!.isCameraPermissionGranted()) {
            binding!!.selfVideoViewWrapper.visibility = View.VISIBLE
            onCameraClick()
            if (cameraEnumerator!!.deviceNames.isEmpty()) {
                binding!!.cameraButton.visibility = View.GONE
            }
        }
    }

    private fun showRationaleDialog(permissionToRequest: String, rationale: String) {
        val rationaleList: MutableList<String> = ArrayList()
        val permissionsToRequest: MutableList<String> = ArrayList()
        rationaleList.add(rationale)
        permissionsToRequest.add(permissionToRequest)
        showRationaleDialog(permissionsToRequest, rationaleList)
    }

    private fun showRationaleDialog(permissionsToRequest: List<String>, rationaleList: List<String>) {
        val rationalesWithLineBreaks = StringBuilder()
        for (rationale in rationaleList) {
            rationalesWithLineBreaks.append(rationale).append("\n\n")
        }
        val dialogBuilder = MaterialAlertDialogBuilder(this)
            .setTitle(R.string.nc_permissions_rationale_dialog_title)
            .setMessage(rationalesWithLineBreaks)
            .setPositiveButton(R.string.nc_permissions_ask) { _, _ ->
                requestPermissionLauncher.launch(permissionsToRequest.toTypedArray())
            }
            .setNegativeButton(R.string.nc_common_dismiss, null)
        viewThemeUtils.dialog.colorMaterialAlertDialogBackground(this, dialogBuilder)
        dialogBuilder.show()
    }

    private fun showRationaleDialogForSettings(rationaleList: List<String>) {
        val rationalesWithLineBreaks = StringBuilder()
        rationalesWithLineBreaks.append(resources.getString(R.string.nc_permissions_denied))
        rationalesWithLineBreaks.append('\n')
        rationalesWithLineBreaks.append(resources.getString(R.string.nc_permissions_settings_hint))
        rationalesWithLineBreaks.append("\n\n")
        for (rationale in rationaleList) {
            rationalesWithLineBreaks.append(rationale).append("\n\n")
        }
        val dialogBuilder = MaterialAlertDialogBuilder(this)
            .setTitle(R.string.nc_permissions_rationale_dialog_title)
            .setMessage(rationalesWithLineBreaks)
            .setPositiveButton(R.string.nc_permissions_settings) { _, _ ->
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                intent.data = Uri.fromParts("package", packageName, null)
                startActivity(intent)
            }
            .setNegativeButton(R.string.nc_common_dismiss, null)
        viewThemeUtils.dialog.colorMaterialAlertDialogBackground(this, dialogBuilder)
        dialogBuilder.show()
    }

    private val isConnectionEstablished: Boolean
        get() = currentCallStatus === GccCallStatus.JOINED || currentCallStatus === GccCallStatus.IN_CONVERSATION

    private fun onAudioManagerDevicesChanged(currentDevice: AudioDevice, availableDevices: Set<AudioDevice>) {
        Log.d(TAG, "onAudioManagerDevicesChanged: $availableDevices, currentDevice: $currentDevice")
        val shouldDisableProximityLock =
            currentDevice == AudioDevice.WIRED_HEADSET ||
                currentDevice == AudioDevice.SPEAKER_PHONE ||
                currentDevice == AudioDevice.BLUETOOTH
        if (shouldDisableProximityLock) {
            powerManagerUtils!!.updatePhoneState(GccPowerManagerUtils.PhoneState.WITHOUT_PROXIMITY_SENSOR_LOCK)
        } else {
            powerManagerUtils!!.updatePhoneState(GccPowerManagerUtils.PhoneState.WITH_PROXIMITY_SENSOR_LOCK)
        }
        if (audioOutputDialog != null) {
            audioOutputDialog!!.updateOutputDeviceList()
        }
        updateAudioOutputButton(currentDevice)
    }

    private fun cameraInitialization() {
        videoCapturer = createCameraCapturer(cameraEnumerator)

        // Create a VideoSource instance
        if (videoCapturer != null) {
            val surfaceTextureHelper = SurfaceTextureHelper.create(
                "CaptureThread",
                rootEglBase!!.eglBaseContext
            )
            videoSource = peerConnectionFactory!!.createVideoSource(false)

            videoCapturer!!.initialize(surfaceTextureHelper, applicationContext, videoSource!!.capturerObserver)
        }
        localVideoTrack = peerConnectionFactory!!.createVideoTrack("NCv0", videoSource)
        localStream!!.addTrack(localVideoTrack)
        localVideoTrack!!.setEnabled(false)
        localCallParticipantModel.isVideoEnabled = false
    }

    private fun microphoneInitialization() {
        startMicInputDetection()

        // create an AudioSource instance
        audioSource = peerConnectionFactory!!.createAudioSource(audioConstraints)
        localAudioTrack = peerConnectionFactory!!.createAudioTrack("NCa0", audioSource)
        localAudioTrack!!.setEnabled(false)
        localStream!!.addTrack(localAudioTrack)
        localCallParticipantModel.isAudioEnabled = false
    }

    @SuppressLint("MissingPermission")
    private fun startMicInputDetection() {
        if (permissionUtil!!.isMicrophonePermissionGranted() && micInputAudioRecordThread == null) {
            micInputAudioRecorder = AudioRecord(
                MediaRecorder.AudioSource.MIC,
                SAMPLE_RATE,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT,
                bufferSize
            )
            isMicInputAudioThreadRunning = true
            micInputAudioRecorder.startRecording()
            micInputAudioRecordThread = Thread(
                Runnable {
                    while (isMicInputAudioThreadRunning) {
                        val byteArr = ByteArray(bufferSize / 2)
                        micInputAudioRecorder.read(byteArr, 0, byteArr.size)
                        val isCurrentlySpeaking = abs(byteArr[0].toDouble()) > MICROPHONE_VALUE_THRESHOLD

                        localCallParticipantModel.isSpeaking = isCurrentlySpeaking

                        Thread.sleep(MICROPHONE_VALUE_SLEEP)
                    }
                }
            )
            micInputAudioRecordThread!!.start()
        }
    }

    private fun createCameraCapturer(enumerator: CameraEnumerator?): VideoCapturer? {
        val deviceNames = enumerator!!.deviceNames

        // First, try to find front facing camera
        Logging.d(TAG, "Looking for front facing cameras.")
        for (deviceName in deviceNames) {
            if (enumerator.isFrontFacing(deviceName)) {
                Logging.d(TAG, "Creating front facing camera capturer.")
                val videoCapturer: VideoCapturer? = enumerator.createCapturer(deviceName, null)
                if (videoCapturer != null) {
                    isFrontCamera = true
                    return videoCapturer
                }
            }
        }

        // Front facing camera not found, try something else
        Logging.d(TAG, "Looking for other cameras.")
        for (deviceName in deviceNames) {
            if (!enumerator.isFrontFacing(deviceName)) {
                Logging.d(TAG, "Creating other camera capturer.")
                val videoCapturer: VideoCapturer? = enumerator.createCapturer(deviceName, null)
                if (videoCapturer != null) {
                    isFrontCamera = false
                    return videoCapturer
                }
            }
        }
        return null
    }

    fun onMicrophoneClick() {
        if (!canPublishAudioStream) {
            microphoneOn = false
            binding!!.microphoneButton.setImageResource(R.drawable.ic_mic_off_white_24px)
            toggleMedia(false, false)
        }
        if (!canPublishAudioStream) {
            // In the case no audio stream will be published it's not needed to check microphone permissions
            return
        }
        if (permissionUtil!!.isMicrophonePermissionGranted()) {
            if (!appPreferences.pushToTalkIntroShown) {
                spotlightView = getSpotlightView()
                appPreferences.pushToTalkIntroShown = true
            }
            if (!isPushToTalkActive) {
                microphoneOn = !microphoneOn
                if (microphoneOn) {
                    binding!!.microphoneButton.setImageResource(R.drawable.ic_mic_white_24px)
                    updatePictureInPictureActions(
                        R.drawable.ic_mic_white_24px,
                        resources.getString(R.string.nc_pip_microphone_mute),
                        MICROPHONE_PIP_REQUEST_MUTE
                    )
                } else {
                    binding!!.microphoneButton.setImageResource(R.drawable.ic_mic_off_white_24px)
                    updatePictureInPictureActions(
                        R.drawable.ic_mic_off_white_24px,
                        resources.getString(R.string.nc_pip_microphone_unmute),
                        MICROPHONE_PIP_REQUEST_UNMUTE
                    )
                }
                toggleMedia(microphoneOn, false)
            } else {
                binding!!.microphoneButton.setImageResource(R.drawable.ic_mic_white_24px)
                pulseAnimation!!.start()
                toggleMedia(true, false)
            }
        } else if (shouldShowRequestPermissionRationale(Manifest.permission.RECORD_AUDIO)) {
            showRationaleDialog(
                Manifest.permission.RECORD_AUDIO,
                resources.getString(R.string.nc_microphone_permission_hint)
            )
        } else {
            requestPermissionLauncher.launch(PERMISSIONS_MICROPHONE)
        }
    }

    private fun getSpotlightView(): SpotlightView? {
        val builder = SpotlightView.Builder(this)
            .introAnimationDuration(INTRO_ANIMATION_DURATION)
            .enableRevealAnimation(true)
            .performClick(false)
            .fadeinTextDuration(FADE_IN_ANIMATION_DURATION)
            .headingTvSize(SPOTLIGHT_HEADING_SIZE)
            .headingTvText(resources.getString(R.string.nc_push_to_talk))
            .subHeadingTvColor(resources.getColor(R.color.bg_default, null))
            .subHeadingTvSize(SPOTLIGHT_SUBHEADING_SIZE)
            .subHeadingTvText(resources.getString(R.string.nc_push_to_talk_desc))
            .maskColor("#dc000000".toColorInt())
            .target(binding!!.microphoneButton)
            .lineAnimDuration(FADE_IN_ANIMATION_DURATION)
            .enableDismissAfterShown(true)
            .dismissOnBackPress(true)
            .usageId("pushToTalk")

        return viewThemeUtils.talk.themeSpotlightView(context, builder).show()
    }

    private fun onCameraClick() {
        if (!canPublishVideoStream) {
            videoOn = false
            binding!!.cameraButton.setImageResource(R.drawable.ic_videocam_off_white_24px)
            return
        }
        if (permissionUtil!!.isCameraPermissionGranted()) {
            videoOn = !videoOn
            if (videoOn) {
                binding!!.cameraButton.setImageResource(R.drawable.ic_videocam_white_24px)
            } else {
                binding!!.cameraButton.setImageResource(R.drawable.ic_videocam_off_white_24px)
            }
            toggleMedia(videoOn, true)
        } else if (shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)) {
            showRationaleDialog(
                Manifest.permission.CAMERA,
                resources.getString(R.string.nc_camera_permission_hint)
            )
        } else {
            requestPermissionLauncher.launch(PERMISSIONS_CAMERA)
        }
    }

    fun switchCamera() {
        val cameraVideoCapturer = videoCapturer as CameraVideoCapturer?
        cameraVideoCapturer?.switchCamera(object : CameraSwitchHandler {
            override fun onCameraSwitchDone(currentCameraIsFront: Boolean) {
                isFrontCamera = currentCameraIsFront
            }

            override fun onCameraSwitchError(s: String) {
                Log.e(TAG, "Error while switching camera: $s")
            }
        })
    }

    private fun toggleMedia(enable: Boolean, video: Boolean) {
        if (video) {
            if (enable) {
                binding!!.cameraButton.alpha = OPACITY_ENABLED
                startVideoCapture(
                    isPortrait = true
                )
                setupOrientationListener(context)
            } else {
                binding!!.cameraButton.alpha = OPACITY_DISABLED
                if (videoCapturer != null) {
                    try {
                        videoCapturer!!.stopCapture()
                    } catch (e: InterruptedException) {
                        Log.d(TAG, "Failed to stop capturing video while sensor is near the ear")
                    }
                }
            }
            if (localStream != null && localStream!!.videoTracks.size > 0) {
                localStream!!.videoTracks[0].setEnabled(enable)
                localCallParticipantModel.isVideoEnabled = enable
            }
            if (enable) {
                binding!!.selfVideoViewWrapper.visibility = View.VISIBLE
                binding!!.pipSelfVideoRenderer.visibility = View.VISIBLE

                initSelfVideoViewForNormalMode()
            } else {
                binding!!.selfVideoViewWrapper.visibility = View.INVISIBLE
                binding!!.pipSelfVideoRenderer.visibility = View.INVISIBLE

                binding!!.pipSelfVideoRenderer.clearImage()
                binding!!.pipSelfVideoRenderer.release()
            }
        } else {
            if (enable) {
                binding!!.microphoneButton.alpha = OPACITY_ENABLED
            } else {
                binding!!.microphoneButton.alpha = OPACITY_DISABLED
            }
            if (localStream != null && localStream!!.audioTracks.size > 0) {
                localStream!!.audioTracks[0].setEnabled(enable)
                localCallParticipantModel.isAudioEnabled = enable
            }
        }
    }

    fun clickRaiseOrLowerHandButton() {
        raiseHandViewModel!!.clickHandButton()
    }

    public override fun onDestroy() {
        if (signalingMessageReceiver != null) {
            signalingMessageReceiver!!.removeListener(localParticipantMessageListener)
            signalingMessageReceiver!!.removeListener(offerMessageListener)
        }
        if (localStream != null) {
            localStream!!.dispose()
            localStream = null
            Log.d(TAG, "Disposed localStream")
        } else {
            Log.d(TAG, "localStream is null")
        }
        if (currentCallStatus !== GccCallStatus.LEAVING) {
            hangup(true, false)
        }
        GccCallForegroundService.stop(applicationContext)
        powerManagerUtils!!.updatePhoneState(GccPowerManagerUtils.PhoneState.IDLE)
        super.onDestroy()
    }

    private fun fetchSignalingSettings() {
        Log.d(TAG, "fetchSignalingSettings")
        val apiVersion = GccApiUtils.getSignalingApiVersion(conversationUser, intArrayOf(GccApiUtils.API_V3, 2, 1))
        ncApi!!.getSignalingSettings(credentials, GccApiUtils.getUrlForSignalingSettings(apiVersion, baseUrl, roomToken!!))
            .subscribeOn(Schedulers.io())
            .retry(API_RETRIES)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : Observer<SignalingSettingsOverall> {
                override fun onSubscribe(d: Disposable) {
                    // unused atm
                }

                override fun onNext(signalingSettingsOverall: SignalingSettingsOverall) {
                    if (signalingSettingsOverall.ocs != null &&
                        signalingSettingsOverall.ocs!!.settings != null
                    ) {
                        externalSignalingServer = GccExternalSignalingServer()
                        if (!TextUtils.isEmpty(signalingSettingsOverall.ocs!!.settings!!.externalSignalingServer) &&
                            !TextUtils.isEmpty(signalingSettingsOverall.ocs!!.settings!!.externalSignalingTicket)
                        ) {
                            externalSignalingServer = GccExternalSignalingServer()
                            externalSignalingServer!!.externalSignalingServer =
                                signalingSettingsOverall.ocs!!.settings!!.externalSignalingServer
                            externalSignalingServer!!.externalSignalingTicket =
                                signalingSettingsOverall.ocs!!.settings!!.externalSignalingTicket
                            externalSignalingServer!!.federation =
                                signalingSettingsOverall.ocs!!.settings!!.federation
                            hasExternalSignalingServer = true
                        } else {
                            hasExternalSignalingServer = false
                        }
                        Log.d(TAG, "   hasExternalSignalingServer: $hasExternalSignalingServer")

                        if ("?" != conversationUser!!.userId && conversationUser!!.id != null) {
                            Log.d(
                                TAG,
                                "Update externalSignalingServer for: " + conversationUser!!.id +
                                    " / " + conversationUser!!.userId
                            )
                            userManager!!.updateExternalSignalingServer(
                                conversationUser!!.id!!,
                                externalSignalingServer!!
                            )
                                .subscribeOn(Schedulers.io())
                                .subscribe()
                        } else {
                            conversationUser!!.externalSignalingServer = externalSignalingServer
                        }

                        addIceServers(signalingSettingsOverall, apiVersion)
                    }
                    checkCapabilities()
                }

                override fun onError(e: Throwable) {
                    Log.e(TAG, e.message, e)
                }

                override fun onComplete() {
                    // unused atm
                }
            })
    }

    private fun addIceServers(signalingSettingsOverall: SignalingSettingsOverall, apiVersion: Int) {
        if (signalingSettingsOverall.ocs!!.settings!!.stunServers != null) {
            val stunServers = signalingSettingsOverall.ocs!!.settings!!.stunServers
            if (apiVersion == GccApiUtils.API_V3) {
                for ((_, urls) in stunServers!!) {
                    if (urls != null) {
                        for (url in urls) {
                            Log.d(TAG, "   STUN server url: $url")
                            iceServers!!.add(PeerConnection.IceServer(url))
                        }
                    }
                }
            } else {
                if (signalingSettingsOverall.ocs!!.settings!!.stunServers != null) {
                    for ((url) in stunServers!!) {
                        Log.d(TAG, "   STUN server url: $url")
                        iceServers!!.add(PeerConnection.IceServer(url))
                    }
                }
            }
        }

        if (signalingSettingsOverall.ocs!!.settings!!.turnServers != null) {
            val turnServers = signalingSettingsOverall.ocs!!.settings!!.turnServers
            for ((_, urls, username, credential) in turnServers!!) {
                if (urls != null) {
                    for (url in urls) {
                        Log.d(TAG, "   TURN server url: $url")
                        iceServers!!.add(PeerConnection.IceServer(url, username, credential))
                    }
                }
            }
        }
    }

    private fun checkCapabilities() {
        ncApi!!.getCapabilities(credentials, GccApiUtils.getUrlForCapabilities(baseUrl!!))
            .retry(API_RETRIES)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : Observer<CapabilitiesOverall> {
                override fun onSubscribe(d: Disposable) {
                    // unused atm
                }

                override fun onNext(capabilitiesOverall: CapabilitiesOverall) {
                    // FIXME check for compatible Call API version
                    if (hasExternalSignalingServer) {
                        setupAndInitiateWebSocketsConnection()
                    } else {
                        signalingMessageReceiver = internalSignalingMessageReceiver
                        signalingMessageReceiver!!.addListener(localParticipantMessageListener)
                        signalingMessageReceiver!!.addListener(offerMessageListener)
                        signalingMessageSender = internalSignalingMessageSender

                        hasMCU = false

                        messageSender = GccMessageSenderNoMcu(
                            signalingMessageSender,
                            getParticipantSessionKeys(),
                            peerConnectionWrapperList
                        )

                        joinRoomAndCall()
                    }
                }

                override fun onError(e: Throwable) {
                    Log.e(TAG, "Failed to fetch capabilities", e)
                    Snackbar.make(binding!!.root, R.string.nc_common_error_sorry, Snackbar.LENGTH_LONG).show()
                    // unused atm
                }

                override fun onComplete() {
                    // unused atm
                }
            })
    }

    private fun getParticipantSessionKeys(): Set<String> =
        callViewModel.participants.value
            .mapNotNull { it.sessionKey }
            .toSet()

    private fun joinRoomAndCall() {


        callSession = GccApplicationWideCurrentRoomHolder.getInstance().session
        val apiVersion = GccApiUtils.getConversationApiVersion(conversationUser, intArrayOf(GccApiUtils.API_V4, 1))
        Log.d(TAG, "joinRoomAndCall")
        Log.d(TAG, "   baseUrl= $baseUrl")
        Log.d(TAG, "   roomToken= $roomToken")
        Log.d(TAG, "   callSession= $callSession")
        val url = GccApiUtils.getUrlForParticipantsActive(apiVersion, baseUrl, roomToken)
        Log.d(TAG, "   url= $url")

        // if session is empty, e.g. we when we got here by notification, we need to join the room to get a session
        if (TextUtils.isEmpty(callSession)) {
            ncApi!!.joinRoom(credentials, url, conversationPassword)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .retry(API_RETRIES)
                .subscribe(object : Observer<RoomOverall> {
                    override fun onSubscribe(d: Disposable) {
                        // unused atm
                    }

                    override fun onNext(roomOverall: RoomOverall) {
                        val conversation = roomOverall.ocs!!.data
                        callRecordingViewModel!!.setRecordingState(conversation!!.callRecording)
                        callSession = conversation.sessionId
                        Log.d(TAG, " new callSession by joinRoom= $callSession")

                        setInitialApplicationWideCurrentRoomHolderValues(conversation)

                        callOrJoinRoomViaWebSocket()
                    }

                    override fun onError(e: Throwable) {
                        Log.e(TAG, "joinRoom onError", e)
                    }

                    override fun onComplete() {
                        Log.d(TAG, "joinRoom onComplete")
                    }
                })
        } else {
            // we are in a room and start a call -> same session needs to be used
            callOrJoinRoomViaWebSocket()
        }
    }

    private fun callOrJoinRoomViaWebSocket() {
        if (hasExternalSignalingServer) {
            webSocketClient!!.joinRoomWithRoomTokenAndSession(
                roomToken!!,
                callSession,
                externalSignalingServer!!.federation
            )
        } else {
            performCall()
        }
    }

    private fun performCall() {
        fun getRoomAndContinue() {
            val getRoomApiVersion = GccApiUtils.getConversationApiVersion(
                conversationUser,
                intArrayOf(GccApiUtils.API_V4, 1)
            )
            ncApi!!.getRoom(credentials, GccApiUtils.getUrlForRoom(getRoomApiVersion, baseUrl, roomToken))
                .retry(API_RETRIES)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : Observer<RoomOverall> {
                    override fun onSubscribe(d: Disposable) {
                        // unused atm
                    }

                    override fun onNext(roomOverall: RoomOverall) {
                        val conversation = roomOverall.ocs!!.data
                        callRecordingViewModel!!.setRecordingState(conversation!!.callRecording)
                        callSession = conversation.sessionId

                        setInitialApplicationWideCurrentRoomHolderValues(conversation)

                        startCallTimeCounter(conversation.callStartTime)

                        if (currentCallStatus !== GccCallStatus.LEAVING) {
                            if (currentCallStatus !== GccCallStatus.IN_CONVERSATION) {
                                setCallState(GccCallStatus.JOINED)
                            }
                            GccApplicationWideCurrentRoomHolder.getInstance().isInCall = true
                            GccApplicationWideCurrentRoomHolder.getInstance().isDialing = false
                            if (!TextUtils.isEmpty(roomToken)) {
                                cancelExistingNotificationsForRoom(
                                    applicationContext,
                                    conversationUser!!,
                                    roomToken!!
                                )
                            }
                            if (!hasExternalSignalingServer) {
                                pullSignalingMessages()
                            }
                        }
                    }

                    override fun onError(e: Throwable) {
                        Log.e(TAG, "Failed to get room", e)
                    }

                    override fun onComplete() {
                        // unused atm
                    }
                })
        }

        var inCallFlag = Participant.InCallFlags.IN_CALL
        if (canPublishAudioStream) {
            inCallFlag += Participant.InCallFlags.WITH_AUDIO
        }
        if (!isVoiceOnlyCall && canPublishVideoStream) {
            inCallFlag += Participant.InCallFlags.WITH_VIDEO
        }
        callParticipantList = GccCallParticipantList(signalingMessageReceiver)
        callParticipantList!!.addObserver(callParticipantListObserver)

        if (hasMCU) {
            localStateBroadcaster = GccLocalStateBroadcasterMcu(localCallParticipantModel, messageSender)
        } else {
            localStateBroadcaster = GccLocalStateBroadcasterNoMcu(
                localCallParticipantModel,
                messageSender as GccMessageSenderNoMcu
            )
        }

        val apiVersion = GccApiUtils.getCallApiVersion(conversationUser, intArrayOf(GccApiUtils.API_V4, 1))
        ncApi!!.joinCall(
            credentials,
            GccApiUtils.getUrlForCall(apiVersion, baseUrl, roomToken!!),
            inCallFlag,
            isCallWithoutNotification,
            recordingConsentGiven
        )
            .subscribeOn(Schedulers.io())
            .retry(API_RETRIES)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : Observer<GenericOverall> {
                override fun onSubscribe(d: Disposable) {
                    // unused atm
                }

                override fun onNext(genericOverall: GenericOverall) {
                    getRoomAndContinue()
                }

                override fun onError(e: Throwable) {
                    Log.e(TAG, "Failed to join call", e)
                    Snackbar.make(binding!!.root, R.string.nc_common_error_sorry, Snackbar.LENGTH_LONG).show()
                    hangup(true, false)
                }

                override fun onComplete() {
                    // unused atm
                }
            })
    }

    private fun setInitialApplicationWideCurrentRoomHolderValues(conversation: Conversation) {
        GccApplicationWideCurrentRoomHolder.getInstance().userInRoom = conversationUser
        GccApplicationWideCurrentRoomHolder.getInstance().session = conversation.sessionId
        // GccApplicationWideCurrentRoomHolder.getInstance().currentRoomId = conversation.roomId
        GccApplicationWideCurrentRoomHolder.getInstance().currentRoomToken = conversation.token
        GccApplicationWideCurrentRoomHolder.getInstance().callStartTime = conversation.callStartTime
    }

    private fun startCallTimeCounter(callStartTime: Long) {
        if (callStartTime != 0L &&
            hasSpreedFeatureCapability(
                conversationUser!!.capabilities!!.spreedCapability!!,
                SpreedFeatures.RECORDING_V1
            )
        ) {
            binding!!.callDuration.visibility = View.VISIBLE
            val currentTimeInSec = System.currentTimeMillis() / SECOND_IN_MILLIS
            elapsedSeconds = currentTimeInSec - callStartTime

            val callTimeTask: Runnable = object : Runnable {
                override fun run() {
                    if (othersInCall) {
                        binding!!.callDuration.text = DateUtils.formatElapsedTime(elapsedSeconds)
                        if (elapsedSeconds.toInt() == CALL_TIME_ONE_HOUR) {
                            showCallRunningSinceOneHourOrMoreInfo()
                        }
                    } else {
                        binding!!.callDuration.text = CALL_DURATION_EMPTY
                    }

                    elapsedSeconds += 1
                    callTimeHandler.postDelayed(this, CALL_TIME_COUNTER_DELAY)
                }
            }
            callTimeHandler.post(callTimeTask)
        } else {
            binding!!.callDuration.visibility = View.GONE
        }
    }

    private fun showCallRunningSinceOneHourOrMoreInfo() {
        binding!!.callDuration.setTypeface(null, Typeface.BOLD)
        vibrateShort(context)
        Snackbar.make(
            binding!!.root,
            context.resources.getString(R.string.call_running_since_one_hour),
            Snackbar.LENGTH_LONG
        ).show()
    }

    private fun pullSignalingMessages() {
        val signalingApiVersion = GccApiUtils.getSignalingApiVersion(conversationUser, intArrayOf(GccApiUtils.API_V3, 2, 1))
        val delayOnError = AtomicInteger(0)

        ncApi!!.pullSignalingMessages(
            credentials,
            GccApiUtils.getUrlForSignaling(
                signalingApiVersion,
                baseUrl,
                roomToken!!
            )
        )
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .repeatWhen { observable: Observable<Any?>? -> observable }
            .takeWhile { isConnectionEstablished }
            .doOnNext { delayOnError.set(0) }
            .retryWhen { errors: Observable<Throwable?> ->
                errors.flatMap { error: Throwable? ->
                    if (!isConnectionEstablished) {
                        return@flatMap Observable.error<Long>(error)
                    }
                    if (delayOnError.get() == 0) {
                        delayOnError.set(1)
                    } else if (delayOnError.get() < DELAY_ON_ERROR_STOP_THRESHOLD) {
                        delayOnError.set(delayOnError.get() * 2)
                    }
                    Observable.timer(delayOnError.get().toLong(), TimeUnit.SECONDS)
                }
            }
            .subscribe(object : Observer<SignalingOverall> {
                override fun onSubscribe(d: Disposable) {
                    signalingDisposable = d
                }

                override fun onNext(signalingOverall: SignalingOverall) {
                    receivedSignalingMessages(signalingOverall.ocs!!.signalings)
                }

                override fun onError(e: Throwable) {
                    dispose(signalingDisposable)
                }

                override fun onComplete() {
                    dispose(signalingDisposable)
                }
            })
    }

    private fun setupAndInitiateWebSocketsConnection() {
        if (webSocketConnectionHelper == null) {
            webSocketConnectionHelper = GccWebSocketConnectionHelper()
        }
        if (webSocketClient == null) {
            webSocketClient = GccWebSocketConnectionHelper.getExternalSignalingInstanceForServer(
                externalSignalingServer!!.externalSignalingServer,
                conversationUser,
                externalSignalingServer!!.externalSignalingTicket,
                TextUtils.isEmpty(credentials)
            )
            // Although setupAndInitiateWebSocketsConnection could be called several times the web socket is
            // initialized just once, so the message receiver is also initialized just once.
            signalingMessageReceiver = webSocketClient!!.getSignalingMessageReceiver()
            signalingMessageReceiver!!.addListener(localParticipantMessageListener)
            signalingMessageReceiver!!.addListener(offerMessageListener)
            signalingMessageSender = webSocketClient!!.signalingMessageSender

            // If the connection with the signaling server was not established yet the value will be false, but it will
            // be overwritten with the right value once the response to the "hello" message is received.
            hasMCU = webSocketClient!!.hasMCU()
            Log.d(TAG, "hasMCU is $hasMCU")

            if (hasMCU) {
                messageSender = GccMessageSenderMcu(
                    signalingMessageSender,
                    getParticipantSessionKeys(),
                    peerConnectionWrapperList,
                    webSocketClient!!.sessionId
                )
            } else {
                messageSender = GccMessageSenderNoMcu(
                    signalingMessageSender,
                    getParticipantSessionKeys(),
                    peerConnectionWrapperList
                )
            }
        } else {
            if (webSocketClient!!.isConnected && currentCallStatus === GccCallStatus.PUBLISHER_FAILED) {
                webSocketClient!!.restartWebSocket()
            }
        }
        joinRoomAndCall()
    }

    private fun initiateCall() {
        if (isConnectionEstablished) {
            Log.d(TAG, "connection already established")
            return
        }
        fetchSignalingSettings()
    }

    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    fun onMessageEvent(webSocketCommunicationEvent: GccWebSocketCommunicationEvent) {
        if (currentCallStatus === GccCallStatus.LEAVING) {
            return
        }
        if (webSocketCommunicationEvent.getHashMap() != null) {
            when (webSocketCommunicationEvent.getType()) {
                "hello" -> {
                    Log.d(TAG, "onMessageEvent 'hello'")

                    hasMCU = webSocketClient!!.hasMCU()
                    Log.d(TAG, "hasMCU is $hasMCU")

                    if (hasMCU) {
                        messageSender = GccMessageSenderMcu(
                            signalingMessageSender,
                            getParticipantSessionKeys(),
                            peerConnectionWrapperList,
                            webSocketClient!!.sessionId
                        )
                    } else {
                        messageSender = GccMessageSenderNoMcu(
                            signalingMessageSender,
                            getParticipantSessionKeys(),
                            peerConnectionWrapperList
                        )
                    }

                    if (!webSocketCommunicationEvent.getHashMap()!!.containsKey("oldResumeId")) {
                        if (currentCallStatus === GccCallStatus.RECONNECTING) {
                            hangup(false, false)
                        } else {
                            setCallState(GccCallStatus.RECONNECTING)
                            runOnUiThread { initiateCall() }
                        }
                    }
                }

                "roomJoined" -> {
                    Log.d(TAG, "onMessageEvent 'roomJoined'")
                    startSendingNick()
                    if (webSocketCommunicationEvent.getHashMap()!!["roomToken"] == roomToken) {
                        performCall()
                    }
                }

                "recordingStatus" -> {
                    Log.d(TAG, "onMessageEvent 'recordingStatus'")
                    if (webSocketCommunicationEvent.getHashMap()!!.containsKey(KEY_RECORDING_STATE)) {
                        val recordingStateString = webSocketCommunicationEvent.getHashMap()!![KEY_RECORDING_STATE]
                        if (recordingStateString != null) {
                            runOnUiThread { callRecordingViewModel!!.setRecordingState(recordingStateString.toInt()) }
                        }
                    }
                }
            }
        }
    }

    private fun dispose(disposable: Disposable?) {
        if (disposable != null && !disposable.isDisposed) {
            disposable.dispose()
        } else if (disposable == null) {
            if (signalingDisposable != null && !signalingDisposable!!.isDisposed) {
                signalingDisposable!!.dispose()
                signalingDisposable = null
            }
        }
    }

    private fun receivedSignalingMessages(signalingList: List<Signaling>?) {
        if (signalingList != null) {
            for (signaling in signalingList) {
                try {
                    receivedSignalingMessage(signaling)
                } catch (e: IOException) {
                    Log.e(TAG, "Failed to process received signaling message", e)
                }
            }
        }
    }

    @Throws(IOException::class)
    private fun receivedSignalingMessage(signaling: Signaling) {
        val messageType = signaling.type
        if (!isConnectionEstablished && currentCallStatus !== GccCallStatus.CONNECTING) {
            return
        }

        when (messageType) {
            "usersInRoom" ->
                internalSignalingMessageReceiver.process(signaling.messageWrapper as List<Map<String?, Any?>?>?)

            "message" -> {
                val ncSignalingMessage = LoganSquare.parse(
                    signaling.messageWrapper.toString(),
                    NCSignalingMessage::class.java
                )
                internalSignalingMessageReceiver.process(ncSignalingMessage)
            }

            else ->
                Log.e(TAG, "unexpected message type when receiving signaling message")
        }
    }

    private fun hangup(shutDownView: Boolean, endCallForAll: Boolean) {
        Log.d(TAG, "hangup! shutDownView=$shutDownView")
        if (shutDownView) {
            setCallState(GccCallStatus.LEAVING)
        }
        stopCallingSound()
        callTimeHandler.removeCallbacksAndMessages(null)
        dispose(null)

        if (shutDownView) {
            terminateAudioVideo()
        }

        val peerConnectionIdsToEnd: MutableList<String> = ArrayList(peerConnectionWrapperList.size)
        for (wrapper in peerConnectionWrapperList) {
            peerConnectionIdsToEnd.add(wrapper.sessionId)
        }
        for (sessionId in peerConnectionIdsToEnd) {
            endPeerConnection(sessionId, "video")
            endPeerConnection(sessionId, "screen")
        }
        val callParticipantIdsToEnd: MutableList<String> = ArrayList(peerConnectionWrapperList.size)
        for (sessionId in callViewModel.participants.value.map { it.sessionKey }) {
            sessionId?.let {
                callParticipantIdsToEnd.add(it)
            }
        }
        for (sessionId in callParticipantIdsToEnd) {
            removeCallParticipant(sessionId)
        }
        GccApplicationWideCurrentRoomHolder.getInstance().isInCall = false
        GccApplicationWideCurrentRoomHolder.getInstance().isDialing = false
        hangupNetworkCalls(shutDownView, endCallForAll)
    }

    private fun terminateAudioVideo() {
        if (videoCapturer != null) {
            try {
                videoCapturer!!.stopCapture()
            } catch (e: InterruptedException) {
                Log.e(TAG, "Failed to stop capturing while hanging up")
            }
            videoCapturer!!.dispose()
            videoCapturer = null
        }

        binding!!.pipSelfVideoRenderer.clearImage()
        binding!!.pipSelfVideoRenderer.release()
        if (audioSource != null) {
            audioSource!!.dispose()
            audioSource = null
        }
        runOnUiThread {
            if (audioManager != null) {
                audioManager!!.stop()
                audioManager = null
            }
        }
        if (videoSource != null) {
            videoSource = null
        }
        if (peerConnectionFactory != null) {
            peerConnectionFactory = null
        }
        localAudioTrack = null
        localVideoTrack = null
        if (TextUtils.isEmpty(credentials) && hasExternalSignalingServer) {
            GccWebSocketConnectionHelper.deleteExternalSignalingInstanceForUserEntity(-1)
        }
    }

    private fun hangupNetworkCalls(shutDownView: Boolean, endCallForAll: Boolean) {
        Log.d(TAG, "hangupNetworkCalls. shutDownView=$shutDownView")
        val apiVersion = GccApiUtils.getCallApiVersion(conversationUser, intArrayOf(GccApiUtils.API_V4, 1))
        if (localStateBroadcaster != null) {
            localStateBroadcaster!!.destroy()
        }
        if (callParticipantList != null) {
            callParticipantList!!.removeObserver(callParticipantListObserver)
            callParticipantList!!.destroy()
        }
        val endCall: Boolean? = if (endCallForAll) true else null

        ncApi!!.leaveCall(credentials, GccApiUtils.getUrlForCall(apiVersion, baseUrl, roomToken!!), endCall)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : Observer<GenericOverall> {
                override fun onSubscribe(d: Disposable) {
                    // unused atm
                }

                override fun onNext(genericOverall: GenericOverall) {
                    if (switchToRoomToken.isNotEmpty()) {
                        val intent = Intent(context, GccChatActivity::class.java)
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                        val bundle = Bundle()
                        bundle.putBoolean(KEY_SWITCH_TO_ROOM, true)
                        bundle.putBoolean(KEY_START_CALL_AFTER_ROOM_SWITCH, true)
                        bundle.putString(KEY_ROOM_TOKEN, switchToRoomToken)
                        bundle.putBoolean(KEY_CALL_VOICE_ONLY, isVoiceOnlyCall)
                        intent.putExtras(bundle)
                        startActivity(intent)
                        finish()
                    } else if (shutDownView) {
                        finish()
                    } else if (currentCallStatus === GccCallStatus.RECONNECTING ||
                        currentCallStatus === GccCallStatus.PUBLISHER_FAILED
                    ) {
                        initiateCall()
                    }
                }

                override fun onError(e: Throwable) {
                    Log.w(TAG, "Something went wrong when leaving the call", e)
                    finish()
                }

                override fun onComplete() {
                    // unused atm
                }
            })
    }

    private fun startVideoCapture(isPortrait: Boolean) {
        val (width, height) = if (isPortrait) {
            WIDTH_4_TO_3_RATIO to HEIGHT_4_TO_3_RATIO
        } else {
            WIDTH_16_TO_9_RATIO to HEIGHT_16_TO_9_RATIO
        }

        videoCapturer?.let {
            it.stopCapture()
            it.startCapture(width, height, FRAME_RATE)
        }
    }

    private fun setupOrientationListener(context: Context) {
        var lastAspectRatio = ""

        val orientationEventListener = object : OrientationEventListener(context) {
            override fun onOrientationChanged(orientation: Int) {
                when (orientation) {
                    in ANGLE_0..ANGLE_PORTRAIT_RIGHT_THRESHOLD,
                    in ANGLE_PORTRAIT_LEFT_THRESHOLD..ANGLE_FULL -> {
                        if (lastAspectRatio != RATIO_4_TO_3) {
                            lastAspectRatio = RATIO_4_TO_3
                            startVideoCapture(true)
                        }
                    }

                    in ANGLE_LANDSCAPE_RIGHT_THRESHOLD_MIN..ANGLE_LANDSCAPE_RIGHT_THRESHOLD_MAX,
                    in ANGLE_LANDSCAPE_LEFT_THRESHOLD_MIN..ANGLE_LANDSCAPE_LEFT_THRESHOLD_MAX -> {
                        if (lastAspectRatio != RATIO_16_TO_9) {
                            lastAspectRatio = RATIO_16_TO_9
                            startVideoCapture(false)
                        }
                    }
                }
            }
        }
        orientationEventListener.enable()
    }

    @Suppress("Detekt.ComplexMethod")
    private fun handleCallParticipantsChanged(
        joined: Collection<Participant>,
        updated: Collection<Participant>,
        left: Collection<Participant>,
        unchanged: Collection<Participant>
    ) {
        Log.d(TAG, "handleCallParticipantsChanged")

        // The signaling session is the same as the Nextcloud session only when the MCU is not used.
        var currentSessionId = callSession
        if (hasMCU) {
            currentSessionId = webSocketClient!!.sessionId
        }
        Log.d(TAG, "   currentSessionId is $currentSessionId")

        val participantsInCall: MutableList<Participant> = ArrayList()
        participantsInCall.addAll(joined)
        participantsInCall.addAll(updated)
        participantsInCall.addAll(unchanged)

        var isSelfInCall = false
        var selfParticipant: Participant? = null

        for (participant in participantsInCall) {
            val inCallFlag = participant.inCall
            if (participant.sessionId != currentSessionId) {
                Log.d(
                    TAG,
                    "   inCallFlag of participant " +
                        participant.sessionId!!.substring(0, SESSION_ID_PREFFIX_END) +
                        " : " +
                        inCallFlag
                )
            } else {
                Log.d(TAG, "   inCallFlag of currentSessionId: $inCallFlag")
                isSelfInCall = inCallFlag != 0L
                selfParticipant = participant
            }
        }

        if (!isSelfInCall &&
            currentCallStatus !== GccCallStatus.LEAVING &&
            GccApplicationWideCurrentRoomHolder.getInstance().isInCall
        ) {
            Log.d(TAG, "Most probably a moderator ended the call for all.")
            hangup(shutDownView = true, endCallForAll = false)
            return
        }

        if (!isSelfInCall) {
            Log.d(TAG, "Self not in call, disconnecting from all other sessions")
            removeSessions(participantsInCall)
            return
        }
        if (currentCallStatus === GccCallStatus.LEAVING) {
            return
        }
        if (hasMCU) {
            // Ensure that own publishing peer is set up.
            getOrCreatePeerConnectionWrapperForSessionIdAndType(
                webSocketClient!!.sessionId,
                VIDEO_STREAM_TYPE_VIDEO,
                true
            )
        }
        handleJoinedCallParticipantsChanged(selfParticipant, joined, currentSessionId)

        if (othersInCall && currentCallStatus !== GccCallStatus.IN_CONVERSATION) {
            setCallState(GccCallStatus.IN_CONVERSATION)
        }
        removeSessions(left)
    }

    private fun removeSessions(sessions: Collection<Participant>) {
        for ((_, _, _, _, _, _, _, _, _, _, session) in sessions) {
            Log.d(TAG, "   session that will be removed is: $session")
            endPeerConnection(session, "video")
            endPeerConnection(session, "screen")
            removeCallParticipant(session)
        }
    }

    private fun handleJoinedCallParticipantsChanged(
        selfParticipant: Participant?,
        joined: Collection<Participant>,
        currentSessionId: String?
    ) {
        var selfJoined = false
        val selfParticipantHasAudioOrVideo = participantInCallFlagsHaveAudioOrVideo(selfParticipant)
        for (participant in joined) {
            val sessionId = participant.sessionId
            if (sessionId == null) {
                Log.w(TAG, "Null sessionId for call participant, this should not happen: $participant")
                continue
            }
            if (sessionId == currentSessionId) {
                selfJoined = true
                continue
            }
            Log.d(TAG, "   newSession joined: $sessionId")
            addCallParticipant(sessionId)

            if (participant.actorType != null && participant.actorId != null) {
                callViewModel.getParticipant(sessionId)?.updateActor(participant.actorType, participant.actorId)
            }

            if (participant.internal != null) {
                callViewModel.getParticipant(sessionId)?.updateIsInternal(participant.internal == true)
            }

            val nick: String? = if (hasExternalSignalingServer) {
                webSocketClient!!.getDisplayNameForSession(sessionId)
            } else {
                if (offerAnswerNickProviders[sessionId] != null) offerAnswerNickProviders[sessionId]?.nick else ""
            }

            callViewModel.getParticipant(sessionId)?.updateNick(nick)
            val participantHasAudioOrVideo = participantInCallFlagsHaveAudioOrVideo(participant)

            // FIXME Without MCU, GccPeerConnectionWrapper only sends an offer if the local session ID is higher than the
            // remote session ID. However, if the other participant does not have audio nor video that participant
            // will not send an offer, so no connection is actually established when the remote participant has a
            // higher session ID but is not publishing media.
            if (hasMCUAndAudioVideo(participantHasAudioOrVideo) ||
                hasNoMCUAndAudioVideo(
                    participantHasAudioOrVideo,
                    selfParticipantHasAudioOrVideo,
                    sessionId,
                    currentSessionId!!
                )
            ) {
                getOrCreatePeerConnectionWrapperForSessionIdAndType(sessionId, VIDEO_STREAM_TYPE_VIDEO, false)
            }
        }
        othersInCall = if (selfJoined) {
            joined.size > 1
        } else {
            joined.isNotEmpty()
        }
    }

    private fun hasMCUAndAudioVideo(participantHasAudioOrVideo: Boolean): Boolean = hasMCU && participantHasAudioOrVideo

    private fun hasNoMCUAndAudioVideo(
        participantHasAudioOrVideo: Boolean,
        selfParticipantHasAudioOrVideo: Boolean,
        sessionId: String,
        currentSessionId: String
    ): Boolean =
        !hasMCU && selfParticipantHasAudioOrVideo && (!participantHasAudioOrVideo || sessionId < currentSessionId)

    private fun participantInCallFlagsHaveAudioOrVideo(participant: Participant?): Boolean =
        if (participant == null) {
            false
        } else {
            participant.inCall and Participant.InCallFlags.WITH_AUDIO.toLong() > 0 ||
                !isVoiceOnlyCall &&
                participant.inCall and Participant.InCallFlags.WITH_VIDEO.toLong() > 0
        }

    private fun getPeerConnectionWrapperForSessionIdAndType(sessionId: String?, type: String): GccPeerConnectionWrapper? {
        for (wrapper in peerConnectionWrapperList) {
            if (wrapper.sessionId == sessionId && wrapper.videoStreamType == type) {
                return wrapper
            }
        }
        return null
    }

    private fun getOrCreatePeerConnectionWrapperForSessionIdAndType(
        sessionId: String?,
        type: String,
        publisher: Boolean
    ): GccPeerConnectionWrapper? {
        var peerConnectionWrapper = getPeerConnectionWrapperForSessionIdAndType(sessionId, type)

        return if (peerConnectionWrapper != null) {
            peerConnectionWrapper
        } else {
            if (peerConnectionFactory == null) {
                Log.e(TAG, "peerConnectionFactory was null in getOrCreatePeerConnectionWrapperForSessionIdAndType")
                Snackbar.make(
                    binding!!.root,
                    context.resources.getString(R.string.nc_common_error_sorry),
                    Snackbar.LENGTH_LONG
                ).show()
                hangup(shutDownView = true, endCallForAll = false)
                return null
            }
            peerConnectionWrapper = createPeerConnectionWrapperForSessionIdAndType(publisher, sessionId, type)
            peerConnectionWrapperList.add(peerConnectionWrapper)
            if (!publisher) {
                if (!callViewModel.doesParticipantExist(sessionId)) {
                    addCallParticipant(sessionId)
                }

                if ("screen" == type) {
                    callViewModel.getParticipant(sessionId)?.setScreenPeerConnection(peerConnectionWrapper)
                } else {
                    callViewModel.getParticipant(sessionId)?.setPeerConnection(peerConnectionWrapper)
                }
            }
            if (publisher) {
                peerConnectionWrapper.addObserver(selfPeerConnectionObserver)
                startSendingNick()
            }
            peerConnectionWrapper
        }
    }

    private fun createPeerConnectionWrapperForSessionIdAndType(
        publisher: Boolean,
        sessionId: String?,
        type: String
    ): GccPeerConnectionWrapper {
        fun getPeerConnectionFactory(type: String): PeerConnectionFactory? {
            fun initScreenSharePeerConnectionFactory(): PeerConnectionFactory? {
                val options = PeerConnectionFactory.Options()
                val softwareVideoEncoderFactory = SoftwareVideoEncoderFactory()
                val softwareVideoDecoderFactory = SoftwareVideoDecoderFactory()
                screenSharePeerConnectionFactory = PeerConnectionFactory.builder()
                    .setOptions(options)
                    .setVideoEncoderFactory(softwareVideoEncoderFactory)
                    .setVideoDecoderFactory(softwareVideoDecoderFactory)
                    .createPeerConnectionFactory()
                return screenSharePeerConnectionFactory
            }

            val tempPeerConnectionFactory = if (type == "screen") {
                screenSharePeerConnectionFactory ?: run {
                    initScreenSharePeerConnectionFactory()
                }
            } else {
                peerConnectionFactory
            }
            return tempPeerConnectionFactory
        }

        val tempPeerConnectionFactory: PeerConnectionFactory?
        val tempSdpConstraints: MediaConstraints?
        val tempIsMCUPublisher: Boolean
        val tempHasMCU: Boolean
        val tempLocalStream: MediaStream?
        if (hasMCU && publisher) {
            tempPeerConnectionFactory = peerConnectionFactory
            tempSdpConstraints = sdpConstraintsForMCUPublisher
            tempIsMCUPublisher = true
            tempHasMCU = true
            tempLocalStream = localStream
        } else if (hasMCU) {
            tempPeerConnectionFactory = getPeerConnectionFactory(type)
            tempSdpConstraints = GccMediaConstraintsHelper(sdpConstraints)
                .copy()
                .applyIf(type == "screen") { replaceOrAddConstraint("OfferToReceiveVideo", "true") }
                .build()
            tempIsMCUPublisher = false
            tempHasMCU = true
            tempLocalStream = null
        } else {
            tempPeerConnectionFactory = getPeerConnectionFactory(type)
            tempSdpConstraints = GccMediaConstraintsHelper(sdpConstraints)
                .copy()
                .applyIf(type == "screen") { replaceOrAddConstraint("OfferToReceiveVideo", "true") }
                .build()
            tempIsMCUPublisher = false
            tempHasMCU = false
            tempLocalStream = if ("screen" != type) {
                localStream
            } else {
                null
            }
        }

        return GccPeerConnectionWrapper(
            tempPeerConnectionFactory,
            iceServers,
            tempSdpConstraints,
            sessionId,
            callSession,
            tempLocalStream,
            tempIsMCUPublisher,
            tempHasMCU,
            type,
            signalingMessageReceiver,
            signalingMessageSender
        )
    }

    private fun addCallParticipant(sessionId: String?) {
        val callParticipantMessageListener: CallParticipantMessageListener =
            CallActivityCallParticipantMessageListener(sessionId)
        callParticipantMessageListeners[sessionId] = callParticipantMessageListener
        signalingMessageReceiver!!.addListener(callParticipantMessageListener, sessionId)
        if (!hasExternalSignalingServer) {
            val offerAnswerNickProvider = OfferAnswerNickProvider(sessionId)
            offerAnswerNickProviders[sessionId] = offerAnswerNickProvider
            signalingMessageReceiver!!.addListener(
                offerAnswerNickProvider.videoWebRtcMessageListener,
                sessionId,
                "video"
            )
            signalingMessageReceiver!!.addListener(
                offerAnswerNickProvider.screenWebRtcMessageListener,
                sessionId,
                "screen"
            )
        }

        callViewModel.addParticipant(
            baseUrl!!,
            roomToken!!,
            sessionId!!,
            signalingMessageReceiver!!
        )

        localStateBroadcaster!!.handleCallParticipantAdded(callViewModel.getParticipant(sessionId)?.uiState?.value)

        initPipMode()
    }

    private fun endPeerConnection(sessionId: String?, type: String) {
        val peerConnectionWrapper = getPeerConnectionWrapperForSessionIdAndType(sessionId, type) ?: return
        if (webSocketClient != null &&
            webSocketClient!!.sessionId != null &&
            webSocketClient!!.sessionId == sessionId
        ) {
            peerConnectionWrapper.removeObserver(selfPeerConnectionObserver)
        }

        if ("screen" == type) {
            callViewModel.getParticipant(sessionId)?.setScreenPeerConnection(null)
        } else {
            callViewModel.getParticipant(sessionId)?.setPeerConnection(null)
        }

        peerConnectionWrapper.removePeerConnection()
        peerConnectionWrapperList.remove(peerConnectionWrapper)
    }

    private fun removeCallParticipant(sessionId: String?) {
        if (!callViewModel.doesParticipantExist(sessionId)) {
            return
        }

        callViewModel.removeParticipant(sessionId!!)

        localStateBroadcaster!!.handleCallParticipantRemoved(sessionId)

        val listener = callParticipantMessageListeners.remove(sessionId)
        signalingMessageReceiver!!.removeListener(listener)
        val offerAnswerNickProvider = offerAnswerNickProviders.remove(sessionId)
        if (offerAnswerNickProvider != null) {
            signalingMessageReceiver!!.removeListener(offerAnswerNickProvider.videoWebRtcMessageListener)
            signalingMessageReceiver!!.removeListener(offerAnswerNickProvider.screenWebRtcMessageListener)
        }
        initPipMode()
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(configurationChangeEvent: GccConfigurationChangeEvent?) {
        powerManagerUtils!!.setOrientation(Objects.requireNonNull(resources).configuration.orientation)
        initPipMode()
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(proximitySensorEvent: GccProximitySensorEvent) {
        if (!isVoiceOnlyCall) {
            val enableVideo = proximitySensorEvent.proximitySensorEventType ==
                GccProximitySensorEvent.ProximitySensorEventType.SENSOR_FAR &&
                videoOn
            if (permissionUtil!!.isCameraPermissionGranted() &&
                isConnectingOrEstablished() &&
                videoOn &&
                enableVideo != localVideoTrack!!.enabled()
            ) {
                toggleMedia(enableVideo, true)
            }
        }
    }

    private fun isConnectingOrEstablished(): Boolean =
        currentCallStatus === GccCallStatus.CONNECTING || isConnectionEstablished

    private fun startSendingNick() {
        val dataChannelMessage = DataChannelMessage()
        dataChannelMessage.type = "nickChanged"
        val nickChangedPayload: MutableMap<String, String> = HashMap()
        nickChangedPayload["userid"] = conversationUser!!.userId!!
        nickChangedPayload["name"] = conversationUser!!.displayName!!
        dataChannelMessage.payloadMap = nickChangedPayload.toMap()
        for (peerConnectionWrapper in peerConnectionWrapperList) {
            if (peerConnectionWrapper.isMCUPublisher) {
                Observable
                    .interval(1, TimeUnit.SECONDS)
                    .repeatUntil { !isConnectionEstablished || isDestroyed }
                    .observeOn(Schedulers.io())
                    .subscribe(object : Observer<Long> {
                        override fun onSubscribe(d: Disposable) {
                            // unused atm
                        }

                        override fun onNext(aLong: Long) {
                            peerConnectionWrapper.send(dataChannelMessage)
                        }

                        override fun onError(e: Throwable) {
                            // unused atm
                        }

                        override fun onComplete() {
                            // unused atm
                        }
                    })
                break
            }
        }
    }

    private fun setCallState(callState: GccCallStatus) {
        if (currentCallStatus == null || currentCallStatus !== callState) {
            currentCallStatus = callState
            if (handler == null) {
                handler = Handler(Looper.getMainLooper())
            } else {
                handler!!.removeCallbacksAndMessages(null)
            }
            when (callState) {
                GccCallStatus.CONNECTING -> handler!!.post { handleCallStateConnected() }
                GccCallStatus.CALLING_TIMEOUT -> handler!!.post { handleCallStateCallingTimeout() }
                GccCallStatus.PUBLISHER_FAILED -> handler!!.post { handleCallStatePublisherFailed() }
                GccCallStatus.RECONNECTING -> handler!!.post { handleCallStateReconnecting() }
                GccCallStatus.JOINED -> {
                    handler!!.postDelayed({ setCallState(GccCallStatus.CALLING_TIMEOUT) }, CALLING_TIMEOUT)
                    handler!!.post { handleCallStateJoined() }
                }

                GccCallStatus.IN_CONVERSATION -> handler!!.post { handleCallStateInConversation() }
                GccCallStatus.OFFLINE -> handler!!.post { handleCallStateOffline() }
                GccCallStatus.LEAVING -> handler!!.post { handleCallStateLeaving() }
            }
        }
    }

    private fun handleCallStateLeaving() {
        if (!isDestroyed) {
            stopCallingSound()
            binding!!.callStates.callStateTextView.setText(R.string.nc_leaving_call)
            binding!!.callStates.callStateRelativeLayout.visibility = View.VISIBLE
            binding!!.composeParticipantGrid.visibility = View.INVISIBLE
            binding!!.callStates.callStateProgressBar.visibility = View.VISIBLE
            binding!!.callStates.errorImageView.visibility = View.GONE
        }
    }

    private fun handleCallStateOffline() {
        stopCallingSound()
        binding!!.callStates.callStateTextView.setText(R.string.nc_offline)
        if (binding!!.callStates.callStateRelativeLayout.visibility != View.VISIBLE) {
            binding!!.callStates.callStateRelativeLayout.visibility = View.VISIBLE
        }
        if (binding!!.composeParticipantGrid.visibility != View.INVISIBLE) {
            binding!!.composeParticipantGrid.visibility = View.INVISIBLE
        }
        if (binding!!.callStates.callStateProgressBar.visibility != View.GONE) {
            binding!!.callStates.callStateProgressBar.visibility = View.GONE
        }
        binding!!.callStates.errorImageView.setImageResource(R.drawable.ic_signal_wifi_off_white_24dp)
        if (binding!!.callStates.errorImageView.visibility != View.VISIBLE) {
            binding!!.callStates.errorImageView.visibility = View.VISIBLE
        }
    }

    private fun handleCallStateInConversation() {
        stopCallingSound()
        if (binding!!.callStates.callStateRelativeLayout.visibility != View.INVISIBLE) {
            binding!!.callStates.callStateRelativeLayout.visibility = View.INVISIBLE
        }
        if (binding!!.callStates.callStateProgressBar.visibility != View.GONE) {
            binding!!.callStates.callStateProgressBar.visibility = View.GONE
        }
        if (binding!!.composeParticipantGrid.visibility != View.VISIBLE) {
            binding!!.composeParticipantGrid.visibility = View.VISIBLE
        }
        if (binding!!.callStates.errorImageView.visibility != View.GONE) {
            binding!!.callStates.errorImageView.visibility = View.GONE
        }
    }

    private fun handleCallStateJoined() {
        if (isIncomingCallFromNotification) {
            binding!!.callStates.callStateTextView.setText(R.string.nc_call_incoming)
        } else {
            binding!!.callStates.callStateTextView.setText(R.string.nc_call_ringing)
        }
        if (binding!!.callStates.callStateRelativeLayout.visibility != View.VISIBLE) {
            binding!!.callStates.callStateRelativeLayout.visibility = View.VISIBLE
        }
        if (binding!!.callStates.callStateProgressBar.visibility != View.VISIBLE) {
            binding!!.callStates.callStateProgressBar.visibility = View.VISIBLE
        }
        if (binding!!.composeParticipantGrid.visibility != View.INVISIBLE) {
            binding!!.composeParticipantGrid.visibility = View.INVISIBLE
        }
        if (binding!!.callStates.errorImageView.visibility != View.GONE) {
            binding!!.callStates.errorImageView.visibility = View.GONE
        }
    }

    private fun handleCallStateReconnecting() {
        playCallingSound()
        binding!!.callStates.callStateTextView.setText(R.string.nc_call_reconnecting)
        if (binding!!.callStates.callStateRelativeLayout.visibility != View.VISIBLE) {
            binding!!.callStates.callStateRelativeLayout.visibility = View.VISIBLE
        }
        if (binding!!.composeParticipantGrid.visibility != View.INVISIBLE) {
            binding!!.composeParticipantGrid.visibility = View.INVISIBLE
        }
        if (binding!!.callStates.callStateProgressBar.visibility != View.VISIBLE) {
            binding!!.callStates.callStateProgressBar.visibility = View.VISIBLE
        }
        if (binding!!.callStates.errorImageView.visibility != View.GONE) {
            binding!!.callStates.errorImageView.visibility = View.GONE
        }
    }

    private fun handleCallStatePublisherFailed() {
        // No calling sound when the publisher failed
        binding!!.callStates.callStateTextView.setText(R.string.nc_call_reconnecting)
        if (binding!!.callStates.callStateRelativeLayout.visibility != View.VISIBLE) {
            binding!!.callStates.callStateRelativeLayout.visibility = View.VISIBLE
        }
        if (binding!!.composeParticipantGrid.visibility != View.INVISIBLE) {
            binding!!.composeParticipantGrid.visibility = View.INVISIBLE
        }
        if (binding!!.callStates.callStateProgressBar.visibility != View.VISIBLE) {
            binding!!.callStates.callStateProgressBar.visibility = View.VISIBLE
        }
        if (binding!!.callStates.errorImageView.visibility != View.GONE) {
            binding!!.callStates.errorImageView.visibility = View.GONE
        }
    }

    private fun handleCallStateCallingTimeout() {
        hangup(shutDownView = false, endCallForAll = false)
        binding!!.callStates.callStateTextView.setText(R.string.nc_call_timeout)
        if (binding!!.callStates.callStateRelativeLayout.visibility != View.VISIBLE) {
            binding!!.callStates.callStateRelativeLayout.visibility = View.VISIBLE
        }
        if (binding!!.callStates.callStateProgressBar.visibility != View.GONE) {
            binding!!.callStates.callStateProgressBar.visibility = View.GONE
        }
        if (binding!!.composeParticipantGrid.visibility != View.INVISIBLE) {
            binding!!.composeParticipantGrid.visibility = View.INVISIBLE
        }
        binding!!.callStates.errorImageView.setImageResource(R.drawable.ic_av_timer_timer_24dp)
        if (binding!!.callStates.errorImageView.visibility != View.VISIBLE) {
            binding!!.callStates.errorImageView.visibility = View.VISIBLE
        }
    }

    private fun handleCallStateConnected() {
        playCallingSound()
        if (isIncomingCallFromNotification) {
            binding!!.callStates.callStateTextView.setText(R.string.nc_call_incoming)
        } else {
            binding!!.callStates.callStateTextView.setText(R.string.nc_call_ringing)
        }
        binding!!.callConversationNameTextView.text = conversationName
        if (binding!!.callStates.callStateRelativeLayout.visibility != View.VISIBLE) {
            binding!!.callStates.callStateRelativeLayout.visibility = View.VISIBLE
        }
        if (binding!!.composeParticipantGrid.visibility != View.INVISIBLE) {
            binding!!.composeParticipantGrid.visibility = View.INVISIBLE
        }
        if (binding!!.callStates.callStateProgressBar.visibility != View.VISIBLE) {
            binding!!.callStates.callStateProgressBar.visibility = View.VISIBLE
        }
        if (binding!!.callStates.errorImageView.visibility != View.GONE) {
            binding!!.callStates.errorImageView.visibility = View.GONE
        }
    }

    private fun playCallingSound() {
        stopCallingSound()
        val ringtoneUri: Uri? = if (isIncomingCallFromNotification) {
            getCallRingtoneUri(applicationContext, appPreferences)
        } else {
            ("android.resource://" + applicationContext.packageName + "/raw/tr110_1_kap8_3_freiton1").toUri()
        }
        if (ringtoneUri != null) {
            mediaPlayer = MediaPlayer()
            try {
                mediaPlayer!!.setDataSource(this, ringtoneUri)
                mediaPlayer!!.isLooping = true
                val audioAttributes = AudioAttributes.Builder().setContentType(
                    AudioAttributes.CONTENT_TYPE_SONIFICATION
                )
                    .setUsage(AudioAttributes.USAGE_VOICE_COMMUNICATION)
                    .build()
                mediaPlayer!!.setAudioAttributes(audioAttributes)
                mediaPlayer!!.setOnPreparedListener { mp: MediaPlayer? -> mediaPlayer!!.start() }
                mediaPlayer!!.prepareAsync()
            } catch (e: IOException) {
                Log.e(TAG, "Failed to play sound")
            }
        }
    }

    private fun stopCallingSound() {
        if (mediaPlayer != null) {
            try {
                if (mediaPlayer!!.isPlaying) {
                    mediaPlayer!!.stop()
                }
            } catch (e: IllegalStateException) {
                Log.e(TAG, "mediaPlayer was not initialized", e)
            } finally {
                if (mediaPlayer != null) {
                    mediaPlayer!!.release()
                }
                mediaPlayer = null
            }
        }
    }

    fun addReactionForAnimation(emoji: String?, displayName: String?) {
        reactionAnimator!!.addReaction(emoji!!, displayName!!)
    }

    /**
     * Temporary implementation of GccSignalingMessageReceiver until signaling related code is extracted from
     * GccCallActivity.
     *
     *
     * All listeners are called in the main thread.
     */
    private class InternalSignalingMessageReceiver : GccSignalingMessageReceiver() {
        fun process(users: List<Map<String?, Any?>?>?) {
            processUsersInRoom(users)
        }

        fun process(message: NCSignalingMessage?) {
            processSignalingMessage(message)
        }
    }

    private inner class OfferAnswerNickProvider(private val sessionId: String?) {
        val videoWebRtcMessageListener: WebRtcMessageListener = WebRtcMessageListener()
        val screenWebRtcMessageListener: WebRtcMessageListener = WebRtcMessageListener()
        var nick: String? = null
            private set

        private inner class WebRtcMessageListener : GccSignalingMessageReceiver.WebRtcMessageListener {
            override fun onOffer(sdp: String, nick: String?) {
                onOfferOrAnswer(nick)
            }

            override fun onAnswer(sdp: String, nick: String?) {
                onOfferOrAnswer(nick)
            }

            override fun onCandidate(sdpMid: String, sdpMLineIndex: Int, sdp: String) {
                // unused atm
            }

            override fun onEndOfCandidates() {
                // unused atm
            }
        }

        private fun onOfferOrAnswer(nick: String?) {
            this.nick = nick
            callViewModel.getParticipant(sessionId)?.updateNick(nick)
        }
    }

    private inner class CallActivityCallParticipantMessageListener(private val sessionId: String?) :
        CallParticipantMessageListener {
        override fun onRaiseHand(state: Boolean, timestamp: Long) {
            if (state) {
                CoroutineScope(Dispatchers.Main).launch {
                    callViewModel.getParticipant(sessionId)?.uiState?.value?.nick?.let {
                        Snackbar.make(
                            binding!!.root,
                            String.format(context.resources.getString(R.string.nc_call_raised_hand), it),
                            Snackbar.LENGTH_LONG
                        ).show()
                    }
                }
            }
        }

        override fun onReaction(reaction: String) {
            CoroutineScope(Dispatchers.Main).launch {
                callViewModel.getParticipant(sessionId)?.uiState?.value?.nick?.let {
                    addReactionForAnimation(
                        emoji = reaction,
                        displayName = it
                    )
                }
            }
        }

        override fun onUnshareScreen() {
            endPeerConnection(sessionId, "screen")
        }
    }

    private inner class CallActivitySelfPeerConnectionObserver : PeerConnectionObserver {
        override fun onStreamAdded(mediaStream: MediaStream) {
            // unused atm
        }

        override fun onStreamRemoved(mediaStream: MediaStream) {
            // unused atm
        }

        override fun onIceConnectionStateChanged(iceConnectionState: IceConnectionState) {
            runOnUiThread {
                if (iceConnectionState == IceConnectionState.FAILED) {
                    setCallState(GccCallStatus.PUBLISHER_FAILED)
                    webSocketClient!!.clearResumeId()
                    hangup(false, false)
                }
            }
        }
    }

    private inner class InternalSignalingMessageSender : GccSignalingMessageSender {
        override fun send(ncSignalingMessage: NCSignalingMessage) {
            addLocalParticipantNickIfNeeded(ncSignalingMessage)
            val serializedNcSignalingMessage: String = try {
                LoganSquare.serialize(ncSignalingMessage)
            } catch (e: IOException) {
                Log.e(TAG, "Failed to serialize signaling message", e)
                return
            }

            // The message wrapper can not be defined in a JSON model to be directly serialized, as sent messages
            // need to be serialized twice; first the signaling message, and then the wrapper as a whole. Received
            // messages, on the other hand, just need to be deserialized once.
            val stringBuilder = StringBuilder()
            stringBuilder.append('{')
                .append("\"fn\":\"")
                .append(StringEscapeUtils.escapeJson(serializedNcSignalingMessage))
                .append('\"')
                .append(',')
                .append("\"sessionId\":")
                .append('\"').append(StringEscapeUtils.escapeJson(callSession)).append('\"')
                .append(',')
                .append("\"ev\":\"message\"")
                .append('}')
            val strings: MutableList<String> = ArrayList()
            val stringToSend = stringBuilder.toString()
            strings.add(stringToSend)
            val apiVersion = GccApiUtils.getSignalingApiVersion(conversationUser, intArrayOf(GccApiUtils.API_V3, 2, 1))
            ncApi!!.sendSignalingMessages(
                credentials,
                GccApiUtils.getUrlForSignaling(apiVersion, baseUrl, roomToken!!),
                strings.toString()
            )
                .retry(API_RETRIES)
                .subscribeOn(Schedulers.io())
                .subscribe(object : Observer<SignalingOverall> {
                    override fun onSubscribe(d: Disposable) {
                        // unused atm
                    }

                    override fun onNext(signalingOverall: SignalingOverall) {
                        // When sending messages to the internal signaling server the response has been empty since
                        // Talk v2.9.0, so it is not really needed to process it, but there is no harm either in
                        // doing that, as technically messages could be returned.
                        receivedSignalingMessages(signalingOverall.ocs!!.signalings)
                    }

                    override fun onError(e: Throwable) {
                        Log.e(TAG, "Failed to send signaling message", e)
                    }

                    override fun onComplete() {
                        // unused atm
                    }
                })
        }

        /**
         * Adds the local participant nick to offers and answers.
         *
         *
         * For legacy reasons the offers and answers sent when the internal signaling server is used are expected to
         * provide the nick of the local participant.
         *
         * @param ncSignalingMessage the message to add the nick to
         */
        private fun addLocalParticipantNickIfNeeded(ncSignalingMessage: NCSignalingMessage) {
            val type = ncSignalingMessage.type
            if ("offer" != type && "answer" != type) {
                return
            }
            val payload = ncSignalingMessage.payload
                ?: // Broken message, this should not happen
                return
            payload.nick = conversationUser!!.displayName
        }
    }

    private inner class MicrophoneButtonTouchListener : OnTouchListener {
        @SuppressLint("ClickableViewAccessibility")
        override fun onTouch(v: View, event: MotionEvent): Boolean {
            v.onTouchEvent(event)
            if (event.action == MotionEvent.ACTION_UP && isPushToTalkActive) {
                isPushToTalkActive = false
                binding!!.microphoneButton.setImageResource(R.drawable.ic_mic_off_white_24px)
                pulseAnimation!!.stop()
                toggleMedia(false, false)
            }
            return true
        }
    }

    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    fun onMessageEvent(networkEvent: GccNetworkEvent) {
        if (networkEvent.networkConnectionEvent == GccNetworkEvent.NetworkConnectionEvent.NETWORK_CONNECTED) {
            if (handler != null) {
                handler!!.removeCallbacksAndMessages(null)
            }
        } else if (networkEvent.networkConnectionEvent ==
            GccNetworkEvent.NetworkConnectionEvent.NETWORK_DISCONNECTED
        ) {
            if (handler != null) {
                handler!!.removeCallbacksAndMessages(null)
            }
        }
    }

    override fun onPictureInPictureModeChanged(isInPictureInPictureMode: Boolean, newConfig: Configuration) {
        super.onPictureInPictureModeChanged(isInPictureInPictureMode, newConfig)
        Log.d(TAG, "onPictureInPictureModeChanged")
        Log.d(TAG, "isInPictureInPictureMode= $isInPictureInPictureMode")
        isInPipMode = isInPictureInPictureMode
        if (isInPictureInPictureMode) {
            mReceiver = object : BroadcastReceiver() {
                override fun onReceive(context: Context, intent: Intent) {
                    if (MICROPHONE_PIP_INTENT_NAME != intent.action) {
                        return
                    }
                    when (intent.getIntExtra(MICROPHONE_PIP_INTENT_EXTRA_ACTION, 0)) {
                        MICROPHONE_PIP_REQUEST_MUTE, MICROPHONE_PIP_REQUEST_UNMUTE -> onMicrophoneClick()
                    }
                }
            }
            registerPermissionHandlerBroadcastReceiver(
                mReceiver,
                IntentFilter(MICROPHONE_PIP_INTENT_NAME),
                permissionUtil!!.privateBroadcastPermission,
                null,
                GccReceiverFlag.NotExported
            )
            updateUiForPipMode()
        } else {
            unregisterReceiver(mReceiver)
            mReceiver = null
            updateUiForNormalMode()
        }
    }

    private fun updatePictureInPictureActions(@DrawableRes iconId: Int, title: String?, requestCode: Int) {
        if (isPipModePossible) {
            val actions = ArrayList<RemoteAction>()
            val icon = Icon.createWithResource(this, iconId)
            val intentFlag: Int = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                PendingIntent.FLAG_IMMUTABLE
            } else {
                0
            }
            val intent = PendingIntent.getBroadcast(
                this,
                requestCode,
                Intent(MICROPHONE_PIP_INTENT_NAME).putExtra(MICROPHONE_PIP_INTENT_EXTRA_ACTION, requestCode),
                intentFlag
            )
            actions.add(RemoteAction(icon, title!!, title, intent))
            mPictureInPictureParamsBuilder.setActions(actions)
            setPictureInPictureParams(mPictureInPictureParamsBuilder.build())
        }
    }

    override fun updateUiForPipMode() {
        Log.d(TAG, "updateUiForPipMode")
        binding!!.callControls.visibility = View.GONE
        binding!!.selfVideoViewWrapper.visibility = View.GONE
        binding!!.callStates.callStateRelativeLayout.visibility = View.GONE
        binding!!.pipCallConversationNameTextView.text = conversationName

        if (callViewModel.participants.value.size == 1) {
            binding!!.pipOverlay.visibility = View.GONE
        } else {
            binding!!.composeParticipantGrid.visibility = View.GONE

            if (localVideoTrack?.enabled() == true) {
                binding!!.pipOverlay.visibility = View.VISIBLE
                binding!!.pipSelfVideoRenderer.visibility = View.VISIBLE

                try {
                    binding!!.pipSelfVideoRenderer.init(rootEglBase!!.eglBaseContext, null)
                } catch (e: IllegalStateException) {
                    Log.d(TAG, "pipGroupVideoRenderer already initialized", e)
                }
                binding!!.pipSelfVideoRenderer.setZOrderMediaOverlay(true)
                // disabled because it causes some devices to crash
                binding!!.pipSelfVideoRenderer.setEnableHardwareScaler(false)
                binding!!.pipSelfVideoRenderer.setScalingType(RendererCommon.ScalingType.SCALE_ASPECT_FIT)

                localVideoTrack?.addSink(binding?.pipSelfVideoRenderer)
            } else {
                binding!!.pipOverlay.visibility = View.VISIBLE
                binding!!.pipSelfVideoRenderer.visibility = View.GONE
            }
        }
    }

    override fun updateUiForNormalMode() {
        Log.d(TAG, "updateUiForNormalMode")
        binding!!.pipOverlay.visibility = View.GONE
        binding!!.composeParticipantGrid.visibility = View.VISIBLE

        binding!!.callControls.visibility = View.VISIBLE
        initViews()
        binding!!.selfVideoViewWrapper.visibility = View.VISIBLE
    }

    override fun suppressFitsSystemWindows() {
        binding!!.callLayout.fitsSystemWindows = false
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        eventBus.post(GccConfigurationChangeEvent())
    }

    val isAllowedToStartOrStopRecording: Boolean
        get() = (
            isCallRecordingAvailable(conversationUser!!.capabilities!!.spreedCapability!!) &&
                isModerator
            )
    val isAllowedToRaiseHand: Boolean
        get() = hasSpreedFeatureCapability(
            conversationUser.capabilities!!.spreedCapability!!,
            SpreedFeatures.RAISE_HAND
        ) ||
            isBreakoutRoom

    companion object {
        var active = false

        const val VIDEO_STREAM_TYPE_VIDEO = "video"
        private val TAG = GccCallActivity::class.java.simpleName
        private val PERMISSIONS_CAMERA = arrayOf(
            Manifest.permission.CAMERA
        )
        private val PERMISSIONS_MICROPHONE = arrayOf(
            Manifest.permission.RECORD_AUDIO
        )
        private const val MICROPHONE_PIP_INTENT_NAME = "microphone_pip_intent"
        private const val MICROPHONE_PIP_INTENT_EXTRA_ACTION = "microphone_pip_action"
        private const val MICROPHONE_PIP_REQUEST_MUTE = 1
        private const val MICROPHONE_PIP_REQUEST_UNMUTE = 2

        const val OPACITY_ENABLED = 1.0f
        const val OPACITY_DISABLED = 0.7f
        const val OPACITY_INVISIBLE = 0.0f

        const val SECOND_IN_MILLIS: Long = 1000
        const val CALL_TIME_COUNTER_DELAY: Long = 1000
        const val CALL_TIME_ONE_HOUR = 3600
        const val CALL_DURATION_EMPTY = "--:--"
        const val API_RETRIES: Long = 3

        private const val SAMPLE_RATE = 8000
        private const val MICROPHONE_VALUE_THRESHOLD = 20
        private const val MICROPHONE_VALUE_SLEEP: Long = 1000

        private const val FRAME_RATE: Int = 30
        private const val WIDTH_16_TO_9_RATIO: Int = 1280
        private const val HEIGHT_16_TO_9_RATIO: Int = 720
        private const val WIDTH_4_TO_3_RATIO: Int = 640
        private const val HEIGHT_4_TO_3_RATIO: Int = 480

        private const val RATIO_4_TO_3 = "RATIO_4_TO_3"
        private const val RATIO_16_TO_9 = "RATIO_16_TO_9"
        private const val ANGLE_0 = 0
        private const val ANGLE_FULL = 360
        private const val ANGLE_PORTRAIT_RIGHT_THRESHOLD = 30
        private const val ANGLE_PORTRAIT_LEFT_THRESHOLD = 330
        private const val ANGLE_LANDSCAPE_RIGHT_THRESHOLD_MIN = 80
        private const val ANGLE_LANDSCAPE_RIGHT_THRESHOLD_MAX = 100
        private const val ANGLE_LANDSCAPE_LEFT_THRESHOLD_MIN = 260
        private const val ANGLE_LANDSCAPE_LEFT_THRESHOLD_MAX = 280

        private const val CALLING_TIMEOUT: Long = 45000
        private const val INTRO_ANIMATION_DURATION: Long = 300
        private const val FADE_IN_ANIMATION_DURATION: Long = 400
        private const val PULSE_ANIMATION_DURATION: Int = 310

        private const val SPOTLIGHT_HEADING_SIZE: Int = 20
        private const val SPOTLIGHT_SUBHEADING_SIZE: Int = 16

        private const val DELAY_ON_ERROR_STOP_THRESHOLD: Int = 16

        private const val SESSION_ID_PREFFIX_END: Int = 4
    }
}
