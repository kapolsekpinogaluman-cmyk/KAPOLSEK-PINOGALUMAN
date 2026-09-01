package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ListAlt
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
import com.example.data.database.entity.ActivityLogEntity
import com.example.data.database.entity.UserEntity
import com.example.ui.components.EmptyStateView
import com.example.ui.components.RoleBadge
import com.example.ui.components.SearchAndFilterHeader
import com.example.ui.theme.GoldPrimary
import com.example.utils.Formatters
import com.example.viewmodel.MainViewModel

@Composable
fun ActivityLogScreen(
    viewModel: MainViewModel,
    currentUser: UserEntity?,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    val allLogs by viewModel.allLogs.collectAsStateWithLifecycle(emptyList())

    val filteredLogs = remember(allLogs, searchQuery) {
        if (searchQuery.isBlank()) allLogs
        else allLogs.filter {
            it.username.contains(searchQuery, ignoreCase = true) ||
                    it.actionType.contains(searchQuery, ignoreCase = true) ||
                    it.details.contains(searchQuery, ignoreCase = true) ||
                    it.userRole.contains(searchQuery, ignoreCase = true)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .testTag("screen_activity_log")
    ) {
        SearchAndFilterHeader(
            query = searchQuery,
            onQueryChange = { searchQuery = it },
            placeholder = "Cari tindakan, username, atau deskripsi log...",
            modifier = Modifier.padding(vertical = 10.dp)
        )

        if (filteredLogs.isEmpty()) {
            EmptyStateView(
                icon = Icons.Default.ListAlt,
                title = "Log Aktivitas Kosong",
                description = "Setiap aktivitas pengguna dan perubahan data sistem akan tercatat secara otomatis di sini."
            )
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                items(filteredLogs, key = { it.id }) { log ->
                    ActivityLogItemCard(log = log)
                }
            }
        }
    }
}

@Composable
fun ActivityLogItemCard(log: ActivityLogEntity) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RoleBadge(role = log.userRole)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = log.username,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }

                Text(
                    text = Formatters.formatDateTime(log.timestamp),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "${log.actionType} ${if (!log.transactionId.isNullOrBlank()) "[${log.transactionId}]" else ""}",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                color = GoldPrimary
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = log.details,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
