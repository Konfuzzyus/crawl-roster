package org.codecranachan.roster.core

import assertk.assertThat
import assertk.assertions.contains
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import assertk.assertions.isNull
import com.benasher44.uuid.Uuid
import org.codecranachan.roster.DiscordUser
import org.codecranachan.roster.core.events.EventBus
import org.codecranachan.roster.core.events.PlayerCreated
import org.codecranachan.roster.repo.Repository
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class PlayerRosterLogicTest {
    private val repository = Repository("jdbc:h2:mem:test")
    private val eventBus = EventBus()
    private val logic = PlayerRosterLogic(repository, eventBus)

    @BeforeEach
    fun setUp() {
        repository.reset()
    }

    @Test
    fun `fetching nonexistent player returns null`() {
        assertThat(logic.getPlayer(Uuid.randomUUID())).isNull()
    }

    @Test
    fun `register nonexisting discord user returns new player`() {
        val user = DiscordUser(
            id = "discordId",
            username = "discordUsername",
            avatar = "discordAvatar",
        )

        lateinit var player: Player
        val captured = eventBus.capture {
            player = logic.registerDiscordPlayer(user).player
        }

        assertThat(player.discordId).isEqualTo(user.id)
        assertThat(player.discordHandle).isEqualTo(user.username)
        assertThat(player.avatarUrl).isEqualTo(user.getAvatarUrl())
        assertThat(captured).contains(PlayerCreated(player))
    }

    @Test
    fun `register existing discord user returns existing player`() {
        val user = DiscordUser(
            id = "discordId",
            username = "discordUsername",
            avatar = "discordAvatar",
        )
        val originalPlayer = logic.registerDiscordPlayer(user).player

        lateinit var existingPlayer: Player
        val captured = eventBus.capture {
            existingPlayer = logic.registerDiscordPlayer(user).player
        }

        assertThat(existingPlayer.id).isEqualTo(originalPlayer.id)
        assertThat(captured).isEmpty()
    }
}