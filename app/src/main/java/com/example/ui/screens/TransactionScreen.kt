package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Download
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.database.entity.UserEntity
import com.example.ui.theme.GoldPrimary
import com.example.viewmodel.MainViewModel

enum class TransactionSubMenu(val title: String, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
    MODAL("Modal Usaha", Icons.Default.AccountBalanceWallet),
    AMBIL_EMAS("Ambil Emas (Tromol)", Icons.Default.Download)
}

@Composable
fun TransactionScreen(
    viewModel: MainViewModel,
    currentUser: UserEntity?,
    initialSubMenu: TransactionSubMenu = TransactionSubMenu.MODAL,
    modifier: Modifier = Modifier
) {
    var selectedSubMenu by remember { mutableStateOf(initialSubMenu) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("screen_transaction_unified")
    ) {
        // Sub Menu Tabs Bar (Modal Usaha | Ambil Emas)
        Surface(
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 2.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            TabRow(
                selectedTabIndex = selectedSubMenu.ordinal,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = GoldPrimary,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
            ) {
                TransactionSubMenu.values().forEach { subMenu ->
                    val isSelected = selectedSubMenu == subMenu
                    Tab(
                        selected = isSelected,
                        onClick = { selectedSubMenu = subMenu },
                        text = {
                            Row(
                                modifier = Modifier.padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = subMenu.icon,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp),
                                    tint = if (isSelected) GoldPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = subMenu.title,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    fontSize = 14.sp
                                )
                            }
                        },
                        selectedContentColor = GoldPrimary,
                        unselectedContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.testTag("tab_transaksi_${subMenu.name.lowercase()}")
                    )
                }
            }
        }

        // Exact content of each menu preserved seamlessly
        Box(modifier = Modifier.fillMaxSize()) {
            when (selectedSubMenu) {
                TransactionSubMenu.MODAL -> {
                    CapitalScreen(
                        viewModel = viewModel,
                        currentUser = currentUser
                    )
                }
                TransactionSubMenu.AMBIL_EMAS -> {
                    GoldIntakeScreen(
                        viewModel = viewModel,
                        currentUser = currentUser
                    )
                }
            }
        }
    }
}
