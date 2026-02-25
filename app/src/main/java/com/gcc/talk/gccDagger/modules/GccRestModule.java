/*
 * Nextcloud Talk - Android Client
 *
 * SPDX-FileCopyrightText: 2017 Mario Danic <mario@lovelyhq.com>
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package com.gcc.talk.gccDagger.modules;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.github.aurae.retrofit2.LoganSquareConverterFactory;
import com.gcc.talk.BuildConfig;
import com.gcc.talk.R;
import com.gcc.talk.gccApi.GccNcApi;
import com.gcc.talk.gccApi.GccNcApiCoroutines;
import com.gcc.talk.gccApplication.GccTalkApplication;
import com.gcc.talk.gccUsers.GccUserManager;
import com.gcc.talk.gccUtils.GccApiUtils;
import com.gcc.talk.gccUtils.GccLoggingUtils;
import com.gcc.talk.gccUtils.preferences.GccAppPreferences;
import com.gcc.talk.gccUtils.ssl.GccKeyManager;
import com.gcc.talk.gccUtils.ssl.GccTrustManager;
import com.gcc.talk.gccUtils.ssl.SSLSocketFactoryCompat;

import java.io.IOException;
import java.net.CookieManager;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.nio.charset.StandardCharsets;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.concurrent.TimeUnit;

import javax.inject.Singleton;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.X509KeyManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import dagger.Module;
import dagger.Provides;
import io.reactivex.schedulers.Schedulers;
import okhttp3.Authenticator;
import okhttp3.Cache;
import okhttp3.Credentials;
import okhttp3.Dispatcher;
import okhttp3.Interceptor;
import okhttp3.JavaNetCookieJar;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.Route;
import okhttp3.internal.tls.OkHostnameVerifier;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;

@Module(includes = GccDatabaseModule.class)
public class GccRestModule {

    private static final String TAG = "GccRestModule";
    private final Context context;

    public GccRestModule(Context context) {
        this.context = context;
    }

    @Singleton
    @Provides
    GccNcApi provideNcApi(Retrofit retrofit) {
        return retrofit.create(GccNcApi.class);
    }

    @Singleton
    @Provides
    GccNcApiCoroutines provideNcApiCoroutines(Retrofit retrofit) {
        return retrofit.create(GccNcApiCoroutines.class);
    }


    @Singleton
    @Provides
    Proxy provideProxy(GccAppPreferences appPreferences) {
        if (!TextUtils.isEmpty(appPreferences.getProxyType()) && !"No proxy".equals(appPreferences.getProxyType())
            && !TextUtils.isEmpty(appPreferences.getProxyHost())) {
            GetProxyRunnable getProxyRunnable = new GetProxyRunnable(appPreferences);
            Thread getProxyThread = new Thread(getProxyRunnable);
            getProxyThread.start();
            try {
                getProxyThread.join();
                return getProxyRunnable.getProxyValue();
            } catch (InterruptedException e) {
                Log.e(TAG, "Failed to join the thread while getting proxy: " + e.getLocalizedMessage());
                return Proxy.NO_PROXY;
            }
        } else {
            return Proxy.NO_PROXY;
        }
    }

    @Singleton
    @Provides
    Retrofit provideRetrofit(OkHttpClient httpClient) {
        Retrofit.Builder retrofitBuilder = new Retrofit.Builder()
            .client(httpClient)
            .baseUrl("https://nextcloud.com")
            .addCallAdapterFactory(RxJava2CallAdapterFactory.createWithScheduler(Schedulers.io()))
            .addConverterFactory(LoganSquareConverterFactory.create());

        return retrofitBuilder.build();
    }

    @Singleton
    @Provides
    GccTrustManager provideTrustManager() {
        return new GccTrustManager();
    }

    @Singleton
    @Provides
    GccKeyManager provideKeyManager(GccAppPreferences appPreferences, GccUserManager userManager) {
        KeyStore keyStore = null;
        try {
            keyStore = KeyStore.getInstance("AndroidKeyStore");
            keyStore.load(null);
            KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            kmf.init(keyStore, null);
            X509KeyManager origKm = (X509KeyManager) kmf.getKeyManagers()[0];
            return new GccKeyManager(origKm, userManager, appPreferences);
        } catch (KeyStoreException e) {
            Log.e(TAG, "KeyStoreException " + e.getLocalizedMessage());
        } catch (CertificateException e) {
            Log.e(TAG, "CertificateException " + e.getLocalizedMessage());
        } catch (NoSuchAlgorithmException e) {
            Log.e(TAG, "NoSuchAlgorithmException " + e.getLocalizedMessage());
        } catch (IOException e) {
            Log.e(TAG, "IOException " + e.getLocalizedMessage());
        } catch (UnrecoverableKeyException e) {
            Log.e(TAG, "UnrecoverableKeyException " + e.getLocalizedMessage());
        }

        return null;
    }

    @Singleton
    @Provides

    SSLSocketFactoryCompat provideSslSocketFactoryCompat(GccKeyManager keyManager, GccTrustManager
        trustManager) {
        return new SSLSocketFactoryCompat(keyManager, trustManager);
    }

    @Singleton
    @Provides
    CookieManager provideCookieManager() {
        return new CookieManager();
    }

    @Singleton
    @Provides
    Cache provideCache() {
        int cacheSize = 128 * 1024 * 1024; // 128 MB

        return new Cache(GccTalkApplication.Companion.getSharedApplication().getCacheDir(), cacheSize);
    }

    @Singleton
    @Provides
    Dispatcher provideDispatcher() {
        Dispatcher dispatcher = new Dispatcher();
        dispatcher.setMaxRequestsPerHost(100);
        dispatcher.setMaxRequests(100);
        return dispatcher;
    }

    @Singleton
    @Provides
    OkHttpClient provideHttpClient(Proxy proxy, GccAppPreferences appPreferences,
                                   GccTrustManager trustManager,
                                   SSLSocketFactoryCompat sslSocketFactoryCompat, Cache cache,
                                   CookieManager cookieManager, Dispatcher dispatcher) {
        OkHttpClient.Builder httpClient = new OkHttpClient.Builder();

        httpClient.retryOnConnectionFailure(true);
        httpClient.connectTimeout(45, TimeUnit.SECONDS);
        httpClient.readTimeout(45, TimeUnit.SECONDS);
        httpClient.writeTimeout(45, TimeUnit.SECONDS);

        httpClient.cookieJar(new JavaNetCookieJar(cookieManager));
        httpClient.cache(cache);

        // Trust own CA and all self-signed certs
        httpClient.sslSocketFactory(sslSocketFactoryCompat, trustManager);
        httpClient.retryOnConnectionFailure(true);
        httpClient.hostnameVerifier(trustManager.getHostnameVerifier(OkHostnameVerifier.INSTANCE));

        httpClient.dispatcher(dispatcher);
        if (!Proxy.NO_PROXY.equals(proxy)) {
            httpClient.proxy(proxy);

            if (appPreferences.getProxyCredentials() &&
                !TextUtils.isEmpty(appPreferences.getProxyUsername()) &&
                !TextUtils.isEmpty(appPreferences.getProxyPassword())) {
                httpClient.proxyAuthenticator(new HttpAuthenticator(
                    Credentials.basic(
                        appPreferences.getProxyUsername(),
                        appPreferences.getProxyPassword(),
                        StandardCharsets.UTF_8),
                    "Proxy-Authorization"));
            }
        }

        httpClient.addInterceptor(new HeadersInterceptor());

        if (BuildConfig.DEBUG && !context.getResources().getBoolean(R.bool.nc_is_debug)) {
            HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
            loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
            loggingInterceptor.redactHeader("Authorization");
            loggingInterceptor.redactHeader("Proxy-Authorization");
            httpClient.addInterceptor(loggingInterceptor);
        } else if (context.getResources().getBoolean(R.bool.nc_is_debug)) {
            HttpLoggingInterceptor.Logger fileLogger =
                s -> GccLoggingUtils.INSTANCE.writeLogEntryToFile(context, s);
            HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor(fileLogger);
            loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
            loggingInterceptor.redactHeader("Authorization");
            loggingInterceptor.redactHeader("Proxy-Authorization");
            httpClient.addInterceptor(loggingInterceptor);
        }

        return httpClient.build();
    }

    public static class HeadersInterceptor implements Interceptor {

        @NonNull
        @Override
        public Response intercept(@NonNull Chain chain) throws IOException {
            Request original = chain.request();
            Request request = original.newBuilder()
                .header("GccUser-Agent", GccApiUtils.getUserAgent())
                .header("Accept", "application/json")
                .header("OCS-APIRequest", "true")
                .header("ngrok-skip-browser-warning", "true")
                .method(original.method(), original.body())
                .build();

            return chain.proceed(request);
        }
    }

    public static class HttpAuthenticator implements Authenticator {

        private final String credentials;
        private final String authenticatorType;

        public HttpAuthenticator(@NonNull String credentials, @NonNull String authenticatorType) {
            this.credentials = credentials;
            this.authenticatorType = authenticatorType;
        }

        @Nullable
        @Override
        public Request authenticate(@Nullable Route route, @NonNull Response response) {
            if (response.request().header(authenticatorType) != null) {
                return null;
            }

            Response countedResponse = response;

            int attemptsCount = 0;

            while ((countedResponse = countedResponse.priorResponse()) != null) {
                attemptsCount++;
                if (attemptsCount == 3) {
                    return null;
                }
            }

            return response.request().newBuilder()
                .header(authenticatorType, credentials)
                .build();
        }
    }

    private class GetProxyRunnable implements Runnable {
        private volatile Proxy proxy;
        private final GccAppPreferences appPreferences;

        GetProxyRunnable(GccAppPreferences appPreferences) {
            this.appPreferences = appPreferences;
        }

        @Override
        public void run() {
            if (Proxy.Type.valueOf(appPreferences.getProxyType()) == Proxy.Type.SOCKS) {
                proxy = new Proxy(Proxy.Type.valueOf(appPreferences.getProxyType()),
                                  InetSocketAddress.createUnresolved(appPreferences.getProxyHost(), Integer.parseInt(
                                      appPreferences.getProxyPort())));
            } else {
                proxy = new Proxy(Proxy.Type.valueOf(appPreferences.getProxyType()),
                                  new InetSocketAddress(appPreferences.getProxyHost(),
                                                        Integer.parseInt(appPreferences.getProxyPort())));
            }
        }

        Proxy getProxyValue() {
            return proxy;
        }
    }
}
