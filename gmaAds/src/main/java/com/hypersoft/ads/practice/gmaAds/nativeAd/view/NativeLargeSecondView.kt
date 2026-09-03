package com.hypersoft.ads.practice.gmaAds.nativeAd.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import androidx.core.view.isVisible
import com.google.android.gms.ads.nativead.NativeAd
import com.hypersoft.ads.practice.gmaAds.databinding.ViewNativeLargeSecondBinding

class NativeLargeSecondView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : FrameLayout(context, attrs, defStyleAttr), NativeContainerView {

    private val binding = ViewNativeLargeSecondBinding.inflate(LayoutInflater.from(context), this, true)

    override fun setNativeAd(nativeAd: NativeAd) {
        binding.mtvLoadingAds.visibility = GONE
        binding.adAttribute.visibility = VISIBLE
        binding.adCallToAction.visibility = VISIBLE

        binding.nativeAdView.mediaView = binding.adMediaView
        binding.nativeAdView.iconView = binding.adAppIcon
        binding.nativeAdView.headlineView = binding.adHeadline
        binding.nativeAdView.bodyView = binding.adBody
        binding.nativeAdView.callToActionView = binding.adCallToAction

        binding.adHeadline.text = nativeAd.headline
        binding.adBody.text = nativeAd.body
        binding.adCallToAction.text = nativeAd.callToAction
        binding.adAppIcon.setImageDrawable(nativeAd.icon?.drawable)

        binding.adAppIcon.isVisible = nativeAd.icon?.drawable != null
        binding.adCallToAction.isVisible = nativeAd.callToAction.isNullOrEmpty().not()

        visibility = VISIBLE
        binding.nativeAdView.setNativeAd(nativeAd)
    }

    override fun clearView() {
        binding.mtvLoadingAds.visibility = VISIBLE
        binding.adAttribute.visibility = GONE
        binding.adCallToAction.visibility = GONE
    }
}
