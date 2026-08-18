package YOUR.PACKAGE.presentation.base.activity

import android.view.LayoutInflater
import androidx.viewbinding.ViewBinding
import YOUR.PACKAGE.core.ui.base.activity.ParentActivity

/**
 * Template — copy to `:presentation` …/base/activity/
 * Replace YOUR.PACKAGE with applicationId root.
 *
 * Add product-specific injects (ads, billing) here only when needed.
 */
abstract class BaseActivity<T : ViewBinding>(
    bindingFactory: (LayoutInflater) -> T,
) : ParentActivity<T>(bindingFactory)