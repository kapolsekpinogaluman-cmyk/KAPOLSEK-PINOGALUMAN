package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import com.example.data.database.entity.GoldInventoryEntity
import com.example.data.database.entity.UserEntity
import com.example.data.model.InventoryStatus
import com.example.ui.components.AppScreen
import com.example.ui.components.EmptyStateView
import com.example.ui.components.SearchAndFilterHeader
import com.example.ui.components.StatusBadge
import com.example.ui.theme.*
import com.example.utils.Formatters
import com.example.viewmodel.MainViewModel

@Composable
fun InventoryScreen(
    viewModel: MainViewModel,
    currentUser: UserEntity?,
    onNavigate: (AppScreen) -> Unit,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedStatusFilter by remember { mutableStateOf<String?>("SEMUA") }

    val allInventory by viewModel.allInventory.collectAsStateWithLifecycle(emptyList())

    val filteredInventory = remember(allInventory, searchQuery, selectedStatusFilter) {
        allInventory.filter { item ->
            val matchSearch = searchQuery.isBlank() ||
                    item.inventoryId.contains(searchQuery, ignoreCase = true) ||
                    item.batchNumber.contains(searchQuery, ignoreCase = true) ||
                    item.kadarName.contains(searchQuery, ignoreCase = true)

            val matchStatus = when (selectedStatusFilter) {
                "SEMUA" -> true
                "TERSEDIA" -> item.status == InventoryStatus.TERSEDIA || item.status == InventoryStatus.SEBAGIAN_TERJUAL
                "TERJUAL" -> item.status == InventoryStatus.TERJUAL
                else -> true
            }

            matchSearch && matchStatus
        }
    }

    val totalAvailableWeight = allInventory.filter { it.status == InventoryStatus.TERSEDIA || it.status == InventoryStatus.SEBAGIAN_TERJUAL }
        .sumOf { it.availableWeightGrams }
    val totalInventoryValue = allInventory.filter { it.status == InventoryStatus.TERSEDIA || it.status == InventoryStatus.SEBAGIAN_TERJUAL }
        .sumOf { it.availableWeightGrams * it.costPerGram }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .testTag("screen_inventory")
    ) {
        // Summary Header Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = SlateNavyDark)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Total Stok Emas Tersedia", style = MaterialTheme.typography.bodySmall, color = Color.LightGray)
                    Text(
                        Formatters.formatGram(totalAvailableWeight),
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold, color = Color.White)
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Total Nilai Modal Stok", style = MaterialTheme.typography.bodySmall, color = Color.LightGray)
                    Text(
                        Formatters.formatRupiah(totalInventoryValue),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = GoldLight)
                    )
                }
            }
        }

        // Search & Filter
        SearchAndFilterHeader(
            query = searchQuery,
            onQueryChange = { searchQuery = it },
            placeholder = "Cari ID emas, tromol, kadar..."
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf("SEMUA", "TERSEDIA", "TERJUAL").forEach { status ->
                FilterChip(
                    selected = selectedStatusFilter == status,
                    onClick = { selectedStatusFilter = status },
                    label = { Text(status) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = GoldPrimary,
                        selectedLabelColor = Color.White
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (filteredInventory.isEmpty()) {
            EmptyStateView(
                icon = Icons.Default.Inventory2,
                title = "Inventory Emas Kosong",
                description = "Emas yang masuk melalui Pengambilan Emas akan otomatis tercatat dan muncul di sini."
            )
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                items(filteredInventory, key = { it.inventoryId }) { item ->
                    InventoryCard(
                        item = item,
                        onSellClick = {
                            onNavigate(AppScreen.PENJUALAN)
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun InventoryCard(
    item: GoldInventoryEntity,
    onSellClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = item.inventoryId,
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = GoldPrimary
                    )
                    Text(
                        text = "Masuk: ${Formatters.formatDate(item.entryDate)} | Asal: ${item.batchNumber}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                StatusBadge(status = item.status.name)
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Kadar Emas", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        item.kadarName,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Sisa Stok", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        Formatters.formatGram(item.availableWeightGrams),
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        color = if (item.availableWeightGrams > 0) SuccessGreen else Color.Gray
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Awal / Terjual", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        "${Formatters.formatNumber(item.initialWeightGrams)}g / ${Formatters.formatNumber(item.soldWeightGrams)}g",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Modal Pokok/g", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(Formatters.formatRupiah(item.costPerGram), style = MaterialTheme.typography.bodySmall)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Total Nilai Modal Sisa", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        Formatters.formatRupiah(item.availableWeightGrams * item.costPerGram),
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        color = GoldPrimary
                    )
                }
            }

            if (item.availableWeightGrams > 0.001) {
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Button(
                        onClick = onSellClick,
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary),
                        modifier = Modifier.testTag("btn_sell_inventory_${item.inventoryId.lowercase()}")
                    ) {
                        Icon(Icons.Default.PointOfSale, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Jual Emas Ini", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
