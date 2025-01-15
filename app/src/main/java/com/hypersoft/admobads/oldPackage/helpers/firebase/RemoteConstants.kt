package com.hypersoft.admobads.oldPackage.helpers.firebase

/**
 * @Author: Muhammad Yaqoob
 * @Date: 14,March,2024.
 * @Accounts
 *      -> https://github.com/orbitalsonic
 *      -> https://www.linkedin.com/in/myaqoob7
 */
object RemoteConstants {

    /**
     * Interstitial Remote Config keys
     */

    const val INTERSTITIAL_AD_KEY = "interstitial_ad"

    /**
     * Interstitial Remote Constants
     *  -> rcv:  denotes 'remote configuration values'
     *  -> Note
     *         0:   Ads off
     *         1:   Admob Active
     */

    var rcvInterAd: Int = 1


    /**
     * Rewarded Remote Config keys
     */
    const val REWARDED_AD_KEY = "rewarded_ad"
    const val REWARDED_INTER_AD_KEY = "rewarded_inter_ad"

    /**
     * Rewarded Remote Constants
     *  -> rcv:  denotes 'remote configuration values'
     *  -> Note
     *         0:   Ads off
     *         1:   Admob Active
     */

    var rcvRewardAd: Int = 1
    var rcvRewardInterAd: Int = 1

    /**
     * Native Remote Config keys
     */
    const val NATIVE_AD_KEY = "native_ad"

    /**
     * Native Remote Constants
     *  -> rcv:  denotes 'remote configuration values'
     *  -> Note
     *         0:   Ads off
     *         1:   Admob Active
     */

    var rcvNativeAd: Int = 1


    /**
     * Banner Remote Config keys
     */
    const val BANNER_AD_KEY = "banner_ad"

    /**
     * Banner Remote Constants
     *  -> rcv:  denotes 'remote configuration values'
     *  -> Note
     *         0:   Ads off
     *         1:   Collapsible
     *         2:   Adaptive
     */

    var rcvBannerAd: Int = 1


    /**
     * AppOpen Remote Config keys
     */
    const val APP_OPEN_KEY = "app_open_ad"

    /**
     * OpenApp Remote Constants
     *  -> rcv:  denotes 'remote configuration values'
     *  -> Note
     *         0:   Ads off
     *         1:   Admob Active
     */

    var rcvAppOpen: Int = 1

    /**
     * Others Remote Config keys
     */
    const val COUNTER_KEY = "counter"

    /**
     * Others Remote Constants
     */

    var rcvRemoteCounter: Int = 3
    var totalCount : Int = 3

}