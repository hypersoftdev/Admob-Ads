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
        consumerProguardFiles("consumer-rules.pro")
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
    buildFeatures {
        buildConfig = true
        viewBinding = true
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