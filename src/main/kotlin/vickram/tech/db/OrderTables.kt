package vickram.tech.db

import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.javatime.datetime
import vickram.tech.models.Address
import vickram.tech.models.Order
import vickram.tech.utils.ORDER_STATUS
import vickram.tech.utils.PGEnum
import java.time.LocalDateTime
import java.util.*

object Orders : UUIDTable("orders") {
    val userId = reference("user_id", Users, onDelete = ReferenceOption.CASCADE)
    val total = decimal("total", 10, 2)
    val status = customEnumeration(
        "status",
        "ORDER_STATUS",
        { value -> ORDER_STATUS.valueOf(value as String) },
        { PGEnum("ORDER_STATUS", it) }
    ).default(ORDER_STATUS.PENDING)
    val address = text("address", eagerLoading = true)
    val createdAt = datetime("created_at").default(LocalDateTime.now())
    val updatedAt = datetime("updated_at").default(LocalDateTime.now())
}

class OrderEntity(id: EntityID<UUID>): UUIDEntity(id) {
    companion object: UUIDEntityClass<OrderEntity>(Orders)

    var user by UserEntity referencedOn Orders.userId
    var total by Orders.total
    var status by Orders.status
    var address by Orders.address
    var createdAt by Orders.createdAt
    var updatedAt by Orders.updatedAt

    fun toOrder() = Order(
        id = id.value,
        userId = user.id.value,
        total = total,
        status = status,
        address = address,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}

object OrderItems : UUIDTable("order_details") {
    val orderId = reference("order_id", Orders, onDelete = ReferenceOption.CASCADE)
    val productId = reference("product_id", Products, onDelete = ReferenceOption.CASCADE)
    val quantity = integer("quantity")
    val unitPrice = decimal("price", 10, 2)
    val discount = decimal("discount", 10, 2).nullable()
}

class OrderItemEntity(id: EntityID<UUID>): UUIDEntity(id) {
    companion object: UUIDEntityClass<OrderItemEntity>(OrderItems)

    var order by OrderEntity referencedOn OrderItems.orderId
    var product by ProductEntity referencedOn OrderItems.productId
    var quantity by OrderItems.quantity
    var unitPrice by OrderItems.unitPrice
    var discount by OrderItems.discount

}

object Addresses: UUIDTable("addresses") {
    val userId = reference("user_id", Users, onDelete = ReferenceOption.CASCADE)
    val name = varchar("name", 250)
    val phone = varchar("phone", 250)
    val county = varchar("county", 250)
    val city = varchar("city", 250)
    val address = text("address", eagerLoading = true)
    val createdAt = datetime("created_at").default(LocalDateTime.now())
    val updatedAt = datetime("updated_at").default(LocalDateTime.now())
}

class AddressEntity(id: EntityID<UUID>): UUIDEntity(id) {
    companion object: UUIDEntityClass<AddressEntity>(Addresses)

    var user by UserEntity referencedOn Addresses.userId
    var name by Addresses.name
    var phone by Addresses.phone
    var county by Addresses.county
    var city by Addresses.city
    var address by Addresses.address
    var createdAt by Addresses.createdAt
    var updatedAt by Addresses.updatedAt

    fun toAddress() = Address(
        id = id.value,
        userId = user.id.value,
        name = name,
        phone = phone,
        county = county,
        city = city,
        address = address,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}