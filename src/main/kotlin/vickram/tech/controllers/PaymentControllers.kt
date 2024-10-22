package vickram.tech.controllers

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import vickram.tech.models.STKErrorResponse
import vickram.tech.models.STKPushRequest
import vickram.tech.models.STKPushResponse
import vickram.tech.models.STKSuccessResponse
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

@Serializable
data class TokenResponse(
    @SerialName("access_token") val accessToken: String,
    @SerialName("expires_in") val expiresIn: String
)

class TokenManager(
    private val client: HttpClient,
    private val consumerKey: String,
    private val consumerSecret: String,
    private val grantUrl: String
) {
    private var accessToken: String? = null
    private var tokenExpirationTime: Long = 0
    private val mutex = Mutex()

    suspend fun getAccessToken(): String {
        return mutex.withLock {
            val currentTime = System.currentTimeMillis() / 1000
            if (accessToken == null || currentTime >= tokenExpirationTime) {
                refreshAccessToken()
            }
            accessToken!!
        }
    }

    private suspend fun refreshAccessToken() {
        val credentials = Base64.getEncoder().encodeToString("$consumerKey:$consumerSecret".toByteArray())
        val response: TokenResponse = client.get(grantUrl) {
            headers { append("Authorization", "Basic $credentials") }
        }.body() ?: throw Exception("Failed to get access token")
        accessToken = response.accessToken
        tokenExpirationTime = (System.currentTimeMillis() / 1000) + response.expiresIn.toLong()
    }
}

suspend fun initiateSTKPush(
    client: HttpClient,
    stkUrl: String,
    tokenManager: TokenManager,
    businessShortCode: String = "174379",
    passKey: String,
    amount: String,
    phoneNumber: String,
    callBackURL: String,
): STKPushResponse? {
    val timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
    val password = Base64.getEncoder().encodeToString("$businessShortCode$passKey$timestamp".toByteArray())
    println("Password: $password")
    val requestBody = STKPushRequest(
        businessShortCode = businessShortCode,
        password = password,
        timestamp = timestamp,
        amount = amount,
        partyA = phoneNumber,
        partyB = businessShortCode,
        phoneNumber = phoneNumber,
        callBackURL = callBackURL,
        accountReference = "Rustic Lights",
        transactionDesc = "Paid Online"
    )
    val accessToken = tokenManager.getAccessToken()
    println("Access Token: $accessToken")

    try {
        val response = client.post(stkUrl) {
            headers { append("Authorization", "Bearer $accessToken") }
            contentType(ContentType.Application.Json)
            setBody(requestBody)
        }

        val responseBody = response.bodyAsText()
        println("Raw Response Body: $responseBody")

        if (response.status.value == 200) {
            return response.body()
        } else {
            println("Server responded with status: ${response.status}")
            return null
        }
    } catch (e: Exception) {
        println("Error initiating STK Push: ${e.message}")
        return null
    }
}

suspend fun stkCallback(call: ApplicationCall) {
    val successResponseBody = call.receive<STKSuccessResponse>()
    val errorResponseBody = call.receive<STKErrorResponse>()

    if (successResponseBody.body.stkCallback.resultCode == 0) {
        println("Payment successful")
    } else {
        println("Payment failed")
    }
}

