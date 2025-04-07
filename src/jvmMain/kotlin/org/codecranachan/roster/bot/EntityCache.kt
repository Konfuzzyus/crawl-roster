package org.codecranachan.roster.bot

import discord4j.common.util.Snowflake
import discord4j.core.`object`.entity.Entity
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ConcurrentHashMap

class EntityCache {
    data class Key(
        val type: Class<*>,
        val name: String,
    )

    val logger: Logger = LoggerFactory.getLogger(javaClass)
    val entityCache: ConcurrentHashMap<Key, CompletableFuture<out Entity>> = ConcurrentHashMap()
    private val removeActions: ConcurrentHashMap<Snowflake, Runnable> = ConcurrentHashMap()

    fun hasKey(name: String, type: Class<*>): Boolean {
        return entityCache.containsKey(Key(type, name))
    }

    inline fun <reified T : Entity> get(
        name: String,
        factory: (String) -> CompletableFuture<T>,
    ): CompletableFuture<T> {
        val key = Key(T::class.java, name)
        return entityCache.getOrPut(key) {
            logger.debug("Cache miss - creating {}", key)
            factory.invoke(name).withRemoveAction(key)
        }.thenApply {
            T::class.java.cast(it)
        }
    }

    fun put(name: String, entity: Entity) {
        val key = Key(entity.javaClass, name)
        entityCache[key] = CompletableFuture.completedFuture(entity).withRemoveAction(key)
    }

    fun <T : Entity> getAll(clazz: Class<T>): Map<Key, CompletableFuture<T>> {
        return entityCache
            .filter { (key, _) -> key.type == clazz }
            .mapValues { (_, v) -> v.thenApply { clazz.cast(it) } }
    }

    fun <T : Entity> CompletableFuture<T>.withRemoveAction(key: Key): CompletableFuture<T> {
        thenAccept {
            removeActions[it.id] = Runnable {
                entityCache.remove(key)?.apply {
                    logger.debug("Removed {} from tracking", key)
                }
            }
        }
        return this
    }

    fun remove(id: Snowflake) {
        removeActions.remove(id)?.apply {
            logger.debug("Running remove action for {}", id)
            run()
        }
    }
}