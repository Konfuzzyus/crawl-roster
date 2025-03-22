package org.codecranachan.roster.repo

import kotlinx.datetime.toJavaInstant
import kotlinx.datetime.toJavaLocalDate
import kotlinx.datetime.toJavaLocalTime
import kotlinx.datetime.toKotlinInstant
import kotlinx.datetime.toKotlinLocalDate
import kotlinx.datetime.toKotlinLocalTime
import org.codecranachan.roster.core.Audience
import org.codecranachan.roster.core.Event
import org.codecranachan.roster.core.Player
import org.codecranachan.roster.core.Registration
import org.codecranachan.roster.core.Table
import org.codecranachan.roster.core.TableLanguage
import org.codecranachan.roster.jooq.enums.Tableaudience
import org.codecranachan.roster.jooq.enums.Tablelanguage
import org.codecranachan.roster.jooq.tables.records.EventregistrationsRecord
import org.codecranachan.roster.jooq.tables.records.EventsRecord
import org.codecranachan.roster.jooq.tables.records.HostedtablesRecord
import org.codecranachan.roster.jooq.tables.records.PlayersRecord
import java.time.OffsetDateTime
import java.time.ZoneId

internal fun EventregistrationsRecord.asModel(): Registration {
    return Registration(
        eventId!!,
        playerId!!,
        Registration.Metadata(registrationTime!!.toInstant().toKotlinInstant()),
        Registration.Details(dungeonMasterId = dungeonMasterId)
    )
}

internal fun Registration.asRecord(): EventregistrationsRecord {
    return EventregistrationsRecord(
        eventId,
        playerId,
        null,
        OffsetDateTime.ofInstant(meta.registrationDate.toJavaInstant(), ZoneId.systemDefault()),
        details.dungeonMasterId
    )
}

internal fun Event.asRecord(): EventsRecord {
    return EventsRecord(
        id,
        date.toJavaLocalDate(),
        details.time?.toJavaLocalTime(),
        guildId,
        null,
        null,
        details.location
    )
}

internal fun EventsRecord.asModel(): Event {
    return Event(
        id!!,
        guildId!!,
        eventDate!!.toKotlinLocalDate(),
        Event.Details(
            eventTime?.toKotlinLocalTime(),
            location,
            null
        ),
    )
}

internal fun Table.asRecord(): HostedtablesRecord {
    return HostedtablesRecord(
        eventId,
        dungeonMasterId,
        details.adventureTitle,
        details.adventureDescription,
        details.moduleDesignation,
        Tablelanguage.valueOf(details.language.name),
        details.playerRange.first,
        details.playerRange.last,
        details.levelRange.first,
        details.levelRange.last,
        Tableaudience.valueOf(details.audience.name),
        details.gameSystem
    )
}

internal fun HostedtablesRecord.asModel(): Table {
    return Table(
        eventId!!,
        dungeonMasterId!!,
        Table.Details(
            adventureTitle,
            adventureDescription,
            moduleDesignation,
            TableLanguage.valueOf(tableLanguage!!.name),
            IntRange(minPlayers!!, maxPlayers!!),
            IntRange(minCharacterLevel!!, maxCharacterLevel!!),
            Audience.valueOf(audience!!.name),
            gameSystem
        )
    )
}

internal fun PlayersRecord.asModel(): Player {
    return Player(
        id!!,
        discordId!!,
        discordName!!,
        discordAvatar,
        Player.Details(
            playerName,
            Repository.decodeLanguages(languages!!),
            tierPreference!!
        )
    )
}

internal fun Player.asRecord(): PlayersRecord {
    return PlayersRecord(
        id,
        details.name,
        Repository.encodeLanguages(details.languages),
        discordId,
        discordHandle,
        avatarUrl,
        details.playTier
    )
}
