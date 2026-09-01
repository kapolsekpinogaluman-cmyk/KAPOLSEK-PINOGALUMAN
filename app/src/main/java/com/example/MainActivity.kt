package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.database.AppDatabase
import com.example.data.model.UserRole
import com.example.data.repository.GoldBookkeepingRepository
import com.example.ui.components.*
import com.example.ui.screens.*
import com.example.ui.theme.CatatanModalEmasTheme
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.SlateNavyDark
import com.example.viewmodel.AuthViewModel
import com.example.viewmodel.MainViewModel
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val database = AppDatabase.getDatabase(applicationContext)
        val repository = GoldBookkeepingRepository(database)

        val viewModelFactory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return when {
                    modelClass.isAssignableFrom(AuthViewModel::class.java) -> {
                        AuthViewModel(repository) as T
                    }
                    modelClass.isAssignableFrom(MainViewModel::class.java) -> {
                        MainViewModel(repository) as T
                    }
                    else -> throw IllegalArgumentException("Unknown ViewModel class")
                }
            }
        }

        setContent {
            CatatanModalEmasTheme {
                val authViewModel: AuthViewModel = viewModel(factory = viewModelFactory)
                val mainViewModel: MainViewModel = viewModel(factory = viewModelFactory)

                GoldBookkeepingApp(
                    authViewModel = authViewModel,
                    mainViewModel = mainViewModel
                )
            }
        }
    }
}

@Composable
fun GoldBookkeepingApp(
    authViewModel: AuthViewModel,
    mainViewModel: MainViewModel
) {
    val currentUser by authViewModel.currentUser.collectAsStateWithLifecycle()
    val confirmationData by mainViewModel.confirmationDialogData.collectAsStateWithLifecycle()
    val userMessage by mainViewModel.userMessage.collectAsStateWithLifecycle()

    var currentScreen by remember { mutableStateOf(AppScreen.BERANDA) }
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    // Handle snackbar messages
    LaunchedEffect(userMessage) {
        userMessage?.let {
            snackbarHostState.showSnackbar(it)
            mainViewModel.clearMessage()
        }
    }

    if (currentUser == null) {
        LoginScreen(
            authViewModel = authViewModel,
            onLoginSuccess = {
                currentScreen = AppScreen.BERANDA
                mainViewModel.showMessage("Berhasil masuk ke sistem!")
            }
        )
        return
    }

    // Handle Back Press cleanly (close drawer or return to Beranda)
    BackHandler(enabled = drawerState.isOpen) {
        scope.launch { drawerState.close() }
    }

    BackHandler(enabled = !drawerState.isOpen && currentScreen != AppScreen.BERANDA) {
        currentScreen = AppScreen.BERANDA
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                modifier = Modifier.width(310.dp),
                drawerContainerColor = Color.White,
                drawerShape = RoundedCornerShape(topEnd = 28.dp, bottomEnd = 28.dp)
            ) {
                // Drawer Header with Modern Obsidian & Gold Theme
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.linearGradient(
                                colors = listOf(Color(0xFF0F172A), Color(0xFF1E1B4B))
                            )
                        )
                        .padding(22.dp)
                ) {
                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(
                                        Brush.linearGradient(
                                            colors = listOf(Color(0xFFFEF3C7), Color(0xFFFDE68A))
                                        )
                                    )
                                    .border(1.dp, GoldPrimary, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Diamond,
                                    contentDescription = null,
                                    tint = Color(0xFF78350F),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "Catatan Emas & Modal",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = (-0.3).sp,
                                    color = Color.White
                                )
                            )
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Sistem Pembukuan Emas & Tromol",
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                            color = Color(0xFF94A3B8)
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        if (currentUser != null) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RoleBadge(role = currentUser!!.role.name)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = currentUser!!.fullName,
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color.White
                                    )
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Navigation Items
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 12.dp)
                ) {
                    Text(
                        text = "MENU UTAMA",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp,
                            letterSpacing = 1.sp
                        ),
                        color = Color(0xFF94A3B8),
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )

                    // BERANDA | JUAL | TRANSAKSI | INVENTORY | PROFIL
                    listOf(
                        AppScreen.BERANDA,
                        AppScreen.PENJUALAN,
                        AppScreen.TRANSAKSI,
                        AppScreen.INVENTORY,
                        AppScreen.PROFIL
                    ).forEach { screen ->
                        DrawerItem(
                            screen = screen,
                            isSelected = currentScreen == screen,
                            onClick = {
                                currentScreen = screen
                                scope.launch { drawerState.close() }
                            }
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "RIWAYAT & KEUANGAN",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp,
                            letterSpacing = 1.sp
                        ),
                        color = Color(0xFF94A3B8),
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )

                    listOf(
                        AppScreen.RIWAYAT,
                        AppScreen.PENGOLAHAN,
                        AppScreen.LABA_RUGI,
                        AppScreen.LAPORAN
                    ).forEach { screen ->
                        DrawerItem(
                            screen = screen,
                            isSelected = currentScreen == screen,
                            onClick = {
                                currentScreen = screen
                                scope.launch { drawerState.close() }
                            }
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "MASTER & LOG",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp,
                            letterSpacing = 1.sp
                        ),
                        color = Color(0xFF94A3B8),
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )

                    listOf(
                        AppScreen.MASTER_KADAR,
                        AppScreen.ACTIVITY_LOG
                    ).forEach { screen ->
                        DrawerItem(
                            screen = screen,
                            isSelected = currentScreen == screen,
                            onClick = {
                                currentScreen = screen
                                scope.launch { drawerState.close() }
                            }
                        )
                    }
                }
            }
        }
    ) {
        Scaffold(
            topBar = {
                AppTopBar(
                    currentScreen = currentScreen,
                    currentUser = currentUser,
                    onOpenDrawer = { scope.launch { drawerState.open() } },
                    onNavigate = { currentScreen = it },
                    onRoleSwitch = { role ->
                        authViewModel.switchRole(role)
                        mainViewModel.showMessage("Beralih ke role ${role.displayName}")
                    },
                    onLogout = {
                        authViewModel.logout()
                        mainViewModel.showMessage("Anda telah keluar dari akun.")
                    }
                )
            },
            bottomBar = {
                AppBottomBar(
                    currentScreen = currentScreen,
                    onNavigate = { currentScreen = it }
                )
            },
            snackbarHost = {
                SnackbarHost(
                    hostState = snackbarHostState,
                    modifier = Modifier.testTag("app_snackbar_host")
                )
            },
            containerColor = Color(0xFFF8FAFC),
            modifier = Modifier.fillMaxSize()
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                when (currentScreen) {
                    AppScreen.BERANDA -> DashboardScreen(
                        viewModel = mainViewModel,
                        currentUser = currentUser,
                        onNavigate = { currentScreen = it }
                    )
                    AppScreen.PENJUALAN -> GoldSalesScreen(
                        viewModel = mainViewModel,
                        currentUser = currentUser
                    )
                    AppScreen.TRANSAKSI -> TransactionScreen(
                        viewModel = mainViewModel,
                        currentUser = currentUser
                    )
                    AppScreen.INVENTORY -> InventoryScreen(
                        viewModel = mainViewModel,
                        currentUser = currentUser,
                        onNavigate = { currentScreen = it }
                    )
                    AppScreen.PROFIL -> ProfileScreen(
                        authViewModel = authViewModel,
                        mainViewModel = mainViewModel,
                        currentUser = currentUser
                    )
                    AppScreen.RIWAYAT -> TransactionHistoryScreen(
                        viewModel = mainViewModel,
                        currentUser = currentUser
                    )
                    AppScreen.PENGOLAHAN -> ProcessingBatchScreen(
                        viewModel = mainViewModel,
                        currentUser = currentUser
                    )
                    AppScreen.LABA_RUGI -> ProfitLossScreen(
                        viewModel = mainViewModel,
                        currentUser = currentUser
                    )
                    AppScreen.LAPORAN -> ReportsScreen(
                        viewModel = mainViewModel,
                        currentUser = currentUser
                    )
                    AppScreen.MASTER_KADAR -> GoldRateMasterScreen(
                        viewModel = mainViewModel,
                        currentUser = currentUser
                    )
                    AppScreen.ACTIVITY_LOG -> ActivityLogScreen(
                        viewModel = mainViewModel,
                        currentUser = currentUser
                    )
                }
            }
        }
    }

    // Modal Confirmation Dialog before saving critical transactions
    if (confirmationData != null) {
        TransactionConfirmDialog(
            data = confirmationData!!,
            onDismiss = { mainViewModel.dismissConfirmation() }
        )
    }
}

@Composable
fun DrawerItem(
    screen: AppScreen,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    NavigationDrawerItem(
        icon = {
            Icon(
                imageVector = if (isSelected) screen.selectedIcon else screen.unselectedIcon,
                contentDescription = screen.title,
                modifier = Modifier.size(20.dp)
            )
        },
        label = {
            Text(
                screen.title,
                fontSize = 13.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
            )
        },
        selected = isSelected,
        onClick = onClick,
        colors = NavigationDrawerItemDefaults.colors(
            selectedContainerColor = Color(0xFFFEF3C7),
            selectedIconColor = Color(0xFF78350F),
            selectedTextColor = Color(0xFF78350F),
            unselectedContainerColor = Color.Transparent,
            unselectedIconColor = Color(0xFF64748B),
            unselectedTextColor = Color(0xFF334155)
        ),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .padding(vertical = 2.dp)
            .testTag("drawer_item_${screen.name.lowercase()}")
    )
}
