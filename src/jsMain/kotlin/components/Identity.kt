package components

import api.updateUserId
import csstype.HtmlAttributes
import org.codecranachan.roster.UserIdentity
import org.reduxkotlin.Store
import react.*
import react.dom.html.ReactHTML.a
import react.dom.html.ReactHTML.div
import react.dom.html.ReactHTML.img
import reducers.ApplicationState

external interface IdentityProps : Props {
    var store: Store<ApplicationState>
}

val Identity = FC<IdentityProps> { props ->
    val (userIdentity, setUserIdentity) = useState<UserIdentity?>(null)

    useEffect {
        val unsubscribe = props.store.subscribe { setUserIdentity(props.store.state.identity.user) }
        cleanup {
            unsubscribe()
        }
    }

    useEffectOnce {
        props.store.dispatch(updateUserId())
    }

    if (userIdentity == null) {
        div {
            a {
                href = "/auth/login"
                +"Proceed to Login"
            }
        }
    } else {
        div {
            +"Logged in as ${userIdentity.name} - "
            a {
                href = "/auth/logout"
                +"Logout"
            }
        }
    }
}