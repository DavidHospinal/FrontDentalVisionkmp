import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.composeMultiplatform)
    // alias(libs.plugins.composeHotReload)
    alias(libs.plugins.kotlinx.serialization)

    // Agregamos el plugin
    id("org.jetbrains.kotlin.plugin.compose")
}

kotlin {
    // Android Target
    androidTarget {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }

    // iOS Targets
    listOf(
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "DentalVisionAI"
            isStatic = true

            // Export common dependencies for iOS
            export(libs.kotlinx.datetime)
        }
    }

    // JVM Target (Desktop)
    jvm()

    // JavaScript Target (Web) - Disabled, using wasmJs instead
    // js {
    //     browser()
    //     binaries.executable()
    // }

    // WebAssembly Target (Modern Web)
    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        browser {
            commonWebpackConfig {
                // Configure js-joda for WASM
                cssSupport {
                    enabled.set(true)
                }
            }
        }
        binaries.executable()
    }

    sourceSets {
        // Common Main - Shared across all platforms
        commonMain.dependencies {
            // Compose Multiplatform UI
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)

            // Lifecycle & ViewModel
            implementation(libs.androidx.lifecycle.viewmodelCompose)
            implementation(libs.androidx.lifecycle.runtimeCompose)

            // Navigation
            implementation(libs.androidx.navigation.compose)

            // Coroutines
            implementation(libs.kotlinx.coroutines.core)

            // Serialization (JSON)
            implementation(libs.kotlinx.serialization.json)

            // DateTime
            implementation(libs.kotlinx.datetime)

            // Ktor Client (HTTP API calls)
            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.content.negotiation)
            implementation(libs.ktor.serialization.kotlinx.json)
            implementation(libs.ktor.client.logging)
            implementation(libs.ktor.client.auth)

            // SQLDelight - Commented until database schema is implemented
            // Note: When implementing, move to platform-specific sourceSets for WASM compatibility
            // implementation(libs.sqldelight.runtime)
            // implementation(libs.sqldelight.coroutines)

            // Image Loading
            implementation(libs.kamel.image)
            implementation(libs.coil.compose)
            implementation(libs.coil.network.ktor)

            // Lottie Animation
            implementation(libs.compottie)

            // Dependency Injection - Koin
            implementation(libs.koin.core)
            implementation(libs.koin.compose)
            implementation(libs.koin.composeVM)

            // Logging
            implementation(libs.napier)
        }

        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }

        // Android Specific
        androidMain.dependencies {
            implementation(compose.preview)
            implementation(libs.androidx.activity.compose)
            implementation(libs.androidx.core.ktx)
            implementation(libs.androidx.appcompat)

            // Ktor Android Engine
            implementation(libs.ktor.client.android)

            // SQLDelight Android Driver - Commented until schema is implemented
            // implementation(libs.sqldelight.android.driver)

            // Koin Android
            implementation(libs.koin.android)
        }

        // iOS Specific
        iosMain.dependencies {
            // Ktor iOS Engine (Darwin)
            implementation(libs.ktor.client.darwin)

            // SQLDelight iOS Driver - Commented until schema is implemented
            // implementation(libs.sqldelight.native.driver)
        }

        // JVM/Desktop Specific
        jvmMain.dependencies {
            implementation(compose.desktop.currentOs)
            implementation(libs.kotlinx.coroutinesSwing)

            // Ktor JVM Engine
            implementation(libs.ktor.client.java)

            // SQLDelight JVM Driver - Commented until schema is implemented
            // implementation(libs.sqldelight.sqlite.driver)

            // Coil for Desktop
            implementation(libs.coil.compose)
            // TODO: Coil network-ktor 3.0.4 no existe, verificar version correcta
            // implementation(libs.coil.network.ktor)
        }

        // JavaScript/Web Specific - Disabled, using wasmJs instead
        // jsMain.dependencies {
        //     // Ktor JS Engine
        //     implementation(libs.ktor.client.js)
        // }

        // WebAssembly Specific
        wasmJsMain.dependencies {
            // Ktor JS Engine (also works for WASM)
            implementation(libs.ktor.client.js)

            // SQLDelight Web Worker Driver - Commented until schema is implemented
            // Note: web-worker-driver has compatibility issues with some WASM targets
            // Consider using IndexedDB wrapper or remote API only for WASM
            // implementation(libs.sqldelight.web.worker.driver)
        }
    }
}

// SQLDelight Configuration - Commented until database schema is implemented
// Uncomment when adding .sq schema files to src/commonMain/sqldelight/
/*
sqldelight {
    databases {
        create("DentalVisionDatabase") {
            packageName.set("com.dentalvision.database")

            // Specify the SQL schema location
            srcDirs.setFrom("src/commonMain/sqldelight")
        }
    }
}
*/

// Android Library Configuration
android {
    namespace = "com.dentalvision.ai"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        minSdk = libs.versions.android.minSdk.get().toInt()
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
            excludes += "META-INF/versions/9/previous-compilation-data.bin"
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    buildFeatures {
        compose = true
    }
}

// Android Dependencies
dependencies {
    debugImplementation(compose.uiTooling)
}

// Compose Desktop Configuration
compose.desktop {
    application {
        mainClass = "com.dentalvision.ai.MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)

            packageName = "Dental Vision AI"
            packageVersion = "1.0.0"
            description = "Professional Dental AI Analysis System with YOLOv12"
            copyright = "© 2025 Dental Vision AI. All rights reserved."
            vendor = "Dental Vision AI"

            // Application icon
            macOS {
                iconFile.set(project.file("src/jvmMain/resources/icon.icns"))
                bundleID = "com.dentalvision.ai"
            }
            windows {
                iconFile.set(project.file("src/jvmMain/resources/icon.ico"))
                menuGroup = "Dental Vision AI"
                upgradeUuid = "dental-vision-ai-uuid"
            }
            linux {
                iconFile.set(project.file("src/jvmMain/resources/icon.png"))
            }
        }

        buildTypes.release.proguard {
            configurationFiles.from("proguard-desktop.pro")
        }
    }
}

// Task to print project structure

tasks.register("printProjectStructure") {
    doLast {
        println("""
            Dental Vision AI - Kotlin Multiplatform Project
            ================================================
            Targets:
              ✓ Android (API 24+)
              ✓ iOS (Arm64 + Simulator)
              ✓ JVM Desktop (Windows, macOS, Linux)
              ✓ Web (JavaScript)
              ✓ Web (WebAssembly)

            Shared Code Percentage: ~85%

            Key Technologies:
              - Compose Multiplatform (UI)
              - Ktor Client (Networking)
              - SQLDelight (NOT YET IMPLEMENTED - using remote API only)
              - Kotlinx Serialization (JSON)
              - Koin (Dependency Injection)
              - Napier (Logging)

            Note: SQLDelight dependencies are commented out until database
            schema is implemented. For WASM compatibility, consider using
            remote API only or IndexedDB for local storage.
        """.trimIndent())
    }
}
