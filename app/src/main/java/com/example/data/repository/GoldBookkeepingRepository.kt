package com.example.data.repository

import com.example.data.database.AppDatabase
import com.example.data.database.entity.*
import com.example.data.model.ActionType
import com.example.data.model.CapitalTransactionType
import com.example.data.model.InventoryStatus
import com.example.utils.IdGenerator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class GoldBookkeepingRepository(private val database: AppDatabase) {

    // User operations
    val allUsers: Flow<List<UserEntity>> = database.userDao().getAllUsers()

    suspend fun login(username: String, password: String): UserEntity? = withContext(Dispatchers.IO) {
        database.userDao().login(username, password)
    }

    suspend fun loginByIdentifier(identifier: String, password: String): UserEntity? = withContext(Dispatchers.IO) {
        val trimmed = identifier.trim()
        val cleanPhone = trimmed
            .replace("-", "")
            .replace(" ", "")
            .let { if (it.startsWith("+62")) "0" + it.removePrefix("+62") else it }
        database.userDao().loginByIdentifier(trimmed, cleanPhone, password.trim())
    }

    suspend fun insertUser(user: UserEntity): Long = withContext(Dispatchers.IO) {
        val id = database.userDao().insertUser(user)
        logActivity(user.username, user.role.name, ActionType.MANAJEMEN_PENGGUNA.name, null, "Menambahkan pengguna baru: ${user.username} (${user.role.name})")
        id
    }

    suspend fun updateUser(user: UserEntity, adminUser: String) = withContext(Dispatchers.IO) {
        database.userDao().updateUser(user)
        logActivity(adminUser, "ADMIN", ActionType.MANAJEMEN_PENGGUNA.name, null, "Memperbarui data pengguna: ${user.username}")
    }

    suspend fun deleteUser(user: UserEntity, adminUser: String) = withContext(Dispatchers.IO) {
        database.userDao().deleteUser(user)
        logActivity(adminUser, "ADMIN", ActionType.MANAJEMEN_PENGGUNA.name, null, "Menghapus pengguna: ${user.username}")
    }

    // Gold Rate Master operations
    val allRates: Flow<List<GoldRateEntity>> = database.goldRateDao().getAllRates()
    val activeRates: Flow<List<GoldRateEntity>> = database.goldRateDao().getActiveRates()
    val priceHistory: Flow<List<PriceHistoryEntity>> = database.goldRateDao().getPriceHistory()

    suspend fun getRateByKadar(kadarName: String): GoldRateEntity? = withContext(Dispatchers.IO) {
        database.goldRateDao().getRateByKadar(kadarName)
    }

    suspend fun insertRate(rate: GoldRateEntity, actor: String) = withContext(Dispatchers.IO) {
        database.goldRateDao().insertRate(rate)
        logActivity(actor, "ADMIN", ActionType.TAMBAH_KADAR.name, null, "Menambah master kadar: ${rate.kadarName} Beli=${rate.buyPricePerGram}, Jual=${rate.sellPricePerGram}")
    }

    suspend fun updateRate(oldRate: GoldRateEntity, newRate: GoldRateEntity, actor: String, reason: String = "") = withContext(Dispatchers.IO) {
        database.goldRateDao().updateRate(newRate)
        if (oldRate.buyPricePerGram != newRate.buyPricePerGram || oldRate.sellPricePerGram != newRate.sellPricePerGram) {
            database.goldRateDao().insertPriceHistory(
                PriceHistoryEntity(
                    kadarName = newRate.kadarName,
                    oldBuyPrice = oldRate.buyPricePerGram,
                    newBuyPrice = newRate.buyPricePerGram,
                    oldSellPrice = oldRate.sellPricePerGram,
                    newSellPrice = newRate.sellPricePerGram,
                    changedBy = actor,
                    changeDate = System.currentTimeMillis(),
                    reason = reason
                )
            )
            logActivity(actor, "ADMIN", ActionType.UBAH_HARGA_KADAR.name, null, "Mengubah harga kadar ${newRate.kadarName}: Beli ${oldRate.buyPricePerGram}->${newRate.buyPricePerGram}, Jual ${oldRate.sellPricePerGram}->${newRate.sellPricePerGram}. Alasan: $reason")
        }
    }

    // Processing Batch (Tromol) operations
    val allBatches: Flow<List<ProcessingBatchEntity>> = database.processingBatchDao().getAllBatches()
    val activeBatches: Flow<List<ProcessingBatchEntity>> = database.processingBatchDao().getActiveBatches()

    suspend fun insertBatch(batch: ProcessingBatchEntity, actor: String) = withContext(Dispatchers.IO) {
        database.processingBatchDao().insertBatch(batch)
        logActivity(actor, "USER", ActionType.TAMBAH_TROMOL.name, batch.batchNumber, "Menambahkan pengolahan tromol: ${batch.batchNumber} - ${batch.batchName} (Biaya: ${batch.processingFee})")
    }

    suspend fun updateBatch(batch: ProcessingBatchEntity, actor: String) = withContext(Dispatchers.IO) {
        database.processingBatchDao().updateBatch(batch)
        logActivity(actor, "USER", ActionType.UBAH_TROMOL.name, batch.batchNumber, "Memperbarui tromol ${batch.batchNumber} status=${batch.status}")
    }

    suspend fun deleteBatch(batchNumber: String, actor: String) = withContext(Dispatchers.IO) {
        database.processingBatchDao().deleteBatch(batchNumber)
        logActivity(actor, "OWNER", ActionType.HAPUS_TRANSAKSI.name, batchNumber, "Menghapus tromol: $batchNumber")
    }

    // Capital Transactions operations
    val allCapitalTransactions: Flow<List<CapitalTransactionEntity>> = database.capitalTransactionDao().getAllCapitalTransactions()

    suspend fun addCapital(
        amount: Double,
        source: String,
        notes: String,
        proofUri: String?,
        actor: String,
        date: Long = System.currentTimeMillis()
    ) = withContext(Dispatchers.IO) {
        val txId = IdGenerator.generateCapitalTxId()
        val tx = CapitalTransactionEntity(
            transactionId = txId,
            transactionDate = date,
            amount = amount,
            type = CapitalTransactionType.TAMBAH_MODAL,
            sourceOrTarget = source,
            notes = notes,
            proofUri = proofUri,
            createdBy = actor
        )
        database.capitalTransactionDao().insertCapitalTransaction(tx)
        logActivity(actor, "OWNER", ActionType.TAMBAH_MODAL.name, txId, "Tambah Modal sebesar $amount dari $source ($notes)")
    }

    suspend fun reduceCapital(
        amount: Double,
        target: String,
        notes: String,
        proofUri: String?,
        actor: String,
        date: Long = System.currentTimeMillis()
    ) = withContext(Dispatchers.IO) {
        val txId = IdGenerator.generateCapitalTxId()
        val tx = CapitalTransactionEntity(
            transactionId = txId,
            transactionDate = date,
            amount = amount,
            type = CapitalTransactionType.PENGURANGAN_MODAL,
            sourceOrTarget = target,
            notes = notes,
            proofUri = proofUri,
            createdBy = actor
        )
        database.capitalTransactionDao().insertCapitalTransaction(tx)
        logActivity(actor, "OWNER", ActionType.PENGURANGAN_MODAL.name, txId, "Pengurangan Modal/Prive sebesar $amount untuk $target ($notes)")
    }

    suspend fun deleteCapitalTransaction(transactionId: String, actor: String) = withContext(Dispatchers.IO) {
        database.capitalTransactionDao().deleteCapitalTransaction(transactionId)
        logActivity(actor, "OWNER", ActionType.HAPUS_TRANSAKSI.name, transactionId, "Menghapus transaksi modal: $transactionId")
    }

    // Gold Intake (Pengambilan Emas) operations
    val allIntakes: Flow<List<GoldIntakeEntity>> = database.goldIntakeDao().getAllIntakes()

    suspend fun processGoldIntake(
        date: Long,
        batchNumber: String,
        processingName: String,
        weightGrams: Double,
        kadarName: String,
        pricePerGram: Double,
        processingFee: Double,
        deductionAmount: Double,
        deductionPercentage: Double,
        deductionMethod: String,
        notes: String,
        photoUri: String?,
        actor: String
    ): GoldIntakeEntity = withContext(Dispatchers.IO) {
        val intakeId = IdGenerator.generateIntakeId()
        val inventoryId = IdGenerator.generateInventoryId()
        val grossValue = weightGrams * pricePerGram
        val netValue = grossValue - deductionAmount + processingFee // Net value used/reduced from modal
        val costPerGram = if (weightGrams > 0) netValue / weightGrams else 0.0

        val intake = GoldIntakeEntity(
            intakeId = intakeId,
            transactionDate = date,
            batchNumber = batchNumber,
            processingName = processingName,
            weightGrams = weightGrams,
            kadarName = kadarName,
            pricePerGram = pricePerGram,
            processingFee = processingFee,
            grossValue = grossValue,
            deductionAmount = deductionAmount,
            deductionPercentage = deductionPercentage,
            deductionMethod = deductionMethod,
            netValue = netValue,
            notes = notes,
            photoUri = photoUri,
            inventoryId = inventoryId,
            createdBy = actor
        )

        // 1. Insert Intake
        database.goldIntakeDao().insertIntake(intake)

        // 2. Automatically create Inventory Entry
        val inventory = GoldInventoryEntity(
            inventoryId = inventoryId,
            intakeId = intakeId,
            entryDate = date,
            batchNumber = batchNumber,
            initialWeightGrams = weightGrams,
            availableWeightGrams = weightGrams,
            soldWeightGrams = 0.0,
            kadarName = kadarName,
            costPerGram = costPerGram,
            totalCostValue = netValue,
            status = InventoryStatus.TERSEDIA,
            location = "Brankas Utama",
            notes = "Diterima dari $batchNumber ($notes)"
        )
        database.goldInventoryDao().insertInventory(inventory)

        // 3. Automatically link and deduct Capital
        val capTxId = IdGenerator.generateCapitalTxId()
        val capTx = CapitalTransactionEntity(
            transactionId = capTxId,
            transactionDate = date,
            amount = netValue,
            type = CapitalTransactionType.PENGAMBILAN_EMAS_USAGE,
            sourceOrTarget = "Pengambilan Emas $batchNumber ($kadarName, ${weightGrams}g)",
            notes = "Pengurangan modal penerimaan emas dari $batchNumber. Potongan: $deductionAmount, Fee: $processingFee",
            proofUri = photoUri,
            relatedIntakeId = intakeId,
            createdBy = actor
        )
        database.capitalTransactionDao().insertCapitalTransaction(capTx)

        // 4. Log activity
        logActivity(
            actor,
            "OPERATOR",
            ActionType.PENGAMBILAN_EMAS.name,
            intakeId,
            "Pengambilan Emas $intakeId: Tromol=$batchNumber, Kadar=$kadarName, Berat=${weightGrams}g, Net Nilai=$netValue -> Masuk Inventory $inventoryId & Potong Modal"
        )

        intake
    }

    suspend fun deleteGoldIntake(intakeId: String, actor: String) = withContext(Dispatchers.IO) {
        val intake = database.goldIntakeDao().getIntakeById(intakeId)
        if (intake != null) {
            database.goldIntakeDao().deleteIntake(intakeId)
            database.goldInventoryDao().deleteInventory(intake.inventoryId)
            database.capitalTransactionDao().deleteByIntakeId(intakeId)
            logActivity(actor, "OWNER", ActionType.HAPUS_TRANSAKSI.name, intakeId, "Menghapus pengambilan emas $intakeId beserta inventory dan catatan modal terkait")
        }
    }

    // Gold Inventory operations
    val allInventory: Flow<List<GoldInventoryEntity>> = database.goldInventoryDao().getAllInventory()
    val availableInventory: Flow<List<GoldInventoryEntity>> = database.goldInventoryDao().getAvailableInventory()

    suspend fun updateInventoryStatus(item: GoldInventoryEntity, actor: String) = withContext(Dispatchers.IO) {
        database.goldInventoryDao().updateInventory(item)
        logActivity(actor, "ADMIN", "UPDATE_INVENTORY", item.inventoryId, "Update inventory ${item.inventoryId} status=${item.status}, lokasi=${item.location}")
    }

    // Gold Sales operations
    val allSales: Flow<List<GoldSalesEntity>> = database.goldSalesDao().getAllSales()

    suspend fun processGoldSale(
        date: Long,
        inventoryItem: GoldInventoryEntity,
        weightSold: Double,
        sellPricePerGram: Double,
        relatedExpense: Double,
        buyerName: String,
        notes: String,
        proofUri: String?,
        actor: String
    ): GoldSalesEntity = withContext(Dispatchers.IO) {
        val salesId = IdGenerator.generateSalesId()
        val totalSalesAmount = weightSold * sellPricePerGram
        val costOfGoodsSold = weightSold * inventoryItem.costPerGram
        val profitAmount = totalSalesAmount - costOfGoodsSold - relatedExpense

        val sales = GoldSalesEntity(
            salesId = salesId,
            transactionDate = date,
            inventoryId = inventoryItem.inventoryId,
            intakeId = inventoryItem.intakeId,
            batchNumber = inventoryItem.batchNumber,
            weightSoldGrams = weightSold,
            kadarName = inventoryItem.kadarName,
            sellPricePerGram = sellPricePerGram,
            totalSalesAmount = totalSalesAmount,
            costOfGoodsSold = costOfGoodsSold,
            relatedExpense = relatedExpense,
            profitAmount = profitAmount,
            buyerName = buyerName,
            notes = notes,
            proofUri = proofUri,
            createdBy = actor
        )

        // 1. Insert Sales
        database.goldSalesDao().insertSales(sales)

        // 2. Update Inventory (Supports partial sales)
        val newAvailableWeight = (inventoryItem.availableWeightGrams - weightSold).coerceAtLeast(0.0)
        val newSoldWeight = inventoryItem.soldWeightGrams + weightSold
        val newStatus = if (newAvailableWeight <= 0.001) InventoryStatus.TERJUAL else InventoryStatus.SEBAGIAN_TERJUAL

        val updatedInventory = inventoryItem.copy(
            availableWeightGrams = newAvailableWeight,
            soldWeightGrams = newSoldWeight,
            status = newStatus,
            updatedAt = System.currentTimeMillis()
        )
        database.goldInventoryDao().updateInventory(updatedInventory)

        // 3. Link Capital Transaction (Revenue coming back into cash / capital)
        val capTxId = IdGenerator.generateCapitalTxId()
        val capTx = CapitalTransactionEntity(
            transactionId = capTxId,
            transactionDate = date,
            amount = totalSalesAmount,
            type = CapitalTransactionType.PENJUALAN_EMAS_REVENUE,
            sourceOrTarget = "Penjualan Emas ${inventoryItem.kadarName} ($buyerName)",
            notes = "Penerimaan kas dari penjualan ${weightSold}g emas (${salesId}). Laba: $profitAmount",
            proofUri = proofUri,
            relatedSalesId = salesId,
            createdBy = actor
        )
        database.capitalTransactionDao().insertCapitalTransaction(capTx)

        // 4. Log activity
        val isPartial = newAvailableWeight > 0.001
        val actionType = if (isPartial) ActionType.PENJUALAN_SEBAGIAN.name else ActionType.PENJUALAN_EMAS.name
        logActivity(
            actor,
            "OPERATOR",
            actionType,
            salesId,
            "Penjualan Emas $salesId: Kadar=${inventoryItem.kadarName}, Berat=${weightSold}g (Sisa ${newAvailableWeight}g), Harga=$sellPricePerGram, Total=$totalSalesAmount, Laba=$profitAmount, Pembeli=$buyerName"
        )

        sales
    }

    suspend fun deleteGoldSale(salesId: String, actor: String) = withContext(Dispatchers.IO) {
        database.goldSalesDao().deleteSales(salesId)
        database.capitalTransactionDao().deleteBySalesId(salesId)
        logActivity(actor, "OWNER", ActionType.HAPUS_TRANSAKSI.name, salesId, "Menghapus transaksi penjualan $salesId")
    }

    // Expense operations
    val allExpenses: Flow<List<ExpenseEntity>> = database.expenseDao().getAllExpenses()

    suspend fun insertExpense(expense: ExpenseEntity, actor: String) = withContext(Dispatchers.IO) {
        database.expenseDao().insertExpense(expense)
        logActivity(actor, "ADMIN", "TAMBAH_BIAYA", null, "Menambah biaya operasional ${expense.category}: ${expense.amount} (${expense.description})")
    }

    suspend fun deleteExpense(expense: ExpenseEntity, actor: String) = withContext(Dispatchers.IO) {
        database.expenseDao().deleteExpense(expense)
        logActivity(actor, "ADMIN", "HAPUS_BIAYA", null, "Menghapus biaya: ${expense.amount} (${expense.description})")
    }

    // Activity & Audit Logs
    val allActivityLogs: Flow<List<ActivityLogEntity>> = database.activityLogDao().getAllLogs()
    val allAuditLogs: Flow<List<AuditLogEntity>> = database.auditLogDao().getAllAuditLogs()

    suspend fun logActivity(
        username: String,
        role: String,
        action: String,
        txId: String?,
        details: String,
        oldData: String? = null,
        newData: String? = null
    ) = withContext(Dispatchers.IO) {
        database.activityLogDao().insertLog(
            ActivityLogEntity(
                username = username,
                userRole = role,
                actionType = action,
                transactionId = txId,
                details = details,
                oldDataJson = oldData,
                newDataJson = newData
            )
        )
    }

    // Settings
    val allSettings: Flow<List<BusinessSettingEntity>> = database.businessSettingDao().getAllSettings()

    suspend fun saveSetting(key: String, value: String, category: String = "GENERAL", actor: String) = withContext(Dispatchers.IO) {
        database.businessSettingDao().insertSetting(
            BusinessSettingEntity(key = key, value = value, category = category, updatedAt = System.currentTimeMillis())
        )
        logActivity(actor, "OWNER", ActionType.UBAH_PENGATURAN.name, null, "Mengubah pengaturan $key = $value")
    }
}
