plugins {
    kotlin("multiplatform")
    id("com.android.application")
    id("org.jetbrains.compose")
    id("org.jetbrains.kotlin.plugin.compose")
}

kotlin {
    androidTarget()

    sourceSets {
        val androidMain by getting {
            dependencies {
                implementation(project(":composeApp"))

                // <--- IMPORTANTE: Estas dependencias son OBLIGATORIAS para MainActivity
                implementation("androidx.activity:activity-compose:1.9.0")
                implementation("androidx.appcompat:appcompat:1.7.0")
                implementation("androidx.core:core-ktx:1.15.0")

                // Logging - Napier (required for MainActivity)
                implementation(libs.napier)

                // Dependency Injection - Koin (required for MainActivity)
                implementation(libs.koin.android)
                implementation(libs.koin.core)
            }
        }
    }
}

android {
    // Usamos valores fijos para asegurar compatibilidad
    compileSdk = 35
    namespace = "com.dentalvision.ai.android"

    // Mapeo explicito para asegurar que encuentre tus archivos en androidMain
    sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")
    sourceSets["main"].res.srcDirs("src/androidMain/res")
    // sourceSets["main"].java.srcDirs("src/androidMain/kotlin") // Esto lo maneja el plugin KMP usualmente

    defaultConfig {
        applicationId = "com.dentalvision.ai"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0.0"
    }

    // <--- IMPORTANTE: Esto activa el compilador de Compose para Android
    buildFeatures {
        compose = true
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
            excludes += "META-INF/versions/9/previous-compilation-data.bin"
        }
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    kotlin {
        jvmToolchain(11)
    }
}