package com.example.model

enum class UserRole(
    val titleEn: String,
    val titleKh: String,
    val defaultUsername: String,
    val fixedPin: String,
    val descriptionKh: String
) {
    ADMIN(
        titleEn = "Admin",
        titleKh = "អ្នកគ្រប់គ្រង (Admin)",
        defaultUsername = "Admin",
        fixedPin = "9999216",
        descriptionKh = "សិទ្ធិពេញលេញ: លក់ទំនិញ, គ្រប់គ្រងស្តុក, មើលរបាយការណ៍, និងការកំណត់"
    ),
    CASHIER(
        titleEn = "Cashier",
        titleKh = "អ្នកគិតលុយ (Cashier)",
        defaultUsername = "Cashier",
        fixedPin = "9999",
        descriptionKh = "សិទ្ធិលក់ & របាយការណ៍: លក់ទំនិញ POS និងពិនិត្យរបាយការណ៍លក់"
    );

    companion object {
        fun fromUsername(username: String): UserRole? {
            return values().find { it.defaultUsername.equals(username.trim(), ignoreCase = true) }
        }
    }
}

data class UserSession(
    val username: String,
    val role: UserRole,
    val loginTimestamp: Long = System.currentTimeMillis()
)
