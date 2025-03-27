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
import react.use
import react.useEffectOnceWithCleanup
import react.useState
import reducers.StoreContext

external interface EventCalendarProps : Props {
    var linkedGuild: LinkedGuild
}

val EventCalendar = FC<EventCalendarProps> { props ->
    val store = use(StoreContext)!!
    var events by useState(store.state.calendar.events)
    val account = store.state.identity.player

    useEffectOnceWithCleanup {
        val unsubscribe = store.subscribe { events = store.state.calendar.events }
        onCleanup(unsubscribe)
    }

    Box {
        CalendarSpanSelector {}
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
                                result = it
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
