package com.hypersoft.ads.practice.gmaAds.common

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

/**
 * Blocks lifecycle App Open while another fullscreen ad (interstitial / rewarded /
 * rewarded interstitial / app open) is showing. Dismiss of Google's [com.google.android.gms.ads.AdActivity]
 * retriggers process ON_START; the 1s hold-off covers that resume.
 */
internal class FullscreenAdGate {

    @Volatile
    var isShowing: Boolean = false
        private set

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private var clearJob: Job? = null

    fun showing() {
        clearJob?.cancel()
        isShowing = true
    }

    fun dismissed() {
        clearJob?.cancel()
        clearJob = scope.launch {
            delay(CLEAR_DELAY_MS.milliseconds)
            isShowing = false
        }
    }

    private companion object {
        const val CLEAR_DELAY_MS = 1_000L
    }
}