package vickram.tech.db

import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.javatime.datetime
import vickram.tech.models.Order
import vickram.tech.utils.ORDER_STATUS
import vickram.tech.utils.PGEnum
import java.util.*

object Orders : UUIDTable("orders") {
    val userId = reference("user_id", Users, onDelete = ReferenceOption.CASCADE)
    val total = decimal("total", 10, 2)
    val status = customEnumeration(
        "status",
        "ORDER_STATUS",
        { value -> ORDER_STATUS.valueOf(value as String) },
        { PGEnum("ORDER_STATUS", it) }
    )
    val createdAt = datetime("created_at")
    val updatedAt = datetime("updated_at")
}

class OrderEntity(id: EntityID<UUID>): UUIDEntity(id) {
    companion object: UUIDEntityClass<OrderEntity>(Orders)

    var user by UserEntity referencedOn Orders.userId
    var total by Orders.total
    var status by Orders.status
    var createdAt by Orders.createdAt
    var updatedAt by Orders.updatedAt

    fun toOrder() = Order(
        id = id.value,
        user = user.toUser(),
        total = total,
        status = status,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}

object OrderItems : UUIDTable("order_details") {
    val orderId = reference("order_id", Orders, onDelete = ReferenceOption.CASCADE)
    val productId = reference("product_id", Products, onDelete = ReferenceOption.CASCADE)
    val quantity = integer("quantity")
    val unitPrice = double("price")
}

class OrderItemEntity(id: EntityID<UUID>): UUIDEntity(id) {
    companion object: UUIDEntityClass<OrderItemEntity>(OrderItems)

    var order by OrderEntity referencedOn OrderItems.orderId
    var product by ProductEntity referencedOn OrderItems.productId
    var quantity by OrderItems.quantity
    var unitPrice by OrderItems.unitPrice

    /*fun toOrderItem() = Order.OrderItem(
        product = product.toProduct(),
        quantity = quantity,
        unitPrice = unitPrice
    )*/
}