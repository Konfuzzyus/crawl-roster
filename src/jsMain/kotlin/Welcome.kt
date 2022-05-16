import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import me.konfuzzyus.roster.Player
import me.konfuzzyus.roster.PlayerListing
import react.FC
import react.Props
import react.dom.html.InputType
import react.dom.html.ReactHTML.div
import react.dom.html.ReactHTML.input
import react.useEffectOnce
import react.useState

external interface WelcomeProps : Props {
    var name: String
    var players: PlayerListing
}

val mainScope = MainScope()

val Welcome = FC<WelcomeProps> { props ->
    var name by useState(props.name)
    var players by useState(props.players)

    useEffectOnce {
        mainScope.launch {
            players = fetchPlayers()
        }
    }
    div {
        +"Hello, $name"
    }
    div {
        +"We have $players."
    }
    div {
        input {
            id = "name-input"
            type = InputType.text
            value = name
            onChange = { event ->
                name = event.target.value
            }
        }
        input {
            type = InputType.button
            value = "Add Player"
            onClick = { _ ->
                val p = Player(
                    handle = name
                )
                mainScope.launch {
                    addPlayer(p)
                    players = fetchPlayers()
                }
            }
        }
    }
}