package dev.kaxiom.injest

import com.google.gson.Gson
import dev.kaxiom.ContentEncoding
import dev.kaxiom.exception.AxiomInjestPoolBuilderException
import java.time.temporal.ChronoUnit

/**
 * Shared builder base for all InjestPools
 */
open class InjestPoolBuilderBase {

    var token: String? = null
    var dataset: String? = null
    var encoding: ContentEncoding = ContentEncoding.GZIP
    var autoFlush: Pair<Long, ChronoUnit>? = null
    var useAxiomTime: Boolean = false

    /**
     * Checks if the required fields are set
     * @throws AxiomInjestPoolBuilderException if the token or dataset fields are not set
     */
    open fun validate() {
        if (this.token == null) {
            throw AxiomInjestPoolBuilderException("Token is required.")
        }
        if (this.dataset == null) {
            throw AxiomInjestPoolBuilderException("Dataset is required.")
        }
    }
}

/**
 * Builder for [JsonInjestPool]
 * @param T The type of the object to be ingested
 */
class JsonPoolBuilder<T : Any> : InjestPoolBuilderBase() {

    var gson = Gson()

    /**
     * Builds a new [JsonInjestPool] with the builder's settings
     * @return The new [JsonInjestPool]
     */
    fun build(): JsonInjestPool<T> {
        this.validate()

        return JsonInjestPool(
            JsonPoolSettings(
                token = this.token!!,
                dataset = this.dataset!!,
                encoding = this.encoding,
                autoFlush = this.autoFlush,
                useAxiomTime = this.useAxiomTime,
                gson = this.gson
            )
        )
    }
}

/**
 * Builder for [CSVInjestPool]
 */
class CSVPoolBuilder : InjestPoolBuilderBase() {

    var header: String? = null

    /**
     * Checks if the required fields are set
     * @throws AxiomInjestPoolBuilderException if the token, dataset, or header fields are not set
     */
    override fun validate() {
        if (this.header == null) {
            throw AxiomInjestPoolBuilderException("Header is required.")
        }
        return super.validate()
    }

    /**
     * Builds a new [CSVInjestPool] with the builder's settings
     * @return The new [CSVInjestPool]
     */
    fun build(): CSVInjestPool {
        this.validate()

        return CSVInjestPool(
            CSVPoolSettings(
                token = this.token!!,
                dataset = this.dataset!!,
                encoding = this.encoding,
                autoFlush = this.autoFlush,
                useAxiomTime = this.useAxiomTime,
                header = this.header!!
            )
        )
    }
}