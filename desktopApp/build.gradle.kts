import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    kotlin("multiplatform")
    id("org.jetbrains.compose")
    // 1. Agregamos el plugin obligatorio para Kotlin 2.1.0
    id("org.jetbrains.kotlin.plugin.compose")
}

kotlin {
    jvm()
    sourceSets {
        val jvmMain by getting  {
            dependencies {
                implementation(compose.desktop.currentOs)
                implementation(project(":composeApp"))

                // Dependencias explícitas necesarias para main.kt
                implementation(libs.koin.core)
                implementation(libs.napier)
            }
        }
    }
}

compose.desktop {
    application {
        // Asegúrate de que este MainKt exista en tu código (com.dentalvision.ai.MainKt)
        // Si falla después, revisaremos el package.
        mainClass = "MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "DentalVisionAI" // Le puse un nombre más bonito
            packageVersion = "1.0.0"
        }
    }
}