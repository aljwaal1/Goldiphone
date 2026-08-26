plugins {
    id("com.android.application")
    kotlin("android")
}

android {
    namespace = "com.explapp.marketpulse"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.explapp.marketpulse"
        minSdk = 24
        targetSdk = 35
        versionCode = 5
        versionName = "2.2.0"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions { jvmTarget = "17" }
}
