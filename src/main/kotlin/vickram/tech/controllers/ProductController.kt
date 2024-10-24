package vickram.tech.controllers

import vickram.tech.db.CategoryEntity
import vickram.tech.db.ProductEntity
import vickram.tech.models.Category
import vickram.tech.models.Product
import vickram.tech.plugins.dbQuery
import vickram.tech.utils.generateRandomSku
import java.time.LocalDateTime
import java.util.*

suspend fun createCategory(category: Category): Category = dbQuery {
    val newCategory = CategoryEntity.new {
        this.name = category.name
    }
    return@dbQuery newCategory.toCategory()
}

suspend fun getCategories(): List<Category> = dbQuery {
    CategoryEntity
        .all()
        .map { it.toCategory() }
}

suspend fun getCategory(id: UUID): Category? = dbQuery {
    CategoryEntity
        .findById(id)
        ?.toCategory()
}

suspend fun deleteCategory(id: UUID): Boolean = dbQuery {
    CategoryEntity
        .findById(id)
        ?.delete()
        ?: false
    return@dbQuery true
}

suspend fun createProduct(product: Product): Product = dbQuery {

    val dbCategory =
        CategoryEntity.findById(product.category)
            ?: throw IllegalArgumentException("Category doesn't exist")

    val newProduct = ProductEntity.new {
        this.name = product.name
        this.shortDescription = product.shortDescription
        this.longDescription = product.detailedDescription
        this.price = product.price
        this.quantity = product.quantity
        this.sku = generateRandomSku(16)
        this.category = dbCategory
        this.image = product.image
        this.discount = product.discount
        this.createdAt = product.createdAt
        this.updatedAt = product.updatedAt
    }

    return@dbQuery newProduct.toProduct()
}

suspend fun getProducts(): List<Product> = dbQuery {
    ProductEntity
        .all()
        .map { it.toProduct() }
}

suspend fun getProduct(id: UUID): Product? = dbQuery {
    return@dbQuery ProductEntity
        .findById(id)
        ?.toProduct()
}

suspend fun deleteProduct(id: UUID): Boolean = dbQuery {
    ProductEntity
        .findById(id)
        ?.delete()
        ?: false
    return@dbQuery true
}

suspend fun updateProduct(
    id: UUID,
    product: Product
): Product? = dbQuery {
    val dbProduct = ProductEntity.findById(id) ?: return@dbQuery null

    val dbCategory =
        CategoryEntity.findById(product.category)
            ?: throw IllegalArgumentException("Category doesn't exist")

    dbProduct.apply {
        name = product.name
        shortDescription = product.shortDescription
        longDescription = product.detailedDescription
        price = product.price
        quantity = product.quantity
        sku = product.sku
        category = dbCategory
        image = product.image
        discount = product.discount
        updatedAt = LocalDateTime.now()
    }

    return@dbQuery dbProduct.toProduct()
}

