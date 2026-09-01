package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.database.entity.UserEntity
import com.example.data.model.UserRole
import com.example.ui.components.AttachmentUploader
import com.example.ui.components.RoleBadge
import com.example.ui.theme.DangerRed
import com.example.ui.theme.GoldLight
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.SlateNavyDark
import com.example.ui.theme.SuccessGreen
import com.example.viewmodel.AuthViewModel
import com.example.viewmodel.MainViewModel

enum class ProfileSubTab(val label: String, val icon: ImageVector) {
    DATA_DIRI("Data Profil", Icons.Default.Person),
    KEAMANAN("Pola Keamanan", Icons.Default.Security),
    MANAJEMEN_USER("Daftar User", Icons.Default.Group),
    INFO_TOKO("Profil Usaha", Icons.Default.Storefront)
}

val DefaultAvatars = listOf(
    "👑", "💎", "⭐", "💼", "🦅", "🛡️", "🔥", "✨"
)

@Composable
fun ProfileScreen(
    authViewModel: AuthViewModel,
    mainViewModel: MainViewModel,
    currentUser: UserEntity?,
    initialTab: ProfileSubTab = ProfileSubTab.DATA_DIRI,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableStateOf(initialTab) }
    val allUsers by authViewModel.allUsers.collectAsStateWithLifecycle(emptyList())

    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("screen_profile")
    ) {
        // Top Sub Tab Navigation
        ScrollableTabRow(
            selectedTabIndex = selectedTab.ordinal,
            edgePadding = 16.dp,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = GoldPrimary,
            divider = {},
            modifier = Modifier.fillMaxWidth()
        ) {
            ProfileSubTab.values().forEach { tab ->
                val isSelected = selectedTab == tab
                Tab(
                    selected = isSelected,
                    onClick = { selectedTab = tab },
                    text = {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = tab.icon,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = if (isSelected) GoldPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = tab.label,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            )
                        }
                    },
                    selectedContentColor = GoldPrimary,
                    unselectedContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.testTag("tab_profile_${tab.name.lowercase()}")
                )
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            when (selectedTab) {
                ProfileSubTab.DATA_DIRI -> PersonalProfileView(authViewModel, mainViewModel, currentUser)
                ProfileSubTab.KEAMANAN -> SecuritySettingsView(authViewModel, mainViewModel, currentUser)
                ProfileSubTab.MANAJEMEN_USER -> UserManagementView(authViewModel, mainViewModel, currentUser, allUsers)
                ProfileSubTab.INFO_TOKO -> BusinessInfoView(mainViewModel)
            }
        }
    }
}

@Composable
fun PersonalProfileView(
    authViewModel: AuthViewModel,
    mainViewModel: MainViewModel,
    currentUser: UserEntity?
) {
    var fullName by remember(currentUser) { mutableStateOf(currentUser?.fullName ?: "") }
    var email by remember(currentUser) { mutableStateOf(currentUser?.email ?: "") }
    var phoneWhatsapp by remember(currentUser) { mutableStateOf(currentUser?.phoneWhatsapp ?: "") }
    var photoUri by remember(currentUser) { mutableStateOf(currentUser?.photoUri) }
    var selectedAvatarEmoji by remember(currentUser) { mutableStateOf("👑") }

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(vertical = 16.dp)
    ) {
        // Profile Card Header
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = SlateNavyDark)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFE8DEF8))
                            .border(3.dp, Color(0xFFD0BCFF), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = selectedAvatarEmoji,
                            fontSize = 36.sp
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = currentUser?.fullName ?: "Nama Pengguna",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = Color.White
                        )
                        Text(
                            text = "@${currentUser?.username ?: "user"}",
                            style = MaterialTheme.typography.bodySmall,
                            color = GoldLight
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        RoleBadge(role = currentUser?.role?.name ?: "OWNER")
                    }
                }
            }
        }

        // Avatar Selection
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
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Pilih Ikon / Foto Profil",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                    )

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(DefaultAvatars) { emoji ->
                            val isSelected = selectedAvatarEmoji == emoji
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant)
                                    .border(if (isSelected) 2.dp else 1.dp, if (isSelected) GoldPrimary else Color.Transparent, CircleShape)
                                    .clickable { selectedAvatarEmoji = emoji },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = emoji, fontSize = 20.sp)
                            }
                        }
                    }

                    AttachmentUploader(
                        photoUri = photoUri,
                        onPhotoSelected = { photoUri = it },
                        label = "Unggah Foto dari Galeri (Opsional)"
                    )
                }
            }
        }

        // Edit Profile Form
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
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = "Edit Informasi Pribadi",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )

                    OutlinedTextField(
                        value = fullName,
                        onValueChange = { fullName = it },
                        label = { Text("Nama Lengkap") },
                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_profile_fullname")
                    )

                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Alamat Email") },
                        leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_profile_email")
                    )

                    OutlinedTextField(
                        value = phoneWhatsapp,
                        onValueChange = { phoneWhatsapp = it },
                        label = { Text("Nomor WhatsApp") },
                        leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                        placeholder = { Text("Contoh: 081234567890") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_profile_phone")
                    )

                    Button(
                        onClick = {
                            if (fullName.isBlank()) {
                                mainViewModel.showMessage("Nama lengkap tidak boleh kosong.")
                                return@Button
                            }
                            authViewModel.updateProfile(
                                fullName = fullName,
                                email = email,
                                phoneWhatsapp = phoneWhatsapp,
                                photoUri = photoUri,
                                actor = currentUser?.username ?: "OWNER"
                            )
                            mainViewModel.showMessage("Data profil dan WhatsApp berhasil diperbarui!")
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("btn_save_profile"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary)
                    ) {
                        Icon(Icons.Default.Save, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Simpan Perubahan Profil", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Logout / Switch Account Card
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
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Sesi & Akun",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = "Keluar dari sesi saat ini untuk masuk menggunakan akun lain (Username, Email, atau No. HP).",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    OutlinedButton(
                        onClick = { authViewModel.logout() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp)
                            .testTag("btn_logout_account"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = DangerRed),
                        border = androidx.compose.foundation.BorderStroke(1.dp, DangerRed.copy(alpha = 0.5f))
                    ) {
                        Icon(Icons.Default.Logout, contentDescription = null, tint = DangerRed)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Keluar dari Akun (Logout)", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun SecuritySettingsView(
    authViewModel: AuthViewModel,
    mainViewModel: MainViewModel,
    currentUser: UserEntity?
) {
    var selectedSecurityType by remember(currentUser) {
        mutableStateOf(currentUser?.securityType ?: "PIN")
    }
    var securityPin by remember(currentUser) {
        mutableStateOf(currentUser?.securityPinOrPattern ?: "1234")
    }
    var showTestingDialog by remember { mutableStateOf(false) }

    val securityOptions = listOf(
        Triple("SIDIK_JARI", "Sidik Jari (Fingerprint)", Icons.Default.Fingerprint),
        Triple("WAJAH", "Pengenalan Wajah (Face Unlock)", Icons.Default.Face),
        Triple("PIN", "PIN Keamanan (4-6 Digit)", Icons.Default.Pin),
        Triple("POLA", "Pola Kunci (Pattern Lock)", Icons.Default.Pattern)
    )

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(vertical = 16.dp)
    ) {
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
                        text = "Metode Keamanan Utama",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = "Pilih metode pengamanan saat membuka aplikasi atau otorisasi transaksi modal tinggi.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    securityOptions.forEach { (typeKey, title, icon) ->
                        val isSelected = selectedSecurityType == typeKey
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedSecurityType = typeKey }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Icon(
                                        imageVector = icon,
                                        contentDescription = null,
                                        tint = if (isSelected) GoldPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Column {
                                        Text(
                                            text = title,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                        )
                                        Text(
                                            text = if (typeKey == "SIDIK_JARI" || typeKey == "WAJAH") "Biometrik Otentikasi Android" else "Kunci Kode Sandi",
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                RadioButton(
                                    selected = isSelected,
                                    onClick = { selectedSecurityType = typeKey }
                                )
                            }
                        }
                    }

                    if (selectedSecurityType == "PIN" || selectedSecurityType == "POLA") {
                        OutlinedTextField(
                            value = securityPin,
                            onValueChange = { securityPin = it },
                            label = { Text(if (selectedSecurityType == "PIN") "Kode PIN Keamanan" else "Kode Pola (Angka 1-9)") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = { showTestingDialog = true },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.VerifiedUser, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Uji Coba")
                        }

                        Button(
                            onClick = {
                                authViewModel.updateSecurity(
                                    securityType = selectedSecurityType,
                                    pinOrPattern = securityPin,
                                    actor = currentUser?.username ?: "OWNER"
                                )
                                mainViewModel.showMessage("Pola keamanan ($selectedSecurityType) berhasil disimpan!")
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Terapkan", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }

    if (showTestingDialog) {
        AlertDialog(
            onDismissRequest = { showTestingDialog = false },
            icon = {
                val icon = when (selectedSecurityType) {
                    "SIDIK_JARI" -> Icons.Default.Fingerprint
                    "WAJAH" -> Icons.Default.Face
                    "POLA" -> Icons.Default.Pattern
                    else -> Icons.Default.Pin
                }
                Icon(icon, contentDescription = null, tint = GoldPrimary, modifier = Modifier.size(36.dp))
            },
            title = { Text("Simulasi Keamanan: $selectedSecurityType") },
            text = {
                Text(
                    text = when (selectedSecurityType) {
                        "SIDIK_JARI" -> "Sentuh sensor sidik jari untuk verifikasi identitas pengguna."
                        "WAJAH" -> "Posisikan wajah Anda di depan kamera depan."
                        "POLA" -> "Hubungkan titik pola pembuka kunci."
                        else -> "Masukkan 4-6 digit angka PIN Anda: $securityPin"
                    }
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showTestingDialog = false
                        mainViewModel.showMessage("Verifikasi $selectedSecurityType Berhasil!")
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen)
                ) {
                    Text("Verifikasi Sukses")
                }
            },
            dismissButton = {
                TextButton(onClick = { showTestingDialog = false }) { Text("Tutup") }
            }
        )
    }
}

@Composable
fun UserManagementView(
    authViewModel: AuthViewModel,
    mainViewModel: MainViewModel,
    currentUser: UserEntity?,
    allUsers: List<UserEntity>
) {
    var showAddUserDialog by remember { mutableStateOf(false) }
    var selectedUserForPasswordChange by remember { mutableStateOf<UserEntity?>(null) }

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(vertical = 16.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Daftar Pengguna Aplikasi",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = "Termasuk User 'Hanya Cukup Melihat' (Viewer)",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Button(
                    onClick = { showAddUserDialog = true },
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary),
                    modifier = Modifier.testTag("btn_add_user_profile")
                ) {
                    Icon(Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Tambah User", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        items(allUsers, key = { it.id }) { user ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = user.fullName,
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                                )
                                if (user.id == currentUser?.id) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("(Akun Aktif)", color = GoldPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                            Text(
                                text = "@${user.username} • WA: ${user.phoneWhatsapp}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        RoleBadge(role = user.role.name)
                    }

                    HorizontalDivider()

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = if (user.isActive) "Status: Aktif" else "Status: Nonaktif",
                                fontSize = 12.sp,
                                color = if (user.isActive) SuccessGreen else Color.Red,
                                fontWeight = FontWeight.SemiBold
                            )
                            if (user.id != currentUser?.id) {
                                Spacer(modifier = Modifier.width(4.dp))
                                Switch(
                                    checked = user.isActive,
                                    onCheckedChange = { authViewModel.toggleUserStatus(user, currentUser?.username ?: "OWNER") }
                                )
                            }
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            TextButton(
                                onClick = { selectedUserForPasswordChange = user },
                                colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.primary)
                            ) {
                                Icon(Icons.Default.LockReset, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Ganti Sandi", fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddUserDialog) {
        EnhancedAddUserDialog(
            onDismiss = { showAddUserDialog = false },
            onSave = { username, fullName, role, pass, email, wa ->
                authViewModel.addUser(
                    username = username,
                    fullName = fullName,
                    role = role,
                    pass = pass,
                    email = email,
                    phoneWhatsapp = wa,
                    actor = currentUser?.username ?: "OWNER"
                )
                mainViewModel.showMessage("User @$username berhasil ditambahkan.")
                showAddUserDialog = false
            }
        )
    }

    if (selectedUserForPasswordChange != null) {
        val target = selectedUserForPasswordChange!!
        ChangePasswordDialog(
            targetUser = target,
            onDismiss = { selectedUserForPasswordChange = null },
            onSave = { newPass ->
                authViewModel.changePassword(target, newPass, currentUser?.username ?: "OWNER")
                mainViewModel.showMessage("Password untuk @${target.username} berhasil direset/diubah.")
                selectedUserForPasswordChange = null
            }
        )
    }
}

@Composable
fun BusinessInfoView(mainViewModel: MainViewModel) {
    var businessName by remember { mutableStateOf("Toko & Pengolahan Emas Pinogaluman") }
    var businessPhone by remember { mutableStateOf("0812-3456-7890") }
    var businessAddress by remember { mutableStateOf("Sentra Tromol & Tambang Emas Pinogaluman") }

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(vertical = 16.dp)
    ) {
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
                        text = "Informasi Usaha & Toko",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )

                    OutlinedTextField(
                        value = businessName,
                        onValueChange = { businessName = it },
                        label = { Text("Nama Usaha / Toko") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = businessPhone,
                        onValueChange = { businessPhone = it },
                        label = { Text("Nomor Telepon / CS WhatsApp") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = businessAddress,
                        onValueChange = { businessAddress = it },
                        label = { Text("Alamat Usaha / Lokasi Tambang") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Button(
                        onClick = { mainViewModel.showMessage("Data usaha berhasil disimpan.") },
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Simpan Informasi Usaha", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EnhancedAddUserDialog(
    onDismiss: () -> Unit,
    onSave: (username: String, fullName: String, role: UserRole, pass: String, email: String, wa: String) -> Unit
) {
    var username by remember { mutableStateOf("") }
    var fullName by remember { mutableStateOf("") }
    var role by remember { mutableStateOf(UserRole.VIEWER) }
    var password by remember { mutableStateOf("123") }
    var email by remember { mutableStateOf("") }
    var phoneWhatsapp by remember { mutableStateOf("") }
    var expandedRole by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Tambah Pengguna Baru") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = fullName,
                    onValueChange = { fullName = it },
                    label = { Text("Nama Lengkap Pengguna *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = username,
                    onValueChange = { username = it },
                    label = { Text("Username Login *") },
                    placeholder = { Text("contoh: viewer_tamu") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // Role Selector (Including VIEWER)
                ExposedDropdownMenuBox(
                    expanded = expandedRole,
                    onExpandedChange = { expandedRole = !expandedRole }
                ) {
                    OutlinedTextField(
                        value = "${role.name} - ${role.displayName}",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Role Akses Pengguna *") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedRole) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = expandedRole,
                        onDismissRequest = { expandedRole = false }
                    ) {
                        UserRole.values().forEach { r ->
                            DropdownMenuItem(
                                text = {
                                    Column {
                                        Text(r.name, fontWeight = FontWeight.Bold)
                                        Text(r.displayName, fontSize = 11.sp, color = Color.Gray)
                                    }
                                },
                                onClick = {
                                    role = r
                                    expandedRole = false
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = phoneWhatsapp,
                    onValueChange = { phoneWhatsapp = it },
                    label = { Text("Nomor WhatsApp (Opsional)") },
                    placeholder = { Text("081234567890") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password Awal *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (username.isNotBlank() && fullName.isNotBlank()) {
                        onSave(username, fullName, role, password, email, phoneWhatsapp)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary)
            ) {
                Text("Tambah Pengguna")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) { Text("Batal") }
        }
    )
}

@Composable
fun ChangePasswordDialog(
    targetUser: UserEntity,
    onDismiss: () -> Unit,
    onSave: (newPassword: String) -> Unit
) {
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Ganti / Reset Sandi @${targetUser.username}") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Pengguna: ${targetUser.fullName} (${targetUser.role.name})",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                OutlinedTextField(
                    value = newPassword,
                    onValueChange = {
                        newPassword = it
                        errorMessage = null
                    },
                    label = { Text("Password Baru") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = {
                        confirmPassword = it
                        errorMessage = null
                    },
                    label = { Text("Konfirmasi Password Baru") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                if (errorMessage != null) {
                    Text(
                        text = errorMessage!!,
                        color = Color.Red,
                        fontSize = 12.sp
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (newPassword.isBlank()) {
                        errorMessage = "Password tidak boleh kosong."
                        return@Button
                    }
                    if (newPassword != confirmPassword) {
                        errorMessage = "Konfirmasi password tidak cocok."
                        return@Button
                    }
                    onSave(newPassword)
                },
                colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary)
            ) {
                Text("Simpan Sandi")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) { Text("Batal") }
        }
    )
}
