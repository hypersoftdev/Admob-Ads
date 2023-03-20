package com.hypersoft.admobads.ui.fragments.sample

import com.hypersoft.admobads.R
import com.hypersoft.admobads.databinding.FragmentBannerBinding
import com.hypersoft.admobads.adsconfig.AdmobBannerAds
import com.hypersoft.admobads.adsconfig.callbacks.BannerCallBack
import com.hypersoft.admobads.adsconfig.enums.CollapsiblePositionType
import com.hypersoft.admobads.helpers.firebase.RemoteConstants
import com.hypersoft.admobads.helpers.observers.SingleLiveEvent
import com.hypersoft.admobads.ui.fragments.base.BaseFragment

class FragmentBanner : BaseFragment<FragmentBannerBinding>(R.layout.fragment_banner) {

    /**
     * Don't use AdmobBannerAds in DI
     */
    private val admobBannerAds by lazy { AdmobBannerAds() }
    private val adsObserver = SingleLiveEvent<Boolean>()
    private var isCollapsibleOpen = false
    private var isBackPressed = false

    override fun onViewCreatedOneTime() {
        loadAds()
    }

    override fun onViewCreatedEverytime() {
        initObserver()
    }

    private fun initObserver(){
        adsObserver.observe(this){
            if (it){
                onBack()
            }
        }
    }

    override fun navIconBackPressed() {
        onBackPressed()
    }

    override fun onBackPressed() {
        if (isAdded){
            try {
                if (!isBackPressed){
                    isBackPressed = true
                    if (isCollapsibleOpen){
                        admobBannerAds.bannerOnDestroy()
                        binding.adsBannerPlaceHolder.removeAllViews()
                    }else{
                        onBack()
                    }
                }
            }catch (ex:Exception){
                isBackPressed = false
            }
        }
    }

    private fun onBack(){
        popFrom(R.id.fragmentBanner)
    }

    private fun loadAds(){
        admobBannerAds.loadBannerAds(
            activity,
            binding.adsBannerPlaceHolder,
            getResString(R.string.admob_banner_collapse_id),
            RemoteConstants.rcvBannerCollapsible,
            diComponent.sharedPreferenceUtils.isAppPurchased,
            diComponent.internetManager.isInternetConnected,
            CollapsiblePositionType.BOTTOM,
            object : BannerCallBack {
                override fun onAdFailedToLoad(adError: String) {}
                override fun onAdLoaded() {}
                override fun onAdImpression() {}
                override fun onPreloaded() {}
                override fun onAdClicked() {}
                override fun onAdSwipeGestureClicked() {}
                override fun onAdClosed() {
                    isCollapsibleOpen = false

                    if (isBackPressed){
                        adsObserver.value = true
                    }
                }

                override fun onAdOpened() {
                    isCollapsibleOpen = true
                }


            }
        )
    }

    override fun onPause() {
        admobBannerAds.bannerOnPause()
        super.onPause()
    }

    override fun onResume() {
        admobBannerAds.bannerOnResume()
        super.onResume()
    }

    override fun onDestroy() {
        admobBannerAds.bannerOnDestroy()
        super.onDestroy()
    }
}