/*
 * Nextcloud Talk - Android Client
 *
 * SPDX-FileCopyrightText: 2022 Daniel Calviño Sánchez <danxuliu@gmail.com>
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package com.gcc.talk.gccSignaling;

import com.gcc.talk.gccModels.json.signaling.NCIceCandidate;
import com.gcc.talk.gccModels.json.signaling.NCMessagePayload;
import com.gcc.talk.gccModels.json.signaling.NCSignalingMessage;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;

import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.only;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

public class SignalingMessageReceiverWebRtcTest {

    private GccSignalingMessageReceiver signalingMessageReceiver;

    @Before
    public void setUp() {
        // GccSignalingMessageReceiver is abstract to prevent direct instantiation without calling the appropriate
        // protected methods.
        signalingMessageReceiver = new GccSignalingMessageReceiver() {
        };
    }

    @Test
    public void testAddWebRtcMessageListenerWithNullListener() {
        Assert.assertThrows(IllegalArgumentException.class, () -> {
            signalingMessageReceiver.addListener(null, "theSessionId", "theRoomType");
        });
    }

    @Test
    public void testAddWebRtcMessageListenerWithNullSessionId() {
        GccSignalingMessageReceiver.WebRtcMessageListener mockedWebRtcMessageListener =
            mock(GccSignalingMessageReceiver.WebRtcMessageListener.class);

        Assert.assertThrows(IllegalArgumentException.class, () -> {
            signalingMessageReceiver.addListener(mockedWebRtcMessageListener, null, "theRoomType");
        });
    }

    @Test
    public void testAddWebRtcMessageListenerWithNullRoomType() {
        GccSignalingMessageReceiver.WebRtcMessageListener mockedWebRtcMessageListener =
            mock(GccSignalingMessageReceiver.WebRtcMessageListener.class);

        Assert.assertThrows(IllegalArgumentException.class, () -> {
            signalingMessageReceiver.addListener(mockedWebRtcMessageListener, "theSessionId", null);
        });
    }

    @Test
    public void testWebRtcMessageOffer() {
        GccSignalingMessageReceiver.WebRtcMessageListener mockedWebRtcMessageListener =
            mock(GccSignalingMessageReceiver.WebRtcMessageListener.class);

        signalingMessageReceiver.addListener(mockedWebRtcMessageListener, "theSessionId", "theRoomType");

        NCSignalingMessage signalingMessage = new NCSignalingMessage();
        signalingMessage.setFrom("theSessionId");
        signalingMessage.setType("offer");
        signalingMessage.setRoomType("theRoomType");
        NCMessagePayload messagePayload = new NCMessagePayload();
        messagePayload.setType("offer");
        messagePayload.setSdp("theSdp");
        signalingMessage.setPayload(messagePayload);
        signalingMessageReceiver.processSignalingMessage(signalingMessage);

        verify(mockedWebRtcMessageListener, only()).onOffer("theSdp", null);
    }

    @Test
    public void testWebRtcMessageOfferWithNick() {
        GccSignalingMessageReceiver.WebRtcMessageListener mockedWebRtcMessageListener =
            mock(GccSignalingMessageReceiver.WebRtcMessageListener.class);

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

        verify(mockedWebRtcMessageListener, only()).onOffer("theSdp", "theNick");
    }

    @Test
    public void testWebRtcMessageAnswer() {
        GccSignalingMessageReceiver.WebRtcMessageListener mockedWebRtcMessageListener =
            mock(GccSignalingMessageReceiver.WebRtcMessageListener.class);

        signalingMessageReceiver.addListener(mockedWebRtcMessageListener, "theSessionId", "theRoomType");

        NCSignalingMessage signalingMessage = new NCSignalingMessage();
        signalingMessage.setFrom("theSessionId");
        signalingMessage.setType("answer");
        signalingMessage.setRoomType("theRoomType");
        NCMessagePayload messagePayload = new NCMessagePayload();
        messagePayload.setType("answer");
        messagePayload.setSdp("theSdp");
        signalingMessage.setPayload(messagePayload);
        signalingMessageReceiver.processSignalingMessage(signalingMessage);

        verify(mockedWebRtcMessageListener, only()).onAnswer("theSdp", null);
    }

    @Test
    public void testWebRtcMessageAnswerWithNick() {
        GccSignalingMessageReceiver.WebRtcMessageListener mockedWebRtcMessageListener =
            mock(GccSignalingMessageReceiver.WebRtcMessageListener.class);

        signalingMessageReceiver.addListener(mockedWebRtcMessageListener, "theSessionId", "theRoomType");

        NCSignalingMessage signalingMessage = new NCSignalingMessage();
        signalingMessage.setFrom("theSessionId");
        signalingMessage.setType("answer");
        signalingMessage.setRoomType("theRoomType");
        NCMessagePayload messagePayload = new NCMessagePayload();
        messagePayload.setType("answer");
        messagePayload.setSdp("theSdp");
        messagePayload.setNick("theNick");
        signalingMessage.setPayload(messagePayload);
        signalingMessageReceiver.processSignalingMessage(signalingMessage);

        verify(mockedWebRtcMessageListener, only()).onAnswer("theSdp", "theNick");
    }

    @Test
    public void testWebRtcMessageCandidate() {
        GccSignalingMessageReceiver.WebRtcMessageListener mockedWebRtcMessageListener =
            mock(GccSignalingMessageReceiver.WebRtcMessageListener.class);

        signalingMessageReceiver.addListener(mockedWebRtcMessageListener, "theSessionId", "theRoomType");

        NCSignalingMessage signalingMessage = new NCSignalingMessage();
        signalingMessage.setFrom("theSessionId");
        signalingMessage.setType("candidate");
        signalingMessage.setRoomType("theRoomType");
        NCMessagePayload messagePayload = new NCMessagePayload();
        NCIceCandidate iceCandidate = new NCIceCandidate();
        iceCandidate.setSdpMid("theSdpMid");
        iceCandidate.setSdpMLineIndex(42);
        iceCandidate.setCandidate("theSdp");
        messagePayload.setIceCandidate(iceCandidate);
        signalingMessage.setPayload(messagePayload);
        signalingMessageReceiver.processSignalingMessage(signalingMessage);

        verify(mockedWebRtcMessageListener, only()).onCandidate("theSdpMid", 42, "theSdp");
    }

    @Test
    public void testWebRtcMessageEndOfCandidates() {
        GccSignalingMessageReceiver.WebRtcMessageListener mockedWebRtcMessageListener =
            mock(GccSignalingMessageReceiver.WebRtcMessageListener.class);

        signalingMessageReceiver.addListener(mockedWebRtcMessageListener, "theSessionId", "theRoomType");

        NCSignalingMessage signalingMessage = new NCSignalingMessage();
        signalingMessage.setFrom("theSessionId");
        signalingMessage.setType("endOfCandidates");
        signalingMessage.setRoomType("theRoomType");
        signalingMessageReceiver.processSignalingMessage(signalingMessage);

        verify(mockedWebRtcMessageListener, only()).onEndOfCandidates();
    }

    @Test
    public void testWebRtcMessageSeveralListenersSameFrom() {
        GccSignalingMessageReceiver.WebRtcMessageListener mockedWebRtcMessageListener1 =
            mock(GccSignalingMessageReceiver.WebRtcMessageListener.class);
        GccSignalingMessageReceiver.WebRtcMessageListener mockedWebRtcMessageListener2 =
            mock(GccSignalingMessageReceiver.WebRtcMessageListener.class);

        signalingMessageReceiver.addListener(mockedWebRtcMessageListener1, "theSessionId", "theRoomType");
        signalingMessageReceiver.addListener(mockedWebRtcMessageListener2, "theSessionId", "theRoomType");

        NCSignalingMessage signalingMessage = new NCSignalingMessage();
        signalingMessage.setFrom("theSessionId");
        signalingMessage.setType("endOfCandidates");
        signalingMessage.setRoomType("theRoomType");
        signalingMessageReceiver.processSignalingMessage(signalingMessage);

        verify(mockedWebRtcMessageListener1, only()).onEndOfCandidates();
        verify(mockedWebRtcMessageListener2, only()).onEndOfCandidates();
    }

    @Test
    public void testWebRtcMessageNotMatchingSessionId() {
        GccSignalingMessageReceiver.WebRtcMessageListener mockedWebRtcMessageListener =
            mock(GccSignalingMessageReceiver.WebRtcMessageListener.class);

        signalingMessageReceiver.addListener(mockedWebRtcMessageListener, "theSessionId", "theRoomType");

        NCSignalingMessage signalingMessage = new NCSignalingMessage();
        signalingMessage.setFrom("notMatchingSessionId");
        signalingMessage.setType("endOfCandidates");
        signalingMessage.setRoomType("theRoomType");
        signalingMessageReceiver.processSignalingMessage(signalingMessage);

        verifyNoInteractions(mockedWebRtcMessageListener);
    }

    @Test
    public void testWebRtcMessageNotMatchingRoomType() {
        GccSignalingMessageReceiver.WebRtcMessageListener mockedWebRtcMessageListener =
            mock(GccSignalingMessageReceiver.WebRtcMessageListener.class);

        signalingMessageReceiver.addListener(mockedWebRtcMessageListener, "theSessionId", "theRoomType");

        NCSignalingMessage signalingMessage = new NCSignalingMessage();
        signalingMessage.setFrom("theSessionId");
        signalingMessage.setType("endOfCandidates");
        signalingMessage.setRoomType("notMatchingRoomType");
        signalingMessageReceiver.processSignalingMessage(signalingMessage);

        verifyNoInteractions(mockedWebRtcMessageListener);
    }

    @Test
    public void testWebRtcMessageAfterRemovingListener() {
        GccSignalingMessageReceiver.WebRtcMessageListener mockedWebRtcMessageListener =
            mock(GccSignalingMessageReceiver.WebRtcMessageListener.class);

        signalingMessageReceiver.addListener(mockedWebRtcMessageListener, "theSessionId", "theRoomType");
        signalingMessageReceiver.removeListener(mockedWebRtcMessageListener);

        NCSignalingMessage signalingMessage = new NCSignalingMessage();
        signalingMessage.setFrom("theSessionId");
        signalingMessage.setType("endOfCandidates");
        signalingMessage.setRoomType("theRoomType");
        signalingMessageReceiver.processSignalingMessage(signalingMessage);

        verifyNoInteractions(mockedWebRtcMessageListener);
    }

    @Test
    public void testWebRtcMessageAfterRemovingSingleListenerOfSeveral() {
        GccSignalingMessageReceiver.WebRtcMessageListener mockedWebRtcMessageListener1 =
            mock(GccSignalingMessageReceiver.WebRtcMessageListener.class);
        GccSignalingMessageReceiver.WebRtcMessageListener mockedWebRtcMessageListener2 =
            mock(GccSignalingMessageReceiver.WebRtcMessageListener.class);
        GccSignalingMessageReceiver.WebRtcMessageListener mockedWebRtcMessageListener3 =
            mock(GccSignalingMessageReceiver.WebRtcMessageListener.class);

        signalingMessageReceiver.addListener(mockedWebRtcMessageListener1, "theSessionId", "theRoomType");
        signalingMessageReceiver.addListener(mockedWebRtcMessageListener2, "theSessionId", "theRoomType");
        signalingMessageReceiver.addListener(mockedWebRtcMessageListener3, "theSessionId", "theRoomType");
        signalingMessageReceiver.removeListener(mockedWebRtcMessageListener2);

        NCSignalingMessage signalingMessage = new NCSignalingMessage();
        signalingMessage.setFrom("theSessionId");
        signalingMessage.setType("endOfCandidates");
        signalingMessage.setRoomType("theRoomType");
        signalingMessageReceiver.processSignalingMessage(signalingMessage);

        verify(mockedWebRtcMessageListener1, only()).onEndOfCandidates();
        verify(mockedWebRtcMessageListener3, only()).onEndOfCandidates();
        verifyNoInteractions(mockedWebRtcMessageListener2);
    }

    @Test
    public void testWebRtcMessageAfterAddingListenerAgainForDifferentFrom() {
        GccSignalingMessageReceiver.WebRtcMessageListener mockedWebRtcMessageListener =
            mock(GccSignalingMessageReceiver.WebRtcMessageListener.class);

        signalingMessageReceiver.addListener(mockedWebRtcMessageListener, "theSessionId", "theRoomType");
        signalingMessageReceiver.addListener(mockedWebRtcMessageListener, "theSessionId2", "theRoomType");

        NCSignalingMessage signalingMessage = new NCSignalingMessage();
        signalingMessage.setFrom("theSessionId");
        signalingMessage.setType("endOfCandidates");
        signalingMessage.setRoomType("theRoomType");
        signalingMessageReceiver.processSignalingMessage(signalingMessage);

        verifyNoInteractions(mockedWebRtcMessageListener);

        signalingMessage.setFrom("theSessionId2");
        signalingMessage.setType("endOfCandidates");
        signalingMessage.setRoomType("theRoomType");
        signalingMessageReceiver.processSignalingMessage(signalingMessage);

        verify(mockedWebRtcMessageListener, only()).onEndOfCandidates();
    }

    @Test
    public void testAddWebRtcMessageListenerWhenHandlingWebRtcMessage() {
        GccSignalingMessageReceiver.WebRtcMessageListener mockedWebRtcMessageListener1 =
            mock(GccSignalingMessageReceiver.WebRtcMessageListener.class);
        GccSignalingMessageReceiver.WebRtcMessageListener mockedWebRtcMessageListener2 =
            mock(GccSignalingMessageReceiver.WebRtcMessageListener.class);

        doAnswer((invocation) -> {
            signalingMessageReceiver.addListener(mockedWebRtcMessageListener2, "theSessionId", "theRoomType");
            return null;
        }).when(mockedWebRtcMessageListener1).onEndOfCandidates();

        signalingMessageReceiver.addListener(mockedWebRtcMessageListener1, "theSessionId", "theRoomType");

        NCSignalingMessage signalingMessage = new NCSignalingMessage();
        signalingMessage.setFrom("theSessionId");
        signalingMessage.setType("endOfCandidates");
        signalingMessage.setRoomType("theRoomType");
        signalingMessageReceiver.processSignalingMessage(signalingMessage);

        verify(mockedWebRtcMessageListener1, only()).onEndOfCandidates();
        verifyNoInteractions(mockedWebRtcMessageListener2);
    }

    @Test
    public void testRemoveWebRtcMessageListenerWhenHandlingWebRtcMessage() {
        GccSignalingMessageReceiver.WebRtcMessageListener mockedWebRtcMessageListener1 =
            mock(GccSignalingMessageReceiver.WebRtcMessageListener.class);
        GccSignalingMessageReceiver.WebRtcMessageListener mockedWebRtcMessageListener2 =
            mock(GccSignalingMessageReceiver.WebRtcMessageListener.class);

        doAnswer((invocation) -> {
            signalingMessageReceiver.removeListener(mockedWebRtcMessageListener2);
            return null;
        }).when(mockedWebRtcMessageListener1).onEndOfCandidates();

        signalingMessageReceiver.addListener(mockedWebRtcMessageListener1, "theSessionId", "theRoomType");
        signalingMessageReceiver.addListener(mockedWebRtcMessageListener2, "theSessionId", "theRoomType");

        NCSignalingMessage signalingMessage = new NCSignalingMessage();
        signalingMessage.setFrom("theSessionId");
        signalingMessage.setType("endOfCandidates");
        signalingMessage.setRoomType("theRoomType");
        signalingMessageReceiver.processSignalingMessage(signalingMessage);

        InOrder inOrder = inOrder(mockedWebRtcMessageListener1, mockedWebRtcMessageListener2);

        inOrder.verify(mockedWebRtcMessageListener1).onEndOfCandidates();
        inOrder.verify(mockedWebRtcMessageListener2).onEndOfCandidates();
    }
}
