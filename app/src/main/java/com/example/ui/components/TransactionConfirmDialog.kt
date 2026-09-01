package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.ui.theme.GoldPrimary
import com.example.utils.Formatters
import com.example.viewmodel.ConfirmationDialogData

@Composable
fun TransactionConfirmDialog(
    data: ConfirmationDialogData,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .testTag("dialog_transaction_confirm"),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = "Konfirmasi",
                    tint = GoldPrimary,
                    modifier = Modifier.size(44.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = data.title,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )

                Text(
                    text = "Mohon periksa rincian sebelum transaksi disimpan ke sistem.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(12.dp))

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            shape = RoundedCornerShape(12.dp)
                        )
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ConfirmRow(label = "Jenis Transaksi", value = data.transactionType)

                    if (!data.batchNumber.isNullOrBlank()) {
                        ConfirmRow(label = "Tromol", value = data.batchNumber)
                    }

                    if (data.weightGrams > 0) {
                        ConfirmRow(label = "Berat Emas", value = Formatters.formatGram(data.weightGrams))
                    }

                    if (data.kadarName.isNotBlank()) {
                        ConfirmRow(label = "Kadar Emas", value = data.kadarName)
                    }

                    if (data.pricePerGram > 0) {
                        ConfirmRow(label = "Harga per Gram", value = Formatters.formatRupiah(data.pricePerGram))
                    }

                    if (data.grossValue > 0) {
                        ConfirmRow(label = "Nilai Kotor", value = Formatters.formatRupiah(data.grossValue))
                    }

                    if (data.deductionAmount > 0) {
                        ConfirmRow(label = "Potongan Modal", value = "- " + Formatters.formatRupiah(data.deductionAmount))
                    }

                    if (data.netValue > 0) {
                        ConfirmRow(
                            label = "Nilai Bersih / Efektif",
                            value = Formatters.formatRupiah(data.netValue),
                            isHighlight = true
                        )
                    }

                    for ((key, value) in data.details) {
                        ConfirmRow(label = key, value = value)
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                    ConfirmRow(
                        label = "Saldo Modal Sebelumnya",
                        value = Formatters.formatRupiah(data.currentBalance)
                    )

                    ConfirmRow(
                        label = "Estimasi Saldo Setelah Transaksi",
                        value = Formatters.formatRupiah(data.balanceAfter),
                        isHighlight = true,
                        highlightColor = if (data.balanceAfter >= 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("btn_confirm_cancel"),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Batal")
                    }

                    Button(
                        onClick = {
                            data.onConfirm()
                            onDismiss()
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("btn_confirm_save"),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary)
                    ) {
                        Text("Simpan", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun ConfirmRow(
    label: String,
    value: String,
    isHighlight: Boolean = false,
    highlightColor: Color = MaterialTheme.colorScheme.primary
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = if (isHighlight) MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold) else MaterialTheme.typography.bodySmall,
            color = if (isHighlight) highlightColor else MaterialTheme.colorScheme.onSurface
        )
    }
}
