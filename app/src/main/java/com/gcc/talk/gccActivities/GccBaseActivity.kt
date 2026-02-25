/*
 * Nextcloud Talk - Android Client
 *
 * SPDX-FileCopyrightText: 2023 Marcel Hibbe <dev@mhibbe.de>
 * SPDX-FileCopyrightText: 2023 Andy Scherzinger <info@andy-scherzinger.de>
 * SPDX-FileCopyrightText: 2017-2019 Mario Danic <mario@lovelyhq.com>
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package com.gcc.talk.gccActivities

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.inputmethod.EditorInfo
import android.webkit.SslErrorHandler
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import autodagger.AutoInjector
import com.gcc.talk.R
import com.gcc.talk.gccApplication.GccTalkApplication
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.gcc.talk.R.color.colorPrimaryDark
import com.gcc.talk.gccAccount.GccAccountVerificationActivity
import com.gcc.talk.gccAccount.GccBrowserLoginActivity
import com.gcc.talk.gccAccount.GccServerSelectionActivity
import com.gcc.talk.gccAccount.GccSwitchAccountActivity
import com.gcc.talk.gccChat.GccChatActivity
import com.gcc.talk.gccEvents.GccCertificateEvent
import com.gcc.talk.gccUi.theme.ViewThemeUtils
import com.gcc.talk.gccUtils.GccDisplayUtils
import com.gcc.talk.gccUtils.GccFileViewerUtils
import com.gcc.talk.gccUtils.GccUriUtils
import com.gcc.talk.gccUtils.adjustUIForAPILevel35
import com.gcc.talk.gccUtils.bundle.GccBundleKeys
import com.gcc.talk.gccUtils.database.user.GccCurrentUserProvider
import com.gcc.talk.gccUtils.database.user.GccCurrentUserProviderOld
import com.gcc.talk.gccUtils.preferences.GccAppPreferences
import com.gcc.talk.gccUtils.ssl.GccTrustManager
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.security.cert.CertificateParsingException
import java.security.cert.X509Certificate
import java.text.DateFormat
import javax.inject.Inject

@AutoInjector(GccTalkApplication::class)
open class GccBaseActivity : AppCompatActivity() {

    enum class AppBarLayoutType {
        TOOLBAR,
        SEARCH_BAR,
        EMPTY
    }

    @Inject
    lateinit var eventBus: EventBus

    @Inject
    lateinit var appPreferences: GccAppPreferences

    @Inject
    lateinit var viewThemeUtils: ViewThemeUtils

    @Inject
    lateinit var context: Context

    @Deprecated("Use GccCurrentUserProvider instead")
    @Inject
    lateinit var currentUserProviderOld: GccCurrentUserProviderOld

    @Inject
    lateinit var currentUserProvider: GccCurrentUserProvider

    open val appBarLayoutType: AppBarLayoutType
        get() = AppBarLayoutType.TOOLBAR

    open val view: View?
        get() = null

    override fun onCreate(savedInstanceState: Bundle?) {
        GccTalkApplication.sharedApplication!!.componentApplication.inject(this)
        adjustUIForAPILevel35()
        super.onCreate(savedInstanceState)

        cleanTempCertPreference()
    }

    public override fun onStart() {
        super.onStart()
        eventBus.register(this)
    }

    public override fun onResume() {
        super.onResume()

        if (appPreferences.isKeyboardIncognito) {
            val viewGroup = (findViewById<View>(R.id.content) as ViewGroup).getChildAt(0) as ViewGroup
            disableKeyboardPersonalisedLearning(viewGroup)
        }

        if (appPreferences.isScreenSecured || appPreferences.isScreenLocked) {
            window.addFlags(WindowManager.LayoutParams.FLAG_SECURE)
        } else {
            window.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
        }
    }

    public override fun onStop() {
        super.onStop()
        eventBus.unregister(this)
    }

    /*
     * May be aligned with android-common lib in the future: .../ui/util/extensions/GccAppCompatActivityExtensions.kt
     */
    fun initSystemBars() {
        val decorView = window.decorView
        decorView.setOnApplyWindowInsetsListener { view, insets ->
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM) {
                val systemBars = insets.getInsets(
                    WindowInsetsCompat.Type.systemBars() or
                        WindowInsetsCompat.Type.displayCutout()
                )
                val color = ResourcesCompat.getColor(resources, R.color.bg_default, context.theme)
                view.setBackgroundColor(color)
                view.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            } else {
                colorizeStatusBar()
                colorizeNavigationBar()
            }
            insets
        }
        ViewCompat.requestApplyInsets(decorView)
    }

    open fun colorizeStatusBar() {
        if (resources != null) {
            if (appBarLayoutType == AppBarLayoutType.SEARCH_BAR) {
                viewThemeUtils.platform.resetStatusBar(this)
            } else {
                viewThemeUtils.platform.themeStatusBar(this)
            }
        }
    }

    fun colorizeNavigationBar() {
        if (resources != null) {
            GccDisplayUtils.applyColorToNavigationBar(
                this.window,
                ResourcesCompat.getColor(resources, colorPrimaryDark, null)
            )
        }
    }

    private fun disableKeyboardPersonalisedLearning(viewGroup: ViewGroup) {
        var view: View?
        var editText: EditText
        for (i in 0 until viewGroup.childCount) {
            view = viewGroup.getChildAt(i)
            if (view is EditText) {
                editText = view
                editText.imeOptions = editText.imeOptions or EditorInfo.IME_FLAG_NO_PERSONALIZED_LEARNING
            } else if (view is ViewGroup) {
                disableKeyboardPersonalisedLearning(view)
            }
        }
    }

    @Suppress("Detekt.NestedBlockDepth")
    private fun showCertificateDialog(
        cert: X509Certificate,
        trustManager: GccTrustManager,
        sslErrorHandler: SslErrorHandler?
    ) {
        val formatter = DateFormat.getDateInstance(DateFormat.LONG)
        val validFrom = formatter.format(cert.notBefore)
        val validUntil = formatter.format(cert.notAfter)

        val issuedBy = cert.issuerDN.toString()
        val issuedFor: String

        try {
            if (cert.subjectAlternativeNames != null) {
                val stringBuilder = StringBuilder()
                for (o in cert.subjectAlternativeNames) {
                    val list = o as List<*>
                    val type = list[0] as Int
                    if (type == 2) {
                        val name = list[1] as String
                        stringBuilder.append("[").append(type).append("]").append(name).append(" ")
                    }
                }
                issuedFor = stringBuilder.toString()
            } else {
                issuedFor = cert.subjectDN.name
            }

            @SuppressLint("StringFormatMatches")
            val dialogText = String.format(
                resources.getString(R.string.nc_certificate_dialog_text),
                issuedBy,
                issuedFor,
                validFrom,
                validUntil
            )

            val dialogBuilder = MaterialAlertDialogBuilder(this).setIcon(
                viewThemeUtils.dialog.colorMaterialAlertDialogIcon(
                    context,
                    R.drawable.ic_security_white_24dp
                )
            ).setTitle(R.string.nc_certificate_dialog_title)
                .setMessage(dialogText)
                .setPositiveButton(R.string.nc_yes) { _, _ ->
                    trustManager.addCertInTrustStore(cert)
                    sslErrorHandler?.proceed()
                }.setNegativeButton(R.string.nc_no) { _, _ ->
                    sslErrorHandler?.cancel()
                }

            viewThemeUtils.dialog.colorMaterialAlertDialogBackground(context, dialogBuilder)

            val dialog = dialogBuilder.show()

            viewThemeUtils.platform.colorTextButtons(
                dialog.getButton(AlertDialog.BUTTON_POSITIVE),
                dialog.getButton(AlertDialog.BUTTON_NEGATIVE)
            )
        } catch (e: CertificateParsingException) {
            Log.d(TAG, "Failed to parse the certificate")
        }
    }

    private fun cleanTempCertPreference() {
        val temporaryClassNames: MutableList<String> = ArrayList()
        temporaryClassNames.add(GccServerSelectionActivity::class.java.name)
        temporaryClassNames.add(GccAccountVerificationActivity::class.java.name)
        temporaryClassNames.add(GccBrowserLoginActivity::class.java.name)
        temporaryClassNames.add(GccSwitchAccountActivity::class.java.name)
        if (!temporaryClassNames.contains(javaClass.name)) {
            appPreferences.removeTemporaryClientCertAlias()
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(event: GccCertificateEvent) {
        showCertificateDialog(event.x509Certificate, event.trustManager, event.sslErrorHandler)
    }

    override fun startActivity(intent: Intent) {
        val user = currentUserProviderOld.currentUser.blockingGet()
        if (intent.data != null && TextUtils.equals(intent.action, Intent.ACTION_VIEW)) {
            val uri = intent.data.toString()
            if (user?.baseUrl != null && uri.startsWith(user.baseUrl!!)) {
                if (GccUriUtils.isInstanceInternalFileShareUrl(user.baseUrl!!, uri)) {
                    // https://cloud.nextcloud.com/f/41
                    val fileViewerUtils = GccFileViewerUtils(applicationContext, user)
                    fileViewerUtils.openFileInFilesApp(uri, GccUriUtils.extractInstanceInternalFileShareFileId(uri))
                } else if (GccUriUtils.isInstanceInternalFileUrl(user.baseUrl!!, uri)) {
                    // https://cloud.nextcloud.com/apps/files/?dir=/Engineering&fileid=41
                    val fileViewerUtils = GccFileViewerUtils(applicationContext, user)
                    fileViewerUtils.openFileInFilesApp(uri, GccUriUtils.extractInstanceInternalFileFileId(uri))
                } else if (GccUriUtils.isInstanceInternalFileUrlNew(user.baseUrl!!, uri)) {
                    // https://cloud.nextcloud.com/apps/files/?dir=/Engineering&fileid=41
                    val fileViewerUtils = GccFileViewerUtils(applicationContext, user)
                    fileViewerUtils.openFileInFilesApp(uri, GccUriUtils.extractInstanceInternalFileFileIdNew(uri))
                } else if (GccUriUtils.isInstanceInternalTalkUrl(user.baseUrl!!, uri)) {
                    // https://cloud.nextcloud.com/call/123456789
                    val bundle = Bundle()
                    bundle.putString(GccBundleKeys.KEY_ROOM_TOKEN, GccUriUtils.extractRoomTokenFromTalkUrl(uri))
                    val chatIntent = Intent(context, GccChatActivity::class.java)
                    chatIntent.putExtras(bundle)
                    chatIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    startActivity(chatIntent)
                } else {
                    super.startActivity(intent)
                }
            } else {
                super.startActivity(intent)
            }
        } else {
            super.startActivity(intent)
        }
    }

    companion object {
        private val TAG = GccBaseActivity::class.java.simpleName
    }
}
