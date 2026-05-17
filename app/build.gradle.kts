plugins {
    alias(libs.plugins.sakuwise.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.sakuwise.hilt)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.gustiadhitya.sakuwise"

    defaultConfig {
        applicationId = "com.gustiadhitya.sakuwise"
        versionCode = 1
        versionName = "1.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        debug {
            applicationIdSuffix = ".debug"
        }
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {
    implementation(projects.core.designsystem)
    implementation(projects.core.common)
    implementation(projects.feature.onboarding)
    implementation(projects.feature.dashboard)
    implementation(projects.feature.plan)
    implementation(projects.feature.asset)
    implementation(projects.feature.settings)

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.bundles.compose)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.nav.compose)
    implementation(libs.hilt.nav.compose)
    implementation(libs.timber)
}
