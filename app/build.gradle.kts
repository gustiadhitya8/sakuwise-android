plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.gustiadhitya.sakuwise"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.gustiadhitya.sakuwise"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables { useSupportLibrary = true }

        ksp {
            arg("room.schemaLocation", "$projectDir/schemas")
        }
    }

    // Release signing — keystore lives at <repo>/keystore/sakuwise-release.jks
    // and is gitignored. Credentials can come from gradle properties (set in
    // ~/.gradle/gradle.properties or via -P flags) or fall back to the local
    // dev defaults below. Production releases should override via gradle
    // properties; the same key must sign every future update.
    signingConfigs {
        create("release") {
            storeFile = rootProject.file("keystore/sakuwise-release.jks")
            storePassword = (project.findProperty("SAKUWISE_STORE_PASSWORD") as String?)
                ?: "sakuwise-release-2026"
            keyAlias = (project.findProperty("SAKUWISE_KEY_ALIAS") as String?) ?: "sakuwise"
            keyPassword = (project.findProperty("SAKUWISE_KEY_PASSWORD") as String?)
                ?: "sakuwise-release-2026"
        }
    }

    buildTypes {
        debug {
            applicationIdSuffix = ".debug"
            isMinifyEnabled = false
        }
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("release")
        }
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
            // Google API client + Drive bring overlapping META-INF resources
            // from multiple jars — strip the duplicates so the APK packager
            // doesn't trip over them. (REQ-2 Drive deps.)
            excludes += "META-INF/INDEX.LIST"
            excludes += "META-INF/DEPENDENCIES"
            excludes += "META-INF/LICENSE"
            excludes += "META-INF/LICENSE.txt"
            excludes += "META-INF/NOTICE"
            excludes += "META-INF/NOTICE.txt"
            excludes += "META-INF/io.netty.versions.properties"
        }
    }
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.process)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.material.icons.extended)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.androidx.core.splashscreen)
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.androidx.biometric)
    implementation(libs.androidx.appcompat)
    // Required for Theme.MaterialComponents.*.Bridge — the activity theme must
    // be an AppCompat descendant since MainActivity extends AppCompatActivity
    // (so AppCompatDelegate.setApplicationLocales actually flips the locale).
    implementation(libs.material)
    implementation(libs.kotlinx.coroutines.android)

    // Hilt
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.androidx.hilt.navigation.compose)

    // Room + SQLCipher
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)
    implementation(libs.sqlcipher.android)
    implementation(libs.androidx.security.crypto)

    // Crypto + KDF
    implementation(libs.argon2kt)

    // Background work
    implementation(libs.androidx.work.runtime.ktx)

    // OCR
    implementation(libs.mlkit.text.recognition)

    // Google Drive (AppData scope) — REQ-2 cloud backup.
    // Deliberate PRD §9.7 exception: this is the ONLY place that touches the
    // network; the rest of the app remains offline-only.
    implementation(libs.play.services.auth)
    implementation(libs.kotlinx.coroutines.play.services)
    implementation(libs.google.api.client.android) {
        exclude(group = "org.apache.httpcomponents")
    }
    implementation(libs.google.api.services.drive) {
        exclude(group = "org.apache.httpcomponents")
    }
    implementation(libs.google.http.client.gson) {
        exclude(group = "org.apache.httpcomponents")
    }

    debugImplementation(libs.androidx.ui.tooling)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
}
