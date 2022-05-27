package components

import com.benasher44.uuid.uuid4
import csstype.HtmlAttributes
import csstype.JustifyContent
import mui.material.*
import mui.system.responsive
import mui.system.sx
import org.codecranachan.roster.Player
import org.w3c.dom.HTMLInputElement
import react.*
import react.dom.onChange
import reducers.StoreContext
import reducers.signUpPlayer

external interface SignUpProps : Props {
    var profile: Player?
}

val Account = FC<SignUpProps> { props ->
    val store = useContext(StoreContext)
    val (name, setName) = useState("")
    val profile = props.profile

    if (profile == null) {
        Stack {
            sx {
                justifyContent = JustifyContent.center
            }
            direction = responsive(StackDirection.row)
            TextField {
                placeholder = "Enter a name to sign up with"
                value = name
                onChange = {
                    val t = it.target as HTMLInputElement
                    setName(t.value)
                }
            }
            Button {
                onClick = { _ -> if (name.isNotBlank()) store.dispatch(signUpPlayer(Player(uuid4(), name))) }
                +"Sign up"
            }
        }
    } else {
        Chip {
            label = ReactNode(profile.name)
        }
    }
}