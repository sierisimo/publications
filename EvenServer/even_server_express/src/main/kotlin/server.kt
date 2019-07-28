external fun require(module: String): dynamic

fun main() {
    val express = require("express")
    val app = express()
    configureEndpoints(app)

    app.listen(8080) {
        println("Running kotlin+express server")
    }
}

fun configureEndpoints(app: dynamic) {
    var count = 2
    app.get("/") { _, res ->
        res.send("Hi! Kotlin+Express server")
    }

    app.get("/info") { _, res ->
        val data = if (count % 2 == 0) {
            count--
            Data()
        } else {
            count++
            DataOptional()
        }

        res.json(data)
    }
}