/*
 * Nextcloud Talk - Android Client
 *
 * SPDX-FileCopyrightText: 2022 Andy Scherzinger <info@andy-scherzinger.de>
 * SPDX-FileCopyrightText: 2017-2018 Mario Danic <mario@lovelyhq.com>
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package com.gcc.talk.gccJobs;

import android.content.Context;

import com.gcc.talk.gccApi.GccNcApi;
import com.gcc.talk.gccApplication.GccTalkApplication;
import com.gcc.talk.gccData.user.model.GccUser;
import com.gcc.talk.gccEvents.GccEventStatus;
import com.gcc.talk.gccModels.json.generic.GenericOverall;
import com.gcc.talk.gccUsers.GccUserManager;
import com.gcc.talk.gccUtils.GccApiUtils;
import com.gcc.talk.gccUtils.GccUserIdUtils;
import com.gcc.talk.gccUtils.bundle.GccBundleKeys;

import org.greenrobot.eventbus.EventBus;

import java.net.CookieManager;

import javax.inject.Inject;

import androidx.annotation.NonNull;
import androidx.work.Data;
import androidx.work.Worker;
import androidx.work.WorkerParameters;
import autodagger.AutoInjector;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import okhttp3.JavaNetCookieJar;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;

@AutoInjector(GccTalkApplication.class)
public class GccDeleteConversationWorker extends Worker {
    @Inject
    Retrofit retrofit;

    @Inject
    OkHttpClient okHttpClient;

    @Inject
    GccUserManager userManager;

    @Inject
    EventBus eventBus;

    GccNcApi ncApi;

    public GccDeleteConversationWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        GccTalkApplication.Companion.getSharedApplication().getComponentApplication().inject(this);
    }

    @NonNull
    @Override
    public Result doWork() {
        Data data = getInputData();
        long operationUserId = data.getLong(GccBundleKeys.KEY_INTERNAL_USER_ID, -1);
        String conversationToken = data.getString(GccBundleKeys.KEY_ROOM_TOKEN);
        GccUser operationUser = userManager.getUserWithId(operationUserId).blockingGet();

        if (operationUser != null) {
            int apiVersion = GccApiUtils.getConversationApiVersion(operationUser, new int[]{GccApiUtils.API_V4, 1});

            String credentials = GccApiUtils.getCredentials(operationUser.getUsername(), operationUser.getToken());
            ncApi = retrofit
                .newBuilder()
                .client(okHttpClient.newBuilder().cookieJar(new JavaNetCookieJar(new CookieManager())).build())
                .build()
                .create(GccNcApi.class);

            GccEventStatus eventStatus = new GccEventStatus(GccUserIdUtils.INSTANCE.getIdForUser(operationUser),
                                                            GccEventStatus.EventType.CONVERSATION_UPDATE,
                                                            true);

            ncApi.deleteRoom(credentials, GccApiUtils.getUrlForRoom(apiVersion, operationUser.getBaseUrl(),
                                                                    conversationToken))
                .subscribeOn(Schedulers.io())
                .blockingSubscribe(new Observer<GenericOverall>() {
                    Disposable disposable;

                    @Override
                    public void onSubscribe(Disposable d) {
                        disposable = d;
                    }

                    @Override
                    public void onNext(GenericOverall genericOverall) {
                        eventBus.postSticky(eventStatus);
                    }

                    @Override
                    public void onError(Throwable e) {
                        // unused atm
                    }

                    @Override
                    public void onComplete() {
                        disposable.dispose();
                    }
                });
        }

        return Result.success();
    }
}
