package YOUR.PACKAGE.presentation.base.sheets

import android.view.LayoutInflater
import androidx.viewbinding.ViewBinding
import YOUR.PACKAGE.core.ui.base.sheet.ParentSheet

/**
 * Template — copy to `:presentation` …/base/sheets/
 * Replace YOUR.PACKAGE with applicationId root.
 */
abstract class BaseSheet<T : ViewBinding>(
    bindingFactory: (LayoutInflater) -> T,
) : ParentSheet<T>(bindingFactory)
