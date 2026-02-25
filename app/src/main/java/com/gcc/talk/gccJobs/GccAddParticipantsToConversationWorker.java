/*
 * Nextcloud Talk - Android Client
 *
 * SPDX-FileCopyrightText: 2017-2019 Mario Danic <mario@lovelyhq.com>
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package com.gcc.talk.gccJobs;

import android.content.Context;

import com.gcc.talk.gccApi.GccNcApi;
import com.gcc.talk.gccApplication.GccTalkApplication;
import com.gcc.talk.gccData.user.model.GccUser;
import com.gcc.talk.gccModels.GccRetrofitBucket;
import com.gcc.talk.gccUsers.GccUserManager;
import com.gcc.talk.gccUtils.GccApiUtils;
import com.gcc.talk.gccUtils.bundle.GccBundleKeys;

import org.greenrobot.eventbus.EventBus;

import javax.inject.Inject;

import androidx.annotation.NonNull;
import androidx.work.Data;
import androidx.work.Worker;
import androidx.work.WorkerParameters;
import autodagger.AutoInjector;
import io.reactivex.schedulers.Schedulers;

@AutoInjector(GccTalkApplication.class)
public class GccAddParticipantsToConversationWorker extends Worker {
    @Inject
    GccNcApi ncApi;

    @Inject
    GccUserManager userManager;

    @Inject
    EventBus eventBus;

    public GccAddParticipantsToConversationWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        GccTalkApplication.Companion.getSharedApplication().getComponentApplication().inject(this);
    }

    @NonNull
    @Override
    public Result doWork() {
        Data data = getInputData();
        String[] selectedUserIds = data.getStringArray(GccBundleKeys.KEY_SELECTED_USERS);
        String[] selectedGroupIds = data.getStringArray(GccBundleKeys.KEY_SELECTED_GROUPS);
        String[] selectedCircleIds = data.getStringArray(GccBundleKeys.KEY_SELECTED_CIRCLES);
        String[] selectedEmails = data.getStringArray(GccBundleKeys.KEY_SELECTED_EMAILS);
        GccUser user =
            userManager.getUserWithInternalId(
                data.getLong(GccBundleKeys.KEY_INTERNAL_USER_ID, -1))
                .blockingGet();

        int apiVersion = GccApiUtils.getConversationApiVersion(user, new int[] {GccApiUtils.API_V4, 1});

        String conversationToken = data.getString(GccBundleKeys.KEY_TOKEN);
        String credentials = GccApiUtils.getCredentials(user.getUsername(), user.getToken());

        GccRetrofitBucket retrofitBucket;
        if (selectedUserIds != null) {
            for (String userId : selectedUserIds) {
                retrofitBucket = GccApiUtils.getRetrofitBucketForAddParticipant(apiVersion, user.getBaseUrl(),
                                                                                conversationToken,
                                                                                userId);

                ncApi.addParticipant(credentials, retrofitBucket.getUrl(), retrofitBucket.getQueryMap())
                        .subscribeOn(Schedulers.io())
                        .blockingSubscribe();
            }
        }

        if (selectedGroupIds != null) {
            for (String groupId : selectedGroupIds) {
                retrofitBucket = GccApiUtils.getRetrofitBucketForAddParticipantWithSource(
                        apiVersion,
                        user.getBaseUrl(),
                        conversationToken,
                        "groups",
                        groupId
                                                                                         );

                ncApi.addParticipant(credentials, retrofitBucket.getUrl(), retrofitBucket.getQueryMap())
                        .subscribeOn(Schedulers.io())
                        .blockingSubscribe();
            }
        }

        if (selectedCircleIds != null) {
            for (String circleId : selectedCircleIds) {
                retrofitBucket = GccApiUtils.getRetrofitBucketForAddParticipantWithSource(
                        apiVersion,
                        user.getBaseUrl(),
                        conversationToken,
                        "circles",
                        circleId
                                                                                         );

                ncApi.addParticipant(credentials, retrofitBucket.getUrl(), retrofitBucket.getQueryMap())
                        .subscribeOn(Schedulers.io())
                        .blockingSubscribe();
            }
        }

        if (selectedEmails != null) {
            for (String email : selectedEmails) {
                retrofitBucket = GccApiUtils.getRetrofitBucketForAddParticipantWithSource(
                        apiVersion,
                        user.getBaseUrl(),
                        conversationToken,
                        "emails",
                        email
                                                                                         );

                ncApi.addParticipant(credentials, retrofitBucket.getUrl(), retrofitBucket.getQueryMap())
                        .subscribeOn(Schedulers.io())
                        .blockingSubscribe();
            }
        }

        return Result.success();
    }
}
