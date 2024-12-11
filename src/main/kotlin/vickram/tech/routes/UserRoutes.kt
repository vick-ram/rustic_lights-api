package vickram.tech.routes

import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import vickram.tech.controllers.*
import vickram.tech.models.Credential
import vickram.tech.models.User
import vickram.tech.plugins.Payload
import vickram.tech.utils.*
import java.util.*

fun Route.userRoutes(payload: Payload) {
    route("/users") {
        route("/auth") {
            post("/register") {
                try {
                    val user = call.receive<User>().validate()
                    val newUser = createUser(user)
                    call.respondJson(
                        success = true,
                        message = "User created",
                        data = newUser,
                        status = HttpStatusCode.Created
                    )
                } catch (e: BlankException) {
                    call.respondJson<User>(
                        success = false,
                        message = e.message!!,
                        data = null,
                        status = HttpStatusCode.BadRequest
                    )
                } catch (e: IllegalArgumentException) {
                    call.respondJson<User>(
                        success = false,
                        message = e.message!!,
                        data = null,
                        status = HttpStatusCode.BadRequest
                    )
                } catch (e: Exception) {
                    call.respondJson<User>(
                        success = false,
                        message = e.message ?: "An error occurred",
                        data = null,
                        status = HttpStatusCode.InternalServerError
                    )
                }
            }

            post("/login") {
                try {
                    val credentials = call.receive<Credential>().validate()
                    val tokenPair = authenticateUser(credentials, payload)
                    call.respondJson(
                        success = true,
                        message = "User authenticated",
                        data = tokenPair,
                        status = HttpStatusCode.OK
                    )
                } catch (e: BlankException) {
                    call.respondJson<User>(
                        success = false,
                        message = e.message!!,
                        data = null,
                        status = HttpStatusCode.BadRequest
                    )
                } catch (e: UnauthorizedException) {
                    call.respondJson<User>(
                        success = false,
                        message = e.message!!,
                        data = null,
                        status = HttpStatusCode.Unauthorized
                    )
                } catch (e: IllegalArgumentException) {
                    call.respondJson<String>(
                        success = false,
                        message = e.message!!,
                        data = null,
                        status = HttpStatusCode.BadRequest
                    )
                } catch (e: Exception) {
                    call.respondJson<String>(
                        success = false,
                        message = e.message ?: "An error occurred",
                        data = null,
                        status = HttpStatusCode.InternalServerError
                    )
                }
            }
            authenticate("auth-jwt") {
                post("/logout") {
                    try {
                        val token = call.request.headers["Authorization"]?.removePrefix("Bearer ")
                            ?: throw IllegalArgumentException("Invalid token")
                        logout(token)
                        call.respondText("User logged out", status = HttpStatusCode.OK)
                    } catch (e: IllegalArgumentException) {
                        call.respondJson<String>(
                            success = false,
                            message = e.message!!,
                            data = null,
                            status = HttpStatusCode.BadRequest
                        )
                    } catch (e: Exception) {
                        call.respondJson<String>(
                            success = false,
                            message = e.message ?: "An error occurred",
                            data = null,
                            status = HttpStatusCode.InternalServerError
                        )
                    }
                }
            }
        }

        authenticate("auth-jwt") {
            patch("/profile/{userId}") {
                try {
                    var profileImg: String? = null
                    val multiPartData = call.receiveMultipart()
                    val userId = call.parameters["userId"]
                        ?: throw UnauthorizedException("User not authenticated")
                    multiPartData.forEachPart { part ->
                        when(part) {
                            is PartData.FileItem -> {
                                if (part.name == "profile") {
                                    profileImg = saveImage(part, "images")
                                }
                            }
                            else -> {}
                        }
                        part.dispose()
                    }
                    val updatedUser = updateUserProfile(userId.toUUID(), profileImg!!)
                    call.respondJson<User>(
                        success = true,
                        message = "User profile updated",
                        data = updatedUser,
                        status = HttpStatusCode.Accepted
                    )
                } catch (e: UnauthorizedException) {
                    call.respondJson<User>(
                        success = false,
                        message = e.message!!,
                        data = null,
                        status = HttpStatusCode.Unauthorized
                    )
                } catch (e: NotFoundException) {
                    call.respondJson<User>(
                        success = false,
                        message = e.message!!,
                        data = null,
                        status = HttpStatusCode.NotFound
                    )
                } catch (e: Exception) {
                    call.respondJson<User>(
                        success = false,
                        message = e.message ?: "An error occurred",
                        data = null,
                        status = HttpStatusCode.InternalServerError
                    )
                }
            }
        }

        get {
            try {
                val email = call.request.queryParameters["email"]
                if (email != null) {
                    val user = getUserByEmail(email)
                    call.respondJson(
                        success = true,
                        message = "User retrieved",
                        data = user,
                        status = HttpStatusCode.OK
                    )
                    return@get
                } else {
                    val users = getUsers()
                    call.respondJson(
                        success = true,
                        message = "Users retrieved",
                        data = users,
                        status = HttpStatusCode.OK
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()
                call.respondJson<User>(
                    success = false,
                    message = e.message ?: "An error occurred",
                    data = null,
                    status = HttpStatusCode.InternalServerError
                )
            }
        }

        get("/{id}") {
            try {
                val id = UUID.fromString(call.parameters["id"])
                    ?: throw IllegalArgumentException("Invalid user ID")
                val user = getUser(id)
                call.respondJson(
                    success = true,
                    message = "User retrieved",
                    data = user,
                    status = HttpStatusCode.OK
                )
            } catch (e: IllegalArgumentException) {
                call.respondJson<User>(
                    success = false,
                    message = e.message!!,
                    data = null,
                    status = HttpStatusCode.BadRequest
                )
            } catch (e: Exception) {
                call.respondJson<User>(
                    success = false,
                    message = e.message ?: "An error occurred",
                    data = null,
                    status = HttpStatusCode.InternalServerError
                )
            }
        }

        put("/{id}") {
            try {
                val id = UUID.fromString(call.parameters["id"])
                    ?: throw IllegalArgumentException("Invalid user ID")
                val user = call.receive<User>().validate()
                val updatedUser = updateUser(id, user)
                call.respondJson(
                    success = true,
                    message = "User updated",
                    data = updatedUser,
                    status = HttpStatusCode.OK
                )
            } catch (e: IllegalArgumentException) {
                call.respondJson<User>(
                    success = false,
                    message = e.message!!,
                    data = null,
                    status = HttpStatusCode.BadRequest
                )
            } catch (e: Exception) {
                call.respondJson<User>(
                    success = false,
                    message = e.message ?: "An error occurred",
                    data = null,
                    status = HttpStatusCode.InternalServerError
                )
            }
        }

        delete("/{id}") {
            try {
                val id = UUID.fromString(call.parameters["id"])
                    ?: throw IllegalArgumentException("Invalid user ID")
                deleteUser(id)
                call.respondJson<User>(
                    success = true,
                    message = "User deleted",
                    data = null,
                    status = HttpStatusCode.NoContent
                )
            } catch (e: NotFoundException) {
                call.respondJson<User>(
                    success = false,
                    message = e.message!!,
                    data = null,
                    status = HttpStatusCode.NotFound
                )
            } catch (e: Exception) {
                call.respondJson<User>(
                    success = false,
                    message = e.message ?: "An error occurred",
                    data = null,
                    status = HttpStatusCode.InternalServerError
                )
            }
        }
    }
}