plugins {
    alias(libs.plugins.sakuwise.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.sakuwise.hilt)
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
    implementation(projects.core.model)
    implementation(projects.core.domain)
    implementation(projects.core.data)
    implementation(projects.core.crypto)

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.bundles.compose)
    implementation(libs.timber)
    implementation(libs.hilt.nav.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
}
