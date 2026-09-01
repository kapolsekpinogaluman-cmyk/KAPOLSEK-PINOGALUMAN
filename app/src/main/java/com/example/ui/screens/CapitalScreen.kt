package com.example.ui.screens

import androidx.compose.foundation.background
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
import com.example.data.database.entity.CapitalTransactionEntity
import com.example.data.database.entity.UserEntity
import com.example.data.model.CapitalTransactionType
import com.example.data.model.UserRole
import com.example.ui.components.ActiveRoleFormHeader
import com.example.ui.components.AttachmentUploader
import com.example.ui.components.EmptyStateView
import com.example.ui.theme.*
import com.example.utils.Formatters
import com.example.viewmodel.ConfirmationDialogData
import com.example.viewmodel.MainViewModel
import java.util.Date

enum class CapitalTab(val title: String) {
    SALDO("Saldo Modal"),
    TAMBAH("Tambah Modal"),
    PENGURANGAN("Kurang Modal"),
    RIWAYAT("Riwayat Modal")
}

@Composable
fun CapitalScreen(
    viewModel: MainViewModel,
    currentUser: UserEntity?,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableStateOf(CapitalTab.SALDO) }
    val summary by viewModel.dashboardSummary.collectAsStateWithLifecycle()
    val capitalTransactions by viewModel.allCapitalTransactions.collectAsStateWithLifecycle(emptyList())

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .testTag("screen_capital")
    ) {
        // Tab Navigation Row
        ScrollableTabRow(
            selectedTabIndex = selectedTab.ordinal,
            edgePadding = 0.dp,
            containerColor = Color.Transparent,
            divider = {},
            modifier = Modifier.padding(vertical = 8.dp)
        ) {
            CapitalTab.values().forEach { tab ->
                Tab(
                    selected = selectedTab == tab,
                    onClick = { selectedTab = tab },
                    text = { Text(tab.title, fontWeight = if (selectedTab == tab) FontWeight.Bold else FontWeight.Normal) },
                    selectedContentColor = GoldPrimary,
                    unselectedContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.testTag("tab_capital_${tab.name.lowercase()}")
                )
            }
        }

        when (selectedTab) {
            CapitalTab.SALDO -> SaldoModalView(summary.currentModalBalance, capitalTransactions)
            CapitalTab.TAMBAH -> TambahModalForm(viewModel, currentUser, summary.currentModalBalance) {
                selectedTab = CapitalTab.SALDO
            }
            CapitalTab.PENGURANGAN -> PenguranganModalForm(viewModel, currentUser, summary.currentModalBalance) {
                selectedTab = CapitalTab.SALDO
            }
            CapitalTab.RIWAYAT -> RiwayatModalView(capitalTransactions, currentUser, viewModel)
        }
    }
}

@Composable
fun SaldoModalView(
    currentBalance: Double,
    transactions: List<CapitalTransactionEntity>
) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(vertical = 12.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = SlateNavyDark)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp)
                ) {
                    Text(
                        text = "TOTAL SALDO MODAL USAHA",
                        style = MaterialTheme.typography.labelMedium.copy(letterSpacing = 1.sp),
                        color = GoldLight
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = Formatters.formatRupiah(currentBalance),
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider(color = Color(0xFF334155))
                    Spacer(modifier = Modifier.height(12.dp))

                    val totalTambah = transactions.filter { it.type == CapitalTransactionType.TAMBAH_MODAL }.sumOf { it.amount }
                    val totalJual = transactions.filter { it.type == CapitalTransactionType.PENJUALAN_EMAS_REVENUE }.sumOf { it.amount }
                    val totalKurang = transactions.filter { it.type == CapitalTransactionType.PENGURANGAN_MODAL }.sumOf { it.amount }
                    val totalIntake = transactions.filter { it.type == CapitalTransactionType.PENGAMBILAN_EMAS_USAGE }.sumOf { it.amount }

                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        LedgerRow(label = "+ Setoran Tambah Modal", value = Formatters.formatRupiah(totalTambah), color = SuccessGreen)
                        LedgerRow(label = "+ Kas Penjualan Emas", value = Formatters.formatRupiah(totalJual), color = SuccessGreen)
                        LedgerRow(label = "- Pengurangan Modal / Prive", value = Formatters.formatRupiah(totalKurang), color = DangerRed)
                        LedgerRow(label = "- Penggunaan Modal Ambil Emas", value = Formatters.formatRupiah(totalIntake), color = DangerRed)
                    }
                }
            }
        }

        item {
            Text(
                text = "5 Mutasi Modal Terakhir",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
            )
        }

        val recentTxs = transactions.take(5)
        if (recentTxs.isEmpty()) {
            item {
                EmptyStateView(
                    icon = Icons.Default.AccountBalanceWallet,
                    title = "Belum Ada Transaksi Modal",
                    description = "Gunakan tab Tambah Modal untuk menyetor modal awal usaha."
                )
            }
        } else {
            items(recentTxs) { tx ->
                CapitalTransactionCard(tx = tx, currentUser = null, onDelete = null)
            }
        }
    }
}

@Composable
fun LedgerRow(label: String, value: String, color: Color) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, style = MaterialTheme.typography.bodySmall, color = Color.LightGray)
        Text(text = value, style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold), color = color)
    }
}

@OptIn(ExperimentalMaterial3Api::class, androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun TambahModalForm(
    viewModel: MainViewModel,
    currentUser: UserEntity?,
    currentBalance: Double,
    onSuccess: () -> Unit
) {
    var nominalText by remember { mutableStateOf("") }
    var sourceText by remember { mutableStateOf("Kas Utama Pemilik") }
    var tromolOwnerText by remember { mutableStateOf("") }
    var selectedBatchNumber by remember { mutableStateOf("") }
    var notesText by remember { mutableStateOf("") }
    var proofUri by remember { mutableStateOf<String?>(null) }
    var expandedBatchDropdown by remember { mutableStateOf(false) }

    val batches by viewModel.allBatches.collectAsStateWithLifecycle(emptyList())

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .imePadding(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 4.dp, bottom = 120.dp)
    ) {
        stickyHeader {
            ActiveRoleFormHeader(
                currentUser = currentUser,
                actionDescription = "Input Tambah Modal Usaha"
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
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.AccountBalanceWallet, contentDescription = null, tint = GoldPrimary)
                        Text(
                            text = "Form Tambah / Salurkan Modal Usaha",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }

                    OutlinedTextField(
                        value = nominalText,
                        onValueChange = { nominalText = it.filter { ch -> ch.isDigit() } },
                        label = { Text("Nominal Modal (Rp) *") },
                        placeholder = { Text("Contoh: 50000000") },
                        prefix = { Text("Rp ") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_modal_nominal")
                    )

                    // Target Penerima Modal / Pemilik Tromol
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                text = "Tujuan Modal / Pemilik Tromol Penerima:",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )

                            if (batches.isNotEmpty()) {
                                ExposedDropdownMenuBox(
                                    expanded = expandedBatchDropdown,
                                    onExpandedChange = { expandedBatchDropdown = !expandedBatchDropdown }
                                ) {
                                    OutlinedTextField(
                                        value = if (selectedBatchNumber.isNotBlank()) "Pilih: $selectedBatchNumber" else "Pilih dari Daftar Tromol (Opsional)",
                                        onValueChange = {},
                                        readOnly = true,
                                        label = { Text("Pilih Tromol Terdaftar") },
                                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedBatchDropdown) },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .menuAnchor()
                                    )
                                    ExposedDropdownMenu(
                                        expanded = expandedBatchDropdown,
                                        onDismissRequest = { expandedBatchDropdown = false }
                                    ) {
                                        batches.forEach { b ->
                                            DropdownMenuItem(
                                                text = {
                                                    Column {
                                                        Text("${b.batchNumber} - ${b.batchName}", fontWeight = FontWeight.Bold)
                                                        Text("Pemilik / Pengelola: ${b.managerName}", fontSize = 11.sp, color = Color.Gray)
                                                    }
                                                },
                                                onClick = {
                                                    selectedBatchNumber = b.batchNumber
                                                    tromolOwnerText = "${b.managerName} (${b.batchName})"
                                                    expandedBatchDropdown = false
                                                }
                                            )
                                        }
                                    }
                                }
                            }

                            OutlinedTextField(
                                value = tromolOwnerText,
                                onValueChange = { tromolOwnerText = it },
                                label = { Text("Nama Pemilik Tromol / Penerima Modal *") },
                                placeholder = { Text("Contoh: H. Ruslan (Tromol Desa Pinogaluman)") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("input_modal_tromol_owner")
                            )
                        }
                    }

                    OutlinedTextField(
                        value = sourceText,
                        onValueChange = { sourceText = it },
                        label = { Text("Sumber Asal Dana / Modal") },
                        placeholder = { Text("Contoh: Setoran Kas Pemilik / Transfer BCA") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_modal_source")
                    )

                    OutlinedTextField(
                        value = notesText,
                        onValueChange = { notesText = it },
                        label = { Text("Keterangan Tambahan") },
                        placeholder = { Text("Catatan penyerahan modal, lokasi tromol, perjanjian...") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_modal_notes")
                    )

                    AttachmentUploader(
                        photoUri = proofUri,
                        onPhotoSelected = { proofUri = it },
                        label = "Bukti Setoran / Kwitansi / Transfer"
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = {
                            val nominal = nominalText.toDoubleOrNull() ?: 0.0
                            if (nominal <= 0) {
                                viewModel.showMessage("Nominal modal harus lebih besar dari 0.")
                                return@Button
                            }
                            if (tromolOwnerText.isBlank() && sourceText.isBlank()) {
                                viewModel.showMessage("Nama pemilik tromol / penerima modal wajib diisi.")
                                return@Button
                            }

                            val fullSourceTarget = buildString {
                                if (tromolOwnerText.isNotBlank()) {
                                    append("Pemilik Tromol: $tromolOwnerText")
                                    if (sourceText.isNotBlank()) append(" • Sumber: $sourceText")
                                } else {
                                    append(sourceText)
                                }
                            }

                            val newBalance = currentBalance + nominal
                            viewModel.showConfirmation(
                                ConfirmationDialogData(
                                    title = "Konfirmasi Tambah Modal",
                                    transactionType = "Tambah Modal Usaha",
                                    currentBalance = currentBalance,
                                    balanceAfter = newBalance,
                                    details = mapOf(
                                        "Nominal" to Formatters.formatRupiah(nominal),
                                        "Pemilik Tromol" to (if (tromolOwnerText.isNotBlank()) tromolOwnerText else "-"),
                                        "Sumber Modal" to (if (sourceText.isNotBlank()) sourceText else "-"),
                                        "Keterangan" to (if (notesText.isNotBlank()) notesText else "-")
                                    ),
                                    onConfirm = {
                                        viewModel.addCapital(
                                            amount = nominal,
                                            source = fullSourceTarget,
                                            notes = notesText,
                                            proofUri = proofUri,
                                            actor = currentUser?.username ?: "OWNER",
                                            date = System.currentTimeMillis()
                                        )
                                        onSuccess()
                                    }
                                )
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("btn_save_tambah_modal"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary)
                    ) {
                        Icon(Icons.Default.Save, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Simpan Modal Usaha", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun PenguranganModalForm(
    viewModel: MainViewModel,
    currentUser: UserEntity?,
    currentBalance: Double,
    onSuccess: () -> Unit
) {
    var nominalText by remember { mutableStateOf("") }
    var targetText by remember { mutableStateOf("") }
    var notesText by remember { mutableStateOf("") }
    var proofUri by remember { mutableStateOf<String?>(null) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .imePadding(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 4.dp, bottom = 120.dp)
    ) {
        stickyHeader {
            ActiveRoleFormHeader(
                currentUser = currentUser,
                actionDescription = "Input Pengurangan Modal / Prive"
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
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = "Form Pengurangan Modal / Prive",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )

                    OutlinedTextField(
                        value = nominalText,
                        onValueChange = { nominalText = it.filter { ch -> ch.isDigit() } },
                        label = { Text("Nominal Pengurangan (Rp)") },
                        placeholder = { Text("Contoh: 15000000") },
                        prefix = { Text("Rp ") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_reduce_modal_nominal")
                    )

                    OutlinedTextField(
                        value = targetText,
                        onValueChange = { targetText = it },
                        label = { Text("Tujuan / Penerima") },
                        placeholder = { Text("Contoh: Prive Pemilik / Pengembalian Investasi") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_reduce_modal_target")
                    )

                    OutlinedTextField(
                        value = notesText,
                        onValueChange = { notesText = it },
                        label = { Text("Keterangan") },
                        placeholder = { Text("Catatan pengurangan modal...") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_reduce_modal_notes")
                    )

                    AttachmentUploader(
                        photoUri = proofUri,
                        onPhotoSelected = { proofUri = it },
                        label = "Bukti Penarikan / Transfer"
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = {
                            val nominal = nominalText.toDoubleOrNull() ?: 0.0
                            if (nominal <= 0) {
                                viewModel.showMessage("Nominal pengurangan modal harus lebih besar dari 0.")
                                return@Button
                            }
                            if (targetText.isBlank()) {
                                viewModel.showMessage("Tujuan pengurangan modal wajib diisi.")
                                return@Button
                            }

                            val newBalance = currentBalance - nominal
                            viewModel.showConfirmation(
                                ConfirmationDialogData(
                                    title = "Konfirmasi Pengurangan Modal",
                                    transactionType = "Pengurangan Modal / Prive",
                                    currentBalance = currentBalance,
                                    balanceAfter = newBalance,
                                    details = mapOf(
                                        "Nominal Pengurangan" to Formatters.formatRupiah(nominal),
                                        "Tujuan" to targetText,
                                        "Keterangan" to (if (notesText.isNotBlank()) notesText else "-")
                                    ),
                                    onConfirm = {
                                        viewModel.reduceCapital(
                                            amount = nominal,
                                            target = targetText,
                                            notes = notesText,
                                            proofUri = proofUri,
                                            actor = currentUser?.username ?: "OWNER",
                                            date = System.currentTimeMillis()
                                        )
                                        onSuccess()
                                    }
                                )
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("btn_save_kurang_modal"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = DangerRed)
                    ) {
                        Icon(Icons.Default.RemoveCircleOutline, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Simpan Pengurangan Modal", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun RiwayatModalView(
    transactions: List<CapitalTransactionEntity>,
    currentUser: UserEntity?,
    viewModel: MainViewModel
) {
    if (transactions.isEmpty()) {
        EmptyStateView(
            icon = Icons.Default.History,
            title = "Riwayat Modal Kosong",
            description = "Semua transaksi modal yang dicatat akan muncul di sini."
        )
    } else {
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(vertical = 12.dp)
        ) {
            items(transactions, key = { it.transactionId }) { tx ->
                CapitalTransactionCard(
                    tx = tx,
                    currentUser = currentUser,
                    onDelete = if (currentUser?.role?.canDeleteTransactions() == true) {
                        { viewModel.deleteCapitalTransaction(tx.transactionId, currentUser.username) }
                    } else null
                )
            }
        }
    }
}

@Composable
fun CapitalTransactionCard(
    tx: CapitalTransactionEntity,
    currentUser: UserEntity?,
    onDelete: (() -> Unit)?
) {
    val isPositive = tx.type == CapitalTransactionType.TAMBAH_MODAL || tx.type == CapitalTransactionType.PENJUALAN_EMAS_REVENUE
    val color = if (isPositive) SuccessGreen else DangerRed
    val prefix = if (isPositive) "+" else "-"

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
                        text = tx.transactionId,
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = GoldPrimary
                    )
                    Text(
                        text = Formatters.formatDateTime(tx.transactionDate),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Text(
                    text = "$prefix ${Formatters.formatRupiah(tx.amount)}",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = color
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = tx.sourceOrTarget,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                )
                Text(
                    text = "User: ${tx.createdBy}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (tx.notes.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = tx.notes,
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
                        Text("Hapus Transaksi", fontSize = 12.sp)
                    }
                }
            }
        }
    }
}
