import kotlinx.browser.document
import react.create
import react.dom.client.createRoot
import reducers.StoreModule

fun main() {
    val container = document.getElementById("root")!!
    val root = createRoot(container)
    root.render(
        StoreModule.create() {
            ThemeModule {
                App {
                    version = "1.0.0"
                }
            }
        }
    )
}