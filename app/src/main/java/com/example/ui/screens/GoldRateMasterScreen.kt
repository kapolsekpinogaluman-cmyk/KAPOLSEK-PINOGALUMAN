package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.database.entity.GoldRateEntity
import com.example.data.database.entity.PriceHistoryEntity
import com.example.data.database.entity.UserEntity
import com.example.ui.components.EmptyStateView
import com.example.ui.components.StatusBadge
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.SuccessGreen
import com.example.utils.Formatters
import com.example.viewmodel.MainViewModel

@Composable
fun GoldRateMasterScreen(
    viewModel: MainViewModel,
    currentUser: UserEntity?,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableStateOf(0) }
    var showAddDialog by remember { mutableStateOf(false) }
    var editingRate by remember { mutableStateOf<GoldRateEntity?>(null) }

    val allRates by viewModel.allRates.collectAsStateWithLifecycle(emptyList())
    val priceHistory by viewModel.priceHistory.collectAsStateWithLifecycle(emptyList())

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .testTag("screen_gold_rates")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TabRow(
                selectedTabIndex = selectedTab,
                modifier = Modifier.weight(1f),
                containerColor = Color.Transparent,
                divider = {}
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Daftar Harga Kadar") },
                    selectedContentColor = GoldPrimary
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Histori Perubahan") },
                    selectedContentColor = GoldPrimary
                )
            }

            if (selectedTab == 0 && currentUser?.role?.canManageMasterPrices() == true) {
                Spacer(modifier = Modifier.width(8.dp))
                FilledTonalButton(
                    onClick = { showAddDialog = true },
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.testTag("btn_add_kadar")
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Tambah", fontSize = 12.sp)
                }
            }
        }

        if (selectedTab == 0) {
            if (allRates.isEmpty()) {
                EmptyStateView(
                    icon = Icons.Default.PriceChange,
                    title = "Belum Ada Master Kadar",
                    description = "Tekan tombol Tambah untuk membuat kadar emas baru."
                )
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    items(allRates, key = { it.id }) { rate ->
                        GoldRateCard(
                            rate = rate,
                            canEdit = currentUser?.role?.canManageMasterPrices() == true,
                            onEdit = { editingRate = rate }
                        )
                    }
                }
            }
        } else {
            if (priceHistory.isEmpty()) {
                EmptyStateView(
                    icon = Icons.Default.History,
                    title = "Histori Harga Belum Ada",
                    description = "Setiap perubahan harga beli/jual akan tercatat otomatis di sini beserta alasan dan usernya."
                )
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    items(priceHistory, key = { it.id }) { history ->
                        PriceHistoryCard(history = history)
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddKadarDialog(
            onDismiss = { showAddDialog = false },
            onSave = { kadar, buy, sell, potPercent, potNominal ->
                viewModel.addGoldRate(
                    kadarName = kadar,
                    buyPrice = buy,
                    sellPrice = sell,
                    deductionPercent = potPercent,
                    deductionNominal = potNominal,
                    actor = currentUser?.username ?: "ADMIN"
                )
                showAddDialog = false
            }
        )
    }

    if (editingRate != null) {
        EditKadarDialog(
            rate = editingRate!!,
            onDismiss = { editingRate = null },
            onSave = { buy, sell, potPercent, potNominal, isActive, reason ->
                viewModel.updateGoldRate(
                    oldRate = editingRate!!,
                    buyPrice = buy,
                    sellPrice = sell,
                    deductionPercent = potPercent,
                    deductionNominal = potNominal,
                    isActive = isActive,
                    actor = currentUser?.username ?: "ADMIN",
                    reason = reason
                )
                editingRate = null
            }
        )
    }
}

@Composable
fun GoldRateCard(
    rate: GoldRateEntity,
    canEdit: Boolean,
    onEdit: () -> Unit
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
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Kadar ${rate.kadarName}",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    StatusBadge(status = if (rate.isActive) "AKTIF" else "NONAKTIF")
                }

                if (canEdit) {
                    IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = "Edit Harga",
                            tint = GoldPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Harga Beli / Acuan Masuk",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${Formatters.formatRupiah(rate.buyPricePerGram)} /g",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Harga Jual Acuan",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${Formatters.formatRupiah(rate.sellPricePerGram)} /g",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        color = SuccessGreen
                    )
                }
            }

            if (rate.defaultDeductionPercent > 0 || rate.defaultDeductionNominal > 0) {
                Spacer(modifier = Modifier.height(6.dp))
                val deductionStr = if (rate.defaultDeductionPercent > 0) {
                    "Potongan Default: ${rate.defaultDeductionPercent}%"
                } else {
                    "Potongan Default: ${Formatters.formatRupiah(rate.defaultDeductionNominal)}"
                }
                Text(
                    text = deductionStr,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun PriceHistoryCard(history: PriceHistoryEntity) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Kadar ${history.kadarName}",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                )
                Text(
                    text = Formatters.formatDateTime(history.changeDate),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Beli: ${Formatters.formatRupiah(history.oldBuyPrice)} -> ${Formatters.formatRupiah(history.newBuyPrice)}",
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    text = "Jual: ${Formatters.formatRupiah(history.oldSellPrice)} -> ${Formatters.formatRupiah(history.newSellPrice)}",
                    style = MaterialTheme.typography.bodySmall
                )
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "Diubah oleh: ${history.changedBy} ${if (history.reason.isNotBlank()) "• Alasan: ${history.reason}" else ""}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun AddKadarDialog(
    onDismiss: () -> Unit,
    onSave: (kadar: String, buy: Double, sell: Double, potPercent: Double, potNominal: Double) -> Unit
) {
    var kadar by remember { mutableStateOf("") }
    var buyPriceText by remember { mutableStateOf("") }
    var sellPriceText by remember { mutableStateOf("") }
    var potPercentText by remember { mutableStateOf("0") }
    var potNominalText by remember { mutableStateOf("0") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Tambah Master Kadar Emas") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = kadar,
                    onValueChange = { kadar = it },
                    label = { Text("Nama Kadar (contoh: 24K, 22.5K, 9K)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = buyPriceText,
                    onValueChange = { buyPriceText = it.filter { ch -> ch.isDigit() } },
                    label = { Text("Harga Beli / Gram (Rp)") },
                    prefix = { Text("Rp ") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = sellPriceText,
                    onValueChange = { sellPriceText = it.filter { ch -> ch.isDigit() } },
                    label = { Text("Harga Jual / Gram (Rp)") },
                    prefix = { Text("Rp ") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = potPercentText,
                    onValueChange = { potPercentText = it },
                    label = { Text("Potongan Default (%)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val buy = buyPriceText.toDoubleOrNull() ?: 0.0
                    val sell = sellPriceText.toDoubleOrNull() ?: 0.0
                    val potP = potPercentText.toDoubleOrNull() ?: 0.0
                    val potN = potNominalText.toDoubleOrNull() ?: 0.0
                    onSave(kadar, buy, sell, potP, potN)
                },
                colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary)
            ) {
                Text("Simpan")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) { Text("Batal") }
        }
    )
}

@Composable
fun EditKadarDialog(
    rate: GoldRateEntity,
    onDismiss: () -> Unit,
    onSave: (buy: Double, sell: Double, potPercent: Double, potNominal: Double, isActive: Boolean, reason: String) -> Unit
) {
    var buyPriceText by remember { mutableStateOf(rate.buyPricePerGram.toLong().toString()) }
    var sellPriceText by remember { mutableStateOf(rate.sellPricePerGram.toLong().toString()) }
    var potPercentText by remember { mutableStateOf(rate.defaultDeductionPercent.toString()) }
    var potNominalText by remember { mutableStateOf(rate.defaultDeductionNominal.toLong().toString()) }
    var isActive by remember { mutableStateOf(rate.isActive) }
    var reasonText by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Ubah Harga Kadar ${rate.kadarName}") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = buyPriceText,
                    onValueChange = { buyPriceText = it.filter { ch -> ch.isDigit() } },
                    label = { Text("Harga Beli / Gram (Rp)") },
                    prefix = { Text("Rp ") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = sellPriceText,
                    onValueChange = { sellPriceText = it.filter { ch -> ch.isDigit() } },
                    label = { Text("Harga Jual / Gram (Rp)") },
                    prefix = { Text("Rp ") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = potPercentText,
                    onValueChange = { potPercentText = it },
                    label = { Text("Potongan Default (%)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = reasonText,
                    onValueChange = { reasonText = it },
                    label = { Text("Alasan Perubahan Harga") },
                    placeholder = { Text("Penyesuaian pasar / fluktuasi harga") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Checkbox(checked = isActive, onCheckedChange = { isActive = it })
                    Text("Kadar Aktif")
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val buy = buyPriceText.toDoubleOrNull() ?: 0.0
                    val sell = sellPriceText.toDoubleOrNull() ?: 0.0
                    val potP = potPercentText.toDoubleOrNull() ?: 0.0
                    val potN = potNominalText.toDoubleOrNull() ?: 0.0
                    onSave(buy, sell, potP, potN, isActive, reasonText)
                },
                colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary)
            ) {
                Text("Simpan Perubahan")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) { Text("Batal") }
        }
    )
}
