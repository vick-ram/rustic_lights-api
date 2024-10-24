package vickram.tech.models

import kotlinx.serialization.Serializable
import vickram.tech.utils.*
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.*

@Serializable
data class Product(
    @Serializable(with = UUIDSerializer::class)
    val id: UUID = UUID.randomUUID(),
    val name: String,
    val shortDescription: String,
    val detailedDescription: String,
    @Serializable(with = BigDecimalSerializer::class)
    val price: BigDecimal,
    val quantity: Int,
    val sku: String = generateRandomSku(16),
    @Serializable(with = UUIDSerializer::class)
    val category: UUID,
    val image: String? = null,
    @Serializable(with = BigDecimalSerializer::class)
    val discount: BigDecimal? = null,
    val favourite: Boolean = false,
    @Serializable(with = DateTimeSerializer::class)
    val createdAt: LocalDateTime = LocalDateTime.now(),
    @Serializable(with = DateTimeSerializer::class)
    val updatedAt: LocalDateTime = LocalDateTime.now()
) {
    fun validate(): Product {
        if (name.isBlank()) {
            throw BlankException("Product name cannot be blank")
        }

        if (shortDescription.isBlank()) {
            throw BlankException("Product description cannot be blank")
        }

        if (detailedDescription.isBlank()) {
            throw BlankException("Product description cannot be blank")
        }

        if (sku.isBlank()) {
            throw BlankException("Product SKU cannot be blank")
        }

        if (price <= BigDecimal.ZERO) {
            throw InvalidInputException("Price must be greater than zero")
        }

        if (discount != null && discount >= price) {
            throw InvalidInputException("Discount amount must be less than the price")
        }

        if (quantity < 0) {
            throw InvalidInputException("Quantity in stock must be greater than or equal to zero")
        }

        return this
    }
}

@Serializable
data class Category(
    @Serializable(with = UUIDSerializer::class)
    val id: UUID = UUID.randomUUID(),
    val name: String
) {
    fun validate(): Category {
        if (name.isBlank()) {
            throw BlankException("Category name cannot be blank")
        }

        return this
    }
}

@Serializable
data class UserFavourite(
    @Serializable(with = UUIDSerializer::class)
    val userId: UUID,
    @Serializable(with = UUIDSerializer::class)
    val productId: UUID,
    val favourite: Boolean
)

@Serializable
data class Cart(
    @Serializable(with = UUIDSerializer::class)
    val id: UUID = UUID.randomUUID(),
    @Serializable(with = UUIDSerializer::class)
    val userId: UUID,
    val items: List<CartItem> = emptyList(),
    @Serializable(with = BigDecimalSerializer::class)
    val total: BigDecimal,
)

@Serializable
data class CartItem(
    @Serializable(with = UUIDSerializer::class)
    val id: UUID = UUID.randomUUID(),
    @Serializable(with = UUIDSerializer::class)
    val cartId: UUID,
    val product: Product,
    val quantity: Int,
    @Serializable(with = BigDecimalSerializer::class)
    val unitPrice: BigDecimal,
    @Serializable(with = BigDecimalSerializer::class)
    val discountPrice: BigDecimal
)
/*To be implemented later*/
data class WishList(
    @Serializable(with = UUIDSerializer::class)
    val id: UUID = UUID.randomUUID(),
    val user: User,
    val items: List<Product> = emptyList(),
    @Serializable(with = BigDecimalSerializer::class)
    val total: BigDecimal,
) {
    fun validate(): WishList {
        if (total < BigDecimal.ZERO) {
            throw InvalidInputException("Total must be greater than zero")
        }

        return this
    }
}

@Serializable
data class Review(
    @Serializable(with = UUIDSerializer::class)
    val id: UUID = UUID.randomUUID(),
    @Serializable(with = UUIDSerializer::class)
    val productId: UUID,
    @Serializable(with = UUIDSerializer::class)
    val userId: UUID,
    val rating: Int,
    val commentTitle: String? = null,
    val comment: String,
    val helpful: Int = 0,
    @Serializable(with = DateTimeSerializer::class)
    val createdAt: LocalDateTime = LocalDateTime.now(),
    @Serializable(with = DateTimeSerializer::class)
    val updatedAt: LocalDateTime = LocalDateTime.now()
) {
    fun validate(): Review {
        if (rating < 1 || rating > 5) {
            throw InvalidInputException("Rating must be between 1 and 5")
        }

        if (comment.isBlank()) {
            throw BlankException("Comment cannot be blank")
        }

        return this
    }
}
/*
@Serializable
data class Offer(
    @Serializable(with = UUISSerializer::class)
    val id: UUID = UUID.randomUUID(),
    val product: Product,
    @Serializable(with = BigDecimalSerializer::class)
    val discount: BigDecimal,
    @Serializable(with = DateTimeSerializer::class)
    val validFrom: LocalDateTime,
    @Serializable(with = DateTimeSerializer::class)
    val validTo: LocalDateTime,
    val isActive: Boolean,
) {
    fun validate(): Boolean {
        val now = LocalDateTime.now()
        if (validFrom.isAfter(now) || validTo.isBefore(now)) {
            throw InvalidInputException("Offer is not valid")
        }

        if (!isActive) {
            throw InvalidInputException("Offer is not active")
        }

        if (validFrom.isEqual(validTo) || validFrom.isAfter(validTo)) {
            throw InvalidInputException("Invalid date range")
        }

        return true
    }
}

@Serializable
data class Coupon(
    @Serializable(with = UUISSerializer::class)
    val id: UUID = UUID.randomUUID(),
    val code: String?,
    @Serializable(with = BigDecimalSerializer::class)
    val discountAmount: BigDecimal,
    val discountPercentage: Double,
    val product: Product,
    val usageLimit: Int,
    val usageCount: Int,
    @Serializable(with = DateTimeSerializer::class)
    val expirationDate: LocalDateTime,
    val isActive: Boolean,
) {
    fun validate(): Boolean {
        val now = LocalDateTime.now()
        if (expirationDate.isBefore(now)) {
            throw InvalidInputException("Coupon has expired")
        }

        if (usageCount >= usageLimit) {
            throw InvalidInputException("Coupon has reached its usage limit")
        }

        return true
    }
}*/
