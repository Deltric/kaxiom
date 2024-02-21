package dev.kaxiom.injest

import com.google.gson.Gson
import dev.kaxiom.ContentType

/**
 * Json payload item.
 */
class JsonPayloadItem<T>(
    private val value: T,
) : PayloadItem {

    var pretty: Boolean = false
    var gson: Gson? = null

    override val supportedTypes = arrayOf(ContentType.NDJSON, ContentType.JSON)

    override fun serialize(contentType: ContentType): String {
        val gson = gson ?: if (pretty) defaultPrettyGson else defaultGson
        return gson.toJson(value)
    }

    companion object {
        private val defaultGson = Gson()
        private val defaultPrettyGson = Gson().newBuilder().setPrettyPrinting().create()
    }
}

/**
 * Creates a JSON payload item.
 * @param value The value to be serialized.
 * @param item The configuration for the JSON payload item.
 */
fun <T> json(value: T, item: JsonPayloadItem<T>.() -> Unit = {}) = JsonPayloadItem(value).apply(item)