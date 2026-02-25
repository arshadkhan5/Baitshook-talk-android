/*
 * Nextcloud Talk - Android Client
 *
 * SPDX-FileCopyrightText: 2025 Julius Linus <juliuslinus1@gmail.com>
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.gcc.talk.gccUi

import android.content.Context
import android.util.Log
import android.view.View.TEXT_ALIGNMENT_VIEW_START
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.LinearLayout
import androidx.compose.animation.animateColor
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.graphics.ColorUtils
import androidx.emoji2.widget.EmojiTextView
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asFlow
import autodagger.AutoInjector
import coil.compose.AsyncImage
import com.elyeproj.loaderviewlibrary.LoaderImageView
import com.elyeproj.loaderviewlibrary.LoaderTextView
import com.gcc.talk.R
import com.gcc.talk.gccAdapters.messages.GccPreviewMessageViewHolder.Companion.KEY_MIMETYPE
import com.gcc.talk.gccApplication.GccTalkApplication
import com.gcc.talk.gccApplication.GccTalkApplication.Companion.sharedApplication
import com.gcc.talk.gccChat.data.model.GccChatMessage
import com.gcc.talk.gccChat.viewmodels.GccChatViewModel
import com.gcc.talk.gccContacts.GccContactsViewModel
import com.gcc.talk.gccContacts.load
import com.gcc.talk.gccContacts.loadImage
import com.gcc.talk.gccData.database.mappers.asModel
import com.gcc.talk.gccData.user.model.GccUser
import com.gcc.talk.gccModels.json.chat.ChatMessageJson
import com.gcc.talk.gccModels.json.chat.ReadStatus
import com.gcc.talk.gccModels.json.opengraph.Reference
import com.gcc.talk.gccUi.theme.ViewThemeUtils
import com.gcc.talk.gccUsers.GccUserManager
import com.gcc.talk.gccUtilss.GccDateUtils
import com.gcc.talk.gccUtils.GccDisplayUtils
import com.gcc.talk.gccUtils.GccDrawableUtils.getDrawableResourceIdForMimeType
import com.gcc.talk.gccUtils.message.GccMessageUtils
import com.gcc.talk.gccUtils.preview.GccComposePreviewUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import kotlin.random.Random

@Suppress("FunctionNaming", "TooManyFunctions", "LongMethod", "StaticFieldLeak", "LargeClass")
class ComposeChatAdapter(
    private var messagesJson: List<ChatMessageJson>? = null,
    private var messageId: String? = null,
    private var threadId: String? = null,
    private val utils: GccComposePreviewUtils? = null
) {

    interface PreviewAble {
        val viewThemeUtils: ViewThemeUtils
        val messageUtils: GccMessageUtils
        val contactsViewModel: GccContactsViewModel
        val chatViewModel: GccChatViewModel
        val context: Context
        val userManager: GccUserManager
    }

    @AutoInjector(GccTalkApplication::class)
    inner class ComposeChatAdapterViewModel :
        ViewModel(),
        PreviewAble {

        @Inject
        override lateinit var viewThemeUtils: ViewThemeUtils

        @Inject
        override lateinit var messageUtils: GccMessageUtils

        @Inject
        override lateinit var contactsViewModel: GccContactsViewModel

        @Inject
        override lateinit var chatViewModel: GccChatViewModel

        @Inject
        override lateinit var context: Context

        @Inject
        override lateinit var userManager: GccUserManager

        init {
            sharedApplication?.componentApplication?.inject(this)
        }
    }

    class ComposeChatAdapterPreviewViewModel(
        override val viewThemeUtils: ViewThemeUtils,
        override val messageUtils: GccMessageUtils,
        override val contactsViewModel: GccContactsViewModel,
        override val chatViewModel: GccChatViewModel,
        override val context: Context,
        override val userManager: GccUserManager
    ) : ViewModel(),
        PreviewAble

    companion object {
        val TAG: String = ComposeChatAdapter::class.java.simpleName
        private val REGULAR_TEXT_SIZE = 16.sp
        private val TIME_TEXT_SIZE = 12.sp
        private val AUTHOR_TEXT_SIZE = 12.sp
        private const val LONG_1000 = 1000
        private const val SCROLL_DELAY = 20L
        private const val QUOTE_SHAPE_OFFSET = 6
        private const val LINE_SPACING = 1.2f
        private const val CAPTION_WEIGHT = 0.8f
        private const val DEFAULT_WAVE_SIZE = 50
        private const val MAP_ZOOM = 15.0
        private const val INT_8 = 8
        private const val INT_128 = 128
        private const val ANIMATION_DURATION = 2500L
        private const val ANIMATED_BLINK = 500
        private const val FLOAT_06 = 0.6f
        private const val HALF_OPACITY = 127
        private const val MESSAGE_LENGTH_THRESHOLD = 25
    }

    private var incomingShape: RoundedCornerShape = RoundedCornerShape(2.dp, 20.dp, 20.dp, 20.dp)
    private var outgoingShape: RoundedCornerShape = RoundedCornerShape(20.dp, 2.dp, 20.dp, 20.dp)

    val viewModel: PreviewAble =
        if (utils != null) {
            ComposeChatAdapterPreviewViewModel(
                utils.viewThemeUtils,
                utils.messageUtils,
                utils.contactsViewModel,
                utils.chatViewModel,
                utils.context,
                utils.userManager
            )
        } else {
            ComposeChatAdapterViewModel()
        }

    val items = mutableStateListOf<GccChatMessage>()
    val currentUser: GccUser = viewModel.userManager.currentUser.blockingGet()
    val colorScheme = viewModel.viewThemeUtils.getColorScheme(viewModel.context)
    val highEmphasisColorInt = if (GccDisplayUtils.isAppThemeDarkMode(viewModel.context)) {
        Color.White.toArgb()
    } else {
        Color.Black.toArgb()
    }
    val highEmphasisColor = Color(highEmphasisColorInt)

    fun addMessages(messages: MutableList<GccChatMessage>, append: Boolean) {
        if (messages.isEmpty()) return

        val processedMessages = messages.toMutableList()
        if (items.isNotEmpty()) {
            if (append) {
                processedMessages.add(items.first())
            } else {
                processedMessages.add(items.last())
            }
        }

        if (append) items.addAll(processedMessages) else items.addAll(0, processedMessages)
    }

    @Composable
    fun GetComposableForMessage(message: GccChatMessage, isBlinkingState: MutableState<Boolean> = mutableStateOf(false)) {
        message.activeUser = currentUser
        when (val type = message.getCalculateMessageType()) {
            GccChatMessage.MessageType.SYSTEM_MESSAGE -> {
                if (!message.shouldFilter()) {
                    SystemMessage(message)
                }
            }

            GccChatMessage.MessageType.VOICE_MESSAGE -> {
                VoiceMessage(message, isBlinkingState)
            }

            GccChatMessage.MessageType.SINGLE_NC_ATTACHMENT_MESSAGE -> {
                ImageMessage(message, isBlinkingState)
            }

            GccChatMessage.MessageType.SINGLE_NC_GEOLOCATION_MESSAGE -> {
                GeolocationMessage(message, isBlinkingState)
            }

            GccChatMessage.MessageType.POLL_MESSAGE -> {
                PollMessage(message, isBlinkingState)
            }

            GccChatMessage.MessageType.DECK_CARD -> {
                DeckMessage(message, isBlinkingState)
            }

            GccChatMessage.MessageType.REGULAR_TEXT_MESSAGE -> {
                if (message.isLinkPreview()) {
                    LinkMessage(message, isBlinkingState)
                } else {
                    TextMessage(message, isBlinkingState)
                }
            }

            else -> {
                Log.d(TAG, "Unknown message type: $type")
            }
        }
    }

    @OptIn(ExperimentalFoundationApi::class)
    @Composable
    fun GetView() {
        val listState = rememberLazyListState()
        val isBlinkingState = remember { mutableStateOf(true) }

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            state = listState,
            modifier = Modifier.padding(16.dp)
        ) {
            stickyHeader {
                if (items.size == 0) {
                    Column(
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        ShimmerGroup()
                    }
                } else {
                    val timestamp = items[listState.firstVisibleItemIndex].timestamp
                    val dateString = formatTime(timestamp * LONG_1000)
                    val color = highEmphasisColor
                    val backgroundColor =
                        LocalResources.current.getColor(R.color.bg_message_list_incoming_bubble, null)
                    Row(
                        horizontalArrangement = Arrangement.Absolute.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Spacer(modifier = Modifier.weight(1f))
                        Text(
                            dateString,
                            fontSize = AUTHOR_TEXT_SIZE,
                            color = color,
                            modifier = Modifier
                                .padding(8.dp)
                                .shadow(
                                    16.dp,
                                    spotColor = colorScheme.primary,
                                    ambientColor = colorScheme.primary
                                )
                                .background(color = Color(backgroundColor), shape = RoundedCornerShape(8.dp))
                                .padding(8.dp)
                        )
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }

            items(items) { message ->
                message.incoming = message.actorId != currentUser.userId
                GetComposableForMessage(message, isBlinkingState)
            }
        }

        if (messageId != null && items.size > 0) {
            LaunchedEffect(Dispatchers.Main) {
                delay(SCROLL_DELAY)
                val pos = searchMessages(messageId!!)
                if (pos > 0) {
                    listState.scrollToItem(pos)
                }
                delay(ANIMATION_DURATION)
                isBlinkingState.value = false
            }
        }
    }

    private fun GccChatMessage.shouldFilter(): Boolean =
        this.isReaction() ||
            this.isPollVotedMessage() ||
            this.isEditMessage() ||
            this.isInfoMessageAboutDeletion() ||
            this.isThreadCreatedMessage()

    private fun GccChatMessage.isInfoMessageAboutDeletion(): Boolean =
        this.parentMessageId != null &&
            this.systemMessageType == GccChatMessage.SystemMessageType.MESSAGE_DELETED

    private fun GccChatMessage.isPollVotedMessage(): Boolean =
        this.systemMessageType == GccChatMessage.SystemMessageType.POLL_VOTED

    private fun GccChatMessage.isEditMessage(): Boolean =
        this.systemMessageType == GccChatMessage.SystemMessageType.MESSAGE_EDITED

    private fun GccChatMessage.isThreadCreatedMessage(): Boolean =
        this.systemMessageType == GccChatMessage.SystemMessageType.THREAD_CREATED

    private fun GccChatMessage.isReaction(): Boolean =
        systemMessageType == GccChatMessage.SystemMessageType.REACTION ||
            systemMessageType == GccChatMessage.SystemMessageType.REACTION_DELETED ||
            systemMessageType == GccChatMessage.SystemMessageType.REACTION_REVOKED

    private fun formatTime(timestampMillis: Long): String {
        val instant = Instant.ofEpochMilli(timestampMillis)
        val dateTime = instant.atZone(ZoneId.systemDefault()).toLocalDate()
        val formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy")
        return dateTime.format(formatter)
    }

    private fun searchMessages(searchId: String): Int {
        items.forEachIndexed { index, message ->
            if (message.id == searchId) return index
        }
        return -1
    }

    @Composable
    private fun CommonMessageQuote(context: Context, message: GccChatMessage) {
        val color = colorResource(R.color.high_emphasis_text)
        Row(
            modifier = Modifier
                .drawWithCache {
                    onDrawWithContent {
                        drawLine(
                            color = color,
                            start = Offset(0f, this.size.height / QUOTE_SHAPE_OFFSET),
                            end = Offset(0f, this.size.height - (this.size.height / QUOTE_SHAPE_OFFSET)),
                            strokeWidth = 4f,
                            cap = StrokeCap.Round
                        )

                        drawContent()
                    }
                }
                .padding(8.dp)
        ) {
            Column {
                Text(message.actorDisplayName!!, fontSize = AUTHOR_TEXT_SIZE)
                val imageUri = message.imageUrl
                if (imageUri != null) {
                    val errorPlaceholderImage: Int = R.drawable.ic_mimetype_image
                    val loadedImage = loadImage(imageUri, context, errorPlaceholderImage)
                    AsyncImage(
                        model = loadedImage,
                        contentDescription = stringResource(R.string.nc_sent_an_image),
                        modifier = Modifier
                            .padding(8.dp)
                            .fillMaxHeight()
                    )
                }
                EnrichedText(message)
            }
        }
    }

    @Composable
    private fun CommonMessageBody(
        message: GccChatMessage,
        includePadding: Boolean = true,
        playAnimation: Boolean = false,
        content: @Composable () -> Unit
    ) {
        fun shouldShowTimeNextToContent(message: GccChatMessage): Boolean {
            val containsLinebreak = message.message?.contains("\n") ?: false ||
                message.message?.contains("\r") ?: false

            return ((message.message?.length ?: 0) < MESSAGE_LENGTH_THRESHOLD) &&
                !isFirstMessageOfThreadInNormalChat(message) &&
                message.messageParameters.isNullOrEmpty() &&
                !containsLinebreak
        }

        val incoming = message.incoming
        val color = if (incoming) {
            if (message.isDeleted) {
                getColorFromTheme(LocalContext.current, R.color.bg_message_list_incoming_bubble_deleted)
            } else {
                getColorFromTheme(LocalContext.current, R.color.bg_message_list_incoming_bubble)
            }
        } else {
            val outgoingBubbleColor = viewModel.viewThemeUtils.talk
                .getOutgoingMessageBubbleColor(LocalContext.current, message.isDeleted, false)

            if (message.isDeleted) {
                ColorUtils.setAlphaComponent(outgoingBubbleColor, HALF_OPACITY)
            } else {
                outgoingBubbleColor
            }
        }

        val shape = if (incoming) incomingShape else outgoingShape

        val rowModifier = if (message.id == messageId && playAnimation) {
            Modifier.withCustomAnimation(incoming)
        } else {
            Modifier
        }

        Row(
            modifier = rowModifier.fillMaxWidth(),
            horizontalArrangement = if (incoming) Arrangement.Start else Arrangement.End
        ) {
            if (incoming) {
                val imageUri = message.actorId?.let { viewModel.contactsViewModel.getImageUri(it, true) }
                val errorPlaceholderImage: Int = R.drawable.account_circle_96dp
                val loadedImage = loadImage(imageUri, LocalContext.current, errorPlaceholderImage)
                AsyncImage(
                    model = loadedImage,
                    contentDescription = stringResource(R.string.user_avatar),
                    modifier = Modifier
                        .size(48.dp)
                        .align(Alignment.CenterVertically)
                        .padding(end = 8.dp)
                )
            } else {
                Spacer(Modifier.width(8.dp))
            }

            Surface(
                modifier = Modifier
                    .defaultMinSize(60.dp, 40.dp)
                    .widthIn(60.dp, 280.dp)
                    .heightIn(40.dp, 450.dp),
                color = Color(color),
                shape = shape
            ) {
                val modifier = if (includePadding) {
                    Modifier.padding(16.dp, 4.dp, 16.dp, 4.dp)
                } else {
                    Modifier
                }

                Column(modifier = modifier) {
                    if (messagesJson != null &&
                        message.parentMessageId != null &&
                        !message.isDeleted &&
                        message.parentMessageId.toString() != threadId
                    ) {
                        messagesJson!!
                            .find { it.parentMessage?.id == message.parentMessageId }
                            ?.parentMessage!!.asModel()
                            .let { CommonMessageQuote(LocalContext.current, it) }
                    }

                    if (incoming) {
                        Text(
                            message.actorDisplayName.toString(),
                            fontSize = AUTHOR_TEXT_SIZE,
                            color = highEmphasisColor
                        )
                    }

                    ThreadTitle(message)

                    if (shouldShowTimeNextToContent(message)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            content()
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(top = 6.dp, start = 8.dp)
                            ) {
                                TimeDisplay(message)
                                ReadStatus(message)
                            }
                        }
                    } else {
                        content()
                        Row(
                            modifier = Modifier.align(Alignment.End),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            TimeDisplay(message)
                            ReadStatus(message)
                        }
                    }
                }
            }
        }
    }

    private fun getColorFromTheme(context: Context, resourceId: Int): Int {
        val isDarkMode = GccDisplayUtils.isAppThemeDarkMode(context)
        val nightConfig = android.content.res.Configuration()
        nightConfig.uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES
        val nightContext = context.createConfigurationContext(nightConfig)

        return if (isDarkMode) {
            nightContext.getColor(resourceId)
        } else {
            context.getColor(resourceId)
        }
    }

    @Composable
    private fun TimeDisplay(message: GccChatMessage) {
        val timeString = GccDateUtils(LocalContext.current)
            .getLocalTimeStringFromTimestamp(message.timestamp)
        Text(
            timeString,
            fontSize = TIME_TEXT_SIZE,
            textAlign = TextAlign.Center,
            color = highEmphasisColor
        )
    }

    @Composable
    private fun ReadStatus(message: GccChatMessage) {
        if (message.readStatus == ReadStatus.NONE) {
            val read = painterResource(R.drawable.ic_check_all)
            Icon(
                read,
                "",
                modifier = Modifier
                    .padding(start = 4.dp)
                    .size(16.dp),
                tint = highEmphasisColor
            )
        }
    }

    @Composable
    private fun ThreadTitle(message: GccChatMessage) {
        if (isFirstMessageOfThreadInNormalChat(message)) {
            Row {
                val read = painterResource(R.drawable.outline_forum_24)
                Icon(
                    read,
                    "",
                    modifier = Modifier
                        .padding(end = 6.dp)
                        .size(18.dp)
                        .align(Alignment.CenterVertically)
                )
                Text(
                    text = message.threadTitle ?: "",
                    fontSize = REGULAR_TEXT_SIZE,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }

    fun isFirstMessageOfThreadInNormalChat(message: GccChatMessage): Boolean = threadId == null && message.isThread

    @Composable
    private fun Modifier.withCustomAnimation(incoming: Boolean): Modifier {
        val infiniteTransition = rememberInfiniteTransition()
        val borderColor by infiniteTransition.animateColor(
            initialValue = colorScheme.primary,
            targetValue = colorScheme.background,
            animationSpec = infiniteRepeatable(
                animation = tween(ANIMATED_BLINK, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            )
        )

        return this.border(
            width = 4.dp,
            color = borderColor,
            shape = if (incoming) incomingShape else outgoingShape
        )
    }

    @Composable
    private fun ShimmerGroup() {
        Shimmer()
        Shimmer(true)
        Shimmer()
        Shimmer(true)
        Shimmer(true)
        Shimmer()
        Shimmer(true)
    }

    @Composable
    private fun Shimmer(outgoing: Boolean = false) {
        Row(modifier = Modifier.padding(top = 16.dp)) {
            if (!outgoing) {
                ShimmerImage(this)
            }

            val v1 by remember { mutableIntStateOf((INT_8..INT_128).random()) }
            val v2 by remember { mutableIntStateOf((INT_8..INT_128).random()) }
            val v3 by remember { mutableIntStateOf((INT_8..INT_128).random()) }

            Column {
                ShimmerText(this, v1, outgoing)
                ShimmerText(this, v2, outgoing)
                ShimmerText(this, v3, outgoing)
            }
        }
    }

    @Composable
    private fun ShimmerImage(rowScope: RowScope) {
        rowScope.apply {
            AndroidView(
                factory = { ctx ->
                    LoaderImageView(ctx).apply {
                        layoutParams = LinearLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT)
                        val color = resources.getColor(R.color.nc_shimmer_default_color, null)
                        setBackgroundColor(color)
                    }
                },
                modifier = Modifier
                    .clip(CircleShape)
                    .size(40.dp)
                    .align(Alignment.Top)
            )
        }
    }

    @Composable
    private fun ShimmerText(columnScope: ColumnScope, margin: Int, outgoing: Boolean = false) {
        columnScope.apply {
            AndroidView(
                factory = { ctx ->
                    LoaderTextView(ctx).apply {
                        layoutParams = LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT)
                        val color = if (outgoing) {
                            colorScheme.primary.toArgb()
                        } else {
                            resources.getColor(R.color.nc_shimmer_default_color, null)
                        }

                        setBackgroundColor(color)
                    }
                },
                modifier = Modifier.padding(
                    top = 6.dp,
                    end = if (!outgoing) margin.dp else 8.dp,
                    start = if (outgoing) margin.dp else 8.dp
                )
            )
        }
    }

    @Composable
    private fun EnrichedText(message: GccChatMessage) {
        AndroidView(factory = { ctx ->
            val incoming = message.actorId != currentUser.userId
            var processedMessageText = viewModel.messageUtils.enrichChatMessageText(
                ctx,
                message,
                incoming,
                viewModel.viewThemeUtils
            )

            processedMessageText = viewModel.messageUtils.processMessageParameters(
                ctx,
                viewModel.viewThemeUtils,
                processedMessageText!!,
                message,
                null
            )

            EmojiTextView(ctx).apply {
                layoutParams = LinearLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT)
                setLineSpacing(0F, LINE_SPACING)
                textAlignment = TEXT_ALIGNMENT_VIEW_START
                text = processedMessageText
                setPadding(0, INT_8, 0, 0)
            }
        }, modifier = Modifier)
    }

    @Composable
    private fun TextMessage(message: GccChatMessage, state: MutableState<Boolean>) {
        CommonMessageBody(message, playAnimation = state.value) {
            EnrichedText(message)
        }
    }

    @Composable
    fun SystemMessage(message: GccChatMessage) {
        val similarMessages = sharedApplication!!.resources.getQuantityString(
            R.plurals.see_similar_system_messages,
            message.expandableChildrenAmount,
            message.expandableChildrenAmount
        )
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            val timeString = GccDateUtils(LocalContext.current).getLocalTimeStringFromTimestamp(message.timestamp)
            Row(horizontalArrangement = Arrangement.Absolute.Center, verticalAlignment = Alignment.CenterVertically) {
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    message.text,
                    fontSize = AUTHOR_TEXT_SIZE,
                    modifier = Modifier
                        .padding(8.dp)
                        .fillMaxWidth(FLOAT_06)
                )
                Text(
                    timeString,
                    fontSize = TIME_TEXT_SIZE,
                    textAlign = TextAlign.End,
                    modifier = Modifier.align(Alignment.CenterVertically)
                )
                Spacer(modifier = Modifier.weight(1f))
            }

            if (message.expandableChildrenAmount > 0) {
                TextButtonNoStyling(similarMessages) {
                    // NOTE: Read only for now
                }
            }
        }
    }

    @Composable
    private fun TextButtonNoStyling(text: String, onClick: () -> Unit) {
        TextButton(onClick = onClick) {
            Text(
                text,
                fontSize = AUTHOR_TEXT_SIZE,
                color = highEmphasisColor
            )
        }
    }

    @Composable
    private fun ImageMessage(message: GccChatMessage, state: MutableState<Boolean>) {
        val hasCaption = (message.message != "{file}")
        val incoming = message.actorId != currentUser.userId
        val timeString = GccDateUtils(LocalContext.current).getLocalTimeStringFromTimestamp(message.timestamp)
        CommonMessageBody(message, includePadding = false, playAnimation = state.value) {
            Column {
                message.activeUser = currentUser
                val imageUri = message.imageUrl
                val mimetype = message.selectedIndividualHashMap!![KEY_MIMETYPE]
                val drawableResourceId = getDrawableResourceIdForMimeType(mimetype)
                val loadedImage = load(imageUri, LocalContext.current, drawableResourceId)

                AsyncImage(
                    model = loadedImage,
                    contentDescription = stringResource(R.string.nc_sent_an_image),
                    modifier = Modifier
                        .fillMaxWidth(),
                    contentScale = ContentScale.FillWidth
                )

                if (hasCaption) {
                    Text(
                        message.text,
                        fontSize = 12.sp,
                        modifier = Modifier
                            .widthIn(20.dp, 140.dp)
                            .padding(8.dp)
                    )
                }
            }
        }

        if (!hasCaption) {
            Row(modifier = Modifier.padding(top = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                if (!incoming) {
                    Spacer(Modifier.weight(1f))
                } else {
                    Spacer(Modifier.size(width = 56.dp, 0.dp)) // To account for avatar size
                }
                Text(message.text, fontSize = 12.sp)
                Text(
                    timeString,
                    fontSize = TIME_TEXT_SIZE,
                    textAlign = TextAlign.End,
                    modifier = Modifier
                        .align(Alignment.CenterVertically)
                        .padding()
                        .padding(start = 4.dp)
                )
                if (message.readStatus == ReadStatus.NONE) {
                    val read = painterResource(R.drawable.ic_check_all)
                    Icon(
                        read,
                        "",
                        modifier = Modifier
                            .padding(start = 4.dp)
                            .size(16.dp)
                            .align(Alignment.CenterVertically)
                    )
                }
            }
        }
    }

    @Composable
    private fun VoiceMessage(message: GccChatMessage, state: MutableState<Boolean>) {
        CommonMessageBody(message, playAnimation = state.value) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Filled.PlayArrow,
                    contentDescription = "play",
                    modifier = Modifier.size(24.dp)
                )

                AndroidView(
                    factory = { ctx ->
                        WaveformSeekBar(ctx).apply {
                            setWaveData(FloatArray(DEFAULT_WAVE_SIZE) { Random.nextFloat() }) // READ ONLY for now
                            setColors(
                                colorScheme.inversePrimary.toArgb(),
                                colorScheme.onPrimaryContainer.toArgb()
                            )
                        }
                    },
                    modifier = Modifier
                        .width(180.dp)
                        .height(80.dp)
                )
            }
        }
    }

    @Composable
    private fun GeolocationMessage(message: GccChatMessage, state: MutableState<Boolean>) {
        CommonMessageBody(message, playAnimation = state.value) {
            Column {
                if (message.messageParameters != null && message.messageParameters!!.isNotEmpty()) {
                    for (key in message.messageParameters!!.keys) {
                        val individualHashMap: Map<String?, String?> = message.messageParameters!![key]!!
                        if (individualHashMap["type"] == "geo-location") {
                            val lat = individualHashMap["latitude"]
                            val lng = individualHashMap["longitude"]

                            if (lat != null && lng != null) {
                                val latitude = lat.toDouble()
                                val longitude = lng.toDouble()
                                OpenStreetMap(latitude, longitude)
                            }
                        }
                    }
                }
            }
        }
    }

    @Composable
    private fun OpenStreetMap(latitude: Double, longitude: Double) {
        AndroidView(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(),
            factory = { context ->
                Configuration.getInstance().userAgentValue = context.packageName
                MapView(context).apply {
                    setTileSource(TileSourceFactory.MAPNIK)
                    setMultiTouchControls(true)

                    val geoPoint = GeoPoint(latitude, longitude)
                    controller.setCenter(geoPoint)
                    controller.setZoom(MAP_ZOOM)

                    val marker = Marker(this)
                    marker.position = geoPoint
                    marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                    marker.title = "Location"
                    overlays.add(marker)

                    invalidate()
                }
            },
            update = { mapView ->
                val geoPoint = GeoPoint(latitude, longitude)
                mapView.controller.setCenter(geoPoint)

                val marker = mapView.overlays.find { it is Marker } as? Marker
                marker?.position = geoPoint
                mapView.invalidate()
            }
        )
    }

    @Composable
    private fun LinkMessage(message: GccChatMessage, state: MutableState<Boolean>) {
        val color = colorResource(R.color.high_emphasis_text)
        viewModel.chatViewModel.getOpenGraph(
            currentUser.getCredentials(),
            currentUser.baseUrl!!,
            message.extractedUrlToPreview!!
        )
        CommonMessageBody(message, playAnimation = state.value) {
            EnrichedText(message)
            Row(
                modifier = Modifier
                    .drawWithCache {
                        onDrawWithContent {
                            drawLine(
                                color = color,
                                start = Offset.Zero,
                                end = Offset(0f, this.size.height),
                                strokeWidth = 4f,
                                cap = StrokeCap.Round
                            )

                            drawContent()
                        }
                    }
                    .padding(8.dp)
                    .padding(4.dp)
            ) {
                Column {
                    val graphObject = viewModel.chatViewModel.getOpenGraph.asFlow().collectAsState(
                        Reference(
                            // Dummy class
                        )
                    ).value.openGraphObject
                    graphObject?.let {
                        Text(it.name, fontSize = REGULAR_TEXT_SIZE, fontWeight = FontWeight.Bold)
                        it.description?.let { Text(it, fontSize = AUTHOR_TEXT_SIZE) }
                        it.link?.let { Text(it, fontSize = TIME_TEXT_SIZE) }
                        it.thumb?.let {
                            val errorPlaceholderImage: Int = R.drawable.ic_mimetype_image
                            val loadedImage = load(it, LocalContext.current, errorPlaceholderImage)
                            AsyncImage(
                                model = loadedImage,
                                contentDescription = stringResource(R.string.nc_sent_an_image),
                                modifier = Modifier
                                    .height(120.dp)
                            )
                        }
                    }
                }
            }
        }
    }

    @Composable
    private fun PollMessage(message: GccChatMessage, state: MutableState<Boolean>) {
        CommonMessageBody(message, playAnimation = state.value) {
            Column {
                if (message.messageParameters != null && message.messageParameters!!.size > 0) {
                    for (key in message.messageParameters!!.keys) {
                        val individualHashMap: Map<String?, String?> = message.messageParameters!![key]!!
                        if (individualHashMap["type"] == "talk-poll") {
                            // val pollId = individualHashMap["id"]
                            val pollName = individualHashMap["name"].toString()
                            Row(modifier = Modifier.padding(start = 8.dp)) {
                                Icon(painterResource(R.drawable.ic_baseline_bar_chart_24), "")
                                Text(pollName, fontSize = AUTHOR_TEXT_SIZE, fontWeight = FontWeight.Bold)
                            }

                            TextButtonNoStyling(stringResource(R.string.message_poll_tap_to_open)) {
                                // NOTE: read only for now
                            }
                        }
                    }
                }
            }
        }
    }

    @Composable
    private fun DeckMessage(message: GccChatMessage, state: MutableState<Boolean>) {
        CommonMessageBody(message, playAnimation = state.value) {
            Column {
                if (message.messageParameters != null && message.messageParameters!!.size > 0) {
                    for (key in message.messageParameters!!.keys) {
                        val individualHashMap: Map<String?, String?> = message.messageParameters!![key]!!
                        if (individualHashMap["type"] == "deck-card") {
                            val cardName = individualHashMap["name"]
                            val stackName = individualHashMap["stackname"]
                            val boardName = individualHashMap["boardname"]
                            // val cardLink = individualHashMap["link"]

                            if (cardName?.isNotEmpty() == true) {
                                val cardDescription = String.format(
                                    LocalContext.current.resources.getString(R.string.deck_card_description),
                                    stackName,
                                    boardName
                                )
                                Row(modifier = Modifier.padding(start = 8.dp)) {
                                    Icon(painterResource(R.drawable.deck), "")
                                    Text(cardName, fontSize = AUTHOR_TEXT_SIZE, fontWeight = FontWeight.Bold)
                                }
                                Text(cardDescription, fontSize = AUTHOR_TEXT_SIZE)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 380, heightDp = 800)
@Composable
@Suppress("MagicNumber", "LongMethod")
fun AllMessageTypesPreview() {
    val previewUtils = GccComposePreviewUtils.getInstance(LocalContext.current)
    val adapter = remember {
        ComposeChatAdapter(
            messagesJson = null,
            messageId = null,
            threadId = null,
            previewUtils
        )
    }

    val sampleMessages = remember {
        listOf(
            // Text Messages
            GccChatMessage().apply {
                jsonMessageId = 1
                actorId = "user1"
                message = "I love Nextcloud"
                timestamp = System.currentTimeMillis()
                actorDisplayName = "User1"
                messageType = GccChatMessage.MessageType.REGULAR_TEXT_MESSAGE.name
            },
            GccChatMessage().apply {
                jsonMessageId = 2
                actorId = "user1_id"
                message = "I love Nextcloud"
                timestamp = System.currentTimeMillis()
                actorDisplayName = "User2"
                messageType = GccChatMessage.MessageType.REGULAR_TEXT_MESSAGE.name
            },
            GccChatMessage().apply {
                jsonMessageId = 3
                actorId = "user1_id"
                message = "This is a really really really really really really really really really long message"
                timestamp = System.currentTimeMillis()
                actorDisplayName = "User2"
                messageType = GccChatMessage.MessageType.REGULAR_TEXT_MESSAGE.name
            },
            GccChatMessage().apply {
                jsonMessageId = 4
                actorId = "user1_id"
                message = "some \n linebreak"
                timestamp = System.currentTimeMillis()
                actorDisplayName = "User2"
                messageType = GccChatMessage.MessageType.REGULAR_TEXT_MESSAGE.name
            },
            GccChatMessage().apply {
                jsonMessageId = 5
                actorId = "user1_id"
                threadTitle = "GccThread title"
                isThread = true
                message = "Content of a first thread message"
                timestamp = System.currentTimeMillis()
                actorDisplayName = "User2"
                messageType = GccChatMessage.MessageType.REGULAR_TEXT_MESSAGE.name
            },
            GccChatMessage().apply {
                jsonMessageId = 6
                actorId = "user1_id"
                threadTitle = "looooooooooooong GccThread title"
                isThread = true
                message = "Content"
                timestamp = System.currentTimeMillis()
                actorDisplayName = "User2"
                messageType = GccChatMessage.MessageType.REGULAR_TEXT_MESSAGE.name
            }
        )
    }

    LaunchedEffect(sampleMessages) {
        // Use LaunchedEffect or similar to update state once
        if (adapter.items.isEmpty()) {
            // Prevent adding multiple times on recomposition
            adapter.addMessages(sampleMessages.toMutableList(), append = false) // Add messages
        }
    }

    MaterialTheme(colorScheme = adapter.colorScheme) {
        // Use the (potentially faked) color scheme
        Box(modifier = Modifier.fillMaxSize()) {
            // Provide a container
            adapter.GetView() // Call the main Composable
        }
    }
}
