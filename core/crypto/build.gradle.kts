plugins {
    alias(libs.plugins.sakuwise.android.library)
}

android {
    namespace = "com.gustiadhitya.sakuwise.core.crypto"
}

dependencies {
    implementation(libs.androidx.security.crypto)
    implementation(libs.argon2kt)
}
