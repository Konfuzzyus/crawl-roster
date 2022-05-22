import components.Identity
import components.RosterWidget
import org.reduxkotlin.Store
import react.FC
import react.Props
import react.dom.html.ReactHTML.div
import react.useEffectOnce
import react.useState
import reducers.ApplicationState
import reducers.updateUserId

external interface AppProps : Props {
    var version: String
    var store: Store<ApplicationState>
}

val App = FC<AppProps> { props ->
    val (isLoaded, setIsLoaded) = useState(props.store.state.identity.isLoaded)

    useEffectOnce {
        val unsubscribe = props.store.subscribe { setIsLoaded(props.store.state.identity.isLoaded) }
        props.store.dispatch(updateUserId())
        cleanup(unsubscribe)
    }

    div {
        div {
            +"Crawl-Roster ${props.version}"
        }
        if (isLoaded) {
            Identity { store = props.store }
            RosterWidget { store = props.store }
        } else {
            +"Checking your login information, hold on..."
        }
    }

}