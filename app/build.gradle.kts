plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.example.learningdashboard"

    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "com.example.learningdashboard"
        minSdk = 24
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner =
            "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            optimization {
                enable = false
            }
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    buildFeatures {
        compose = true
    }
}

dependencies {

    // Compose
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)

    // Core
    implementation(libs.androidx.core.ktx)

    // Lifecycle
    implementation(libs.androidx.lifecycle.runtime.ktx)

    // ViewModel
    implementation(
        "androidx.lifecycle:lifecycle-viewmodel-compose:2.11.0"
    )

    // Navigation
    implementation(
        "androidx.navigation:navigation-compose:2.9.7"
    )

    // Room
    implementation(
        "androidx.room:room-runtime:2.8.4"
    )

    implementation(
        "androidx.room:room-ktx:2.8.4"
    )

    // Room Compiler
    ksp(
        "androidx.room:room-compiler:2.8.4"
    )

    // Testing
    testImplementation(libs.junit)

    testImplementation(
        "org.jetbrains.kotlinx:kotlinx-coroutines-test:1.10.2"
    )

    // Android Testing
    androidTestImplementation(
        platform(libs.androidx.compose.bom)
    )

    androidTestImplementation(
        libs.androidx.compose.ui.test.junit4
    )

    androidTestImplementation(
        libs.androidx.espresso.core
    )

    androidTestImplementation(
        libs.androidx.junit
    )

    // Debug
    debugImplementation(
        libs.androidx.compose.ui.test.manifest
    )

    debugImplementation(
        libs.androidx.compose.ui.tooling
    )
}