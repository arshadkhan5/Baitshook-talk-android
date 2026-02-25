/*
 * Nextcloud Talk - Android Client
 *
 * SPDX-FileCopyrightText: 2022 Daniel Calviño Sánchez <danxuliu@gmail.com>
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package com.gcc.talk.gccSignaling;

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

public class SignalingMessageReceiverCallParticipantTest {

    private GccSignalingMessageReceiver signalingMessageReceiver;

    @Before
    public void setUp() {
        // GccSignalingMessageReceiver is abstract to prevent direct instantiation without calling the appropriate
        // protected methods.
        signalingMessageReceiver = new GccSignalingMessageReceiver() {
        };
    }

    @Test
    public void testAddCallParticipantMessageListenerWithNullListener() {
        Assert.assertThrows(IllegalArgumentException.class, () -> {
            signalingMessageReceiver.addListener(null, "theSessionId");
        });
    }

    @Test
    public void testAddCallParticipantMessageListenerWithNullSessionId() {
        GccSignalingMessageReceiver.CallParticipantMessageListener mockedCallParticipantMessageListener =
            mock(GccSignalingMessageReceiver.CallParticipantMessageListener.class);

        Assert.assertThrows(IllegalArgumentException.class, () -> {
            signalingMessageReceiver.addListener(mockedCallParticipantMessageListener, null);
        });
    }

    @Test
    public void testCallParticipantMessageRaiseHand() {
       GccSignalingMessageReceiver.CallParticipantMessageListener mockedCallParticipantMessageListener =
            mock(GccSignalingMessageReceiver.CallParticipantMessageListener.class);

        signalingMessageReceiver.addListener(mockedCallParticipantMessageListener, "theSessionId");

        NCSignalingMessage signalingMessage = new NCSignalingMessage();
        signalingMessage.setFrom("theSessionId");
        signalingMessage.setType("raiseHand");
        signalingMessage.setRoomType("theRoomType");
        NCMessagePayload messagePayload = new NCMessagePayload();
        messagePayload.setType("raiseHand");
        messagePayload.setState(Boolean.TRUE);
        messagePayload.setTimestamp(4815162342L);
        signalingMessage.setPayload(messagePayload);
        signalingMessageReceiver.processSignalingMessage(signalingMessage);

        verify(mockedCallParticipantMessageListener, only()).onRaiseHand(true, 4815162342L);
    }

    @Test
    public void testCallParticipantMessageReaction() {
        GccSignalingMessageReceiver.CallParticipantMessageListener mockedCallParticipantMessageListener =
            mock(GccSignalingMessageReceiver.CallParticipantMessageListener.class);

        signalingMessageReceiver.addListener(mockedCallParticipantMessageListener, "theSessionId");

        NCSignalingMessage signalingMessage = new NCSignalingMessage();
        signalingMessage.setFrom("theSessionId");
        signalingMessage.setType("reaction");
        signalingMessage.setRoomType("theRoomType");
        NCMessagePayload messagePayload = new NCMessagePayload();
        messagePayload.setType("reaction");
        messagePayload.setReaction("theReaction");
        signalingMessage.setPayload(messagePayload);
        signalingMessageReceiver.processSignalingMessage(signalingMessage);

        verify(mockedCallParticipantMessageListener, only()).onReaction("theReaction");
    }

    @Test
    public void testCallParticipantMessageUnshareScreen() {
        GccSignalingMessageReceiver.CallParticipantMessageListener mockedCallParticipantMessageListener =
            mock(GccSignalingMessageReceiver.CallParticipantMessageListener.class);

        signalingMessageReceiver.addListener(mockedCallParticipantMessageListener, "theSessionId");

        NCSignalingMessage signalingMessage = new NCSignalingMessage();
        signalingMessage.setFrom("theSessionId");
        signalingMessage.setType("unshareScreen");
        signalingMessage.setRoomType("theRoomType");
        signalingMessageReceiver.processSignalingMessage(signalingMessage);

        verify(mockedCallParticipantMessageListener, only()).onUnshareScreen();
    }

    @Test
    public void testCallParticipantMessageSeveralListenersSameFrom() {
        GccSignalingMessageReceiver.CallParticipantMessageListener mockedCallParticipantMessageListener1 =
            mock(GccSignalingMessageReceiver.CallParticipantMessageListener.class);
        GccSignalingMessageReceiver.CallParticipantMessageListener mockedCallParticipantMessageListener2 =
            mock(GccSignalingMessageReceiver.CallParticipantMessageListener.class);

        signalingMessageReceiver.addListener(mockedCallParticipantMessageListener1, "theSessionId");
        signalingMessageReceiver.addListener(mockedCallParticipantMessageListener2, "theSessionId");

        NCSignalingMessage signalingMessage = new NCSignalingMessage();
        signalingMessage.setFrom("theSessionId");
        signalingMessage.setType("unshareScreen");
        signalingMessage.setRoomType("theRoomType");
        signalingMessageReceiver.processSignalingMessage(signalingMessage);

        verify(mockedCallParticipantMessageListener1, only()).onUnshareScreen();
        verify(mockedCallParticipantMessageListener2, only()).onUnshareScreen();
    }

    @Test
    public void testCallParticipantMessageNotMatchingSessionId() {
        GccSignalingMessageReceiver.CallParticipantMessageListener mockedCallParticipantMessageListener =
            mock(GccSignalingMessageReceiver.CallParticipantMessageListener.class);

        signalingMessageReceiver.addListener(mockedCallParticipantMessageListener, "theSessionId");

        NCSignalingMessage signalingMessage = new NCSignalingMessage();
        signalingMessage.setFrom("notMatchingSessionId");
        signalingMessage.setType("unshareScreen");
        signalingMessage.setRoomType("theRoomType");
        signalingMessageReceiver.processSignalingMessage(signalingMessage);

        verifyNoInteractions(mockedCallParticipantMessageListener);
    }

    @Test
    public void testCallParticipantMessageAfterRemovingListener() {
        GccSignalingMessageReceiver.CallParticipantMessageListener mockedCallParticipantMessageListener =
            mock(GccSignalingMessageReceiver.CallParticipantMessageListener.class);

        signalingMessageReceiver.addListener(mockedCallParticipantMessageListener, "theSessionId");
        signalingMessageReceiver.removeListener(mockedCallParticipantMessageListener);

        NCSignalingMessage signalingMessage = new NCSignalingMessage();
        signalingMessage.setFrom("theSessionId");
        signalingMessage.setType("unshareScreen");
        signalingMessage.setRoomType("theRoomType");
        signalingMessageReceiver.processSignalingMessage(signalingMessage);

        verifyNoInteractions(mockedCallParticipantMessageListener);
    }

    @Test
    public void testCallParticipantMessageAfterRemovingSingleListenerOfSeveral() {
       GccSignalingMessageReceiver.CallParticipantMessageListener mockedCallParticipantMessageListener1 =
            mock(GccSignalingMessageReceiver.CallParticipantMessageListener.class);
       GccSignalingMessageReceiver.CallParticipantMessageListener mockedCallParticipantMessageListener2 =
            mock(GccSignalingMessageReceiver.CallParticipantMessageListener.class);
        GccSignalingMessageReceiver.CallParticipantMessageListener mockedCallParticipantMessageListener3 =
            mock(GccSignalingMessageReceiver.CallParticipantMessageListener.class);

        signalingMessageReceiver.addListener(mockedCallParticipantMessageListener1, "theSessionId");
        signalingMessageReceiver.addListener(mockedCallParticipantMessageListener2, "theSessionId");
        signalingMessageReceiver.addListener(mockedCallParticipantMessageListener3, "theSessionId");
        signalingMessageReceiver.removeListener(mockedCallParticipantMessageListener2);

        NCSignalingMessage signalingMessage = new NCSignalingMessage();
        signalingMessage.setFrom("theSessionId");
        signalingMessage.setType("unshareScreen");
        signalingMessage.setRoomType("theRoomType");
        signalingMessageReceiver.processSignalingMessage(signalingMessage);

        verify(mockedCallParticipantMessageListener1, only()).onUnshareScreen();
        verify(mockedCallParticipantMessageListener3, only()).onUnshareScreen();
        verifyNoInteractions(mockedCallParticipantMessageListener2);
    }

    @Test
    public void testCallParticipantMessageAfterAddingListenerAgainForDifferentFrom() {
        GccSignalingMessageReceiver.CallParticipantMessageListener mockedCallParticipantMessageListener =
            mock(GccSignalingMessageReceiver.CallParticipantMessageListener.class);

        signalingMessageReceiver.addListener(mockedCallParticipantMessageListener, "theSessionId");
        signalingMessageReceiver.addListener(mockedCallParticipantMessageListener, "theSessionId2");

        NCSignalingMessage signalingMessage = new NCSignalingMessage();
        signalingMessage.setFrom("theSessionId");
        signalingMessage.setType("unshareScreen");
        signalingMessage.setRoomType("theRoomType");
        signalingMessageReceiver.processSignalingMessage(signalingMessage);

        verifyNoInteractions(mockedCallParticipantMessageListener);

        signalingMessage.setFrom("theSessionId2");
        signalingMessage.setType("unshareScreen");
        signalingMessage.setRoomType("theRoomType");
        signalingMessageReceiver.processSignalingMessage(signalingMessage);

        verify(mockedCallParticipantMessageListener, only()).onUnshareScreen();
    }

    @Test
    public void testAddCallParticipantMessageListenerWhenHandlingCallParticipantMessage() {
        GccSignalingMessageReceiver.CallParticipantMessageListener mockedCallParticipantMessageListener1 =
            mock(GccSignalingMessageReceiver.CallParticipantMessageListener.class);
        GccSignalingMessageReceiver.CallParticipantMessageListener mockedCallParticipantMessageListener2 =
            mock(GccSignalingMessageReceiver.CallParticipantMessageListener.class);

        doAnswer((invocation) -> {
            signalingMessageReceiver.addListener(mockedCallParticipantMessageListener2, "theSessionId");
            return null;
        }).when(mockedCallParticipantMessageListener1).onUnshareScreen();

        signalingMessageReceiver.addListener(mockedCallParticipantMessageListener1, "theSessionId");

        NCSignalingMessage signalingMessage = new NCSignalingMessage();
        signalingMessage.setFrom("theSessionId");
        signalingMessage.setType("unshareScreen");
        signalingMessage.setRoomType("theRoomType");
        signalingMessageReceiver.processSignalingMessage(signalingMessage);

        verify(mockedCallParticipantMessageListener1, only()).onUnshareScreen();
        verifyNoInteractions(mockedCallParticipantMessageListener2);
    }

    @Test
    public void testRemoveCallParticipantMessageListenerWhenHandlingCallParticipantMessage() {
        GccSignalingMessageReceiver.CallParticipantMessageListener mockedCallParticipantMessageListener1 =
            mock(GccSignalingMessageReceiver.CallParticipantMessageListener.class);
        GccSignalingMessageReceiver.CallParticipantMessageListener mockedCallParticipantMessageListener2 =
            mock(GccSignalingMessageReceiver.CallParticipantMessageListener.class);

        doAnswer((invocation) -> {
            signalingMessageReceiver.removeListener(mockedCallParticipantMessageListener2);
            return null;
        }).when(mockedCallParticipantMessageListener1).onUnshareScreen();

        signalingMessageReceiver.addListener(mockedCallParticipantMessageListener1, "theSessionId");
        signalingMessageReceiver.addListener(mockedCallParticipantMessageListener2, "theSessionId");

        NCSignalingMessage signalingMessage = new NCSignalingMessage();
        signalingMessage.setFrom("theSessionId");
        signalingMessage.setType("unshareScreen");
        signalingMessage.setRoomType("theRoomType");
        signalingMessageReceiver.processSignalingMessage(signalingMessage);

        InOrder inOrder = inOrder(mockedCallParticipantMessageListener1, mockedCallParticipantMessageListener2);

        inOrder.verify(mockedCallParticipantMessageListener1).onUnshareScreen();
        inOrder.verify(mockedCallParticipantMessageListener2).onUnshareScreen();
    }
}
