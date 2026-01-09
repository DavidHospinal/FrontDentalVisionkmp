import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    kotlin("multiplatform")
    id("org.jetbrains.compose")
    // 1. Agregamos el plugin obligatorio para Kotlin 2.1.0
    id("org.jetbrains.kotlin.plugin.compose")
}

kotlin {
    jvm()
    
    // Forzar uso de JDK 17 para compatibilidad con java.net.http (requerido por Ktor Java Engine)
    jvmToolchain(17)
    
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
        mainClass = "MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            
            // SOLUCIÓN CRÍTICA: Incluir TODOS los módulos Java (incluye java.net.http)
            includeAllModules = true
            
            packageName = "DentalVisionAI"
            packageVersion = "1.0.0"
            description = "Professional Dental AI Analysis System"
            vendor = "Dental Vision AI"
            
            windows {
                menuGroup = "Dental Vision AI"
                upgradeUuid = "dental-vision-ai-uuid"
            }
        }
    }
}