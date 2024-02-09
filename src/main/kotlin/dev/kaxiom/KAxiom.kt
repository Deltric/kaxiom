package dev.kaxiom

import com.google.gson.Gson
import com.google.gson.JsonObject
import dev.kaxiom.exception.AxiomAuthException
import dev.kaxiom.exception.AxiomException
import dev.kaxiom.exception.AxiomInvalidPayloadException
import dev.kaxiom.injest.InjestBuilder
import java.net.HttpURLConnection
import java.net.URL
import java.util.zip.GZIPOutputStream

class KAxiom(
    private val token: String,
) {
    private val baseUrl = "$AXIOM_URL/$API_VERSION"

    /**
     * Sends a payload to the specified dataset.
     *
     * @param builder The builder for the injest request.
     *
     * @throws AxiomAuthException If the token is invalid.
     * @throws AxiomInvalidPayloadException If the payload is invalid.
     * @throws AxiomException If the request fails for any other reason.
     */
    fun injest(builder: InjestBuilder.() -> Unit) {
        val injest = InjestBuilder().apply(builder).build()
        this.injest(
            dataset = injest.dataset,
            payload = injest.payload,
            contentType = injest.type,
            encoding = injest.encoding
        )
    }

    /**
     * Sends a payload to the specified dataset.
     *
     * @param dataset The dataset to send the payload to.
     * @param payload The payload to send.
     * @param contentType The content type of the payload.
     * @param encoding The encoding of the payload.
     *
     * @throws AxiomAuthException If the token is invalid.
     * @throws AxiomInvalidPayloadException If the payload is invalid.
     * @throws AxiomException If the request fails for any other reason.
     */
    fun injest(
        dataset: String,
        payload: String,
        contentType: ContentType,
        encoding: ContentEncoding = ContentEncoding.GZIP,
    ) {
        val url = URL("$baseUrl/datasets/$dataset/ingest")
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
        val response = gson.fromJson(responseBody, JsonObject::class.java)
        val message = response["message"].asString

        if (responseCode == 403) {
            throw AxiomAuthException()
        } else if (responseCode == 400) {
            throw AxiomInvalidPayloadException(message)
        }
        throw AxiomException(message)
    }

    companion object {
        const val AXIOM_URL = "https://api.axiom.co"
        const val API_VERSION = "v1"
        private val gson = Gson()
    }

}