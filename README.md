# KAxiom
KAxiom is an unofficial Kotlin JVM binding for the Axiom API. 

- This project is actively in development and breaking changes may occur during this early stage.
- We'd love to hear any feedback, questions, or suggestions you have. Please feel free to open an issue.
- **Injest data:** KAxiom provides a simple way to ingest data into Axiom. Supports all of Axiom's injest formats and includes automatic serialization of objects utilizing Gson.
- **Query data:** KAxiom currently does not support querying data from Axiom. This is a planned feature that is currently in the works.

## Documentation
- Dokka (TODO)
- [Wiki](https://github.com/Deltric/kaxiom/wiki)

## Installation
## Gradle (Kotlin DSL)
```kotlin
TODO
```

## Gradle (Groovy DSL)
```groovy
TODO
```

## Maven
```xml
TODO
```

## Quickstart
Create a new injest pool like this:
```kotlin
import java.util.UUID
import dev.kaxiom.KAxiom

data class ExampleEvent(
    val id: UUID,
    val name: String,
    val context: Map<String, Any>
)

fun main() {
    // Build your InjestPool by providing your Axiom API key and dataset name
    val pool = KAxiom.createInjestPool<ExampleEvent> {
        this.token = "my-token"
        this.dataset = "my-dataset"
    }

    pool.injest(ExampleEvent(UUID.randomUUID(), "Foo Event", mapOf("foo" to "bar")))

    // Flush the pool to send events to Axiom
    pool.flush()
}
```

## License
KAxiom is licensed under [MIT License](LICENSE)