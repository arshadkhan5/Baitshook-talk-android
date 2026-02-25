/*
 * Nextcloud Talk - Android Client
 *
 * SPDX-FileCopyrightText: 2025 Marcel Hibbe <dev@mhibbe.de>
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.gcc.talk.gccConversationinfo.viewmodel

import org.junit.Test
import org.junit.Assert.assertEquals

class ConversationInfoViewModelTest {

    @Test
    fun `createConversationNameByParticipants should combine names correctly`() {
        val original = listOf("Dave", null, "Charlie")
        val all = listOf("Bob", "Charlie", "Dave", "Alice", null, "Simon")

        val expectedName = "Charlie, Dave, Alice, Bob, Simon"
        val result = GccConversationInfoViewModel.createConversationNameByParticipants(original, all)

        assertEquals(expectedName, result)
    }
}
