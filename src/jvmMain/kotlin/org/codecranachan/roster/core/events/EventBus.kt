package org.codecranachan.roster.core.events

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

    /**
     * Returns the events that have been published during the exection of the given block.
     * Intended to be used for testing purposes.
     */
    fun capture(action: () -> Unit): List<RosterEvent> {
        val capturedEvents = mutableListOf<RosterEvent>()
        val connected = getFlux().subscribe(capturedEvents::add)
        action()
        connected.dispose()
        return capturedEvents
    }
}