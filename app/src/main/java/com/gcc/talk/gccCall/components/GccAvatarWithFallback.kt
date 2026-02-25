/*
 * Nextcloud Talk - Android Client
 *
 * SPDX-FileCopyrightText: 2025 Marcel Hibbe <dev@mhibbe.de>
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.gcc.talk.gccCall.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.gcc.talk.gccActivities.GccParticipantUiState
import com.gcc.talk.gccModels.json.participants.Participant
import com.gcc.talk.gccUtils.GccApiUtils
import com.gcc.talk.gccUtils.GccDisplayUtils.isDarkModeOn

@Composable
fun AvatarWithFallback(participant: GccParticipantUiState, displayName: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(CircleShape),
        contentAlignment = Alignment.Center
    ) {
        val avatarUrl = getUrlForAvatar(
            participant = participant,
            displayName = displayName
        )
        if (avatarUrl.isNotEmpty()) {
            AsyncImage(
                model = avatarUrl,
                contentDescription = "Avatar",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape)
            )
        } else {
            FallbackAvatar(participant = participant)
        }
    }
}

@Composable
private fun FallbackAvatar(participant: GccParticipantUiState) {
    val initials = participant.nick!!
        .split(" ")
        .mapNotNull { it.firstOrNull()?.uppercase() }
        .take(2)
        .joinToString("")
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = initials.ifEmpty { "?" },
            color = Color.Black,
            fontSize = 24.sp
        )
    }
}

@Composable
fun getUrlForAvatar(participant: GccParticipantUiState, displayName: String): String {
    var url = GccApiUtils.getUrlForAvatar(
        participant.baseUrl,
        participant.actorId,
        true
    )
    if (Participant.ActorType.GUESTS == participant.actorType ||
        Participant.ActorType.EMAILS == participant.actorType
    ) {
        url = GccApiUtils.getUrlForGuestAvatar(
            participant.baseUrl,
            displayName,
            true
        )
    }
    if (participant.actorType == Participant.ActorType.FEDERATED) {
        val darkTheme = if (isDarkModeOn(LocalContext.current)) 1 else 0
        url = GccApiUtils.getUrlForFederatedAvatar(
            participant.baseUrl,
            participant.roomToken,
            participant.actorId!!,
            darkTheme,
            true
        )
    }
    return url
}
