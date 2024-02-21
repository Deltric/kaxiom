# kaxiom
Unofficial Kotlin bindings for the Axiom API

## Example Usage
### Creating a new Axiom instance
```kotlin
val axiom = KAxiom("YOUR_API_TOKEN")
```

### Injesting a object
```kotlin
data class ItemSoldEvent(
    val item: String, 
    val price: Double, 
    val quantity: Int
)

// Json with serialization through Gson
axiom.injest {
    this.dataset = "example-purchases-dataset"
    this.payload = listOf(
        json(ItemSoldEvent("Apple", 1.0, 1)) {
            // Optionally you can supply a custom Gson instance or the default will be JsonPayloadItem.defaultGson.
            this.gson = Gson()
        },
        json(ItemSoldEvent("Banana", 2.3, 2))
    )
}

// CSV
axiom.injest {
    this.dataset = "example-purchases-dataset"
    this.header = "item,price,quantity"
    this.payload = listOf(
        csv("Apple,1.0,1"),
        csv("Banana,2.3,2")
    )
}
```