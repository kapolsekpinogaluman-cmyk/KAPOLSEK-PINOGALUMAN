package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.database.entity.UserEntity
import com.example.ui.components.EmptyStateView
import com.example.ui.components.SearchAndFilterHeader
import com.example.ui.theme.DangerRed
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.SuccessGreen
import com.example.utils.Formatters
import com.example.viewmodel.MainViewModel

enum class HistoryCategoryTab(val label: String, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
    SEMUA("Semua", Icons.Default.History),
    MODAL("Modal Usaha", Icons.Default.AccountBalanceWallet),
    AMBIL_EMAS("Ambil Emas", Icons.Default.Download),
    JUAL_EMAS("Jual Emas", Icons.Default.PointOfSale)
}

data class UnifiedTransaction(
    val id: String,
    val type: String,
    val date: Long,
    val title: String,
    val subtitle: String,
    val amount: Double,
    val isIncome: Boolean,
    val actor: String,
    val category: HistoryCategoryTab
)

@Composable
fun TransactionHistoryScreen(
    viewModel: MainViewModel,
    currentUser: UserEntity?,
    initialTab: HistoryCategoryTab = HistoryCategoryTab.SEMUA,
    modifier: Modifier = Modifier
) {
    var selectedCategory by remember { mutableStateOf(initialTab) }
    var searchQuery by remember { mutableStateOf("") }

    val allCapital by viewModel.allCapitalTransactions.collectAsStateWithLifecycle(emptyList())
    val allIntakes by viewModel.allIntakes.collectAsStateWithLifecycle(emptyList())
    val allSales by viewModel.allSales.collectAsStateWithLifecycle(emptyList())

    val unifiedList = remember(allCapital, allIntakes, allSales) {
        val list = mutableListOf<UnifiedTransaction>()

        allCapital.forEach { c ->
            val isInc = c.type == com.example.data.model.CapitalTransactionType.TAMBAH_MODAL || c.type == com.example.data.model.CapitalTransactionType.PENJUALAN_EMAS_REVENUE
            list.add(
                UnifiedTransaction(
                    id = c.transactionId,
                    type = c.type.name,
                    date = c.transactionDate,
                    title = if (isInc) "Pemasukan Modal" else "Pengeluaran Modal",
                    subtitle = "${c.sourceOrTarget} ${if (c.notes.isNotBlank()) "• ${c.notes}" else ""}",
                    amount = c.amount,
                    isIncome = isInc,
                    actor = c.createdBy,
                    category = HistoryCategoryTab.MODAL
                )
            )
        }

        allIntakes.forEach { intake ->
            list.add(
                UnifiedTransaction(
                    id = intake.intakeId,
                    type = "PENGAMBILAN_EMAS",
                    date = intake.transactionDate,
                    title = "Ambil Emas (Tromol ${intake.batchNumber})",
                    subtitle = "${intake.kadarName} - ${Formatters.formatGram(intake.weightGrams)} @ ${Formatters.formatRupiah(intake.pricePerGram)}",
                    amount = intake.netValue,
                    isIncome = false,
                    actor = intake.createdBy,
                    category = HistoryCategoryTab.AMBIL_EMAS
                )
            )
        }

        allSales.forEach { sale ->
            list.add(
                UnifiedTransaction(
                    id = sale.salesId,
                    type = "PENJUALAN_EMAS",
                    date = sale.transactionDate,
                    title = "Jual Emas ke ${sale.buyerName}",
                    subtitle = "${sale.kadarName} - ${Formatters.formatGram(sale.weightSoldGrams)} | Laba: ${Formatters.formatRupiah(sale.profitAmount)}",
                    amount = sale.totalSalesAmount,
                    isIncome = true,
                    actor = sale.createdBy,
                    category = HistoryCategoryTab.JUAL_EMAS
                )
            )
        }

        list.sortedByDescending { it.date }
    }

    val filteredList = remember(unifiedList, selectedCategory, searchQuery) {
        val byCat = if (selectedCategory == HistoryCategoryTab.SEMUA) {
            unifiedList
        } else {
            unifiedList.filter { it.category == selectedCategory }
        }

        if (searchQuery.isBlank()) byCat
        else byCat.filter {
            it.id.contains(searchQuery, ignoreCase = true) ||
                    it.title.contains(searchQuery, ignoreCase = true) ||
                    it.subtitle.contains(searchQuery, ignoreCase = true) ||
                    it.actor.contains(searchQuery, ignoreCase = true)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .testTag("screen_transaction_history")
    ) {
        // Sub Menu Filter Tabs (Semua | Modal Usaha | Ambil Emas | Jual Emas)
        ScrollableTabRow(
            selectedTabIndex = selectedCategory.ordinal,
            edgePadding = 0.dp,
            containerColor = Color.Transparent,
            divider = {},
            modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
        ) {
            HistoryCategoryTab.values().forEach { cat ->
                val isSelected = selectedCategory == cat
                Tab(
                    selected = isSelected,
                    onClick = { selectedCategory = cat },
                    text = {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = cat.icon,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = if (isSelected) GoldPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = cat.label,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    },
                    selectedContentColor = GoldPrimary,
                    unselectedContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.testTag("tab_history_${cat.name.lowercase()}")
                )
            }
        }

        SearchAndFilterHeader(
            query = searchQuery,
            onQueryChange = { searchQuery = it },
            placeholder = "Cari nomor transaksi, tromol, pembeli...",
            modifier = Modifier.padding(vertical = 8.dp)
        )

        if (filteredList.isEmpty()) {
            EmptyStateView(
                icon = Icons.Default.History,
                title = "Belum Ada Riwayat ${selectedCategory.label}",
                description = "Seluruh catatan mutasi transaksi akan otomatis terekam dan ditampilkan di sini."
            )
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                items(filteredList, key = { it.id }) { item ->
                    UnifiedTransactionCard(item = item)
                }
            }
        }
    }
}

@Composable
fun UnifiedTransactionCard(item: UnifiedTransaction) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
                    ) {
                        Text(
                            text = item.category.label,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    Text(
                        text = item.id,
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = GoldPrimary
                    )
                }

                Text(
                    text = "${if (item.isIncome) "+" else "-"} ${Formatters.formatRupiah(item.amount)}",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = if (item.isIncome) SuccessGreen else DangerRed
                )
            }

            Spacer(modifier = Modifier.height(6.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                )
                Text(
                    text = Formatters.formatDateTime(item.date),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(2.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = item.subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = "User: ${item.actor}",
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}
