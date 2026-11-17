package com.example.offsuitpoker.util

import com.example.offsuitpoker.data.PlayerProfile

object CurrencyFormatter {
    var currencySymbol: String = "₹"
    var useIndianNumbering: Boolean = true

    fun initFromProfile(profile: PlayerProfile) {
        currencySymbol = profile.currencySymbol
        useIndianNumbering = profile.useIndianNumbering
    }

    fun setCurrency(symbol: String, isIndian: Boolean) {
        currencySymbol = symbol
        useIndianNumbering = isIndian
    }

    /**
     * Formats number in Western 3-digit comma grouping:
     * Examples: 1,000 | 10,000 | 100,000 | 1,000,000
     */
    fun formatWestern(amount: Long): String {
        val isNegative = amount < 0
        val s = kotlin.math.abs(amount).toString()
        if (s.length <= 3) return (if (isNegative) "-" else "") + s
        val sb = StringBuilder()
        var count = 0
        for (i in s.length - 1 downTo 0) {
            sb.append(s[i])
            count++
            if (count % 3 == 0 && i > 0) {
                sb.append(',')
            }
        }
        val formatted = sb.reverse().toString()
        return (if (isNegative) "-" else "") + formatted
    }

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
    fun formatWestern(amount: Int): String = formatWestern(amount.toLong())

    fun formatNumber(amount: Long, indianFormat: Boolean = useIndianNumbering): String {
        return if (indianFormat) formatIndian(amount) else formatWestern(amount)
    }

    fun formatNumber(amount: Int, indianFormat: Boolean = useIndianNumbering): String {
        return formatNumber(amount.toLong(), indianFormat)
    }

    /**
     * Formats currency with the active symbol and formatting: e.g. "₹10,000", "$10,000", "10,000 Chips"
     */
    fun formatChips(amount: Long): String {
        val formattedNum = formatNumber(amount)
        return when {
            currencySymbol.isEmpty() -> formattedNum
            currencySymbol.length > 2 && currencySymbol.all { it.isLetter() } -> "$formattedNum $currencySymbol"
            else -> "$currencySymbol$formattedNum"
        }
    }

    fun formatChips(amount: Int): String = formatChips(amount.toLong())
}
