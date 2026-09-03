package YOUR.PACKAGE.presentation.base.sheets

import android.view.LayoutInflater
import androidx.viewbinding.ViewBinding
import YOUR.PACKAGE.core.ui.base.dialog.ParentDialog

/**
 * Template — copy to `:presentation` …/base/sheets/
 * Replace YOUR.PACKAGE with applicationId root.
 */
abstract class BaseDialog<T : ViewBinding>(
    bindingFactory: (LayoutInflater) -> T,
) : ParentDialog<T>(bindingFactory)
