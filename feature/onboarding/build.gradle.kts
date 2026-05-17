plugins {
    alias(libs.plugins.sakuwise.android.feature)
}

android {
    namespace = "com.gustiadhitya.sakuwise.feature.onboarding"
}

dependencies {
    implementation(projects.core.designsystem)
    implementation(projects.core.ui)
    implementation(projects.core.domain)
    implementation(projects.core.model)
    implementation(projects.core.common)

    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.bundles.compose)
    implementation(libs.kotlinx.coroutines.android)
}
