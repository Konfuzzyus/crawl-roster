package org.codecranachan.roster.core.events

import org.codecranachan.roster.core.Event
import org.codecranachan.roster.core.Player
import org.codecranachan.roster.core.Registration
import org.codecranachan.roster.core.Table
import org.codecranachan.roster.query.EventQueryResult
import org.codecranachan.roster.query.TableQueryResult

interface RosterEvent
data class PlayerCreated(val player: Player) : RosterEvent

data class CalendarEventCreated(val event: EventQueryResult) : RosterEvent
data class CalendarEventUpdated(val event: EventQueryResult) : RosterEvent
data class CalendarEventCanceled(val event: EventQueryResult) : RosterEvent
data class CalendarEventClosed(val event: EventQueryResult) : RosterEvent

data class RegistrationCreated(val registration: Registration): RosterEvent
data class RegistrationUpdated(val registration: Registration): RosterEvent
data class RegistrationCanceled(val registration: Registration): RosterEvent

data class TableCreated(val table: TableQueryResult) : RosterEvent
data class TableUpdated(val table: TableQueryResult) : RosterEvent
data class TableCanceled(val table: TableQueryResult) : RosterEvent