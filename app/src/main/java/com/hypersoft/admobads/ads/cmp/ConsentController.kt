package com.hypersoft.admobads.ads.cmp

import android.app.Activity
import android.util.Log
import com.google.android.ump.ConsentDebugSettings
import com.google.android.ump.ConsentForm
import com.google.android.ump.ConsentInformation
import com.google.android.ump.ConsentInformation.ConsentStatus
import com.google.android.ump.ConsentRequestParameters
import com.google.android.ump.UserMessagingPlatform
import com.hypersoft.admobads.BuildConfig
import com.hypersoft.admobads.ads.cmp.callback.ConsentCallback
import com.hypersoft.admobads.utilities.utils.Constants.TAG

/**
 * Created by: Sohaib Ahmed
 * Date: 2/10/2025
 *
 * Links:
 * - LinkedIn: https://linkedin.com/in/epegasus
 * - GitHub: https://github.com/epegasus
 */

class ConsentController(private val activity: Activity) {

    private var consentInformation: ConsentInformation? = null
    private var consentCallback: ConsentCallback? = null
    private var consentForm: ConsentForm? = null

    val canRequestAds: Boolean get() = consentInformation?.canRequestAds() == true

    fun initConsent(@Debug("Device Id is only use for DEBUG") deviceId: String, callback: ConsentCallback?) {
        this.consentCallback = callback

        val isDebug = BuildConfig.DEBUG

        val debugSettings = ConsentDebugSettings.Builder(activity)
            .setDebugGeography(ConsentDebugSettings.DebugGeography.DEBUG_GEOGRAPHY_EEA)
            .addTestDeviceHashedId(deviceId)
            .build()

        val params = when (isDebug) {
            true -> ConsentRequestParameters.Builder().setConsentDebugSettings(debugSettings).build()
            false -> ConsentRequestParameters.Builder().setTagForUnderAgeOfConsent(false).build()
        }

        consentInformation = UserMessagingPlatform.getConsentInformation(activity).also {
            if (isDebug) {
                Log.d(TAG, "Consent Form reset() in Debug")
                it.reset()
            }

            Log.d(TAG, "Consent ready for initialization")
            it.requestConsentInfoUpdate(activity, params, {
                Log.d(TAG, "Consent successfully initialized \n Is Consent Form Available: ${it.isConsentFormAvailable}")

                if (it.isConsentFormAvailable) {

                    // Show Consent Logs
                    when (consentInformation?.consentStatus) {
                        ConsentStatus.REQUIRED -> Log.d(TAG, "consentStatus: REQUIRED")
                        ConsentStatus.NOT_REQUIRED -> Log.d(TAG, "consentStatus: NOT_REQUIRED")
                        ConsentStatus.OBTAINED -> Log.d(TAG, "consentStatus: OBTAINED")
                        ConsentStatus.UNKNOWN -> Log.d(TAG, "consentStatus: UNKNOWN")
                    }

                    // Show Policy Logs
                    when (consentInformation?.privacyOptionsRequirementStatus) {
                        ConsentInformation.PrivacyOptionsRequirementStatus.REQUIRED -> Log.d(TAG, "privacyOptionsRequirementStatus: REQUIRED")
                        ConsentInformation.PrivacyOptionsRequirementStatus.NOT_REQUIRED -> Log.d(TAG, "privacyOptionsRequirementStatus: NOT_REQUIRED")
                        ConsentInformation.PrivacyOptionsRequirementStatus.UNKNOWN -> Log.d(TAG, "privacyOptionsRequirementStatus: UNKNOWN")
                        null -> Log.d(TAG, "Consent Information is null")
                    }

                    // Whether consent & policy is required or not
                    when (consentInformation?.consentStatus == ConsentStatus.REQUIRED) {
                        true -> loadConsentForm()
                        false -> consentCallback?.onAdsLoad(canRequestAds)
                    }

                    val isPolicyRequired = consentInformation?.privacyOptionsRequirementStatus == ConsentInformation.PrivacyOptionsRequirementStatus.REQUIRED
                    consentCallback?.onPolicyStatus(isPolicyRequired)

                } else {
                    Log.d(TAG, "Consent form is not available")
                    consentCallback?.onAdsLoad(canRequestAds)
                }
            }, { error ->
                Log.e(TAG, "initializationError: ${error.message}")
                consentCallback?.onAdsLoad(canRequestAds)
            })
        }
    }

    private fun loadConsentForm() {
        UserMessagingPlatform.loadConsentForm(activity, { consentForm ->
            Log.d(TAG, "Consent Form Load Successfully")
            this.consentForm = consentForm
            consentCallback?.onConsentFormLoaded()
        }) { formError ->
            Log.e(TAG, "Consent Form Load to Fail: ${formError.message}")
            consentCallback?.onAdsLoad(canRequestAds)
        }
    }

    fun showConsentForm() {
        Log.i(TAG, "Consent form is showing")
        consentForm?.show(activity) { formError ->
            Log.i(TAG, "consent Form Dismissed")

            consentCallback?.onConsentFormDismissed()
            consentCallback?.onAdsLoad(canRequestAds)

            when (formError == null) {
                true -> checkConsentAndPrivacyStatus()
                false -> Log.e(TAG, "Consent Form Show to fail: ${formError.message}")
            }

        } ?: run {
            Log.e(TAG, "Consent form failed to show")
            consentCallback?.onAdsLoad(canRequestAds)
        }
    }

    private fun checkConsentAndPrivacyStatus() {
        Log.d(TAG, "Check Consent And Privacy Status After Form Dismissed")

        when (consentInformation?.consentStatus) {
            ConsentStatus.REQUIRED -> Log.d(TAG, "consentStatus: REQUIRED")
            ConsentStatus.NOT_REQUIRED -> Log.d(TAG, "consentStatus: NOT_REQUIRED")
            ConsentStatus.OBTAINED -> Log.d(TAG, "consentStatus: OBTAINED")
            ConsentStatus.UNKNOWN -> Log.d(TAG, "consentStatus: UNKNOWN")
            null -> Log.d(TAG, "Consent Information is null")
        }
        when (consentInformation?.privacyOptionsRequirementStatus) {
            ConsentInformation.PrivacyOptionsRequirementStatus.REQUIRED -> Log.d(TAG, "privacyOptionsRequirementStatus: REQUIRED")
            ConsentInformation.PrivacyOptionsRequirementStatus.NOT_REQUIRED -> Log.d(TAG, "privacyOptionsRequirementStatus: NOT_REQUIRED")
            ConsentInformation.PrivacyOptionsRequirementStatus.UNKNOWN -> Log.d(TAG, "privacyOptionsRequirementStatus: UNKNOWN")
            null -> Log.d(TAG, "Consent Information is null")
        }
    }

    annotation class Debug(val message: String = "For Debug Feature")
}