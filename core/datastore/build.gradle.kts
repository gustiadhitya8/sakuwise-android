plugins {
    alias(libs.plugins.sakuwise.android.library)
    alias(libs.plugins.sakuwise.hilt)
}

android {
    namespace = "com.gustiadhitya.sakuwise.core.datastore"
}

dependencies {
    implementation(projects.core.model)
    implementation(projects.core.domain)
    implementation(libs.androidx.datastore.preferences)
    testImplementation(libs.junit.jupiter)
}
