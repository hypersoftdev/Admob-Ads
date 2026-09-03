package YOUR.PACKAGE.core.ui.base.sheet

import android.content.DialogInterface
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

/**
 * Template — copy to `:core-ui` …/base/sheet/
 * Replace YOUR.PACKAGE with applicationId root.
 */
open class ParentSheetDismissal : BottomSheetDialogFragment() {

    var onDismissCallback: (() -> Unit)? = null

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        onDismissCallback?.invoke()
        onDismissCallback = null
    }
}

fun AppCompatActivity.safeShow(sheet: BottomSheetDialogFragment, tag: String) {
    if (isFinishing || isDestroyed) return
    val fm = supportFragmentManager
    if (fm.isStateSaved || fm.findFragmentByTag(tag) != null) return
    sheet.show(fm, tag)
}

fun Fragment.safeShow(sheet: BottomSheetDialogFragment, tag: String) {
    if (!isAdded) return
    val fm = parentFragmentManager
    if (fm.isStateSaved || fm.findFragmentByTag(tag) != null) return
    sheet.show(fm, tag)
}

fun BottomSheetDialogFragment.safeDismiss() {
    if (!isAdded) return
    if (isStateSaved) dismissAllowingStateLoss() else dismiss()
}