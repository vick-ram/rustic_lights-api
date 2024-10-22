package vickram.tech.models

import kotlinx.serialization.Serializable
import vickram.tech.utils.*
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.*

@Serializable
data class Order(
    @Serializable(with = UUIDSerializer::class)
    val id: UUID = UUID.randomUUID(),
    val user: User,
    val items: List<OrderItem> = emptyList(),
    @Serializable(with = BigDecimalSerializer::class)
    val total: BigDecimal,
    val status: ORDER_STATUS,
    @Serializable(with = DateTimeSerializer::class)
    val createdAt: LocalDateTime = LocalDateTime.now(),
    @Serializable(with = DateTimeSerializer::class)
    val updatedAt: LocalDateTime = LocalDateTime.now()
)

@Serializable
data class OrderItem(
    @Serializable(with = UUIDSerializer::class)
    val id: UUID = UUID.randomUUID(),
    val product: Product,
    val quantity: Int,
    val unitPrice: Double
) {
    fun validate(): OrderItem {
        if (quantity <= 0) {
            throw InvalidInputException("Quantity must be greater than zero")
        }

        return this
    }
}