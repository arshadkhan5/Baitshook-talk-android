/*
 * Nextcloud Talk - Android Client
 *
 * SPDX-FileCopyrightText: 2022 Andy Scherzinger <info@andy-scherzinger.de>
 * SPDX-FileCopyrightText: 2017-2018 Mario Danic <mario@lovelyhq.com>
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package com.gcc.talk.gccJobs;

import android.content.Context;
import android.util.Log;

import com.gcc.talk.gccApi.GccNcApi;
import com.gcc.talk.gccApplication.GccTalkApplication;
import com.gcc.talk.gccData.user.model.GccUser;
import com.gcc.talk.gccEvents.GccEventStatus;
import com.gcc.talk.gccModels.json.capabilities.CapabilitiesOverall;
import com.gcc.talk.gccUsers.GccUserManager;
import com.gcc.talk.gccUtils.GccApiUtils;
import com.gcc.talk.gccUtils.GccUserIdUtils;
import com.gcc.talk.gccUtils.bundle.GccBundleKeys;

import org.greenrobot.eventbus.EventBus;

import java.net.CookieManager;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import androidx.annotation.NonNull;
import androidx.work.Data;
import androidx.work.Worker;
import androidx.work.WorkerParameters;
import autodagger.AutoInjector;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import okhttp3.JavaNetCookieJar;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;

@AutoInjector(GccTalkApplication.class)
public class GccCapabilitiesWorker extends Worker {
    public static final String TAG = "GccCapabilitiesWorker";
    public static final long NO_ID = -1;

    @Inject
    GccUserManager userManager;

    @Inject
    Retrofit retrofit;

    @Inject
    EventBus eventBus;

    @Inject
    OkHttpClient okHttpClient;

    GccNcApi ncApi;

    public GccCapabilitiesWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    private void updateUser(CapabilitiesOverall capabilitiesOverall, GccUser user) {
        if (capabilitiesOverall.getOcs() != null && capabilitiesOverall.getOcs().getData() != null &&
            capabilitiesOverall.getOcs().getData().getCapabilities() != null) {

            user.setCapabilities(capabilitiesOverall.getOcs().getData().getCapabilities());
            user.setServerVersion(capabilitiesOverall.getOcs().getData().getServerVersion());

            try {
                int rowsCount = userManager.updateOrCreateUser(user).blockingGet();
                if (rowsCount > 0) {
                    eventBus.post(new GccEventStatus(GccUserIdUtils.INSTANCE.getIdForUser(user),
                                                     GccEventStatus.EventType.CAPABILITIES_FETCH,
                                                     true));
                } else {
                    Log.w(TAG, "Error updating user");
                    eventBus.post(new GccEventStatus(GccUserIdUtils.INSTANCE.getIdForUser(user),
                                                     GccEventStatus.EventType.CAPABILITIES_FETCH,
                                                     false));
                }
            } catch (Exception e) {
                Log.e(TAG, "Error updating user", e);
                eventBus.post(new GccEventStatus(GccUserIdUtils.INSTANCE.getIdForUser(user),
                                                 GccEventStatus.EventType.CAPABILITIES_FETCH,
                                                 false));
            }
        }
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

            ncApi = retrofit
                .newBuilder()
                .client(okHttpClient
                            .newBuilder()
                            .cookieJar(new JavaNetCookieJar(new CookieManager()))
                            .build())
                .build()
                .create(GccNcApi.class);

            String url = "";
            String baseurl = user.getBaseUrl();
            if (baseurl != null) {
                url = GccApiUtils.getUrlForCapabilities(baseurl);
            }

            ncApi.getCapabilities(GccApiUtils.getCredentials(user.getUsername(), user.getToken()), url)
                .retry(3)
                .blockingSubscribe(new Observer<CapabilitiesOverall>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        // unused atm
                    }

                    @Override
                    public void onNext(CapabilitiesOverall capabilitiesOverall) {
                        updateUser(capabilitiesOverall, user);
                    }

                    @Override
                    public void onError(Throwable e) {
                        eventBus.post(new GccEventStatus(user.getId(),
                                                         GccEventStatus.EventType.CAPABILITIES_FETCH,
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
