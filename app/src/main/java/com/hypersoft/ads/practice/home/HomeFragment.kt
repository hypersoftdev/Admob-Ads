package com.hypersoft.ads.practice.home

import android.widget.Toast
import androidx.navigation.findNavController
import com.hypersoft.ads.practice.R
import com.hypersoft.ads.practice.base.fragment.BaseFragment
import com.hypersoft.ads.practice.databinding.FragmentHomeBinding
import com.hypersoft.ads.practice.gmaAds.common.extensions.loadInterstitialAd
import com.hypersoft.ads.practice.gmaAds.common.extensions.loadNativeAd
import com.hypersoft.ads.practice.gmaAds.common.extensions.loadRewardedAd
import com.hypersoft.ads.practice.gmaAds.common.extensions.loadRewardedInterstitialAd
import com.hypersoft.ads.practice.gmaAds.common.extensions.showInterstitialAd
import com.hypersoft.ads.practice.gmaAds.common.extensions.showRewardedAd
import com.hypersoft.ads.practice.gmaAds.common.extensions.showRewardedInterstitialAd
import com.hypersoft.ads.practice.gmaAds.interstitial.InterstitialAdKey
import com.hypersoft.ads.practice.gmaAds.nativeAd.NativeAdKey
import com.hypersoft.ads.practice.gmaAds.rewarded.RewardedAdKey
import com.hypersoft.ads.practice.gmaAds.rewardedInterstitial.RewardedInterstitialAdKey

class HomeFragment : BaseFragment<FragmentHomeBinding>(FragmentHomeBinding::inflate) {

    override fun onViewCreated() {
        loadAds()

        binding.btnFeatureOneHome.setOnClickListener { showInterstitialAd(InterstitialAdKey.HOME) { navigateOnRoot(R.id.action_dashboardFragment_to_featureOneFragment) } }
        binding.btnFeatureTwoHome.setOnClickListener { showInterstitialAd(InterstitialAdKey.HOME) { navigateOnRoot(R.id.action_dashboardFragment_to_featureTwoFragment) } }
        binding.btnRewardedHome.setOnClickListener { showRewardedAd(RewardedAdKey.HOME) { granted -> showToast(if (granted) "Rewarded ad: Reward granted" else "Rewarded ad: Reward not granted") } }
        binding.btnRewardedInterstitialHome.setOnClickListener { showRewardedInterstitialAd(RewardedInterstitialAdKey.HOME) { granted -> showToast(if (granted) "Rewarded Interstitial ad: Reward granted" else "Rewarded Interstitial ad: Reward not granted") } }
    }

    private fun loadAds() {
        loadNativeAd(NativeAdKey.HOME, binding.nativeAdViewHome)
        loadInterstitialAd(InterstitialAdKey.HOME)
        loadRewardedAd(RewardedAdKey.HOME)
        loadRewardedInterstitialAd(RewardedInterstitialAdKey.HOME)
    }

    private fun navigateOnRoot(actionId: Int) {
        val rootNavController = requireActivity().findNavController(R.id.fcvContainerMain)
        if (rootNavController.currentDestination?.id == R.id.dashboardFragment) {
            rootNavController.navigate(actionId)
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }
}