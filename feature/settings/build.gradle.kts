plugins {
    alias(libs.plugins.sakuwise.android.feature)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.gustiadhitya.sakuwise.feature.settings"
}

dependencies {
    implementation(projects.core.designsystem)
    implementation(projects.core.ui)
    implementation(projects.core.domain)
    implementation(projects.core.model)
    implementation(projects.core.common)
}
