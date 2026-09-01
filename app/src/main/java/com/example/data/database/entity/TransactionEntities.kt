package com.example.data.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.data.model.CapitalTransactionType
import com.example.data.model.InventoryStatus

@Entity(tableName = "capital_transactions")
data class CapitalTransactionEntity(
    @PrimaryKey
    val transactionId: String, // e.g. MOD-20260901-0001
    val transactionDate: Long,
    val amount: Double, // nominal uang
    val type: CapitalTransactionType, // TAMBAH_MODAL, PENGURANGAN_MODAL, PENGAMBILAN_EMAS_USAGE, PENJUALAN_EMAS_REVENUE
    val sourceOrTarget: String, // e.g. "Pemilik", "Bank BCA", "Pengolahan Tromol TRM-001", "Pembeli Toko Bintang"
    val notes: String = "",
    val proofUri: String? = null,
    val relatedIntakeId: String? = null,
    val relatedSalesId: String? = null,
    val createdBy: String,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "gold_intakes")
data class GoldIntakeEntity(
    @PrimaryKey
    val intakeId: String, // e.g. EMAS-20260901-0001
    val transactionDate: Long,
    val batchNumber: String, // Tromol ID
    val processingName: String = "",
    val weightGrams: Double, // Berat emas masuk (gram)
    val kadarName: String, // Kadar e.g. "22K"
    val pricePerGram: Double, // Harga emas per gram yg disepakati saat intake
    val processingFee: Double = 0.0, // Biaya pengolahan terkait
    val grossValue: Double, // weightGrams * pricePerGram
    val deductionAmount: Double = 0.0, // Potongan modal nominal
    val deductionPercentage: Double = 0.0, // Potongan modal %
    val deductionMethod: String = "NOMINAL", // "PERSEN", "NOMINAL", "HARGA_BERSIH", "TANPA_POTONGAN"
    val netValue: Double, // Nilai yang dipotong dari modal (Gross - Deduction + processingFee jika dibayar dari modal)
    val notes: String = "",
    val photoUri: String? = null,
    val inventoryId: String, // auto generated inventory id
    val createdBy: String,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "gold_inventory")
data class GoldInventoryEntity(
    @PrimaryKey
    val inventoryId: String, // e.g. INV-20260901-0001
    val intakeId: String, // Reference to gold_intakes
    val entryDate: Long,
    val batchNumber: String, // Tromol asal
    val initialWeightGrams: Double, // Quantity awal (e.g. 10 gram)
    val availableWeightGrams: Double, // Quantity tersisa (e.g. 7 gram)
    val soldWeightGrams: Double = 0.0, // Quantity terjual (e.g. 3 gram)
    val kadarName: String,
    val costPerGram: Double, // Modal dasar per gram
    val totalCostValue: Double, // Total modal
    val status: InventoryStatus = InventoryStatus.TERSEDIA,
    val location: String = "Brankas Utama",
    val notes: String = "",
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "gold_sales")
data class GoldSalesEntity(
    @PrimaryKey
    val salesId: String, // e.g. JUAL-20260901-0001
    val transactionDate: Long,
    val inventoryId: String,
    val intakeId: String,
    val batchNumber: String,
    val weightSoldGrams: Double, // Berat yang dijual (gram)
    val kadarName: String,
    val sellPricePerGram: Double, // Harga jual per gram
    val totalSalesAmount: Double, // weightSoldGrams * sellPricePerGram
    val costOfGoodsSold: Double, // Modal yang terpakai (weightSoldGrams * costPerGram)
    val relatedExpense: Double = 0.0, // Biaya terkait transaksi
    val profitAmount: Double, // totalSalesAmount - costOfGoodsSold - relatedExpense
    val buyerName: String,
    val notes: String = "",
    val proofUri: String? = null,
    val createdBy: String,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "expenses")
data class ExpenseEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val expenseDate: Long,
    val category: String, // e.g. "Bahan Kimia", "Operasional Tromol", "Transportasi", "Lainnya"
    val amount: Double,
    val description: String,
    val relatedBatchNumber: String? = null,
    val createdBy: String,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "activity_logs")
data class ActivityLogEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val username: String,
    val userRole: String,
    val actionType: String,
    val timestamp: Long = System.currentTimeMillis(),
    val transactionId: String? = null,
    val details: String,
    val oldDataJson: String? = null,
    val newDataJson: String? = null
)

@Entity(tableName = "audit_logs")
data class AuditLogEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val tableName: String,
    val recordId: String,
    val action: String, // "INSERT", "UPDATE", "DELETE"
    val oldValue: String? = null,
    val newValue: String? = null,
    val modifiedBy: String,
    val modifiedAt: Long = System.currentTimeMillis(),
    val reason: String = ""
)

@Entity(tableName = "business_settings")
data class BusinessSettingEntity(
    @PrimaryKey
    val key: String,
    val value: String,
    val category: String = "GENERAL",
    val updatedAt: Long = System.currentTimeMillis()
)
