package components

import components.events.HostedTableRow
import components.events.RegistrationRow
import mui.material.Size
import mui.material.Table
import mui.material.TableBody
import mui.material.TableContainer
import org.codecranachan.roster.core.Player
import org.codecranachan.roster.query.EventQueryResult
import react.FC
import react.Props

external interface EventLineupProps : Props {
    var result: EventQueryResult
    var me: Player
}

val EventLineup = FC<EventLineupProps> { props ->
    TableContainer {
        Table {
            size = Size.small
            TableBody {
                props.result.tables.forEach { session ->
                    HostedTableRow {
                        me = props.me
                        eventData = props.result
                        tableData = session.value
                    }
                }
            }
        }
        Table {
            size = Size.small
            TableBody {
                props.result.registrations.forEach { reg ->
                    RegistrationRow {
                        me = props.me
                        registration = reg
                    }
                }
            }
        }
    }
}

