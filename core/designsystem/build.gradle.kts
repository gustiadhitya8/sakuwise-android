plugins {
    alias(libs.plugins.sakuwise.android.library)
    alias(libs.plugins.sakuwise.android.library.compose)
}

android {
    namespace = "com.gustiadhitya.sakuwise.core.designsystem"
}

dependencies {
    implementation(projects.core.common)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.bundles.compose)
    implementation(libs.androidx.compose.material.icons.extended)
    debugImplementation(libs.androidx.compose.ui.tooling)
}
