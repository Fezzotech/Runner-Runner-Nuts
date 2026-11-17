package com.example.offsuitpoker.util

object CurrencyFormatter {
    /**
     * Formats an integer or long value into the Indian numbering system:
     * Grouping of last 3 digits, followed by groups of 2 digits (thousands, lakhs, crores).
     * Examples:
     * 1000 -> "1,000"
     * 10000 -> "10,000"
     * 100000 -> "1,00,000"
     * 1000000 -> "10,00,000"
     * 10000000 -> "1,00,00,000"
     */
    fun formatIndian(amount: Long): String {
        val isNegative = amount < 0
        val s = kotlin.math.abs(amount).toString()
        if (s.length <= 3) {
            return (if (isNegative) "-" else "") + s
        }
        val last3 = s.takeLast(3)
        var remaining = s.dropLast(3)
        val groups = mutableListOf<String>()
        while (remaining.length > 2) {
            groups.add(0, remaining.takeLast(2))
            remaining = remaining.dropLast(2)
        }
        if (remaining.isNotEmpty()) {
            groups.add(0, remaining)
        }
        val result = groups.joinToString(",") + "," + last3
        return (if (isNegative) "-" else "") + result
    }

    fun formatIndian(amount: Int): String = formatIndian(amount.toLong())

    /**
     * Formats currency with the Rupee symbol and Indian grouping: e.g. "₹10,000", "₹1,00,000"
     */
    fun formatChips(amount: Long): String = "₹${formatIndian(amount)}"
    fun formatChips(amount: Int): String = "₹${formatIndian(amount)}"
}
