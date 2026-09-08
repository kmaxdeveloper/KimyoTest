package uz.kmax.kimyotest.data.ads

import android.app.Activity
import android.content.Context
import com.yandex.mobile.ads.common.AdError
import com.yandex.mobile.ads.common.AdRequestConfiguration
import com.yandex.mobile.ads.common.AdRequestError
import com.yandex.mobile.ads.common.ImpressionData
import com.yandex.mobile.ads.common.MobileAds
import com.yandex.mobile.ads.interstitial.InterstitialAd
import com.yandex.mobile.ads.interstitial.InterstitialAdEventListener
import com.yandex.mobile.ads.interstitial.InterstitialAdLoadListener
import com.yandex.mobile.ads.interstitial.InterstitialAdLoader
import com.yandex.mobile.ads.appopenad.AppOpenAd
import com.yandex.mobile.ads.appopenad.AppOpenAdEventListener
import com.yandex.mobile.ads.appopenad.AppOpenAdLoadListener
import com.yandex.mobile.ads.appopenad.AppOpenAdLoader
import com.yandex.mobile.ads.nativeads.NativeAd
import com.yandex.mobile.ads.nativeads.NativeAdLoadListener
import com.yandex.mobile.ads.nativeads.NativeAdLoader
import com.yandex.mobile.ads.nativeads.NativeAdRequestConfiguration
import com.yandex.mobile.ads.rewarded.Reward
import com.yandex.mobile.ads.rewarded.RewardedAd
import com.yandex.mobile.ads.rewarded.RewardedAdEventListener
import com.yandex.mobile.ads.rewarded.RewardedAdLoadListener
import com.yandex.mobile.ads.rewarded.RewardedAdLoader

class YandexAdsManager(private var context: Context) {
    private var interstitialAd: InterstitialAd? = null
    private var interstitialAdsLoader : InterstitialAdLoader? = null
    
    private var rewardedAd: RewardedAd? = null
    private var rewardedAdLoader: RewardedAdLoader? = null
    
    private var appOpenAd: AppOpenAd? = null
    private var appOpenAdLoader: AppOpenAdLoader? = null

    private var nativeAdLoader: NativeAdLoader? = null
    
    private var adUnit = "R-M-17326172-1"
    private var rewardedAdUnitId = "R-M-17326172-2" 
    private var appOpenAdUnitId = "R-M-17326172-5"
    private var nativeAdUnitId = "R-M-17326172-3" // Placeholder ID
    private var lastAdShowTime: Long = 0
    private val adShowInterval: Long = 20000
    private var onYandexAdLoadListener: ((Boolean) -> Unit)? = null
    private var onYandexAdDismissListener: (() -> Unit)? = null
    private var onYandexAdClickListener: (() -> Unit)? = null
    
    private var onAppOpenAdStatusListener: ((Boolean) -> Unit)? = null
    
    private var onYandexRewardedListener: ((reward: Int, success: Boolean) -> Unit)? = null

    fun setOnAppOpenAdStatusListener(listener: (Boolean) -> Unit) {
        onAppOpenAdStatusListener = listener
    }

    fun setOnYandexAdLoadListener(listener: (Boolean) -> Unit) {
        onYandexAdLoadListener = listener
    }

    fun setOnYandexAdDismissListener(listener: () -> Unit) {
        onYandexAdDismissListener = listener
    }

    fun setOnYandexAdClickListener(listener: () -> Unit) {
        onYandexAdClickListener = listener
    }

    fun initYandexAds(){
        MobileAds.initialize(context){
            interstitialAdsLoader = InterstitialAdLoader(context).apply {
                setAdLoadListener(object : InterstitialAdLoadListener{
                    override fun onAdFailedToLoad(error: AdRequestError) {
                        onYandexAdLoadListener?.invoke(false)
                    }

                    override fun onAdLoaded(ads: InterstitialAd) {
                        interstitialAd = ads
                        onYandexAdLoadListener?.invoke(true)
                        adsCallsBack()
                    }

                })
            }

            loadInterstitialAds()
            initRewardedAds()
            initAppOpenAds()
        }
    }

    private fun initAppOpenAds() {
        appOpenAdLoader = AppOpenAdLoader(context).apply {
            setAdLoadListener(object : AppOpenAdLoadListener {
                override fun onAdLoaded(ad: AppOpenAd) {
                    appOpenAd = ad
                    setupAppOpenCallbacks()
                }

                override fun onAdFailedToLoad(error: AdRequestError) {
                    appOpenAd = null
                }
            })
        }
        loadAppOpenAd()
    }

    fun loadAppOpenAd() {
        val adRequestConfiguration = AdRequestConfiguration.Builder(appOpenAdUnitId).build()
        appOpenAdLoader?.loadAd(adRequestConfiguration)
    }

    private fun setupAppOpenCallbacks() {
        appOpenAd?.setAdEventListener(object : AppOpenAdEventListener {
            override fun onAdShown() {
                onAppOpenAdStatusListener?.invoke(true)
            }
            override fun onAdFailedToShow(error: AdError) {
                onAppOpenAdStatusListener?.invoke(false)
                appOpenAd = null
                loadAppOpenAd()
            }

            override fun onAdDismissed() {
                onAppOpenAdStatusListener?.invoke(false)
                appOpenAd = null
                loadAppOpenAd()
            }

            override fun onAdClicked() {}
            override fun onAdImpression(data: ImpressionData?) {}
        })
    }

    fun showAppOpenAd(activity: Activity) {
        if (appOpenAd != null) {
            appOpenAd?.show(activity)
        } else {
            loadAppOpenAd()
        }
    }

    fun yandexAppOpenIsReady(): Boolean = appOpenAd != null

    private fun initRewardedAds() {
        rewardedAdLoader = RewardedAdLoader(context).apply {
            setAdLoadListener(object : RewardedAdLoadListener {
                override fun onAdLoaded(ad: RewardedAd) {
                    rewardedAd = ad
                    setupRewardedCallbacks()
                }

                override fun onAdFailedToLoad(error: AdRequestError) {
                    rewardedAd = null
                }
            })
        }
        loadRewardedAd()
    }

    private fun loadRewardedAd() {
        val adRequestConfiguration = AdRequestConfiguration.Builder(rewardedAdUnitId).build()
        rewardedAdLoader?.loadAd(adRequestConfiguration)
    }

    private fun setupRewardedCallbacks() {
        rewardedAd?.setAdEventListener(object : RewardedAdEventListener {
            override fun onAdShown() {}
            override fun onAdFailedToShow(error: AdError) {
                rewardedAd = null
                onYandexRewardedListener?.invoke(0, false)
                loadRewardedAd()
            }

            override fun onAdDismissed() {
                rewardedAd = null
                onYandexRewardedListener?.invoke(0, false)
                loadRewardedAd()
            }

            override fun onAdClicked() {}
            override fun onAdImpression(data: ImpressionData?) {}
            override fun onRewarded(reward: Reward) {
                onYandexRewardedListener?.invoke(reward.amount, true)
            }
        })
    }

    fun showYandexRewarded(activity: Activity, onResult: (reward: Int, success: Boolean) -> Unit) {
        onYandexRewardedListener = onResult
        if (rewardedAd != null) {
            rewardedAd?.show(activity)
        } else {
            onResult(0, false)
            loadRewardedAd()
        }
    }

    fun yandexRewardedIsReady(): Boolean = rewardedAd != null

    private fun loadInterstitialAds() {
        val adRequestConfiguration = AdRequestConfiguration.Builder(adUnit).build()
        interstitialAdsLoader?.loadAd(adRequestConfiguration)
    }

    fun adsCallsBack(){
        interstitialAd?.apply {
            setAdEventListener(object : InterstitialAdEventListener{
                override fun onAdClicked() {
                    onYandexAdClickListener?.invoke()
                }
                override fun onAdDismissed() {
                    destroyInterstitialAd()
                    onYandexAdDismissListener?.invoke()
                    lastAdShowTime = System.currentTimeMillis()
                    loadInterstitialAds()
                }

                override fun onAdFailedToShow(adError: AdError) {
                    destroyInterstitialAd()
                    onYandexAdDismissListener?.invoke()
                    loadInterstitialAds()
                }
                override fun onAdImpression(impressionData: ImpressionData?) {}
                override fun onAdShown() {}

            })
        }
    }

    fun showYandexAds(activity: Activity, onResultShowAds: ((boolean:Boolean) -> Unit)? = null){
        if (interstitialAd !=null){
            interstitialAd?.show(activity)
            onResultShowAds?.invoke(true)
        }else{
            onResultShowAds?.invoke(false)
        }
    }

    fun yandexIsReady() : Boolean{
        val currentTime = System.currentTimeMillis()
        return interstitialAd != null && (currentTime - lastAdShowTime) >= adShowInterval
    }

    fun destroy(){
        interstitialAdsLoader?.setAdLoadListener(null)
        interstitialAdsLoader = null
        
        rewardedAdLoader?.setAdLoadListener(null)
        rewardedAdLoader = null
        
        appOpenAdLoader?.setAdLoadListener(null)
        appOpenAdLoader = null

        destroyInterstitialAd()
        destroyRewardedAd()
        destroyAppOpenAd()
    }

    private fun destroyInterstitialAd() {
        interstitialAd?.setAdEventListener(null)
        interstitialAd = null
    }
    
    private fun destroyRewardedAd() {
        rewardedAd?.setAdEventListener(null)
        rewardedAd = null
    }

    private fun destroyAppOpenAd() {
        appOpenAd?.setAdEventListener(null)
        appOpenAd = null
    }

    fun loadNativeAd(activity: Activity, onLoaded: (NativeAd?) -> Unit) {

        if (nativeAdLoader == null) {
            nativeAdLoader = NativeAdLoader(activity)
        }

        nativeAdLoader?.setNativeAdLoadListener(object : NativeAdLoadListener {
            override fun onAdLoaded(nativeAd: NativeAd) {
                onLoaded(nativeAd)
            }

            override fun onAdFailedToLoad(error: AdRequestError) {
                onLoaded(null)
            }
        })

        val adRequest = NativeAdRequestConfiguration.Builder(nativeAdUnitId).build()

        nativeAdLoader?.loadAd(adRequest)
    }

}