package vickram.tech.routes

import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.server.request.*
import io.ktor.server.routing.*
import vickram.tech.controllers.*
import vickram.tech.models.Category
import vickram.tech.models.Product
import vickram.tech.utils.BlankException
import vickram.tech.utils.respondJson
import vickram.tech.utils.saveImage
import vickram.tech.utils.toUUID
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
                    data = null
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
                    data = null
                )
            } catch (e: Exception) {
                call.respondJson<Category>(
                    success = false,
                    message = e.message!!,
                    data = null
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
                    data = null
                )
            } catch (e: Exception) {
                call.respondJson<Category>(
                    success = false,
                    message = e.message!!,
                    data = null
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
                    data = null
                )
            } catch (e: Exception) {
                call.respondJson<Category>(
                    success = false,
                    message = e.message!!,
                    data = null
                )
            }
        }
    }
    route("/products") {
        get {
            try {
                val products = getProducts()
                call.respondJson(
                    success = true,
                    message = "Products retrieved successfully",
                    data = products
                )
            } catch (e: Exception) {
                call.respondJson<Product>(
                    success = false,
                    message = e.message!!,
                    data = null
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
                    data = null
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
                    data = null
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
                    data = null
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
                    data = null
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
                    data = null
                )
            }
        }
    }
}