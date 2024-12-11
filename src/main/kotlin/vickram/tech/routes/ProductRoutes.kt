package vickram.tech.routes

import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.routing.*
import vickram.tech.controllers.*
import vickram.tech.models.Category
import vickram.tech.models.Product
import vickram.tech.models.Review
import vickram.tech.utils.*
import java.math.BigDecimal
import java.util.*

fun Route.productRoutes() {
    route("/categories") {
        get {
            try {
                val categories = getCategories().map { it.validate() }
                call.respondJson(
                    success = true,
                    message = "Categories retrieved successfully",
                    data = categories
                )
            } catch (e: Exception) {
                call.respondJson<Category>(
                    success = false,
                    message = e.message!!,
                    data = null,
                    status = HttpStatusCode.InternalServerError
                )
            }
        }

        post {
            try {
                val category = call.receive<Category>()
                val newCategory = createCategory(category).validate()
                call.respondJson(
                    success = true,
                    message = "Category created successfully",
                    data = newCategory
                )
            } catch (e: BlankException) {
                call.respondJson<Category>(
                    success = false,
                    message = e.message!!,
                    data = null,
                    status = HttpStatusCode.BadRequest
                )
            } catch (e: Exception) {
                call.respondJson<Category>(
                    success = false,
                    message = e.message!!,
                    data = null,
                    status = HttpStatusCode.InternalServerError
                )
            }
        }

        get("/{id}") {
            try {
                val id = UUID.fromString(call.parameters["id"])
                    ?: throw IllegalArgumentException("Invalid ID")
                val category = getCategory(id)?.validate()
                call.respondJson(
                    success = true,
                    message = "Category retrieved successfully",
                    data = category
                )
            } catch (e: IllegalArgumentException) {
                call.respondJson<Category>(
                    success = false,
                    message = e.message!!,
                    data = null,
                    status = HttpStatusCode.BadRequest
                )
            } catch (e: Exception) {
                call.respondJson<Category>(
                    success = false,
                    message = e.message!!,
                    data = null,
                    status = HttpStatusCode.InternalServerError
                )
            }
        }

        delete("/{id}") {
            try {
                val id = UUID.fromString(call.parameters["id"])
                    ?: throw IllegalArgumentException("Invalid ID")
                val deleted = deleteCategory(id)
                call.respondJson<Category>(
                    success = deleted,
                    message = if (deleted) "Category deleted successfully" else "Category not found",
                    data = null
                )
            } catch (e: IllegalArgumentException) {
                call.respondJson<Category>(
                    success = false,
                    message = e.message!!,
                    data = null,
                    status = HttpStatusCode.BadRequest
                )
            } catch (e: Exception) {
                call.respondJson<Category>(
                    success = false,
                    message = e.message!!,
                    data = null,
                    status = HttpStatusCode.InternalServerError
                )
            }
        }
    }
    route("/products") {
        get {
            try {
                val searchQuery = call.request.queryParameters["search"]
                val products = if (searchQuery != null) {
                    searchProducts(searchQuery)
                } else {
                    getProducts()
                }
                call.respondJson(
                    success = true,
                    message = "Products retrieved successfully",
                    data = products
                )
            } catch (e: Exception) {
                call.respondJson<Product>(
                    success = false,
                    message = e.message!!,
                    data = null,
                    status = HttpStatusCode.InternalServerError
                )
            }
        }

        post {
            try {
                var name: String? = null
                var shortDescription: String? = null
                var detailedDescription: String? = null
                var price: BigDecimal? = null
                var quantityInStock: Int? = null
                var category: String? = null
                var image: String? = null
                var discountAmount: BigDecimal? = null

                val multiPart = call.receiveMultipart()
                multiPart.forEachPart { part ->
                    when (part) {
                        is PartData.FormItem -> {
                            when (part.name) {
                                "name" -> name = part.value
                                "shortDescription" -> shortDescription = part.value
                                "detailedDescription" -> detailedDescription = part.value
                                "price" -> price = BigDecimal(part.value)
                                "quantityInStock" -> quantityInStock = part.value.toInt()
                                "category" -> category = part.value
                                "image" -> image = part.value
                                "discountAmount" -> discountAmount = BigDecimal(part.value)
                            }
                        }

                        is PartData.FileItem -> {
                            if (part.name == "image") {
                                image = saveImage(part, "images")
                            }
                        }

                        else -> {}
                    }
                    part.dispose()
                }
                val newProduct = createProduct(
                    Product(
                        name = name!!,
                        shortDescription = shortDescription!!,
                        detailedDescription = detailedDescription!!,
                        price = price!!,
                        quantity = quantityInStock!!,
                        category = category?.toUUID()!!,
                        image = image,
                        discount = discountAmount
                    )
                ).validate()
                call.respondJson(
                    success = true,
                    message = "Product created successfully",
                    data = newProduct
                )
            } catch (e: BlankException) {
                call.respondJson<Product>(
                    success = false,
                    message = e.message!!,
                    data = null,
                    status = HttpStatusCode.BadRequest
                )
            } catch (e: Exception) {
                call.respondJson<Product>(
                    success = false,
                    message = e.message!!,
                    data = null,
                    status = HttpStatusCode.InternalServerError
                )
            }
        }

        authenticate("auth-jwt") {
            patch("/favourite/{id}") {
                try {
                    val principal = call.principal<JWTPrincipal>()
                    val userId = principal?.subject
                        ?: return@patch
                    val productId = call.parameters["id"]
                        ?: return@patch
                    val favourite = call.request.queryParameters["favourite"]
                        ?: return@patch
                    val favouriteProduct = addProductToFavourites(
                        userId.toUUID(),
                        productId.toUUID(),
                        favourite.toBoolean()
                    )
                    call.respondJson<Product>(
                        success = true,
                        message = "Product added to favourites",
                        data = favouriteProduct
                    )
                } catch (e: Exception) {
                    e.printStackTrace()
                    call.respondJson<Product>(
                        success = false,
                        message = e.message!!,
                        data = null,
                        status = HttpStatusCode.InternalServerError
                    )
                }
            }
        }

        authenticate("auth-jwt") {
            get("/favourite") {
                try {
                    val principal = call.principal<JWTPrincipal>()
                    val userId = principal?.subject
                        ?: return@get
                    val favouriteProducts = getUserFavourites(userId.toUUID())
                    call.respondJson(
                        success = true,
                        message = "Favourite products retrieved successfully",
                        data = favouriteProducts
                    )
                } catch (e: Exception) {
                    e.printStackTrace()
                    call.respondJson<Product>(
                        success = false,
                        message = e.message!!,
                        data = null,
                        status = HttpStatusCode.InternalServerError
                    )
                }
            }
        }

        get("/{id}") {
            try {
                val id = UUID.fromString(call.parameters["id"])
                    ?: throw IllegalArgumentException("Invalid ID")
                val product = getProduct(id)
                call.respondJson(
                    success = true,
                    message = "Product retrieved successfully",
                    data = product
                )
            } catch (e: IllegalArgumentException) {
                call.respondJson<Product>(
                    success = false,
                    message = e.message!!,
                    data = null
                )
            } catch (e: Exception) {
                call.respondJson<Product>(
                    success = false,
                    message = e.message!!,
                    data = null,
                    status = HttpStatusCode.InternalServerError
                )
            }
        }

        put("/{id}") {
            try {
                var name: String? = null
                var shortDescription: String? = null
                var detailedDescription: String? = null
                var price: BigDecimal? = null
                var quantityInStock: Int? = null
                var category: String? = null
                var image: String? = null
                var discountAmount: BigDecimal? = null

                val updatePart = call.receiveMultipart()

                updatePart.forEachPart { part ->
                    when (part) {
                        is PartData.FormItem -> {
                            when (part.name) {
                                "name" -> name = part.value
                                "shortDescription" -> shortDescription = part.value
                                "detailedDescription" -> detailedDescription = part.value
                                "price" -> price = BigDecimal(part.value)
                                "quantityInStock" -> quantityInStock = part.value.toInt()
                                "category" -> category = part.value
                                "image" -> image = part.value
                                "discountAmount" -> discountAmount = BigDecimal(part.value)
                            }
                        }

                        is PartData.FileItem -> {
                            if (part.name == "image") {
                                image = saveImage(part, "images")
                            }
                        }

                        else -> {}
                    }
                    part.dispose()
                }
                val id = UUID.fromString(call.parameters["id"])
                    ?: throw IllegalArgumentException("Invalid ID")

                val updatedProduct = updateProduct(
                    id,
                    Product(
                        name = name!!,
                        shortDescription = shortDescription!!,
                        detailedDescription = detailedDescription!!,
                        price = price!!,
                        quantity = quantityInStock!!,
                        category = category?.toUUID()!!,
                        image = image,
                        discount = discountAmount
                    )
                )?.validate()
                call.respondJson(
                    success = true,
                    message = "Product updated successfully",
                    data = updatedProduct
                )
            } catch (e: BlankException) {
                call.respondJson<Product>(
                    success = false,
                    message = e.message!!,
                    data = null,
                    status = HttpStatusCode.BadRequest
                )
            } catch (e: IllegalArgumentException) {
                call.respondJson<Product>(
                    success = false,
                    message = e.message!!,
                    data = null,
                    status = HttpStatusCode.BadRequest
                )
            } catch (e: Exception) {
                call.respondJson<Product>(
                    success = false,
                    message = e.message!!,
                    data = null,
                    status = HttpStatusCode.InternalServerError
                )
            }
        }

        delete("/{id}") {
            try {
                val id = UUID.fromString(call.parameters["id"])
                    ?: throw IllegalArgumentException("Invalid ID")
                val deleted = deleteProduct(id)
                call.respondJson<Product>(
                    success = deleted,
                    message = if (deleted) "Product deleted successfully" else "Product not found",
                    data = null,
                )
            } catch (e: IllegalArgumentException) {
                call.respondJson<Product>(
                    success = false,
                    message = e.message!!,
                    data = null,
                    status = HttpStatusCode.BadRequest
                )
            } catch (e: Exception) {
                call.respondJson<Product>(
                    success = false,
                    message = e.message!!,
                    data = null,
                    status = HttpStatusCode.InternalServerError
                )
            }
        }
    }

    route("/reviews") {
        post("/{productId}") {
            try {
                val principal = call.principal<JWTPrincipal>()
                val userId = principal?.subject
                    ?: return@post
                val review = call.receive<Review>()
                val productId = call.parameters["productId"]
                    ?: throw IllegalArgumentException("Invalid Product ID")
                val newReview = addReviewToProduct(
                    userId = userId.toUUID(),
                    productId = productId.toUUID(),
                    review = review.validate()
                )
                call.respondJson(
                    success = true,
                    message = "Review added successfully",
                    data = newReview
                )
            } catch (e: InvalidInputException) {
                e.printStackTrace()
                call.respondJson<String>(
                    success = false,
                    message = e.message!!,
                    data = null,
                    status = HttpStatusCode.BadRequest
                )
            }catch (e: BlankException) {
                e.printStackTrace()
                call.respondJson<String>(
                    success = false,
                    message = e.message!!,
                    data = null,
                    status = HttpStatusCode.BadRequest
                )
            }catch (e: IllegalArgumentException) {
                e.printStackTrace()
                call.respondJson<String>(
                    success = false,
                    message = e.message!!,
                    data = null,
                    status = HttpStatusCode.BadRequest
                )
            }catch (e: Exception) {
                e.printStackTrace()
                call.respondJson<String>(
                    success = false,
                    message = e.message!!,
                    data = null,
                    status = HttpStatusCode.InternalServerError
                )
            }
        }

        get("/{productId}") {
            try {
                val productId = call.parameters["productId"]
                    ?: throw IllegalArgumentException("Invalid Product ID")
                val reviews = getReviewsForProduct(productId.toUUID())
                call.respondJson(
                    success = true,
                    message = "Reviews retrieved successfully",
                    data = reviews
                )
            } catch (e: IllegalArgumentException) {
                e.printStackTrace()
                call.respondJson<String>(
                    success = false,
                    message = e.message!!,
                    data = null,
                    status = HttpStatusCode.BadRequest
                )
            } catch (e: Exception) {
                e.printStackTrace()
                call.respondJson<String>(
                    success = false,
                    message = e.message!!,
                    data = null,
                    status = HttpStatusCode.InternalServerError
                )
            }
        }
    }
}