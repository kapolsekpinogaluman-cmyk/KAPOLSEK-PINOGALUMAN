package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.database.entity.*
import com.example.data.model.*
import com.example.data.repository.GoldBookkeepingRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Calendar

data class DashboardSummary(
    val currentModalBalance: Double = 0.0,
    val totalTambahModal: Double = 0.0,
    val totalPenguranganModal: Double = 0.0,
    val totalIntakesNetValue: Double = 0.0,
    val totalInventoryValue: Double = 0.0,
    val totalInventoryWeight: Double = 0.0,
    val totalSalesRevenue: Double = 0.0,
    val totalCogs: Double = 0.0,
    val totalExpenses: Double = 0.0,
    val totalNetProfit: Double = 0.0,
    val totalProcessingFees: Double = 0.0,
    val totalBatchCount: Int = 0,
    val totalTransactionCount: Int = 0,
    val intakeCount: Int = 0,
    val salesCount: Int = 0,
    val totalSoldWeight: Double = 0.0
)

data class ConfirmationDialogData(
    val title: String,
    val transactionType: String,
    val batchNumber: String? = null,
    val weightGrams: Double = 0.0,
    val kadarName: String = "",
    val pricePerGram: Double = 0.0,
    val grossValue: Double = 0.0,
    val deductionAmount: Double = 0.0,
    val netValue: Double = 0.0,
    val currentBalance: Double = 0.0,
    val balanceAfter: Double = 0.0,
    val details: Map<String, String> = emptyMap(),
    val onConfirm: () -> Unit
)

private data class RawDataSnapshot(
    val capTxs: List<CapitalTransactionEntity> = emptyList(),
    val intakes: List<GoldIntakeEntity> = emptyList(),
    val inventory: List<GoldInventoryEntity> = emptyList(),
    val sales: List<GoldSalesEntity> = emptyList(),
    val batches: List<ProcessingBatchEntity> = emptyList(),
    val expenses: List<ExpenseEntity> = emptyList()
)

class MainViewModel(private val repository: GoldBookkeepingRepository) : ViewModel() {

    // Filter states
    private val _timeFilter = MutableStateFlow(TimeFilter.ALL_TIME)
    val timeFilter: StateFlow<TimeFilter> = _timeFilter.asStateFlow()

    private val _customStartDate = MutableStateFlow<Long?>(null)
    val customStartDate: StateFlow<Long?> = _customStartDate.asStateFlow()

    private val _customEndDate = MutableStateFlow<Long?>(null)
    val customEndDate: StateFlow<Long?> = _customEndDate.asStateFlow()

    // Confirmation dialog state
    private val _confirmDialogData = MutableStateFlow<ConfirmationDialogData?>(null)
    val confirmationDialogData: StateFlow<ConfirmationDialogData?> = _confirmDialogData.asStateFlow()

    // Message / Toast alert
    private val _userMessage = MutableStateFlow<String?>(null)
    val userMessage: StateFlow<String?> = _userMessage.asStateFlow()

    // Raw flows from DB
    val allCapitalTransactions = repository.allCapitalTransactions
    val allIntakes = repository.allIntakes
    val allInventory = repository.allInventory
    val availableInventory = repository.availableInventory
    val allSales = repository.allSales
    val allBatches = repository.allBatches
    val activeBatches = repository.activeBatches
    val allRates = repository.allRates
    val activeRates = repository.activeRates
    val priceHistory = repository.priceHistory
    val allExpenses = repository.allExpenses
    val allLogs = repository.allActivityLogs
    val allAuditLogs = repository.allAuditLogs
    val allUsers = repository.allUsers
    val allSettings = repository.allSettings

    // Staged Combine for raw entities
    private val rawDataFlow: Flow<RawDataSnapshot> = combine(
        allCapitalTransactions,
        allIntakes,
        allInventory,
        allSales
    ) { capTxs, intakes, inv, sales ->
        RawDataSnapshot(capTxs = capTxs, intakes = intakes, inventory = inv, sales = sales)
    }.combine(allBatches) { snap, batches ->
        snap.copy(batches = batches)
    }.combine(allExpenses) { snap, expenses ->
        snap.copy(expenses = expenses)
    }

    // Final Combined Dashboard Calculation
    val dashboardSummary: StateFlow<DashboardSummary> = combine(
        rawDataFlow,
        _timeFilter,
        _customStartDate,
        _customEndDate
    ) { snap, filter, customStart, customEnd ->
        computeDashboard(
            snap.capTxs,
            snap.intakes,
            snap.inventory,
            snap.sales,
            snap.batches,
            snap.expenses,
            filter,
            customStart,
            customEnd
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DashboardSummary())

    private fun computeDashboard(
        capTxs: List<CapitalTransactionEntity>,
        intakes: List<GoldIntakeEntity>,
        inventory: List<GoldInventoryEntity>,
        sales: List<GoldSalesEntity>,
        batches: List<ProcessingBatchEntity>,
        expenses: List<ExpenseEntity>,
        filter: TimeFilter,
        customStart: Long?,
        customEnd: Long?
    ): DashboardSummary {
        // Overall current modal ledger calculation
        var ledgerModal = 0.0
        for (tx in capTxs) {
            when (tx.type) {
                CapitalTransactionType.TAMBAH_MODAL -> ledgerModal += tx.amount
                CapitalTransactionType.PENJUALAN_EMAS_REVENUE -> ledgerModal += tx.amount
                CapitalTransactionType.PENGURANGAN_MODAL -> ledgerModal -= tx.amount
                CapitalTransactionType.PENGAMBILAN_EMAS_USAGE -> ledgerModal -= tx.amount
            }
        }
        for (exp in expenses) {
            ledgerModal -= exp.amount
        }

        // Time filtering for period statistics
        val (startTime, endTime) = getTimeBounds(filter, customStart, customEnd)

        val filteredCapTxs = if (startTime == null) capTxs else capTxs.filter { it.transactionDate in startTime..endTime!! }
        val filteredIntakes = if (startTime == null) intakes else intakes.filter { it.transactionDate in startTime..endTime!! }
        val filteredSales = if (startTime == null) sales else sales.filter { it.transactionDate in startTime..endTime!! }
        val filteredBatches = if (startTime == null) batches else batches.filter { it.startDate in startTime..endTime!! }
        val filteredExpenses = if (startTime == null) expenses else expenses.filter { it.expenseDate in startTime..endTime!! }

        val tambahModalSum = filteredCapTxs.filter { it.type == CapitalTransactionType.TAMBAH_MODAL }.sumOf { it.amount }
        val kurangModalSum = filteredCapTxs.filter { it.type == CapitalTransactionType.PENGURANGAN_MODAL }.sumOf { it.amount }
        val totalIntakesNet = filteredIntakes.sumOf { it.netValue }
        val salesRevenueSum = filteredSales.sumOf { it.totalSalesAmount }
        val totalCogsSum = filteredSales.sumOf { it.costOfGoodsSold }
        val grossProfitSum = filteredSales.sumOf { it.profitAmount }
        val expensesSum = filteredExpenses.sumOf { it.amount }
        val netProfit = grossProfitSum - expensesSum

        val totalProcessingFees = filteredBatches.sumOf { it.processingFee }

        // Current Inventory totals
        val availableInv = inventory.filter { it.availableWeightGrams > 0.0001 }
        val totalInventoryWeight = availableInv.sumOf { it.availableWeightGrams }
        val totalInventoryValue = availableInv.sumOf { it.availableWeightGrams * it.costPerGram }
        val totalSoldWeight = filteredSales.sumOf { it.weightSoldGrams }

        val txCount = filteredCapTxs.size + filteredIntakes.size + filteredSales.size

        return DashboardSummary(
            currentModalBalance = ledgerModal,
            totalTambahModal = tambahModalSum,
            totalPenguranganModal = kurangModalSum,
            totalIntakesNetValue = totalIntakesNet,
            totalInventoryValue = totalInventoryValue,
            totalInventoryWeight = totalInventoryWeight,
            totalSalesRevenue = salesRevenueSum,
            totalCogs = totalCogsSum,
            totalExpenses = expensesSum,
            totalNetProfit = netProfit,
            totalProcessingFees = totalProcessingFees,
            totalBatchCount = filteredBatches.size,
            totalTransactionCount = txCount,
            intakeCount = filteredIntakes.size,
            salesCount = filteredSales.size,
            totalSoldWeight = totalSoldWeight
        )
    }

    private fun getTimeBounds(filter: TimeFilter, customStart: Long?, customEnd: Long?): Pair<Long?, Long?> {
        val cal = Calendar.getInstance()
        return when (filter) {
            TimeFilter.ALL_TIME -> Pair(null, null)
            TimeFilter.TODAY -> {
                cal.set(Calendar.HOUR_OF_DAY, 0)
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0)
                cal.set(Calendar.MILLISECOND, 0)
                val start = cal.timeInMillis
                cal.set(Calendar.HOUR_OF_DAY, 23)
                cal.set(Calendar.MINUTE, 59)
                cal.set(Calendar.SECOND, 59)
                val end = cal.timeInMillis
                Pair(start, end)
            }
            TimeFilter.THIS_WEEK -> {
                cal.set(Calendar.DAY_OF_WEEK, cal.firstDayOfWeek)
                cal.set(Calendar.HOUR_OF_DAY, 0)
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0)
                val start = cal.timeInMillis
                cal.add(Calendar.DAY_OF_WEEK, 6)
                cal.set(Calendar.HOUR_OF_DAY, 23)
                cal.set(Calendar.MINUTE, 59)
                cal.set(Calendar.SECOND, 59)
                val end = cal.timeInMillis
                Pair(start, end)
            }
            TimeFilter.THIS_MONTH -> {
                cal.set(Calendar.DAY_OF_MONTH, 1)
                cal.set(Calendar.HOUR_OF_DAY, 0)
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0)
                val start = cal.timeInMillis
                cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH))
                cal.set(Calendar.HOUR_OF_DAY, 23)
                cal.set(Calendar.MINUTE, 59)
                cal.set(Calendar.SECOND, 59)
                val end = cal.timeInMillis
                Pair(start, end)
            }
            TimeFilter.THIS_YEAR -> {
                cal.set(Calendar.DAY_OF_YEAR, 1)
                cal.set(Calendar.HOUR_OF_DAY, 0)
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0)
                val start = cal.timeInMillis
                cal.set(Calendar.DAY_OF_YEAR, cal.getActualMaximum(Calendar.DAY_OF_YEAR))
                cal.set(Calendar.HOUR_OF_DAY, 23)
                cal.set(Calendar.MINUTE, 59)
                cal.set(Calendar.SECOND, 59)
                val end = cal.timeInMillis
                Pair(start, end)
            }
            TimeFilter.CUSTOM -> {
                Pair(customStart ?: 0L, customEnd ?: System.currentTimeMillis())
            }
        }
    }

    fun setTimeFilter(filter: TimeFilter, customStart: Long? = null, customEnd: Long? = null) {
        _timeFilter.value = filter
        _customStartDate.value = customStart
        _customEndDate.value = customEnd
    }

    fun showConfirmation(data: ConfirmationDialogData) {
        _confirmDialogData.value = data
    }

    fun dismissConfirmation() {
        _confirmDialogData.value = null
    }

    fun showMessage(msg: String) {
        _userMessage.value = msg
    }

    fun clearMessage() {
        _userMessage.value = null
    }

    // Capital operations
    fun addCapital(amount: Double, source: String, notes: String, proofUri: String?, actor: String, date: Long) {
        if (amount <= 0) {
            showMessage("Nominal modal harus lebih dari 0.")
            return
        }
        viewModelScope.launch {
            repository.addCapital(amount, source, notes, proofUri, actor, date)
            showMessage("Tambah modal sebesar Rp $amount berhasil disimpan.")
        }
    }

    fun reduceCapital(amount: Double, target: String, notes: String, proofUri: String?, actor: String, date: Long) {
        if (amount <= 0) {
            showMessage("Nominal pengurangan modal harus lebih dari 0.")
            return
        }
        viewModelScope.launch {
            repository.reduceCapital(amount, target, notes, proofUri, actor, date)
            showMessage("Pengurangan modal sebesar Rp $amount berhasil dicatat.")
        }
    }

    fun deleteCapitalTransaction(id: String, actor: String) {
        viewModelScope.launch {
            repository.deleteCapitalTransaction(id, actor)
            showMessage("Transaksi modal $id berhasil dihapus.")
        }
    }

    // Tromol / Processing Batch operations
    fun addBatch(
        batchNumber: String,
        batchName: String,
        managerName: String,
        startDate: Long,
        endDate: Long?,
        rawWeight: Double,
        processingFee: Double,
        notes: String,
        status: String,
        actor: String
    ) {
        if (batchNumber.isBlank() || batchName.isBlank()) {
            showMessage("Nomor dan nama tromol wajib diisi.")
            return
        }
        if (processingFee < 0) {
            showMessage("Biaya pengolahan tidak boleh negatif.")
            return
        }
        viewModelScope.launch {
            val batch = ProcessingBatchEntity(
                batchNumber = batchNumber.trim(),
                batchName = batchName.trim(),
                managerName = managerName.trim(),
                startDate = startDate,
                endDate = endDate,
                rawMaterialWeight = rawWeight,
                processingFee = processingFee,
                notes = notes.trim(),
                status = status
            )
            repository.insertBatch(batch, actor)
            showMessage("Data tromol $batchNumber berhasil disimpan.")
        }
    }

    fun updateBatch(batch: ProcessingBatchEntity, actor: String) {
        viewModelScope.launch {
            repository.updateBatch(batch, actor)
            showMessage("Tromol ${batch.batchNumber} berhasil diperbarui.")
        }
    }

    fun deleteBatch(batchNumber: String, actor: String) {
        viewModelScope.launch {
            repository.deleteBatch(batchNumber, actor)
            showMessage("Tromol $batchNumber berhasil dihapus.")
        }
    }

    // Master Gold Rate operations
    fun addGoldRate(
        kadarName: String,
        buyPrice: Double,
        sellPrice: Double,
        deductionPercent: Double,
        deductionNominal: Double,
        actor: String
    ) {
        if (kadarName.isBlank()) {
            showMessage("Nama kadar tidak boleh kosong.")
            return
        }
        if (buyPrice <= 0 || sellPrice <= 0) {
            showMessage("Harga beli dan harga jual harus lebih besar dari 0.")
            return
        }
        viewModelScope.launch {
            val rate = GoldRateEntity(
                kadarName = kadarName.trim().uppercase(),
                buyPricePerGram = buyPrice,
                sellPricePerGram = sellPrice,
                defaultDeductionPercent = deductionPercent,
                defaultDeductionNominal = deductionNominal,
                updatedBy = actor
            )
            repository.insertRate(rate, actor)
            showMessage("Master kadar $kadarName berhasil ditambahkan.")
        }
    }

    fun updateGoldRate(
        oldRate: GoldRateEntity,
        buyPrice: Double,
        sellPrice: Double,
        deductionPercent: Double,
        deductionNominal: Double,
        isActive: Boolean,
        actor: String,
        reason: String
    ) {
        if (buyPrice <= 0 || sellPrice <= 0) {
            showMessage("Harga emas tidak boleh bernilai negatif atau nol.")
            return
        }
        viewModelScope.launch {
            val updated = oldRate.copy(
                buyPricePerGram = buyPrice,
                sellPricePerGram = sellPrice,
                defaultDeductionPercent = deductionPercent,
                defaultDeductionNominal = deductionNominal,
                isActive = isActive,
                updatedAt = System.currentTimeMillis(),
                updatedBy = actor
            )
            repository.updateRate(oldRate, updated, actor, reason)
            showMessage("Harga kadar ${oldRate.kadarName} berhasil diperbarui.")
        }
    }

    // Gold Intake (Pengambilan Emas dari Tromol)
    fun processGoldIntake(
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
    ) {
        if (batchNumber.isBlank()) {
            showMessage("Tromol wajib dipilih.")
            return
        }
        if (weightGrams <= 0) {
            showMessage("Berat emas wajib diisi dan harus lebih dari 0.")
            return
        }
        if (pricePerGram <= 0) {
            showMessage("Harga emas belum ditentukan untuk kadar ini.")
            return
        }
        viewModelScope.launch {
            val intake = repository.processGoldIntake(
                date = date,
                batchNumber = batchNumber,
                processingName = processingName,
                weightGrams = weightGrams,
                kadarName = kadarName,
                pricePerGram = pricePerGram,
                processingFee = processingFee,
                deductionAmount = deductionAmount,
                deductionPercentage = deductionPercentage,
                deductionMethod = deductionMethod,
                notes = notes,
                photoUri = photoUri,
                actor = actor
            )
            showMessage("Penerimaan emas ${intake.intakeId} sukses. Emas masuk ke inventory dan modal telah disesuaikan.")
        }
    }

    fun deleteGoldIntake(intakeId: String, actor: String) {
        viewModelScope.launch {
            repository.deleteGoldIntake(intakeId, actor)
            showMessage("Pengambilan emas $intakeId berhasil dibatalkan dan dihapus.")
        }
    }

    // Gold Sales (Penjualan Emas Penuh maupun Sebagian)
    fun processGoldSale(
        date: Long,
        inventoryItem: GoldInventoryEntity,
        weightSold: Double,
        sellPricePerGram: Double,
        relatedExpense: Double,
        buyerName: String,
        notes: String,
        proofUri: String?,
        actor: String
    ) {
        if (weightSold <= 0) {
            showMessage("Berat yang dijual harus lebih besar dari 0 gram.")
            return
        }
        if (weightSold > inventoryItem.availableWeightGrams + 0.0001) {
            showMessage("Stok emas tidak mencukupi. Tersedia: ${inventoryItem.availableWeightGrams} gram.")
            return
        }
        if (sellPricePerGram <= 0) {
            showMessage("Harga jual per gram harus lebih dari 0.")
            return
        }
        if (buyerName.isBlank()) {
            showMessage("Nama pembeli wajib diisi.")
            return
        }

        viewModelScope.launch {
            val sales = repository.processGoldSale(
                date = date,
                inventoryItem = inventoryItem,
                weightSold = weightSold,
                sellPricePerGram = sellPricePerGram,
                relatedExpense = relatedExpense,
                buyerName = buyerName.trim(),
                notes = notes.trim(),
                proofUri = proofUri,
                actor = actor
            )
            showMessage("Penjualan ${sales.salesId} berhasil dicatat. Sisa stok diperbarui.")
        }
    }

    fun deleteGoldSale(salesId: String, actor: String) {
        viewModelScope.launch {
            repository.deleteGoldSale(salesId, actor)
            showMessage("Transaksi penjualan $salesId telah dihapus.")
        }
    }

    // User management
    fun addUser(user: UserEntity) {
        viewModelScope.launch {
            repository.insertUser(user)
            showMessage("Pengguna ${user.username} berhasil dibuat.")
        }
    }

    fun updateUser(user: UserEntity, actor: String) {
        viewModelScope.launch {
            repository.updateUser(user, actor)
            showMessage("Pengguna ${user.username} berhasil diperbarui.")
        }
    }

    fun deleteUser(user: UserEntity, actor: String) {
        viewModelScope.launch {
            repository.deleteUser(user, actor)
            showMessage("Pengguna ${user.username} berhasil dihapus.")
        }
    }

    // Settings
    fun saveSetting(key: String, value: String, actor: String) {
        viewModelScope.launch {
            repository.saveSetting(key, value, "GENERAL", actor)
            showMessage("Pengaturan $key disimpan.")
        }
    }
}
