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
import com.gcc.talk.gccModels.GccExternalSignalingServer;
import com.gcc.talk.gccModels.json.signaling.settings.SignalingSettingsOverall;
import com.gcc.talk.gccUsers.GccUserManager;
import com.gcc.talk.gccUtils.GccApiUtils;
import com.gcc.talk.gccUtils.GccUserIdUtils;
import com.gcc.talk.gccUtils.bundle.GccBundleKeys;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import androidx.annotation.NonNull;
import androidx.work.Data;
import androidx.work.Worker;
import androidx.work.WorkerParameters;
import autodagger.AutoInjector;
import io.reactivex.Observer;
import io.reactivex.SingleObserver;
import io.reactivex.disposables.Disposable;

@AutoInjector(GccTalkApplication.class)
public class GccSignalingSettingsWorker extends Worker {

    @Inject
    GccUserManager userManager;

    @Inject
    GccNcApi ncApi;

    @Inject
    EventBus eventBus;

    public GccSignalingSettingsWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        GccTalkApplication.Companion.getSharedApplication().getComponentApplication().inject(this);

        Data data = getInputData();

        long internalUserId = data.getLong(GccBundleKeys.KEY_INTERNAL_USER_ID, -1);

        List<GccUser> userEntityObjectList = new ArrayList<>();
        boolean userNotFound = userManager.getUserWithInternalId(internalUserId).isEmpty().blockingGet();

        if (internalUserId == -1 || userNotFound) {
            userEntityObjectList = userManager.getUsers().blockingGet();
        } else {
            userEntityObjectList.add(userManager.getUserWithInternalId(internalUserId).blockingGet());
        }

        for (GccUser user : userEntityObjectList) {

            int apiVersion = GccApiUtils.getSignalingApiVersion(user, new int[] {GccApiUtils.API_V3, 2, 1});

            ncApi.getSignalingSettings(
                            GccApiUtils.getCredentials(user.getUsername(), user.getToken()),
                            GccApiUtils.getUrlForSignalingSettings(apiVersion, user.getBaseUrl()))
                .blockingSubscribe(new Observer<SignalingSettingsOverall>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        // unused stm
                    }

                    @Override
                    public void onNext(SignalingSettingsOverall signalingSettingsOverall) {
                        GccExternalSignalingServer externalSignalingServer;
                        externalSignalingServer = new GccExternalSignalingServer();

                        if (signalingSettingsOverall.getOcs() != null &&
                            signalingSettingsOverall.getOcs().getSettings() != null) {
                            externalSignalingServer.setExternalSignalingServer(signalingSettingsOverall
                                                                                   .getOcs()
                                                                                   .getSettings()
                                                                                   .getExternalSignalingServer());
                            externalSignalingServer.setExternalSignalingTicket(signalingSettingsOverall
                                                                                   .getOcs()
                                                                                   .getSettings()
                                                                                   .getExternalSignalingTicket());
                        }

                        user.setExternalSignalingServer(externalSignalingServer);

                        userManager.saveUser(user).subscribe(new SingleObserver<Integer>() {
                            @Override
                            public void onSubscribe(Disposable d) {
                                // unused atm
                            }

                            @Override
                            public void onSuccess(Integer rows) {
                                if (rows > 0) {
                                    eventBus.post(new GccEventStatus(GccUserIdUtils.INSTANCE.getIdForUser(user),
                                                                     GccEventStatus.EventType.SIGNALING_SETTINGS,
                                                                     true));
                                } else {
                                    eventBus.post(new GccEventStatus(GccUserIdUtils.INSTANCE.getIdForUser(user),
                                                                     GccEventStatus.EventType.SIGNALING_SETTINGS,
                                                                     false));
                                }
                            }

                            @Override
                            public void onError(Throwable e) {
                                eventBus.post(new GccEventStatus(GccUserIdUtils.INSTANCE.getIdForUser(user),
                                                                 GccEventStatus.EventType.SIGNALING_SETTINGS,
                                                                 false));
                            }
                        });
                    }

                    @Override
                    public void onError(Throwable e) {
                        eventBus.post(new GccEventStatus(GccUserIdUtils.INSTANCE.getIdForUser(user),
                                                         GccEventStatus.EventType.SIGNALING_SETTINGS,
                                                         false));
                    }

                    @Override
                    public void onComplete() {
                        // unused atm
                    }
                });
        }

        return Result.success();
    }
}
