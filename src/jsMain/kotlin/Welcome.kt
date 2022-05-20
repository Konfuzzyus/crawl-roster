import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import org.codecranachan.roster.PlayerListing
import org.codecranachan.roster.UserIdentity
import react.FC
import react.Props
import react.dom.html.ReactHTML.div
import react.useEffectOnce
import react.useState

external interface WelcomeProps : Props {
    var name: String
    var players: PlayerListing
}

val mainScope = MainScope()

val Welcome = FC<WelcomeProps> { _ ->
    var identity: UserIdentity? by useState(null)

    useEffectOnce {
        mainScope.launch {
            identity = fetchUserId()
        }
    }
    div {
        +"Hello, ${identity?.name}"
    }
}