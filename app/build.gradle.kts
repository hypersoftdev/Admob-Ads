plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.parcelize)
    alias(libs.plugins.google.services)
    alias(libs.plugins.firebase.crashlytics)
    alias(libs.plugins.navigation.safe.args)
}

android {
    namespace = "com.hypersoft.admobads"
    compileSdk {
        version = release(37) {
            minorApiLevel = 1
        }
    }

    defaultConfig {
        applicationId = "com.hypersoft.admobads.testing"
        minSdk = 24
        targetSdk = 37
        versionCode = 2
        versionName = "2.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        create("release") {
            // No .jks in repo; file("") is invalid — set storeFile when a keystore is added
            storePassword = ""
            keyAlias = ""
            keyPassword = ""
        }
    }

    buildTypes {
        debug {
            resValue("string", "admob_app_id", "ca-app-pub-3940256099942544~3347511713")

            // App Open Ad
            resValue("string", "admob_app_open_id", "ca-app-pub-3940256099942544/9257395921")

            // Banner Ad
            resValue("string", "admob_banner_home_id", "ca-app-pub-3940256099942544/2014213617")

            // Inter Ad
            resValue("string", "admob_inter_splash_id", "ca-app-pub-3940256099942544/1033173712")
            resValue("string", "admob_inter_on_boarding_id", "ca-app-pub-3940256099942544/1033173712")

            // Rewarded Ad
            resValue("string", "admob_rewarded_ai_feature_id", "ca-app-pub-3940256099942544/5224354917")
            resValue("string", "admob_rewarded_inter_ai_feature_id", "ca-app-pub-3940256099942544/5354046379")

            // Native Ad
            resValue("string", "admob_native_language_id", "ca-app-pub-3940256099942544/2247696110")
            resValue("string", "admob_native_on_boarding_id", "ca-app-pub-3940256099942544/2247696110")
            resValue("string", "admob_native_home_id", "ca-app-pub-3940256099942544/2247696110")
            resValue("string", "admob_native_full_screen_id", "ca-app-pub-3940256099942544/2247696110")
            resValue("string", "admob_native_settings_id", "ca-app-pub-3940256099942544/2247696110")

            isMinifyEnabled = false
            isShrinkResources = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
        release {
            signingConfig = signingConfigs.getByName("release")

            resValue("string", "admob_app_id", "ca-app-pub-3940256099942544~3347511713")

            // App Open Ad
            resValue("string", "admob_app_open_id", "ca-app-pub-3940256099942544/9257395921")

            // Banner Ad
            resValue("string", "admob_banner_home_id", "ca-app-pub-3940256099942544/2014213617")

            // Inter Ad
            resValue("string", "admob_inter_splash_id", "ca-app-pub-3940256099942544/1033173712")
            resValue("string", "admob_inter_on_boarding_id", "ca-app-pub-3940256099942544/1033173712")

            // Rewarded Ad
            resValue("string", "admob_rewarded_ai_feature_id", "ca-app-pub-3940256099942544/5224354917")
            resValue("string", "admob_rewarded_inter_ai_feature_id", "ca-app-pub-3940256099942544/5354046379")

            // Native Ad
            resValue("string", "admob_native_language_id", "ca-app-pub-3940256099942544/2247696110")
            resValue("string", "admob_native_on_boarding_id", "ca-app-pub-3940256099942544/2247696110")
            resValue("string", "admob_native_home_id", "ca-app-pub-3940256099942544/2247696110")
            resValue("string", "admob_native_full_screen_id", "ca-app-pub-3940256099942544/2247696110")
            resValue("string", "admob_native_settings_id", "ca-app-pub-3940256099942544/2247696110")

            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    buildFeatures {
        viewBinding = true
        buildConfig = true
        resValues = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    bundle {
        language {
            enableSplit = false
        }
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
    }
}

base {
    archivesName = "Admob-Ads-HS-v${android.defaultConfig.versionCode}(${android.defaultConfig.versionName})"
}

dependencies {
    // Android Core
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity.ktx)
    implementation(libs.androidx.constraintlayout)

    // SDP / SSP
    implementation(libs.sdp.android)
    implementation(libs.ssp.android)

    // Lifecycle
    implementation(libs.androidx.lifecycle.livedata.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.lifecycle.process)

    // Navigational Components
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)

    // Google
    implementation(libs.play.services.ads)

    // Firebase
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.config)
    implementation(libs.firebase.analytics)
    implementation(libs.firebase.crashlytics)

    // Dependency Injection -> Koin
    implementation(libs.koin.android)

    // Work
    implementation(libs.androidx.work.runtime.ktx)
}