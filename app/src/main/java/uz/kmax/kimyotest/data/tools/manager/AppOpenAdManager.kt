package uz.kmax.kimyotest.data.tools.manager

import android.app.Activity
import android.app.Application
import android.os.Bundle
import android.view.Window
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import uz.kmax.kimyotest.data.ads.AdsManager
import uz.kmax.kimyotest.data.tools.tools.SharedPref
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppOpenAdManager @Inject constructor(
    private val adsManager: AdsManager,
    private val sharedPref: SharedPref
) : DefaultLifecycleObserver, Application.ActivityLifecycleCallbacks {

    private var currentActivity: Activity? = null
    private var isAdShowing = false
    private var lastAdTime: Long = 0
    private val MIN_INTERVAL = 30000L 
    private val DAILY_LIMIT = 20

    private val KEY_AD_COUNT = "app_open_ad_count"
    private val KEY_LAST_AD_DATE = "app_open_ad_date"

    fun register(application: Application) {
        application.registerActivityLifecycleCallbacks(this)
        try {
            ProcessLifecycleOwner.get().lifecycle.addObserver(this)
        } catch (e: Exception) {}
    }

    override fun onStart(owner: LifecycleOwner) {
        super.onStart(owner)
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastAdTime > MIN_INTERVAL) {
            if (canShowAdToday()) {
                showAdIfAvailable()
            }
        }
    }

    private fun canShowAdToday(): Boolean {
        val sdf = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
        val today = sdf.format(Date())
        val lastDate = sharedPref.getString(KEY_LAST_AD_DATE, "")

        if (today != lastDate) {
            sharedPref.saveString(KEY_LAST_AD_DATE, today)
            sharedPref.saveInt(KEY_AD_COUNT, 0)
            return true
        }

        val count = sharedPref.getInt(KEY_AD_COUNT, 0)
        return count < DAILY_LIMIT
    }

    private fun incrementAdCount() {
        val count = sharedPref.getInt(KEY_AD_COUNT, 0)
        sharedPref.saveInt(KEY_AD_COUNT, count + 1)
    }

    private fun showAdIfAvailable() {
        currentActivity?.let { activity ->
            if (!isAdShowing) {
                adsManager.showAppOpenAd(activity)
                lastAdTime = System.currentTimeMillis()
                incrementAdCount()
            }
        }
    }

    private fun hideSystemUI(window: Window) {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        val controller = WindowInsetsControllerCompat(window, window.decorView)
        controller.hide(WindowInsetsCompat.Type.systemBars())
        controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
    }

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
        currentActivity = activity
        hideSystemUI(activity.window)
    }

    override fun onActivityStarted(activity: Activity) {
        currentActivity = activity
        hideSystemUI(activity.window)
    }

    override fun onActivityResumed(activity: Activity) {
        currentActivity = activity
        hideSystemUI(activity.window)
    }

    override fun onActivityPaused(activity: Activity) {}

    override fun onActivityStopped(activity: Activity) {}

    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}

    override fun onActivityDestroyed(activity: Activity) {
        if (currentActivity == activity) {
            currentActivity = null
        }
    }
}
