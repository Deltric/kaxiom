package dev.kaxiom.injest

import com.google.gson.Gson
import dev.kaxiom.ContentEncoding
import dev.kaxiom.ContentType
import java.time.temporal.ChronoUnit

/**
 * Common settings for a [InjestPool].
 * @param token - The Axiom API token.
 * @param dataset - The dataset to injest into.
 * @param type - The type of content to ingest.
 * @param encoding - The encoding to use for the payload.
 * @param autoFlush - The time and unit to auto flush the pool.
 * @param useAxiomTime - Whether to use Axiom time for event timestamps or not.
 * If false, a timestamp will be added to each payload.
 * Useful if you have a long auto flush time and want a more accurate timestamp.
 */
open class PoolSettings(
    val token: String,
    val dataset: String,
    val type: ContentType,
    val encoding: ContentEncoding,
    val autoFlush: Pair<Long, ChronoUnit>?,
    val useAxiomTime: Boolean,
)

/**
 * Settings for a [JsonInjestPool].
 * @param token - The Axiom API token.
 * @param dataset - The dataset to injest into.
 * @param encoding - The encoding to use for the payload.
 * @param autoFlush - The time and unit to auto flush the pool.
 * @param useAxiomTime - Whether to use Axiom time for event timestamps or not.
 * If false, a timestamp will be added to each payload.
 * Useful if you have a long auto flush time and want a more accurate timestamp.
 * @param gson - The Gson instance to use for serialization.
 * @param format - The [JsonFormat] to use for formating the payload.
 */
class JsonPoolSettings(
    token: String,
    dataset: String,
    encoding: ContentEncoding = ContentEncoding.GZIP,
    autoFlush: Pair<Long, ChronoUnit>?,
    useAxiomTime: Boolean,
    val gson: Gson = Gson(),
    val format: JsonFormat = JsonFormat.JSON,
) : PoolSettings(
    token = token,
    dataset = dataset,
    type = format.contentType,
    encoding = encoding,
    autoFlush = autoFlush,
    useAxiomTime = useAxiomTime
)

/**
 * How to format the json payload.
 */
enum class JsonFormat(
    val contentType: ContentType
) {
    JSON(ContentType.JSON),
    NDJSON(ContentType.NDJSON),
}

/**
 * Settings for a [CSVInjestPool].
 * @param token - The Axiom API token.
 * @param dataset - The dataset to injest into.
 * @param encoding - The encoding to use for the payload.
 * @param autoFlush - The time and unit to auto flush the pool.
 * @param useAxiomTime - Whether to use Axiom time for event timestamps or not.
 * If false, a timestamp will be added to each payload.
 * Useful if you have a long auto flush time and want a more accurate timestamp.
 * @param header - The header to use for the CSV payload.
 */
class CSVPoolSettings(
    token: String,
    dataset: String,
    encoding: ContentEncoding = ContentEncoding.GZIP,
    autoFlush: Pair<Long, ChronoUnit>?,
    useAxiomTime: Boolean,
    val header: String,
) : PoolSettings(
    token = token,
    dataset = dataset,
    type = ContentType.CSV,
    encoding = encoding,
    autoFlush = autoFlush,
    useAxiomTime = useAxiomTime
)