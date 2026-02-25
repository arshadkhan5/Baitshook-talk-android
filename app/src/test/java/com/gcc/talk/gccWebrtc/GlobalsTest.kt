/*
 * Nextcloud Talk - Android Client
 *
 * SPDX-FileCopyrightText: 2023 Samanwith KSN <samanwith21@gmail.com>
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package com.gcc.talk.gccWebrtc

import org.junit.Assert
import org.junit.Test

class GlobalsTest {
    @Test
    fun testRoomToken() {
        Assert.assertEquals("roomToken", GccGlobals.ROOM_TOKEN)
    }

    @Test
    fun testTargetParticipants() {
        Assert.assertEquals("participants", GccGlobals.TARGET_PARTICIPANTS)
    }

    @Test
    fun testTargetRoom() {
        Assert.assertEquals("room", GccGlobals.TARGET_ROOM)
    }
}
