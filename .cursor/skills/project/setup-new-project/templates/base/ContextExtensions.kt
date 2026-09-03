package YOUR.PACKAGE.core.ui.extensions

import android.content.Context
import android.widget.Toast
import androidx.annotation.StringRes

/**
 * Template — copy to `:core-ui` …/extensions/ContextExtensions.kt
 * Replace YOUR.PACKAGE with applicationId root.
 *
 * Usage:
 *   context?.showToast("Saved")
 *   context?.showToast(R.string.error_generic)
 *   requireContext().showToast(effect.messageResId)
 */

fun Context.showToast(message: CharSequence, duration: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(this, message, duration).show()
}

fun Context.showToast(@StringRes messageResId: Int, duration: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(this, messageResId, duration).show()
}