package org.codecranachan.roster.repo

import org.codecranachan.roster.Configuration
import org.codecranachan.roster.TableLanguage
import org.flywaydb.core.Flyway
import org.jooq.DSLContext
import org.jooq.SQLDialect
import org.jooq.impl.DSL
import java.sql.Connection
import java.sql.DriverManager

class Repository {
    val guildRepository = GuildRepositoryImpl(this)
    val eventRepository = EventRepositoryImpl(this)
    val playerRepository = PlayerRepositoryImpl(this)

    companion object {
        init {
            // Deactivate jooq spam
            System.setProperty("org.jooq.no-tips", "true")
            System.setProperty("org.jooq.no-logo", "true")
        }

        fun encodeLanguages(languages: List<TableLanguage>): String {
            return languages.joinToString(",") { it.short }
        }

        fun decodeLanguages(text: String): List<TableLanguage> {
            return text.split(",").mapNotNull { TableLanguage.ofShort(it) }
        }
    }

    fun <R> withJooq(block: DSLContext.() -> R): R {
        getConnection().use {
            return block(DSL.using(it, SQLDialect.H2))
        }
    }

    fun migrate() {
        val flyway = baseFlyway().load()
        flyway.migrate()
    }

    fun reset() {
        val flyway = baseFlyway()
            .cleanDisabled(false)
            .cleanOnValidationError(true)
            .load()
        flyway.migrate()
    }

    private fun getConnection(): Connection {
        return DriverManager.getConnection(Configuration.jdbcUri)
    }

    private fun baseFlyway() = Flyway.configure()
        .dataSource(Configuration.jdbcUri, null, null)
        .schemas("ROSTER")
        .createSchemas(true)

}
