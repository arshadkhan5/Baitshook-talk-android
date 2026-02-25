/*
 * Nextcloud Talk - Android Client
 *
 * SPDX-FileCopyrightText: 2021 Andy Scherzinger <info@andy-scherzinger.de>
 * SPDX-FileCopyrightText: 2017-2018 Mario Danic <mario@lovelyhq.com>
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package com.gcc.talk.gccEvents;

public class GccConfigurationChangeEvent {
    public boolean equals(final Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof GccConfigurationChangeEvent)) {
            return false;
        }
        final GccConfigurationChangeEvent other = (GccConfigurationChangeEvent) o;

        return other.canEqual((Object) this);
    }

    protected boolean canEqual(final Object other) {
        return other instanceof GccConfigurationChangeEvent;
    }

    public int hashCode() {
        return 1;
    }

    public String toString() {
        return "GccConfigurationChangeEvent()";
    }
}
