package vickram.tech.controllers

import org.jetbrains.exposed.sql.and
import vickram.tech.db.*
import vickram.tech.models.Address
import vickram.tech.models.Cart
import vickram.tech.models.Order
import vickram.tech.plugins.dbQuery
import vickram.tech.utils.NotFoundException
import vickram.tech.utils.ORDER_STATUS
import java.math.BigDecimal
import java.util.*

suspend fun addProductToCart(
    userId: UUID,
    productId: UUID,
    quantity: Int = 1
): Cart = dbQuery {
    var totalAmount = BigDecimal.ZERO
    val user = UserEntity.find { Users.id eq userId }.firstOrNull()
        ?: throw NotFoundException("User not found")
    val cart = CartEntity.find { Carts.userId eq user.id }.firstOrNull()
        ?: CartEntity.new {
            this.user = user
            this.total = totalAmount
        }

    val product =
        ProductEntity.find { Products.id eq productId }.firstOrNull()
            ?: throw NotFoundException("Product not found")
    val cartItem = CartItemEntity.find {
        (CartItems.cartId eq cart.id) and (CartItems.productId eq product.id)
    }.firstOrNull()
    if (cartItem != null) {
        cartItem.quantity += quantity
    } else {
        CartItemEntity.new {
            this.cart = cart
            this.product = product
            this.quantity = quantity
            this.unitPrice = product.price
        }
    }
    totalAmount = cart.items.sumOf { it.product.price * it.quantity.toBigDecimal() }
    cart.total = totalAmount

    return@dbQuery cart.toCart()
}

suspend fun updateProductCartQuantity(
    userId: UUID,
    productId: UUID,
    quantity: Int
): Cart = dbQuery {
    var totalAmount = BigDecimal.ZERO
    val user = UserEntity.find { Users.id eq userId }.firstOrNull()
        ?: throw NotFoundException("User not found")
    val cart = CartEntity.find { Carts.userId eq user.id }.firstOrNull()
        ?: throw NotFoundException("Cart not found")
    val product =
        ProductEntity.find { Products.id eq productId }.firstOrNull()
            ?: throw NotFoundException("Product not found")
    val cartItem = CartItemEntity.find {
        (CartItems.cartId eq cart.id) and (CartItems.productId eq product.id)
    }.firstOrNull()
        ?: throw NotFoundException("Product not found in cart")
    cartItem.quantity = quantity
    totalAmount = totalAmount.plus(product.price.times(quantity.toBigDecimal()))
    cart.total = totalAmount
    return@dbQuery cart.toCart()
}

suspend fun deleteProductFromCart(userId: UUID, productId: UUID): Cart = dbQuery {
    var totalAmount = BigDecimal.ZERO
    val user = UserEntity.find { Users.id eq userId }.firstOrNull()
        ?: throw NotFoundException("User not found")
    val cart = CartEntity.find { Carts.userId eq user.id }.firstOrNull()
        ?: throw NotFoundException("Cart not found")
    val product =
        ProductEntity.find { Products.id eq productId }.firstOrNull()
            ?: throw NotFoundException("Product not found")
    val cartItem = CartItemEntity.find {
        (CartItems.cartId eq cart.id) and (CartItems.productId eq product.id)
    }.firstOrNull()
        ?: throw NotFoundException("Product not found in cart")
    cartItem.delete()
    totalAmount = totalAmount.plus(product.price.times(cartItem.quantity.toBigDecimal()))
    cart.total = totalAmount
    return@dbQuery cart.toCart()
}

suspend fun getCart(userId: UUID): Cart = dbQuery {
    val user = UserEntity.find { Users.id eq userId }.firstOrNull()
        ?: throw NotFoundException("User not found")
    val cart = CartEntity.find { Carts.userId eq user.id }.firstOrNull()
        ?: throw NotFoundException("Cart not found")
    return@dbQuery cart.toCart()
}

suspend fun createAddress(address: Address): Address = dbQuery {
    val user = UserEntity.find { Users.id eq address.userId }.firstOrNull()
        ?: throw NotFoundException("User not found")
    val newAddress = AddressEntity.new {
        this.user = user
        this.name = address.name
        this.phone = address.phone
        this.county = address.county
        this.city = address.city
        this.address = address.address
    }
    return@dbQuery newAddress.toAddress()
}

suspend fun getAddresses(userId: UUID): List<Address> = dbQuery {
    return@dbQuery AddressEntity.find { Addresses.userId eq userId }
        .map(AddressEntity::toAddress)
}

suspend fun createOrder(order: Order): Order = dbQuery {
    val user = UserEntity.find { Users.id eq order.id }.firstOrNull()
        ?: throw NotFoundException("User not found")
    val newOrder = OrderEntity.new {
        this.user = user
        this.total = order.total
        this.status = order.status
        this.createdAt = order.createdAt
        this.updatedAt = order.updatedAt
    }
    order.items.forEach {
        val product =
            ProductEntity.find { Products.id eq it.product.id }.firstOrNull()
                ?: throw NotFoundException("Product not found")
        OrderItemEntity.new {
            this.order = newOrder
            this.product = product
            this.quantity = it.quantity
            this.unitPrice = it.unitPrice
        }
    }
    return@dbQuery newOrder.toOrder()
}

suspend fun updateOrderStatus(id: UUID, status: ORDER_STATUS): Order = dbQuery {
    val order = OrderEntity.find { Orders.id.eq(id) }.firstOrNull()
        ?: throw NotFoundException("Order not found")
    order.status = status
    return@dbQuery order.toOrder()
}

suspend fun getOrders(): List<Order> = dbQuery {
    OrderEntity.all()
        .map(OrderEntity::toOrder)
}

suspend fun getOrder(id: UUID): Order? = dbQuery {
    return@dbQuery OrderEntity
        .findById(id)
        ?.toOrder()
}

suspend fun deleteOrder(id: UUID): Boolean = dbQuery {
    val order = OrderEntity.findById(id)
        ?: throw NotFoundException("Order not found")
    order.delete()
    return@dbQuery true
}


