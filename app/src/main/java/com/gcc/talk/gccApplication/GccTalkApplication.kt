/*
 * Nextcloud Talk - Android Client
 *
 * SPDX-FileCopyrightText: 2022 Andy Scherzinger <info@andy-scherzinger.de>
 * SPDX-FileCopyrightText: 2022 Marcel Hibbe <dev@mhibbe.de>
 * SPDX-FileCopyrightText: 2022 Tim Krüger <t@timkrueger.me>
 * SPDX-FileCopyrightText: 2017 Mario Danic <mario@lovelyhq.com>
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package com.gcc.talk.gccApplication

import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build.VERSION.SDK_INT
import android.os.Build.VERSION_CODES.P
import android.util.Log
import androidx.appcompat.app.AppCompatDelegate
import androidx.emoji2.bundled.BundledEmojiCompatConfig
import androidx.emoji2.text.EmojiCompat
import androidx.lifecycle.LifecycleObserver
import androidx.multidex.MultiDex
import androidx.multidex.MultiDexApplication
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.OneTimeWorkRequest
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkManager
import autodagger.AutoComponent
import autodagger.AutoInjector
import coil.Coil
import coil.ImageLoader
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import coil.decode.SvgDecoder
import coil.memory.MemoryCache
import coil.util.DebugLogger
import com.gcc.talk.BuildConfig
import com.gcc.talk.gccFilebrowser.webdav.GccDavUtils
import com.gcc.talk.gccAccount.GccAccountVerificationActivity
import com.gcc.talk.gccAccount.GccBrowserLoginActivity
import com.gcc.talk.gccDagger.modules.GccBusModule
import com.gcc.talk.gccDagger.modules.GccContextModule
import com.gcc.talk.gccDagger.modules.GccDaosModule
import com.gcc.talk.gccDagger.modules.GccDatabaseModule
import com.gcc.talk.gccDagger.modules.GccManagerModule
import com.gcc.talk.gccDagger.modules.GccRepositoryModule
import com.gcc.talk.gccDagger.modules.GccRestModule
import com.gcc.talk.gccDagger.modules.GccUtilsModule
import com.gcc.talk.gccDagger.modules.ViewModelModule
import com.gcc.talk.gccJobs.GccAccountRemovalWorker
import com.gcc.talk.gccJobs.GccCapabilitiesWorker
import com.gcc.talk.gccJobs.GccSignalingSettingsWorker
import com.gcc.talk.gccJobs.GccWebsocketConnectionsWorker
import com.gcc.talk.gccUi.theme.ThemeModule
import com.gcc.talk.utils.ClosedInterfaceImpl
import com.gcc.talk.gccUtils.GccDeviceUtils
import com.gcc.talk.gccUtils.GccNotificationUtils
import com.gcc.talk.gccUtils.database.arbitrarystorage.GccArbitraryStorageModule
import com.gcc.talk.gccUtils.database.user.GccUserModule
import com.gcc.talk.gccUtils.preferences.GccAppPreferences
import com.vanniktech.emoji.EmojiManager
import com.vanniktech.emoji.google.GoogleEmojiProvider
import de.cotech.hw.SecurityKeyManager
import de.cotech.hw.SecurityKeyManagerConfig
import okhttp3.OkHttpClient
import org.conscrypt.Conscrypt
import org.webrtc.PeerConnectionFactory
import java.security.Security
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@AutoComponent(
    modules = [
        GccBusModule::class,
        GccContextModule::class,
        GccDatabaseModule::class,
        GccRestModule::class,
        GccUserModule::class,
        GccArbitraryStorageModule::class,
        ViewModelModule::class,
        GccRepositoryModule::class,
        GccUtilsModule::class,
        ThemeModule::class,
        GccManagerModule::class,
        GccDaosModule::class
    ]
)
@Singleton
@AutoInjector(GccTalkApplication::class)
class GccTalkApplication :
    MultiDexApplication(),
    LifecycleObserver {
    //region Fields (components)
    lateinit var componentApplication:GccTalkApplicationComponent
        private set
    //endregion

    //region Getters

    @Inject
    lateinit var appPreferences: GccAppPreferences

    @Inject
    lateinit var okHttpClient: OkHttpClient
    //endregion

    //region private methods
    private fun initializeWebRtc() {
        try {
            PeerConnectionFactory.initialize(
                PeerConnectionFactory.InitializationOptions.builder(this)
                    .createInitializationOptions()
            )
        } catch (e: UnsatisfiedLinkError) {
            Log.w(TAG, e)
        }
    }

    //endregion

    //region Overridden methods
    override fun onCreate() {
        Log.d(TAG, "onCreate")
        sharedApplication = this

        val securityKeyManager = SecurityKeyManager.getInstance()
        val securityKeyConfigBuilder = SecurityKeyManagerConfig.Builder()
            .setEnableDebugLogging(BuildConfig.DEBUG)

        try {
            val packageManager = packageManager
            val activities = packageManager.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES).activities
            activities?.forEach { activityInfo ->
                try {
                    val activityClass = Class.forName(activityInfo.name)
                    if (activityClass != GccAccountVerificationActivity::class.java &&
                        activityClass != GccBrowserLoginActivity::class.java
                    ) {
                        @Suppress("UNCHECKED_CAST")
                        securityKeyConfigBuilder.addExcludedActivityClass(activityClass as Class<out Activity>)
                    }
                } catch (exception: ClassNotFoundException) {
                    Log.e(TAG, "Couldn't disable activity for security key listener", exception)
                }
            }
        } catch (exception: PackageManager.NameNotFoundException) {
            Log.e(TAG, "Couldn't disable activities for security key listener", exception)
        }

        securityKeyManager.init(this, securityKeyConfigBuilder.build())
        initializeWebRtc()
        buildComponent()
        GccDavUtils.registerCustomFactories()

        Security.insertProviderAt(Conscrypt.newProvider(), 1)

        componentApplication.inject(this)

        Coil.setImageLoader(buildDefaultImageLoader())
        setAppTheme(appPreferences.theme)
        super.onCreate()

        ClosedInterfaceImpl().providerInstallerInstallIfNeededAsync()
        GccDeviceUtils.ignoreSpecialBatteryFeatures()

        initWorkers()

        val config = BundledEmojiCompatConfig(this)
        config.setReplaceAll(true)
        val emojiCompat = EmojiCompat.init(config)

        EmojiManager.install(GoogleEmojiProvider())

        GccNotificationUtils.registerNotificationChannels(applicationContext, appPreferences)
    }

    private fun initWorkers() {
        val accountRemovalWork = OneTimeWorkRequest.Builder(GccAccountRemovalWorker::class.java).build()
        val capabilitiesUpdateWork = OneTimeWorkRequest.Builder(GccCapabilitiesWorker::class.java).build()
        val signalingSettingsWork = OneTimeWorkRequest.Builder(GccSignalingSettingsWorker::class.java).build()
        val websocketConnectionsWorker = OneTimeWorkRequest.Builder(GccWebsocketConnectionsWorker::class.java).build()

        WorkManager.getInstance(applicationContext)
            .beginWith(accountRemovalWork)
            .then(capabilitiesUpdateWork)
            .then(signalingSettingsWork)
            .then(websocketConnectionsWorker)
            .enqueue()

        val periodicCapabilitiesUpdateWork = PeriodicWorkRequest.Builder(
            GccCapabilitiesWorker::class.java,
            HALF_DAY,
            TimeUnit.HOURS
        ).build()
        WorkManager.getInstance(applicationContext).enqueueUniquePeriodicWork(
            "DailyCapabilitiesUpdateWork",
            ExistingPeriodicWorkPolicy.REPLACE,
            periodicCapabilitiesUpdateWork
        )
    }

    override fun onTerminate() {
        super.onTerminate()
        sharedApplication = null
    }
    //endregion

    //region Protected methods
    protected fun buildComponent() {
        componentApplication = DaggerGccTalkApplicationComponent.builder()
            .gccBusModule(GccBusModule())
            .gccContextModule(GccContextModule(applicationContext))
            .gccDatabaseModule(GccDatabaseModule())
            .gccRestModule(GccRestModule(applicationContext))
            .gccArbitraryStorageModule(GccArbitraryStorageModule())
            .build()
    }

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(base)
        MultiDex.install(this)
    }

    private fun buildDefaultImageLoader(): ImageLoader {
        val imageLoaderBuilder = ImageLoader.Builder(applicationContext)
            .memoryCache {
                // Use 50% of the application's available memory.
                MemoryCache.Builder(applicationContext).maxSizePercent(FIFTY_PERCENT).build()
            }
            .crossfade(true) // Show a short crossfade when loading images from network or disk into an ImageView.
            .components {
                if (SDK_INT >= P) {
                    add(ImageDecoderDecoder.Factory())
                } else {
                    add(GifDecoder.Factory())
                }
                add(SvgDecoder.Factory())
            }

        if (BuildConfig.DEBUG) {
            imageLoaderBuilder.logger(DebugLogger())
        }

        imageLoaderBuilder.okHttpClient(okHttpClient)

        return imageLoaderBuilder.build()
    }

    companion object {
        private val TAG = GccTalkApplication::class.java.simpleName
        const val FIFTY_PERCENT = 0.5
        const val HALF_DAY: Long = 12
        const val CIPHER_V4_MIGRATION: Int = 7
        //region Singleton
        //endregion

        var sharedApplication: GccTalkApplication? = null
            protected set
        //endregion

        //region Setters
        fun setAppTheme(theme: String) {
            when (theme) {
                "night_no" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                "night_yes" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                "battery_saver" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_AUTO_BATTERY)
                else ->
                    // will be "follow_system" only for now
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
            }
        }
    }
    //endregion
}
