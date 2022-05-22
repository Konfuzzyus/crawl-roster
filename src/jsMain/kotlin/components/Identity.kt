package components

import org.reduxkotlin.Store
import react.FC
import react.Props
import react.dom.html.ReactHTML.a
import react.dom.html.ReactHTML.div
import react.useEffect
import react.useState
import reducers.ApplicationState

external interface IdentityProps : Props {
    var store: Store<ApplicationState>
}

val Identity = FC<IdentityProps> { props ->
    val (profile, setProfile) = useState(props.store.state.identity.profile)

    useEffect {
        val unsubscribe = props.store.subscribe { setProfile(props.store.state.identity.profile) }
        cleanup(unsubscribe)
    }
    if (profile == null) {
        div {
            a {
                href = "/auth/login"
                +"Proceed to Login"
            }
        }
    } else {
        div {
            +"Logged in as ${profile.name} - "
            a {
                href = "/auth/logout"
                +"Logout"
            }
        }
    }
}