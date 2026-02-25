/*
 * Nextcloud Talk - Android Client
 *
 * SPDX-FileCopyrightText: 2022 Andy Scherzinger <info@andy-scherzinger.de>
 * SPDX-FileCopyrightText: 2017 Mario Danic <mario@lovelyhq.com>
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package com.gcc.talk.gccJobs;

import android.app.NotificationManager;
import android.content.Context;
import android.util.Log;

import com.gcc.talk.R;
import com.gcc.talk.gccApi.GccNcApi;
import com.gcc.talk.gccApplication.GccTalkApplication;
import com.gcc.talk.gccArbitrarystorage.GccArbitraryStorageManager;
import com.gcc.talk.gccData.database.dao.GccChatBlocksDao;
import com.gcc.talk.gccData.database.dao.GccChatMessagesDao;
import com.gcc.talk.gccData.database.dao.GccConversationsDao;
import com.gcc.talk.gccData.user.model.GccUser;
import com.gcc.talk.gccModels.json.generic.GenericMeta;
import com.gcc.talk.gccModels.json.generic.GenericOverall;
import com.gcc.talk.gccModels.json.push.PushConfigurationState;
import com.gcc.talk.gccUsers.GccUserManager;
import com.gcc.talk.gccUtils.GccApiUtils;
import com.gcc.talk.gccUtils.preferences.GccAppPreferences;
import com.gcc.talk.gccWebrtc.GccWebSocketConnectionHelper;

import java.net.CookieManager;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.zip.CRC32;

import javax.inject.Inject;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;
import autodagger.AutoInjector;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import okhttp3.JavaNetCookieJar;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;

@AutoInjector(GccTalkApplication.class)
public class GccAccountRemovalWorker extends Worker {
    public static final String TAG = "GccAccountRemovalWorker";

    @Inject GccUserManager userManager;

    @Inject GccArbitraryStorageManager arbitraryStorageManager;

    @Inject GccAppPreferences appPreferences;

    @Inject Retrofit retrofit;

    @Inject OkHttpClient okHttpClient;

    @Inject GccChatMessagesDao chatMessagesDao;

    @Inject GccConversationsDao conversationsDao;

    @Inject GccChatBlocksDao chatBlocksDao;

    GccNcApi ncApi;

    public GccAccountRemovalWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        Objects.requireNonNull(GccTalkApplication.Companion.getSharedApplication()).getComponentApplication().inject(this);

        List<GccUser> users = userManager.getUsersScheduledForDeletion().blockingGet();
        for (GccUser user : users) {
            if (user.getPushConfigurationState() != null) {
                PushConfigurationState finalPushConfigurationState = user.getPushConfigurationState();

                ncApi = retrofit
                    .newBuilder()
                    .client(okHttpClient
                                .newBuilder()
                                .cookieJar(new JavaNetCookieJar(new CookieManager()))
                                .build())
                    .build()
                    .create(GccNcApi.class);

                ncApi.unregisterDeviceForNotificationsWithNextcloud(
                        GccApiUtils.getCredentials(user.getUsername(), user.getToken()),
                        GccApiUtils.getUrlNextcloudPush(Objects.requireNonNull(user.getBaseUrl())))
                    .blockingSubscribe(new Observer<GenericOverall>() {
                        @Override
                        public void onSubscribe(@io.reactivex.annotations.NonNull Disposable d) {
                            // unused atm
                        }

                        @Override
                        public void onNext(@io.reactivex.annotations.NonNull GenericOverall genericOverall) {
                            GenericMeta meta = Objects.requireNonNull(genericOverall.getOcs()).getMeta();
                            int statusCode = Objects.requireNonNull(meta).getStatusCode();

                            if (statusCode == 200 || statusCode == 202) {
                                HashMap<String, String> queryMap = new HashMap<>();
                                queryMap.put("deviceIdentifier",
                                             finalPushConfigurationState.getDeviceIdentifier());
                                queryMap.put("userPublicKey", finalPushConfigurationState.getUserPublicKey());
                                queryMap.put("deviceIdentifierSignature",
                                             finalPushConfigurationState.getDeviceIdentifierSignature());
                                unregisterDeviceForNotificationWithProxy(queryMap, user);
                            }
                        }

                        @Override
                        public void onError(@io.reactivex.annotations.NonNull Throwable e) {
                            Log.e(TAG, "error while trying to unregister Device For Notifications", e);
                            initiateUserDeletion(user);
                        }

                        @Override
                        public void onComplete() {
                            // unused atm
                        }
                    });
            } else {
                initiateUserDeletion(user);
            }
        }

        return Result.success();
    }

    private void unregisterDeviceForNotificationWithProxy(HashMap<String, String> queryMap, GccUser user) {
        ncApi.unregisterDeviceForNotificationsWithProxy
                (GccApiUtils.getUrlPushProxy(), queryMap)
                .subscribe(new Observer<Void>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        // unused atm
                    }

                    @Override
                    public void onNext(Void aVoid) {
                            String groupName = String.format(
                                getApplicationContext()
                                    .getResources()
                                    .getString(R.string.nc_notification_channel), user.getUserId(), user.getBaseUrl());
                            CRC32 crc32 = new CRC32();
                            crc32.update(groupName.getBytes());
                            NotificationManager notificationManager =
                                    (NotificationManager) getApplicationContext()
                                        .getSystemService(Context.NOTIFICATION_SERVICE);

                            if (notificationManager != null) {
                                notificationManager.deleteNotificationChannelGroup(
                                    Long.toString(crc32.getValue()));
                            }

                        initiateUserDeletion(user);
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e(TAG, "error while trying to unregister Device For Notification With Proxy", e);
                        initiateUserDeletion(user);
                    }

                    @Override
                    public void onComplete() {
                        // unused atm
                    }
                });
    }

    private void initiateUserDeletion(GccUser user) {
        if (user.getId() != null) {
            long id = user.getId();
            GccWebSocketConnectionHelper.deleteExternalSignalingInstanceForUserEntity(id);

            try {
                arbitraryStorageManager.deleteAllEntriesForAccountIdentifier(id);
                deleteUser(user);
            } catch (Throwable e) {
                Log.e(TAG, "error while trying to delete All Entries For Account Identifier", e);
            }
        }
    }

    private void deleteUser(GccUser user) {
        if (user.getId() != null) {
            String username = user.getUsername();
            try {
                userManager.deleteUser(user.getId());
                if (username != null) {
                    Log.d(TAG, "deleted user: " + username);
                }
            } catch (Throwable e) {
                Log.e(TAG, "error while trying to delete user", e);
            }
        }
    }
}
