package org.codecranachan.roster.bot

import discord4j.common.util.Snowflake
import discord4j.core.`object`.entity.Entity
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ConcurrentHashMap

class EntityCache {
    data class Key(
        val type: Class<*>,
        val name: String
    )

    val entityCache: ConcurrentHashMap<Key, CompletableFuture<out Entity>> = ConcurrentHashMap()
    private val removeActions: ConcurrentHashMap<Snowflake, Runnable> = ConcurrentHashMap()

    inline fun <reified T : Entity> get(
        name: String,
        factory: (String) -> CompletableFuture<T>
    ): CompletableFuture<T> {
        val key = Key(T::class.java, name)
        return entityCache.getOrPut(key) {
            factory.invoke(name).withRemoveAction(key)
        }.thenApply {
            T::class.java.cast(it)
        }
    }

    fun put(name: String, entity: Entity) {
        val key = Key(entity.javaClass, name)
        entityCache[key] = CompletableFuture.completedFuture(entity).withRemoveAction(key)
    }

    fun <T : Entity> CompletableFuture<T>.withRemoveAction(key: Key): CompletableFuture<T> {
        thenAccept { removeActions[it.id] = Runnable { entityCache.remove(key) } }
        return this
    }

    fun remove(id: Snowflake) {
        removeActions.remove(id)?.run()
    }
}