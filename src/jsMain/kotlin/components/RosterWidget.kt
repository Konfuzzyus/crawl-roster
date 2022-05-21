package components

import api.updateUserId
import org.codecranachan.roster.Identity
import org.reduxkotlin.Store
import react.*
import react.dom.html.ReactHTML.div
import reducers.ApplicationState

external interface RosterWidgetProps : Props {
    var store: Store<ApplicationState>
}

val RosterWidget = FC<RosterWidgetProps> { props ->
    val (userIdentity, setUserIdentity) = useState<Identity?>(null)

    useEffect {
        val unsubscribe = props.store.subscribe { setUserIdentity(props.store.state.identity.profile) }
        cleanup {
            unsubscribe()
        }
    }

    useEffectOnce {
        props.store.dispatch(updateUserId())
    }

    if (userIdentity == null) {
        div {
            +"Greetings, traveler. You'll have to log in to continue."
        }
    } else {
        div {
            SignUp {
                store = props.store
                profile = userIdentity.profile
            }
        }
    }
}