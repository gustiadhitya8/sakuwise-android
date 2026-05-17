plugins {
    alias(libs.plugins.sakuwise.android.library)
}

android {
    namespace = "com.gustiadhitya.sakuwise.core.common"
}

dependencies {
    implementation(libs.androidx.core.ktx)
}
