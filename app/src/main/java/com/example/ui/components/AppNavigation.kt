package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.database.entity.UserEntity
import com.example.data.model.UserRole
import com.example.ui.theme.*

enum class AppScreen(
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val requiresRole: List<UserRole> = listOf(UserRole.OWNER, UserRole.ADMIN, UserRole.OPERATOR, UserRole.VIEWER)
) {
    BERANDA("Beranda", Icons.Filled.Home, Icons.Outlined.Home),
    PENJUALAN("Jual", Icons.Filled.PointOfSale, Icons.Outlined.PointOfSale),
    TRANSAKSI("Transaksi", Icons.Filled.AccountBalanceWallet, Icons.Outlined.AccountBalanceWallet),
    INVENTORY("Inventory", Icons.Filled.Inventory2, Icons.Outlined.Inventory2),
    PROFIL("Profil", Icons.Filled.AccountCircle, Icons.Outlined.AccountCircle),
    
    // Screens in Drawer / Sub-navigation
    RIWAYAT("Riwayat", Icons.Filled.History, Icons.Outlined.History),
    PENGOLAHAN("Tromol", Icons.Filled.PrecisionManufacturing, Icons.Outlined.PrecisionManufacturing),
    LABA_RUGI("Laba / Rugi", Icons.Filled.TrendingUp, Icons.Outlined.TrendingUp, listOf(UserRole.OWNER, UserRole.ADMIN)),
    LAPORAN("Laporan", Icons.Filled.Assessment, Icons.Outlined.Assessment),
    MASTER_KADAR("Kadar & Harga", Icons.Filled.PriceChange, Icons.Outlined.PriceChange, listOf(UserRole.OWNER, UserRole.ADMIN)),
    ACTIVITY_LOG("Log Aktivitas", Icons.Filled.ListAlt, Icons.Outlined.ListAlt)
}

// Exactly: BERANDA | JUAL | TRANSAKSI | INVENTORY | PROFIL
val BottomNavScreens = listOf(
    AppScreen.BERANDA,
    AppScreen.PENJUALAN,
    AppScreen.TRANSAKSI,
    AppScreen.INVENTORY,
    AppScreen.PROFIL
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppTopBar(
    currentScreen: AppScreen,
    currentUser: UserEntity?,
    onOpenDrawer: () -> Unit,
    onNavigate: (AppScreen) -> Unit,
    onRoleSwitch: (UserRole) -> Unit,
    onLogout: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var showTopRightMenu by remember { mutableStateOf(false) }
    val isProfileScreen = currentScreen == AppScreen.PROFIL

    val initials = remember(currentUser) {
        val name = currentUser?.fullName ?: currentUser?.username ?: "US"
        name.split(" ").filter { it.isNotBlank() }.map { it.first().uppercase() }.take(2).joinToString("")
            .ifEmpty { "EM" }
    }

    Surface(
        color = Color.White,
        modifier = modifier
            .fillMaxWidth()
            .shadow(2.dp, spotColor = Color(0x1A0F172A), ambientColor = Color(0x080F172A)),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFF1F5F9))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                if (isProfileScreen) {
                    Surface(
                        onClick = onOpenDrawer,
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFFF8FAFC),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
                        modifier = Modifier.testTag("btn_open_menu_drawer")
                    ) {
                        Box(modifier = Modifier.size(40.dp), contentAlignment = Alignment.Center) {
                            Icon(
                                Icons.Default.Menu,
                                contentDescription = "Menu Navigasi",
                                tint = Color(0xFF0F172A),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))
                } else {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFFEF3C7))
                            .border(1.dp, GoldPrimary, RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = currentScreen.selectedIcon,
                            contentDescription = null,
                            tint = Color(0xFF78350F),
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))
                }

                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = currentScreen.title,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = (-0.5).sp,
                                fontSize = 18.sp
                            ),
                            color = Color(0xFF0F172A)
                        )
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(GoldPrimary)
                        )
                    }
                    Text(
                        text = "Pembukuan Emas • ${currentUser?.role?.displayName ?: "Owner"}",
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                        color = Color(0xFF64748B)
                    )
                }
            }

            if (isProfileScreen) {
                // Top Right: 3 lines / Avatar sub menu with rich options
                Box {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            onClick = { showTopRightMenu = true },
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFFF8FAFC),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
                            modifier = Modifier.testTag("btn_top_right_menu")
                        ) {
                            Box(modifier = Modifier.size(40.dp), contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.Default.MoreVert,
                                    contentDescription = "Menu Profil & Opsi",
                                    tint = Color(0xFF0F172A),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }

                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.linearGradient(
                                        colors = listOf(Color(0xFFFEF3C7), Color(0xFFFDE68A))
                                    )
                                )
                                .border(1.5.dp, GoldPrimary, CircleShape)
                                .clickable { showTopRightMenu = true }
                                .testTag("btn_user_profile_avatar"),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = initials,
                                color = Color(0xFF78350F),
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                            )
                        }
                    }

                DropdownMenu(
                    expanded = showTopRightMenu,
                    onDismissRequest = { showTopRightMenu = false },
                    modifier = Modifier
                        .background(Color.White)
                        .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(16.dp))
                        .widthIn(min = 250.dp)
                ) {
                    // Header inside dropdown
                    Column(
                        modifier = Modifier
                            .background(Color(0xFFFFFBEB))
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(GoldPrimary),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = initials,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                            }
                            Column {
                                Text(
                                    text = currentUser?.fullName ?: "Profil Saya",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = Color(0xFF0F172A)
                                )
                                Text(
                                    text = currentUser?.role?.displayName ?: "Owner",
                                    fontSize = 11.sp,
                                    color = GoldPrimary,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "WA: ${currentUser?.phoneWhatsapp ?: "0812-3456-7890"}",
                            fontSize = 11.sp,
                            color = Color(0xFF64748B)
                        )
                    }
                    HorizontalDivider(color = Color(0xFFE2E8F0))

                    DropdownMenuItem(
                        text = { Text("Edit Profil (Foto, Nama, WA)", fontSize = 13.sp) },
                        leadingIcon = { Icon(Icons.Default.ManageAccounts, contentDescription = null, tint = GoldPrimary, modifier = Modifier.size(18.dp)) },
                        onClick = {
                            onNavigate(AppScreen.PROFIL)
                            showTopRightMenu = false
                        }
                    )

                    DropdownMenuItem(
                        text = { Text("Pola Keamanan (Biometrik, PIN, Pola)", fontSize = 13.sp) },
                        leadingIcon = { Icon(Icons.Default.Security, contentDescription = null, tint = GoldPrimary, modifier = Modifier.size(18.dp)) },
                        onClick = {
                            onNavigate(AppScreen.PROFIL)
                            showTopRightMenu = false
                        }
                    )

                    DropdownMenuItem(
                        text = { Text("Kelola Pengguna (Tambah Viewer)", fontSize = 13.sp) },
                        leadingIcon = { Icon(Icons.Default.GroupAdd, contentDescription = null, tint = GoldPrimary, modifier = Modifier.size(18.dp)) },
                        onClick = {
                            onNavigate(AppScreen.PROFIL)
                            showTopRightMenu = false
                        }
                    )

                    DropdownMenuItem(
                        text = { Text("Menu Riwayat (Modal, Ambil, Jual)", fontSize = 13.sp) },
                        leadingIcon = { Icon(Icons.Default.History, contentDescription = null, tint = GoldPrimary, modifier = Modifier.size(18.dp)) },
                        onClick = {
                            onNavigate(AppScreen.RIWAYAT)
                            showTopRightMenu = false
                        }
                    )

                    HorizontalDivider(color = Color(0xFFE2E8F0))

                    Text(
                        text = "BERALIH ROLE AKUN:",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF94A3B8),
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                    )

                    DropdownMenuItem(
                        text = { Text("Role: OWNER (Pemilik)", fontSize = 12.sp) },
                        leadingIcon = { Icon(Icons.Default.AdminPanelSettings, contentDescription = null, tint = GoldPrimary, modifier = Modifier.size(16.dp)) },
                        onClick = {
                            onRoleSwitch(UserRole.OWNER)
                            showTopRightMenu = false
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Role: ADMIN (Operasional)", fontSize = 12.sp) },
                        leadingIcon = { Icon(Icons.Default.SupervisorAccount, contentDescription = null, tint = Color(0xFF3B82F6), modifier = Modifier.size(16.dp)) },
                        onClick = {
                            onRoleSwitch(UserRole.ADMIN)
                            showTopRightMenu = false
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Role: OPERATOR (Lapangan)", fontSize = 12.sp) },
                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = Color(0xFF10B981), modifier = Modifier.size(16.dp)) },
                        onClick = {
                            onRoleSwitch(UserRole.OPERATOR)
                            showTopRightMenu = false
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Role: VIEWER (Hanya Melihat)", fontSize = 12.sp) },
                        leadingIcon = { Icon(Icons.Default.Visibility, contentDescription = null, tint = Color(0xFF64748B), modifier = Modifier.size(16.dp)) },
                        onClick = {
                            onRoleSwitch(UserRole.VIEWER)
                            showTopRightMenu = false
                        }
                    )

                    HorizontalDivider(color = Color(0xFFE2E8F0))

                    DropdownMenuItem(
                        text = { Text("Keluar Akun (Logout)", fontSize = 12.sp, color = DangerRed, fontWeight = FontWeight.Bold) },
                        leadingIcon = { Icon(Icons.Default.Logout, contentDescription = "Logout", tint = DangerRed, modifier = Modifier.size(16.dp)) },
                        onClick = {
                            showTopRightMenu = false
                            onLogout()
                        }
                    )
                }
            }
        }
    }
}
}

@Composable
fun AppBottomBar(
    currentScreen: AppScreen,
    onNavigate: (AppScreen) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        color = Color.White,
        shadowElevation = 8.dp,
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFF1F5F9)),
        modifier = modifier.fillMaxWidth()
    ) {
        NavigationBar(
            containerColor = Color.White,
            tonalElevation = 0.dp,
            windowInsets = WindowInsets.navigationBars,
            modifier = Modifier.testTag("bottom_navigation_bar")
        ) {
            BottomNavScreens.forEach { screen ->
                val isSelected = currentScreen == screen
                NavigationBarItem(
                    selected = isSelected,
                    onClick = { onNavigate(screen) },
                    icon = {
                        Icon(
                            imageVector = if (isSelected) screen.selectedIcon else screen.unselectedIcon,
                            contentDescription = screen.title,
                            modifier = Modifier.size(22.dp)
                        )
                    },
                    label = {
                        Text(
                            text = screen.title.uppercase(),
                            maxLines = 1,
                            fontSize = 10.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            letterSpacing = 0.5.sp
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFF78350F),
                        selectedTextColor = Color(0xFF78350F),
                        unselectedIconColor = Color(0xFF64748B),
                        unselectedTextColor = Color(0xFF64748B),
                        indicatorColor = Color(0xFFFEF3C7)
                    ),
                    modifier = Modifier.testTag("nav_item_${screen.name.lowercase()}")
                )
            }
        }
    }
}

