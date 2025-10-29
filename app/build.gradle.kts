plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.hiltAndroid)
    //id("org.jetbrains.kotlin.kapt")
    id("kotlin-kapt")
    alias(libs.plugins.google.gms.google.services)
}

android {
    namespace = "uz.kmax.kimyotest"
    compileSdk = 35

    defaultConfig {
        applicationId = "uz.kmax.kimyotest"
        minSdk = 24
        targetSdk = 35
        versionCode = 4
        versionName = "1.1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
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
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    implementation("com.google.firebase:firebase-database-ktx:21.0.0")
    implementation("com.google.firebase:firebase-storage-ktx:21.0.1")
    implementation("com.github.kmaxdeveloper:baselibrary:1.6.0")
    implementation("com.google.android.gms:play-services-ads:23.6.0")
    implementation("com.google.android.play:review-ktx:2.0.2")
    implementation("nl.dionsegijn:konfetti-xml:2.0.3")
    implementation("nl.dionsegijn:konfetti:1.3.2")
    implementation ("com.airbnb.android:lottie:3.4.0")
    implementation("com.google.android.play:app-update-ktx:2.1.0")
    implementation("io.github.afreakyelf:Pdf-Viewer:2.1.1")
    implementation("com.yandex.android:mobileads:7.12.3") // X.x.x o'rniga eng yangi versiyani qo'ying

    // dagger
    kapt("com.google.dagger:hilt-compiler:2.50")
    implementation(libs.hilt.android)
}