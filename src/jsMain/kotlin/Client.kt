import kotlinx.browser.document
import react.create
import react.dom.client.createRoot
import react.redux.Provider
import redux.Action
import redux.Reducer
import redux.createStore

data class AppState(
    val version: String = "1.0.0"
)

val AppReducer: Reducer<AppState, Action> = { state: AppState, action: Action ->
    state
}

fun main() {
    val container = document.getElementById("root")!!
    val appStore = createStore(AppReducer, AppState())

    val root = createRoot(container)
    root.render(
        Provider.create {
            store = appStore

            App.create {
                version = "1.0.0"
            }
        }
    )
}