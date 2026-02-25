/*
 * Nextcloud Talk - Android Client
 *
 * SPDX-FileCopyrightText: 2017-2019 Mario Danic <mario@lovelyhq.com>
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package com.gcc.talk.gccEvents;

public class GccNetworkEvent {
    private final NetworkConnectionEvent networkConnectionEvent;

    public GccNetworkEvent(NetworkConnectionEvent networkConnectionEvent) {
        this.networkConnectionEvent = networkConnectionEvent;
    }

    public NetworkConnectionEvent getNetworkConnectionEvent() {
        return this.networkConnectionEvent;
    }

    public boolean equals(final Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof GccNetworkEvent)) {
            return false;
        }
        final GccNetworkEvent other = (GccNetworkEvent) o;
        if (!other.canEqual((Object) this)) {
            return false;
        }
        final Object this$networkConnectionEvent = this.getNetworkConnectionEvent();
        final Object other$networkConnectionEvent = other.getNetworkConnectionEvent();

        return this$networkConnectionEvent == null ? other$networkConnectionEvent == null : this$networkConnectionEvent.equals(other$networkConnectionEvent);
    }

    protected boolean canEqual(final Object other) {
        return other instanceof GccNetworkEvent;
    }

    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        final Object $networkConnectionEvent = this.getNetworkConnectionEvent();
        return result * PRIME + ($networkConnectionEvent == null ? 43 : $networkConnectionEvent.hashCode());
    }

    public String toString() {
        return "GccNetworkEvent(networkConnectionEvent=" + this.getNetworkConnectionEvent() + ")";
    }

    public enum NetworkConnectionEvent {
        NETWORK_CONNECTED, NETWORK_DISCONNECTED
    }
}
