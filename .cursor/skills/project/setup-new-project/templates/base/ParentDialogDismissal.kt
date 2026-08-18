package YOUR.PACKAGE.core.ui.base.dialog

import android.content.DialogInterface
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment

/**
 * Template — copy to `:core-ui` …/base/dialog/
 * Replace YOUR.PACKAGE with applicationId root.
 */
open class ParentDialogDismissal : DialogFragment() {

    var onDismissCallback: (() -> Unit)? = null

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        onDismissCallback?.invoke()
        onDismissCallback = null
    }
}

fun AppCompatActivity.safeShow(dialog: DialogFragment, tag: String) {
    if (isFinishing || isDestroyed) return
    val fm = supportFragmentManager
    if (fm.isStateSaved || fm.findFragmentByTag(tag) != null) return
    dialog.show(fm, tag)
}

fun Fragment.safeShow(dialog: DialogFragment, tag: String) {
    if (!isAdded) return
    val fm = parentFragmentManager
    if (fm.isStateSaved || fm.findFragmentByTag(tag) != null) return
    dialog.show(fm, tag)
}

fun DialogFragment.safeDismiss() {
    if (!isAdded) return
    if (isStateSaved) dismissAllowingStateLoss() else dismiss()
}
