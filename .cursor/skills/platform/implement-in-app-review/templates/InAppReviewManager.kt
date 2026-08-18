package REPLACE_APPLICATION_ID.core.ui.inAppReview

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.fragment.app.Fragment
import com.google.android.play.core.review.ReviewManager
import com.google.android.play.core.review.ReviewManagerFactory
import REPLACE_APPLICATION_ID.core.common.constants.Constants.TAG

/**
 * Play In-App Review helper. Construct from a Fragment; call [destroy] from
 * [Fragment.onDestroyView] (or when the host no longer needs it).
 *
 * Note: a successful [launchReview] Task does **not** mean the user submitted a
 * review — Play may skip showing the UI (quota / eligibility).
 */
class InAppReviewManager(
    private val fragment: Fragment,
) {

    private var reviewManager: ReviewManager? = null

    fun initManager(context: Context) {
        if (reviewManager == null) {
            reviewManager = ReviewManagerFactory.create(context.applicationContext)
            Log.d(TAG, "InAppReviewManager: initManager: Success")
        }
    }

    /**
     * Requests [com.google.android.play.core.review.ReviewInfo] then launches the
     * Play review flow on the host Activity.
     */
    fun launchReview(callback: (launched: Boolean, message: String) -> Unit) {
        val manager = reviewManager
        if (manager == null) {
            Log.e(TAG, "InAppReviewManager: launchReview: Failed: reviewManager is null")
            callback(false, "reviewManager is null")
            return
        }

        if (!fragment.isAdded) {
            Log.w(TAG, "InAppReviewManager: launchReview: Failed: fragment not added")
            callback(false, "Fragment not added")
            return
        }

        val activity: Activity? = fragment.activity
        if (activity == null) {
            Log.e(TAG, "InAppReviewManager: launchReview: Failed: activity is null")
            callback(false, "Activity is null")
            return
        }

        Log.d(TAG, "InAppReviewManager: launchReview: Started")
        manager.requestReviewFlow()
            .addOnCompleteListener { requestTask ->
                if (!requestTask.isSuccessful) {
                    val message = requestTask.exception?.message.orEmpty()
                    Log.e(TAG, "InAppReviewManager: launchReview: Failed: request: $message")
                    callback(false, message.ifEmpty { "requestReviewFlow failed" })
                    return@addOnCompleteListener
                }

                if (!fragment.isAdded || fragment.activity == null) {
                    Log.w(TAG, "InAppReviewManager: launchReview: Failed: fragment detached after request")
                    callback(false, "Fragment detached")
                    return@addOnCompleteListener
                }

                val host = fragment.activity ?: run {
                    callback(false, "Activity is null")
                    return@addOnCompleteListener
                }

                Log.d(TAG, "InAppReviewManager: launchReview: Success: reviewInfo ready")
                manager.launchReviewFlow(host, requestTask.result)
                    .addOnCompleteListener { launchTask ->
                        val ok = launchTask.isSuccessful
                        if (ok) {
                            Log.d(TAG, "InAppReviewManager: launchReview: Success: flow finished")
                            callback(true, "Review flow finished")
                        } else {
                            val message = launchTask.exception?.message.orEmpty()
                            Log.e(TAG, "InAppReviewManager: launchReview: Failed: launch: $message")
                            callback(false, message.ifEmpty { "launchReviewFlow failed" })
                        }
                    }
            }
    }

    fun destroy() {
        Log.d(TAG, "InAppReviewManager: destroy: Success")
        reviewManager = null
    }
}