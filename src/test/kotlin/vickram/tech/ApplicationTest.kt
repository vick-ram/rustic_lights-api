package vickram.tech

import com.typesafe.config.ConfigFactory
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.config.*
import io.ktor.server.testing.*
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.sql.Database
import vickram.tech.models.User
import vickram.tech.plugins.initTestDb
import vickram.tech.utils.ApiResponse
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class ApplicationTest {
    @Test
    fun testRoot() = testApplication {
        initTestDb()
        application {
            module()
        }
        environment {
            config = HoconApplicationConfig(
                ConfigFactory.parseResources("application-test.conf")
                    .withFallback(ConfigFactory.load("application.conf"))
            )
        }

        val client = createClient {
            install(ContentNegotiation) {
                json(Json {
                    prettyPrint = true
                    isLenient = true
                    ignoreUnknownKeys = true
                })
            }
        }

        /*Test for users routes*/
        client.post("/users/auth/register") {
            val user = User(
                name = "Kevin Karanja",
                email = "kevin@gmail.com",
                password = "password12",
                phone = "254712345678"
            )
            contentType(ContentType.Application.Json)
            setBody(user)
        }.apply {
            assertEquals(HttpStatusCode.Created, status)
            val res = body<ApiResponse<User>>()
            assertEquals("User created", res.message)
            assertNull(res.data)
            assertEquals("Kevin Karanja", res.data?.name)
            assertEquals("kevin@gmail.com", res.data?.email)
        }

        client.get("/").apply {
            assertEquals(HttpStatusCode.OK, status)
            assertEquals("Hello World!", bodyAsText())
        }
    }
}
