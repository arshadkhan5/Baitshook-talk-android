/*
 * Nextcloud Talk - Android Client
 *
 * SPDX-FileCopyrightText: 2022 Daniel Calviño Sánchez <danxuliu@gmail.com>
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package com.gcc.talk.gccWebrtc;

import org.webrtc.MediaStream;
import org.webrtc.PeerConnection;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Helper class to register and notify PeerConnectionObserver.
 * <p>
 * This class is only meant for internal use by GccPeerConnectionWrapper; observers must register themselves against
 * a GccPeerConnectionWrapper rather than against a GccPeerConnectionNotifier.
 */
public class GccPeerConnectionNotifier {

    private final Set<GccPeerConnectionWrapper.PeerConnectionObserver> peerConnectionObservers = new LinkedHashSet<>();

    public synchronized void addObserver(GccPeerConnectionWrapper.PeerConnectionObserver observer) {
        if (observer == null) {
            throw new IllegalArgumentException("PeerConnectionObserver can not be null");
        }

        peerConnectionObservers.add(observer);
    }

    public synchronized void removeObserver(GccPeerConnectionWrapper.PeerConnectionObserver observer) {
        peerConnectionObservers.remove(observer);
    }

    public synchronized void notifyStreamAdded(MediaStream stream) {
        for (GccPeerConnectionWrapper.PeerConnectionObserver observer : new ArrayList<>(peerConnectionObservers)) {
            observer.onStreamAdded(stream);
        }
    }

    public synchronized void notifyStreamRemoved(MediaStream stream) {
        for (GccPeerConnectionWrapper.PeerConnectionObserver observer : new ArrayList<>(peerConnectionObservers)) {
            observer.onStreamRemoved(stream);
        }
    }

    public synchronized void notifyIceConnectionStateChanged(PeerConnection.IceConnectionState state) {
        for (GccPeerConnectionWrapper.PeerConnectionObserver observer : new ArrayList<>(peerConnectionObservers)) {
            observer.onIceConnectionStateChanged(state);
        }
    }
}
