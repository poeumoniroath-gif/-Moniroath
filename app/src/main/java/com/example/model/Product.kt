package com.example.model

enum class ProductCategory(val titleKh: String) {
    ALL("ទាំងអស់"),
    DRINKS_SWEETS("ភេសជ្ជៈ & បង្អែម"),
    SNACKS("នំ & អាហារ"),
    TOYS("របស់លេង")
}

data class Product(
    val id: String,
    val nameKh: String,
    val priceRiel: Int,
    val category: ProductCategory,
    val categoryKh: String,
    val iconEmoji: String,
    val primaryColorHex: Long,
    val descriptionKh: String = ""
)

object ProductCatalog {
    val items: List<Product> = listOf(
        Product(
            id = "slushie",
            nameKh = "Slushie",
            priceRiel = 4000,
            category = ProductCategory.DRINKS_SWEETS,
            categoryKh = "ភេសជ្ជៈ",
            iconEmoji = "🥤",
            primaryColorHex = 0xFF00BCD4,
            descriptionKh = "ភេសជ្ជៈទឹកកកឈូសរសជាតិផ្លែឈើឆ្ងាញ់ត្រជាក់ចិត្ត"
        ),
        Product(
            id = "nom_gop_thom",
            nameKh = "នំ កញ្ចប់ ធំ",
            priceRiel = 3500,
            category = ProductCategory.SNACKS,
            categoryKh = "អាហារសម្រន់",
            iconEmoji = "🍿",
            primaryColorHex = 0xFFFF9800,
            descriptionKh = "នំកញ្ចប់ខ្នាតធំ ស្រួយឆ្ងាញ់"
        ),
        Product(
            id = "nom_gop_toch",
            nameKh = "នំកញ្ចប់ តូច",
            priceRiel = 1500,
            category = ProductCategory.SNACKS,
            categoryKh = "អាហារសម្រន់",
            iconEmoji = "🍘",
            primaryColorHex = 0xFFFFB74D,
            descriptionKh = "នំកញ្ចប់ខ្នាតតូច រសជាតិឆ្ងាញ់"
        ),
        Product(
            id = "saray_korea",
            nameKh = "សារាយ កូរ៉េ",
            priceRiel = 2000,
            category = ProductCategory.SNACKS,
            categoryKh = "អាហារសម្រន់",
            iconEmoji = "🍙",
            primaryColorHex = 0xFF4CAF50,
            descriptionKh = "សារាយសមុទ្របន្ទះបំពងបែបកូរ៉េ"
        ),
        Product(
            id = "mee_kambong",
            nameKh = "មី កំប៉ុង",
            priceRiel = 3500,
            category = ProductCategory.SNACKS,
            categoryKh = "អាហារសម្រន់",
            iconEmoji = "🍜",
            primaryColorHex = 0xFFE91E63,
            descriptionKh = "មីកំប៉ុងឆ្ងាញ់ពិសាស្រួលញ៉ាំ"
        ),
        Product(
            id = "nido_chroung_ori",
            nameKh = "នីដូ ជ្រុង Ori",
            priceRiel = 12000,
            category = ProductCategory.DRINKS_SWEETS,
            categoryKh = "នំ & ទឹកដោះគោ",
            iconEmoji = "🧃",
            primaryColorHex = 0xFF9C27B0,
            descriptionKh = "នីដូជ្រុង រសជាតិដើម Original"
        ),
        Product(
            id = "nido_chroung_n",
            nameKh = "នីដូជ្រុង N",
            priceRiel = 10000,
            category = ProductCategory.DRINKS_SWEETS,
            categoryKh = "នំ & ទឹកដោះគោ",
            iconEmoji = "📦",
            primaryColorHex = 0xFF673AB7,
            descriptionKh = "នីដូជ្រុង ប្រភេទ N"
        ),
        Product(
            id = "nido_moul",
            nameKh = "នីដូ មូល",
            priceRiel = 10000,
            category = ProductCategory.DRINKS_SWEETS,
            categoryKh = "នំ & ទឹកដោះគោ",
            iconEmoji = "🥛",
            primaryColorHex = 0xFF3F51B5,
            descriptionKh = "នីដូមូល រសជាតិឆ្ងាញ់ពិសា"
        ),
        Product(
            id = "koun_neak",
            nameKh = "កូននាគ",
            priceRiel = 10000,
            category = ProductCategory.TOYS,
            categoryKh = "របស់លេង",
            iconEmoji = "🐲",
            primaryColorHex = 0xFF009688,
            descriptionKh = "តុក្កតារបស់លេងកូននាគ"
        ),
        Product(
            id = "nom_pao",
            nameKh = "នំប៉ាវ",
            priceRiel = 8000,
            category = ProductCategory.SNACKS,
            categoryKh = "អាហារសម្រន់",
            iconEmoji = "🥟",
            primaryColorHex = 0xFF8D6E63,
            descriptionKh = "នំប៉ាវក្តៅៗរសជាតិឆ្ងាញ់"
        ),
        Product(
            id = "strawberry_bangvil",
            nameKh = "ស្ត្របឺរី បង្វិល",
            priceRiel = 2000,
            category = ProductCategory.DRINKS_SWEETS,
            categoryKh = "ស្ករគ្រាប់",
            iconEmoji = "🍓",
            primaryColorHex = 0xFFF06292,
            descriptionKh = "ស្ករគ្រាប់ស្ត្របឺរីបង្វិលសប្បាយញ៉ាំ"
        ),
        Product(
            id = "blind_box",
            nameKh = "Blind Box",
            priceRiel = 1000,
            category = ProductCategory.TOYS,
            categoryKh = "របស់លេង",
            iconEmoji = "🎁",
            primaryColorHex = 0xFFFF5722,
            descriptionKh = "ប្រអប់កាដូភ្ញាក់ផ្អើល Blind Box"
        ),
        Product(
            id = "fan",
            nameKh = "Fan",
            priceRiel = 1000,
            category = ProductCategory.TOYS,
            categoryKh = "របស់លេង",
            iconEmoji = "🪭",
            primaryColorHex = 0xFF03A9F4,
            descriptionKh = "កង្ហារតូចគួរឱ្យស្រលាញ់"
        ),
        Product(
            id = "gummy",
            nameKh = "Gummy",
            priceRiel = 1500,
            category = ProductCategory.DRINKS_SWEETS,
            categoryKh = "ស្ករគ្រាប់",
            iconEmoji = "🍬",
            primaryColorHex = 0xFF8BC34A,
            descriptionKh = "ស្ករស្វិតចាហួយ Gummy រសជាតិផ្លែឈើ"
        )
    )

    fun getProductById(id: String): Product? = items.find { it.id == id }
}
