/*
 * Nextcloud Talk - Android Client
 *
 * SPDX-FileCopyrightText: 2024 Daniel Calviño Sánchez <danxuliu@gmail.com>
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package com.gcc.talk.gccCall;

import com.gcc.talk.gccModels.json.signaling.DataChannelMessage;
import com.gcc.talk.gccSignaling.GccSignalingMessageSender;
import com.gcc.talk.gccWebrtc.GccPeerConnectionWrapper;

import java.util.List;
import java.util.Set;

/**
 * Helper class to send messages to participants in a call when an MCU is used.
 * <p>
 * Note that when Janus is used it is not possible to send a data channel message to a specific participant. Any data
 * channel message will be broadcast to all the subscribers of the publisher peer connection (the own peer connection).
 */
public class GccMessageSenderMcu extends GccMessageSender {

    private final String ownSessionId;

    public GccMessageSenderMcu(GccSignalingMessageSender signalingMessageSender,
                               Set<String> callParticipantSessionIds,
                               List<GccPeerConnectionWrapper> peerConnectionWrappers,
                               String ownSessionId) {
        super(signalingMessageSender, callParticipantSessionIds, peerConnectionWrappers);

        this.ownSessionId = ownSessionId;
    }

    public void sendToAll(DataChannelMessage dataChannelMessage) {
        GccPeerConnectionWrapper ownPeerConnectionWrapper = getPeerConnectionWrapper(ownSessionId);
        if (ownPeerConnectionWrapper != null) {
            ownPeerConnectionWrapper.send(dataChannelMessage);
        }
    }
}
