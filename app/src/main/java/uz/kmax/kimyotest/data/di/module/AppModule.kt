package uz.kmax.kimyotest.data.di.module

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import uz.kmax.kimyotest.data.ads.AdmobManager
import uz.kmax.kimyotest.data.ads.AdsManager
import uz.kmax.kimyotest.data.ads.YandexAdsManager
import uz.kmax.kimyotest.data.tools.tools.SharedPref
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule{

    @Provides
    @Singleton
    fun provideAdmobManager(@ApplicationContext context: Context) : AdmobManager {
        return AdmobManager(context)
    }

    @Provides
    @Singleton
    fun provideSharedPref(@ApplicationContext context: Context) : SharedPref{
        return SharedPref(context)
    }

    @Provides
    @Singleton
    fun provideYandexManager(@ApplicationContext context: Context) : YandexAdsManager{
        return YandexAdsManager(context)
    }

    @Provides
    @Singleton
    fun provideAdsManager(
        admobManager: AdmobManager,
        yandexAdsManager: YandexAdsManager
    ) : AdsManager{
        return AdsManager(admobManager,yandexAdsManager)
    }
}