package YOUR.PACKAGE.core.platform.firebase

import android.os.Bundle
import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.analytics
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.installations.FirebaseInstallations
import YOUR.PACKAGE.core.common.Constants.TAG_FIREBASE

/**
 * Template — copy to `:core-platform` …/firebase/
 * Replace `YOUR.PACKAGE` with the applicationId root.
 *
 * Ads revenue (`logRevenueEvent`) is optional — add only when the app has ads.
 * Pass `Context` as an argument; never store it on this object.
 * Do not copy another app's prefs names (`rossPref`, `TaichiTroasCache`).
 * Do not log the Installation token value (secrets / 14-security-secrets).
 */
object PlatformFirebase {

    fun Throwable.recordException(log: String) {
        Log.e(TAG_FIREBASE, "PlatformFirebase: recordException: Failed: $log")
        FirebaseCrashlytics.getInstance().log(log)
        FirebaseCrashlytics.getInstance().recordException(this)
    }

    fun String.postFirebaseEvent() {
        val bundle = Bundle().apply {
            putString(FirebaseAnalytics.Param.ITEM_NAME, this@postFirebaseEvent)
        }
        Firebase.analytics.logEvent(this, bundle)
        Log.d(TAG_FIREBASE, "PlatformFirebase: postFirebaseEvent: Success: event=$this")
    }

    fun getDeviceToken() {
        FirebaseInstallations.getInstance().getToken(false)
            .addOnCompleteListener { task ->
                when {
                    task.isSuccessful && task.result != null ->
                        Log.d(TAG_FIREBASE, "PlatformFirebase: getDeviceToken: Success")
                    else ->
                        Log.e(TAG_FIREBASE, "PlatformFirebase: getDeviceToken: Failed")
                }
            }
    }
}
