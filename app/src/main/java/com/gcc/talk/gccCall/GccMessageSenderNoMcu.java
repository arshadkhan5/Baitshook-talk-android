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
 * Helper class to send messages to participants in a call when an MCU is not used.
 */
public class GccMessageSenderNoMcu extends GccMessageSender {

    public GccMessageSenderNoMcu(GccSignalingMessageSender signalingMessageSender,
                                 Set<String> callParticipantSessionIds,
                                 List<GccPeerConnectionWrapper> peerConnectionWrappers) {
        super(signalingMessageSender, callParticipantSessionIds, peerConnectionWrappers);
    }

    /**
     * Sends the given data channel message to the given signaling session ID.
     *
     * @param dataChannelMessage the message to send
     * @param sessionId the signaling session ID of the participant to send the message to
     */
    public void send(DataChannelMessage dataChannelMessage, String sessionId) {
        GccPeerConnectionWrapper peerConnectionWrapper = getPeerConnectionWrapper(sessionId);
        if (peerConnectionWrapper != null) {
            peerConnectionWrapper.send(dataChannelMessage);
        }
    }

    public void sendToAll(DataChannelMessage dataChannelMessage) {
        for (GccPeerConnectionWrapper peerConnectionWrapper: peerConnectionWrappers) {
            if ("video".equals(peerConnectionWrapper.getVideoStreamType())){
                peerConnectionWrapper.send(dataChannelMessage);
            }
        }
    }
}
