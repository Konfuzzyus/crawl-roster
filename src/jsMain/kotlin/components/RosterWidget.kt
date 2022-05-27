package components

import mui.material.Paper
import mui.system.Box
import react.*
import reducers.StoreContext

val RosterWidget = FC<Props> {
    val myStore = useContext(StoreContext)
    val (userIdentity, setUserIdentity) = useState(myStore.state.identity.data)
    val (currentGuild, setCurrentGuild) = useState(myStore.state.calendar.selectedGuild)

    useEffectOnce {
        val unsubscribe = myStore.subscribe {
            setUserIdentity(myStore.state.identity.data)
            setCurrentGuild(myStore.state.calendar.selectedGuild)
        }
        cleanup(unsubscribe)
    }
    Paper {
        if (userIdentity == null) {
            +"Greetings, traveler. You'll have to log in to continue."
        } else {
            GuildSelector { }
            if (userIdentity.profile != null && currentGuild != null) {
                EventCalendar {
                    profile = userIdentity.profile
                    guild = currentGuild
                }
            }
        }
    }
}