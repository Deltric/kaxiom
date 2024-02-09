package dev.kaxiom.injest

import dev.kaxiom.ContentEncoding
import dev.kaxiom.ContentType

/**
 * Injest request.
 */
data class Injest(
    val dataset: String,
    val type: ContentType,
    val encoding: ContentEncoding,
    val payload: String
)