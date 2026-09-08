# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# ─── Hilt / Dagger ─────────────────────────────────────────────────────────────
-keep class androidx.hilt.lifecycle.ViewModelFactoryModules { *; }
-keep class * extends androidx.lifecycle.ViewModel
-keep class * extends dagger.hilt.components.SingletonComponent
-keep @dagger.hilt.android.lifecycle.HiltViewModel class *

# ─── Firebase ─────────────────────────────────────────────────────────────────
-keep class com.google.firebase.** { *; }
-keepattributes Signature
-keepattributes *Annotation*

# ─── Google Play Services & AdMob ─────────────────────────────────────────────
-keep class com.google.android.gms.ads.** { *; }
-keep class com.google.android.gms.common.** { *; }

# AdMob Mediation: IronSource
-keepclassmembers class com.ironsource.sdk.controller.IronSourceWebView$JSInterface {
    public *;
}
-keep class com.ironsource.** { *; }
-dontwarn com.ironsource.**

# AdMob Mediation: Facebook (Meta)
-keep class com.facebook.ads.** { *; }

# AdMob Mediation: Unity
-keep class com.unity3d.ads.** { *; }

# ─── Yandex Mobile Ads ────────────────────────────────────────────────────────
-keep class com.yandex.mobile.ads.** { *; }
-dontwarn com.yandex.mobile.ads.**

# ─── Lottie & Konfetti ────────────────────────────────────────────────────────
-keep class com.airbnb.lottie.** { *; }
-keep class nl.dionsegijn.konfetti.** { *; }

# ─── Kotlin Serialization / Coroutines ────────────────────────────────────────
-keepnames class kotlinx.serialization.internal.EnumSerializer
-keepclassmembernames class kotlinx.coroutines.android.HandlerContext {
    private static java.util.concurrent.atomic.AtomicIntegerFieldUpdater UPDATER;
}

# ─── Project Specific Models ──────────────────────────────────────────────────
# Keep Firebase models to prevent serialization issues
-keep class uz.kmax.kimyotest.domain.models.** { *; }
-keep class uz.kmax.kimyotest.data.tools.tools.SharedPref { *; }

# ─── General Rules ────────────────────────────────────────────────────────────
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile
-keep public class * extends android.app.Activity
-keep public class * extends android.app.Application
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.content.ContentProvider
-keep public class * extends android.view.View {
    public <init>(android.content.Context);
    public <init>(android.content.Context, android.util.AttributeSet);
    public <init>(android.content.Context, android.util.AttributeSet, int);
    public void set*(...);
}

# ─── Missing Classes Fix (R8 Errors) ──────────────────────────────────────────
-dontwarn com.facebook.infer.annotation.**
-dontwarn com.google.firebase.ktx.**
-dontwarn com.google.firebase.appcheck.ktx.**
-dontwarn com.unity3d.ads.**
-dontwarn com.unity3d.services.**
-dontwarn com.google.ads.mediation.unity.**
-dontwarn javax.annotation.**
-dontwarn org.checkerframework.**