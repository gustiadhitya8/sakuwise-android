plugins {
    alias(libs.plugins.sakuwise.android.library)
    alias(libs.plugins.sakuwise.android.library.compose)
}

android {
    namespace = "com.gustiadhitya.sakuwise.core.ui"
}

dependencies {
    implementation(projects.core.designsystem)
    implementation(projects.core.common)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.bundles.compose)
}
