package components

import components.editors.EventEditor
import components.editors.PlayerEditor
import components.editors.TableEditor
import mui.material.Box
import mui.material.Paper
import mui.material.Tab
import mui.material.Tabs
import react.FC
import react.Props
import react.ReactNode
import react.use
import react.useEffectOnceWithCleanup
import react.useState
import reducers.StoreContext

val RosterWidget = FC<Props> {
    val myStore = use(StoreContext)!!
    val (userIdentity, setUserIdentity) = useState(myStore.state.identity.player)
    val (currentGuild, setCurrentGuild) = useState(myStore.state.calendar.selectedLinkedGuild)
    val (tabState, setTabState) = useState(0)

    useEffectOnceWithCleanup {
        val unsubscribe = myStore.subscribe {
            setUserIdentity(myStore.state.identity.player)
            setCurrentGuild(myStore.state.calendar.selectedLinkedGuild)
        }
        onCleanup(unsubscribe)
    }
    Paper {
        elevation = 0
        if (userIdentity == null) {
            +"Greetings, traveler. You'll have to log in to continue."
        } else {
            GuildSelector { }
            if (currentGuild != null) {
                CalendarSpanSelector {}
                Box {
                    Tabs {
                        value = tabState
                        onChange = { _, value -> setTabState(value as Int) }
                        Tab {
                            label = ReactNode("Calendar")
                        }
                        Tab {
                            label = ReactNode("Statistics")
                        }
                    }
                }
                Box {
                    when (tabState) {
                        0 ->
                            EventCalendar {
                                linkedGuild = currentGuild
                            }

                        else ->
                            GuildStatistics { }
                    }
                }
            }
            EventEditor { }
            TableEditor { }
            PlayerEditor { }
        }
    }
}