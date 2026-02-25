/*
 * Nextcloud Talk - Android Client
 *
 * SPDX-FileCopyrightText: 2022 Daniel Calviño Sánchez <danxuliu@gmail.com>
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package com.gcc.talk.gccCall;

import com.gcc.talk.gccModels.json.participants.Participant;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Helper class to register and notify GccCallParticipantList.Observers.
 * <p>
 * This class is only meant for internal use by GccCallParticipantList; listeners must register themselves against
 * a GccCallParticipantList rather than against a CallParticipantListNotifier.
 */
class CallParticipantListNotifier {

    private final Set<GccCallParticipantList.Observer> callParticipantListObservers = new LinkedHashSet<>();

    public synchronized void addObserver(GccCallParticipantList.Observer observer) {
        if (observer == null) {
            throw new IllegalArgumentException("GccCallParticipantList.Observer can not be null");
        }

        callParticipantListObservers.add(observer);
    }

    public synchronized void removeObserver(GccCallParticipantList.Observer observer) {
        callParticipantListObservers.remove(observer);
    }

    public synchronized void notifyChanged(Collection<Participant> joined, Collection<Participant> updated,
                                           Collection<Participant> left, Collection<Participant> unchanged) {
        for (GccCallParticipantList.Observer observer : new ArrayList<>(callParticipantListObservers)) {
            observer.onCallParticipantsChanged(joined, updated, left, unchanged);
        }
    }

    public synchronized void notifyCallEndedForAll() {
        for (GccCallParticipantList.Observer observer : new ArrayList<>(callParticipantListObservers)) {
            observer.onCallEndedForAll();
        }
    }
}
