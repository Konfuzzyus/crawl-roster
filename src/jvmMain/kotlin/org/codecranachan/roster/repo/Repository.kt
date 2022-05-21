package org.codecranachan.roster.repo

import org.flywaydb.core.Flyway
import org.jooq.DSLContext
import org.jooq.SQLDialect
import org.jooq.impl.DSL
import java.sql.Connection
import java.sql.DriverManager

private const val databaseUrl = "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1"

class Repository {
    companion object {
        init {
            // Deactivate jooq spam
            System.setProperty("org.jooq.no-tips", "true")
            System.setProperty("org.jooq.no-logo", "true")
        }
    }

    fun <R> withJooq(block: DSLContext.() -> R): R {
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
}
