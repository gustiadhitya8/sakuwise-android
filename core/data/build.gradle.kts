plugins {
    alias(libs.plugins.sakuwise.android.library)
    alias(libs.plugins.sakuwise.hilt)
}

android {
    namespace = "com.gustiadhitya.sakuwise.core.data"
}

dependencies {
    implementation(projects.core.domain)
    implementation(projects.core.database)
    implementation(projects.core.datastore)
    implementation(projects.core.common)
    implementation(libs.kotlinx.coroutines.android)
}
