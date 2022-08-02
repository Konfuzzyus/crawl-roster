package org.codecranachan.roster.logic.events

import org.codecranachan.roster.Event
import org.codecranachan.roster.Table

interface RosterEvent

data class CalendarEventCreated(val event: Event) : RosterEvent
data class CalendarEventUpdated(val event: Event) : RosterEvent
data class TableCanceled(val event: Event, val table: Table) : RosterEvent