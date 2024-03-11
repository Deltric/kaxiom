package dev.kaxiom.injest

import dev.kaxiom.KAxiom
import dev.kaxiom.serializer.CSVEncodable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.ExperimentalCoroutinesApi
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.atomic.AtomicBoolean
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.newSingleThreadContext
import java.time.Duration
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import kotlin.time.toKotlinDuration

/**
 * Abstract class representing a pool of events to be injested.
 * @param T The type of events in the pool.
 * @param settings The settings for the pool.
 */
abstract class InjestPool<T : Any>(
    settings: PoolSettings
) {

    protected val queue = ConcurrentLinkedQueue<String>()
    private val shutdown = AtomicBoolean(false)
    private var autoFlushJob : Job? = null

    init {
        // When autoFlush is set, a new coroutine is launched to flush the queue at the specified interval.
        if (settings.autoFlush != null) {
            @OptIn(ExperimentalCoroutinesApi::class, DelicateCoroutinesApi::class)
            this.autoFlushJob = CoroutineScope(newSingleThreadContext("")).launch {
                while (this.isActive && !shutdown.get()) {
                    val (time, unit) = settings.autoFlush
                    flush()
                    delay(Duration.of(time, unit).toKotlinDuration())
                }
            }
        }
    }

    /**
     * Processes and adds the given events to the queue.
     * @param events The events to add to the queue.
     */
    open fun injest(vararg events : T) {
        if (this.isShutdown()) {
            return
        }
        this.queue.addAll(events.map { this.processEvent(it) })
    }

    /**
     * Processes and adds the given events to the queue.
     * @param events The events to add to the queue.
     */
    open fun injest(events : List<T>) {
        if (this.isShutdown()) {
            return
        }
        this.queue.addAll(events.map { this.processEvent(it) })
    }

    /**
     * Processes an event item before adding it to the queue.
     * @param event The event to process.
     * @return The processed event as a string.
     */
    abstract fun processEvent(event : T): String

    /**
     * Flushes all queued events by sending them to the Axiom injest API.
     */
    abstract fun flush()

    /**
     * Prevents any new events from being added to the queue and flushes any remaining events.
     * Also cancels the auto flush job if one is running.
     */
    fun shutdown() {
        this.flush()
        this.autoFlushJob?.cancel()
        this.shutdown.set(true)
    }

    fun isShutdown() : Boolean = this.shutdown.get()
}

/**
 * A pool for injesting events that can be serialized to JSON.
 * @param T The type of events in the pool.
 * @param settings The settings for the pool.
 */
class JsonInjestPool<T : Any>(
    private val settings: JsonPoolSettings
) : InjestPool<T>(settings) {

    /**
     * Processes an event item before adding it to the queue.
     * Appends the current time to the event if useAxiomTime is false.
     *
     * @param event The event to process.
     * @return The processed event as a string.
     */
    override fun processEvent(event: T): String {
        val itemJson = this.settings.gson.toJson(event)
        if (settings.useAxiomTime) {
            return itemJson
        }
        val currentTime = ZonedDateTime.now( ZoneOffset.UTC ).format( DateTimeFormatter.ISO_INSTANT )
        return itemJson.substring(0, itemJson.length - 1) + ",\"_time\":\"$currentTime\"}"
    }

    /**
     * Sends all queued events to the Axiom injest API as a JSON payload.
     */
    override fun flush() {
        val events = mutableListOf<String>()
        while (this.queue.peek() != null) {
            events.add(this.queue.poll())
        }

        if (events.isEmpty()) {
            return
        }

        val payload = when (settings.format) {
            JsonFormat.JSON -> createJsonPayload(events)
            JsonFormat.NDJSON -> createNDJsonPayload(events)
        }

        KAxiom.injest(
            token = settings.token,
            dataset = settings.dataset,
            payload = payload,
            contentType = settings.type,
            encoding = settings.encoding,
        )
    }

    /**
     * Creates a JSON payload.
     * Format: [{item1},{item2},...]
     * @param payload The list of JSON events to include in the payload.
     * @return The JSON payload.
     */
    private fun createJsonPayload(payload: List<String>): String {
        return "[${payload.joinToString(",")}]"
    }

    /**
     * Creates a NDJSON payload.
     * Format: {item1}\n{item2}\n...
     * @param payload The list of JSON events to include in the payload.
     * @return The NDJSON payload.
     */
    private fun createNDJsonPayload(payload: List<String>): String {
        return payload.joinToString("\n")
    }
}

/**
 * A pool for injesting events that can be serialized to CSV or are CSV.
 * @param settings The settings for the pool.
 */
class CSVInjestPool(
    private val settings: CSVPoolSettings
) : InjestPool<String>(settings) {

    /**
     * Processes and adds the given [CSVEncodable] events to the queue.
     * @param events The [CSVEncodable] events to add to the queue.
     */
    fun injest(vararg events : CSVEncodable) {
        if (this.isShutdown()) {
            return
        }
        this.queue.addAll(events.map { this.processEvent(it.toCSV()) })
    }

    /**
     * Processes and adds the given [CSVEncodable] events to the queue.
     * @param events The [CSVEncodable] events to add to the queue.
     */
    fun injestEncodable(events : List<CSVEncodable>) {
        if (this.isShutdown()) {
            return
        }
        this.queue.addAll(events.map { this.processEvent(it.toCSV()) })
    }

    /**
     * Processes an event item before adding it to the queue.
     * Appends the current time to the event if useAxiomTime is false.
     * @param event The event to process.
     * @return The processed event as a string.
     */
    override fun processEvent(event: String): String {
        if (settings.useAxiomTime) {
            return event
        }
        val currentTime = ZonedDateTime.now( ZoneOffset.UTC ).format( DateTimeFormatter.ISO_INSTANT )
        return "$event,$currentTime"
    }

    /**
     * Sends all queued events to the Axiom injest API as a CSV payload.
     */
    override fun flush() {
        val events = mutableListOf<String>()
        while (this.queue.peek() != null) {
            events.add(this.queue.poll())
        }

        if (events.isEmpty()) {
            return
        }

        KAxiom.injest(
            token = settings.token,
            dataset = settings.dataset,
            payload = this.createCsvPayload(events),
            contentType = settings.type,
            encoding = settings.encoding,
        )
    }

    /**
     * Creates a CSV payload.
     * Format: header\n{item1}\n{item2}\n...
     * @return The CSV payload.
     */
    private fun createCsvPayload(payload: List<String>): String {
        val header = if (settings.useAxiomTime) {
            "${settings.header},_time"
        } else {
            settings.header
        }
        return "$header\n${payload.joinToString("\n")}"
    }
}