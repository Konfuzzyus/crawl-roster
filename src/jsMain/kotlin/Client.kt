import react.create
import react.dom.client.createRoot
import reducers.StoreModule
import theme.ThemeModule
import web.dom.Element
import web.dom.document

fun main() {
    val container: Element = document.getElementById("root")!!
    val root = createRoot(container)
    root.render(
        StoreModule.create() {
            ThemeModule {
                App { }
            }
        }
    )
}