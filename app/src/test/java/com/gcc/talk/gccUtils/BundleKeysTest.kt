/*
 * Nextcloud Talk - Android Client
 *
 * SPDX-FileCopyrightText: 2023 Samanwith KSN <samanwith21@gmail.com>
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package com.gcc.talk.gccUtils

import com.gcc.talk.gccUtils.bundle.GccBundleKeys
import junit.framework.TestCase.assertEquals
import org.junit.Test
class BundleKeysTest {

    @Test
    fun testBundleKeysValues() {
        assertEquals("KEY_SELECTED_USERS", GccBundleKeys.KEY_SELECTED_USERS)
        assertEquals("KEY_SELECTED_GROUPS", GccBundleKeys.KEY_SELECTED_GROUPS)
        assertEquals("KEY_SELECTED_CIRCLES", GccBundleKeys.KEY_SELECTED_CIRCLES)
        assertEquals("KEY_SELECTED_EMAILS", GccBundleKeys.KEY_SELECTED_EMAILS)
        assertEquals("KEY_USERNAME", GccBundleKeys.KEY_USERNAME)
        assertEquals("KEY_TOKEN", GccBundleKeys.KEY_TOKEN)
        assertEquals("KEY_TRANSLATE_MESSAGE", GccBundleKeys.KEY_TRANSLATE_MESSAGE)
        assertEquals("KEY_BASE_URL", GccBundleKeys.KEY_BASE_URL)
        assertEquals("KEY_IS_ACCOUNT_IMPORT", GccBundleKeys.KEY_IS_ACCOUNT_IMPORT)
        assertEquals("KEY_ORIGINAL_PROTOCOL", GccBundleKeys.KEY_ORIGINAL_PROTOCOL)
        assertEquals("KEY_OPERATION_CODE", GccBundleKeys.KEY_OPERATION_CODE)
        assertEquals("KEY_APP_ITEM_PACKAGE_NAME", GccBundleKeys.KEY_APP_ITEM_PACKAGE_NAME)
        assertEquals("KEY_APP_ITEM_NAME", GccBundleKeys.KEY_APP_ITEM_NAME)
        assertEquals("KEY_CONVERSATION_PASSWORD", GccBundleKeys.KEY_CONVERSATION_PASSWORD)
        assertEquals("KEY_ROOM_TOKEN", GccBundleKeys.KEY_ROOM_TOKEN)
        assertEquals("KEY_ROOM_ONE_TO_ONE", GccBundleKeys.KEY_ROOM_ONE_TO_ONE)
        assertEquals("KEY_NEW_CONVERSATION", GccBundleKeys.KEY_NEW_CONVERSATION)
        assertEquals("KEY_ADD_PARTICIPANTS", GccBundleKeys.KEY_ADD_PARTICIPANTS)
        assertEquals("KEY_EXISTING_PARTICIPANTS", GccBundleKeys.KEY_EXISTING_PARTICIPANTS)
        assertEquals("KEY_CALL_URL", GccBundleKeys.KEY_CALL_URL)
        assertEquals("KEY_NEW_ROOM_NAME", GccBundleKeys.KEY_NEW_ROOM_NAME)
        assertEquals("KEY_MODIFIED_BASE_URL", GccBundleKeys.KEY_MODIFIED_BASE_URL)
        assertEquals("KEY_NOTIFICATION_SUBJECT", GccBundleKeys.KEY_NOTIFICATION_SUBJECT)
        assertEquals("KEY_NOTIFICATION_SIGNATURE", GccBundleKeys.KEY_NOTIFICATION_SIGNATURE)
        assertEquals("KEY_INTERNAL_USER_ID", GccBundleKeys.KEY_INTERNAL_USER_ID)
        assertEquals("KEY_CONVERSATION_TYPE", GccBundleKeys.KEY_CONVERSATION_TYPE)
        assertEquals("KEY_INVITED_PARTICIPANTS", GccBundleKeys.KEY_INVITED_PARTICIPANTS)
        assertEquals("KEY_INVITED_CIRCLE", GccBundleKeys.KEY_INVITED_CIRCLE)
        assertEquals("KEY_INVITED_GROUP", GccBundleKeys.KEY_INVITED_GROUP)
        assertEquals("KEY_INVITED_EMAIL", GccBundleKeys.KEY_INVITED_EMAIL)
    }

    @Test
    fun testBundleKeysValues2() {
        assertEquals("KEY_CONVERSATION_NAME", GccBundleKeys.KEY_CONVERSATION_NAME)
        assertEquals("KEY_RECORDING_STATE", GccBundleKeys.KEY_RECORDING_STATE)
        assertEquals("KEY_CALL_VOICE_ONLY", GccBundleKeys.KEY_CALL_VOICE_ONLY)
        assertEquals("KEY_CALL_WITHOUT_NOTIFICATION", GccBundleKeys.KEY_CALL_WITHOUT_NOTIFICATION)
        assertEquals("KEY_FROM_NOTIFICATION_START_CALL", GccBundleKeys.KEY_FROM_NOTIFICATION_START_CALL)
        assertEquals("KEY_ROOM_ID", GccBundleKeys.KEY_ROOM_ID)
        assertEquals("KEY_ARE_CALL_SOUNDS", GccBundleKeys.KEY_ARE_CALL_SOUNDS)
        assertEquals("KEY_FILE_PATHS", GccBundleKeys.KEY_FILE_PATHS)
        assertEquals("KEY_ACCOUNT", GccBundleKeys.KEY_ACCOUNT)
        assertEquals("KEY_FILE_ID", GccBundleKeys.KEY_FILE_ID)
        assertEquals("KEY_NOTIFICATION_ID", GccBundleKeys.KEY_NOTIFICATION_ID)
        assertEquals("KEY_NOTIFICATION_TIMESTAMP", GccBundleKeys.KEY_NOTIFICATION_TIMESTAMP)
        assertEquals("KEY_SHARED_TEXT", GccBundleKeys.KEY_SHARED_TEXT)
        assertEquals("KEY_GEOCODING_QUERY", GccBundleKeys.KEY_GEOCODING_QUERY)
        assertEquals("KEY_META_DATA", GccBundleKeys.KEY_META_DATA)
        assertEquals("KEY_FORWARD_MSG_FLAG", GccBundleKeys.KEY_FORWARD_MSG_FLAG)
        assertEquals("KEY_FORWARD_MSG_TEXT", GccBundleKeys.KEY_FORWARD_MSG_TEXT)
        assertEquals("KEY_FORWARD_HIDE_SOURCE_ROOM", GccBundleKeys.KEY_FORWARD_HIDE_SOURCE_ROOM)
        assertEquals("KEY_SYSTEM_NOTIFICATION_ID", GccBundleKeys.KEY_SYSTEM_NOTIFICATION_ID)
        assertEquals("KEY_MESSAGE_ID", GccBundleKeys.KEY_MESSAGE_ID)
        assertEquals("KEY_MIME_TYPE_FILTER", GccBundleKeys.KEY_MIME_TYPE_FILTER)
        assertEquals(
            "KEY_PARTICIPANT_PERMISSION_CAN_PUBLISH_AUDIO",
            GccBundleKeys.KEY_PARTICIPANT_PERMISSION_CAN_PUBLISH_AUDIO
        )
        assertEquals(
            "KEY_PARTICIPANT_PERMISSION_CAN_PUBLISH_VIDEO",
            GccBundleKeys.KEY_PARTICIPANT_PERMISSION_CAN_PUBLISH_VIDEO
        )
        assertEquals("KEY_IS_MODERATOR", GccBundleKeys.KEY_IS_MODERATOR)
        assertEquals("KEY_SWITCH_TO_ROOM", GccBundleKeys.KEY_SWITCH_TO_ROOM)
        assertEquals("KEY_START_CALL_AFTER_ROOM_SWITCH", GccBundleKeys.KEY_START_CALL_AFTER_ROOM_SWITCH)
        assertEquals("KEY_IS_BREAKOUT_ROOM", GccBundleKeys.KEY_IS_BREAKOUT_ROOM)
        assertEquals("KEY_NOTIFICATION_RESTRICT_DELETION", GccBundleKeys.KEY_NOTIFICATION_RESTRICT_DELETION)
        assertEquals("KEY_DISMISS_RECORDING_URL", GccBundleKeys.KEY_DISMISS_RECORDING_URL)
        assertEquals("KEY_SHARE_RECORDING_TO_CHAT_URL", GccBundleKeys.KEY_SHARE_RECORDING_TO_CHAT_URL)
        assertEquals("KEY_GEOCODING_RESULT", GccBundleKeys.KEY_GEOCODING_RESULT)
        assertEquals("ADD_ADDITIONAL_ACCOUNT", GccBundleKeys.ADD_ADDITIONAL_ACCOUNT)
        assertEquals("SAVED_TRANSLATED_MESSAGE", GccBundleKeys.SAVED_TRANSLATED_MESSAGE)
    }
}
