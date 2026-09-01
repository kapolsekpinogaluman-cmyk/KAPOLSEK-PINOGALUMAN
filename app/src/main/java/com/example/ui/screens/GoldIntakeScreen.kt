package com.example.ui.screens

import androidx.compose.foundation.background
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
import com.example.data.database.entity.GoldIntakeEntity
import com.example.data.database.entity.UserEntity
import com.example.data.model.DeductionType
import com.example.ui.components.ActiveRoleFormHeader
import com.example.ui.components.AttachmentUploader
import com.example.ui.components.EmptyStateView
import com.example.ui.theme.DangerRed
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.SuccessGreen
import com.example.utils.Formatters
import com.example.viewmodel.ConfirmationDialogData
import com.example.viewmodel.MainViewModel

@Composable
fun GoldIntakeScreen(
    viewModel: MainViewModel,
    currentUser: UserEntity?,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableStateOf(0) }
    val summary by viewModel.dashboardSummary.collectAsStateWithLifecycle()
    val allIntakes by viewModel.allIntakes.collectAsStateWithLifecycle(emptyList())
    val batches by viewModel.allBatches.collectAsStateWithLifecycle(emptyList())
    val activeRates by viewModel.activeRates.collectAsStateWithLifecycle(emptyList())

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .testTag("screen_gold_intake")
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
                text = { Text("Form Pengambilan Emas") },
                selectedContentColor = GoldPrimary
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("Riwayat Pengambilan (${allIntakes.size})") },
                selectedContentColor = GoldPrimary
            )
        }

        if (selectedTab == 0) {
            GoldIntakeForm(
                viewModel = viewModel,
                currentUser = currentUser,
                currentModalBalance = summary.currentModalBalance,
                batches = batches,
                rates = activeRates,
                onSuccess = { selectedTab = 1 }
            )
        } else {
            if (allIntakes.isEmpty()) {
                EmptyStateView(
                    icon = Icons.Default.Download,
                    title = "Belum Ada Pengambilan Emas",
                    description = "Gunakan form untuk mencatat penerimaan emas hasil pengolahan tromol."
                )
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    items(allIntakes, key = { it.intakeId }) { intake ->
                        GoldIntakeCard(
                            intake = intake,
                            currentUser = currentUser,
                            onDelete = if (currentUser?.role?.canDeleteTransactions() == true) {
                                { viewModel.deleteGoldIntake(intake.intakeId, currentUser.username) }
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
fun GoldIntakeForm(
    viewModel: MainViewModel,
    currentUser: UserEntity?,
    currentModalBalance: Double,
    batches: List<com.example.data.database.entity.ProcessingBatchEntity>,
    rates: List<com.example.data.database.entity.GoldRateEntity>,
    onSuccess: () -> Unit
) {
    var selectedBatchNumber by remember { mutableStateOf("") }
    var processingName by remember { mutableStateOf("") }
    var weightText by remember { mutableStateOf("") }
    var selectedKadar by remember { mutableStateOf("24K") }
    var pricePerGramText by remember { mutableStateOf("") }
    var processingFeeText by remember { mutableStateOf("0") }
    var deductionType by remember { mutableStateOf(DeductionType.PERCENT) }
    var deductionValueText by remember { mutableStateOf("0") }
    var notesText by remember { mutableStateOf("") }
    var photoUri by remember { mutableStateOf<String?>(null) }

    var expandedTromolDropdown by remember { mutableStateOf(false) }
    var expandedKadarDropdown by remember { mutableStateOf(false) }

    // Update auto price and defaults when Kadar changes
    LaunchedEffect(selectedKadar, rates) {
        val rate = rates.find { it.kadarName.equals(selectedKadar, ignoreCase = true) }
        if (rate != null) {
            pricePerGramText = rate.buyPricePerGram.toLong().toString()
            if (rate.defaultDeductionPercent > 0) {
                deductionType = DeductionType.PERCENT
                deductionValueText = rate.defaultDeductionPercent.toString()
            } else if (rate.defaultDeductionNominal > 0) {
                deductionType = DeductionType.NOMINAL
                deductionValueText = rate.defaultDeductionNominal.toLong().toString()
            }
        }
    }

    // Update processing fee and name when Tromol changes
    LaunchedEffect(selectedBatchNumber, batches) {
        val batch = batches.find { it.batchNumber == selectedBatchNumber }
        if (batch != null) {
            processingName = batch.batchName
            processingFeeText = batch.processingFee.toLong().toString()
        }
    }

    // Calculations
    val weight = weightText.toDoubleOrNull() ?: 0.0
    val pricePerGram = pricePerGramText.toDoubleOrNull() ?: 0.0
    val processingFee = processingFeeText.toDoubleOrNull() ?: 0.0
    val grossValue = weight * pricePerGram

    val deductionAmount = when (deductionType) {
        DeductionType.PERCENT -> {
            val p = deductionValueText.toDoubleOrNull() ?: 0.0
            grossValue * (p / 100.0)
        }
        DeductionType.NOMINAL -> {
            deductionValueText.toDoubleOrNull() ?: 0.0
        }
        DeductionType.NONE -> 0.0
    }

    val netValue = (grossValue - deductionAmount + processingFee).coerceAtLeast(0.0)
    val balanceAfter = currentModalBalance - netValue

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
                actionDescription = "Input Pengambilan Emas Tromol"
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
                        text = "Data Penerimaan Emas Tromol",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )

                    // Pilih Tromol Dropdown
                    ExposedDropdownMenuBox(
                        expanded = expandedTromolDropdown,
                        onExpandedChange = { expandedTromolDropdown = !expandedTromolDropdown }
                    ) {
                        OutlinedTextField(
                            value = if (selectedBatchNumber.isNotBlank()) "$selectedBatchNumber ($processingName)" else "",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Pilih Nomor Tromol Pengolahan *") },
                            placeholder = { Text("Pilih batch tromol...") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedTromolDropdown) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor()
                                .testTag("select_tromol")
                        )
                        ExposedDropdownMenu(
                            expanded = expandedTromolDropdown,
                            onDismissRequest = { expandedTromolDropdown = false }
                        ) {
                            if (batches.isEmpty()) {
                                DropdownMenuItem(
                                    text = { Text("Belum ada tromol. Buat di menu Tromol dahulu.") },
                                    onClick = { expandedTromolDropdown = false }
                                )
                            } else {
                                batches.forEach { batch ->
                                    DropdownMenuItem(
                                        text = {
                                            Column {
                                                Text("${batch.batchNumber} - ${batch.batchName}", fontWeight = FontWeight.Bold)
                                                Text("Biaya: ${Formatters.formatRupiah(batch.processingFee)} | Pengelola: ${batch.managerName}", fontSize = 11.sp, color = Color.Gray)
                                            }
                                        },
                                        onClick = {
                                            selectedBatchNumber = batch.batchNumber
                                            expandedTromolDropdown = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    // Berat Emas (Gram)
                    OutlinedTextField(
                        value = weightText,
                        onValueChange = { weightText = it },
                        label = { Text("Berat Emas Diterima (Gram) *") },
                        placeholder = { Text("Contoh: 3.25 atau 10") },
                        suffix = { Text("gram") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_intake_weight")
                    )

                    // Pilih Kadar Dropdown
                    ExposedDropdownMenuBox(
                        expanded = expandedKadarDropdown,
                        onExpandedChange = { expandedKadarDropdown = !expandedKadarDropdown }
                    ) {
                        OutlinedTextField(
                            value = "Kadar $selectedKadar",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Kadar Emas *") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedKadarDropdown) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor()
                                .testTag("select_kadar")
                        )
                        ExposedDropdownMenu(
                            expanded = expandedKadarDropdown,
                            onDismissRequest = { expandedKadarDropdown = false }
                        ) {
                            rates.forEach { rate ->
                                DropdownMenuItem(
                                    text = {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(rate.kadarName, fontWeight = FontWeight.Bold)
                                            Text(Formatters.formatRupiah(rate.buyPricePerGram), color = GoldPrimary)
                                        }
                                    },
                                    onClick = {
                                        selectedKadar = rate.kadarName
                                        expandedKadarDropdown = false
                                    }
                                )
                            }
                        }
                    }

                    // Harga Emas per Gram (Editable)
                    OutlinedTextField(
                        value = pricePerGramText,
                        onValueChange = { pricePerGramText = it.filter { ch -> ch.isDigit() } },
                        label = { Text("Harga Emas per Gram (Rp) *") },
                        prefix = { Text("Rp ") },
                        supportingText = { Text("Harga default otomatis terisi, dapat disesuaikan per transaksi") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_intake_price")
                    )

                    // Biaya Pengolahan Tromol
                    OutlinedTextField(
                        value = processingFeeText,
                        onValueChange = { processingFeeText = it.filter { ch -> ch.isDigit() } },
                        label = { Text("Biaya Pengolahan Tromol Terkait (Rp)") },
                        prefix = { Text("Rp ") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Metode Potongan Modal
                    Text(
                        text = "Pengaturan Potongan Modal",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold)
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = deductionType == DeductionType.PERCENT,
                            onClick = { deductionType = DeductionType.PERCENT },
                            label = { Text("Persen (%)") }
                        )
                        FilterChip(
                            selected = deductionType == DeductionType.NOMINAL,
                            onClick = { deductionType = DeductionType.NOMINAL },
                            label = { Text("Nominal (Rp)") }
                        )
                        FilterChip(
                            selected = deductionType == DeductionType.NONE,
                            onClick = {
                                deductionType = DeductionType.NONE
                                deductionValueText = "0"
                            },
                            label = { Text("Tanpa Potongan") }
                        )
                    }

                    if (deductionType != DeductionType.NONE) {
                        OutlinedTextField(
                            value = deductionValueText,
                            onValueChange = { deductionValueText = it },
                            label = {
                                Text(if (deductionType == DeductionType.PERCENT) "Nilai Potongan (%)" else "Nilai Potongan (Rp)")
                            },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    OutlinedTextField(
                        value = notesText,
                        onValueChange = { notesText = it },
                        label = { Text("Keterangan Tambahan") },
                        placeholder = { Text("Kualitas, catatan timbangan, dll.") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    AttachmentUploader(
                        photoUri = photoUri,
                        onPhotoSelected = { photoUri = it },
                        label = "Foto Emas / Bukti Timbangan"
                    )

                    // Real-Time Calculation Preview Box
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
                                text = "Rincian Perhitungan Transaksi:",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Nilai Kotor (${weightText.ifBlank { "0" }}g × Rp ${Formatters.formatNumber(pricePerGram)}):", fontSize = 12.sp)
                                Text(Formatters.formatRupiah(grossValue), fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            }
                            if (deductionAmount > 0) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Potongan:", fontSize = 12.sp)
                                    Text("- ${Formatters.formatRupiah(deductionAmount)}", fontSize = 12.sp, color = DangerRed)
                                }
                            }
                            if (processingFee > 0) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Biaya Pengolahan:", fontSize = 12.sp)
                                    Text("+ ${Formatters.formatRupiah(processingFee)}", fontSize = 12.sp)
                                }
                            }
                            HorizontalDivider()
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("TOTAL POTONG MODAL:", fontWeight = FontWeight.Bold, color = GoldPrimary)
                                Text(Formatters.formatRupiah(netValue), fontWeight = FontWeight.ExtraBold, color = GoldPrimary)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Button(
                        onClick = {
                            if (selectedBatchNumber.isBlank()) {
                                viewModel.showMessage("Nomor tromol wajib dipilih.")
                                return@Button
                            }
                            if (weight <= 0) {
                                viewModel.showMessage("Berat emas wajib diisi.")
                                return@Button
                            }
                            if (pricePerGram <= 0) {
                                viewModel.showMessage("Harga emas belum ditentukan untuk kadar ini.")
                                return@Button
                            }

                            viewModel.showConfirmation(
                                ConfirmationDialogData(
                                    title = "Konfirmasi Pengambilan Emas",
                                    transactionType = "Pengambilan Emas",
                                    batchNumber = selectedBatchNumber,
                                    weightGrams = weight,
                                    kadarName = selectedKadar,
                                    pricePerGram = pricePerGram,
                                    grossValue = grossValue,
                                    deductionAmount = deductionAmount,
                                    netValue = netValue,
                                    currentBalance = currentModalBalance,
                                    balanceAfter = balanceAfter,
                                    details = mapOf(
                                        "Biaya Pengolahan" to Formatters.formatRupiah(processingFee),
                                        "Metode Potongan" to deductionType.name
                                    ),
                                    onConfirm = {
                                        viewModel.processGoldIntake(
                                            date = System.currentTimeMillis(),
                                            batchNumber = selectedBatchNumber,
                                            processingName = processingName,
                                            weightGrams = weight,
                                            kadarName = selectedKadar,
                                            pricePerGram = pricePerGram,
                                            processingFee = processingFee,
                                            deductionAmount = deductionAmount,
                                            deductionPercentage = if (deductionType == DeductionType.PERCENT) (deductionValueText.toDoubleOrNull() ?: 0.0) else 0.0,
                                            deductionMethod = deductionType.name,
                                            notes = notesText,
                                            photoUri = photoUri,
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
                            .testTag("btn_save_intake"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary)
                    ) {
                        Icon(Icons.Default.Save, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Simpan Penerimaan Emas", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun GoldIntakeCard(
    intake: GoldIntakeEntity,
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
                        text = intake.intakeId,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = GoldPrimary
                    )
                    Text(
                        text = Formatters.formatDateTime(intake.transactionDate),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Text(
                        text = "Kadar ${intake.kadarName}",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
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
                    Text("Tromol Pengolahan", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(intake.batchNumber, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold))
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Berat Diterima", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        Formatters.formatGram(intake.weightGrams),
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        color = SuccessGreen
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Harga/Gram", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(Formatters.formatRupiah(intake.pricePerGram), style = MaterialTheme.typography.bodySmall)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Potong Modal Bersih", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        Formatters.formatRupiah(intake.netValue),
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.ExtraBold),
                        color = GoldPrimary
                    )
                }
            }

            if (intake.notes.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    "Catatan: ${intake.notes}",
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
                        Text("Batalkan & Hapus Intake", fontSize = 12.sp)
                    }
                }
            }
        }
    }
}
