package YOUR.PACKAGE.core.ui.base.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewbinding.ViewBinding

/**
 * Template — copy to `:core-ui` …/base/fragment/
 * Replace YOUR.PACKAGE with applicationId root.
 */
abstract class ParentFragment<T : ViewBinding>(
    val bindingFactory: (LayoutInflater) -> T,
) : Fragment() {

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
        initObservers()
        onViewCreated(savedInstanceState)
        onViewCreated()
    }

    open fun initObservers() {}

    open fun onViewCreated(savedInstanceState: Bundle?) {}

    abstract fun onViewCreated()

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
