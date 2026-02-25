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

public class SignalingMessageReceiverOfferTest {

    private GccSignalingMessageReceiver signalingMessageReceiver;

    @Before
    public void setUp() {
        // GccSignalingMessageReceiver is abstract to prevent direct instantiation without calling the appropriate
        // protected methods.
        signalingMessageReceiver = new GccSignalingMessageReceiver() {
        };
    }

    @Test
    public void testAddOfferMessageListenerWithNullListener() {
        Assert.assertThrows(IllegalArgumentException.class, () -> {
            signalingMessageReceiver.addListener((GccSignalingMessageReceiver.OfferMessageListener) null);
        });
    }

    @Test
    public void testOfferMessage() {
        GccSignalingMessageReceiver.OfferMessageListener mockedOfferMessageListener =
            mock(GccSignalingMessageReceiver.OfferMessageListener.class);

        signalingMessageReceiver.addListener(mockedOfferMessageListener);

        NCSignalingMessage signalingMessage = new NCSignalingMessage();
        signalingMessage.setFrom("theSessionId");
        signalingMessage.setType("offer");
        signalingMessage.setRoomType("theRoomType");
        NCMessagePayload messagePayload = new NCMessagePayload();
        messagePayload.setType("offer");
        messagePayload.setSdp("theSdp");
        signalingMessage.setPayload(messagePayload);
        signalingMessageReceiver.processSignalingMessage(signalingMessage);

        verify(mockedOfferMessageListener, only()).onOffer("theSessionId", "theRoomType", "theSdp", null);
    }

    @Test
    public void testOfferMessageWithNick() {
        GccSignalingMessageReceiver.OfferMessageListener mockedOfferMessageListener =
            mock(GccSignalingMessageReceiver.OfferMessageListener.class);

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

        verify(mockedOfferMessageListener, only()).onOffer("theSessionId", "theRoomType", "theSdp", "theNick");
    }

    @Test
    public void testOfferMessageAfterRemovingListener() {
        GccSignalingMessageReceiver.OfferMessageListener mockedOfferMessageListener =
            mock(GccSignalingMessageReceiver.OfferMessageListener.class);

        signalingMessageReceiver.addListener(mockedOfferMessageListener);
        signalingMessageReceiver.removeListener(mockedOfferMessageListener);

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

        verifyNoInteractions(mockedOfferMessageListener);
    }

    @Test
    public void testOfferMessageAfterRemovingSingleListenerOfSeveral() {
        GccSignalingMessageReceiver.OfferMessageListener mockedOfferMessageListener1 =
            mock(GccSignalingMessageReceiver.OfferMessageListener.class);
        GccSignalingMessageReceiver.OfferMessageListener mockedOfferMessageListener2 =
            mock(GccSignalingMessageReceiver.OfferMessageListener.class);
        GccSignalingMessageReceiver.OfferMessageListener mockedOfferMessageListener3 =
            mock(GccSignalingMessageReceiver.OfferMessageListener.class);

        signalingMessageReceiver.addListener(mockedOfferMessageListener1);
        signalingMessageReceiver.addListener(mockedOfferMessageListener2);
        signalingMessageReceiver.addListener(mockedOfferMessageListener3);
        signalingMessageReceiver.removeListener(mockedOfferMessageListener2);

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

        verify(mockedOfferMessageListener1, only()).onOffer("theSessionId", "theRoomType", "theSdp", "theNick");
        verify(mockedOfferMessageListener3, only()).onOffer("theSessionId", "theRoomType", "theSdp", "theNick");
        verifyNoInteractions(mockedOfferMessageListener2);
    }

    @Test
    public void testOfferMessageAfterAddingListenerAgain() {
        GccSignalingMessageReceiver.OfferMessageListener mockedOfferMessageListener =
            mock(GccSignalingMessageReceiver.OfferMessageListener.class);

        signalingMessageReceiver.addListener(mockedOfferMessageListener);
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

        verify(mockedOfferMessageListener, only()).onOffer("theSessionId", "theRoomType", "theSdp", "theNick");
    }

    @Test
    public void testAddOfferMessageListenerWhenHandlingOffer() {
        GccSignalingMessageReceiver.OfferMessageListener mockedOfferMessageListener1 =
            mock(GccSignalingMessageReceiver.OfferMessageListener.class);
        GccSignalingMessageReceiver.OfferMessageListener mockedOfferMessageListener2 =
            mock(GccSignalingMessageReceiver.OfferMessageListener.class);

        doAnswer((invocation) -> {
            signalingMessageReceiver.addListener(mockedOfferMessageListener2);
            return null;
        }).when(mockedOfferMessageListener1).onOffer("theSessionId", "theRoomType", "theSdp", "theNick");

        signalingMessageReceiver.addListener(mockedOfferMessageListener1);

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

        verify(mockedOfferMessageListener1, only()).onOffer("theSessionId", "theRoomType", "theSdp", "theNick");
        verifyNoInteractions(mockedOfferMessageListener2);
    }

    @Test
    public void testRemoveOfferMessageListenerWhenHandlingOffer() {
        GccSignalingMessageReceiver.OfferMessageListener mockedOfferMessageListener1 =
            mock(GccSignalingMessageReceiver.OfferMessageListener.class);
        GccSignalingMessageReceiver.OfferMessageListener mockedOfferMessageListener2 =
            mock(GccSignalingMessageReceiver.OfferMessageListener.class);

        doAnswer((invocation) -> {
            signalingMessageReceiver.removeListener(mockedOfferMessageListener2);
            return null;
        }).when(mockedOfferMessageListener1).onOffer("theSessionId", "theRoomType", "theSdp", "theNick");

        signalingMessageReceiver.addListener(mockedOfferMessageListener1);
        signalingMessageReceiver.addListener(mockedOfferMessageListener2);

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

        InOrder inOrder = inOrder(mockedOfferMessageListener1, mockedOfferMessageListener2);

        inOrder.verify(mockedOfferMessageListener1).onOffer("theSessionId", "theRoomType", "theSdp", "theNick");
        inOrder.verify(mockedOfferMessageListener2).onOffer("theSessionId", "theRoomType", "theSdp", "theNick");
    }
}
