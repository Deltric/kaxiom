package dev.kaxiom.injest

import dev.kaxiom.ContentType
import dev.kaxiom.serializer.CSVEncodable

/**
 * CSV payload item.
 */
class CSVPayloadItem(
    private val value: String
) : PayloadItem {
    constructor(encodableValue: CSVEncodable) : this(encodableValue.toCSV().joinToString(","))

    override val supportedTypes = arrayOf(ContentType.CSV)

    override fun serialize(contentType: ContentType): String {
        return this.value
    }
}

/**
 * Creates a CSV payload item.
 * @param value The CSV formatted value string.
 * @param item The configuration for the CSV payload item.
 */
fun csv(value: String, item: CSVPayloadItem.() -> Unit = {}) = CSVPayloadItem(value).apply(item)

/**
 * Creates a CSV payload item.
 * @param encodableValue The CSV encodable to be serialized.
 * @param item The configuration for the CSV payload item.
 */
fun csv(encodableValue: CSVEncodable, item: CSVPayloadItem.() -> Unit = {}) = CSVPayloadItem(encodableValue).apply(item)