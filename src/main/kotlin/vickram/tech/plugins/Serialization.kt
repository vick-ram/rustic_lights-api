package vickram.tech.plugins

import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.*
import kotlinx.serialization.json.Json

fun Application.configureSerialization() {
    install(ContentNegotiation) {
        json(Json {
            this.isLenient = true
            this.prettyPrint = true
            this.ignoreUnknownKeys = true
        })
    }
}
