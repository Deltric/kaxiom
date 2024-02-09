package dev.kaxiom

enum class ContentEncoding(
    val encoding: String
) {
    IDENTITY("1"),
    GZIP("gzip"),
}