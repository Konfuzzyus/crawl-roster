package components

import mui.material.Box
import mui.material.CircularProgress
import mui.material.Table
import mui.material.TableBody
import mui.material.TableContainer
import mui.material.TableHead
import org.codecranachan.roster.LinkedGuild
import react.FC
import react.Props
import react.useContext
import react.useEffectOnce
import react.useState
import reducers.StoreContext

external interface EventCalendarProps : Props {
    var linkedGuild: LinkedGuild
}

val EventCalendar = FC<EventCalendarProps> { props ->
    val store = useContext(StoreContext)
    var events by useState(store.state.calendar.events)
    val account = store.state.identity.player

    useEffectOnce {
        val unsubscribe = store.subscribe { events = store.state.calendar.events }
        cleanup(unsubscribe)
    }

    Box {
        if (events == null) {
            CircularProgress {}
        } else {
            TableContainer {
                Table {
                    stickyHeader = true
                    TableHead {
                        EventCalendarHeaderRow {}
                    }
                    TableBody {
                        events!!.forEach {
                            EventCalendarBodyRow {
                                event = it
                            }
                        }
                    }
                }
            }
            if (account?.isAdminOf(props.linkedGuild.id) == true) {
                SubmitEvent {
                    linkedGuild = props.linkedGuild
                }
            }
        }
    }
}
