/*
 * Nextcloud Talk - Android Client
 *
 * SPDX-FileCopyrightText: 2022 Daniel Calviño Sánchez <danxuliu@gmail.com>
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package com.gcc.talk.gccSignaling;

import com.gcc.talk.gccModels.json.participants.Participant;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Helper class to register and notify ParticipantListMessageListeners.
 * <p>
 * This class is only meant for internal use by GccSignalingMessageReceiver; listeners must register themselves against
 * a GccSignalingMessageReceiver rather than against a ParticipantListMessageNotifier.
 */
class GccParticipantListMessageNotifier {

    private final Set<GccSignalingMessageReceiver.ParticipantListMessageListener> participantListMessageListeners = new LinkedHashSet<>();

    public synchronized void addListener(GccSignalingMessageReceiver.ParticipantListMessageListener listener) {
        if (listener == null) {
            throw new IllegalArgumentException("participantListMessageListeners can not be null");
        }

        participantListMessageListeners.add(listener);
    }

    public synchronized void removeListener(GccSignalingMessageReceiver.ParticipantListMessageListener listener) {
        participantListMessageListeners.remove(listener);
    }

    public synchronized void notifyUsersInRoom(List<Participant> participants) {
        for (GccSignalingMessageReceiver.ParticipantListMessageListener listener : new ArrayList<>(participantListMessageListeners)) {
            listener.onUsersInRoom(participants);
        }
    }

    public synchronized void notifyParticipantsUpdate(List<Participant> participants) {
        for (GccSignalingMessageReceiver.ParticipantListMessageListener listener : new ArrayList<>(participantListMessageListeners)) {
            listener.onParticipantsUpdate(participants);
        }
    }

    public synchronized void notifyAllParticipantsUpdate(long inCall) {
        for (GccSignalingMessageReceiver.ParticipantListMessageListener listener : new ArrayList<>(participantListMessageListeners)) {
            listener.onAllParticipantsUpdate(inCall);
        }
    }
}
