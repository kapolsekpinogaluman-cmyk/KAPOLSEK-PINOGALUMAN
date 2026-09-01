package com.example.data.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.data.model.UserRole

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val username: String,
    val fullName: String,
    val email: String,
    val phoneWhatsapp: String = "0812-3456-7890",
    val photoUri: String? = null,
    val password: String,
    val role: UserRole,
    val securityType: String = "PIN", // "SIDIK_JARI", "WAJAH", "PIN", "POLA"
    val securityPinOrPattern: String = "1234",
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "gold_rates")
data class GoldRateEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val kadarName: String, // e.g. "24K", "22K", "18K", "16K"
    val buyPricePerGram: Double, // Harga beli / modal acuan
    val sellPricePerGram: Double, // Harga jual acuan
    val defaultDeductionPercent: Double = 0.0, // Potongan %
    val defaultDeductionNominal: Double = 0.0, // Potongan Rp
    val isActive: Boolean = true,
    val updatedAt: Long = System.currentTimeMillis(),
    val updatedBy: String = "SYSTEM"
)

@Entity(tableName = "price_history")
data class PriceHistoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val kadarName: String,
    val oldBuyPrice: Double,
    val newBuyPrice: Double,
    val oldSellPrice: Double,
    val newSellPrice: Double,
    val changedBy: String,
    val changeDate: Long = System.currentTimeMillis(),
    val reason: String = ""
)

@Entity(tableName = "processing_batches")
data class ProcessingBatchEntity(
    @PrimaryKey
    val batchNumber: String, // e.g. TRM-20260901-0001 or TRM-001
    val batchName: String,
    val managerName: String,
    val startDate: Long,
    val endDate: Long? = null,
    val rawMaterialWeight: Double = 0.0, // Gram bahan masuk
    val processingFee: Double, // Biaya pengolahan per tromol (dapat berbeda tiap tromol)
    val notes: String = "",
    val status: String = "PROSES", // PROSES, SELESAI, DIBATALKAN
    val createdAt: Long = System.currentTimeMillis()
)
