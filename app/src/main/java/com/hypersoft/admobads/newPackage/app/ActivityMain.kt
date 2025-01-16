package com.hypersoft.admobads.newPackage.app

import android.os.Bundle
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.fragment.NavHostFragment
import com.hypersoft.admobads.R
import com.hypersoft.admobads.databinding.ActivityMainBinding
import com.hypersoft.admobads.newPackage.utilities.base.activities.BaseActivity

class ActivityMain : BaseActivity<ActivityMainBinding>(ActivityMainBinding::inflate) {

    private val navController by lazy { (supportFragmentManager.findFragmentById(binding.fcvContainerMain.id) as NavHostFragment).navController }

    override fun onCreated() {
        navController.addOnDestinationChangedListener(destinationChangedListener())
    }

    private fun destinationChangedListener() = object : NavController.OnDestinationChangedListener {
        override fun onDestinationChanged(controller: NavController, destination: NavDestination, arguments: Bundle?) {
            when (destination.id) {
                R.id.fragmentEntrance -> {
                    includeTopPadding = false
                    includeBottomPadding = false
                }

                else -> {
                    includeTopPadding = true
                    includeBottomPadding = true
                }
            }
        }
    }
}