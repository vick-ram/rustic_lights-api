package vickram.tech.db

import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.javatime.datetime
import vickram.tech.models.*
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.*

object Products : UUIDTable("products") {
    val name = varchar("name", 250)
    val shortDescription = text("short_description", eagerLoading = true)
    val longDescription = text("long_description", eagerLoading = true)
    val price = decimal("price", 10, 2)
    val quantity = integer("quantity")
    val sku = varchar("sku", 250)
    val categoryId = reference("category_id", Categories, onDelete = ReferenceOption.CASCADE)
    val image = varchar("image", 250).nullable()
    val discount = decimal("discount", 10, 2).nullable()
    val favourite = bool("favourite").default(false)
    val createdAt = datetime("created_at").default(LocalDateTime.now())
    val updatedAt = datetime("updated_at").default(LocalDateTime.now())

    init {
        uniqueIndex(name, sku)
    }
}

class ProductEntity(id: EntityID<UUID>): UUIDEntity(id) {
    companion object: UUIDEntityClass<ProductEntity>(Products)

    var name by Products.name
    var shortDescription by Products.shortDescription
    var longDescription by Products.longDescription
    var price by Products.price
    var quantity by Products.quantity
    var sku by Products.sku
    var category by CategoryEntity referencedOn Products.categoryId
    var image by Products.image
    var discount by Products.discount
    var favourite by Products.favourite
    var createdAt by Products.createdAt
    var updatedAt by Products.updatedAt

    fun toProduct() = Product(
        id = id.value,
        name = name,
        shortDescription = shortDescription,
        detailedDescription = longDescription,
        price = price,
        quantity = quantity,
        sku = sku,
        category = category.id.value,
        image = image,
        discount = discount,
        favourite = favourite,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}

object Categories : UUIDTable("categories") {
    val name = varchar("name", 250)
}

class CategoryEntity(id: EntityID<UUID>): UUIDEntity(id) {
    companion object: UUIDEntityClass<CategoryEntity>(Categories)

    var name by Categories.name

    fun toCategory() = Category(
        id = id.value,
        name = this.name
    )
}

object UserFavourites : UUIDTable() {
    val userId = reference("user_id", Users, onDelete = ReferenceOption.CASCADE)
    val productId = reference("product_id", Products, onDelete = ReferenceOption.CASCADE)
    val favourite = bool("favourite")
}

class UserFavouriteEntity(id: EntityID<UUID>) : UUIDEntity(id) {
    companion object : UUIDEntityClass<UserFavouriteEntity>(UserFavourites)

    var user by UserEntity referencedOn UserFavourites.userId
    var product by ProductEntity referencedOn UserFavourites.productId
    var favourite by UserFavourites.favourite

    fun toUserFavourite() = UserFavourite(
        userId = user.id.value,
        productId = product.id.value,
        favourite = favourite
    )
}

object Carts : UUIDTable("carts") {
    val userId = reference("user_id", Users, onDelete = ReferenceOption.CASCADE)
    val total = decimal("total", 10, 2)
}

class CartEntity(id: EntityID<UUID>): UUIDEntity(id) {
    companion object: UUIDEntityClass<CartEntity>(Carts)

    var user by UserEntity referencedOn Carts.userId
    var total by Carts.total

    val items by CartItemEntity referrersOn CartItems.cartId

    fun toCart() = Cart(
        id = id.value,
        userId = user.id.value,
        items = items.map { it.toCartItem() },
        total = total
    )
}

object CartItems : UUIDTable("cart_items") {
    val cartId = reference("cart_id", Carts, onDelete = ReferenceOption.CASCADE)
    val productId = reference("product_id", Products, onDelete = ReferenceOption.CASCADE)
    val quantity = integer("quantity")
    val unitPrice = decimal("price", 10, 2)
    val discount = decimal("discount", 10, 2).nullable()
}

class CartItemEntity(id: EntityID<UUID>): UUIDEntity(id) {
    companion object: UUIDEntityClass<CartItemEntity>(CartItems)

    var cart by CartEntity referencedOn CartItems.cartId
    var product by ProductEntity referencedOn CartItems.productId
    var quantity by CartItems.quantity
    var unitPrice by CartItems.unitPrice
    var discount by CartItems.discount

    fun toCartItem(): CartItem {
        return CartItem(
            id = id.value,
            cartId = cart.id.value,
            product = product.toProduct(),
            quantity = quantity,
            unitPrice = unitPrice,
            discountPrice = discount ?: BigDecimal.ZERO
        )
    }
}

object Reviews: UUIDTable("reviews") {
    val productId = reference("product_id", Products, onDelete = ReferenceOption.CASCADE)
    val userId = reference("user_id", Users, onDelete = ReferenceOption.CASCADE)
    val rating = integer("rating")
    val commentTitle = varchar("comment_title", 250).nullable()
    val comment = text("comment", eagerLoading = true)
    val helpful = integer("helpful").default(0)
    val createdAt = datetime("created_at").default(LocalDateTime.now())
    val updatedAt = datetime("updated_at").default(LocalDateTime.now())
}

class ReviewEntity(id: EntityID<UUID>): UUIDEntity(id) {
    companion object: UUIDEntityClass<ReviewEntity>(Reviews)

    var product by ProductEntity referencedOn Reviews.productId
    var user by UserEntity referencedOn Reviews.userId
    var rating by Reviews.rating
    var commentTitle by Reviews.commentTitle
    var comment by Reviews.comment
    var helpful by Reviews.helpful
    var createdAt by Reviews.createdAt
    var updatedAt by Reviews.updatedAt

    fun toReview() = Review(
        id = id.value,
        productId = product.id.value,
        userId = user.id.value,
        rating = rating,
        commentTitle = commentTitle,
        comment = comment,
        helpful = helpful,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}

/*
object Offers : UUIDTable("offers") {
    val productId = reference("product_id", Products, onDelete = ReferenceOption.CASCADE)
    val discount = decimal("discount", 10, 2)
    val validFrom = datetime("valid_from")
    val validTo = datetime("valid_to")
    val isActive = bool("is_active")
}

class OfferEntity(id: EntityID<UUID>): UUIDEntity(id) {
    companion object: UUIDEntityClass<OfferEntity>(Offers)

    var product by ProductEntity referencedOn Offers.productId
    var discount by Offers.discount
    var validFrom by Offers.validFrom
    var validTo by Offers.validTo
    var isActive by Offers.isActive

    fun toOffer() = Offer(
        id = id.value,
        product = product.toProduct(),
        discount = discount,
        validFrom = validFrom,
        validTo = validTo,
        isActive = isActive
    )
}

object Coupons : UUIDTable("coupons") {
    val code = varchar("code", 250).nullable()
    val discountAmount = decimal("discount_amount", 10, 2)
    val discountPercentage = decimal("discount_percentage", 10, 2)
    val productId = reference("product_id", Products, onDelete = ReferenceOption.CASCADE)
    val usageLimit = integer("usage_limit")
    val usageCount = integer("usage_count")
    val expirationDate = datetime("expiration_date")
    val isActive = bool("is_active")
}

class CouponEntity(id: EntityID<UUID>): UUIDEntity(id) {
    companion object: UUIDEntityClass<CouponEntity>(Coupons)

    var code by Coupons.code
    var discountAmount by Coupons.discountAmount
    var discountPercentage by Coupons.discountPercentage
    var product by ProductEntity referencedOn Coupons.productId
    var usageLimit by Coupons.usageLimit
    var usageCount by Coupons.usageCount
    var expirationDate by Coupons.expirationDate
    var isActive by Coupons.isActive

    fun toCoupon() = Coupon(
        id = id.value,
        code = code,
        discountAmount = discountAmount,
        discountPercentage = discountPercentage.toDouble(),
        product = product.toProduct(),
        usageLimit = usageLimit,
        usageCount = usageCount,
        expirationDate = expirationDate,
        isActive = isActive
    )
}*/
