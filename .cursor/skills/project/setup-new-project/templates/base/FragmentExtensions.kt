package YOUR.PACKAGE.core.ui.extensions

import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.withCreated
import androidx.lifecycle.withResumed
import androidx.lifecycle.withStarted
import androidx.navigation.NavDirections
import androidx.navigation.fragment.findNavController
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

/**
 * Template — copy to `:core-ui` …/extensions/FragmentExtensions.kt
 * Replace YOUR.PACKAGE with applicationId root.
 *
 * Collectors MUST use viewLifecycleOwner so they stop in onDestroyView
 * (avoids duplicate collectors after navigate away / back).
 *
 * State → collectWhenStarted; Effects → collectWhenCreated (typical MVI).
 */

fun Fragment.onBackPressedDispatcher(callback: () -> Unit) {
    (activity as? AppCompatActivity)?.onBackPressedDispatcher?.addCallback(
        viewLifecycleOwner,
        object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                callback.invoke()
            }
        },
    )
}

fun Fragment.repeatWhen(
    lifecycleState: Lifecycle.State = Lifecycle.State.STARTED,
    block: suspend () -> Unit,
) {
    viewLifecycleOwner.lifecycleScope.launch {
        viewLifecycleOwner.repeatOnLifecycle(lifecycleState) {
            block()
        }
    }
}

fun <T> Fragment.collectWhenCreated(flow: Flow<T>, collector: suspend (T) -> Unit) {
    repeatWhen(lifecycleState = Lifecycle.State.CREATED) {
        flow.collect(collector)
    }
}

fun <T> Fragment.collectWhenStarted(flow: Flow<T>, collector: suspend (T) -> Unit) {
    repeatWhen(lifecycleState = Lifecycle.State.STARTED) {
        flow.collect(collector)
    }
}

fun Fragment.launchWhenCreated(callback: () -> Unit) {
    viewLifecycleOwner.lifecycleScope.launch {
        viewLifecycleOwner.lifecycle.withCreated(callback)
    }
}

fun Fragment.launchWhenStarted(callback: () -> Unit) {
    viewLifecycleOwner.lifecycleScope.launch {
        viewLifecycleOwner.lifecycle.withStarted(callback)
    }
}

fun Fragment.launchWhenResumed(callback: () -> Unit) {
    viewLifecycleOwner.lifecycleScope.launch {
        viewLifecycleOwner.lifecycle.withResumed(callback)
    }
}

fun Fragment.isCurrentDestination(fragmentId: Int): Boolean =
    findNavController().currentDestination?.id == fragmentId

fun Fragment.navigateTo(fragmentId: Int, action: Int, bundle: Bundle) {
    if (isAdded && isCurrentDestination(fragmentId)) {
        findNavController().navigate(action, bundle)
    }
}

fun Fragment.navigateTo(fragmentId: Int, action: Int) {
    if (isAdded && isCurrentDestination(fragmentId)) {
        findNavController().navigate(action)
    }
}

fun Fragment.navigateTo(fragmentId: Int, action: NavDirections) {
    if (isAdded && isCurrentDestination(fragmentId)) {
        findNavController().navigate(action)
    }
}

fun Fragment.popFrom(fragmentId: Int) {
    if (isAdded && isCurrentDestination(fragmentId)) {
        findNavController().popBackStack()
    }
}

fun Fragment.popFrom(fragmentId: Int, destinationFragmentId: Int, inclusive: Boolean = false) {
    if (isAdded && isCurrentDestination(fragmentId)) {
        findNavController().popBackStack(destinationFragmentId, inclusive)
    }
}