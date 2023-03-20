package com.hypersoft.admobads.helpers.firebase

object RemoteConstants {

    /**
     * Remote Config keys
     */

    const val INTER_SPLASH_KEY = "interSplash"
    const val INTER_MAIN_KEY = "interMain"
    const val NATIVE_SPLASH_KEY = "nativeSplash"
    const val NATIVE_HOME_KEY = "nativeHome"
    const val NATIVE_SAMPLE_KEY = "nativeSample"
    const val BANNER_HOME_KEY = "bannerHome"
    const val BANNER_COLLAPSIBLE_KEY = "bannerCollapsible"
    const val OPEN_APP_AD_KEY = "openAppAd"
    const val REMOTE_COUNTER_KEY = "remoteCounter"


    /**
     * Constants
     *  -> rcv:  denotes 'remote configuration values'
     *  -> Note
     *         0:   Ads off
     *         1:   Admob Active
     *         2:   Facebook Active
     */

    var rcvInterSplash: Int = 0
    var rcvInterMain: Int = 0
    var rcvNativeSplash: Int = 0
    var rcvNativeHome: Int = 0
    var rcvNativeSample: Int = 0
    var rcvBannerHome: Int = 0
    var rcvBannerCollapsible: Int = 0
    var rcvOpenApp: Int = 0
    var rcvRemoteCounter: Int = 2
    var totalCount : Int = 0



    fun reset() {
        rcvInterSplash = 0
        rcvInterMain = 0
        rcvNativeSplash = 0
        rcvNativeHome = 0
        rcvNativeSample = 0
        rcvBannerHome = 0
        rcvBannerCollapsible = 0
        rcvOpenApp = 0
        rcvRemoteCounter = 0
        totalCount = 0
    }
}