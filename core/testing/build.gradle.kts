plugins {
    alias(libs.plugins.sakuwise.android.library)
    alias(libs.plugins.sakuwise.android.library.compose)
}

android {
    namespace = "com.gustiadhitya.sakuwise.core.testing"
}

dependencies {
    implementation(projects.core.domain)
    implementation(projects.core.model)
    implementation(libs.kotlinx.coroutines.test)
    implementation(libs.mockk)
    implementation(libs.turbine)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui.test.junit4)
}
