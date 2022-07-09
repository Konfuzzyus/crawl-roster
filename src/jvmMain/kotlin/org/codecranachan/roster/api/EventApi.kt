package org.codecranachan.roster.api

import com.benasher44.uuid.Uuid
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sessions.*
import org.codecranachan.roster.Event
import org.codecranachan.roster.EventRegistration
import org.codecranachan.roster.TableHosting
import org.codecranachan.roster.UserSession
import org.codecranachan.roster.repo.Repository
import org.codecranachan.roster.repo.addEvent
import org.codecranachan.roster.repo.addEventRegistration
import org.codecranachan.roster.repo.addHostedTable
import org.codecranachan.roster.repo.fetchEvent
import org.codecranachan.roster.repo.fetchGuild
import org.codecranachan.roster.repo.isHostingForEvent
import org.codecranachan.roster.repo.isRegisteredForEvent
import org.codecranachan.roster.repo.removeEventRegistration
import org.codecranachan.roster.repo.removeHostedTable
import org.codecranachan.roster.repo.updateEventRegistration

class EventApi(private val repository: Repository) {
    private val permissions = Permissions(repository)

    fun install(r: Route) {
        with(r) {
            get("/api/v1/events/{id}") {
                val id = Uuid.fromString(call.parameters["id"])
                val event = repository.fetchEvent(id)
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
                    val guild = repository.fetchGuild(event.guildId)
                    if (guild?.let { permissions.hasAdminRightsForGuild(userSession.playerId, it) } == true) {
                        repository.addEvent(event)
                        call.respond(HttpStatusCode.Created)
                    } else {
                        call.respond(HttpStatusCode.Forbidden, "Only guild admins can create events")
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
                    if (reg.playerId == userSession.playerId) {
                        if (repository.isHostingForEvent(reg.playerId, evtId)) {
                            call.respond(HttpStatusCode.Conflict, "Player is already hosting a table")
                        } else {
                            repository.addEventRegistration(EventRegistration(reg.id, evtId, reg.playerId, reg.tableId))
                            call.respond(HttpStatusCode.Created)
                        }
                    } else {
                        call.respond(HttpStatusCode.Forbidden, "You can sign up yourself")
                    }
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
                        repository.updateEventRegistration(evtId, plrId, reg.tableId)
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
                        repository.removeEventRegistration(evtId, plrId)
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
                        if (repository.isRegisteredForEvent(tbl.dungeonMasterId, evtId)) {
                            call.respond(HttpStatusCode.Conflict, "Player is already playing a character")
                        } else {
                            repository.addHostedTable(TableHosting(tbl.id, evtId, tbl.dungeonMasterId))
                            call.respond(HttpStatusCode.Created)
                        }
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
                        repository.removeHostedTable(evtId, dmId)
                        call.respond(HttpStatusCode.OK)
                    } else {
                        call.respond(HttpStatusCode.Forbidden, "Only the DM can cancel a table")
                    }
                }
            }

        }

    }
}