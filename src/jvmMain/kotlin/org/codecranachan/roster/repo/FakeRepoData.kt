package org.codecranachan.roster.repo

import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import kotlinx.datetime.todayIn
import org.codecranachan.roster.DiscordUser
import org.codecranachan.roster.Event
import org.codecranachan.roster.EventRegistration
import org.codecranachan.roster.Guild
import org.codecranachan.roster.Player
import org.codecranachan.roster.TableHosting

class FakeRepoData(val repo: Repository) {

    fun insert() {
        // Make a guild
        val guilds = (0..0).map {
            val g = makeGuild(it)
            repo.addLinkedGuild(g)
            g
        }
        // Make some events
        val events = (0..2).map {
            val e = makeEvent(it, guilds[0])
            repo.addEvent(e)
            e
        }
        // Make some players
        val players = (0..8).map {
            repo.addPlayer(DiscordUser("DiscordId$it", "DiscordHandle$it"))
        }
        // Make some tables
        val tables = (0..3).map {
            try {
                val h = makeTableHosting(it, events[it % events.size], players[it % players.size])
                repo.addHostedTable(h)
                h
            } catch (e: Exception) {
                null
            }
        }.filterNotNull()
        // Make some event registrations
        val registrations = (0..7).map {
            try {
                val r = makeEventRegistration(it, events[(it + 1) % events.size], players[it % players.size])
                repo.addEventRegistration(r)
                r
            } catch (e: Exception) {
                null
            }
        }
    }

    private fun makeGuild(i: Int): Guild {
        return Guild(name = "GuildName$i", discordId = "DiscordId$i")
    }

    private fun makeEvent(i: Int, g: Guild): Event {
        return Event(guildId = g.id, date = Clock.System.todayIn(TimeZone.UTC).plus(i, DateTimeUnit.DAY))
    }

    private fun makeTableHosting(i: Int, e: Event, dm: Player): TableHosting {
        return TableHosting(eventId = e.id, dungeonMasterId = dm.id)
    }

    private fun makeEventRegistration(i: Int, e: Event, p: Player): EventRegistration {
        return EventRegistration(eventId = e.id, playerId = p.id)
    }
}