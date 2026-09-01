package com.example.ui.screens

import androidx.compose.foundation.clickable
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
import com.example.data.database.entity.GoldInventoryEntity
import com.example.data.database.entity.GoldSalesEntity
import com.example.data.database.entity.UserEntity
import com.example.ui.components.ActiveRoleFormHeader
import com.example.ui.components.AttachmentUploader
import com.example.ui.components.EmptyStateView
import com.example.ui.theme.DangerRed
import com.example.ui.theme.GoldLight
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.SuccessGreenLight
import com.example.utils.Formatters
import com.example.viewmodel.ConfirmationDialogData
import com.example.viewmodel.MainViewModel

@Composable
fun GoldSalesScreen(
    viewModel: MainViewModel,
    currentUser: UserEntity?,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableStateOf(0) }
    val summary by viewModel.dashboardSummary.collectAsStateWithLifecycle()
    val availableInventory by viewModel.availableInventory.collectAsStateWithLifecycle(emptyList())
    val allSales by viewModel.allSales.collectAsStateWithLifecycle(emptyList())
    val activeRates by viewModel.activeRates.collectAsStateWithLifecycle(emptyList())

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .testTag("screen_gold_sales")
    ) {
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = Color.Transparent,
            divider = {},
            modifier = Modifier.padding(vertical = 8.dp)
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("Form Penjualan Emas") },
                selectedContentColor = GoldPrimary
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("Riwayat Penjualan (${allSales.size})") },
                selectedContentColor = GoldPrimary
            )
        }

        if (selectedTab == 0) {
            GoldSalesForm(
                viewModel = viewModel,
                currentUser = currentUser,
                currentModalBalance = summary.currentModalBalance,
                inventoryList = availableInventory,
                rates = activeRates,
                onSuccess = { selectedTab = 1 }
            )
        } else {
            if (allSales.isEmpty()) {
                EmptyStateView(
                    icon = Icons.Default.PointOfSale,
                    title = "Belum Ada Penjualan Emas",
                    description = "Catat penjualan emas dari inventory menggunakan form penjualan."
                )
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    items(allSales, key = { it.salesId }) { sale ->
                        GoldSaleCard(
                            sale = sale,
                            currentUser = currentUser,
                            onDelete = if (currentUser?.role?.canDeleteTransactions() == true) {
                                { viewModel.deleteGoldSale(sale.salesId, currentUser.username) }
                            } else null
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun GoldSalesForm(
    viewModel: MainViewModel,
    currentUser: UserEntity?,
    currentModalBalance: Double,
    inventoryList: List<GoldInventoryEntity>,
    rates: List<com.example.data.database.entity.GoldRateEntity>,
    onSuccess: () -> Unit
) {
    var selectedInventory by remember { mutableStateOf<GoldInventoryEntity?>(inventoryList.firstOrNull()) }
    var weightSoldText by remember { mutableStateOf("") }
    var sellPricePerGramText by remember { mutableStateOf("") }
    var relatedExpenseText by remember { mutableStateOf("0") }
    var buyerName by remember { mutableStateOf("") }
    var notesText by remember { mutableStateOf("") }
    var proofUri by remember { mutableStateOf<String?>(null) }

    var expandedInventoryDropdown by remember { mutableStateOf(false) }

    // When selected inventory changes, auto populate default sell price from rates
    LaunchedEffect(selectedInventory, rates) {
        if (selectedInventory != null) {
            val rate = rates.find { it.kadarName.equals(selectedInventory?.kadarName, ignoreCase = true) }
            if (rate != null && sellPricePerGramText.isBlank()) {
                sellPricePerGramText = rate.sellPricePerGram.toLong().toString()
            }
        }
    }

    val weightSold = weightSoldText.toDoubleOrNull() ?: 0.0
    val sellPricePerGram = sellPricePerGramText.toDoubleOrNull() ?: 0.0
    val relatedExpense = relatedExpenseText.toDoubleOrNull() ?: 0.0

    val totalSalesAmount = weightSold * sellPricePerGram
    val cogs = weightSold * (selectedInventory?.costPerGram ?: 0.0)
    val profit = totalSalesAmount - cogs - relatedExpense

    val remainingWeightAfterSale = ((selectedInventory?.availableWeightGrams ?: 0.0) - weightSold).coerceAtLeast(0.0)
    val isPartial = remainingWeightAfterSale > 0.001
    val balanceAfter = currentModalBalance + totalSalesAmount

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .imePadding(),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(top = 4.dp, bottom = 120.dp)
    ) {
        stickyHeader {
            ActiveRoleFormHeader(
                currentUser = currentUser,
                actionDescription = "Input Penjualan Emas"
            )
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Form Penjualan Emas (Mendukung Sebagian)",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )

                    // Dropdown Pilih Emas dari Inventory
                    ExposedDropdownMenuBox(
                        expanded = expandedInventoryDropdown,
                        onExpandedChange = { expandedInventoryDropdown = !expandedInventoryDropdown }
                    ) {
                        OutlinedTextField(
                            value = if (selectedInventory != null) {
                                "${selectedInventory!!.inventoryId} | Kadar ${selectedInventory!!.kadarName} (Sisa: ${Formatters.formatGram(selectedInventory!!.availableWeightGrams)})"
                            } else "Pilih Emas dari Inventory...",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Pilih Emas dari Inventory *") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedInventoryDropdown) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor()
                                .testTag("select_sales_inventory")
                        )
                        ExposedDropdownMenu(
                            expanded = expandedInventoryDropdown,
                            onDismissRequest = { expandedInventoryDropdown = false }
                        ) {
                            if (inventoryList.isEmpty()) {
                                DropdownMenuItem(
                                    text = { Text("Stok inventory habis. Masukkan intake emas terlebih dahulu.") },
                                    onClick = { expandedInventoryDropdown = false }
                                )
                            } else {
                                inventoryList.forEach { inv ->
                                    DropdownMenuItem(
                                        text = {
                                            Column {
                                                Text("${inv.inventoryId} - Kadar ${inv.kadarName}", fontWeight = FontWeight.Bold)
                                                Text(
                                                    "Tersedia: ${Formatters.formatGram(inv.availableWeightGrams)} | Modal: ${Formatters.formatRupiah(inv.costPerGram)}/g",
                                                    fontSize = 11.sp,
                                                    color = Color.Gray
                                                )
                                            }
                                        },
                                        onClick = {
                                            selectedInventory = inv
                                            expandedInventoryDropdown = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    if (selectedInventory != null) {
                        // Inventory details badge
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    "Stok Tersedia: ${Formatters.formatGram(selectedInventory!!.availableWeightGrams)}",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    "Modal Pokok: ${Formatters.formatRupiah(selectedInventory!!.costPerGram)}/g",
                                    fontSize = 12.sp,
                                    color = GoldPrimary
                                )
                            }
                        }
                    }

                    // Berat Dijual (Gram)
                    OutlinedTextField(
                        value = weightSoldText,
                        onValueChange = { weightSoldText = it },
                        label = { Text("Berat Emas yang Dijual (Gram) *") },
                        placeholder = { Text("Contoh: 3 atau 2.5 (Bisa sebagian)") },
                        suffix = { Text("gram") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_sales_weight")
                    )

                    // Harga Jual per Gram
                    OutlinedTextField(
                        value = sellPricePerGramText,
                        onValueChange = { sellPricePerGramText = it.filter { ch -> ch.isDigit() } },
                        label = { Text("Harga Jual per Gram (Rp) *") },
                        prefix = { Text("Rp ") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_sales_price")
                    )

                    // Biaya Terkait
                    OutlinedTextField(
                        value = relatedExpenseText,
                        onValueChange = { relatedExpenseText = it.filter { ch -> ch.isDigit() } },
                        label = { Text("Biaya Terkait Transaksi / Ongkos (Rp)") },
                        prefix = { Text("Rp ") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Nama Pembeli
                    OutlinedTextField(
                        value = buyerName,
                        onValueChange = { buyerName = it },
                        label = { Text("Nama Pembeli / Pelanggan *") },
                        placeholder = { Text("Contoh: Toko Emas Sejahtera / Bpk. Hendra") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_sales_buyer")
                    )

                    OutlinedTextField(
                        value = notesText,
                        onValueChange = { notesText = it },
                        label = { Text("Keterangan Penjualan") },
                        placeholder = { Text("Catatan faktur / metode pembayaran...") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    AttachmentUploader(
                        photoUri = proofUri,
                        onPhotoSelected = { proofUri = it },
                        label = "Bukti Pembayaran / Nota"
                    )

                    // Profit Calculation Preview Box
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "Kalkulasi Penjualan & Laba Bersih:",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Total Penjualan (${weightSoldText.ifBlank { "0" }}g × Rp ${Formatters.formatNumber(sellPricePerGram)}):", fontSize = 12.sp)
                                Text(Formatters.formatRupiah(totalSalesAmount), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = SuccessGreen)
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Modal Pokok Terpakai (COGS):", fontSize = 12.sp)
                                Text("- ${Formatters.formatRupiah(cogs)}", fontSize = 12.sp, color = DangerRed)
                            }
                            if (relatedExpense > 0) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Biaya Terkait:", fontSize = 12.sp)
                                    Text("- ${Formatters.formatRupiah(relatedExpense)}", fontSize = 12.sp, color = DangerRed)
                                }
                            }
                            HorizontalDivider()
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("LABA BERSIH TRANSAKSI:", fontWeight = FontWeight.Bold)
                                Text(
                                    Formatters.formatRupiah(profit),
                                    fontWeight = FontWeight.ExtraBold,
                                    color = if (profit >= 0) SuccessGreen else DangerRed
                                )
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Sisa Stok Setelah Penjualan:", fontSize = 11.sp, color = Color.Gray)
                                Text(
                                    "${Formatters.formatGram(remainingWeightAfterSale)} (${if (isPartial) "Sebagian Terjual" else "Habis Terjual"})",
                                    fontSize = 11.sp,
                                    color = if (remainingWeightAfterSale > 0) GoldPrimary else Color.Gray
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Button(
                        onClick = {
                            if (selectedInventory == null) {
                                viewModel.showMessage("Pilih emas dari inventory terlebih dahulu.")
                                return@Button
                            }
                            if (weightSold <= 0) {
                                viewModel.showMessage("Berat emas wajib diisi.")
                                return@Button
                            }
                            if (weightSold > (selectedInventory?.availableWeightGrams ?: 0.0) + 0.0001) {
                                viewModel.showMessage("Stok emas tidak mencukupi.")
                                return@Button
                            }
                            if (sellPricePerGram <= 0) {
                                viewModel.showMessage("Harga jual harus lebih dari 0.")
                                return@Button
                            }
                            if (buyerName.isBlank()) {
                                viewModel.showMessage("Nama pembeli wajib diisi.")
                                return@Button
                            }

                            viewModel.showConfirmation(
                                ConfirmationDialogData(
                                    title = "Konfirmasi Penjualan Emas",
                                    transactionType = if (isPartial) "Penjualan Sebagian Emas" else "Penjualan Emas Penuh",
                                    weightGrams = weightSold,
                                    kadarName = selectedInventory!!.kadarName,
                                    pricePerGram = sellPricePerGram,
                                    grossValue = totalSalesAmount,
                                    netValue = totalSalesAmount,
                                    currentBalance = currentModalBalance,
                                    balanceAfter = balanceAfter,
                                    details = mapOf(
                                        "Pembeli" to buyerName,
                                        "Modal Terpakai" to Formatters.formatRupiah(cogs),
                                        "Estimasi Laba" to Formatters.formatRupiah(profit),
                                        "Sisa Stok Inventory" to Formatters.formatGram(remainingWeightAfterSale)
                                    ),
                                    onConfirm = {
                                        viewModel.processGoldSale(
                                            date = System.currentTimeMillis(),
                                            inventoryItem = selectedInventory!!,
                                            weightSold = weightSold,
                                            sellPricePerGram = sellPricePerGram,
                                            relatedExpense = relatedExpense,
                                            buyerName = buyerName,
                                            notes = notesText,
                                            proofUri = proofUri,
                                            actor = currentUser?.username ?: "OPERATOR"
                                        )
                                        onSuccess()
                                    }
                                )
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("btn_save_sales"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary)
                    ) {
                        Icon(Icons.Default.Save, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Simpan Transaksi Penjualan", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun GoldSaleCard(
    sale: GoldSalesEntity,
    currentUser: UserEntity?,
    onDelete: (() -> Unit)?
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
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
                        text = sale.salesId,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = GoldPrimary
                    )
                    Text(
                        text = Formatters.formatDateTime(sale.transactionDate),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = SuccessGreenLight
                ) {
                    Text(
                        text = "+ Laba ${Formatters.formatRupiah(sale.profitAmount)}",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = SuccessGreen,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
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
                    Text("Pembeli", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(sale.buyerName, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold))
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Kadar / Berat", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        "${sale.kadarName} - ${Formatters.formatGram(sale.weightSoldGrams)}",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Harga Jual/g", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(Formatters.formatRupiah(sale.sellPricePerGram), style = MaterialTheme.typography.bodySmall)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Total Penjualan", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        Formatters.formatRupiah(sale.totalSalesAmount),
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.ExtraBold),
                        color = SuccessGreen
                    )
                }
            }

            if (sale.notes.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    "Catatan: ${sale.notes}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (onDelete != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(
                        onClick = onDelete,
                        colors = ButtonDefaults.textButtonColors(contentColor = DangerRed)
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Hapus Penjualan", fontSize = 12.sp)
                    }
                }
            }
        }
    }
}
