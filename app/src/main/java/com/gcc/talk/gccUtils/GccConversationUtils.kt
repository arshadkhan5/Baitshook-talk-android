/*
 * Nextcloud Talk - Android Client
 *
 * SPDX-FileCopyrightText: 2023 Marcel Hibbe <dev@mhibbe.de>
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package com.gcc.talk.gccUtils

import com.gcc.talk.gccModels.domain.GccConversationModel
import com.gcc.talk.gccModels.json.capabilities.SpreedCapability
import com.gcc.talk.gccModels.json.conversations.ConversationEnums
import com.gcc.talk.gccModels.json.participants.Participant

object GccConversationUtils {
    private val TAG = GccConversationUtils::class.java.simpleName

    fun isPublic(conversation: GccConversationModel): Boolean =
        ConversationEnums.ConversationType.ROOM_PUBLIC_CALL == conversation.type

    fun isGuest(conversation: GccConversationModel): Boolean =
        Participant.ParticipantType.GUEST == conversation.participantType ||
            Participant.ParticipantType.GUEST_MODERATOR == conversation.participantType ||
            Participant.ParticipantType.USER_FOLLOWING_LINK == conversation.participantType

    fun isParticipantOwnerOrModerator(conversation: GccConversationModel): Boolean =
        Participant.ParticipantType.OWNER == conversation.participantType ||
            Participant.ParticipantType.GUEST_MODERATOR == conversation.participantType ||
            Participant.ParticipantType.MODERATOR == conversation.participantType

    fun isLockedOneToOne(conversation: GccConversationModel, spreedCapabilities: SpreedCapability?): Boolean =
        conversation.type == ConversationEnums.ConversationType.ROOM_TYPE_ONE_TO_ONE_CALL &&
            CapabilitiesUtil.hasSpreedFeatureCapability(spreedCapabilities, SpreedFeatures.LOCKED_ONE_TO_ONE)

    fun canModerate(conversation: GccConversationModel, spreedCapabilities: SpreedCapability?): Boolean =
        isParticipantOwnerOrModerator(conversation) &&
            !isLockedOneToOne(conversation, spreedCapabilities) &&
            conversation.type != ConversationEnums.ConversationType.FORMER_ONE_TO_ONE &&
            !isNoteToSelfConversation(conversation)

    fun isConversationReadOnlyAvailable(
        conversation: GccConversationModel,
        spreedCapabilities: SpreedCapability
    ): Boolean =
        CapabilitiesUtil.hasSpreedFeatureCapability(spreedCapabilities, SpreedFeatures.READ_ONLY_ROOMS) &&
            canModerate(conversation, spreedCapabilities)

    fun isLobbyViewApplicable(conversation: GccConversationModel, spreedCapabilities: SpreedCapability): Boolean =
        !canModerate(conversation, spreedCapabilities) &&
            (
                conversation.type == ConversationEnums.ConversationType.ROOM_GROUP_CALL ||
                    conversation.type == ConversationEnums.ConversationType.ROOM_PUBLIC_CALL
                )

    fun isNameEditable(conversation: GccConversationModel, spreedCapabilities: SpreedCapability): Boolean =
        canModerate(conversation, spreedCapabilities) &&
            ConversationEnums.ConversationType.ROOM_TYPE_ONE_TO_ONE_CALL != conversation.type

    fun isNoteToSelfConversation(currentConversation: GccConversationModel?): Boolean =
        currentConversation != null &&
            currentConversation.type == ConversationEnums.ConversationType.NOTE_TO_SELF
}
