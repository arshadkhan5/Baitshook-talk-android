/*
 * Nextcloud Talk - Android Client
 *
 * SPDX-FileCopyrightText: 2022 Daniel Calviño Sánchez <danxuliu@gmail.com>
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package com.gcc.talk.gccSignaling;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Helper class to register and notify OfferMessageListeners.
 * <p>
 * This class is only meant for internal use by GccSignalingMessageReceiver; listeners must register themselves against
 * a GccSignalingMessageReceiver rather than against an OfferMessageNotifier.
 */
class GccOfferMessageNotifier {

    private final Set<GccSignalingMessageReceiver.OfferMessageListener> offerMessageListeners = new LinkedHashSet<>();

    public synchronized void addListener(GccSignalingMessageReceiver.OfferMessageListener listener) {
        if (listener == null) {
            throw new IllegalArgumentException("OfferMessageListener can not be null");
        }

        offerMessageListeners.add(listener);
    }

    public synchronized void removeListener(GccSignalingMessageReceiver.OfferMessageListener listener) {
        offerMessageListeners.remove(listener);
    }

    public synchronized void notifyOffer(String sessionId, String roomType, String sdp, String nick) {
        for (GccSignalingMessageReceiver.OfferMessageListener listener : new ArrayList<>(offerMessageListeners)) {
            listener.onOffer(sessionId, roomType, sdp, nick);
        }
    }
}
