import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.dentalvision.ai.App

fun main() = application {
    Window(onCloseRequest = ::exitApplication, title = "Dental Vision AI") {
        App()
    }
}