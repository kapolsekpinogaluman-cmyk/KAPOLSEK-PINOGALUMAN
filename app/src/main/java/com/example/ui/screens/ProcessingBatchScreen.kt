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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.database.entity.ProcessingBatchEntity
import com.example.data.database.entity.UserEntity
import com.example.ui.components.EmptyStateView
import com.example.ui.components.SearchAndFilterHeader
import com.example.ui.components.StatusBadge
import com.example.ui.theme.DangerRed
import com.example.ui.theme.GoldPrimary
import com.example.utils.Formatters
import com.example.utils.IdGenerator
import com.example.viewmodel.MainViewModel

@Composable
fun ProcessingBatchScreen(
    viewModel: MainViewModel,
    currentUser: UserEntity?,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var showAddDialog by remember { mutableStateOf(false) }
    var editingBatch by remember { mutableStateOf<ProcessingBatchEntity?>(null) }

    val allBatches by viewModel.allBatches.collectAsStateWithLifecycle(emptyList())

    val filteredBatches = remember(allBatches, searchQuery) {
        if (searchQuery.isBlank()) allBatches
        else allBatches.filter {
            it.batchNumber.contains(searchQuery, ignoreCase = true) ||
                    it.batchName.contains(searchQuery, ignoreCase = true) ||
                    it.managerName.contains(searchQuery, ignoreCase = true)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .testTag("screen_tromol")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            SearchAndFilterHeader(
                query = searchQuery,
                onQueryChange = { searchQuery = it },
                placeholder = "Cari nomor tromol, nama pengolahan...",
                modifier = Modifier.weight(1f)
            )

            Spacer(modifier = Modifier.width(8.dp))

            FilledTonalButton(
                onClick = { showAddDialog = true },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.testTag("btn_add_tromol")
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(4.dp))
                Text("Tromol Baru", fontSize = 12.sp)
            }
        }

        if (filteredBatches.isEmpty()) {
            EmptyStateView(
                icon = Icons.Default.PrecisionManufacturing,
                title = "Data Tromol Belum Ada",
                description = "Tekan tombol Tromol Baru untuk mendaftarkan batch pengolahan tromol."
            )
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                items(filteredBatches, key = { it.batchNumber }) { batch ->
                    BatchCard(
                        batch = batch,
                        currentUser = currentUser,
                        onEdit = { editingBatch = batch },
                        onDelete = { viewModel.deleteBatch(batch.batchNumber, currentUser?.username ?: "OWNER") }
                    )
                }
            }
        }
    }

    if (showAddDialog) {
        AddEditBatchDialog(
            existing = null,
            onDismiss = { showAddDialog = false },
            onSave = { batchNumber, name, manager, rawWeight, fee, notes, status ->
                viewModel.addBatch(
                    batchNumber = batchNumber,
                    batchName = name,
                    managerName = manager,
                    startDate = System.currentTimeMillis(),
                    endDate = if (status == "SELESAI") System.currentTimeMillis() else null,
                    rawWeight = rawWeight,
                    processingFee = fee,
                    notes = notes,
                    status = status,
                    actor = currentUser?.username ?: "ADMIN"
                )
                showAddDialog = false
            }
        )
    }

    if (editingBatch != null) {
        AddEditBatchDialog(
            existing = editingBatch,
            onDismiss = { editingBatch = null },
            onSave = { batchNumber, name, manager, rawWeight, fee, notes, status ->
                viewModel.updateBatch(
                    editingBatch!!.copy(
                        batchName = name,
                        managerName = manager,
                        rawMaterialWeight = rawWeight,
                        processingFee = fee,
                        notes = notes,
                        status = status,
                        endDate = if (status == "SELESAI" && editingBatch!!.endDate == null) System.currentTimeMillis() else editingBatch!!.endDate
                    ),
                    actor = currentUser?.username ?: "ADMIN"
                )
                editingBatch = null
            }
        )
    }
}

@Composable
fun BatchCard(
    batch: ProcessingBatchEntity,
    currentUser: UserEntity?,
    onEdit: () -> Unit,
    onDelete: () -> Unit
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
                        text = batch.batchNumber,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = GoldPrimary
                    )
                    Text(
                        text = batch.batchName,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                    )
                }
                StatusBadge(status = batch.status)
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Pengelola / Pemilik", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(batch.managerName, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium))
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Biaya Pengolahan", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        Formatters.formatRupiah(batch.processingFee),
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
                Text(
                    "Tgl Masuk: ${Formatters.formatDate(batch.startDate)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (batch.rawMaterialWeight > 0) {
                    Text(
                        "Bahan: ${Formatters.formatGram(batch.rawMaterialWeight)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (batch.notes.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "Catatan: ${batch.notes}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                IconButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit", tint = MaterialTheme.colorScheme.primary)
                }
                if (currentUser?.role?.canDeleteTransactions() == true) {
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Default.Delete, contentDescription = "Hapus", tint = DangerRed)
                    }
                }
            }
        }
    }
}

@Composable
fun AddEditBatchDialog(
    existing: ProcessingBatchEntity?,
    onDismiss: () -> Unit,
    onSave: (batchNumber: String, name: String, manager: String, rawWeight: Double, fee: Double, notes: String, status: String) -> Unit
) {
    var batchNumber by remember { mutableStateOf(existing?.batchNumber ?: IdGenerator.generateBatchId()) }
    var name by remember { mutableStateOf(existing?.batchName ?: "") }
    var manager by remember { mutableStateOf(existing?.managerName ?: "") }
    var rawWeightText by remember { mutableStateOf(existing?.rawMaterialWeight?.toString() ?: "0") }
    var feeText by remember { mutableStateOf(existing?.processingFee?.toLong()?.toString() ?: "2000000") }
    var notes by remember { mutableStateOf(existing?.notes ?: "") }
    var status by remember { mutableStateOf(existing?.status ?: "PROSES") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (existing == null) "Tambah Tromol Pengolahan" else "Ubah Data Tromol") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = batchNumber,
                    onValueChange = { batchNumber = it },
                    label = { Text("Nomor Tromol (ID Unik)") },
                    enabled = existing == null,
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nama Pengolahan") },
                    placeholder = { Text("Contoh: Tromol Galangan A") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = manager,
                    onValueChange = { manager = it },
                    label = { Text("Pemilik / Pengelola") },
                    placeholder = { Text("Contoh: Pak Budi") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = feeText,
                    onValueChange = { feeText = it.filter { ch -> ch.isDigit() } },
                    label = { Text("Biaya Pengolahan (Rp) - Khusus Tromol Ini") },
                    prefix = { Text("Rp ") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = rawWeightText,
                    onValueChange = { rawWeightText = it },
                    label = { Text("Berat Bahan Masuk (gram/kg jika ada)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Keterangan") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val rawWeight = rawWeightText.toDoubleOrNull() ?: 0.0
                    val fee = feeText.toDoubleOrNull() ?: 0.0
                    onSave(batchNumber, name, manager, rawWeight, fee, notes, status)
                },
                colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary)
            ) {
                Text("Simpan Tromol")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) { Text("Batal") }
        }
    )
}
