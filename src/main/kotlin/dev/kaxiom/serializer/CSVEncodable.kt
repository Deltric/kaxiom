package dev.kaxiom.serializer

/**
 * Interface for objects that can be encoded to CSV.
 */
interface CSVEncodable {

    /**
     * Returns a CSV representation of the object.
     * @return The CSV representation of the object.
     */
    fun toCSV(): String

}