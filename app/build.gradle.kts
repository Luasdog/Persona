plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("org.jetbrains.kotlin.plugin.compose") version "2.0.21"
    id(libs.plugins.kotlin.parcelize.get().pluginId)
    id(libs.plugins.kotlin.kapt.get().pluginId)
    id(libs.plugins.hilt.android.gradle.plugin.get().pluginId)
}

android {
    namespace = "com.example.persona"
    compileSdk = 34 // Corrected to stable SDK

    defaultConfig {
        applicationId = "com.example.persona"
        minSdk = 23
        targetSdk = 34 // Corrected to stable SDK
        versionCode = 1
        versionName = "1.0"

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
        compose = true
    }
    // Since you are using Kotlin 2.0.21, you should use the new Compose compiler plugin or configure it here if not using the plugin block
    // composeOptions {
    //    kotlinCompilerExtensionVersion = "1.5.8" // This is for older Kotlin versions (pre 2.0)
    // }
}

dependencies {
    // 核心依赖
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)

    // Compose依赖
    // Ensure BOM is used for version alignment if possible, but manual versions are fine if they match
    implementation(platform("androidx.compose:compose-bom:2024.04.01")) 
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.activity)
    debugImplementation(libs.androidx.compose.ui.tooling)

    // ViewModel依赖
    implementation(libs.androidx.viewmodel)
    implementation(libs.androidx.viewmodel.compose)

    // 网络请求依赖
    implementation(libs.retrofit)
    implementation(libs.retrofit.gson)
    implementation(libs.okhttp)

    // 测试依赖
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    implementation(libs.hilt.android)
    implementation(libs.hilt.navigation.compose)
    kapt(libs.hilt.android.compiler)
}