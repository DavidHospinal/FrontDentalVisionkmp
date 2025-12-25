import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.dentalvision.ai.App
import com.dentalvision.ai.di.appModules
import io.github.aakira.napier.DebugAntilog
import io.github.aakira.napier.Napier
import org.koin.core.context.startKoin

fun main() {
    // Initialize Napier logging
    Napier.base(DebugAntilog())
    Napier.d("Desktop App: Initializing Koin...")

    // Initialize Koin for Desktop
    startKoin {
        modules(appModules)
    }

    Napier.i("Desktop App: Koin initialized successfully")

    application {
        Window(onCloseRequest = ::exitApplication, title = "Dental Vision AI") {
            App()
        }
    }
}