package components

import components.editors.EventEditor
import components.editors.PlayerEditor
import components.editors.TableEditor
import mui.material.Paper
import react.FC
import react.Props
import react.use
import react.useEffectOnceWithCleanup
import react.useState
import reducers.StoreContext

val RosterWidget = FC<Props> {
    val myStore = use(StoreContext)!!
    val (userIdentity, setUserIdentity) = useState(myStore.state.identity.player)
    val (currentGuild, setCurrentGuild) = useState(myStore.state.calendar.selectedLinkedGuild)

    useEffectOnceWithCleanup {
        val unsubscribe = myStore.subscribe {
            setUserIdentity(myStore.state.identity.player)
            setCurrentGuild(myStore.state.calendar.selectedLinkedGuild)
        }
        onCleanup(unsubscribe)
    }
    Paper {
        if (userIdentity == null) {
            +"Greetings, traveler. You'll have to log in to continue."
        } else {
            GuildSelector { }
            if (currentGuild != null) {
                EventCalendar {
                    linkedGuild = currentGuild
                }
            }
            EventEditor { }
            TableEditor { }
            PlayerEditor { }
        }
    }
}