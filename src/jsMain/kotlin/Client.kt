import kotlinx.browser.document
import org.reduxkotlin.*
import react.create
import react.dom.client.createRoot
import reducers.ApplicationState
import reducers.identityReducer

fun main() {
    val appStore: Store<ApplicationState> = createStore(
        combineReducers(identityReducer),
        ApplicationState(),
        applyMiddleware(createThunkMiddleware())
    )

    val container = document.getElementById("root")!!
    val root = createRoot(container)
    root.render(
        App.create {
            version = "1.0.0"
            store = appStore
        }
    )
}