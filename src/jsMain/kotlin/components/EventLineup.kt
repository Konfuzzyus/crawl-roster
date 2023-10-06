package components

import components.events.HostedTableRow
import components.events.RegistrationRow
import components.events.UnseatedPlayersRow
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
                props.result.tables.forEach { (_, table) ->
                    HostedTableRow {
                        me = props.me
                        eventData = props.result
                        tableData = table
                    }
                    props.result.registrations
                        .filter { it.dungeonMaster?.id == table.dungeonMaster.id }
                        .forEach { reg ->
                            RegistrationRow {
                                me = props.me
                                registration = reg
                            }
                        }
                }
                UnseatedPlayersRow {
                    unseatedPlayers = props.result.unseated
                }
                props.result.registrations
                    .filter { it.dungeonMaster?.id == null }
                    .forEach { reg ->
                        RegistrationRow {
                            me = props.me
                            registration = reg
                        }
                    }
            }
        }
    }
}

