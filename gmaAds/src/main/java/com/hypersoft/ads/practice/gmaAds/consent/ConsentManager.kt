package com.hypersoft.ads.practice.gmaAds.consent

import android.app.Activity
import android.util.Log
import com.google.android.ump.ConsentDebugSettings
import com.google.android.ump.ConsentForm
import com.google.android.ump.ConsentInformation
import com.google.android.ump.ConsentRequestParameters
import com.google.android.ump.UserMessagingPlatform
import com.hypersoft.ads.practice.core.Constants.TAG
import com.hypersoft.ads.practice.gmaAds.BuildConfig
import com.hypersoft.ads.practice.gmaAds.common.extensions.isSafeForAd
import java.lang.ref.WeakReference
import java.util.concurrent.atomic.AtomicBoolean

class ConsentManager(activity: Activity) {

    private val activityRef = WeakReference(activity)
    private var consentInformation: ConsentInformation? = null
    private var consentForm: ConsentForm? = null
    private var listener: ConsentListener? = null
    private val adsAllowedNotified = AtomicBoolean(false)

    val canRequestAds: Boolean
        get() = consentInformation?.canRequestAds() == true

    val isPrivacyOptionsRequired: Boolean
        get() = consentInformation?.privacyOptionsRequirementStatus ==
                ConsentInformation.PrivacyOptionsRequirementStatus.REQUIRED

    fun start(
        testDeviceHashedId: String = "",
        underAgeOfConsent: Boolean = false,
        listener: ConsentListener,
    ) {
        this.listener = listener
        adsAllowedNotified.set(false)

        val hostActivity = hostActivity()
        if (hostActivity == null) {
            Log.e(TAG, "ConsentManager: start: Failed: activity finishing/destroyed")
            notifyAdsAllowed()
            return
        }

        val information = UserMessagingPlatform.getConsentInformation(hostActivity)
        consentInformation = information

        information.requestConsentInfoUpdate(
            hostActivity,
            buildRequestParameters(hostActivity, testDeviceHashedId, underAgeOfConsent),
            { onConsentInfoUpdated(information) },
            { error ->
                Log.e(TAG, "ConsentManager: requestConsentInfoUpdate: Failed: ${error.message}")
                notifyAdsAllowed()
            },
        )
    }

    fun showForm() {
        val hostActivity = hostActivity()
        if (hostActivity == null) {
            Log.e(TAG, "ConsentManager: showForm: Failed: activity finishing/destroyed")
            notifyAdsAllowed()
            return
        }
        val form = consentForm
        if (form == null) {
            Log.e(TAG, "ConsentManager: showForm: Failed: form missing")
            notifyAdsAllowed()
            return
        }

        Log.i(TAG, "ConsentManager: showForm: Success: presenting")
        if (!hostActivity.isSafeForAd()) {
            Log.e(TAG, "ConsentManager: showForm: Failed: activity finishing/destroyed")
            notifyAdsAllowed()
            return
        }
        form.show(hostActivity) { error ->
            if (error != null) {
                Log.e(TAG, "ConsentManager: showForm: Failed: ${error.message}")
            } else {
                logStatuses("afterForm")
            }
            notifyAdsAllowed()
        }
    }

    fun showPrivacyOptions() {
        val hostActivity = hostActivity()
        if (hostActivity == null) {
            Log.e(TAG, "ConsentManager: showPrivacyOptions: Failed: activity finishing/destroyed")
            notifyAdsAllowed()
            return
        }

        Log.i(TAG, "ConsentManager: showPrivacyOptions: Success: presenting")
        if (!hostActivity.isSafeForAd()) {
            Log.e(TAG, "ConsentManager: showPrivacyOptions: Failed: activity finishing/destroyed")
            notifyAdsAllowed()
            return
        }
        UserMessagingPlatform.showPrivacyOptionsForm(hostActivity) { error ->
            if (error != null) {
                Log.e(TAG, "ConsentManager: showPrivacyOptions: Failed: ${error.message}")
            }
            notifyAdsAllowed()
        }
    }

    fun reset() {
        listener = null
        consentForm = null
    }

    private fun onConsentInfoUpdated(information: ConsentInformation) {
        if (hostActivity() == null) {
            Log.e(TAG, "ConsentManager: onConsentInfoUpdated: Failed: activity finishing/destroyed")
            notifyAdsAllowed()
            return
        }
        logStatuses("updated")
        listener?.onPrivacyOptionsRequired(isPrivacyOptionsRequired)

        val formRequired = information.isConsentFormAvailable &&
                information.consentStatus == ConsentInformation.ConsentStatus.REQUIRED
        if (formRequired) {
            loadForm()
        } else {
            Log.d(TAG, "ConsentManager: onConsentInfoUpdated: Success: form not required")
            notifyAdsAllowed()
        }
    }

    private fun loadForm() {
        val hostActivity = hostActivity()
        if (hostActivity == null) {
            Log.e(TAG, "ConsentManager: loadForm: Failed: activity finishing/destroyed")
            notifyAdsAllowed()
            return
        }

        UserMessagingPlatform.loadConsentForm(
            hostActivity,
            { form ->
                if (hostActivity() == null) {
                    Log.e(TAG, "ConsentManager: loadForm: Failed: activity finishing/destroyed")
                    notifyAdsAllowed()
                } else {
                    Log.d(TAG, "ConsentManager: loadForm: Success: loaded")
                    consentForm = form
                    listener?.onConsentFormReady()
                }
            },
            { error ->
                Log.e(TAG, "ConsentManager: loadForm: Failed: ${error.message}")
                notifyAdsAllowed()
            },
        )
    }

    private fun buildRequestParameters(
        activity: Activity,
        testDeviceHashedId: String,
        underAgeOfConsent: Boolean,
    ): ConsentRequestParameters {
        val builder = ConsentRequestParameters.Builder()
            .setTagForUnderAgeOfConsent(underAgeOfConsent)

        if (BuildConfig.DEBUG && testDeviceHashedId.isNotBlank()) {
            val debugSettings = ConsentDebugSettings.Builder(activity)
                .setDebugGeography(ConsentDebugSettings.DebugGeography.DEBUG_GEOGRAPHY_EEA)
                .addTestDeviceHashedId(testDeviceHashedId)
                .build()
            builder.setConsentDebugSettings(debugSettings)
        }

        return builder.build()
    }

    private fun hostActivity(): Activity? {
        val activity = activityRef.get() ?: return null
        if (!activity.isSafeForAd()) return null
        return activity
    }

    private fun notifyAdsAllowed() {
        if (adsAllowedNotified.compareAndSet(false, true)) {
            listener?.onAdsAllowed(canRequestAds)
        }
    }

    private fun logStatuses(stage: String) {
        val status = when (consentInformation?.consentStatus) {
            ConsentInformation.ConsentStatus.REQUIRED -> "REQUIRED"
            ConsentInformation.ConsentStatus.NOT_REQUIRED -> "NOT_REQUIRED"
            ConsentInformation.ConsentStatus.OBTAINED -> "OBTAINED"
            ConsentInformation.ConsentStatus.UNKNOWN -> "UNKNOWN"
            else -> "NULL"
        }
        val privacy = when (consentInformation?.privacyOptionsRequirementStatus) {
            ConsentInformation.PrivacyOptionsRequirementStatus.REQUIRED -> "REQUIRED"
            ConsentInformation.PrivacyOptionsRequirementStatus.NOT_REQUIRED -> "NOT_REQUIRED"
            ConsentInformation.PrivacyOptionsRequirementStatus.UNKNOWN -> "UNKNOWN"
            else -> "NULL"
        }
        Log.d(TAG, "ConsentManager: logStatuses: Success: stage=$stage consent=$status privacy=$privacy")
    }
}