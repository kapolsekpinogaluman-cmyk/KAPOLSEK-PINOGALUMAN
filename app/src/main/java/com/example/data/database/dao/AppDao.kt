package com.example.data.database.dao

import androidx.room.*
import com.example.data.database.entity.*
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM users ORDER BY id ASC")
    fun getAllUsers(): Flow<List<UserEntity>>

    @Query("""
        SELECT * FROM users 
        WHERE (LOWER(username) = LOWER(:identifier) 
           OR LOWER(email) = LOWER(:identifier) 
           OR REPLACE(REPLACE(REPLACE(phoneWhatsapp, '-', ''), ' ', ''), '+62', '0') = :cleanPhone
           OR phoneWhatsapp = :identifier)
          AND password = :password 
          AND isActive = 1 
        LIMIT 1
    """)
    suspend fun loginByIdentifier(identifier: String, cleanPhone: String, password: String): UserEntity?

    @Query("SELECT * FROM users WHERE username = :username AND password = :password AND isActive = 1 LIMIT 1")
    suspend fun login(username: String, password: String): UserEntity?

    @Query("SELECT * FROM users WHERE username = :username LIMIT 1")
    suspend fun getUserByUsername(username: String): UserEntity?

    @Query("SELECT COUNT(*) FROM users")
    suspend fun getUserCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity): Long

    @Update
    suspend fun updateUser(user: UserEntity)

    @Delete
    suspend fun deleteUser(user: UserEntity)
}

@Dao
interface GoldRateDao {
    @Query("SELECT * FROM gold_rates ORDER BY id ASC")
    fun getAllRates(): Flow<List<GoldRateEntity>>

    @Query("SELECT * FROM gold_rates WHERE isActive = 1 ORDER BY id ASC")
    fun getActiveRates(): Flow<List<GoldRateEntity>>

    @Query("SELECT * FROM gold_rates WHERE kadarName = :kadarName LIMIT 1")
    suspend fun getRateByKadar(kadarName: String): GoldRateEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRate(rate: GoldRateEntity): Long

    @Update
    suspend fun updateRate(rate: GoldRateEntity)

    @Query("SELECT * FROM price_history ORDER BY changeDate DESC")
    fun getPriceHistory(): Flow<List<PriceHistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPriceHistory(history: PriceHistoryEntity)
}

@Dao
interface ProcessingBatchDao {
    @Query("SELECT * FROM processing_batches ORDER BY createdAt DESC")
    fun getAllBatches(): Flow<List<ProcessingBatchEntity>>

    @Query("SELECT * FROM processing_batches WHERE batchNumber = :batchNumber LIMIT 1")
    suspend fun getBatchByNumber(batchNumber: String): ProcessingBatchEntity?

    @Query("SELECT * FROM processing_batches WHERE status = 'PROSES' ORDER BY startDate DESC")
    fun getActiveBatches(): Flow<List<ProcessingBatchEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBatch(batch: ProcessingBatchEntity)

    @Update
    suspend fun updateBatch(batch: ProcessingBatchEntity)

    @Query("DELETE FROM processing_batches WHERE batchNumber = :batchNumber")
    suspend fun deleteBatch(batchNumber: String)
}

@Dao
interface CapitalTransactionDao {
    @Query("SELECT * FROM capital_transactions ORDER BY transactionDate DESC, createdAt DESC")
    fun getAllCapitalTransactions(): Flow<List<CapitalTransactionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCapitalTransaction(tx: CapitalTransactionEntity)

    @Query("DELETE FROM capital_transactions WHERE transactionId = :transactionId")
    suspend fun deleteCapitalTransaction(transactionId: String)

    @Query("DELETE FROM capital_transactions WHERE relatedIntakeId = :intakeId")
    suspend fun deleteByIntakeId(intakeId: String)

    @Query("DELETE FROM capital_transactions WHERE relatedSalesId = :salesId")
    suspend fun deleteBySalesId(salesId: String)
}

@Dao
interface GoldIntakeDao {
    @Query("SELECT * FROM gold_intakes ORDER BY transactionDate DESC, createdAt DESC")
    fun getAllIntakes(): Flow<List<GoldIntakeEntity>>

    @Query("SELECT * FROM gold_intakes WHERE intakeId = :intakeId LIMIT 1")
    suspend fun getIntakeById(intakeId: String): GoldIntakeEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertIntake(intake: GoldIntakeEntity)

    @Query("DELETE FROM gold_intakes WHERE intakeId = :intakeId")
    suspend fun deleteIntake(intakeId: String)
}

@Dao
interface GoldInventoryDao {
    @Query("SELECT * FROM gold_inventory ORDER BY entryDate DESC")
    fun getAllInventory(): Flow<List<GoldInventoryEntity>>

    @Query("SELECT * FROM gold_inventory WHERE availableWeightGrams > 0 AND status IN ('TERSEDIA', 'SEBAGIAN_TERJUAL') ORDER BY entryDate DESC")
    fun getAvailableInventory(): Flow<List<GoldInventoryEntity>>

    @Query("SELECT * FROM gold_inventory WHERE inventoryId = :id LIMIT 1")
    suspend fun getInventoryById(id: String): GoldInventoryEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInventory(item: GoldInventoryEntity)

    @Update
    suspend fun updateInventory(item: GoldInventoryEntity)

    @Query("DELETE FROM gold_inventory WHERE inventoryId = :id")
    suspend fun deleteInventory(id: String)
}

@Dao
interface GoldSalesDao {
    @Query("SELECT * FROM gold_sales ORDER BY transactionDate DESC, createdAt DESC")
    fun getAllSales(): Flow<List<GoldSalesEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSales(sales: GoldSalesEntity)

    @Query("DELETE FROM gold_sales WHERE salesId = :salesId")
    suspend fun deleteSales(salesId: String)
}

@Dao
interface ExpenseDao {
    @Query("SELECT * FROM expenses ORDER BY expenseDate DESC")
    fun getAllExpenses(): Flow<List<ExpenseEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpense(expense: ExpenseEntity)

    @Delete
    suspend fun deleteExpense(expense: ExpenseEntity)
}

@Dao
interface ActivityLogDao {
    @Query("SELECT * FROM activity_logs ORDER BY timestamp DESC LIMIT 500")
    fun getAllLogs(): Flow<List<ActivityLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: ActivityLogEntity)
}

@Dao
interface AuditLogDao {
    @Query("SELECT * FROM audit_logs ORDER BY modifiedAt DESC LIMIT 500")
    fun getAllAuditLogs(): Flow<List<AuditLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAuditLog(audit: AuditLogEntity)
}

@Dao
interface BusinessSettingDao {
    @Query("SELECT * FROM business_settings")
    fun getAllSettings(): Flow<List<BusinessSettingEntity>>

    @Query("SELECT value FROM business_settings WHERE `key` = :key LIMIT 1")
    suspend fun getSettingValue(key: String): String?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSetting(setting: BusinessSettingEntity)
}
