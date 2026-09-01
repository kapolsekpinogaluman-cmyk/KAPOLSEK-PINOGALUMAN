package com.example.utils

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.random.Random

object IdGenerator {

    private fun getDatePart(): String {
        val sdf = SimpleDateFormat("yyyyMMdd", Locale.US)
        return sdf.format(Date())
    }

    private fun getRandomSequence(): String {
        val num = Random.nextInt(1000, 9999)
        return num.toString()
    }

    fun generateCapitalTxId(): String {
        return "MOD-${getDatePart()}-${getRandomSequence()}"
    }

    fun generateIntakeId(): String {
        return "EMAS-${getDatePart()}-${getRandomSequence()}"
    }

    fun generateSalesId(): String {
        return "JUAL-${getDatePart()}-${getRandomSequence()}"
    }

    fun generateBatchId(): String {
        return "TRM-${getDatePart()}-${getRandomSequence()}"
    }

    fun generateInventoryId(): String {
        return "INV-${getDatePart()}-${getRandomSequence()}"
    }
}
