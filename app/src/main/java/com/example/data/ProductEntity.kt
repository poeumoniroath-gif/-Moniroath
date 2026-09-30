package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.model.Product
import com.example.model.ProductCategory

@Entity(tableName = "products")
data class ProductEntity(
    @PrimaryKey
    val id: String,
    val nameKh: String,
    val priceRiel: Int,
    val categoryName: String,
    val categoryKh: String,
    val iconEmoji: String,
    val primaryColorHex: Long,
    val descriptionKh: String = "",
    val stockCount: Int = 20,
    val lowStockThreshold: Int = 5
) {
    fun toProduct(): Product {
        val cat = try {
            ProductCategory.valueOf(categoryName)
        } catch (_: Exception) {
            ProductCategory.DRINKS_SWEETS
        }
        return Product(
            id = id,
            nameKh = nameKh,
            priceRiel = priceRiel,
            category = cat,
            categoryKh = categoryKh,
            iconEmoji = iconEmoji,
            primaryColorHex = primaryColorHex,
            descriptionKh = descriptionKh,
            stockCount = stockCount,
            lowStockThreshold = lowStockThreshold
        )
    }

    companion object {
        fun fromProduct(product: Product): ProductEntity {
            return ProductEntity(
                id = product.id,
                nameKh = product.nameKh,
                priceRiel = product.priceRiel,
                categoryName = product.category.name,
                categoryKh = product.categoryKh,
                iconEmoji = product.iconEmoji,
                primaryColorHex = product.primaryColorHex,
                descriptionKh = product.descriptionKh,
                stockCount = product.stockCount,
                lowStockThreshold = product.lowStockThreshold
            )
        }
    }
}
