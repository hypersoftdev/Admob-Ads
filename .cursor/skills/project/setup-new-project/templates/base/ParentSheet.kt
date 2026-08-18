package YOUR.PACKAGE.core.ui.base.sheet

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.viewbinding.ViewBinding

/**
 * Template — copy to `:core-ui` …/base/sheet/
 * Replace YOUR.PACKAGE with applicationId root.
 *
 * Binding is null-safe (same pattern as ParentFragment) — not `!!`.
 */
abstract class ParentSheet<T : ViewBinding>(
    private val bindingFactory: (LayoutInflater) -> T,
) : ParentSheetDismissal() {

    private var _binding: T? = null
    protected val binding get() =
        _binding ?: throw IllegalStateException("Accessing binding outside of view lifecycle")

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = bindingFactory(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        onSheetCreated()
        initObservers()
    }

    abstract fun onSheetCreated()

    open fun initObservers() {}

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
