package org.codecranachan.roster.core.events

import org.codecranachan.roster.core.Event
import org.codecranachan.roster.core.Player
import org.codecranachan.roster.core.Registration
import org.codecranachan.roster.core.Table

interface RosterEvent
data class PlayerCreated(val player: Player) : RosterEvent

data class CalendarEventCreated(val event: Event) : RosterEvent
data class CalendarEventUpdated(val event: Event) : RosterEvent
data class CalendarEventCanceled(val event: Event) : RosterEvent
data class CalendarEventClosed(val event: Event) : RosterEvent

data class RegistrationCreated(val registration: Registration): RosterEvent
data class RegistrationUpdated(val registration: Registration): RosterEvent
data class RegistrationCanceled(val registration: Registration): RosterEvent

data class TableCreated(val table: Table) : RosterEvent
data class TableUpdated(val table: Table) : RosterEvent
data class TableCanceled(val table: Table) : RosterEvent