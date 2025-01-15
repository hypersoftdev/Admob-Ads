package com.hypersoft.admobads.newPackage.utilities.extensions

import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.NavDirections
import androidx.navigation.fragment.findNavController
import com.hypersoft.admobads.newPackage.utilities.utils.withDelay

/**
 * Created by: Sohaib Ahmed
 * Date: 1/15/2025
 *
 * Links:
 * - LinkedIn: https://linkedin.com/in/epegasus
 * - GitHub: https://github.com/epegasus
 */

/* ---------------------------------------------- BackPress ---------------------------------------------- */

fun Fragment.onBackPressedDispatcher(callback: () -> Unit) {
    (activity as? AppCompatActivity)?.onBackPressedDispatcher?.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            callback.invoke()
        }
    })
}

/* ----------------------------------------- Navigation's -----------------------------------------*/

/**
 *     Used launchWhenCreated, bcz of screen rotation
 *     Used launchWhenResumed, bcz of screen rotation
 * @param fragmentId : Current Fragment's Id (from Nav Graph)
 * @param action : Action / Id of other fragment
 * @param bundle : Pass bundle as a NavArgs to destination.
 */

fun Fragment.navigateTo(fragmentId: Int, action: Int, bundle: Bundle) {
    launchWhenCreated {
        if (isAdded && isCurrentDestination(fragmentId)) {
            findNavController().navigate(action, bundle)
        }
    }
}

fun Fragment.navigateTo(fragmentId: Int, action: Int) {
    launchWhenCreated {
        if (isAdded && isCurrentDestination(fragmentId)) {
            findNavController().navigate(action)
        }
    }
}

fun Fragment.navigateTo(fragmentId: Int, action: NavDirections) {
    launchWhenCreated {
        if (isAdded && isCurrentDestination(fragmentId)) {
            findNavController().navigate(action)
        }
    }
}

fun Fragment.popFrom(fragmentId: Int) {
    launchWhenCreated {
        if (isAdded && isCurrentDestination(fragmentId)) {
            findNavController().popBackStack()
        }
    }
}

fun Fragment.popFrom(fragmentId: Int, destinationFragmentId: Int, inclusive: Boolean = false) {
    launchWhenCreated {
        if (isAdded && isCurrentDestination(fragmentId)) {
            findNavController().popBackStack(destinationFragmentId, inclusive)
        }
    }
}

private fun Fragment.isCurrentDestination(fragmentId: Int): Boolean {
    return findNavController().currentDestination?.id == fragmentId
}

/* ----------------------------------------- Delays -----------------------------------------*/

fun Fragment.withDelaySafe(delay: Long = 300, block: () -> Unit) {
    withDelay(delay) {
        if (isAdded) block.invoke()
    }
}