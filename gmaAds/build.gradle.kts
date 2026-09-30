plugins {
    alias(libs.plugins.android.library)
}

android {
    namespace = "com.hypersoft.ads.practice.gmaAds"
    compileSdk {
        version = release(37) {
            minorApiLevel = 1
        }
    }

    defaultConfig {
        minSdk = 24
    }

    buildTypes {
        debug {
            // Google sample ad unit IDs.
            resValue("string", "admob_app_id", "ca-app-pub-3940256099942544~3347511713")

            resValue("string", "admob_app_open_lifecycle_id", "ca-app-pub-3940256099942544/9257395921")
            resValue("string", "admob_app_open_entrance_id", "ca-app-pub-3940256099942544/9257395921")

            resValue("string", "admob_banner_language_id", "ca-app-pub-3940256099942544/2014213617")
            resValue("string", "admob_banner_on_boarding_id", "ca-app-pub-3940256099942544/2014213617")
            resValue("string", "admob_banner_dashboard_id", "ca-app-pub-3940256099942544/2014213617")
            resValue("string", "admob_banner_feature_one_id", "ca-app-pub-3940256099942544/9214589741")
            resValue("string", "admob_banner_feature_two_id", "ca-app-pub-3940256099942544/9214589741")

            resValue("string", "admob_inter_entrance_id", "ca-app-pub-3940256099942544/1033173712")
            resValue("string", "admob_inter_on_boarding_id", "ca-app-pub-3940256099942544/1033173712")
            resValue("string", "admob_inter_home_id", "ca-app-pub-3940256099942544/1033173712")
            resValue("string", "admob_inter_bottom_navigation_id", "ca-app-pub-3940256099942544/1033173712")
            resValue("string", "admob_inter_back_press_id", "ca-app-pub-3940256099942544/1033173712")
            resValue("string", "admob_inter_exit_id", "ca-app-pub-3940256099942544/1033173712")

            resValue("string", "admob_native_language_id", "ca-app-pub-3940256099942544/2247696110")
            resValue("string", "admob_native_on_boarding_id", "ca-app-pub-3940256099942544/2247696110")
            resValue("string", "admob_native_menu_id", "ca-app-pub-3940256099942544/2247696110")
            resValue("string", "admob_native_home_id", "ca-app-pub-3940256099942544/2247696110")
            resValue("string", "admob_native_trending_id", "ca-app-pub-3940256099942544/2247696110")
            resValue("string", "admob_native_setting_id", "ca-app-pub-3940256099942544/2247696110")
            resValue("string", "admob_native_feature_one_id", "ca-app-pub-3940256099942544/2247696110")
            resValue("string", "admob_native_feature_two_id", "ca-app-pub-3940256099942544/2247696110")

            resValue("string", "admob_rewarded_home_id", "ca-app-pub-3940256099942544/5224354917")

            resValue("string", "admob_rewarded_inter_home_id", "ca-app-pub-3940256099942544/5354046379")
        }
        release {
            // Still Google sample IDs. Replace with production units before shipping.
            resValue("string", "admob_app_id", "ca-app-pub-3940256099942544~3347511713")

            resValue("string", "admob_app_open_lifecycle_id", "ca-app-pub-3940256099942544/9257395921")
            resValue("string", "admob_app_open_entrance_id", "ca-app-pub-3940256099942544/9257395921")

            resValue("string", "admob_banner_language_id", "ca-app-pub-3940256099942544/2014213617")
            resValue("string", "admob_banner_on_boarding_id", "ca-app-pub-3940256099942544/2014213617")
            resValue("string", "admob_banner_dashboard_id", "ca-app-pub-3940256099942544/2014213617")
            resValue("string", "admob_banner_feature_one_id", "ca-app-pub-3940256099942544/9214589741")
            resValue("string", "admob_banner_feature_two_id", "ca-app-pub-3940256099942544/9214589741")

            resValue("string", "admob_inter_entrance_id", "ca-app-pub-3940256099942544/1033173712")
            resValue("string", "admob_inter_on_boarding_id", "ca-app-pub-3940256099942544/1033173712")
            resValue("string", "admob_inter_home_id", "ca-app-pub-3940256099942544/1033173712")
            resValue("string", "admob_inter_bottom_navigation_id", "ca-app-pub-3940256099942544/1033173712")
            resValue("string", "admob_inter_back_press_id", "ca-app-pub-3940256099942544/1033173712")
            resValue("string", "admob_inter_exit_id", "ca-app-pub-3940256099942544/1033173712")

            resValue("string", "admob_native_language_id", "ca-app-pub-3940256099942544/2247696110")
            resValue("string", "admob_native_on_boarding_id", "ca-app-pub-3940256099942544/2247696110")
            resValue("string", "admob_native_menu_id", "ca-app-pub-3940256099942544/2247696110")
            resValue("string", "admob_native_home_id", "ca-app-pub-3940256099942544/2247696110")
            resValue("string", "admob_native_trending_id", "ca-app-pub-3940256099942544/2247696110")
            resValue("string", "admob_native_setting_id", "ca-app-pub-3940256099942544/2247696110")
            resValue("string", "admob_native_feature_one_id", "ca-app-pub-3940256099942544/2247696110")
            resValue("string", "admob_native_feature_two_id", "ca-app-pub-3940256099942544/2247696110")

            resValue("string", "admob_rewarded_home_id", "ca-app-pub-3940256099942544/5224354917")

            resValue("string", "admob_rewarded_inter_home_id", "ca-app-pub-3940256099942544/5354046379")
        }
    }

    buildFeatures {
        buildConfig = true
        viewBinding = true
        resValues = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
}

dependencies {
    implementation(project(":core"))
    implementation(project(":data"))

    // Android
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.material)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.process)

    // Coroutines
    api(libs.kotlinx.coroutines.android)

    // Google
    implementation(libs.play.services.ads)
    implementation(libs.user.messaging.platform)

    // DI - KOIN
    implementation(libs.koin.android)
    implementation(libs.koin.core.coroutines)
}