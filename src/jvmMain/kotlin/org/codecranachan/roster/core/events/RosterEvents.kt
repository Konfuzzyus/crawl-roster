package org.codecranachan.roster.core.events

import org.codecranachan.roster.core.Event
import org.codecranachan.roster.core.Player
import org.codecranachan.roster.core.Registration
import org.codecranachan.roster.core.Table
import org.codecranachan.roster.query.EventQueryResult
import org.codecranachan.roster.query.TableQueryResult

interface RosterEvent
data class PlayerCreated(val current: Player) : RosterEvent

data class CalendarEventCreated(val current: Event) : RosterEvent
data class CalendarEventUpdated(val previous: Event, val current: Event) : RosterEvent
data class CalendarEventCanceled(val previous: Event) : RosterEvent
data class CalendarEventClosed(val current: Event) : RosterEvent

data class RegistrationCreated(val current: Registration): RosterEvent
data class RegistrationUpdated(val previous: Registration, val current: Registration): RosterEvent
data class RegistrationCanceled(val previous: Registration): RosterEvent

data class TableCreated(val current: Table) : RosterEvent
data class TableUpdated(val previous: Table, val current: Table) : RosterEvent
data class TableCanceled(val previous: Table) : RosterEvent