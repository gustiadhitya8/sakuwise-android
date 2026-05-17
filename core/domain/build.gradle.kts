plugins {
    alias(libs.plugins.sakuwise.jvm.library)
}

dependencies {
    implementation(projects.core.model)
    implementation(libs.kotlinx.coroutines.android)
}
