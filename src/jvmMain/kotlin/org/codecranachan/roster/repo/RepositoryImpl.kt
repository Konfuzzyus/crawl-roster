package org.codecranachan.roster.repo

import org.codecranachan.roster.core.TableLanguage
import org.flywaydb.core.Flyway
import org.h2.jdbcx.JdbcConnectionPool
import org.jooq.DSLContext
import org.jooq.SQLDialect
import org.jooq.impl.DSL

class Repository(jdbcUri: String) {
    val guildRepository = GuildRepository(this)
    val eventRepository = EventRepository(this)
    val playerRepository = PlayerRepository(this)

    private val dataSource = JdbcConnectionPool.create(jdbcUri, null, null)
    private val dsl = DSL.using(dataSource, SQLDialect.H2)

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

    fun <R> withJooq(action: DSLContext.() -> R): R {
        return action(dsl)
    }

    fun migrate() {
        val flyway = baseFlyway().load()
        flyway.migrate()
    }

    fun reset(hard: Boolean = false) {
        val flyway = baseFlyway()
            .cleanDisabled(false)
            .cleanOnValidationError(true)
            .load()
        if (hard) flyway.clean()
        flyway.migrate()
    }

    private fun baseFlyway() = Flyway.configure()
        .dataSource(dataSource)
        .schemas("ROSTER")
        .createSchemas(true)

}
