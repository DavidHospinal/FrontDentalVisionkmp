# Dental Vision AI - Kotlin Multiplatform

Professional dental AI analysis system with YOLOv12 integration for cross-platform deployment.

## Overview

Dental Vision AI is a multiplatform application for dental image analysis using YOLOv12 deep learning model. The system provides comprehensive patient management, automated dental image analysis, and professional reporting capabilities across Android, iOS, and Desktop platforms.

## Architecture

### Technology Stack

- **UI Framework**: Compose Multiplatform with Material3
- **Backend Communication**: Ktor Client 2.3.7
- **Serialization**: Kotlinx Serialization 1.6.2
- **Dependency Injection**: Koin 3.5.3
- **State Management**: AndroidX Lifecycle ViewModel + StateFlow
- **Navigation**: AndroidX Navigation Compose
- **Logging**: Napier 2.7.1
- **Image Loading**: Kamel 0.9.1

### Platform Support

- **Android**: API 24+ (Android 7.0 Nougat)
- **iOS**: iOS 14.0+
- **Desktop**: Windows, macOS, Linux (JVM 11+)
- **Web**: JavaScript and WebAssembly targets

### Project Structure

```
composeApp/
├── src/
│   ├── commonMain/          # Shared code (95%+)
│   │   └── kotlin/com/dentalvision/ai/
│   │       ├── App.kt       # Application entry point
│   │       ├── domain/      # Business logic and models
│   │       ├── data/        # Data layer and repositories
│   │       ├── presentation/# UI screens and ViewModels
│   │       └── di/          # Dependency injection modules
│   ├── androidMain/         # Android-specific code
│   ├── iosMain/             # iOS-specific code
│   ├── jvmMain/             # Desktop-specific code
│   ├── jsMain/              # Web JavaScript
│   └── wasmJsMain/          # Web WebAssembly
```

## Features

### Patient Management
- Complete CRUD operations for patient records
- Patient search and filtering
- Medical history tracking
- FDI tooth numbering system support

### AI-Powered Analysis
- Real-time dental image analysis
- YOLOv12 object detection integration
- Tooth detection with bounding boxes
- Confidence scoring per detection
- Multi-tooth analysis in single image

### Report Generation
- Professional PDF report generation
- Analysis summary and findings
- Detected conditions documentation
- Treatment recommendations
- Patient information integration

## Backend Integration

Backend API: https://dental-vision-ai-backend.onrender.com

The application communicates with a Flask backend that interfaces with YOLOv12 model deployed on HuggingFace Spaces for dental image analysis.

## Build Instructions

### Prerequisites

- JDK 11 or higher
- Android Studio Hedgehog (2023.1.1) or newer
- Xcode 15+ (for iOS development on macOS)

### Android Build

```bash
./gradlew :composeApp:assembleDebug
```

Install on connected device:
```bash
./gradlew :composeApp:installDebug
```

### iOS Build

```bash
./gradlew :composeApp:iosArm64Build
```

Open iosApp/iosApp.xcodeproj in Xcode to run on simulator or device.

### Desktop Build

```bash
./gradlew :composeApp:run
```

Create distributable package:
```bash
./gradlew :composeApp:createDistributable
```

### Web Build

JavaScript target:
```bash
./gradlew :composeApp:jsBrowserDevelopmentRun
```

WebAssembly target:
```bash
./gradlew :composeApp:wasmJsBrowserDevelopmentRun
```

## Development

### Code Style

- Kotlin official coding conventions
- Material Design 3 guidelines
- Clean Architecture principles
- Repository pattern for data layer
- MVVM pattern for presentation layer

### Testing

Run all tests:
```bash
./gradlew test
```

Run Android instrumented tests:
```bash
./gradlew :composeApp:connectedAndroidTest
```

## Configuration

### Backend API

Backend URL is configured in build.gradle.kts:
```kotlin
buildConfigField("String", "BACKEND_API_URL", "\"https://dental-vision-ai-backend.onrender.com\"")
```

### Build Variants

- **Debug**: Development build with logging enabled
- **Release**: Production build with ProGuard optimization

## License

MIT License - Copyright 2025 Dental Vision AI

## Contact

David Hospinal
Email: u202021214@upc.edu.pe
GitHub: https://github.com/DavidHospinal
