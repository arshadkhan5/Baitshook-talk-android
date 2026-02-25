/*
 * Nextcloud Talk - Android Client
 *
 * SPDX-FileCopyrightText: 2024 Daniel Calviño Sánchez <danxuliu@gmail.com>
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package com.gcc.talk.gccCall

import com.gcc.talk.gccModels.json.signaling.DataChannelMessage
import com.gcc.talk.gccSignaling.GccSignalingMessageSender
import com.gcc.talk.gccWebrtc.GccPeerConnectionWrapper
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito
import org.mockito.Mockito.never

class MessageSenderNoMcuTest {

    private var peerConnectionWrappers: MutableList<GccPeerConnectionWrapper?>? = null
    private var peerConnectionWrapper1: GccPeerConnectionWrapper? = null
    private var peerConnectionWrapper2: GccPeerConnectionWrapper? = null
    private var peerConnectionWrapper2Screen: GccPeerConnectionWrapper? = null
    private var peerConnectionWrapper4Screen: GccPeerConnectionWrapper? = null

    private var messageSender: GccMessageSenderNoMcu? = null

    @Before
    fun setUp() {
        val signalingMessageSender = Mockito.mock(GccSignalingMessageSender::class.java)

        val callParticipants = HashMap<String, Set<String>>()

        peerConnectionWrappers = ArrayList()

        peerConnectionWrapper1 = Mockito.mock(GccPeerConnectionWrapper::class.java)
        Mockito.`when`(peerConnectionWrapper1!!.sessionId).thenReturn("theSessionId1")
        Mockito.`when`(peerConnectionWrapper1!!.videoStreamType).thenReturn("video")
        peerConnectionWrappers!!.add(peerConnectionWrapper1)

        peerConnectionWrapper2 = Mockito.mock(GccPeerConnectionWrapper::class.java)
        Mockito.`when`(peerConnectionWrapper2!!.sessionId).thenReturn("theSessionId2")
        Mockito.`when`(peerConnectionWrapper2!!.videoStreamType).thenReturn("video")
        peerConnectionWrappers!!.add(peerConnectionWrapper2)

        peerConnectionWrapper2Screen = Mockito.mock(GccPeerConnectionWrapper::class.java)
        Mockito.`when`(peerConnectionWrapper2Screen!!.sessionId).thenReturn("theSessionId2")
        Mockito.`when`(peerConnectionWrapper2Screen!!.videoStreamType).thenReturn("screen")
        peerConnectionWrappers!!.add(peerConnectionWrapper2Screen)

        peerConnectionWrapper4Screen = Mockito.mock(GccPeerConnectionWrapper::class.java)
        Mockito.`when`(peerConnectionWrapper4Screen!!.sessionId).thenReturn("theSessionId4")
        Mockito.`when`(peerConnectionWrapper4Screen!!.videoStreamType).thenReturn("screen")
        peerConnectionWrappers!!.add(peerConnectionWrapper4Screen)

        messageSender = GccMessageSenderNoMcu(signalingMessageSender, callParticipants.keys, peerConnectionWrappers)
    }

    @Test
    fun testSendDataChannelMessage() {
        val message = DataChannelMessage()
        messageSender!!.send(message, "theSessionId2")

        Mockito.verify(peerConnectionWrapper2!!).send(message)
        Mockito.verify(peerConnectionWrapper1!!, never()).send(message)
        Mockito.verify(peerConnectionWrapper2Screen!!, never()).send(message)
        Mockito.verify(peerConnectionWrapper4Screen!!, never()).send(message)
    }

    @Test
    fun testSendDataChannelMessageIfScreenPeerConnection() {
        val message = DataChannelMessage()
        messageSender!!.send(message, "theSessionId4")

        Mockito.verify(peerConnectionWrapper1!!, never()).send(message)
        Mockito.verify(peerConnectionWrapper2!!, never()).send(message)
        Mockito.verify(peerConnectionWrapper2Screen!!, never()).send(message)
        Mockito.verify(peerConnectionWrapper4Screen!!, never()).send(message)
    }

    @Test
    fun testSendDataChannelMessageIfNoPeerConnection() {
        val message = DataChannelMessage()
        messageSender!!.send(message, "theSessionId3")

        Mockito.verify(peerConnectionWrapper1!!, never()).send(message)
        Mockito.verify(peerConnectionWrapper2!!, never()).send(message)
        Mockito.verify(peerConnectionWrapper2Screen!!, never()).send(message)
        Mockito.verify(peerConnectionWrapper4Screen!!, never()).send(message)
    }

    @Test
    fun testSendDataChannelMessageToAll() {
        val message = DataChannelMessage()
        messageSender!!.sendToAll(message)

        Mockito.verify(peerConnectionWrapper1!!).send(message)
        Mockito.verify(peerConnectionWrapper2!!).send(message)
        Mockito.verify(peerConnectionWrapper2Screen!!, never()).send(message)
        Mockito.verify(peerConnectionWrapper4Screen!!, never()).send(message)
    }
}
