package com.example.utils

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object Formatters {

    private val indonesianLocale = Locale("id", "ID")

    fun formatRupiah(amount: Double): String {
        val symbols = DecimalFormatSymbols(indonesianLocale).apply {
            groupingSeparator = '.'
            decimalSeparator = ','
        }
        val df = DecimalFormat("#,##0", symbols)
        return "Rp " + df.format(amount)
    }

    fun formatGram(weight: Double): String {
        val symbols = DecimalFormatSymbols(indonesianLocale).apply {
            groupingSeparator = '.'
            decimalSeparator = ','
        }
        val df = DecimalFormat("#,##0.##", symbols)
        return df.format(weight) + " gram"
    }

    fun formatNumber(number: Double): String {
        val symbols = DecimalFormatSymbols(indonesianLocale).apply {
            groupingSeparator = '.'
            decimalSeparator = ','
        }
        val df = DecimalFormat("#,##0.##", symbols)
        return df.format(number)
    }

    fun formatDate(timestamp: Long): String {
        val sdf = SimpleDateFormat("dd MMM yyyy", indonesianLocale)
        return sdf.format(Date(timestamp))
    }

    fun formatDateTime(timestamp: Long): String {
        val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", indonesianLocale)
        return sdf.format(Date(timestamp))
    }

    fun formatIsoDate(timestamp: Long): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        return sdf.format(Date(timestamp))
    }

    fun parseAmount(text: String): Double? {
        val clean = text.replace("Rp", "")
            .replace(".", "")
            .replace(",", ".")
            .trim()
        return clean.toDoubleOrNull()
    }

    fun parseWeight(text: String): Double? {
        val clean = text.replace("gram", "")
            .replace("g", "")
            .replace(",", ".")
            .trim()
        return clean.toDoubleOrNull()
    }
}
