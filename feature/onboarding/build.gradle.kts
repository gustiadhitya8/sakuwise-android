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
}
