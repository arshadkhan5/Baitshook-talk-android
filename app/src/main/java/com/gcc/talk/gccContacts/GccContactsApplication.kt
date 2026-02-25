/*
 * Nextcloud Talk - Android Client
 *
 * SPDX-FileCopyrightText: 2024 Sowjanya Kota <sowjanya.kch@gmail.com>
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.gcc.talk.gccContacts

import android.app.Application
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.disk.DiskCache
import coil.memory.MemoryCache
import coil.util.DebugLogger
import com.gcc.talk.gccUtils.GccContactUtils

class GccContactsApplication :
    Application(),
    ImageLoaderFactory {
    override fun newImageLoader(): ImageLoader {
        val imageLoader = ImageLoader.Builder(this)
            .memoryCache {
                MemoryCache.Builder(this)
                    .maxSizePercent(GccContactUtils.CACHE_MEMORY_SIZE_PERCENTAGE)
                    .build()
            }
            .diskCache {
                DiskCache.Builder()
                    .maxSizePercent(GccContactUtils.CACHE_DISK_SIZE_PERCENTAGE)
                    .directory(cacheDir)
                    .build()
            }
            .logger(DebugLogger())
            .build()
        return imageLoader
    }
}
