@file:Suppress("unused")

package com.hypersoft.ads.practice.gmaAds.common.extensions

import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.withResumed
import com.hypersoft.ads.practice.core.extensions.launchWhenResumed
import com.hypersoft.ads.practice.gmaAds.AdsManager
import com.hypersoft.ads.practice.gmaAds.appOpen.AppOpenAdConfig
import com.hypersoft.ads.practice.gmaAds.appOpen.AppOpenAdKey
import com.hypersoft.ads.practice.gmaAds.banner.BannerAdKey
import com.hypersoft.ads.practice.gmaAds.banner.view.BannerContainerView
import com.hypersoft.ads.practice.gmaAds.common.AdLoadResult
import com.hypersoft.ads.practice.gmaAds.common.NativeShowResult
import com.hypersoft.ads.practice.gmaAds.common.RewardedShowResult
import com.hypersoft.ads.practice.gmaAds.common.isSettled
import com.hypersoft.ads.practice.gmaAds.interstitial.InterstitialAdConfig
import com.hypersoft.ads.practice.gmaAds.interstitial.InterstitialAdKey
import com.hypersoft.ads.practice.gmaAds.nativeAd.NativeAdKey
import com.hypersoft.ads.practice.gmaAds.nativeAd.view.NativeContainerView
import com.hypersoft.ads.practice.gmaAds.rewarded.RewardedAdKey
import com.hypersoft.ads.practice.gmaAds.rewardedInterstitial.RewardedInterstitialAdKey
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import org.koin.android.ext.android.getKoin
import com.hypersoft.ads.practice.gmaAds.appOpen.model.shouldNavigate as appOpenShouldNavigate
import com.hypersoft.ads.practice.gmaAds.interstitial.model.shouldNavigate as interstitialShouldNavigate

private val AppCompatActivity.adsManager: AdsManager get() = getKoin().get()

private suspend fun AdsManager.awaitNativeLoadSettled(key: NativeAdKey): AdLoadResult {
    return native.load(key).first { it.isSettled }
}

private suspend fun AdsManager.awaitBannerLoadSettled(activity: AppCompatActivity, key: BannerAdKey, adWidthDp: Int): AdLoadResult {
    return banner.load(activity, key, adWidthDp).first { it.isSettled }
}

/* ------------------------------------------- App Open ------------------------------------------- */

fun AppCompatActivity.loadAppOpenAd(appOpenAdKey: AppOpenAdKey, callback: ((AdLoadResult) -> Unit)? = null) {
    lifecycleScope.launch {
        val result = adsManager.appOpen.load(appOpenAdKey).firstOrNull { it.isSettled } ?: return@launch
        callback?.invoke(result)
    }
}

fun AppCompatActivity.showAppOpenAd(appOpenAdKey: AppOpenAdKey, onDone: (() -> Unit)? = null) = launchWhenResumed {
    lifecycleScope.launch {
        if (!isSafeForAd()) {
            onDone?.invoke()
            return@launch
        }
        adsManager.appOpen.show(this@showAppOpenAd, appOpenAdKey).first { it.appOpenShouldNavigate(AppOpenAdConfig[appOpenAdKey].navigateOn) }
        onDone?.invoke()
    }
}

fun AppCompatActivity.blockAppOpen() {
    adsManager.appOpen.shouldBlock = true
}

fun AppCompatActivity.unblockAppOpen() {
    adsManager.appOpen.shouldBlock = false
}

/* ------------------------------------------- Banner ------------------------------------------- */

fun AppCompatActivity.loadBannerAd(bannerAdKey: BannerAdKey, container: BannerContainerView, callback: ((AdLoadResult) -> Unit)? = null) {
    lifecycleScope.launch {
        if (!isSafeForAd()) return@launch
        val view = container as View
        val widthDp = view.awaitBannerWidthDp()
        if (!isSafeForAd()) return@launch
        val result = adsManager.awaitBannerLoadSettled(this@loadBannerAd, bannerAdKey, widthDp)
        callback?.invoke(result)
        when (result) {
            AdLoadResult.Loaded,
            AdLoadResult.SkippedFallback,
                -> {
                lifecycle.withResumed { }
                if (!isSafeForAd()) return@launch
                adsManager.banner.show(bannerAdKey, container).first()
            }

            else -> view.visibility = View.GONE
        }
    }
}

fun AppCompatActivity.pauseBannerAd(bannerAdKey: BannerAdKey) {
    adsManager.banner.pause(bannerAdKey)
}

fun AppCompatActivity.resumeBannerAd(bannerAdKey: BannerAdKey) {
    adsManager.banner.resume(bannerAdKey)
}

fun AppCompatActivity.destroyBannerAd(bannerAdKey: BannerAdKey, destroyIfImpressionReceived: Boolean = false) {
    adsManager.banner.destroy(bannerAdKey, destroyIfImpressionReceived)
}

/* ------------------------------------------- Interstitial ------------------------------------------- */

fun AppCompatActivity.loadInterstitialAd(interstitialAdKey: InterstitialAdKey, callback: ((AdLoadResult) -> Unit)? = null) {
    lifecycleScope.launch {
        val result = adsManager.interstitial.load(interstitialAdKey).firstOrNull { it.isSettled } ?: return@launch
        callback?.invoke(result)
    }
}

fun AppCompatActivity.showInterstitialAd(interstitialAdKey: InterstitialAdKey, onDone: (() -> Unit)? = null) = launchWhenResumed {
    lifecycleScope.launch {
        if (!isSafeForAd()) {
            onDone?.invoke()
            return@launch
        }
        adsManager.interstitial.show(this@showInterstitialAd, interstitialAdKey).first { it.interstitialShouldNavigate(InterstitialAdConfig[interstitialAdKey].navigateOn) }
        onDone?.invoke()
    }
}

/* ------------------------------------------- Native ------------------------------------------- */

fun AppCompatActivity.loadNativeAd(nativeAdKey: NativeAdKey, container: NativeContainerView, callback: ((AdLoadResult) -> Unit)? = null) {
    lifecycleScope.launch {
        val result = adsManager.awaitNativeLoadSettled(nativeAdKey)
        callback?.invoke(result)
        when (result) {
            AdLoadResult.Loaded,
            AdLoadResult.SkippedFallback,
                -> adsManager.native.show(nativeAdKey, container).first()

            else -> (container as View).visibility = View.GONE
        }
    }
}

fun AppCompatActivity.loadNativeAd(nativeAdKey: NativeAdKey, callback: ((AdLoadResult) -> Unit)? = null) {
    lifecycleScope.launch {
        val result = adsManager.awaitNativeLoadSettled(nativeAdKey)
        callback?.invoke(result)
    }
}

fun AppCompatActivity.showNativeAd(nativeAdKey: NativeAdKey, container: NativeContainerView, callback: ((NativeShowResult) -> Unit)? = null) {
    lifecycleScope.launch {
        var showResult = adsManager.native.show(nativeAdKey, container).first()
        if (showResult == NativeShowResult.NotAvailable) {
            val loadResult = adsManager.awaitNativeLoadSettled(nativeAdKey)
            showResult = when (loadResult) {
                AdLoadResult.Loaded,
                AdLoadResult.SkippedFallback,
                    -> adsManager.native.show(nativeAdKey, container).first()

                else -> {
                    (container as View).visibility = View.GONE
                    NativeShowResult.NotAvailable
                }
            }
        }
        if (showResult is NativeShowResult.Failed) {
            (container as View).visibility = View.GONE
        }
        callback?.invoke(showResult)
    }
}

fun AppCompatActivity.destroyNativeAd(nativeAdKey: NativeAdKey, destroyIfImpressionReceived: Boolean = false) {
    adsManager.native.destroy(nativeAdKey, destroyIfImpressionReceived)
}

/* ------------------------------------------- Rewarded ------------------------------------------- */

fun AppCompatActivity.loadRewardedAd(rewardedAdKey: RewardedAdKey, callback: ((AdLoadResult) -> Unit)? = null) {
    lifecycleScope.launch {
        val result = adsManager.rewarded.load(rewardedAdKey).firstOrNull { it.isSettled } ?: return@launch
        callback?.invoke(result)
    }
}

fun AppCompatActivity.showRewardedAd(rewardedAdKey: RewardedAdKey, onDone: (rewardGranted: Boolean) -> Unit) = launchWhenResumed {
    lifecycleScope.launch {
        if (!isSafeForAd()) {
            onDone(false)
            return@launch
        }
        val result = adsManager.rewarded.show(this@showRewardedAd, rewardedAdKey).first()
        onDone(result is RewardedShowResult.Closed && result.rewardGranted)
    }
}

/* ------------------------------------------- Rewarded Interstitial ------------------------------------------- */

fun AppCompatActivity.loadRewardedInterstitialAd(rewardedInterstitialAdKey: RewardedInterstitialAdKey, callback: ((AdLoadResult) -> Unit)? = null) {
    lifecycleScope.launch {
        val result = adsManager.rewardedInterstitial.load(rewardedInterstitialAdKey).firstOrNull { it.isSettled } ?: return@launch
        callback?.invoke(result)
    }
}

fun AppCompatActivity.showRewardedInterstitialAd(rewardedInterstitialAdKey: RewardedInterstitialAdKey, onDone: (rewardGranted: Boolean) -> Unit) = launchWhenResumed {
    lifecycleScope.launch {
        if (!isSafeForAd()) {
            onDone(false)
            return@launch
        }
        val result = adsManager.rewardedInterstitial.show(this@showRewardedInterstitialAd, rewardedInterstitialAdKey).first()
        onDone(result is RewardedShowResult.Closed && result.rewardGranted)
    }
}