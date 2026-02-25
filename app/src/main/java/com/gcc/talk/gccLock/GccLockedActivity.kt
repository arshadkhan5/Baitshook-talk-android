/*
 * Nextcloud Talk - Android Client
 *
 * SPDX-FileCopyrightText: 2023 Marcel Hibbe <dev@mhibbe.de>
 * SPDX-FileCopyrightText: 2021 Andy Scherzinger <info@andy-scherzinger.de>
 * SPDX-FileCopyrightText: 2017-2018 Mario Danic <mario@lovelyhq.com>
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package com.gcc.talk.gccLock;

import android.app.Activity
import android.app.KeyguardManager
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricPrompt
import autodagger.AutoInjector
import com.gcc.talk.R
import com.gcc.talk.gccApplication.GccTalkApplication
import com.gcc.talk.databinding.ActivityLockedBinding
import com.gcc.talk.gccUtils.GccBrandingUtils
import com.gcc.talk.gccUtils.GccSecurityUtils
import com.gcc.talk.gccUtils.preferences.GccAppPreferences
import java.util.concurrent.Executor
import java.util.concurrent.Executors
import javax.inject.Inject

@AutoInjector(GccTalkApplication::class)
class GccLockedActivity : AppCompatActivity() {

    @Inject
    lateinit var context: Context

    @Inject
    lateinit var appPreferences: GccAppPreferences

    private val startForCredentialsResult = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult
            ()
    ) {
        onConfirmDeviceCredentials(it)
    }

    private lateinit var binding: ActivityLockedBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        GccTalkApplication.sharedApplication!!.componentApplication.inject(this)

        binding = ActivityLockedBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    override fun onResume() {
        super.onResume()
        hideLogoForBrandedClients()

        binding.unlockContainer.setOnClickListener {
            checkIfWeAreSecure()
        }
        checkIfWeAreSecure()
    }

    private fun hideLogoForBrandedClients() {
        if (!GccBrandingUtils.isOriginalNextcloudClient(applicationContext)) {
            binding.appLogo.visibility = View.GONE
        }
    }

    private fun checkIfWeAreSecure() {
        val keyguardManager = getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager?
        if (keyguardManager?.isKeyguardSecure == true && appPreferences.isScreenLocked) {
            if (!GccSecurityUtils.checkIfWeAreAuthenticated(appPreferences.screenLockTimeout)) {
                Log.d(TAG, "showBiometricDialog because 'we are NOT authenticated'...")
                showBiometricDialog()
            } else {
                finish()
            }
        }
    }

    private fun showBiometricDialog() {
        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle(
                String.format(
                    context.getString(R.string.nc_biometric_unlock),
                    context.getString(R.string.nc_app_product_name)
                )
            )
            .setNegativeButtonText(context.getString(R.string.nc_cancel))
            .build()
        val executor: Executor = Executors.newSingleThreadExecutor()
        val biometricPrompt = BiometricPrompt(
            this,
            executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    Log.d(TAG, "Fingerprint recognised successfully")
                    finish()
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    Log.d(TAG, "Fingerprint not recognised")
                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    showAuthenticationScreen()
                }
            }
        )
        val cryptoObject = GccSecurityUtils.getCryptoObject()
        if (cryptoObject != null) {
            biometricPrompt.authenticate(promptInfo, cryptoObject)
        } else {
            biometricPrompt.authenticate(promptInfo)
        }
    }

    private fun showAuthenticationScreen() {
        Log.d(TAG, "showAuthenticationScreen")
        val keyguardManager = getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager?
        val intent = keyguardManager?.createConfirmDeviceCredentialIntent(null, null)
        if (intent != null) {
            startForCredentialsResult.launch(intent)
        }
    }

    private fun onConfirmDeviceCredentials(result: ActivityResult) {
        if (result.resultCode == Activity.RESULT_OK) {
            if (
                GccSecurityUtils.checkIfWeAreAuthenticated(appPreferences.screenLockTimeout)
            ) {
                finish()
            }
        } else {
            Log.d(TAG, "Authorization failed")
        }
    }

    companion object {
        private val TAG = GccLockedActivity::class.java.simpleName
    }
}
