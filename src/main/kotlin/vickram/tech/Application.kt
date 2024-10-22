package vickram.tech

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.auth.*
import io.ktor.client.plugins.auth.providers.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import kotlinx.serialization.json.Json
import vickram.tech.plugins.*

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

fun Application.module() {
    val client = HttpClient(CIO) {

        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                isLenient = true
                prettyPrint = true
            })
        }
    }
    val config = environment.config
    val jwtRealm = config.property("jwt.realm").getString()
    val jwtSecret = config.property("jwt.secret").getString()
    val jwtAudience = config.property("jwt.audience").getString()
    val jwtDomain = config.property("jwt.domain").getString()
    val payload = Payload(jwtDomain, jwtAudience, jwtSecret, jwtRealm)
    val consumerKey = config.property("mpesa.consumerKey").getString()
    val consumerSecret = config.property("mpesa.consumerSecret").getString()
    val grantUrl = config.property("mpesa.grantUrl").getString()
    val stkUrl = config.property("mpesa.stkUrl").getString()
    configureSecurity(payload)
    configureHTTP()
    configureSerialization()
    configureDatabases()
    configureRouting(
        payload = payload,
        client = client,
        consumerKey = consumerKey,
        consumerSecret = consumerSecret,
        grantUrl = grantUrl,
        stkUrl = stkUrl
    )

}
