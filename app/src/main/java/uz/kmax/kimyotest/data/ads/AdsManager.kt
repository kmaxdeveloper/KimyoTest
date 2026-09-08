package uz.kmax.kimyotest.data.ads

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.nativead.NativeAdView as AdMobNativeAdView
import com.yandex.mobile.ads.nativeads.NativeAdView as YandexNativeAdView
import com.yandex.mobile.ads.nativeads.NativeAdViewBinder
import com.yandex.mobile.ads.nativeads.NativeAd as YandexNativeAd
import uz.kmax.kimyotest.databinding.ItemNativeAdBinding
import javax.inject.Inject

class AdsManager @Inject constructor(
    private val admobManager: AdmobManager,
    private val yandexManager: YandexAdsManager
) {
    private var statusYandexAds : Boolean = false
    private var statusAdmobAds : Boolean = false
    private var onAdDismissListener: (() -> Unit)? = null
    private var onAdClickListener: (() -> Unit)? = null
    private var onRewardedAdsDismissListener: ((reward: Int) -> Unit)? = null
    private var isAppOpenAdShowing = false

    /** Interface */
    fun setOnAdDismissListener(listener: () -> Unit) {
        onAdDismissListener = listener
    }

    fun setOnAdClickListener(listener: () -> Unit) {
        onAdClickListener = listener
    }

    fun setOnRewardedAdsDismissListener(listener: (reward: Int) -> Unit){
        onRewardedAdsDismissListener = listener
    }

    fun setOnAppOpenAdStatusListener(listener: (Boolean) -> Unit) {
        yandexManager.setOnAppOpenAdStatusListener { showing ->
            isAppOpenAdShowing = showing
            listener(showing)
        }
    }

    fun isAppOpenAdShowing() = isAppOpenAdShowing

    fun init(onAdLoadListener: ((type : String) -> Unit)? = null){
        admobManager.initialize()
        yandexManager.initYandexAds()
        admobManager.setOnAdLoadListener {
            statusAdmobAds = it
            if (it){
                onAdLoadListener?.invoke("Admob Ads is loaded !")
            }
        }

        yandexManager.setOnYandexAdLoadListener {
            statusYandexAds = it
            if (it){
                onAdLoadListener?.invoke("Yandex Ads is loaded !")
            }
        }

        optionalFunc()
    }

    private fun optionalFunc(){
        yandexManager.setOnYandexAdDismissListener {
            onAdDismissListener?.invoke()
        }

        yandexManager.setOnYandexAdClickListener {
            onAdClickListener?.invoke()
        }

        admobManager.setOnAdClickListener {
            onAdClickListener?.invoke()
        }

        admobManager.setOnAdDismissListener {
            onAdDismissListener?.invoke()
        }
    }

    private var adCounter = 0
    private var lastAdShowTime = 0L
    private val MIN_AD_INTERVAL = 45000L // 45 sekund

    fun showAds(activity: Activity, isBackPress: Boolean = false, onResultShowAds: ((Boolean) -> Unit)? = null) {
        val currentTime = System.currentTimeMillis()
        val timeSinceLastAd = currentTime - lastAdShowTime

        // 45 sekundlik intervalni tekshirish
        if (timeSinceLastAd < MIN_AD_INTERVAL) {
            onResultShowAds?.invoke(false)
            return
        }

        if (isBackPress) {
            // Orqaga qaytishda 100% reklama (faqat interval saqlangan holda)
            performShowAds(activity) { success ->
                if (success) lastAdShowTime = System.currentTimeMillis()
                onResultShowAds?.invoke(success)
            }
        } else {
            // Birinchi kirishda har 3-marta reklama ko'rsatish
            adCounter++
            if (adCounter % 3 == 0) {
                performShowAds(activity) { success ->
                    if (success) lastAdShowTime = System.currentTimeMillis()
                    onResultShowAds?.invoke(success)
                }
            } else {
                onResultShowAds?.invoke(false)
            }
        }
    }

    private fun performShowAds(activity: Activity, onResult: (Boolean) -> Unit) {
        if (admobManager.admobIsReady()) {
            admobManager.showInterstitialAd(activity) {
                onResult(it)
            }
        } else if (yandexManager.yandexIsReady()) {
            yandexManager.showYandexAds(activity) {
                onResult(it)
            }
        } else {
            onResult(false)
        }
    }

    fun loadBanners(adView: AdView){
        admobManager.loadBannerAd(adView)
    }

    fun initRewardedAds(){
        admobManager.initRewarded()
        // Yandex rewarded allaqachon init() ichida yandexManager.initYandexAds() orqali chaqirilgan

        admobManager.setOnRewardedAdsListener {
            when(it) {
                1->{
                    onRewardedAdsDismissListener?.invoke(3)
                }
                0->{}
                2->{}
                3->{}
                4->{}
            }
        }
    }

    fun showRewardedAds(activity: Activity,onResultShowAds: ((reward: Int,adsStatus: Boolean) -> Unit)? = null){
        if (admobManager.admobRewardedAdsIsReady()) {
            admobManager.showRewardedAds(activity){ reward, status ->
                onResultShowAds?.invoke(reward, status)
            }
        } else if (yandexManager.yandexRewardedIsReady()) {
            yandexManager.showYandexRewarded(activity) { reward, status ->
                onResultShowAds?.invoke(reward, status)
            }
        } else {
            onResultShowAds?.invoke(0, false)
        }
    }

    fun admobRewardedAdsReady(): Boolean{
        return admobManager.admobRewardedAdsIsReady() || yandexManager.yandexRewardedIsReady()
    }

    fun showAppOpenAd(activity: Activity) {
        if (yandexManager.yandexAppOpenIsReady()) {
            yandexManager.showAppOpenAd(activity)
        } else {
            yandexManager.loadAppOpenAd()
        }
    }

    fun loadNativeAd(parent: ViewGroup, onLoaded: (View?) -> Unit) {
        admobManager.loadNativeAd { admobNativeAd ->
            if (admobNativeAd != null) {
                val inflater = LayoutInflater.from(parent.context)
                val adView = AdMobNativeAdView(parent.context)
                val adBinding = ItemNativeAdBinding.inflate(inflater, adView, true)
                
                adView.headlineView = adBinding.adHeadline
                adView.bodyView = adBinding.adBody
                adView.callToActionView = adBinding.adCallToAction
                adView.iconView = adBinding.adAppIcon
                
                adBinding.adHeadline.text = admobNativeAd.headline
                adBinding.adBody.text = admobNativeAd.body
                adBinding.adCallToAction.text = admobNativeAd.callToAction
                
                // Rasm yuklash logikasi (Icon yoki birinchi rasm)
                if (admobNativeAd.icon != null) {
                    adBinding.adAppIcon.setImageDrawable(admobNativeAd.icon?.drawable)
                } else if (admobNativeAd.images.isNotEmpty()) {
                    adBinding.adAppIcon.setImageDrawable(admobNativeAd.images[0].drawable)
                }
                
                adView.setNativeAd(admobNativeAd)
                onLoaded(adView)
            } else {
                val activity = parent.context as? Activity ?: return@loadNativeAd onLoaded(null)
                yandexManager.loadNativeAd(activity) { yandexNativeAd ->
                    if (yandexNativeAd != null) {
                        val inflater = LayoutInflater.from(parent.context)
                        val yandexAdView = YandexNativeAdView(parent.context)
                        val adBinding = ItemNativeAdBinding.inflate(inflater, yandexAdView, true)
                        
                        val assets = yandexNativeAd.adAssets
                        adBinding.adHeadline.text = assets.title ?: ""
                        adBinding.adBody.text = assets.body ?: ""
                        adBinding.adCallToAction.text = assets.callToAction ?: ""
                        
                        // Yandex v7 uchun rasm logikasi
                        val binderBuilder = NativeAdViewBinder.Builder(yandexAdView)
                            .setTitleView(adBinding.adHeadline)
                            .setBodyView(adBinding.adBody)
                            .setCallToActionView(adBinding.adCallToAction)
                        
                        // Favicon yoki Icon mavjudligini tekshirish
                        if (assets.favicon != null) {
                            binderBuilder.setFaviconView(adBinding.adAppIcon)
                        } else if (assets.icon != null) {
                            binderBuilder.setIconView(adBinding.adAppIcon)
                        }
                        
                        val binder = binderBuilder.build()
                        
                        try {
                            yandexNativeAd.bindNativeAd(binder)
                            onLoaded(yandexAdView)
                        } catch (e: Exception) {
                            onLoaded(null)
                        }
                    } else {
                        onLoaded(null)
                    }
                }
            }
        }
    }
}
