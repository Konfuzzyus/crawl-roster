package org.codecranachan.roster.testkit

import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.plus
import org.codecranachan.roster.LinkedGuild
import org.codecranachan.roster.core.Event
import org.codecranachan.roster.core.Player
import org.codecranachan.roster.core.Registration
import org.codecranachan.roster.core.Table
import org.codecranachan.roster.repo.Repository
import java.util.function.Supplier

class EntityGenerator {
    private var entityCounter = 0

    companion object {
        inline fun <reified T : Any> makeMany(amount: Int, supplier: Supplier<T>): Array<T> {
            return (1..amount).map { supplier.get() }.toTypedArray()
        }

        fun insertEntities(repo: Repository, vararg items: Any) {
            items.forEach {
                when (it) {
                    is LinkedGuild -> repo.guildRepository.addLinkedGuild(it)
                    is Player -> repo.playerRepository.addPlayer(it)
                    is Event -> repo.eventRepository.addEvent(it)
                    is Registration -> repo.eventRepository.addRegistration(it)
                    is Table -> repo.eventRepository.addTable(it)
                    else -> throw java.lang.IllegalArgumentException("Unknown object $it.javaClass")
                }
            }
        }
    }

    fun makeGuild(): LinkedGuild {
        val nr = ++entityCounter
        return LinkedGuild(
            name = "name#$nr",
            discordId = "discordId#$nr"
        )
    }

    fun makePlayer(): Player {
        val nr = ++entityCounter
        return Player(
            discordId = "discordId#$nr",
            discordHandle = "discordHandle#$nr"
        )
    }

    fun makeEvent(guild: LinkedGuild, date: LocalDate = LocalDate.parse("2022-06-06")): Event {
        val nr = ++entityCounter
        return Event(
            guildId = guild.id,
            date = LocalDate(2022,1,1).plus(nr, DateTimeUnit.DAY)
        )
    }

    fun makeTable(event: Event, dm: Player): Table {
        return Table(
            eventId = event.id,
            dungeonMasterId = dm.id
        )
    }

    fun makeRegistration(event: Event, player: Player, dm: Player?): Registration {
        return Registration(
            event.id,
            player.id,
            details = Registration.Details(dungeonMasterId = dm?.id)
        )
    }

}