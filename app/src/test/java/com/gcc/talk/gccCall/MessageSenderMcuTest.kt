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

class MessageSenderMcuTest {

    private var peerConnectionWrappers: MutableList<GccPeerConnectionWrapper?>? = null
    private var peerConnectionWrapper1: GccPeerConnectionWrapper? = null
    private var peerConnectionWrapper2: GccPeerConnectionWrapper? = null
    private var peerConnectionWrapper2Screen: GccPeerConnectionWrapper? = null
    private var peerConnectionWrapper4Screen: GccPeerConnectionWrapper? = null
    private var ownPeerConnectionWrapper: GccPeerConnectionWrapper? = null
    private var ownPeerConnectionWrapperScreen: GccPeerConnectionWrapper? = null

    private var messageSender: GccMessageSenderMcu? = null

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

        ownPeerConnectionWrapper = Mockito.mock(GccPeerConnectionWrapper::class.java)
        Mockito.`when`(ownPeerConnectionWrapper!!.sessionId).thenReturn("ownSessionId")
        Mockito.`when`(ownPeerConnectionWrapper!!.videoStreamType).thenReturn("video")
        peerConnectionWrappers!!.add(ownPeerConnectionWrapper)

        ownPeerConnectionWrapperScreen = Mockito.mock(GccPeerConnectionWrapper::class.java)
        Mockito.`when`(ownPeerConnectionWrapperScreen!!.sessionId).thenReturn("ownSessionId")
        Mockito.`when`(ownPeerConnectionWrapperScreen!!.videoStreamType).thenReturn("screen")
        peerConnectionWrappers!!.add(ownPeerConnectionWrapperScreen)

        messageSender = GccMessageSenderMcu(
            signalingMessageSender,
            callParticipants.keys,
            peerConnectionWrappers,
            "ownSessionId"
        )
    }

    @Test
    fun testSendDataChannelMessageToAll() {
        val message = DataChannelMessage()
        messageSender!!.sendToAll(message)

        Mockito.verify(ownPeerConnectionWrapper!!).send(message)
        Mockito.verify(ownPeerConnectionWrapperScreen!!, never()).send(message)
        Mockito.verify(peerConnectionWrapper1!!, never()).send(message)
        Mockito.verify(peerConnectionWrapper2!!, never()).send(message)
        Mockito.verify(peerConnectionWrapper2Screen!!, never()).send(message)
        Mockito.verify(peerConnectionWrapper4Screen!!, never()).send(message)
    }

    @Test
    fun testSendDataChannelMessageToAllIfOwnScreenPeerConnection() {
        peerConnectionWrappers!!.remove(ownPeerConnectionWrapper)

        val message = DataChannelMessage()
        messageSender!!.sendToAll(message)

        Mockito.verify(ownPeerConnectionWrapper!!, never()).send(message)
        Mockito.verify(ownPeerConnectionWrapperScreen!!, never()).send(message)
        Mockito.verify(peerConnectionWrapper1!!, never()).send(message)
        Mockito.verify(peerConnectionWrapper2!!, never()).send(message)
        Mockito.verify(peerConnectionWrapper2Screen!!, never()).send(message)
        Mockito.verify(peerConnectionWrapper4Screen!!, never()).send(message)
    }

    @Test
    fun testSendDataChannelMessageToAllWithoutOwnPeerConnection() {
        peerConnectionWrappers!!.remove(ownPeerConnectionWrapper)
        peerConnectionWrappers!!.remove(ownPeerConnectionWrapperScreen)

        val message = DataChannelMessage()
        messageSender!!.sendToAll(message)

        Mockito.verify(ownPeerConnectionWrapper!!, never()).send(message)
        Mockito.verify(ownPeerConnectionWrapperScreen!!, never()).send(message)
        Mockito.verify(peerConnectionWrapper1!!, never()).send(message)
        Mockito.verify(peerConnectionWrapper2!!, never()).send(message)
        Mockito.verify(peerConnectionWrapper2Screen!!, never()).send(message)
        Mockito.verify(peerConnectionWrapper4Screen!!, never()).send(message)
    }
}
