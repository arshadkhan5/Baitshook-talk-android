/*
 * Nextcloud Talk - Android Client
 *
 * SPDX-FileCopyrightText: 2020 Tobias Kaminsky <tobias@kaminsky.me>
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package com.gcc.talk.gccUtils;


import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

public class GccSyncService extends Service {

    private static final Object sSyncAdapterLock = new Object();

    private static GccSyncAdapter sSyncAdapter = null;

    @Override
    public void onCreate() {
        Log.i("GccSyncService", "Sync service created");
        synchronized (sSyncAdapterLock) {
            if (sSyncAdapter == null) {
                sSyncAdapter = new GccSyncAdapter(getApplicationContext(), true);
            }
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.i("GccSyncService", "Sync service binded");
        return sSyncAdapter.getSyncAdapterBinder();
    }
}
