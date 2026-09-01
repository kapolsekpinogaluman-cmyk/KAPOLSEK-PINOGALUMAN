package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.database.entity.UserEntity
import com.example.data.model.TimeFilter
import com.example.data.model.UserRole
import com.example.ui.components.AppScreen
import com.example.ui.components.MetricCard
import com.example.ui.components.TimeFilterRow
import com.example.ui.theme.*
import com.example.utils.Formatters
import com.example.viewmodel.MainViewModel

@Composable
fun DashboardScreen(
    viewModel: MainViewModel,
    currentUser: UserEntity?,
    onNavigate: (AppScreen) -> Unit,
    modifier: Modifier = Modifier
) {
    val summary by viewModel.dashboardSummary.collectAsStateWithLifecycle()
    val timeFilter by viewModel.timeFilter.collectAsStateWithLifecycle()
    val inventory by viewModel.allInventory.collectAsStateWithLifecycle(emptyList())

    var isBalanceVisible by remember { mutableStateOf(true) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .testTag("screen_dashboard"),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 10.dp, bottom = 28.dp)
    ) {
        // Modern Executive Obsidian & Gold Hero Card
        item {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(
                        elevation = 8.dp,
                        shape = RoundedCornerShape(26.dp),
                        spotColor = Color(0x330F172A),
                        ambientColor = Color(0x1A0F172A)
                    )
                    .testTag("card_hero_summary"),
                shape = RoundedCornerShape(26.dp),
                color = Color.Transparent,
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    Brush.linearGradient(
                        listOf(Color(0xFFF59E0B).copy(alpha = 0.6f), Color(0xFF3B82F6).copy(alpha = 0.2f))
                    )
                )
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.linearGradient(
                                colors = listOf(
                                    Color(0xFF0F172A),
                                    Color(0xFF1E1B4B),
                                    Color(0xFF2E1065)
                                )
                            )
                        )
                        .padding(22.dp)
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF10B981))
                                )
                                Text(
                                    text = "SALDO MODAL AKTIF",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 1.5.sp,
                                        fontSize = 11.sp
                                    ),
                                    color = Color(0xFFFDE68A)
                                )
                            }

                            Surface(
                                onClick = { isBalanceVisible = !isBalanceVisible },
                                shape = CircleShape,
                                color = Color.White.copy(alpha = 0.12f)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(34.dp)
                                        .padding(6.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = if (isBalanceVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                        contentDescription = "Toggle saldo",
                                        tint = Color.White,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = if (isBalanceVisible) Formatters.formatRupiah(summary.currentModalBalance) else "Rp ••••••••••",
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 30.sp,
                                letterSpacing = (-0.8).sp
                            ),
                            color = Color.White
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Modern Progress Track Bar with Gold Sheen
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(Color.White.copy(alpha = 0.15f))
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .fillMaxWidth(0.78f)
                                    .clip(RoundedCornerShape(3.dp))
                                    .background(
                                        Brush.horizontalGradient(
                                            listOf(Color(0xFFF59E0B), Color(0xFFFBBF24))
                                        )
                                    )
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Diamond,
                                    contentDescription = null,
                                    tint = Color(0xFFFBBF24),
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    text = "Stok: ${Formatters.formatGram(summary.totalInventoryWeight)} emas murni",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium
                                    ),
                                    color = Color.White.copy(alpha = 0.9f)
                                )
                            }

                            Text(
                                text = "${summary.intakeCount} Ambil • ${summary.salesCount} Jual",
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                color = Color(0xFFCBD5E1)
                            )
                        }
                    }
                }
            }
        }

        // Modern Period Filter Container
        item {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(1.dp, RoundedCornerShape(20.dp), spotColor = Color(0x100F172A)),
                shape = RoundedCornerShape(20.dp),
                color = Color.White,
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                Icons.Default.DateRange,
                                contentDescription = null,
                                tint = GoldPrimary,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "Periode Waktu",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                ),
                                color = Color(0xFF0F172A)
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFFFFFBEB),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFDE68A))
                        ) {
                            Text(
                                text = timeFilter.label.uppercase(),
                                color = Color(0xFF78350F),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp
                                ),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    TimeFilterRow(
                        selected = timeFilter,
                        onSelect = { viewModel.setTimeFilter(it) }
                    )
                }
            }
        }

        // Modern Quick Action Buttons
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (currentUser?.role?.canManageCapital() == true) {
                    Button(
                        onClick = { onNavigate(AppScreen.TRANSAKSI) },
                        shape = RoundedCornerShape(18.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = GoldPrimary,
                            contentColor = Color.White
                        ),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp)
                            .testTag("btn_quick_modal")
                    ) {
                        Icon(Icons.Default.AddCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("+ Modal", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Button(
                    onClick = { onNavigate(AppScreen.TRANSAKSI) },
                    shape = RoundedCornerShape(18.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF0F172A),
                        contentColor = Color.White
                    ),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp)
                        .testTag("btn_quick_intake")
                ) {
                    Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Ambil Emas", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = { onNavigate(AppScreen.PENJUALAN) },
                    shape = RoundedCornerShape(18.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF10B981),
                        contentColor = Color.White
                    ),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp)
                        .testTag("btn_quick_sales")
                ) {
                    Icon(Icons.Default.PointOfSale, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Jual Emas", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Section Title: Statistik Operasional
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Statistik Operasional",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = (-0.3).sp,
                        fontSize = 17.sp
                    ),
                    color = Color(0xFF0F172A)
                )
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFFFFBEB),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFDE68A)),
                    modifier = Modifier.clickable { onNavigate(AppScreen.LAPORAN) }
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "LAPORAN",
                            color = Color(0xFF78350F),
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp
                            )
                        )
                        Icon(
                            Icons.Default.ChevronRight,
                            contentDescription = null,
                            tint = Color(0xFF78350F),
                            modifier = Modifier.size(12.dp)
                        )
                    }
                }
            }
        }

        // 1. Total Tambah Modal & 2. Total Nilai Emas
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                MetricCard(
                    title = "Total Tambah Modal",
                    value = Formatters.formatRupiah(summary.totalTambahModal),
                    icon = Icons.Default.AccountBalance,
                    badgeColor = Color(0xFFDBEAFE),
                    iconTint = Color(0xFF1E40AF),
                    modifier = Modifier.weight(1f)
                )
                MetricCard(
                    title = "Total Nilai Emas",
                    value = Formatters.formatRupiah(summary.totalInventoryValue),
                    icon = Icons.Default.Paid,
                    badgeColor = Color(0xFFFEF3C7),
                    iconTint = Color(0xFF92400E),
                    subtitle = "Stok di brankas",
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // 3. Total Berat Emas & 4. Total Penjualan
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                MetricCard(
                    title = "Total Berat Emas",
                    value = Formatters.formatGram(summary.totalInventoryWeight),
                    icon = Icons.Default.Scale,
                    badgeColor = Color(0xFFD1FAE5),
                    iconTint = Color(0xFF065F46),
                    subtitle = "Tersedia",
                    modifier = Modifier.weight(1f)
                )
                MetricCard(
                    title = "Total Penjualan",
                    value = Formatters.formatRupiah(summary.totalSalesRevenue),
                    icon = Icons.Default.PointOfSale,
                    badgeColor = Color(0xFFEDE9FE),
                    iconTint = Color(0xFF5B21B6),
                    subtitle = "${summary.salesCount} transaksi",
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // 5. Total Laba Bersih & 6. Total Biaya Pengolahan
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                MetricCard(
                    title = "Total Laba Bersih",
                    value = Formatters.formatRupiah(summary.totalNetProfit),
                    icon = Icons.Default.TrendingUp,
                    badgeColor = if (summary.totalNetProfit >= 0) Color(0xFFD1FAE5) else Color(0xFFFEE2E2),
                    iconTint = if (summary.totalNetProfit >= 0) Color(0xFF065F46) else Color(0xFF991B1B),
                    subtitle = if (summary.totalNetProfit >= 0) "Keuntungan" else "Kerugian",
                    modifier = Modifier.weight(1f)
                )
                MetricCard(
                    title = "Biaya Pengolahan",
                    value = Formatters.formatRupiah(summary.totalProcessingFees),
                    icon = Icons.Default.Engineering,
                    badgeColor = Color(0xFFFEF3C7),
                    iconTint = Color(0xFF92400E),
                    subtitle = "Tromol",
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // 7. Jumlah Tromol & 8. Jumlah Transaksi
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                MetricCard(
                    title = "Jumlah Tromol",
                    value = "${summary.totalBatchCount} Batch",
                    icon = Icons.Default.PrecisionManufacturing,
                    badgeColor = Color(0xFFF1F5F9),
                    iconTint = Color(0xFF475569),
                    modifier = Modifier.weight(1f)
                )
                MetricCard(
                    title = "Jumlah Transaksi",
                    value = "${summary.totalTransactionCount}",
                    icon = Icons.Default.ReceiptLong,
                    badgeColor = Color(0xFFF1F5F9),
                    iconTint = Color(0xFF475569),
                    subtitle = "${summary.intakeCount} Ambil | ${summary.salesCount} Jual",
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Modern Visual Gold Distribution Card
        item {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(1.dp, RoundedCornerShape(22.dp), spotColor = Color(0x100F172A)),
                shape = RoundedCornerShape(22.dp),
                color = Color.White,
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Komposisi Stok Emas",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                ),
                                color = Color(0xFF0F172A)
                            )
                            Text(
                                text = "Berdasarkan Kadar & Karat",
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                color = Color(0xFF64748B)
                            )
                        }

                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFFFFBEB)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.PieChart,
                                contentDescription = null,
                                tint = GoldPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    val activeInv = inventory.filter { it.availableWeightGrams > 0.001 }
                    if (activeInv.isEmpty()) {
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = Color(0xFFF8FAFC),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Belum ada persediaan emas aktif di gudang/inventory.",
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                                color = Color(0xFF64748B),
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                    } else {
                        val groupedByKadar = activeInv.groupBy { it.kadarName }
                        val totalWeight = activeInv.sumOf { it.availableWeightGrams }

                        groupedByKadar.forEach { (kadar, items) ->
                            val kadarWeight = items.sumOf { it.availableWeightGrams }
                            val fraction = if (totalWeight > 0) (kadarWeight / totalWeight).toFloat() else 0f
                            val percent = (fraction * 100).toInt()

                            Column(modifier = Modifier.padding(vertical = 6.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(6.dp)
                                                .clip(CircleShape)
                                                .background(GoldPrimary)
                                        )
                                        Text(
                                            text = "Kadar $kadar",
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                fontWeight = FontWeight.SemiBold,
                                                color = Color(0xFF0F172A)
                                            )
                                        )
                                    }

                                    Text(
                                        text = "${Formatters.formatGram(kadarWeight)} ($percent%)",
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = GoldPrimary
                                        )
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                LinearProgressIndicator(
                                    progress = { fraction },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(7.dp)
                                        .clip(RoundedCornerShape(4.dp)),
                                    color = GoldPrimary,
                                    trackColor = Color(0xFFFEF3C7)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

