package dev.kaxiom.serializer

/**
 * Interface for objects that can be encoded to CSV.
 */
interface CSVEncodable {

    /**
     * Encodes the object to a list of strings, where each string is a field in the row.
     * @return The list of fields in the row.
     */
    fun toCSV(): List<String>

}