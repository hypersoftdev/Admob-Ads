package YOUR.PACKAGE.core.ui.base.dialog

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import androidx.viewbinding.ViewBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder

/**
 * Template — copy to `:core-ui` …/base/dialog/
 * Replace YOUR.PACKAGE with applicationId root.
 */
abstract class ParentDialog<T : ViewBinding>(
    private val bindingInflater: (LayoutInflater) -> T,
) : ParentDialogDismissal() {

    private var _binding: T? = null
    protected val binding: T
        get() = _binding ?: throw IllegalStateException("Binding accessed after onDestroyView")

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        _binding = bindingInflater(layoutInflater)
        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setView(binding.root)
            .create()
        dialog.setOnShowListener { onDialogCreated() }
        return dialog
    }

    protected open fun onDialogCreated() {}

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
