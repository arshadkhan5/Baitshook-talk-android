/*
 * Nextcloud Talk - Android Client
 *
 * SPDX-FileCopyrightText: 2022 Andy Scherzinger <info@andy-scherzinger.de>
 * SPDX-FileCopyrightText: 2022 Marcel Hibbe <dev@mhibbe.de>
 * SPDX-FileCopyrightText: 2017 Mario Danic <mario@lovelyhq.com>
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package com.gcc.talk.gccJobs;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Data;
import androidx.work.Worker;
import androidx.work.WorkerParameters;
import autodagger.AutoInjector;
import okhttp3.CookieJar;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;

import com.gcc.talk.gccApi.GccNcApi;
import com.gcc.talk.gccApplication.GccTalkApplication;
import com.gcc.talk.utils.ClosedInterfaceImpl;
import com.gcc.talk.gccUtils.GccPushUtils;

import javax.inject.Inject;

@AutoInjector(GccTalkApplication.class)
public class GccPushRegistrationWorker extends Worker {
    public static final String TAG = "GccPushRegistrationWorker";
    public static final String ORIGIN = "origin";

    @Inject
    Retrofit retrofit;

    @Inject
    OkHttpClient okHttpClient;

    public GccPushRegistrationWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        GccTalkApplication.Companion.getSharedApplication().getComponentApplication().inject(this);
        if (new ClosedInterfaceImpl().isGooglePlayServicesAvailable()) {
            Data data = getInputData();
            String origin = data.getString("origin");
            Log.d(TAG, "GccPushRegistrationWorker called via " + origin);

            GccNcApi ncApi = retrofit
                .newBuilder()
                .client(okHttpClient
                            .newBuilder()
                            .cookieJar(CookieJar.NO_COOKIES)
                            .build())
                .build()
                .create(GccNcApi.class);

            GccPushUtils pushUtils = new GccPushUtils();
            pushUtils.generateRsa2048KeyPair();
            pushUtils.pushRegistrationToServer(ncApi);

            return Result.success();
        }
        Log.w(TAG, "executing GccPushRegistrationWorker doesn't make sense because Google Play Services are not " +
            "available");
        return Result.failure();
    }
}
