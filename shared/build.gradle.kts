plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.ksp)
    alias(libs.plugins.roborazzi)
}

android {
    namespace = "com.stocktracker.shared"
    compileSdk = 35

    defaultConfig {
        minSdk = 30
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlin {
        compilerOptions {
            jvmTarget = org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17
        }
    }

    buildFeatures {
        compose = true
    }

    testOptions {
        unitTests {
            isIncludeAndroidResources = true
            isReturnDefaultValues = true
            all {
                val isLive = gradle.startParameter.taskNames.any { task ->
                    task.contains("testLiveApi", ignoreCase = true)
                }
                val isRelease = it.name.contains("Release", ignoreCase = true)
                it.useJUnit {
                    if (isLive) {
                        includeCategories("com.stocktracker.shared.testutil.LiveApiTest")
                    } else {
                        excludeCategories("com.stocktracker.shared.testutil.LiveApiTest")
                    }
                }
                if (isRelease) {
                    it.exclude("**/PriceChartSnapshotTest*")
                }
            }
        }
    }
}

roborazzi {
    outputDir.set(file("src/test/snapshots/roborazzi"))
}

tasks.register("testLiveApi") {
    group = "verification"
    description = "Run live Yahoo Finance API tests (requires network)"
    dependsOn("testDebugUnitTest")
}

dependencies {
    implementation(libs.compose.foundation)
    implementation(libs.compose.ui)

    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    ksp(libs.room.compiler)

    implementation(libs.retrofit)
    implementation(libs.retrofit.gson)
    implementation(libs.okhttp.logging)

    implementation(libs.coroutines.android)

    testImplementation(libs.junit)
    testImplementation(libs.mockk)
    testImplementation(libs.coroutines.test)
    testImplementation(libs.turbine)
    testImplementation(libs.okhttp.mockwebserver)
    testImplementation(libs.roborazzi.core)
    testImplementation(libs.roborazzi.compose)
    testImplementation(libs.roborazzi.junit.rule)
    testImplementation(libs.robolectric)
    testImplementation(libs.compose.ui.test.junit4)
    testImplementation(libs.compose.ui.test.manifest)

    androidTestImplementation(libs.test.runner)
    androidTestImplementation(libs.test.rules)
    androidTestImplementation(libs.room.testing)
    androidTestImplementation(libs.compose.ui.test.junit4)
    debugImplementation(libs.compose.ui.test.manifest)
}
