package vickram.tech.routes

import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.routing.*
import vickram.tech.controllers.*
import vickram.tech.models.*
import vickram.tech.utils.*

fun Route.orderRoutes() {
    route("/cart") {
        authenticate("auth-jwt") {
            post {
                try {
                    val principal = call.principal<JWTPrincipal>()
                    val userId = principal?.payload?.subject
                        ?: return@post call.respondJson<String>(
                            false,
                            "User not found",
                            null,
                            HttpStatusCode.BadRequest
                        )
                    val productId = call.request.queryParameters["productId"]
                        ?: return@post call.respondJson<String>(
                            false,
                            "Product not found",
                            null,
                            HttpStatusCode.BadRequest
                        )
                    val quantity = call.request.queryParameters["quantity"]?.toIntOrNull() ?: 1
                    val addedProduct = addProductToCart(userId.toUUID(), productId.toUUID(), quantity)
                    call.respondJson<Cart>(
                        true,
                        "Product added to cart",
                        addedProduct,
                        HttpStatusCode.Created
                    )
                } catch (e: NotFoundException) {
                    call.respondJson<User>(
                        false,
                        e.message ?: "Product not found",
                        null,
                        HttpStatusCode.NotFound
                    )
                } catch (e: NotFoundException) {
                    call.respondJson<Product>(
                        false,
                        e.message ?: "User not found",
                        null,
                        HttpStatusCode.NotFound
                    )
                } catch (e: Exception) {
                    call.respondJson<String>(
                        false,
                        e.message ?: "An error occurred",
                        null,
                        HttpStatusCode.InternalServerError
                    )
                }
            }

            authenticate("auth-jwt") {
                get {
                    try {
                        val principal = call.principal<JWTPrincipal>()
                        val userId = principal?.payload?.subject
                            ?: return@get call.respondJson<String>(
                                false,
                                "User not found",
                                null,
                                HttpStatusCode.BadRequest
                            )
                        val cart = getCart(userId.toUUID())
                        call.respondJson<Cart>(
                            true,
                            "Cart retrieved",
                            cart,
                            HttpStatusCode.OK
                        )
                    } catch (e: NotFoundException) {
                        call.respondJson<User>(
                            false,
                            e.message ?: "User not found",
                            null,
                            HttpStatusCode.NotFound
                        )
                    } catch (e: Exception) {
                        call.respondJson<String>(
                            false,
                            e.message ?: "An error occurred",
                            null,
                            HttpStatusCode.InternalServerError
                        )
                    } catch (e: Exception) {
                        call.respondJson<String>(
                            false,
                            e.message ?: "An error occurred",
                            null,
                            HttpStatusCode.InternalServerError
                        )
                    }
                }
            }
        }

        authenticate("auth-jwt") {
            patch("/{productId}") {
                try {
                    val userId = call.principal<JWTPrincipal>()?.payload?.subject?.toUUID()
                        ?: return@patch call.respondJson<String>(
                            false,
                            "User not found",
                            null,
                            HttpStatusCode.BadRequest
                        )
                    val productId = call.parameters["productId"]?.toUUID()
                        ?: return@patch call.respondJson<String>(
                            false,
                            "Product not found",
                            null,
                            HttpStatusCode.BadRequest
                        )
                    val quantity = call.request.queryParameters["quantity"]?.toIntOrNull()
                        ?: return@patch call.respondJson<String>(
                            false,
                            "Quantity not found",
                            null,
                            HttpStatusCode.BadRequest
                        )
                    updateProductCartQuantity(userId, productId, quantity)
                    call.respondJson<String>(
                        true,
                        "Product quantity updated",
                        null,
                        HttpStatusCode.OK
                    )
                } catch (e: NotFoundException) {
                    call.respondJson<String>(
                        false,
                        e.message ?: "Product not found",
                        null,
                        HttpStatusCode.NotFound
                    )
                } catch (e: Exception) {
                    call.respondJson<String>(
                        false,
                        e.message ?: "An error occurred",
                        null,
                        HttpStatusCode.InternalServerError
                    )
                }
            }
        }

        authenticate("auth-jwt") {
            delete {
                try {
                    val userId = call.principal<JWTPrincipal>()?.payload?.subject?.toUUID()
                        ?: return@delete call.respondJson<String>(
                            false,
                            "User not found",
                            null,
                            HttpStatusCode.BadRequest
                        )
                    val productId = call.request.queryParameters["productId"]?.toUUID()
                        ?: return@delete call.respondJson<String>(
                            false,
                            "Product not found",
                            null,
                            HttpStatusCode.BadRequest
                        )
                    deleteProductFromCart(userId, productId)
                    call.respondJson<String>(
                        true,
                        "Product deleted from cart",
                        null,
                        HttpStatusCode.NoContent
                    )
                } catch (e: NotFoundException) {
                    call.respondJson<String>(
                        false,
                        e.message ?: "Product not found",
                        null,
                        HttpStatusCode.NotFound
                    )
                } catch (e: Exception) {
                    call.respondJson<String>(
                        false,
                        e.message ?: "An error occurred",
                        null,
                        HttpStatusCode.InternalServerError
                    )
                }
            }
        }
    }


    route("/address") {
        authenticate("auth-jwt") {
            post {
                try {
                    val addressRequest = call.receive<Address>().validate()
                    val address = createAddress(addressRequest)
                    call.respondJson<Address>(
                        true,
                        "Address created",
                        address,
                        HttpStatusCode.Created
                    )
                } catch (e: BlankException) {
                    call.respondJson<String>(
                        false,
                        e.message ?: "Address cannot be blank",
                        null,
                        HttpStatusCode.BadRequest
                    )
                } catch (e: IllegalArgumentException) {
                    call.respondJson<String>(
                        false,
                        e.message ?: "Invalid address format",
                        null,
                        HttpStatusCode.BadRequest
                    )
                } catch (e: Exception) {
                    call.respondJson<String>(
                        false,
                        e.message ?: "An error occurred",
                        null,
                        HttpStatusCode.InternalServerError
                    )
                }
            }
        }
        authenticate("auth-jwt") {
            get {
                try {
                    val principal = call.principal<JWTPrincipal>()
                    val userId = principal?.payload?.subject
                        ?: return@get call.respondJson<String>(
                            false,
                            "User not found",
                            null,
                            HttpStatusCode.BadRequest
                        )
                    val addresses = getAddresses(userId.toUUID())
                    call.respondJson(
                        true,
                        "Addresses retrieved",
                        addresses,
                        HttpStatusCode.OK
                    )
                } catch (e: Exception) {
                    call.respondJson<String>(
                        false,
                        e.message ?: "An error occurred",
                        null,
                        HttpStatusCode.InternalServerError
                    )
                }
            }
        }
    }


    route("/orders") {
        authenticate("auth-jwt") {
            post {
                try {
                    val principal = call.principal<JWTPrincipal>()
                    val userId = principal?.subject
                        ?: return@post
                    val newOrder = createOrder(userId.toUUID())
                    call.respondJson<Order>(
                        true,
                        "Order created",
                        newOrder,
                        HttpStatusCode.Created
                    )
                } catch (e: NotFoundException) {
                    e.printStackTrace()
                    call.respondJson<String>(
                        false,
                        e.message ?: "User not found",
                        null,
                        HttpStatusCode.NotFound
                    )
                } catch (e: Exception) {
                    e.printStackTrace()
                    call.respondJson<String>(
                        false,
                        e.message ?: "An error occurred",
                        null,
                        HttpStatusCode.InternalServerError
                    )
                }
            }
        }

        authenticate("auth-jwt") {
            get {
                try {
                    val principal = call.principal<JWTPrincipal>()
                    val userId = principal?.subject
                        ?: return@get call.respondJson<String>(
                            false,
                            "User not found",
                            null,
                            HttpStatusCode.BadRequest
                        )
                    val orders = getUserOrders(userId.toUUID())
                    call.respondJson(
                        true,
                        "Orders retrieved",
                        orders,
                        HttpStatusCode.OK
                    )
                } catch (e: Exception) {
                    call.respondJson<String>(
                        false,
                        e.message ?: "An error occurred",
                        null,
                        HttpStatusCode.InternalServerError
                    )
                }
            }
        }

/*        get {
            try {
                val orders = getOrders()
                call.respondJson(
                    true,
                    "Orders retrieved",
                    orders,
                    HttpStatusCode.OK
                )
            } catch (e: Exception) {
                call.respondJson<String>(
                    false,
                    e.message ?: "An error occurred",
                    null,
                    HttpStatusCode.InternalServerError
                )
            }
        }*/

        get("/{id}") {
            try {
                val id = call.parameters["id"]?.toUUID()
                    ?: return@get call.respondJson<String>(
                        false,
                        "Order not found",
                        null,
                        HttpStatusCode.BadRequest
                    )
                val order = getOrder(id)
                    ?: return@get call.respondJson<String>(
                        false,
                        "Order not found",
                        null,
                        HttpStatusCode.NotFound
                    )
                call.respondJson<Order>(
                    true,
                    "Order retrieved",
                    order,
                    HttpStatusCode.OK
                )
            } catch (e: Exception) {
                call.respondJson<String>(
                    false,
                    e.message ?: "An error occurred",
                    null,
                    HttpStatusCode.InternalServerError
                )
            }
        }

        patch("/{id}") {
            try {
                val id = call.parameters["id"]?.toUUID()
                    ?: return@patch call.respondJson<String>(
                        false,
                        "Order not found",
                        null,
                        HttpStatusCode.BadRequest
                    )
                val status = call.request.queryParameters["status"]
                    ?: return@patch call.respondJson<String>(
                        false,
                        "Status not found",
                        null,
                        HttpStatusCode.BadRequest
                    )
                val order = updateOrderStatus(id, ORDER_STATUS.valueOf(status))
                call.respondJson<Order>(
                    true,
                    "Order status updated",
                    order,
                    HttpStatusCode.OK
                )
            } catch (e: NotFoundException) {
                call.respondJson<String>(
                    false,
                    e.message ?: "Order not found",
                    null,
                    HttpStatusCode.NotFound
                )
            } catch (e: Exception) {
                call.respondJson<String>(
                    false,
                    e.message ?: "An error occurred",
                    null,
                    HttpStatusCode.InternalServerError
                )
            }
        }

        delete("/{id}") {
            try {
                val id = call.parameters["id"]?.toUUID()
                    ?: return@delete call.respondJson<String>(
                        false,
                        "Order not found",
                        null,
                        HttpStatusCode.BadRequest
                    )
                val deleted = deleteOrder(id)
                if (deleted) {
                    call.respondJson<String>(
                        true,
                        "Order deleted",
                        null,
                        HttpStatusCode.OK
                    )
                } else {
                    call.respondJson<String>(
                        false,
                        "Order not found",
                        null,
                        HttpStatusCode.NotFound
                    )
                }
            } catch (e: Exception) {
                call.respondJson<String>(
                    false,
                    e.message ?: "An error occurred",
                    null,
                    HttpStatusCode.InternalServerError
                )
            }
        }
    }
}
