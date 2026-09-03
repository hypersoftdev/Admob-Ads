package com.hypersoft.ads.practice.gmaAds.common

import android.content.Context
import android.util.Log
import com.google.android.gms.ads.MobileAds
import com.hypersoft.ads.practice.core.Constants.TAG_ADS
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import kotlin.coroutines.resume
import kotlin.time.Duration.Companion.milliseconds

internal class AdsSdk(context: Context) {

    private val context: Context = context.applicationContext
    private val initMutex = Mutex()

    @Volatile
    private var isInitialized = false

    suspend fun initialize() {
        if (isInitialized) return
        initMutex.withLock {
            if (isInitialized) return
            try {
                Log.d(TAG_ADS, "Admob -> initialize: Starting")
                withTimeout(INIT_TIMEOUT_MS.milliseconds) {
                    withContext(Dispatchers.IO) {
                        suspendCancellableCoroutine { continuation ->
                            MobileAds.initialize(context) {
                                if (continuation.isActive) continuation.resume(Unit)
                            }
                        }
                    }
                }
                isInitialized = true
                Log.i(TAG_ADS, "Admob -> initialize: Ready")
            } catch (e: TimeoutCancellationException) {
                Log.e(TAG_ADS, "Admob -> initialize: Failed: timeout")
                throw e
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG_ADS, "Admob -> initialize: Failed: ${e.message}")
                throw e
            }
        }
    }

    private companion object {
        const val INIT_TIMEOUT_MS = 15_000L
    }
}