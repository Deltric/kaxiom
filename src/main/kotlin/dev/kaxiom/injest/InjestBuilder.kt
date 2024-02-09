package dev.kaxiom.injest

import dev.kaxiom.ContentEncoding
import dev.kaxiom.ContentType

class InjestBuilder {

    var dataset: String? = null
    var type: ContentType? = null
    var encoding: ContentEncoding = ContentEncoding.GZIP
    var header: String? = null
    var payload: List<PayloadItem> = emptyList()

    /**
     * Creates an [Injest] request.
     *
     * @return The [Injest] request.
     * @throws IllegalArgumentException If the dataset or type is not set.
     */
    fun build(): Injest {
        if (this.payload.isEmpty()) {
            throw IllegalArgumentException("Payload cannot be empty.")
        }

        val contentType = this.type ?: this.payload.first().supportedTypes.first()

        if (this.payload.any { !it.supportedTypes.contains(contentType) }) {
            throw IllegalArgumentException("All payload items must be of the same type as the type of injest.")
        }

        val payload = when(contentType) {
            ContentType.JSON -> this.createJsonPayload()
            ContentType.NDJSON -> this.createNDJsonPayload()
            ContentType.CSV -> this.createCsvPayload()
        }

        return Injest(
            dataset = this.dataset ?: throw IllegalArgumentException("Dataset must be set."),
            type = contentType,
            encoding = this.encoding,
            payload = payload
        )
    }

    /**
     * Creates a JSON payload.
     * Format: [{item1},{item2},...]
     *
     * @return The JSON payload.
     */
    private fun createJsonPayload(): String {
        return "[${this.payload.joinToString(",") { it.serialize(ContentType.JSON) }}]"
    }

    /**
     * Creates a NDJSON payload.
     * Format: {item1}\n{item2}\n...
     *
     * @return The NDJSON payload.
     */
    private fun createNDJsonPayload(): String {
        return this.payload.joinToString("\n") { it.serialize(ContentType.NDJSON) }
    }

    /**
     * Creates a CSV payload.
     * Format: header\n{item1}\n{item2}\n...
     *
     * @return The CSV payload.
     */
    private fun createCsvPayload(): String {
        val header = this.header ?: throw IllegalArgumentException("CSV header must be set, when using ContentType.CSV.")
        val payload = this.payload.joinToString("\n") { it.serialize(ContentType.CSV) }
        return "$header\n$payload}"
    }

}