package components

import api.signUpPlayer
import com.benasher44.uuid.uuid4
import csstype.HtmlAttributes
import org.codecranachan.roster.Identity
import org.codecranachan.roster.Player
import org.reduxkotlin.Store
import react.FC
import react.Props
import react.dom.html.ReactHTML.button
import react.dom.html.ReactHTML.div
import react.dom.html.ReactHTML.input
import react.dom.html.ReactHTML.s
import react.useEffect
import react.useState
import reducers.ApplicationState

external interface SignUpProps : Props {
    var store: Store<ApplicationState>
    var profile: Player?
}

val SignUp = FC<SignUpProps> { props ->
    val (name, setName) = useState("")
    val profile = props.profile

    div {
        if (profile == null) {
            +"You seem to be new here. You'll have to sign up to continue."
            input {
                placeholder = "Enter your name"
                value = name
                onChange = { e -> setName(e.target.value) }
            }
            button {
                onClick = { _ -> if (name.isNotBlank()) props.store.dispatch(signUpPlayer(Player(uuid4(), name))) }
                +"Sign up"
            }
        } else {
            +"Signed in as ${profile.name}"
        }
    }
}