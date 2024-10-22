package vickram.tech.routes

import io.ktor.client.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import vickram.tech.controllers.TokenManager
import vickram.tech.controllers.initiateSTKPush

fun Route.paymentRoutes(
    client: HttpClient,
    consumerKey: String,
    consumerSecret: String,
    grantUrl: String,
    stkUrl: String,
) {
    route("/payments") {
        post("/stk-push") {
            try {
                val tokenManager = TokenManager(client, consumerKey, consumerSecret, grantUrl)
                val response = initiateSTKPush(
                    client = client,
                    stkUrl = stkUrl,
                    tokenManager = tokenManager,
                    businessShortCode = "174379",
                    passKey = "bfb279f9aa9bdbcf158e97dd71a467cd2e0c893059b10f78e6b72ada1ed2c919",
                    amount = "1",
                    phoneNumber = "254794157205",
                    callBackURL = "https://mydomain.com/pat"
                )
                response?.let {
                    call.respond(it)
                }
            } catch (e: Exception) {
                call.respondText("Failed to initiate STK Push")
            }
        }
        post("/stk-callback") {
            // handleSTKCallback(client, apiUrl, accessToken, businessShortCode, passKey, amount, phoneNumber, callBackURL)
        }
    }
}