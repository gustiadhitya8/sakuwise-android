plugins {
    alias(libs.plugins.sakuwise.android.library)
    alias(libs.plugins.sakuwise.hilt)
}

android {
    namespace = "com.gustiadhitya.sakuwise.core.crypto"
}

dependencies {
    implementation(libs.androidx.security.crypto)
    implementation(libs.argon2kt)
    testImplementation(libs.junit.jupiter)
}
