/*
 * Nextcloud Talk - Android Client
 *
 * SPDX-FileCopyrightText: 2022 Daniel Calviño Sánchez <danxuliu@gmail.com>
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package com.gcc.talk.gccSignaling;

import com.gcc.talk.gccModels.json.signaling.NCMessagePayload;
import com.gcc.talk.gccModels.json.signaling.NCSignalingMessage;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;

import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.only;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

public class SignalingMessageReceiverTest {

    private GccSignalingMessageReceiver signalingMessageReceiver;

    @Before
    public void setUp() {
        // GccSignalingMessageReceiver is abstract to prevent direct instantiation without calling the appropriate
        // protected methods.
        signalingMessageReceiver = new GccSignalingMessageReceiver() {
        };
    }

    @Test
    public void testOfferWithOfferAndWebRtcMessageListeners() {
        GccSignalingMessageReceiver.OfferMessageListener mockedOfferMessageListener =
            mock(GccSignalingMessageReceiver.OfferMessageListener.class);
        GccSignalingMessageReceiver.WebRtcMessageListener mockedWebRtcMessageListener =
            mock(GccSignalingMessageReceiver.WebRtcMessageListener.class);

        signalingMessageReceiver.addListener(mockedOfferMessageListener);
        signalingMessageReceiver.addListener(mockedWebRtcMessageListener, "theSessionId", "theRoomType");

        NCSignalingMessage signalingMessage = new NCSignalingMessage();
        signalingMessage.setFrom("theSessionId");
        signalingMessage.setType("offer");
        signalingMessage.setRoomType("theRoomType");
        NCMessagePayload messagePayload = new NCMessagePayload();
        messagePayload.setType("offer");
        messagePayload.setSdp("theSdp");
        messagePayload.setNick("theNick");
        signalingMessage.setPayload(messagePayload);
        signalingMessageReceiver.processSignalingMessage(signalingMessage);

        InOrder inOrder = inOrder(mockedOfferMessageListener, mockedWebRtcMessageListener);

        inOrder.verify(mockedOfferMessageListener).onOffer("theSessionId", "theRoomType", "theSdp", "theNick");
        inOrder.verify(mockedWebRtcMessageListener).onOffer("theSdp", "theNick");
    }

    @Test
    public void testAddWebRtcMessageListenerWhenHandlingOffer() {
        GccSignalingMessageReceiver.OfferMessageListener mockedOfferMessageListener =
            mock(GccSignalingMessageReceiver.OfferMessageListener.class);
        GccSignalingMessageReceiver.WebRtcMessageListener mockedWebRtcMessageListener =
            mock(GccSignalingMessageReceiver.WebRtcMessageListener.class);

        doAnswer((invocation) -> {
            signalingMessageReceiver.addListener(mockedWebRtcMessageListener, "theSessionId", "theRoomType");
            return null;
        }).when(mockedOfferMessageListener).onOffer("theSessionId", "theRoomType", "theSdp", "theNick");

        signalingMessageReceiver.addListener(mockedOfferMessageListener);

        NCSignalingMessage signalingMessage = new NCSignalingMessage();
        signalingMessage.setFrom("theSessionId");
        signalingMessage.setType("offer");
        signalingMessage.setRoomType("theRoomType");
        NCMessagePayload messagePayload = new NCMessagePayload();
        messagePayload.setType("offer");
        messagePayload.setSdp("theSdp");
        messagePayload.setNick("theNick");
        signalingMessage.setPayload(messagePayload);
        signalingMessageReceiver.processSignalingMessage(signalingMessage);

        InOrder inOrder = inOrder(mockedOfferMessageListener, mockedWebRtcMessageListener);

        inOrder.verify(mockedOfferMessageListener).onOffer("theSessionId", "theRoomType", "theSdp", "theNick");
        inOrder.verify(mockedWebRtcMessageListener).onOffer("theSdp", "theNick");
    }

    @Test
    public void testRemoveWebRtcMessageListenerWhenHandlingOffer() {
        GccSignalingMessageReceiver.OfferMessageListener mockedOfferMessageListener =
            mock(GccSignalingMessageReceiver.OfferMessageListener.class);
        GccSignalingMessageReceiver.WebRtcMessageListener mockedWebRtcMessageListener =
            mock(GccSignalingMessageReceiver.WebRtcMessageListener.class);

        doAnswer((invocation) -> {
            signalingMessageReceiver.removeListener(mockedWebRtcMessageListener);
            return null;
        }).when(mockedOfferMessageListener).onOffer("theSessionId", "theRoomType", "theSdp", "theNick");

        signalingMessageReceiver.addListener(mockedOfferMessageListener);
        signalingMessageReceiver.addListener(mockedWebRtcMessageListener, "theSessionId", "theRoomType");

        NCSignalingMessage signalingMessage = new NCSignalingMessage();
        signalingMessage.setFrom("theSessionId");
        signalingMessage.setType("offer");
        signalingMessage.setRoomType("theRoomType");
        NCMessagePayload messagePayload = new NCMessagePayload();
        messagePayload.setType("offer");
        messagePayload.setSdp("theSdp");
        messagePayload.setNick("theNick");
        signalingMessage.setPayload(messagePayload);
        signalingMessageReceiver.processSignalingMessage(signalingMessage);

        verify(mockedOfferMessageListener, only()).onOffer("theSessionId", "theRoomType", "theSdp", "theNick");
        verifyNoInteractions(mockedWebRtcMessageListener);
    }
}
