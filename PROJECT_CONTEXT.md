# Dental Vision AI - Kotlin Multiplatform
## Contexto Completo del Proyecto

**Última Actualización:** 11 de Enero 2026
**Estado:** Proyecto completado y enviado al concurso Kotlin Multiplatform
**Desarrollador:** Oscar David Hospinal Roman
**Universidad:** Pontifical Catholic University of Chile

---

## 1. OVERVIEW DEL PROYECTO

Dental Vision AI es una aplicación multiplataforma profesional para análisis de imágenes dentales mediante inteligencia artificial. El sistema integra el modelo de deep learning YOLOv12 para detección dental y Gemini AI para generar insights clínicos personalizados.

### Plataformas Implementadas (3 Principales)
- ✅ **Android** (API 24+) - Totalmente funcional con Material You
- ✅ **Desktop JVM** (Windows, macOS, Linux) - Totalmente funcional con distribuciones nativas
- ✅ **Web WASM** (WebAssembly) - Totalmente funcional en navegadores modernos
- ⚠️ **iOS** - Implementación parcial/experimental (no es objetivo principal)

### Características Principales
- Gestión completa de pacientes (CRUD operations)
- Análisis de imágenes dentales con IA (YOLOv12)
- Generación de reportes clínicos profesionales
- Insights clínicos con Gemini AI
- Sistema de notación dental FDI
- Búsqueda en tiempo real con debounce
- Interfaz responsive con Material Design 3

---

## 2. ARQUITECTURA TÉCNICA

### Stack Tecnológico Core

| Componente | Tecnología | Versión | Propósito |
|------------|------------|---------|-----------|
| **UI Framework** | Compose Multiplatform | 1.7.1 | Interfaz compartida (95% código común) |
| **Lenguaje** | Kotlin | 2.1.0 | Lenguaje base multiplataforma |
| **Build System** | Gradle | 8.0+ | Sistema de construcción |
| **JDK** | OpenJDK | 17 | Plataforma Java |
| **Material Design** | Material3 | Latest | Sistema de diseño |

### Networking y Serialización

| Componente | Tecnología | Versión | Propósito |
|------------|------------|---------|-----------|
| **HTTP Client** | Ktor Client | 3.0.0 | Comunicación con backend |
| **Serialization** | Kotlinx Serialization | 1.7.3 | JSON parsing |
| **Coroutines** | Kotlinx Coroutines | 1.10.2 | Asincronía multiplataforma |
| **DateTime** | Kotlinx DateTime | 0.6.1 | Manejo de fechas |

### Gestión de Estado y DI

| Componente | Tecnología | Versión | Propósito |
|------------|------------|---------|-----------|
| **State Management** | AndroidX Lifecycle ViewModel | 2.8.2 | MVVM pattern |
| **Navigation** | AndroidX Navigation Compose | 2.8.0-alpha10 | Navegación multiplataforma |
| **Dependency Injection** | Koin | 4.0.0 | Inyección de dependencias |
| **Logging** | Napier | 2.7.1 | Logging multiplataforma |

### Multimedia y Animaciones

| Componente | Tecnología | Versión | Propósito |
|------------|------------|---------|-----------|
| **Image Loading** | Kamel | 1.0.3 | Carga de imágenes ByteArray |
| **Image Loading** | Coil3 | 3.0.4 | Carga de imágenes URL/remoto |
| **Lottie Animations** | Compottie | 2.0.0-rc01 | Animaciones JSON Lottie |

### Platform-Specific Engines

| Plataforma | HTTP Engine | Propósito |
|------------|-------------|-----------|
| **Android** | ktor-client-android | Motor HTTP nativo Android |
| **iOS** | ktor-client-darwin | Motor HTTP nativo iOS/macOS |
| **Desktop** | ktor-client-java | Motor HTTP Java/JVM |
| **Web** | ktor-client-js | Motor HTTP JavaScript/WASM |

---

## 3. APIS EXTERNAS INTEGRADAS

### 3.1 Google Gemini API
- **URL Base:** `https://generativelanguage.googleapis.com/v1beta/models/`
- **Modelo Utilizado:** `gemini-1.5-flash-latest`
- **Propósito:** Generación de insights clínicos personalizados
- **Configuración:** API key incluida en `Secrets.kt` (demo para concurso)
- **Features:**
  - Análisis de resultados de detección YOLOv12
  - Generación de recomendaciones de tratamiento
  - Evaluación de riesgo (LOW/MODERATE/HIGH)
  - Planes preventivos personalizados
  - Streaming de respuestas en tiempo real

**Endpoints Utilizados:**
```
POST /v1beta/models/gemini-1.5-flash-latest:generateContent
```

**Formato de Prompt:**
```kotlin
"""
Hello Dr. {doctorName},

I'm analyzing a dental radiograph for patient {patientName} (ID: {patientId}).

Detection Results:
- Total teeth detected: {teethCount}
- Cavities found: {cavitiesCount}
- Average confidence: {confidence}%
- Overall status: {status}

Please provide:
1. Clinical interpretation
2. Risk assessment
3. Treatment recommendations
4. Preventive care plan
"""
```

### 3.2 HuggingFace Spaces API
- **URL:** `https://huggingface.co/spaces/DavidHosp/Dental-vision-kmp-contest`
- **Modelo:** YOLOv12 Dental Detection
- **Framework:** Gradio
- **Propósito:** Detección de dientes y caries en imágenes dentales
- **Configuración:** Endpoint público sin autenticación

**Cliente Utilizado:**
```kotlin
implementation("dev.gradio:client:0.2.0") // Gradio Kotlin Client
```

**Proceso de Análisis:**
1. Upload de imagen dental (ByteArray)
2. Procesamiento con YOLOv12
3. Detección de bounding boxes
4. Clasificación de dientes y caries
5. Cálculo de confidence scores
6. Retorno de imagen procesada con detecciones

**Formato de Respuesta:**
```json
{
  "detections": [
    {
      "bbox": [x1, y1, x2, y2],
      "class": "tooth" | "cavity",
      "confidence": 0.0-1.0,
      "label": "Tooth 11" | "Cavity"
    }
  ],
  "processed_image_base64": "...",
  "summary": {
    "total_teeth": 32,
    "cavities_found": 2,
    "avg_confidence": 0.95
  }
}
```

### 3.3 Backend Flask API (Render)
- **URL:** `https://backenddental-vision-ai.onrender.com/`
- **Framework:** Flask + SQLAlchemy
- **Database:** SQLite
- **Propósito:** Gestión de datos y orquestación
- **Autenticación:** JWT tokens

**Endpoints Principales:**

#### Autenticación
```
POST /api/v1/auth/login
Body: { "username": "admin", "password": "admin123" }
Response: { "token": "jwt_token", "doctor_name": "Mario Herdinger" }
```

#### Pacientes
```
GET    /api/v1/patients              # Listar pacientes
GET    /api/v1/patients/search?q=    # Buscar pacientes
POST   /api/v1/patients              # Crear paciente
PUT    /api/v1/patients/{id}         # Actualizar paciente
DELETE /api/v1/patients/{id}         # Eliminar paciente
```

#### Análisis
```
GET  /api/v1/analyses                # Listar análisis
GET  /api/v1/analyses/{id}           # Obtener análisis específico
POST /api/v1/analyses                # Crear análisis
POST /api/v1/analyses/analyze        # Ejecutar análisis con YOLOv12
```

#### Reportes
```
GET /api/v1/reports                  # Listar reportes
GET /api/v1/reports/{id}             # Obtener reporte específico
GET /api/v1/reports/{id}/pdf         # Descargar PDF
```

#### Sistema
```
POST /api/v1/system/reset            # Reset database con seed data
GET  /api/v1/system/health           # Health check
```

**Características del Backend:**
- Free tier Render (se duerme después de inactividad)
- Tiempo de warm-up: 30-60 segundos
- SQLite persistente con 25 pacientes demo
- Auto-seed data con notación FDI
- CORS habilitado para frontend

---

## 4. ESTRUCTURA DEL PROYECTO

```
dentalkpmfront/
├── composeApp/
│   ├── src/
│   │   ├── commonMain/          # 95% código compartido
│   │   │   └── kotlin/com/dentalvision/ai/
│   │   │       ├── App.kt       # Entry point
│   │   │       ├── data/
│   │   │       │   ├── remote/
│   │   │       │   │   ├── api/
│   │   │       │   │   │   ├── ApiClient.kt
│   │   │       │   │   │   └── Secrets.kt  # ⚠️ API keys (demo)
│   │   │       │   │   ├── gemini/
│   │   │       │   │   │   └── GeminiApiClient.kt
│   │   │       │   │   ├── gradio/
│   │   │       │   │   │   └── GradioClient.kt
│   │   │       │   │   └── service/
│   │   │       │   │       ├── AuthService.kt
│   │   │       │   │       ├── PatientService.kt
│   │   │       │   │       ├── AnalysisService.kt
│   │   │       │   │       └── ReportService.kt
│   │   │       │   └── repository/
│   │   │       │       ├── AuthRepositoryImpl.kt
│   │   │       │       ├── PatientRepositoryImpl.kt
│   │   │       │       └── AnalysisRepositoryImpl.kt
│   │   │       ├── domain/
│   │   │       │   └── model/
│   │   │       │       ├── Patient.kt
│   │   │       │       ├── Analysis.kt
│   │   │       │       ├── Report.kt
│   │   │       │       └── Detection.kt
│   │   │       ├── presentation/
│   │   │       │   ├── component/
│   │   │       │   │   ├── AppInputs.kt
│   │   │       │   │   ├── DentalImagePreview.kt
│   │   │       │   │   ├── NavigationDrawerContent.kt
│   │   │       │   │   ├── ResponsiveLayout.kt
│   │   │       │   │   └── StateViews.kt
│   │   │       │   ├── screen/
│   │   │       │   │   ├── splash/
│   │   │       │   │   │   └── SplashScreen.kt
│   │   │       │   │   ├── login/
│   │   │       │   │   │   └── LoginScreen.kt
│   │   │       │   │   ├── home/
│   │   │       │   │   │   └── HomeScreen.kt
│   │   │       │   │   ├── patient/
│   │   │       │   │   │   ├── PatientsScreen.kt
│   │   │       │   │   │   └── PatientFormDialog.kt
│   │   │       │   │   ├── analysis/
│   │   │       │   │   │   └── NewAnalysisScreen.kt
│   │   │       │   │   ├── reports/
│   │   │       │   │   │   └── ReportsScreen.kt
│   │   │       │   │   └── insights/
│   │   │       │   │       └── ClinicalInsightsScreen.kt
│   │   │       │   ├── viewmodel/
│   │   │       │   │   ├── AuthViewModel.kt
│   │   │       │   │   ├── PatientsViewModel.kt
│   │   │       │   │   ├── NewAnalysisViewModel.kt
│   │   │       │   │   ├── ReportsViewModel.kt
│   │   │       │   │   └── ClinicalInsightsViewModel.kt
│   │   │       │   └── navigation/
│   │   │       │       ├── NavGraph.kt
│   │   │       │       └── Screen.kt
│   │   │       ├── di/
│   │   │       │   └── AppModule.kt
│   │   │       └── theme/
│   │   │           ├── Color.kt
│   │   │           └── Theme.kt
│   │   ├── androidMain/         # Android-specific
│   │   ├── iosMain/             # iOS-specific
│   │   ├── jvmMain/             # Desktop-specific
│   │   └── wasmJsMain/          # Web-specific
│   └── build.gradle.kts
├── desktopApp/
│   └── src/jvmMain/kotlin/
│       └── main.kt
├── androidApp/
│   └── src/androidMain/
│       └── AndroidManifest.xml
├── gradle/
│   └── libs.versions.toml      # Dependency versions
├── samples-images/              # Imágenes de prueba
├── README.md
├── PROJECT_CONTEXT.md          # Este archivo
└── build.gradle.kts
```

---

## 5. FUNCIONALIDADES IMPLEMENTADAS

### 5.1 Sistema de Autenticación
- **Tecnología:** JWT tokens + LocalDoctorName CompositionLocal
- **Credenciales Demo:** admin / admin123
- **Doctor por defecto:** Mario Herdinger
- **Features:**
  - Login con validación de credenciales
  - Persistencia de sesión con tokens
  - Nombre de doctor dinámico en toda la app
  - Logout con limpieza de estado

**Archivos clave:**
- `presentation/screen/login/LoginScreen.kt`
- `presentation/viewmodel/AuthViewModel.kt`
- `data/remote/service/AuthService.kt`

### 5.2 Gestión de Pacientes
- **Patrón:** MVVM + Repository pattern
- **Operaciones:** Create, Read, Update, Delete
- **Features:**
  - Lista de pacientes con búsqueda en tiempo real
  - Búsqueda con debounce de 300ms para optimización
  - Shimmer loading effects durante carga
  - Empty states profesionales
  - Formulario de creación/edición
  - Validación de datos (email, teléfono, etc.)
  - Sistema de notación FDI para dientes

**Archivos clave:**
- `presentation/screen/patient/PatientsScreen.kt`
- `presentation/screen/patient/PatientFormDialog.kt`
- `presentation/viewmodel/PatientsViewModel.kt`
- `data/repository/PatientRepositoryImpl.kt`

**Optimización de Búsqueda:**
```kotlin
init {
    launchWithErrorHandler {
        _searchQuery
            .debounce(300.milliseconds)
            .distinctUntilChanged()
            .collect { query ->
                loadPatients()
            }
    }
}
```

### 5.3 Análisis de Imágenes Dentales
- **Tecnología:** YOLOv12 via HuggingFace Gradio
- **Flow:** Selección paciente → Upload imagen → Análisis → Resultados
- **Features:**
  - Image picker multiplataforma
  - Preview de imagen con zoom/pan (Kamel)
  - Procesamiento asíncrono con loading states
  - Visualización de bounding boxes
  - Estadísticas de detección (dientes, caries, confianza)
  - Oral Health Index con progress bars
  - Quick Stats Card
  - Risk badge (LOW/MODERATE/HIGH)

**Componentes de UI:**
- `DentalImagePreview.kt` - Visor con zoom/pan
- `NewAnalysisScreen.kt` - Flow completo de análisis
- `StateViews.kt` - Shimmer effects y loading states

**Features de UX:**
- Auto-focus en búsqueda de pacientes (Android)
- Keyboard controller para dispositivos físicos
- Zoom controls (+/-, reset)
- Scale indicator
- Error handling con mensajes detallados
- Proceso incremental con estados visuales

### 5.4 Clinical Insights con Gemini AI
- **Modelo:** gemini-1.5-flash-latest
- **Patrón:** Reactive state con StateFlow
- **Features:**
  - Generación de insights clínicos personalizados
  - Análisis de riesgo (LOW/MODERATE/HIGH)
  - Recomendaciones de tratamiento
  - Plan preventivo personalizado
  - Animaciones condicionales según riesgo:
    - LOW: Confetti + serpentinas de celebración
    - MODERATE: Amber glow background
    - HIGH: Pulsing red background
  - Nombre de doctor dinámico en prompt
  - Streaming de respuestas (opcional)

**Archivos clave:**
- `presentation/screen/insights/ClinicalInsightsScreen.kt`
- `presentation/viewmodel/ClinicalInsightsViewModel.kt`
- `data/remote/gemini/GeminiApiClient.kt`

**Animaciones Implementadas:**
```kotlin
// LOW RISK - Celebración
ConfettiAnimation()
SerpentineAnimation()

// MODERATE RISK - Amber glow
AmberGlowBackground()

// HIGH RISK - Pulsing red
HighRiskPulsingBackground() // Infinite transition con alpha animation
```

### 5.5 Sistema de Reportes
- **Tecnología:** Backend PDF generation (futuro) + In-app viewer
- **Features:**
  - Lista de análisis históricos
  - Búsqueda de reportes con debounce
  - Filtrado por paciente
  - Vista de calendario (funcional pero vacío por seed data)
  - Navegación a Clinical Insights
  - Export capabilities (preparado)

**Optimización implementada:**
```kotlin
// ReportsViewModel.kt - Debounced search
_searchQuery
    .debounce(300.milliseconds)
    .distinctUntilChanged()
    .collect { query ->
        loadAllAnalyses()
    }
```

### 5.6 Splash Screen con Lottie
- **Tecnología:** Compottie + Lottie JSON animation
- **Features:**
  - Animación de logo con BeeClean.json
  - Gradient background animado
  - Floating shapes parallax
  - Pulsing scale animation
  - Glow effects en título
  - Click-to-continue (sin auto-navegación)
  - Safe resource loading para WASM
  - Fallback UI en caso de error
  - Developer credits

**Decisión técnica crítica:**
```kotlin
// NO usar withContext(Dispatchers.Default) en WASM
// LaunchedEffect ya provee el contexto correcto
LaunchedEffect(Unit) {
    try {
        val bytes = Res.readBytes("files/BeeClean.json")
        jsonString = bytes.decodeToString()
    } catch (e: Exception) {
        loadingError = e.message
    }
}
```

### 5.7 Navigation Drawer
- **Features:**
  - Scale animation en items seleccionados (1.0f → 1.15f)
  - Container color con alpha 0.1 para highlight
  - Material Icons nativos (en lugar de ExtendedIcons)
  - Smooth transitions con 300ms duration
  - Navegación a 6 pantallas principales

---

## 6. PROBLEMAS RESUELTOS Y DECISIONES TÉCNICAS

### 6.1 WASM Crash con kotlinx.coroutines
**Problema:** App WASM mostraba pantalla blanca con error `kotlinx.coroutines.error_$external_fun`

**Diagnóstico:**
- Bug conocido en kotlinx-coroutines 1.9.0 (GitHub issue #4213)
- Uso incorrecto de `Dispatchers.Default` en WASM single-threaded

**Solución:**
1. Upgrade a kotlinx-coroutines 1.10.2
2. Eliminar `withContext(Dispatchers.Default)` en resource loading
3. Confiar en el contexto de `LaunchedEffect`

**Commit:** ebdcb5e, 5e97fb5

**Aprendizaje:** WASM es single-threaded y no soporta dispatchers de I/O. LaunchedEffect ya provee contexto adecuado.

### 6.2 Desktop .exe Crash en Login
**Problema:** `java/net/http/HttpClient$Version` error al iniciar app empaquetada

**Diagnóstico:**
- jpackage crea JRE minimalista excluyendo módulos por defecto
- Ktor requiere `java.net.http` para HTTP operations
- SSL/HTTPS requiere `jdk.crypto.ec` para Elliptic Curve Cryptography

**Solución:**
```kotlin
// composeApp/build.gradle.kts
nativeDistributions {
    includeAllModules = true
    // O específicamente:
    modules(
        "java.instrument",
        "java.net.http",       // HTTP client API
        "jdk.unsupported",
        "jdk.crypto.ec",       // SSL/HTTPS support
        "java.logging",
        "java.xml",
        "java.sql",
        "java.naming"
    )
}
```

**Commits:** 97d24dc

**Aprendizaje:** Desktop distributions necesitan módulos Java explícitos para networking seguro.

### 6.3 Lottie Animation Removed (Rejected)
**Problema:** Propuesta de eliminar Lottie para "solucionar" WASM crash

**Feedback del Usuario:** "RECHAZO la solución anterior. NO quiero eliminar la animación Lottie"

**Solución correcta:**
- Implementar safe resource loading pattern
- Try-catch anidado para resource loading y decoding
- Conditional rendering (only when composition ready)
- Fallback "DV" logo on error
- Migrar a Image + rememberLottiePainter (nuevo API)

**Aprendizaje:** No eliminar features por bugs. Implementar defensive programming.

### 6.4 Search Performance en Reports (20s delay)
**Problema:** Búsqueda lenta en Desktop/Web vs Android

**Diagnóstico:**
- `onSearchQueryChanged()` llamaba `loadAllAnalyses()` en cada keystroke
- Cada keystroke = 1 HTTP request
- "Judith" = 6 requests antes de solución

**Solución:**
```kotlin
init {
    launchWithErrorHandler {
        _searchQuery
            .debounce(300.milliseconds)  // Wait 300ms después del último keystroke
            .distinctUntilChanged()       // Skip queries duplicados
            .collect { query ->
                _uiState.value = ReportsUiState.Loading
                loadAllAnalyses()
            }
    }
}
```

**Resultado:** Reducción del 83% en requests, respuesta instantánea

**Commit:** 8eb8621

### 6.5 Android Keyboard No Aparece en Dispositivos Físicos
**Problema:** Teclado no aparece al tocar search field en dispositivos físicos Android (sí en emulador)

**Diagnóstico:**
- TextField no solicita focus explícitamente
- Dispositivos físicos requieren invocación explícita de keyboard controller
- Emuladores tienen comportamiento más permisivo

**Solución:**
```kotlin
val searchFocusRequester = remember { FocusRequester() }
val keyboardController = LocalSoftwareKeyboardController.current

TextField(
    modifier = Modifier
        .focusRequester(searchFocusRequester)
        .onFocusChanged { focusState ->
            if (focusState.isFocused) {
                keyboardController?.show()
            }
        }
)

LaunchedEffect(isDropdownExpanded) {
    if (isDropdownExpanded) {
        delay(100)
        searchFocusRequester.requestFocus()
    }
}
```

**Commit:** d6566a3

### 6.6 Fechas en Español en Appointment Dialog
**Problema:** `selectedDate.toString()` mostraba "ene., diciembre" en lugar de inglés

**Solución:**
```kotlin
val formattedDate = selectedDate?.let {
    val monthName = it.month.name.lowercase().replaceFirstChar { char ->
        char.uppercase()
    }
    "$monthName ${it.dayOfMonth}, ${it.year}"
} ?: "Select date"
```

**Commit:** 7a1b463

### 6.7 Gemini Hardcoded Doctor Name
**Problema:** Clinical Insights siempre saludaba "Dr. David" en lugar del doctor loggeado

**Solución:**
```kotlin
// ClinicalInsightsScreen.kt
val doctorName = LocalDoctorName.current
viewModel.generateInsight(
    analysis = analysis,
    doctorName = doctorName  // Pasar doctor name dinámicamente
)

// ClinicalInsightsViewModel.kt
fun generateInsight(analysis: Analysis, doctorName: String) {
    val prompt = """
        Hello Dr. $doctorName,  // Usar parámetro en lugar de hardcoded
        ...
    """
}
```

**Commit:** 7a1b463

### 6.8 Image Preview Not Rendering on Web/Android
**Problema:** DentalImagePreview mostraba errores genéricos sin detalles

**Solución:**
```kotlin
KamelImage(
    resource = asyncPainterResource(data = imageData),
    contentDescription = "Dental Image",
    onLoading = {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator()
            Text("Loading image...", color = DentalColors.Primary)
        }
    },
    onFailure = { exception ->
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Default.Error, contentDescription = null, size = 48.dp)
            Spacer(height = 8.dp)
            Text("Error: ${exception.message ?: "Unknown error"}")
            Text("Image size: ${(imageBytes.size / 1024)} KB")
        }
    }
)
```

**Mejora:** Mensajes de error detallados con tamaño de imagen para debugging

### 6.9 Telephoto Library Incompatibility con WASM
**Problema:** `telephoto-zoomable` no soporta WasmJS target

**Solución:**
- Eliminar telephoto dependencies
- Usar Kamel (ya incluido) para image loading
- Implementar zoom manual con `rememberTransformableState`
- GraphicsLayer con scale/translation

**Resultado:** Zoom/pan funciona en Desktop, Web, y Android

### 6.10 Contest Zero-Config Requirement
**Problema:** Reglas del concurso requieren ejecución sin configuración adicional

**Decisión:**
1. Incluir API key de Gemini en `Secrets.kt` (force-add to git)
2. Actualizar README de "MANDATORY configuration" a "Zero Config"
3. Documentar que key es temporal y solo para concurso
4. Security disclaimer sobre violación intencional de best practices

**Commit:** d779a78

**Justificación:** Permitir evaluación inmediata por jueces sin crear cuentas Google

---

## 7. COMANDOS ÚTILES

### Build Commands

#### Android
```bash
# Compilar
./gradlew :composeApp:compileDebugKotlinAndroid

# Construir APK
./gradlew :composeApp:assembleDebug

# Instalar en dispositivo
./gradlew :composeApp:installDebug
adb devices
adb install androidApp/build/outputs/apk/debug/androidApp-debug.apk
```

#### Desktop (JVM)
```bash
# Compilar
JAVA_HOME='/mnt/c/Program Files/Java/jdk-17' ./gradlew :composeApp:compileKotlinJvm

# Ejecutar
./gradlew :desktopApp:run

# Crear distribución
./gradlew :desktopApp:createDistributable
# Output: desktopApp/build/compose/binaries/main/app/

# Crear instalador nativo
./gradlew :desktopApp:packageDistributionForCurrentOS
```

#### Web (WASM)
```bash
# Compilar
./gradlew :composeApp:compileKotlinWasmJs

# Ejecutar dev server
./gradlew :composeApp:wasmJsBrowserDevelopmentRun
# Access at http://localhost:8080

# Build production
./gradlew :composeApp:wasmJsBrowserProductionWebpack
```

### Multi-platform Build
```bash
# Compilar todas las plataformas
JAVA_HOME='/mnt/c/Program Files/Java/jdk-17' ./gradlew \
    :composeApp:compileKotlinJvm \
    :composeApp:compileKotlinWasmJs \
    :composeApp:compileDebugKotlinAndroid
```

### Debugging Commands
```bash
# Ver dependencias
./gradlew :composeApp:dependencies --configuration compileClasspath

# Limpiar build
./gradlew clean

# Ver tareas disponibles
./gradlew :desktopApp:tasks

# Verificar estructura del proyecto
./gradlew printProjectStructure
```

### Git Commands
```bash
# Ver estado
git status

# Add con force (para Secrets.kt)
git add -f composeApp/src/commonMain/kotlin/com/dentalvision/ai/data/remote/api/Secrets.kt

# Commit
git commit -m "mensaje"

# Ver historial
git log --oneline -10

# Ver cambios
git diff
git diff --stat
```

---

## 8. CONFIGURACIÓN DEL ENTORNO

### Requisitos Instalados
- **JDK:** OpenJDK 17 (ubicado en `/mnt/c/Program Files/Java/jdk-17`)
- **Android Studio:** Hedgehog 2023.1.1+
- **Android SDK:** API 24-35
- **Gradle:** 8.0+ (via wrapper)
- **Node.js:** No requerido (no es proyecto web tradicional)

### Variables de Entorno Clave
```bash
export JAVA_HOME='/mnt/c/Program Files/Java/jdk-17'
export PATH="$JAVA_HOME/bin:$PATH"
```

### Archivos de Configuración

#### gradle/libs.versions.toml
```toml
[versions]
kotlin = "2.1.0"
compose-multiplatform = "1.7.1"
kotlinx-coroutines = "1.10.2"  # ⚠️ Crítico para WASM
ktor = "3.0.0"
koin = "4.0.0"
coil = "3.0.4"
compottie = "2.0.0-rc01"
```

#### composeApp/build.gradle.kts - Módulos Desktop
```kotlin
nativeDistributions {
    includeAllModules = true  // ⚠️ Crítico para SSL/HTTPS
}
```

---

## 9. DATOS DEMO Y TESTING

### Credenciales de Login
```
Username: admin
Password: admin123
Doctor: Mario Herdinger
```

### Backend Demo Data
- **25 pacientes pre-seeded** con notación FDI
- **Análisis históricos** de ejemplo
- **Endpoints:**
  - Backend: https://backenddental-vision-ai.onrender.com/
  - HuggingFace: https://huggingface.co/spaces/DavidHosp/Dental-vision-kmp-contest

### Imágenes de Prueba
- **Ubicación:** `samples-images/` en raíz del proyecto
- **Cantidad:** 5 imágenes dentales profesionales
- **Formatos:** JPG, PNG
- **Propósito:** Testing inmediato del modelo YOLOv12

### Warm-up de Servicios (Free Tier)
Antes del primer uso, activar servicios:
1. Abrir https://backenddental-vision-ai.onrender.com/ (esperar 50s)
2. Abrir https://huggingface.co/spaces/DavidHosp/Dental-vision-kmp-contest

---

## 10. ESTADO ACTUAL DEL PROYECTO

### Completado ✅
- [x] Implementación completa de 3 plataformas (Android, Desktop, Web)
- [x] Integración con YOLOv12 para análisis dental
- [x] Integración con Gemini AI para insights clínicos
- [x] Sistema completo de gestión de pacientes
- [x] Análisis de imágenes con detección de bounding boxes
- [x] Clinical Insights con animaciones condicionales
- [x] Sistema de reportes con búsqueda optimizada
- [x] Splash screen animado con Lottie
- [x] Navigation drawer con animaciones
- [x] Todas las optimizaciones de UX/performance
- [x] Desktop distribution con módulos Java correctos
- [x] WASM build estable sin crashes
- [x] Configuración zero-config para concurso
- [x] README comprehensivo con videos demo
- [x] Proyecto enviado al concurso (11 Enero 2026)

### Features Futuras (Post-Concurso)
- [ ] Exportación de reportes a PDF (actualmente solo backend)
- [ ] Implementación completa de iOS (actualmente experimental)
- [ ] Sistema de notificaciones push
- [ ] Modo offline con cache local
- [ ] Historial de cambios de pacientes
- [ ] Múltiples idiomas (i18n)
- [ ] Dark mode completo
- [ ] Generación de gráficos de tendencias
- [ ] Integración con sistemas PACS médicos
- [ ] Autenticación biométrica

### Conocido Limitaciones
- **iOS:** Implementación parcial, no es plataforma principal
- **SQLDelight:** Comentado en código (usando solo API remota)
- **PDF Export:** Backend implementado, frontend preparado pero no conectado
- **Calendar View:** Funcional pero vacío (seed data limitado)
- **Free Tier Backends:** Requieren warm-up de 30-60s

---

## 11. CONTEXTO DEL CONCURSO

### Información de Envío
- **Fecha de Envío:** 11 de Enero 2026
- **Plataformas Presentadas:** Android, Desktop (JVM), Web (WASM)
- **Concurso:** Kotlin Multiplatform Contest
- **Requisitos Cumplidos:**
  - ✅ Mínimo 2 plataformas (presentamos 3)
  - ✅ Zero-config setup (API key incluida)
  - ✅ README con instrucciones claras
  - ✅ Video demo (8 min mostrando 3 plataformas)
  - ✅ Video técnico extendido (26 min en YouTube)

### Videos Demo
1. **Contest Demo (8 min):** https://www.youtube.com/watch?v=P7qHrPUVlpY
   - Muestra las 3 plataformas funcionando
   - Workflow completo de análisis
   - Features principales demostradas

2. **Technical Deep Dive (26 min):** https://www.youtube.com/watch?v=ZGjs8cYaYCI
   - Arquitectura del sistema
   - Walkthrough de código
   - Integraciones con APIs externas
   - Debugging workflow

### Releases
- **GitHub Release:** v1.0.0
- **Binarios Incluidos:**
  - Android APK: `DentalVisionAI-v1.0.0.apk`
  - Windows: `.msi` installer
  - macOS: `.dmg` installer
  - Linux: `.deb` installer

### Repositorio
- **URL:** https://github.com/DavidHospinal/FrontDentalVisionkmp
- **Branch Principal:** main
- **Último Commit:** d779a78 - "feat: Add zero-config setup for Kotlin Multiplatform Contest submission"

---

## 12. LECCIONES APRENDIDAS

### Technical Learnings
1. **WASM es single-threaded:** No usar `Dispatchers.IO` o `Dispatchers.Default`
2. **JPackage requiere módulos explícitos:** Especialmente `jdk.crypto.ec` para HTTPS
3. **Coroutines 1.9.0 tiene bugs en WASM:** Actualizar a 1.10.2+
4. **Kamel vs Coil3:** Kamel mejor para ByteArray, Coil3 mejor para URLs
5. **Telephoto no soporta WASM:** Implementar zoom manual
6. **Android físico vs emulador:** Dispositivos físicos necesitan keyboard controller explícito
7. **Debounce es crítico:** Search sin debounce destruye performance
8. **Resource loading en WASM:** Usar try-catch exhaustivo, no asumir éxito
9. **Lottie en KMP:** Compottie funciona pero requiere safe loading
10. **Desktop distributions:** `includeAllModules = true` es más seguro que lista manual

### Development Workflow Learnings
1. **Compilar frecuentemente en todas las plataformas:** Desktop puede compilar pero WASM fallar
2. **No eliminar features por bugs:** Siempre buscar fix primero
3. **User feedback es crítico:** Soluciones propuestas deben validarse con usuario
4. **Git force-add con precaución:** Solo para casos específicos como contest submissions
5. **Documentation es tiempo bien invertido:** README comprehensivo ayuda a jueces y colaboradores

### Architectural Learnings
1. **MVVM + Repository es óptimo para KMP**
2. **Koin es excelente para DI multiplataforma**
3. **StateFlow + ViewModel es suficiente (no necesitas Redux)**
4. **Compose Multiplatform permite 95% code sharing real**
5. **Platform-specific code solo para file pickers, notifications**

---

## 13. CONTACTO Y RECURSOS

### Desarrollador
- **Nombre:** Oscar David Hospinal Roman
- **Email Universidad:** u202021214@upc.edu.pe
- **Email Postgrado:** oscardavid.hospinal@uc.cl
- **GitHub:** https://github.com/DavidHospinal
- **LinkedIn:** https://www.linkedin.com/in/oscardavidhospinal/
- **Portfolio:** https://portfolio-cv-oscardavid-hospinal.vercel.app/

### Universidad
- **Institución:** Pontifical Catholic University of Chile
- **Programa:** Undergraduate Student

### Recursos Externos
- **Backend Deploy:** https://backenddental-vision-ai.onrender.com/
- **HuggingFace Space:** https://huggingface.co/spaces/DavidHosp/Dental-vision-kmp-contest
- **GitHub Releases:** https://github.com/DavidHospinal/FrontDentalVisionkmp/releases/tag/v1.0.0

### Documentación Técnica Útil
- Kotlin Multiplatform: https://kotlinlang.org/docs/multiplatform.html
- Compose Multiplatform: https://www.jetbrains.com/lp/compose-multiplatform/
- Ktor Client: https://ktor.io/docs/client.html
- Koin: https://insert-koin.io/
- Gemini API: https://ai.google.dev/docs
- Gradio Client: https://www.gradio.app/guides/getting-started-with-the-python-client

---

## 14. NOTAS FINALES

Este proyecto representa un sistema completo de producción para análisis dental con IA, implementado con las mejores prácticas de Kotlin Multiplatform. El código está optimizado, documentado, y listo para evaluación en concurso.

**Próximos pasos recomendados (post-concurso):**
1. Rotar API key de Gemini (revocar la demo)
2. Implementar sistema de API keys por usuario
3. Completar integración de PDF export en frontend
4. Añadir tests unitarios y de integración
5. Implementar CI/CD pipeline
6. Considerar monetización o implementación en clínicas reales

**Fecha de creación de este documento:** 11 de Enero 2026
**Última actualización:** 11 de Enero 2026
**Versión:** 1.0.0 - Contest Submission

---

*Este documento fue generado para preservar el contexto completo del proyecto y facilitar la reanudación del desarrollo en el futuro.*
