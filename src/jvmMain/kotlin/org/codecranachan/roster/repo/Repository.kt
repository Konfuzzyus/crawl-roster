package org.codecranachan.roster.repo

import Configuration
import org.flywaydb.core.Flyway
import org.jooq.DSLContext
import org.jooq.SQLDialect
import org.jooq.impl.DSL
import java.sql.Connection
import java.sql.DriverManager

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
        return DriverManager.getConnection(Configuration.jdbcUri)
    }

    fun migrate() {
        val flyway = Flyway.configure()
            .dataSource(Configuration.jdbcUri, null, null)
            .schemas("ROSTER")
            .createSchemas(true)
            .load()
        flyway.migrate()
    }
}
