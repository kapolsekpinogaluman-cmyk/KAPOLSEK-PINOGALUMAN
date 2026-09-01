package com.example.ui.screens

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.database.entity.UserEntity
import com.example.data.model.ReportType
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.SuccessGreen
import com.example.utils.ExportHelper
import com.example.utils.Formatters
import com.example.viewmodel.MainViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsScreen(
    viewModel: MainViewModel,
    currentUser: UserEntity?,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val coroutineScope = rememberCoroutineScope()

    var selectedReportType by remember { mutableStateOf(ReportType.MODAL) }
    var expandedDropdown by remember { mutableStateOf(false) }

    val allCapital by viewModel.allCapitalTransactions.collectAsStateWithLifecycle(emptyList())
    val allIntakes by viewModel.allIntakes.collectAsStateWithLifecycle(emptyList())
    val allBatches by viewModel.allBatches.collectAsStateWithLifecycle(emptyList())
    val allInventory by viewModel.allInventory.collectAsStateWithLifecycle(emptyList())
    val allSales by viewModel.allSales.collectAsStateWithLifecycle(emptyList())
    val allLogs by viewModel.allLogs.collectAsStateWithLifecycle(emptyList())
    val summary by viewModel.dashboardSummary.collectAsStateWithLifecycle()

    fun shareCsvFile(content: String, filename: String) {
        try {
            val file = File(context.cacheDir, filename)
            file.writeText(content)
            val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/csv"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(shareIntent, "Bagikan Laporan CSV"))
        } catch (e: Exception) {
            // Fallback copy content
            clipboardManager.setText(AnnotatedString(content))
            viewModel.showMessage("Teks CSV berhasil disalin ke clipboard.")
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .testTag("screen_reports"),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(vertical = 12.dp)
    ) {
        item {
            Text(
                text = "Pusat Laporan & Ekspor Data Bisnis",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
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
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("Pilih Jenis Laporan", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))

                    ExposedDropdownMenuBox(
                        expanded = expandedDropdown,
                        onExpandedChange = { expandedDropdown = !expandedDropdown }
                    ) {
                        OutlinedTextField(
                            value = selectedReportType.title,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Jenis Laporan") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedDropdown) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor()
                                .testTag("select_report_type")
                        )
                        ExposedDropdownMenu(
                            expanded = expandedDropdown,
                            onDismissRequest = { expandedDropdown = false }
                        ) {
                            ReportType.values().forEach { type ->
                                DropdownMenuItem(
                                    text = { Text(type.title) },
                                    onClick = {
                                        selectedReportType = type
                                        expandedDropdown = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = {
                                val csv = when (selectedReportType) {
                                    ReportType.MODAL -> ExportHelper.exportCapitalTransactionsToCsv(allCapital)
                                    ReportType.PENGAMBILAN_EMAS -> ExportHelper.exportGoldIntakesToCsv(allIntakes)
                                    ReportType.TROMOL -> ExportHelper.exportBatchesToCsv(allBatches)
                                    ReportType.INVENTORY -> ExportHelper.exportInventoryToCsv(allInventory)
                                    ReportType.PENJUALAN -> ExportHelper.exportSalesToCsv(allSales)
                                    ReportType.LABA_RUGI -> ExportHelper.exportSalesToCsv(allSales)
                                    ReportType.ARUS_KAS -> ExportHelper.exportCapitalTransactionsToCsv(allCapital)
                                    ReportType.AKTIVITAS_USER -> ExportHelper.exportActivityLogsToCsv(allLogs)
                                }
                                shareCsvFile(csv, "Laporan_${selectedReportType.name}_${System.currentTimeMillis()}.csv")
                            },
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("btn_export_csv")
                        ) {
                            Icon(Icons.Default.Download, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Export CSV", fontWeight = FontWeight.Bold)
                        }

                        OutlinedButton(
                            onClick = {
                                val summaryText = buildString {
                                    appendLine("=== LAPORAN BISNIS EMAS & MODAL ===")
                                    appendLine("Jenis Laporan: ${selectedReportType.title}")
                                    appendLine("Tanggal Cetak: ${Formatters.formatDateTime(System.currentTimeMillis())}")
                                    appendLine("User: ${currentUser?.fullName} (${currentUser?.role?.name})")
                                    appendLine("----------------------------------------")
                                    appendLine("Saldo Modal Saat Ini: ${Formatters.formatRupiah(summary.currentModalBalance)}")
                                    appendLine("Total Stok Emas: ${Formatters.formatGram(summary.totalInventoryWeight)}")
                                    appendLine("Total Nilai Stok: ${Formatters.formatRupiah(summary.totalInventoryValue)}")
                                    appendLine("Total Penjualan: ${Formatters.formatRupiah(summary.totalSalesRevenue)}")
                                    appendLine("Total Laba Bersih: ${Formatters.formatRupiah(summary.totalNetProfit)}")
                                    appendLine("Total Batch Tromol: ${summary.totalBatchCount}")
                                    appendLine("========================================")
                                }
                                clipboardManager.setText(AnnotatedString(summaryText))
                                viewModel.showMessage("Ringkasan laporan berhasil disalin ke clipboard.")
                            },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.ContentCopy, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Salin Ringkasan")
                        }
                    }
                }
            }
        }

        // Preview of current selected report
        item {
            Text(
                text = "Pratinjau Data (${selectedReportType.title})",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
            )
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    when (selectedReportType) {
                        ReportType.MODAL -> {
                            Text("Total Transaksi Modal: ${allCapital.size} Catatan", fontWeight = FontWeight.Bold)
                            allCapital.take(5).forEach { tx ->
                                Text("• ${Formatters.formatDate(tx.transactionDate)} - ${tx.type.name} - ${Formatters.formatRupiah(tx.amount)} (${tx.sourceOrTarget})", fontSize = 12.sp)
                            }
                        }
                        ReportType.PENGAMBILAN_EMAS -> {
                            Text("Total Pengambilan Emas: ${allIntakes.size} Transaksi", fontWeight = FontWeight.Bold)
                            allIntakes.take(5).forEach { intake ->
                                Text("• ${intake.intakeId} | Tromol: ${intake.batchNumber} | ${Formatters.formatGram(intake.weightGrams)} (${intake.kadarName}) | Bersih: ${Formatters.formatRupiah(intake.netValue)}", fontSize = 12.sp)
                            }
                        }
                        ReportType.TROMOL -> {
                            Text("Total Batch Pengolahan: ${allBatches.size} Tromol", fontWeight = FontWeight.Bold)
                            allBatches.take(5).forEach { batch ->
                                Text("• ${batch.batchNumber} - ${batch.batchName} | Biaya: ${Formatters.formatRupiah(batch.processingFee)} | Status: ${batch.status}", fontSize = 12.sp)
                            }
                        }
                        ReportType.INVENTORY -> {
                            Text("Total Item Inventory: ${allInventory.size} Item", fontWeight = FontWeight.Bold)
                            allInventory.take(5).forEach { inv ->
                                Text("• ${inv.inventoryId} - Kadar ${inv.kadarName} | Sisa: ${Formatters.formatGram(inv.availableWeightGrams)} / ${Formatters.formatGram(inv.initialWeightGrams)} | Status: ${inv.status.name}", fontSize = 12.sp)
                            }
                        }
                        ReportType.PENJUALAN -> {
                            Text("Total Transaksi Penjualan: ${allSales.size} Transaksi", fontWeight = FontWeight.Bold)
                            allSales.take(5).forEach { sale ->
                                Text("• ${sale.salesId} - ${sale.buyerName} | Omzet: ${Formatters.formatRupiah(sale.totalSalesAmount)} | Laba: ${Formatters.formatRupiah(sale.profitAmount)}", fontSize = 12.sp)
                            }
                        }
                        ReportType.LABA_RUGI -> {
                            Text("Ringkasan Laba / Rugi Bersih Usaha", fontWeight = FontWeight.Bold)
                            Text("Total Omzet Penjualan: ${Formatters.formatRupiah(summary.totalSalesRevenue)}", fontSize = 12.sp)
                            Text("Total HPP Modal: ${Formatters.formatRupiah(summary.totalCogs)}", fontSize = 12.sp)
                            Text("Total Biaya Terkait: ${Formatters.formatRupiah(summary.totalExpenses)}", fontSize = 12.sp)
                            Text("Total Laba Bersih: ${Formatters.formatRupiah(summary.totalNetProfit)}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = SuccessGreen)
                        }
                        ReportType.ARUS_KAS -> {
                            Text("Laporan Arus Kas Keluar & Masuk", fontWeight = FontWeight.Bold)
                            Text("Kas Masuk (Tambah Modal + Jual Emas): ${Formatters.formatRupiah(summary.totalTambahModal + summary.totalSalesRevenue)}", fontSize = 12.sp, color = SuccessGreen)
                            Text("Kas Keluar (Ambil Emas + Kurang Modal): ${Formatters.formatRupiah(summary.totalIntakesNetValue + summary.totalExpenses)}", fontSize = 12.sp, color = Color.Red)
                        }
                        ReportType.AKTIVITAS_USER -> {
                            Text("Total Log Audit: ${allLogs.size} Aktivitas", fontWeight = FontWeight.Bold)
                            allLogs.take(5).forEach { log ->
                                Text("• [${log.actionType}] ${log.username}: ${log.details}", fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}
