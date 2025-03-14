package org.codecranachan.roster.api

import com.benasher44.uuid.Uuid
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sessions.*
import org.codecranachan.roster.UserSession
import org.codecranachan.roster.core.Event
import org.codecranachan.roster.core.Registration
import org.codecranachan.roster.core.RosterCore
import org.codecranachan.roster.core.Table

class EventApi(private val core: RosterCore) {

    fun install(r: Route) {
        with(r) {
            get("/api/v1/events/{evtId}") {
                val id = Uuid.fromString(call.parameters["evtId"])
                val event = core.eventCalendar.queryEvent(id)
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
                        core.eventCalendar.addEvent(event)
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
                    val query = core.eventCalendar.queryEvent(evtId)
                    if (query == null) {
                        call.respond(HttpStatusCode.NotFound)
                    } else {
                        if (core.playerRoster.isGuildAdmin(userSession.playerId, query.event.guildId)) {
                            val details = call.receive<Event.Details>()
                            core.eventCalendar.updateEvent(evtId, details)
                            call.respond(HttpStatusCode.Created)
                        } else {
                            call.respond(HttpStatusCode.Forbidden, "Only guild admins can update events")
                        }
                    }
                }
            }

            delete("/api/v1/events/{evtId}") {
                val userSession = call.sessions.get<UserSession>()
                if (userSession == null) {
                    call.respond(HttpStatusCode.Unauthorized, "Not logged in")
                } else {
                    val evtId = Uuid.fromString(call.parameters["evtId"])
                    val query = core.eventCalendar.queryEvent(evtId)
                    if (query == null) {
                        call.respond(HttpStatusCode.NotFound)
                    } else {
                        if (core.playerRoster.isGuildAdmin(userSession.playerId, query.event.guildId)) {
                            core.eventCalendar.cancelEvent(evtId)
                            call.respond(HttpStatusCode.OK)
                        } else {
                            call.respond(HttpStatusCode.Forbidden, "Only guild admins can cancel events")
                        }
                    }
                }
            }

            // player registrations

            put("/api/v1/events/{evtId}/players/{plrId}") {
                val userSession = call.sessions.get<UserSession>()
                if (userSession == null) {
                    call.respond(HttpStatusCode.Unauthorized, "Not logged in")
                } else {
                    val evtId = Uuid.fromString(call.parameters["evtId"])
                    val plrId = Uuid.fromString(call.parameters["plrId"])
                    val reg = call.receive<Registration.Details>()
                    if (plrId == userSession.playerId) {
                        core.eventCalendar.addPlayerRegistration(evtId, plrId, reg)
                        call.respond(HttpStatusCode.OK)
                    } else {
                        call.respond(HttpStatusCode.Forbidden, "You can only create your own registration")
                    }
                }
            }

            patch("/api/v1/events/{evtId}/players/{plrId}") {
                val userSession = call.sessions.get<UserSession>()
                if (userSession == null) {
                    call.respond(HttpStatusCode.Unauthorized, "Not logged in")
                } else {
                    val evtId = Uuid.fromString(call.parameters["evtId"])
                    val plrId = Uuid.fromString(call.parameters["plrId"])
                    val reg = call.receive<Registration.Details>()

                    val current = core.eventCalendar.getPlayerRegistration(evtId, plrId)
                    val isKickByDm =
                        reg.dungeonMasterId == null && current?.details?.dungeonMasterId == userSession.playerId
                    val isInviteByDm =
                        reg.dungeonMasterId == userSession.playerId && current?.details?.dungeonMasterId == null

                    if (plrId == userSession.playerId || isKickByDm || isInviteByDm) {
                        core.eventCalendar.updatePlayerRegistration(evtId, plrId, reg)
                        call.respond(HttpStatusCode.OK)
                    } else {
                        call.respond(HttpStatusCode.Forbidden, "Only you or your DM cam edit your registration")
                    }
                }
            }

            delete("/api/v1/events/{evtId}/players/{plrId}") {
                val userSession = call.sessions.get<UserSession>()
                if (userSession == null) {
                    call.respond(HttpStatusCode.Unauthorized, "Not logged in")
                } else {
                    val evtId = Uuid.fromString(call.parameters["evtId"])
                    val plrId = Uuid.fromString(call.parameters["plrId"])
                    if (plrId == userSession.playerId) {
                        core.eventCalendar.deletePlayerRegistration(evtId, plrId)
                        call.respond(HttpStatusCode.OK)
                    } else {
                        call.respond(HttpStatusCode.Forbidden, "You can only cancel your own registration")
                    }
                }
            }

            // dm registrations

            put("/api/v1/events/{evtId}/dms/{dmId}") {
                val userSession = call.sessions.get<UserSession>()
                if (userSession == null) {
                    call.respond(HttpStatusCode.Unauthorized, "Not logged in")
                } else {
                    val evtId = Uuid.fromString(call.parameters["evtId"])
                    val dmId = Uuid.fromString(call.parameters["dmId"])
                    val tbl = call.receive<Table.Details>()
                    if (dmId == userSession.playerId) {
                        core.eventCalendar.addDmRegistration(evtId, dmId, tbl)
                        call.respond(HttpStatusCode.OK)
                    } else {
                        call.respond(HttpStatusCode.Forbidden, "You can only create your own registration")
                    }

                }
            }

            patch("/api/v1/events/{evtId}/dms/{dmId}") {
                val userSession = call.sessions.get<UserSession>()
                if (userSession == null) {
                    call.respond(HttpStatusCode.Unauthorized, "Not logged in")
                } else {
                    val evtId = Uuid.fromString(call.parameters["evtId"])
                    val dmId = Uuid.fromString(call.parameters["dmId"])
                    val tbl = call.receive<Table.Details>()
                    if (dmId == userSession.playerId) {
                        core.eventCalendar.updateDmRegistration(evtId, dmId, tbl)
                        call.respond(HttpStatusCode.OK)
                    } else {
                        call.respond(HttpStatusCode.Forbidden, "You can only edit your own registration")
                    }
                }
            }

            delete("/api/v1/events/{evtId}/dms/{dmId}") {
                val userSession = call.sessions.get<UserSession>()
                if (userSession == null) {
                    call.respond(HttpStatusCode.Unauthorized)
                } else {
                    val evtId = Uuid.fromString(call.parameters["evtId"])
                    val dmId = Uuid.fromString(call.parameters["dmId"])
                    if (dmId == userSession.playerId) {
                        core.eventCalendar.cancelDmRegistration(evtId, dmId)
                        call.respond(HttpStatusCode.OK)
                    } else {
                        call.respond(HttpStatusCode.Forbidden, "You can only cancel your own registration")
                    }
                }
            }

        }

    }
}