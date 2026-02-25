/*
 * Nextcloud Talk - Android Client
 *
 * SPDX-FileCopyrightText: 2023 Samanwith KSN <samanwith21@gmail.com>
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package com.gcc.talk.gccWebrtc

import com.gcc.talk.gccWebrtc.GccPeerConnectionWrapper.PeerConnectionObserver
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito
import org.webrtc.MediaStream
import org.webrtc.PeerConnection

class PeerConnectionNotifierTest {
    private var notifier: GccPeerConnectionNotifier? = null
    private var observer1: PeerConnectionObserver? = null
    private var observer2: PeerConnectionObserver? = null
    private var mockStream: MediaStream? = null

    @Before
    fun setUp() {
        notifier = GccPeerConnectionNotifier()
        observer1 = Mockito.mock(PeerConnectionObserver::class.java)
        observer2 = Mockito.mock(PeerConnectionObserver::class.java)
        mockStream = Mockito.mock(MediaStream::class.java)
    }

    @Test
    fun testAddObserver() {
        notifier!!.addObserver(observer1)
        notifier!!.notifyStreamAdded(mockStream)
        Mockito.verify(observer1)?.onStreamAdded(mockStream)
        Mockito.verify(observer2, Mockito.never())?.onStreamAdded(mockStream)
    }

    @Test
    fun testRemoveObserver() {
        notifier!!.addObserver(observer1)
        notifier!!.addObserver(observer2)
        notifier!!.removeObserver(observer1)
        notifier!!.notifyStreamAdded(mockStream)
        Mockito.verify(observer1, Mockito.never())?.onStreamAdded(mockStream)
        Mockito.verify(observer2)?.onStreamAdded(mockStream)
    }

    @Test
    fun testNotifyStreamAdded() {
        notifier!!.addObserver(observer1)
        notifier!!.notifyStreamAdded(mockStream)
        Mockito.verify(observer1)?.onStreamAdded(mockStream)
    }

    @Test
    fun testNotifyStreamRemoved() {
        notifier!!.addObserver(observer1)
        notifier!!.notifyStreamRemoved(mockStream)
        Mockito.verify(observer1)?.onStreamRemoved(mockStream)
    }

    @Test
    fun testNotifyIceConnectionStateChanged() {
        notifier!!.addObserver(observer1)
        notifier!!.notifyIceConnectionStateChanged(PeerConnection.IceConnectionState.CONNECTED)
        Mockito.verify(observer1)?.onIceConnectionStateChanged(PeerConnection.IceConnectionState.CONNECTED)
    }
}
