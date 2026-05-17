plugins {
    alias(libs.plugins.sakuwise.android.library)
    alias(libs.plugins.sakuwise.hilt)
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.gustiadhitya.sakuwise.core.database"
}

dependencies {
    implementation(projects.core.model)
    implementation(projects.core.crypto)
    implementation(libs.bundles.room)
    ksp(libs.androidx.room.compiler)
    implementation(libs.sqlcipher.android)
    implementation(libs.androidx.security.crypto)
}
