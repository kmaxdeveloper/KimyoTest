plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.hiltAndroid)
    //id("org.jetbrains.kotlin.kapt")
    id("kotlin-kapt")
    alias(libs.plugins.google.gms.google.services)
    alias(libs.plugins.firebase.crashlytics)
}

android {
    namespace = "uz.kmax.kimyotest"
    compileSdk = 36

    defaultConfig {
        applicationId = "uz.kmax.kimyotest"
        minSdk = 24
        targetSdk = 36
        versionCode = 9
        versionName = "2.5.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            ndk.debugSymbolLevel = "full"
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }

    buildFeatures {
        viewBinding = true
    }

    hilt {
        enableAggregatingTask = false
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.firebase.database)
    implementation(libs.firebase.crashlytics)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    implementation("com.google.firebase:firebase-database-ktx:21.0.0")
    implementation("com.google.firebase:firebase-storage-ktx:21.0.1")
    implementation("com.google.firebase:firebase-messaging-ktx:24.1.0")
    implementation("com.github.kmaxdeveloper:baselibrary:1.6.0")
    implementation("com.google.android.gms:play-services-ads:23.6.0")
    implementation("com.google.android.play:review-ktx:2.0.2")
    implementation("nl.dionsegijn:konfetti-xml:2.0.3")
    implementation("nl.dionsegijn:konfetti:1.3.2")
    implementation ("com.airbnb.android:lottie:3.4.0")
    implementation("com.facebook.shimmer:shimmer:0.5.0")
    implementation("com.google.android.play:app-update-ktx:2.1.0")
    implementation("androidx.work:work-runtime-ktx:2.9.1")
    implementation("androidx.lifecycle:lifecycle-process:2.8.7")
    implementation("io.github.afreakyelf:Pdf-Viewer:2.1.1")
    implementation("com.yandex.android:mobileads:7.12.3")
    
    // AdMob Mediation Adapters
    implementation("com.google.ads.mediation:ironsource:8.6.1.0")
    implementation("com.google.ads.mediation:facebook:6.19.0.1")
    implementation("com.google.ads.mediation:unity:4.13.1.0")

    // dagger
    kapt(libs.hilt.compiler)
    implementation(libs.hilt.android)
}