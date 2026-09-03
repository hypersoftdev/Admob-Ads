package com.hypersoft.ads.practice.dashboard

import android.view.MenuItem
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.onNavDestinationSelected
import androidx.navigation.ui.setupWithNavController
import com.hypersoft.ads.practice.R
import com.hypersoft.ads.practice.base.fragment.BaseFragment
import com.hypersoft.ads.practice.core.extensions.onBackPressedDispatcher
import com.hypersoft.ads.practice.databinding.FragmentDashboardBinding
import com.hypersoft.ads.practice.gmaAds.appOpen.AppOpenAdConfig
import com.hypersoft.ads.practice.gmaAds.appOpen.AppOpenAdKey
import com.hypersoft.ads.practice.gmaAds.banner.BannerAdKey
import com.hypersoft.ads.practice.gmaAds.common.extensions.destroyBannerAd
import com.hypersoft.ads.practice.gmaAds.common.extensions.loadAppOpenAd
import com.hypersoft.ads.practice.gmaAds.common.extensions.loadBannerAd
import com.hypersoft.ads.practice.gmaAds.common.extensions.loadInterstitialAd
import com.hypersoft.ads.practice.gmaAds.common.extensions.pauseBannerAd
import com.hypersoft.ads.practice.gmaAds.common.extensions.resumeBannerAd
import com.hypersoft.ads.practice.gmaAds.common.extensions.showInterstitialAd
import com.hypersoft.ads.practice.gmaAds.common.extensions.unblockAppOpen
import com.hypersoft.ads.practice.gmaAds.interstitial.InterstitialAdKey

class DashboardFragment : BaseFragment<FragmentDashboardBinding>(FragmentDashboardBinding::inflate) {

    private val navController by lazy { (childFragmentManager.findFragmentById(binding.fcvContainerDashboard.id) as NavHostFragment).navController }

    override fun onViewCreated() {
        setupBottomNavigation()
        loadAds()
    }

    override fun onResume() {
        super.onResume()
        registerBackPress()
        resumeBannerAd(BannerAdKey.DASHBOARD)
    }

    override fun onPause() {
        pauseBannerAd(BannerAdKey.DASHBOARD)
        super.onPause()
    }

    override fun onDestroyView() {
        binding.bannerAdViewDashboard.clearView()
        super.onDestroyView()
    }

    override fun onDestroy() {
        destroyBannerAd(BannerAdKey.DASHBOARD)
        super.onDestroy()
    }

    private fun setupBottomNavigation() {
        binding.bnvDashboard.setupWithNavController(navController)
        binding.bnvDashboard.setOnItemSelectedListener { item ->
            val currentDestinationId = navController.currentDestination?.id
            if (currentDestinationId != null && currentDestinationId != item.itemId) {
                showInterstitial(item)
                false
            } else {
                item.onNavDestinationSelected(navController)
            }
        }
    }

    private fun loadAds() {
        loadBanner()
        loadInterstitials()
        loadAppOpen()
    }

    private fun loadBanner() {
        loadBannerAd(BannerAdKey.DASHBOARD, binding.bannerAdViewDashboard)
    }

    private fun loadInterstitials() {
        loadInterstitialAd(InterstitialAdKey.BOTTOM_NAVIGATION)
        loadInterstitialAd(InterstitialAdKey.EXIT)
    }

    private fun loadAppOpen() {
        unblockAppOpen()
        loadAppOpenAd(AppOpenAdKey.LIFECYCLE)
    }

    private fun showInterstitial(item: MenuItem) {
        showInterstitialAd(InterstitialAdKey.BOTTOM_NAVIGATION) {
            loadInterstitialAd(InterstitialAdKey.BOTTOM_NAVIGATION)
            item.onNavDestinationSelected(navController)
        }
    }

    private fun registerBackPress() {
        onBackPressedDispatcher {
            if (!navController.popBackStack()) {
                navigateToExit()
            }
        }
    }

    private fun navigateToExit() {
        showInterstitialAd(InterstitialAdKey.EXIT) {
            val rootNavController = findNavController()
            if (rootNavController.currentDestination?.id == R.id.dashboardFragment) {
                rootNavController.navigate(R.id.action_dashboardFragment_to_exitFragment)
            }
        }
    }
}