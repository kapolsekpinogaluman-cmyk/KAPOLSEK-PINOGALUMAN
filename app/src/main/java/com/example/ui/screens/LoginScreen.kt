package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.DangerRed
import com.example.ui.theme.GoldPrimary
import com.example.viewmodel.AuthViewModel

enum class LoginIdentifierType(val label: String, val example: String, val keyboardType: KeyboardType) {
    USERNAME("Username", "Contoh: ferrynani", KeyboardType.Text),
    EMAIL("Email", "Contoh: ferry.nani@gmail.com", KeyboardType.Email),
    PHONE("No. HP / WA", "Contoh: 08124180909", KeyboardType.Phone)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    authViewModel: AuthViewModel,
    onLoginSuccess: () -> Unit,
    modifier: Modifier = Modifier
) {
    val authState by authViewModel.uiState.collectAsState()
    val focusManager = LocalFocusManager.current

    var selectedType by remember { mutableStateOf(LoginIdentifierType.USERNAME) }
    var identifierInput by remember { mutableStateOf("ferrynani") }
    var passwordInput by remember { mutableStateOf("833273") }
    var isPasswordVisible by remember { mutableStateOf(false) }

    val scrollState = rememberScrollState()

    fun performLogin() {
        focusManager.clearFocus()
        authViewModel.login(identifierInput, passwordInput) { success ->
            if (success) {
                onLoginSuccess()
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF0F172A),
                        Color(0xFF1E1B4B),
                        Color(0xFF0F172A)
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .imePadding()
                .verticalScroll(scrollState)
                .padding(horizontal = 24.dp, vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Header Logo & Branding
            Box(
                modifier = Modifier
                    .size(76.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            colors = listOf(Color(0xFFFEF3C7), Color(0xFFFDE68A), Color(0xFFD97706))
                        )
                    )
                    .border(2.dp, Color(0xFFFBBF24), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Diamond,
                    contentDescription = "Logo",
                    tint = Color(0xFF78350F),
                    modifier = Modifier.size(42.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Catatan Emas & Modal",
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = (-0.5).sp,
                    color = Color.White
                )
            )

            Text(
                text = "Sistem Pembukuan & Manajemen Tromol",
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF94A3B8)
            )

            Spacer(modifier = Modifier.height(28.dp))

            // Main Login Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 480.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp)
                ) {
                    Text(
                        text = "Masuk ke Akun",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        ),
                        color = Color(0xFF0F172A)
                    )
                    Text(
                        text = "Gunakan salah satu dari 3 pilihan login di bawah:",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF64748B)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // 3 Identifier Option Tabs (Username / Email / No. HP)
                    Text(
                        text = "PILIHAN MASUK:",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp,
                            letterSpacing = 1.sp
                        ),
                        color = Color(0xFF475569)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFF1F5F9))
                            .padding(4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        LoginIdentifierType.values().forEach { type ->
                            val isSelected = selectedType == type
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (isSelected) Color.White else Color.Transparent)
                                    .clickable {
                                        selectedType = type
                                        authViewModel.clearLoginError()
                                        // Set recommended default if switching
                                        when (type) {
                                            LoginIdentifierType.USERNAME -> if (identifierInput.contains("@") || identifierInput.startsWith("08")) identifierInput = "ferrynani"
                                            LoginIdentifierType.EMAIL -> if (!identifierInput.contains("@")) identifierInput = "ferry.nani@gmail.com"
                                            LoginIdentifierType.PHONE -> if (identifierInput != "08124180909") identifierInput = "08124180909"
                                        }
                                    }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = when (type) {
                                            LoginIdentifierType.USERNAME -> Icons.Default.Person
                                            LoginIdentifierType.EMAIL -> Icons.Default.Email
                                            LoginIdentifierType.PHONE -> Icons.Default.Phone
                                        },
                                        contentDescription = null,
                                        tint = if (isSelected) Color(0xFF78350F) else Color(0xFF64748B),
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = type.label,
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                            fontSize = 11.sp
                                        ),
                                        color = if (isSelected) Color(0xFF78350F) else Color(0xFF64748B),
                                        maxLines = 1
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Identifier Input Field
                    OutlinedTextField(
                        value = identifierInput,
                        onValueChange = {
                            identifierInput = it
                            authViewModel.clearLoginError()
                        },
                        label = { Text(selectedType.label) },
                        placeholder = { Text(selectedType.example) },
                        leadingIcon = {
                            Icon(
                                imageVector = when (selectedType) {
                                    LoginIdentifierType.USERNAME -> Icons.Default.Person
                                    LoginIdentifierType.EMAIL -> Icons.Default.Email
                                    LoginIdentifierType.PHONE -> Icons.Default.Phone
                                },
                                contentDescription = null,
                                tint = GoldPrimary
                            )
                        },
                        trailingIcon = {
                            if (identifierInput.isNotEmpty()) {
                                IconButton(onClick = { identifierInput = "" }) {
                                    Icon(Icons.Default.Clear, contentDescription = "Hapus", tint = Color(0xFF94A3B8))
                                }
                            }
                        },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = selectedType.keyboardType,
                            imeAction = ImeAction.Next
                        ),
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = GoldPrimary,
                            unfocusedBorderColor = Color(0xFFCBD5E1),
                            focusedLabelColor = Color(0xFF78350F)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_login_identifier")
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Password Input Field with Eye Toggle (Tanda Mata)
                    OutlinedTextField(
                        value = passwordInput,
                        onValueChange = {
                            passwordInput = it
                            authViewModel.clearLoginError()
                        },
                        label = { Text("Kata Sandi / Password") },
                        placeholder = { Text("Masukkan kata sandi (833273)") },
                        leadingIcon = {
                            Icon(Icons.Default.Lock, contentDescription = null, tint = GoldPrimary)
                        },
                        trailingIcon = {
                            IconButton(
                                onClick = { isPasswordVisible = !isPasswordVisible },
                                modifier = Modifier.testTag("btn_toggle_password_visibility")
                            ) {
                                Icon(
                                    imageVector = if (isPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = if (isPasswordVisible) "Sembunyikan Kata Sandi" else "Lihat Kata Sandi",
                                    tint = if (isPasswordVisible) GoldPrimary else Color(0xFF94A3B8)
                                )
                            }
                        },
                        visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = { performLogin() }
                        ),
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = GoldPrimary,
                            unfocusedBorderColor = Color(0xFFCBD5E1),
                            focusedLabelColor = Color(0xFF78350F)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_login_password")
                    )

                    // Error message banner
                    if (authState.loginError != null) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = DangerRed.copy(alpha = 0.1f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, DangerRed.copy(alpha = 0.3f))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.ErrorOutline,
                                    contentDescription = null,
                                    tint = DangerRed,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = authState.loginError ?: "",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = DangerRed
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Login Button
                    Button(
                        onClick = { performLogin() },
                        enabled = !authState.isLoading,
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = GoldPrimary,
                            contentColor = Color.White
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("btn_submit_login")
                    ) {
                        if (authState.isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(22.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text("Memverifikasi...")
                        } else {
                            Icon(Icons.Default.Login, contentDescription = null, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Masuk ke Sistem",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Quick Info & Credentials Card for User Convenience
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color.White.copy(alpha = 0.08f),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.15f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 480.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.Default.VerifiedUser,
                            contentDescription = null,
                            tint = Color(0xFFFDE68A),
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "Kredensial Akun Resmi Sistem",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "• Username: ferrynani\n• Email: ferry.nani@gmail.com\n• No. Handphone: 08124180909\n• Password: " + (if (isPasswordVisible) "833273" else "••••••"),
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp, lineHeight = 18.sp),
                        color = Color(0xFFCBD5E1)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // 1-Tap Autofill Button
                    OutlinedButton(
                        onClick = {
                            when (selectedType) {
                                LoginIdentifierType.USERNAME -> identifierInput = "ferrynani"
                                LoginIdentifierType.EMAIL -> identifierInput = "ferry.nani@gmail.com"
                                LoginIdentifierType.PHONE -> identifierInput = "08124180909"
                            }
                            passwordInput = "833273"
                            authViewModel.clearLoginError()
                        },
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFFDE68A)),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFDE68A).copy(alpha = 0.5f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.AutoFixHigh, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Isi Otomatis Akun Ferry Nani",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold)
                        )
                    }
                }
            }
        }
    }
}
