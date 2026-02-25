/*
 * Nextcloud Talk - Android Client
 *
 * SPDX-FileCopyrightText: 2022 Andy Scherzinger <info@andy-scherzinger.de>
 * SPDX-FileCopyrightText: 2017-2018 Mario Danic <mario@lovelyhq.com>
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package com.gcc.talk.gccJobs;

import android.annotation.SuppressLint;
import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.gcc.talk.gccApplication.GccTalkApplication;
import com.gcc.talk.gccData.user.model.GccUser;
import com.gcc.talk.gccUsers.GccUserManager;
import com.gcc.talk.gccWebrtc.GccWebSocketConnectionHelper;


import java.util.List;

import javax.inject.Inject;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;
import autodagger.AutoInjector;

@AutoInjector(GccTalkApplication.class)
public class GccWebsocketConnectionsWorker extends Worker {

    public static final String TAG = "GccWebsocketConnectionsWorker";

    @Inject
    GccUserManager userManager;

    public GccWebsocketConnectionsWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @SuppressLint("LongLogTag")
    @NonNull
    @Override
    public Result doWork() {
        Log.d(TAG, "GccWebsocketConnectionsWorker started ");

        GccTalkApplication.Companion.getSharedApplication().getComponentApplication().inject(this);

        List<GccUser> users = userManager.getUsers().blockingGet();
        for (GccUser user : users) {
            if (user.getExternalSignalingServer() != null &&
                user.getExternalSignalingServer().getExternalSignalingServer() != null &&
                !TextUtils.isEmpty(user.getExternalSignalingServer().getExternalSignalingServer()) &&
                !TextUtils.isEmpty(user.getExternalSignalingServer().getExternalSignalingTicket())) {

                Log.d(TAG, "trying to getExternalSignalingInstanceForServer for user " + user.getDisplayName());

                GccWebSocketConnectionHelper.getExternalSignalingInstanceForServer(
                    user.getExternalSignalingServer().getExternalSignalingServer(),
                    user,
                    user.getExternalSignalingServer().getExternalSignalingTicket(),
                    false);
            } else {
                Log.d(TAG, "skipped to getExternalSignalingInstanceForServer for user " + user.getDisplayName());
            }
        }

        return Result.success();
    }
}
