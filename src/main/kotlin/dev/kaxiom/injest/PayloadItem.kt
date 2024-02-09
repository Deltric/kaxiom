package dev.kaxiom.injest

import dev.kaxiom.ContentType

/**
 * Represents an item for an [Injest] payload.
 */
interface PayloadItem {
    /**
     * The content types supported by the payload item.
     */
    val supportedTypes: Array<ContentType>

    /**
     * Serializes the payload item to a string.
     */
    fun serialize(contentType: ContentType): String
}