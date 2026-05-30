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
        versionCode = 4
        versionName = "1.0.3"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables { useSupportLibrary = true }

        ksp {
            arg("room.schemaLocation", "$projectDir/schemas")
        }
    }

    // Release signing — keystore is NOT in the repo (gitignored).
    // Set these four properties in ~/.gradle/gradle.properties (never in-repo):
    //   SAKUWISE_KEYSTORE_PATH=/absolute/path/to/your.jks
    //   SAKUWISE_STORE_PASSWORD=...
    //   SAKUWISE_KEY_ALIAS=...
    //   SAKUWISE_KEY_PASSWORD=...
    signingConfigs {
        create("release") {
            storeFile = (project.findProperty("SAKUWISE_KEYSTORE_PATH") as String?)
                ?.let { file(it) }
            storePassword = project.findProperty("SAKUWISE_STORE_PASSWORD") as String?
            keyAlias = project.findProperty("SAKUWISE_KEY_ALIAS") as String?
            keyPassword = project.findProperty("SAKUWISE_KEY_PASSWORD") as String?
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
    // Expose the exported Room schema JSON to instrumented tests so
    // MigrationTestHelper can load schema 5 as a baseline (Item 1, v1.0.4).
    sourceSets {
        getByName("androidTest").assets.srcDirs(files("$projectDir/schemas"))
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
    androidTestImplementation(libs.androidx.room.testing)
}

// Convenience task: build release AAB and copy it to a fixed, easy-to-find
// location at ~/AndroidStudioProjects/Sakuwise/app/release/app-release.aab
// Usage: ./gradlew exportAab
tasks.register<Copy>("exportAab") {
    dependsOn("bundleRelease")
    val destDir = file("${System.getProperty("user.home")}/AndroidStudioProjects/Sakuwise/app/build/outputs/bundle/release")
    from(layout.buildDirectory.dir("outputs/bundle/release"))
    include("app-release.aab")
    into(destDir)
    doFirst { destDir.mkdirs() }
    doLast { println("\n✓ AAB siap di: $destDir/app-release.aab\n") }
}
