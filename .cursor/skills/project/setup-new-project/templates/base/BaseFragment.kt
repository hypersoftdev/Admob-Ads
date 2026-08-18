package YOUR.PACKAGE.presentation.base.fragment

import android.view.LayoutInflater
import androidx.viewbinding.ViewBinding

/**
 * Template — copy to `:presentation` …/base/fragment/
 * Replace YOUR.PACKAGE with applicationId root.
 *
 * Thin by default. When the app has ads/billing, inject managers here
 * (presentation BaseFragment role) — do not put ads into ParentFragment.
 */
abstract class BaseFragment<T : ViewBinding>(
    bindingFactory: (LayoutInflater) -> T,
) : BasePermissionFragment<T>(bindingFactory)
