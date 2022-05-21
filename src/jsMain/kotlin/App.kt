import components.Identity
import components.RosterWidget
import org.reduxkotlin.Store
import react.FC
import react.Props
import react.dom.html.ReactHTML.div
import reducers.ApplicationState

external interface AppProps : Props {
    var version: String
    var store: Store<ApplicationState>
}

val App = FC<AppProps> { props ->
    div {
        div {
            +"Crawl-Roster ${props.version}"
        }
        Identity { store = props.store }
        RosterWidget { store = props.store }
    }
}