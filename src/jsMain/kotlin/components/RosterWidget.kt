package components

import api.fetchLinkedGuilds
import reducers.updateUserId
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import org.codecranachan.roster.Guild
import org.codecranachan.roster.Identity
import org.reduxkotlin.Store
import react.FC
import react.Props
import react.dom.html.ReactHTML.div
import react.useEffectOnce
import react.useState
import reducers.ApplicationState
import reducers.GuildSelected

external interface RosterWidgetProps : Props {
    var store: Store<ApplicationState>
}

val RosterWidget = FC<RosterWidgetProps> { props ->
    val (userIdentity, setUserIdentity) = useState(props.store.state.identity.profile)
    val (currentGuild, setCurrentGuild) = useState(props.store.state.calendar.selectedGuild)

    useEffectOnce {
        MainScope().launch {
            val guilds = fetchLinkedGuilds()
            if (guilds.isNotEmpty()) props.store.dispatch(GuildSelected(guilds[0]))
        }
        val unsubscribe = props.store.subscribe {
            setUserIdentity(props.store.state.identity.profile)
            setCurrentGuild(props.store.state.calendar.selectedGuild)
        }
        cleanup(unsubscribe)
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
            GuildSelector {
                store = props.store
                selectedGuild = currentGuild
            }
            if (userIdentity.profile != null && currentGuild != null) {
                EventCalendar {
                    store = props.store
                    profile = userIdentity.profile
                    guild = currentGuild
                }
            }
        }
    }
}