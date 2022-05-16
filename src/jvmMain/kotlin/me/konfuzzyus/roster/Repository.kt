package me.konfuzzyus.roster

import me.konfuzzyus.roster.jooq.Tables
import me.konfuzzyus.roster.jooq.tables.records.PlayersRecord
import org.flywaydb.core.Flyway
import org.jooq.DSLContext
import org.jooq.SQLDialect
import org.jooq.impl.DSL
import java.sql.Connection
import java.sql.DriverManager
import java.util.*

private const val databaseUrl = "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1"

class Repository {
    companion object {
        init {
            // Deactivate jooq spam
            System.setProperty("org.jooq.no-tips", "true")
            System.setProperty("org.jooq.no-logo", "true")
        }
    }

    private fun <R> withJooq(block: DSLContext.() -> R): R {
        getConnection().use {
            return block(DSL.using(it, SQLDialect.H2))
        }
    }

    private fun getConnection(): Connection {
        return DriverManager.getConnection(databaseUrl)
    }


    fun migrate() {
        val flyway = Flyway.configure()
            .dataSource(databaseUrl, null, null)
            .schemas("ROSTER")
            .createSchemas(true)
            .load()
        flyway.migrate()
    }

    fun fetchAllPlayers(): List<PlayersRecord> {
        return withJooq {
            selectFrom(Tables.PLAYERS).fetch().toList()
        }
    }

    fun fetchPlayer(id: UUID): PlayersRecord? {
        return withJooq {
            selectFrom(Tables.PLAYERS).where(Tables.PLAYERS.ID.eq(id)).fetchOne()
        }
    }

    fun addPlayer(record: PlayersRecord) {
        withJooq {
            insertInto(Tables.PLAYERS).set(record).execute()
        }
    }
}
