package uz.kmax.kimyotest.data.ads

import android.app.Activity
import android.content.Context
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.OnUserEarnedRewardListener
import com.google.android.gms.ads.VideoOptions
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdOptions
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback

class AdmobManager(private var context: Context) {
    private var interstitialAd: InterstitialAd? = null
    private var rewardedAd: RewardedAd? = null
    private var adRequest: AdRequest = AdRequest.Builder().build()
    private var isAdLoading: Boolean = false
    private var isRewardedAdLoading = false
    private var lastAdShowTime: Long = 0
    private val adShowInterval: Long = 30000
    private val AD_UNIT_ID = "ca-app-pub-4664801446868642/5382958969"
    private val AD_UNIT_ID_REWARDED = "ca-app-pub-4664801446868642/6492575246"
    private val AD_UNIT_ID_NATIVE = "ca-app-pub-4664801446868642/8896842613" // Placeholder ID

    private var onAdDismissListener: (() -> Unit)? = null
    private var onAdClickListener: (() -> Unit)? = null
    private var onAdLoadListener: ((Boolean) -> Unit)? = null
    private var onRewardedListener: ((type : Int)->Unit)? = null

    fun initialize() {
        MobileAds.initialize(context) {}
        loadInterstitialAd()
    }

    fun initRewarded(){
        loadRewardedAds()
    }

    fun setOnAdDismissListener(listener: () -> Unit) {
        onAdDismissListener = listener
    }

    fun setOnAdClickListener(listener: () -> Unit) {
        onAdClickListener = listener
    }

    fun setOnAdLoadListener(listener: (Boolean) -> Unit) {
        onAdLoadListener = listener
    }

    fun setOnRewardedAdsListener(listener: (reward: Int)-> Unit){
        onRewardedListener = listener
    }

    private fun loadRewardedAds(){
        RewardedAd.load(
            context,
            AD_UNIT_ID_REWARDED,
            AdRequest.Builder().build(),
            object : RewardedAdLoadCallback() {
                override fun onAdLoaded(ad: RewardedAd) {
                    isRewardedAdLoading = true
                    rewardedAd = ad
                    setupCallbacksRewardedAds()
                }

                override fun onAdFailedToLoad(adError: LoadAdError) {
                    isRewardedAdLoading = false
                    rewardedAd = null
                    // 5 soniyadan keyin qayta yuklashga urinish
                    android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                        loadRewardedAds()
                    }, 5000)
                }
            },
        )
    }

    private fun loadInterstitialAd() {
        if (isAdLoading) { return }
        isAdLoading = true
        InterstitialAd.load(context, AD_UNIT_ID, adRequest, object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    interstitialAd = ad
                    isAdLoading = false
                    setupAdCallbacks()
                    onAdLoadListener?.invoke(true)
                }

                override fun onAdFailedToLoad(adError: LoadAdError) {
                    interstitialAd = null
                    isAdLoading = false
                    onAdLoadListener?.invoke(false)
                }
            }
        )
    }

    private fun setupAdCallbacks() {
        interstitialAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdClicked() {
                onAdClickListener?.invoke()
            }

            override fun onAdDismissedFullScreenContent() {
                interstitialAd = null
                lastAdShowTime = System.currentTimeMillis()
                loadInterstitialAd()
                onAdDismissListener?.invoke()
            }

            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                interstitialAd = null
                loadInterstitialAd()
                onAdDismissListener?.invoke()
            }

            override fun onAdShowedFullScreenContent() {
                interstitialAd = null
                loadInterstitialAd()
            }
        }
    }

    private fun setupCallbacksRewardedAds(){
        rewardedAd?.fullScreenContentCallback =
            object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    rewardedAd = null
                    loadRewardedAds()
                    onRewardedListener?.invoke(1)
                }

                override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                    rewardedAd = null
                    loadRewardedAds()
                    onRewardedListener?.invoke(0)
                }

                override fun onAdShowedFullScreenContent() {
                    rewardedAd = null
                    loadRewardedAds()
                    onRewardedListener?.invoke(2)
                }

                override fun onAdImpression() {
                    onRewardedListener?.invoke(3)
                }

                override fun onAdClicked() {
                    onRewardedListener?.invoke(4)
                }
            }
    }

    fun showInterstitialAd(activity: Activity,onResultShowAds: ((boolean:Boolean) -> Unit)? = null) {
        if (interstitialAd != null) {
            interstitialAd?.show(activity)
            onResultShowAds?.invoke(true)
        } else {
            onResultShowAds?.invoke(false)
        }
    }

    fun loadBannerAd(adView: AdView,onResultShowAds: ((adsClicked:Boolean) -> Unit)? = null) {
        adView.loadAd(adRequest)
        adView.adListener = object : AdListener() {
            override fun onAdClicked() {
                onResultShowAds?.invoke(true)
            }
            override fun onAdClosed() {}
            override fun onAdFailedToLoad(adError: LoadAdError) {}
            override fun onAdLoaded() {}
            override fun onAdOpened() {}
        }
    }

    fun showRewardedAds(activity: Activity,onResultShowAds: ((reward:Int, adsStatus : Boolean) -> Unit)? = null){
        if (rewardedAd != null) {
            rewardedAd?.show(
                activity,
                OnUserEarnedRewardListener { rewardItem ->
                    val rewardAmount: Int = rewardItem.amount
                    onResultShowAds?.invoke(rewardAmount,true)
                    val rewardType = rewardItem.type
                }
            )
        }else{
            onResultShowAds?.invoke(0,false)
        }
    }

    fun admobIsReady() : Boolean {
        val currentTime = System.currentTimeMillis()
        return interstitialAd != null && (currentTime - lastAdShowTime) >= adShowInterval
    }

    fun admobRewardedAdsIsReady() : Boolean {
        return rewardedAd != null
    }

    fun loadNativeAd(onLoaded: (NativeAd?) -> Unit) {
        val adLoader = com.google.android.gms.ads.AdLoader.Builder(context, AD_UNIT_ID_NATIVE)
            .forNativeAd { nativeAd ->
                onLoaded(nativeAd)
            }
            .withAdListener(object : com.google.android.gms.ads.AdListener() {
                override fun onAdFailedToLoad(adError: LoadAdError) {
                    onLoaded(null)
                }
            })
            .withNativeAdOptions(NativeAdOptions.Builder()
                .setVideoOptions(VideoOptions.Builder().setStartMuted(true).build())
                .build())
            .build()

        adLoader.loadAd(AdRequest.Builder().build())
    }
}