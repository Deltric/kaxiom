package dev.kaxiom

/**
 * Represents the encoding of a payload.
 */
enum class ContentEncoding(
    val encoding: String
) {
    IDENTITY("1"),
    GZIP("gzip"),
}