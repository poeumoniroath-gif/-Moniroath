package com.example.model

enum class PaymentMethod(
    val code: String,
    val nameKh: String,
    val nameEn: String,
    val iconEmoji: String,
    val colorHex: Long
) {
    CASH(
        code = "CASH",
        nameKh = "សាច់ប្រាក់",
        nameEn = "Cash",
        iconEmoji = "💵",
        colorHex = 0xFF059669
    ),
    ABA(
        code = "ABA",
        nameKh = "ABA Pay",
        nameEn = "ABA Transfer",
        iconEmoji = "📲",
        colorHex = 0xFF0284C7
    );

    companion object {
        fun fromCode(code: String?): PaymentMethod {
            return when (code?.uppercase()) {
                "ABA" -> ABA
                else -> CASH
            }
        }
    }
}
