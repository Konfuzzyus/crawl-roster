package me.konfuzzyus.crawlroster

import me.konfuzzyus.crawlroster.jooq.Tables
import me.konfuzzyus.crawlroster.jooq.tables.records.HeroRecord
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
            .load()
        flyway.migrate()
    }

    fun addHero(hero: HeroRecord) {
        withJooq {
            attach(hero)
            hero.store()
        }
    }

    fun getHeroes(): List<HeroRecord> {
        return withJooq {
            selectFrom(Tables.HERO).fetch().toList()
        }
    }
}