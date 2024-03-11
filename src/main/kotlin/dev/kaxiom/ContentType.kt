package dev.kaxiom

/**
 * Represents the content type of injest payload.
 * @param type - The content type.
 */
enum class ContentType(
    val type: String
) {
    JSON("application/json"),
    NDJSON("application/x-ndjson"),
    CSV("text/csv")
}