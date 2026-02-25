/*
 * Nextcloud Talk - Android Client
 *
 * SPDX-FileCopyrightText: 2023 Samanwith KSN <samanwith21@gmail.com>
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package com.gcc.talk.gccUtils

import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertTrue
import org.junit.Test

class UriUtilsIT {

    @Test
    fun testHasHttpProtocolPrefixed() {
        val uriHttp = "http://www.example.com"
        val resultHttp = GccUriUtils.hasHttpProtocolPrefixed(uriHttp)
        assertTrue(resultHttp)

        val uriHttps = "https://www.example.com"
        val resultHttps = GccUriUtils.hasHttpProtocolPrefixed(uriHttps)
        assertTrue(resultHttps)

        val uriWithoutPrefix = "www.example.com"
        val resultWithoutPrefix = GccUriUtils.hasHttpProtocolPrefixed(uriWithoutPrefix)
        assertFalse(resultWithoutPrefix)
    }

    @Test
    fun testExtractInstanceInternalFileFileId() {
        assertEquals(
            "42",
            GccUriUtils.extractInstanceInternalFileFileId(
                "https://cloud.nextcloud.com/apps/files/?dir=/Engineering&fileid=42"
            )
        )
    }

    @Test
    fun testExtractInstanceInternalFileShareFileId() {
        assertEquals(
            "42",
            GccUriUtils.extractInstanceInternalFileShareFileId("https://cloud.nextcloud.com/f/42")
        )
    }

    @Test
    fun testIsInstanceInternalFileShareUrl() {
        assertTrue(
            GccUriUtils.isInstanceInternalFileShareUrl(
                "https://cloud.nextcloud.com",
                "https://cloud.nextcloud.com/f/42"
            )
        )

        assertFalse(
            GccUriUtils.isInstanceInternalFileShareUrl(
                "https://nextcloud.nextcloud.com",
                "https://cloud.nextcloud.com/f/42"
            )
        )

        assertFalse(
            GccUriUtils.isInstanceInternalFileShareUrl(
                "https://nextcloud.nextcloud.com",
                "https://cloud.nextcloud.com/f/"
            )
        )

        assertFalse(
            GccUriUtils.isInstanceInternalFileShareUrl(
                "https://nextcloud.nextcloud.com",
                "https://cloud.nextcloud.com/f/test123"
            )
        )
    }

    @Test
    fun testIsInstanceInternalFileUrl() {
        assertTrue(
            GccUriUtils.isInstanceInternalFileUrl(
                "https://cloud.nextcloud.com",
                "https://cloud.nextcloud.com/apps/files/?dir=/Engineering&fileid=41"
            )
        )

        assertFalse(
            GccUriUtils.isInstanceInternalFileUrl(
                "https://nextcloud.nextcloud.com",
                "https://cloud.nextcloud.com/apps/files/?dir=/Engineering&fileid=41"
            )
        )

        assertFalse(
            GccUriUtils.isInstanceInternalFileUrl(
                "https://nextcloud.nextcloud.com",
                "https://cloud.nextcloud.com/apps/files/?dir=/Engineering&fileid=test123"
            )
        )

        assertFalse(
            GccUriUtils.isInstanceInternalFileUrl(
                "https://nextcloud.nextcloud.com",
                "https://cloud.nextcloud.com/apps/files/?dir=/Engineering&fileid="
            )
        )

        assertFalse(
            GccUriUtils.isInstanceInternalFileUrl(
                "https://cloud.nextcloud.com",
                "https://cloud.nextcloud.com/apps/files/?dir=/Engineering"
            )
        )
    }

    @Test
    fun testIsInstanceInternalFileUrlNew() {
        assertTrue(
            GccUriUtils.isInstanceInternalFileUrlNew(
                "https://cloud.nextcloud.com",
                "https://cloud.nextcloud.com/apps/files/files/41?dir=/"
            )
        )

        assertFalse(
            GccUriUtils.isInstanceInternalFileUrlNew(
                "https://nextcloud.nextcloud.com",
                "https://cloud.nextcloud.com/apps/files/files/41?dir=/"
            )
        )
    }

    @Test
    fun testExtractInstanceInternalFileFileIdNew() {
        assertEquals(
            "42",
            GccUriUtils.extractInstanceInternalFileFileIdNew("https://cloud.nextcloud.com/apps/files/files/42?dir=/")
        )
    }
}
