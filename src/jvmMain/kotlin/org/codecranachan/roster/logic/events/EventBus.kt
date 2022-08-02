package org.codecranachan.roster.logic.events

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import reactor.core.publisher.Flux
import reactor.core.publisher.Sinks


class EventBus {
    private val logger: Logger = LoggerFactory.getLogger(javaClass)
    private val sink: Sinks.Many<RosterEvent> = Sinks.many().multicast().directAllOrNothing()

    fun publish(event: RosterEvent) {
        logger.info("Publishing $event")
        when (val r = sink.tryEmitNext(event)) {
            Sinks.EmitResult.OK -> {}
            Sinks.EmitResult.FAIL_ZERO_SUBSCRIBER -> logger.debug("No subscribers found")
            else -> logger.warn("Failed to publish $event - $r")
        }
    }

    fun getFlux(): Flux<RosterEvent> = sink.asFlux()
}