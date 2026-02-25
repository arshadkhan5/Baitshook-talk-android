/*
 * Nextcloud Talk - Android Client
 *
 * SPDX-FileCopyrightText: 2021 Andy Scherzinger <info@andy-scherzinger.de>
 * SPDX-FileCopyrightText: 2021 Marcel Hibbe <dev@mhibbe.de>
 * SPDX-FileCopyrightText: 2017-2019 Mario Danic <mario@lovelyhq.com>
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package com.gcc.talk.gccFilebrowser.models.properties

import android.text.TextUtils
import android.util.Log
import at.bitfire.dav4jvm.Property
import at.bitfire.dav4jvm.PropertyFactory
import at.bitfire.dav4jvm.XmlUtils.readText
import com.gcc.talk.gccFilebrowser.webdav.GccDavUtils
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserException
import java.io.IOException

class GccNCPermission private constructor(var ncPermission: String?) : Property {

    class Factory : PropertyFactory {
        override fun create(parser: XmlPullParser): Property {
            try {
                val text = readText(parser)
                if (!TextUtils.isEmpty(text)) {
                    return GccNCPermission(text)
                }
            } catch (e: IOException) {
                Log.e("GccNCPermission", "failed to create property", e)
            } catch (e: XmlPullParserException) {
                Log.e("GccNCPermission", "failed to create property", e)
            }
            return GccNCPermission("")
        }

        override fun getName(): Property.Name = NAME
    }

    companion object {
        @JvmField
        val NAME: Property.Name = Property.Name(GccDavUtils.OC_NAMESPACE, GccDavUtils.EXTENDED_PROPERTY_NAME_PERMISSIONS)
    }
}
