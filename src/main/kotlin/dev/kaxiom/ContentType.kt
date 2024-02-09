package dev.kaxiom

enum class ContentType(
    val type: String
) {
    JSON("application/json"),
    NDJSON("application/x-ndjson"),
    CSV("text/csv")
}