package org.codecranachan.roster.api

import com.benasher44.uuid.Uuid
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sessions.*
import org.codecranachan.roster.Event
import org.codecranachan.roster.EventDetails
import org.codecranachan.roster.EventRegistration
import org.codecranachan.roster.logic.RosterCore
import org.codecranachan.roster.TableHosting
import org.codecranachan.roster.UserSession

class EventApi(private val core: RosterCore) {

    fun install(r: Route) {
        with(r) {
            get("/api/v1/events/{id}") {
                val id = Uuid.fromString(call.parameters["id"])
                val event = core.eventCalendar.getEvent(id)
                if (event == null) {
                    call.respond(HttpStatusCode.NotFound)
                } else {
                    call.respond(event)
                }
            }

            post("/api/v1/events") {
                val userSession = call.sessions.get<UserSession>()
                if (userSession == null) {
                    call.respond(HttpStatusCode.Unauthorized, "Not logged in")
                } else {
                    val event = call.receive<Event>()
                    if (core.playerRoster.isGuildAdmin(userSession.playerId, event.guildId)) {
                        core.eventCalendar.addEvent(event.guildId, event)
                        call.respond(HttpStatusCode.Created)
                    } else {
                        call.respond(HttpStatusCode.Forbidden, "Only guild admins can create events")
                    }
                }
            }

            patch("/api/v1/events/{evtId}") {
                val userSession = call.sessions.get<UserSession>()
                if (userSession == null) {
                    call.respond(HttpStatusCode.Unauthorized, "Not logged in")
                } else {
                    val evtId = Uuid.fromString(call.parameters["evtId"])
                    val event = core.eventCalendar.getEvent(evtId)
                    if (event == null) {
                        call.respond(HttpStatusCode.NotFound)
                    } else {
                        if (core.playerRoster.isGuildAdmin(userSession.playerId, event.guildId)) {
                            val details = call.receive<EventDetails>()
                            core.eventCalendar.updateEvent(evtId, details)
                            call.respond(HttpStatusCode.Created)
                        } else {
                            call.respond(HttpStatusCode.Forbidden, "Only guild admins can update events")
                        }
                    }
                }
            }

            post("/api/v1/events/{evtId}/registrations") {
                val userSession = call.sessions.get<UserSession>()
                if (userSession == null) {
                    call.respond(HttpStatusCode.Unauthorized, "Not logged in")
                } else {
                    val evtId = Uuid.fromString(call.parameters["evtId"])
                    val reg = call.receive<EventRegistration>()
                    core.eventCalendar.registerPlayer(evtId, userSession.playerId, reg.tableId)
                    call.respond(HttpStatusCode.OK)
                }
            }

            patch("/api/v1/events/{evtId}/registrations/{plrId}") {
                val userSession = call.sessions.get<UserSession>()
                if (userSession == null) {
                    call.respond(HttpStatusCode.Unauthorized, "Not logged in")
                } else {
                    val evtId = Uuid.fromString(call.parameters["evtId"])
                    val plrId = Uuid.fromString(call.parameters["plrId"])
                    val reg = call.receive<EventRegistration>()
                    if (plrId == userSession.playerId) {
                        core.eventCalendar.updatePlayerRegistration(evtId, userSession.playerId, reg.tableId)
                        call.respond(HttpStatusCode.OK)
                    } else {
                        call.respond(HttpStatusCode.Forbidden, "You can only edit your own registrations")
                    }
                }
            }

            delete("/api/v1/events/{evtId}/registrations/{plrId}") {
                val userSession = call.sessions.get<UserSession>()
                if (userSession == null) {
                    call.respond(HttpStatusCode.Unauthorized, "Not logged in")
                } else {
                    val evtId = Uuid.fromString(call.parameters["evtId"])
                    val plrId = Uuid.fromString(call.parameters["plrId"])
                    if (plrId == userSession.playerId) {
                        core.eventCalendar.unregisterPlayer(evtId, plrId)
                        call.respond(HttpStatusCode.OK)
                    } else {
                        call.respond(HttpStatusCode.Forbidden, "You can only remove yourself from an event")
                    }
                }
            }


            post("/api/v1/events/{evtId}/tables") {
                val userSession = call.sessions.get<UserSession>()
                if (userSession == null) {
                    call.respond(HttpStatusCode.Unauthorized, "Not logged in")
                } else {
                    val evtId = Uuid.fromString(call.parameters["evtId"])
                    val tbl = call.receive<TableHosting>()
                    if (tbl.dungeonMasterId == userSession.playerId) {
                        core.eventCalendar.hostTable(TableHosting(tbl.id, evtId, tbl.dungeonMasterId))
                        call.respond(HttpStatusCode.OK)
                    } else {
                        call.respond(HttpStatusCode.Forbidden, "You can only sign yourself up to DM")
                    }

                }
            }

            delete("/api/v1/events/{evtId}/tables/{dmId}") {
                val userSession = call.sessions.get<UserSession>()
                if (userSession == null) {
                    call.respond(HttpStatusCode.Unauthorized)
                } else {
                    val evtId = Uuid.fromString(call.parameters["evtId"])
                    val dmId = Uuid.fromString(call.parameters["dmId"])
                    if (dmId == userSession.playerId) {
                        core.eventCalendar.cancelTable(evtId, dmId)
                        call.respond(HttpStatusCode.OK)
                    } else {
                        call.respond(HttpStatusCode.Forbidden, "Only the DM can cancel a table")
                    }
                }
            }

        }

    }
}