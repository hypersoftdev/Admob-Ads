package com.hypersoft.ads.practice.gmaAds.appOpen

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.hypersoft.ads.practice.core.Constants.TAG_ADS
import com.hypersoft.ads.practice.core.extensions.onBackPressedDispatcher
import com.hypersoft.ads.practice.gmaAds.AdsManager
import com.hypersoft.ads.practice.gmaAds.appOpen.model.shouldNavigate
import com.hypersoft.ads.practice.gmaAds.common.isSettled
import com.hypersoft.ads.practice.gmaAds.databinding.ActivityAppOpenLoadingBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import kotlin.time.Duration.Companion.milliseconds

class AppOpenLoadingActivity : AppCompatActivity() {

    private val adsManager: AdsManager by inject()
    private lateinit var binding: ActivityAppOpenLoadingBinding
    private var timeoutJob: Job? = null
    private var finished: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAppOpenLoadingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        Log.d(TAG_ADS, "AppOpenLoadingActivity: onCreate")
        registerBackPress()
        startTimeout()
        loadAd()
    }

    private fun registerBackPress() {
        onBackPressedDispatcher { }
    }

    private fun loadAd() {
        lifecycleScope.launch {
            adsManager.appOpen.load(AppOpenAdKey.LIFECYCLE).firstOrNull { it.isSettled }
            showAdAndFinish()
        }
    }

    private fun startTimeout() {
        timeoutJob = lifecycleScope.launch(Dispatchers.Main.immediate) {
            delay(LOAD_TIMEOUT_MS.milliseconds)
            Log.d(TAG_ADS, "AppOpenLoadingActivity: timeout")
            showAdAndFinish()
        }
    }

    private fun showAdAndFinish() {
        if (finished || isFinishing || isDestroyed) return
        finished = true
        timeoutJob?.cancel()
        lifecycleScope.launch {
            adsManager.appOpen.show(this@AppOpenLoadingActivity, AppOpenAdKey.LIFECYCLE)
                .first { it.shouldNavigate(AppOpenAdConfig[AppOpenAdKey.LIFECYCLE].navigateOn) }
            if (!isFinishing && !isDestroyed) finish()
        }
    }

    override fun onDestroy() {
        timeoutJob?.cancel()
        timeoutJob = null
        super.onDestroy()
    }

    private companion object {
        const val LOAD_TIMEOUT_MS = 5_000L
    }
}