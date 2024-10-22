package vickram.tech.plugins

import io.github.smiley4.ktorswaggerui.SwaggerUI
import io.ktor.client.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.http.content.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.webjars.*
import vickram.tech.routes.orderRoutes
import vickram.tech.routes.paymentRoutes
import vickram.tech.routes.productRoutes
import vickram.tech.routes.userRoutes
import java.io.File

fun Application.configureRouting(
    payload: Payload,
    client: HttpClient,
    consumerKey: String,
    consumerSecret: String,
    grantUrl: String,
    stkUrl: String,
) {
    install(Webjars) {
        path = "/webjars"
    }
    install(SwaggerUI) {
        swagger {
            swaggerUrl = "swagger-ui"
            forwardRoot = true
        }
        info {
            title = "Example API"
            version = "latest"
            description = "Example API for testing and demonstration purposes."
        }
        server {
            url = "http://localhost:8080"
            description = "Development Server"
        }
    }
    install(StatusPages) {
        exception<Throwable> { call, cause ->
            call.respondText(
                text = "500: $cause",
                status = HttpStatusCode.InternalServerError
            )
        }
    }
    routing {
        get("/") {
            call.respondText("Hello World!")
        }
        get("/webjars") {
            call.respondText("<script src='/webjars/jquery/jquery.js'></script>", ContentType.Text.Html)
        }
        userRoutes(payload)
        productRoutes()
        orderRoutes()
        paymentRoutes(
            client = client,
            consumerKey = consumerKey,
            consumerSecret = consumerSecret,
            grantUrl = grantUrl,
            stkUrl = stkUrl,
        )

        // static file image serving
        staticFiles(
            remotePath = "/images",
            dir = File("images")
        )
    }
}
