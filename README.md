# Dental Vision AI - Kotlin Multiplatform

Professional dental AI analysis system with YOLOv12 integration for cross-platform deployment.

## Overview

Dental Vision AI is a multiplatform application for dental image analysis using YOLOv12 deep learning model. The system provides comprehensive patient management, automated dental image analysis, and professional reporting capabilities across Android, iOS, Desktop, and Web platforms.

## External APIs & Architecture

This project integrates three key external services to deliver a robust AI-powered dental analysis solution:

| Service | Purpose | Details |
|---------|---------|---------|
| **Gemini API (Google)** | AI-Powered Clinical Insights | Generates personalized clinical recommendations, treatment plans, and diagnostic summaries based on YOLOv12 detection results. Provides natural language explanations for dental professionals. |
| **Hugging Face** | ML Model Hosting | Hosts the YOLOv12 dental detection model via Gradio Spaces. Provides REST API endpoints for real-time dental image analysis with bounding box detection and confidence scoring. |
| **Render** | Backend Deployment | Hosts the Python Flask backend that orchestrates communication between the KMP frontend, HuggingFace inference API, and SQLite database. Handles patient management, analysis history, and report generation. |

### Architecture Diagram

```
┌─────────────────────────────────────────────────────────────────┐
│                    Kotlin Multiplatform Frontend                │
│          (Android, iOS, Desktop, Web - Compose UI)              │
└──────────────────────┬──────────────────────────────────────────┘
                       │
                       ↓
         ┌─────────────────────────────┐
         │   Render (Backend Host)     │
         │   Flask REST API Server     │
         │   - Patient Management      │
         │   - Analysis Orchestration  │
         │   - SQLite Database         │
         └─────┬───────────────────┬───┘
               │                   │
       ┌───────↓─────┐     ┌──────↓────────┐
       │ HuggingFace │     │  Gemini API   │
       │  YOLOv12    │     │  (Google AI)  │
       │   Model     │     │   Insights    │
       └─────────────┘     └───────────────┘
```

## Setup & Configuration

### API Key Configuration (MANDATORY)

The application requires a Google Gemini API key for AI-powered clinical insights. **For security and contest compliance, API keys are NOT included in the repository.**

#### Quick Setup (3 steps):

1. **Get a Free API Key**
   - Visit [Google AI Studio](https://aistudio.google.com/app/apikey)
   - Sign in with your Google account
   - Click "Create API Key"
   - Copy the generated key

2. **Configure the Application**
   - Copy `composeApp/Secrets.sample.kt` to `composeApp/src/commonMain/kotlin/com/dentalvision/ai/data/remote/api/Secrets.kt`
   - Open the new `Secrets.kt` file and replace `INSERT_YOUR_API_KEY_HERE` with your actual API key
   - The file path structure is already correct in the template

3. **Build and Run**
   - The application is now ready to build and run on any platform
   - See Build Instructions below for platform-specific commands

#### Security Note

The `Secrets.kt` file is excluded from version control via `.gitignore` to protect sensitive API keys. **Never commit this file to public repositories.** This configuration method complies with security best practices for software development contests and production deployments.

#### Alternative: Pre-compiled Binaries

If you prefer to test the application without configuring API keys, pre-compiled binaries are available in the [Releases](https://github.com/DavidHospinal/dental-vision-ai/releases) section with demo credentials.

## Testing Resources & Sample Data

To facilitate quick testing and demonstration, the repository includes ready-to-use sample data:

### Sample Images Folder

- **Location**: `samples-images/` directory in the project root
- **Contents**: 5 professionally curated dental X-ray images for immediate testing
- **Usage**: These images are pre-validated to work optimally with the YOLOv12 detection model

### Testing Flexibility

- You are **NOT limited** to the provided samples
- Use your own dental images in standard formats (JPG, PNG)
- Download external dental X-ray datasets for extensive testing
- The model supports panoramic and periapical radiographs

### Quick Test Workflow

1. Launch the application (see Build Instructions below)
2. Navigate to "New Analysis" from the main menu
3. Select a patient or create a test patient
4. Upload an image from `samples-images/` or your own source
5. View real-time detection results with bounding boxes
6. Generate a professional PDF report with clinical insights

## Supported Platforms

Current implementation status across all Kotlin Multiplatform targets:

| Platform | Status | Details |
|----------|--------|---------|
| Android | Fully Functional | API 24+ (Android 7.0+), tested on physical devices and emulators |
| Desktop (JVM) | Fully Functional | Windows, macOS, Linux support with native window decorations |
| Web (Wasm/JS) | Fully Functional | Modern browsers with WebAssembly support, fallback to JS |
| iOS | Work in Progress | Beta implementation, UI functional, backend integration pending |

### Platform-Specific Features

- **Android**: Material You dynamic theming, notification support
- **Desktop**: Menu bar integration, file picker dialogs
- **Web**: Progressive Web App (PWA) capabilities, offline mode
- **iOS**: SwiftUI interop for native components (in development)

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

## Installation Manual - Getting Started

### Prerequisites

Before building the project, ensure you have the following installed:

| Requirement | Version | Notes |
|-------------|---------|-------|
| JDK | 17 or higher | OpenJDK recommended |
| Android Studio | Hedgehog (2023.1.1)+ | Required for Android builds |
| Xcode | 15+ | macOS only, for iOS development |
| Gradle | 8.0+ | Included via wrapper |

### Build Instructions

#### Android Build

**Debug Build:**
```bash
./gradlew :composeApp:assembleDebug
```

**Install on Connected Device:**
```bash
./gradlew :composeApp:installDebug
```

**Run on Emulator:**
```bash
./gradlew :composeApp:installDebug
# Then launch the app from the emulator
```

#### Desktop Build (JVM)

**Run Application:**
```bash
./gradlew :composeApp:run
```

**Create Distributable Package:**
```bash
./gradlew :composeApp:createDistributable
```

The distributable will be created in `composeApp/build/compose/binaries/main/`

#### Web Build

**JavaScript Target (Development Server):**
```bash
./gradlew :composeApp:jsBrowserDevelopmentRun
```

Access at: http://localhost:8080

**WebAssembly Target (Development Server):**
```bash
./gradlew :composeApp:wasmJsBrowserDevelopmentRun
```

Access at: http://localhost:8080

**Production Build:**
```bash
./gradlew :composeApp:jsBrowserProductionWebpack
```

#### iOS Build

**Build for ARM64 (Physical Devices):**
```bash
./gradlew :composeApp:iosArm64Build
```

**Build for Simulator:**
```bash
./gradlew :composeApp:iosSimulatorArm64Build
```

**Run in Xcode:**
1. Open `iosApp/iosApp.xcodeproj` in Xcode
2. Select your target device or simulator
3. Press Run (Cmd + R)

### First Run Checklist

- [ ] JDK 17+ installed and configured in PATH
- [ ] Gemini API key configured in `Secrets.kt`
- [ ] Internet connection available for backend communication
- [ ] (Android) Physical device connected or emulator running
- [ ] (iOS) Xcode project synced and provisioning profile configured
- [ ] Sample images available in `samples-images/` folder

### Troubleshooting

**Build fails with "Unresolved reference: Secrets"**
- Ensure you copied `Secrets.sample.kt` to the correct location
- Check that the API key is properly formatted (no extra quotes or spaces)

**Cannot connect to backend**
- Verify internet connection
- Check if Render backend is online: https://dental-vision-ai-backend.onrender.com/health
- Review Ktor client logs in console output

**Web build shows blank screen**
- Clear browser cache and reload
- Check browser console for JavaScript errors
- Ensure WebAssembly is supported (Chrome 91+, Firefox 89+, Safari 15+)

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
