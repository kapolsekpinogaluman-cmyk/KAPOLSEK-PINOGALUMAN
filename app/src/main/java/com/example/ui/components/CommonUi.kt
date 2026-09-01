package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.database.entity.UserEntity
import com.example.data.model.InventoryStatus
import com.example.data.model.TimeFilter
import com.example.data.model.UserRole
import com.example.ui.theme.*

@Composable
fun StatusBadge(
    status: String,
    modifier: Modifier = Modifier
) {
    val (bg, textColor, borderColor) = when (status.uppercase()) {
        "TERSEDIA", "SELESAI", "AKTIF" -> Triple(Color(0xFFD1FAE5), Color(0xFF065F46), Color(0xFFA7F3D0))
        "SEBAGIAN_TERJUAL", "PROSES" -> Triple(Color(0xFFFEF3C7), Color(0xFF92400E), Color(0xFFFDE68A))
        "TERJUAL" -> Triple(Color(0xFFF1F5F9), Color(0xFF475569), Color(0xFFE2E8F0))
        "DIBATALKAN", "RUSAK", "HILANG", "NONAKTIF" -> Triple(Color(0xFFFEE2E2), Color(0xFF991B1B), Color(0xFFFECACA))
        else -> Triple(Color(0xFFEDE9FE), Color(0xFF5B21B6), Color(0xFFDDD6FE))
    }

    val displayStatus = status.replace("_", " ")

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = bg,
        border = androidx.compose.foundation.BorderStroke(1.dp, borderColor),
        modifier = modifier
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(5.dp)
                    .clip(CircleShape)
                    .background(textColor)
            )
            Text(
                text = displayStatus,
                color = textColor,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp
                )
            )
        }
    }
}

@Composable
fun RoleBadge(role: String) {
    val (bg, textColor, borderColor) = when (role.uppercase()) {
        "OWNER" -> Triple(Color(0xFFFEF3C7), Color(0xFF92400E), Color(0xFFFCD34D))
        "ADMIN" -> Triple(Color(0xFFDBEAFE), Color(0xFF1E40AF), Color(0xFF93C5FD))
        "OPERATOR" -> Triple(Color(0xFFD1FAE5), Color(0xFF065F46), Color(0xFFA7F3D0))
        else -> Triple(Color(0xFFF1F5F9), Color(0xFF475569), Color(0xFFCBD5E1))
    }

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = bg,
        border = androidx.compose.foundation.BorderStroke(1.dp, borderColor)
    ) {
        Text(
            text = role,
            color = textColor,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 10.sp,
                letterSpacing = 0.5.sp
            ),
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
        )
    }
}

@Composable
fun TimeFilterRow(
    selected: TimeFilter,
    onSelect: (TimeFilter) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        TimeFilter.values().forEach { filter ->
            val isSelected = selected == filter
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = if (isSelected) GoldPrimary else Color.White,
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (isSelected) GoldPrimary else Color(0xFFE2E8F0)
                ),
                shadowElevation = if (isSelected) 2.dp else 0.dp,
                modifier = Modifier
                    .clickable { onSelect(filter) }
                    .testTag("filter_chip_${filter.name.lowercase()}")
            ) {
                Text(
                    text = filter.label,
                    color = if (isSelected) Color.White else Color(0xFF64748B),
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        fontSize = 12.sp
                    ),
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp)
                )
            }
        }
    }
}

@Composable
fun EmptyStateView(
    icon: ImageVector = Icons.Default.Inbox,
    title: String = "Belum Ada Data",
    description: String = "Data transaksi atau data master akan muncul di sini.",
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(68.dp)
                .clip(CircleShape)
                .background(Color(0xFFFFFBEB))
                .border(1.dp, Color(0xFFFEF3C7), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = GoldPrimary,
                modifier = Modifier.size(32.dp)
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            ),
            color = Color(0xFF0F172A)
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = description,
            style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
            color = Color(0xFF64748B),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    }
}

@Composable
fun SearchAndFilterHeader(
    query: String,
    onQueryChange: (String) -> Unit,
    placeholder: String = "Cari nomor transaksi, tromol, kadar...",
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        placeholder = { Text(placeholder, fontSize = 13.sp, color = Color(0xFF94A3B8)) },
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Search",
                tint = GoldPrimary,
                modifier = Modifier.size(20.dp)
            )
        },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Clear", tint = Color(0xFF64748B))
                }
            }
        },
        singleLine = true,
        shape = RoundedCornerShape(16.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = Color.White,
            unfocusedContainerColor = Color.White,
            focusedBorderColor = GoldPrimary,
            unfocusedBorderColor = Color(0xFFE2E8F0)
        ),
        modifier = modifier
            .fillMaxWidth()
            .shadow(1.dp, RoundedCornerShape(16.dp), spotColor = Color(0x100F172A))
            .testTag("input_search")
    )
}

@Composable
fun AttachmentUploader(
    photoUri: String?,
    onPhotoSelected: (String?) -> Unit,
    label: String = "Lampiran Foto / Bukti Timbangan",
    modifier: Modifier = Modifier
) {
    var showDialog by remember { mutableStateOf(false) }

    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
            color = Color(0xFF0F172A)
        )
        Spacer(modifier = Modifier.height(6.dp))

        if (photoUri == null) {
            Surface(
                onClick = { showDialog = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp)
                    .testTag("btn_upload_attachment"),
                shape = RoundedCornerShape(16.dp),
                color = Color(0xFFFFFBEB),
                border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFFFDE68A))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color.White),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AddPhotoAlternate,
                            contentDescription = null,
                            tint = GoldPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Upload Bukti Foto / Dokumen Timbangan",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                        color = Color(0xFF78350F)
                    )
                }
            }
        } else {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = Color(0xFFF8FAFC),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFFEF3C7)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Image,
                                contentDescription = null,
                                tint = GoldPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Terlampir: $photoUri",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFF0F172A)
                            ),
                            maxLines = 1
                        )
                    }
                    IconButton(onClick = { onPhotoSelected(null) }) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Hapus foto",
                            tint = DangerRed
                        )
                    }
                }
            }
        }
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = {
                Text(
                    "Pilih Sumber Lampiran",
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0F172A)
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Surface(
                        onClick = {
                            onPhotoSelected("Kamera_Timbangan_${System.currentTimeMillis()}.jpg")
                            showDialog = false
                        },
                        shape = RoundedCornerShape(14.dp),
                        color = Color(0xFFF8FAFC),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(14.dp)
                        ) {
                            Icon(Icons.Default.CameraAlt, contentDescription = null, tint = GoldPrimary)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text("Ambil Foto Kamera (Timbangan/Emas)", fontSize = 13.sp, fontWeight = FontWeight.Medium)
                        }
                    }
                    Surface(
                        onClick = {
                            onPhotoSelected("Galeri_Bukti_${System.currentTimeMillis()}.jpg")
                            showDialog = false
                        },
                        shape = RoundedCornerShape(14.dp),
                        color = Color(0xFFF8FAFC),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(14.dp)
                        ) {
                            Icon(Icons.Default.PhotoLibrary, contentDescription = null, tint = GoldPrimary)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text("Pilih dari Galeri / Dokumen", fontSize = 13.sp, fontWeight = FontWeight.Medium)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("Tutup", color = GoldPrimary, fontWeight = FontWeight.Bold)
                }
            }
        )
    }
}

@Composable
fun ActiveRoleFormHeader(
    currentUser: UserEntity?,
    actionDescription: String = "Mengisi Formulir Transaksi",
    modifier: Modifier = Modifier
) {
    val role = currentUser?.role ?: UserRole.OWNER
    val (bgRole, textRole, borderRole) = when (role) {
        UserRole.OWNER -> Triple(Color(0xFFFEF3C7), Color(0xFF78350F), Color(0xFFFCD34D))
        UserRole.ADMIN -> Triple(Color(0xFFDBEAFE), Color(0xFF1E40AF), Color(0xFF93C5FD))
        UserRole.OPERATOR -> Triple(Color(0xFFD1FAE5), Color(0xFF065F46), Color(0xFFA7F3D0))
        UserRole.VIEWER -> Triple(Color(0xFFF1F5F9), Color(0xFF475569), Color(0xFFCBD5E1))
    }

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
        shadowElevation = 2.dp,
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .testTag("active_role_form_header")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(bgRole)
                        .border(1.dp, borderRole, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = when (role) {
                            UserRole.OWNER -> Icons.Default.AdminPanelSettings
                            UserRole.ADMIN -> Icons.Default.SupervisorAccount
                            UserRole.OPERATOR -> Icons.Default.Engineering
                            UserRole.VIEWER -> Icons.Default.Visibility
                        },
                        contentDescription = null,
                        tint = textRole,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = currentUser?.fullName ?: currentUser?.username ?: "Pemilik",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            ),
                            color = Color(0xFF0F172A),
                            maxLines = 1
                        )
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF10B981))
                        )
                    }
                    Text(
                        text = "$actionDescription • Aktif",
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                        color = Color(0xFF64748B),
                        maxLines = 1
                    )
                }
            }

            Surface(
                shape = RoundedCornerShape(10.dp),
                color = bgRole,
                border = androidx.compose.foundation.BorderStroke(1.dp, borderRole)
            ) {
                Text(
                    text = "ROLE: ${role.displayName.uppercase()}",
                    color = textRole,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp,
                        letterSpacing = 0.5.sp
                    ),
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
    }
}


