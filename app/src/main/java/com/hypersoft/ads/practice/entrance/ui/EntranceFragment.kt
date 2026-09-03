package com.hypersoft.ads.practice.entrance.ui

import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.hypersoft.ads.practice.R
import com.hypersoft.ads.practice.base.fragment.BaseFragment
import com.hypersoft.ads.practice.core.extensions.collectWhenCreated
import com.hypersoft.ads.practice.core.extensions.collectWhenStarted
import com.hypersoft.ads.practice.core.extensions.launchWhenResumed
import com.hypersoft.ads.practice.databinding.FragmentEntranceBinding
import com.hypersoft.ads.practice.entrance.effect.EntranceEffect
import com.hypersoft.ads.practice.entrance.intent.EntranceIntent
import com.hypersoft.ads.practice.entrance.state.EntranceState
import com.hypersoft.ads.practice.entrance.viewModel.EntranceViewModel
import com.hypersoft.ads.practice.gmaAds.appOpen.AppOpenAdKey
import com.hypersoft.ads.practice.gmaAds.common.extensions.blockAppOpen
import com.hypersoft.ads.practice.gmaAds.common.extensions.loadAppOpenAd
import com.hypersoft.ads.practice.gmaAds.common.extensions.loadInterstitialAd
import com.hypersoft.ads.practice.gmaAds.common.extensions.loadNativeAd
import com.hypersoft.ads.practice.gmaAds.common.extensions.showAppOpenOrInterstitialAd
import com.hypersoft.ads.practice.gmaAds.consent.ConsentListener
import com.hypersoft.ads.practice.gmaAds.consent.ConsentManager
import com.hypersoft.ads.practice.gmaAds.interstitial.InterstitialAdKey
import com.hypersoft.ads.practice.gmaAds.nativeAd.NativeAdKey
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class EntranceFragment : BaseFragment<FragmentEntranceBinding>(FragmentEntranceBinding::inflate) {

    private val viewModel: EntranceViewModel by viewModel()

    private var consentManager: ConsentManager? = null

    override fun onViewCreated() {
        blockAppOpen()
        viewModel.handleIntent(EntranceIntent.ScreenStarted)
    }

    override fun initObservers() {
        collectWhenStarted(viewModel.state, ::renderState)
        collectWhenCreated(viewModel.effect, ::handleEffect)
    }

    private fun renderState(state: EntranceState) {
        binding.cpiLoadingEntrance.isVisible = state.showLoading
    }

    private fun handleEffect(effect: EntranceEffect) {
        when (effect) {
            EntranceEffect.InitializeConsent -> initializeConsent()
            EntranceEffect.LoadAds -> loadAds()
            EntranceEffect.NavigateToLanguage -> showEntranceAd { findNavController().navigate(R.id.action_entranceFragment_to_languageFragment) }
            EntranceEffect.NavigateToOnboarding -> showEntranceAd { findNavController().navigate(R.id.action_entranceFragment_to_onboardingFragment) }
            EntranceEffect.NavigateToMenu -> showEntranceAd { findNavController().navigate(R.id.action_entranceFragment_to_menuFragment) }
        }
    }

    private fun initializeConsent() {
        val hostActivity = activity ?: return
        val manager = ConsentManager(hostActivity)
        consentManager = manager
        manager.start(
            listener = object : ConsentListener {
                override fun onConsentFormReady() {
                    launchWhenResumed {
                        manager.showForm()
                        viewModel.handleIntent(EntranceIntent.ConsentFormShown)
                    }
                }

                override fun onAdsAllowed(canRequestAds: Boolean) {
                    viewModel.handleIntent(EntranceIntent.AdsCanBeLoaded)
                }
            },
        )
    }

    private fun loadAds() {
        var remaining = 2
        val onSettled = {
            remaining -= 1
            if (remaining == 0) viewModel.handleIntent(EntranceIntent.AdsLoaded)
        }
        loadAppOpenAd(AppOpenAdKey.ENTRANCE) { onSettled() }
        loadInterstitialAd(InterstitialAdKey.ENTRANCE) { onSettled() }
        preloadFunnelAds()
    }

    private fun showEntranceAd(onDone: () -> Unit) {
        showAppOpenOrInterstitialAd(AppOpenAdKey.ENTRANCE, InterstitialAdKey.ENTRANCE, onDone)
    }

    private fun preloadFunnelAds() {
        viewLifecycleOwner.lifecycleScope.launch {
            if (!sharedPrefRepository.isFirstTimeUser()) return@launch
            if (sharedPrefRepository.isLanguageSelected()) return@launch
            loadNativeAd(NativeAdKey.LANGUAGE)
        }
    }

    override fun onDestroyView() {
        consentManager?.reset()
        consentManager = null
        super.onDestroyView()
    }
}