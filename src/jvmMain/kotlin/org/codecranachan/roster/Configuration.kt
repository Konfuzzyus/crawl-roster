package org.codecranachan.roster

import io.ktor.server.sessions.*
import io.ktor.util.*

object Configuration {
    private val env = System.getenv()

    val guildLimit = (env["ROSTER_GUILD_LIMIT"] ?: "3").toInt()
    val devMode = env["ROSTER_DEV_MODE"] == "true"
    val rootUrl = env["ROSTER_ROOT_URL"] ?: "http://localhost:8080"
    val jdbcUri = env["ROSTER_JDBC_URI"] ?: "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1"
    val botToken = env["DISCORD_BOT_TOKEN"]
    val discordCredentials = getClientCreds("DISCORD")

    private val sessionEncryptKey = env["ROSTER_SESSION_ENCRYPT_KEY"] ?: "79EBA26C10A7A6D8B22864DB05987369"
    private val sessionSignKey = env["ROSTER_SESSION_SIGN_KEY"] ?: "2FFBBF335042E92C09B988DF4BC040A5"
    val sessionTransformer = SessionTransportTransformerEncrypt(hex(sessionEncryptKey), hex(sessionSignKey))


    private fun getClientCreds(prefix: String): ClientCredentials? {
        val id = env["${prefix}_CLIENT_ID"]
        val secret = env["${prefix}_CLIENT_SECRET"]
        return if (id != null && secret != null) {
            ClientCredentials(id, secret)
        } else {
            null
        }
    }
}