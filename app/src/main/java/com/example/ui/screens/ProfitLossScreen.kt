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
import com.example.data.database.entity.GoldSalesEntity
import com.example.data.database.entity.UserEntity
import com.example.data.model.TimeFilter
import com.example.ui.components.EmptyStateView
import com.example.ui.components.MetricCard
import com.example.ui.components.TimeFilterRow
import com.example.ui.theme.*
import com.example.utils.Formatters
import com.example.viewmodel.MainViewModel

@Composable
fun ProfitLossScreen(
    viewModel: MainViewModel,
    currentUser: UserEntity?,
    modifier: Modifier = Modifier
) {
    val summary by viewModel.dashboardSummary.collectAsStateWithLifecycle()
    val timeFilter by viewModel.timeFilter.collectAsStateWithLifecycle()
    val allSales by viewModel.allSales.collectAsStateWithLifecycle(emptyList())

    val totalRevenue = summary.totalSalesRevenue
    val totalCogs = summary.totalCogs
    val totalExpenses = summary.totalExpenses
    val netProfit = summary.totalNetProfit
    val marginPercent = if (totalRevenue > 0) ((netProfit / totalRevenue) * 100).toInt() else 0

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .testTag("screen_profit_loss")
    ) {
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(14.dp),
            contentPadding = PaddingValues(vertical = 12.dp)
        ) {
            item {
                Text(
                    text = "Filter Periode Laporan Laba / Rugi",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                )
                Spacer(modifier = Modifier.height(6.dp))
                TimeFilterRow(
                    selected = timeFilter,
                    onSelect = { viewModel.setTimeFilter(it) }
                )
            }

            // Executive Summary Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (netProfit >= 0) SlateNavyDark else Color(0xFF450A0A)
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp)
                    ) {
                        Text(
                            text = "TOTAL LABA / RUGI BERSIH (${timeFilter.label.uppercase()})",
                            style = MaterialTheme.typography.labelMedium.copy(letterSpacing = 1.sp),
                            color = if (netProfit >= 0) GoldLight else Color(0xFFFCA5A5)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = Formatters.formatRupiah(netProfit),
                            style = MaterialTheme.typography.headlineLarge.copy(
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White
                            )
                        )
                        Text(
                            text = "Margin Laba Bersih: $marginPercent% dari Total Omzet Penjualan",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.LightGray
                        )

                        Spacer(modifier = Modifier.height(14.dp))
                        HorizontalDivider(color = Color(0xFF334155))
                        Spacer(modifier = Modifier.height(12.dp))

                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Total Omzet Penjualan", style = MaterialTheme.typography.bodySmall, color = Color.LightGray)
                                Text(Formatters.formatRupiah(totalRevenue), style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold), color = SuccessGreen)
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Total Modal Pokok Terpakai (COGS)", style = MaterialTheme.typography.bodySmall, color = Color.LightGray)
                                Text("- ${Formatters.formatRupiah(totalCogs)}", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold), color = Color(0xFFF87171))
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Total Biaya Operasional / Terkait", style = MaterialTheme.typography.bodySmall, color = Color.LightGray)
                                Text("- ${Formatters.formatRupiah(totalExpenses)}", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold), color = Color(0xFFF87171))
                            }
                        }
                    }
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    MetricCard(
                        title = "Omzet Penjualan",
                        value = Formatters.formatRupiah(totalRevenue),
                        icon = Icons.Default.PointOfSale,
                        badgeColor = SuccessGreenLight,
                        iconTint = SuccessGreen,
                        modifier = Modifier.weight(1f)
                    )
                    MetricCard(
                        title = "Modal Pokok (COGS)",
                        value = Formatters.formatRupiah(totalCogs),
                        icon = Icons.Default.Inventory2,
                        badgeColor = Color(0xFFFEF3C7),
                        iconTint = Color(0xFFD97706),
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            item {
                Text(
                    text = "Rincian Laba Per Transaksi Penjualan",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            }

            if (allSales.isEmpty()) {
                item {
                    EmptyStateView(
                        icon = Icons.Default.TrendingUp,
                        title = "Belum Ada Catatan Laba/Rugi",
                        description = "Laba/Rugi dihitung secara real-time berdasarkan setiap transaksi penjualan emas."
                    )
                }
            } else {
                items(allSales, key = { it.salesId }) { sale ->
                    ProfitLossItemCard(sale = sale)
                }
            }
        }
    }
}

@Composable
fun ProfitLossItemCard(sale: GoldSalesEntity) {
    val margin = if (sale.totalSalesAmount > 0) ((sale.profitAmount / sale.totalSalesAmount) * 100).toInt() else 0

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = sale.salesId,
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = GoldPrimary
                    )
                    Text(
                        text = "${Formatters.formatDateTime(sale.transactionDate)} • ${sale.buyerName}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "+ ${Formatters.formatRupiah(sale.profitAmount)}",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = if (sale.profitAmount >= 0) SuccessGreen else DangerRed
                    )
                    Text(
                        text = "Margin: $margin%",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Penjualan: ${Formatters.formatRupiah(sale.totalSalesAmount)} (${sale.kadarName} - ${Formatters.formatGram(sale.weightSoldGrams)})",
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    text = "HPP: ${Formatters.formatRupiah(sale.costOfGoodsSold)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
