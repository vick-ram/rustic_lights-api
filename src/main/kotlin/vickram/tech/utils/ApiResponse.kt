package vickram.tech.utils

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import kotlinx.serialization.Serializable

@Serializable
data class ApiResponse<T>(
    val status: Boolean,
    val message: String,
    val data: T? = null
)

suspend inline fun <reified T> ApplicationCall.respondJson(
    success: Boolean,
    message: String,
    data: T? = null,
    status: HttpStatusCode = HttpStatusCode.OK
) {
    val response = ApiResponse(success, message, data)
    this.respond(status, response)
}

