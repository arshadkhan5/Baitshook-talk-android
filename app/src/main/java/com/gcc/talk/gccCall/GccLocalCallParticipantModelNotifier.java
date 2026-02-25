/*
 * Nextcloud Talk - Android Client
 *
 * SPDX-FileCopyrightText: 2024 Daniel Calviño Sánchez <danxuliu@gmail.com>
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package com.gcc.talk.gccCall;

import android.os.Handler;
import android.os.Looper;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Helper class to register and notify GccLocalCallParticipantModel.Observers.
 * <p>
 * This class is only meant for internal use by GccLocalCallParticipantModel; observers must register themselves against a
 * GccLocalCallParticipantModel rather than against a LocalCallParticipantModelNotifier.
 */
class GccLocalCallParticipantModelNotifier {

    private final List<LocalCallParticipantModelObserverOn> localCallParticipantModelObserversOn = new ArrayList<>();

    /**
     * Helper class to associate a GccLocalCallParticipantModel.Observer with a Handler.
     */
    private static class LocalCallParticipantModelObserverOn {
        public final GccLocalCallParticipantModel.Observer observer;
        public final Handler handler;

        private LocalCallParticipantModelObserverOn(GccLocalCallParticipantModel.Observer observer, Handler handler) {
            this.observer = observer;
            this.handler = handler;
        }
    }

    public synchronized void addObserver(GccLocalCallParticipantModel.Observer observer, Handler handler) {
        if (observer == null) {
            throw new IllegalArgumentException("GccLocalCallParticipantModel.Observer can not be null");
        }

        removeObserver(observer);

        localCallParticipantModelObserversOn.add(new LocalCallParticipantModelObserverOn(observer, handler));
    }

    public synchronized void removeObserver(GccLocalCallParticipantModel.Observer observer) {
        Iterator<LocalCallParticipantModelObserverOn> it = localCallParticipantModelObserversOn.iterator();
        while (it.hasNext()) {
            LocalCallParticipantModelObserverOn observerOn = it.next();

            if (observerOn.observer == observer) {
                it.remove();

                return;
            }
        }
    }

    public synchronized void notifyChange() {
        for (LocalCallParticipantModelObserverOn observerOn : new ArrayList<>(localCallParticipantModelObserversOn)) {
            if (observerOn.handler == null || observerOn.handler.getLooper() == Looper.myLooper()) {
                observerOn.observer.onChange();
            } else {
                observerOn.handler.post(() -> {
                    observerOn.observer.onChange();
                });
            }
        }
    }
}
