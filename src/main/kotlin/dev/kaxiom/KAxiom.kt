package dev.kaxiom

import com.google.gson.Gson
import com.google.gson.JsonObject
import dev.kaxiom.exception.AxiomAuthException
import dev.kaxiom.exception.AxiomException
import dev.kaxiom.exception.AxiomInvalidPayloadException
import dev.kaxiom.injest.CSVInjestPool
import dev.kaxiom.injest.CSVPoolBuilder
import dev.kaxiom.injest.JsonInjestPool
import dev.kaxiom.injest.JsonPoolBuilder
import java.net.HttpURLConnection
import java.net.URL
import java.util.zip.GZIPOutputStream

/**
 * Central object for interacting with the Axiom API.
 */
object KAxiom {

    private const val AXIOM_URL = "https://api.axiom.co"
    private const val API_VERSION = "v1"
    private const val BASE_URL = "$AXIOM_URL/$API_VERSION"

    private val responseGson = Gson()

    /**
     * Creates a new [JsonInjestPool] with the specified settings.
     * @param builder The settings for the pool.
     * @return The created json pool.
     */
    fun <T : Any> createInjestPool(builder: JsonPoolBuilder<T>.() -> Unit): JsonInjestPool<T> {
        return JsonPoolBuilder<T>().apply(builder).build()
    }

    /**
     * Creates a new [CSVInjestPool] with the specified settings.
     * @param builder The settings for the pool.
     * @return The created csv pool.
     */
    fun createInjestPool(builder: CSVPoolBuilder.() -> Unit): CSVInjestPool {
        return CSVPoolBuilder().apply(builder).build()
    }

    /**
     * Sends a payload to the specified dataset.
     * @param dataset The dataset to send the payload to.
     * @param payload The payload to send.
     * @param contentType The content type of the payload.
     * @param encoding The encoding of the payload.
     * @throws AxiomAuthException If the token is invalid.
     * @throws AxiomInvalidPayloadException If the payload is invalid.
     * @throws AxiomException If the request fails for any other reason.
     */
    fun injest(
        token: String,
        dataset: String,
        payload: String,
        contentType: ContentType,
        encoding: ContentEncoding = ContentEncoding.GZIP,
    ) {
        val url = URL("$BASE_URL/datasets/$dataset/ingest")
        val connection = url.openConnection() as HttpURLConnection
        connection.requestMethod = "POST"

        connection.doOutput = true
        connection.setRequestProperty("Authorization", "Bearer $token")
        connection.setRequestProperty("Content-Type", contentType.type)

        if (encoding != ContentEncoding.IDENTITY) {
            connection.setRequestProperty("Content-Encoding", encoding.encoding)
        }

        if (encoding == ContentEncoding.GZIP) {
            GZIPOutputStream(connection.outputStream).use {
                it.write(payload.toByteArray())
            }
        } else {
            connection.outputStream.use { os ->
                os.write(payload.toByteArray())
            }
        }

        val responseCode = connection.responseCode
        if (responseCode == 200) {
            return
        }

        val responseBody = connection.errorStream.bufferedReader().use { it.readText() }
        val response = responseGson.fromJson(responseBody, JsonObject::class.java)
        val message = response["message"].asString

        if (responseCode == 403) {
            throw AxiomAuthException()
        } else if (responseCode == 400) {
            throw AxiomInvalidPayloadException(message)
        }
        throw AxiomException(message)
    }
}