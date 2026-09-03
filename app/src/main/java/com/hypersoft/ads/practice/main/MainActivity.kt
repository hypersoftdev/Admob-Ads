package com.hypersoft.ads.practice.main

import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.navigation.fragment.NavHostFragment
import com.hypersoft.ads.practice.R
import com.hypersoft.ads.practice.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }

    private val navController by lazy { (supportFragmentManager.findFragmentById(R.id.fcvContainerMain) as NavHostFragment).navController }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        applyWindowInsets()
        registerBackPress()
    }

    private fun applyWindowInsets() {
        enableEdgeToEdge()
        ViewCompat.setOnApplyWindowInsetsListener(binding.fcvContainerMain) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        setContentView(binding.root)
    }

    private fun registerBackPress() {
        onBackPressedDispatcher.addCallback(
            this,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    when (navController.currentDestination?.id) {
                        R.id.entranceFragment,
                        R.id.languageFragment,
                        R.id.onboardingFragment,
                        R.id.menuFragment,
                            -> Unit

                        R.id.dashboardFragment -> finish()
                        else -> navController.popBackStack()
                    }
                }
            },
        )
    }
}