/*
 * Nextcloud Talk - Android Client
 *
 * SPDX-FileCopyrightText: 2022 Daniel Calviño Sánchez <danxuliu@gmail.com>
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package com.gcc.talk.gccWebrtc;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Helper class to register and notify DataChannelMessageListeners.
 * <p>
 * This class is only meant for internal use by GccPeerConnectionWrapper; listeners must register themselves against
 * a GccPeerConnectionWrapper rather than against a GccDataChannelMessageNotifier.
 */
public class GccDataChannelMessageNotifier {

    public final Set<GccPeerConnectionWrapper.DataChannelMessageListener> dataChannelMessageListeners =
        new LinkedHashSet<>();

    public synchronized void addListener(GccPeerConnectionWrapper.DataChannelMessageListener listener) {
        if (listener == null) {
            throw new IllegalArgumentException("DataChannelMessageListener can not be null");
        }

        dataChannelMessageListeners.add(listener);
    }

    public synchronized void removeListener(GccPeerConnectionWrapper.DataChannelMessageListener listener) {
        dataChannelMessageListeners.remove(listener);
    }

    public synchronized void notifyAudioOn() {
        for (GccPeerConnectionWrapper.DataChannelMessageListener listener : new ArrayList<>(dataChannelMessageListeners)) {
            listener.onAudioOn();
        }
    }

    public synchronized void notifyAudioOff() {
        for (GccPeerConnectionWrapper.DataChannelMessageListener listener : new ArrayList<>(dataChannelMessageListeners)) {
            listener.onAudioOff();
        }
    }

    public synchronized void notifyVideoOn() {
        for (GccPeerConnectionWrapper.DataChannelMessageListener listener : new ArrayList<>(dataChannelMessageListeners)) {
            listener.onVideoOn();
        }
    }

    public synchronized void notifyVideoOff() {
        for (GccPeerConnectionWrapper.DataChannelMessageListener listener : new ArrayList<>(dataChannelMessageListeners)) {
            listener.onVideoOff();
        }
    }

    public synchronized void notifyNickChanged(String nick) {
        for (GccPeerConnectionWrapper.DataChannelMessageListener listener : new ArrayList<>(dataChannelMessageListeners)) {
            listener.onNickChanged(nick);
        }
    }
}
