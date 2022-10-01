package org.codecranachan.roster.util

import java.util.*

/**
 * Converts Java's "Optional" to Kotlin's "?"
 */
fun <T> Optional<T>.orNull(): T? = orElse(null)
