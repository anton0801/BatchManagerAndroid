package com.managerbatching.batchmanager.ui.history

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.managerbatching.batchmanager.model.Batch
import com.managerbatching.batchmanager.model.BatchStatus
import com.managerbatching.batchmanager.ui.dashboard.toEmoji
import com.managerbatching.batchmanager.ui.theme.*
import com.managerbatching.batchmanager.viewmodel.BatchViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BatchHistoryScreen(
    viewModel: BatchViewModel,
    onCreateBatch: () -> Unit,
    onBatchClick: (String) -> Unit
) {
    val completed by viewModel.completedBatches.collectAsState()
    var showClearDialog by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = BackgroundYellow,
        topBar = {
            TopAppBar(
                title = { Text("Batch History", fontFamily = NunitoFont, fontWeight = FontWeight.ExtraBold, color = TextBrown) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BackgroundYellow)
            )
        }
    ) { padding ->
        LazyColumn(
            Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(bottom = 120.dp)
        ) {
            if (completed.isEmpty()) {
                item {
                    Column(
                        Modifier.fillMaxWidth().padding(48.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("📜", fontSize = 64.sp)
                        Spacer(Modifier.height(12.dp))
                        Text("No completed batches yet", fontFamily = NunitoFont, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, color = TextBrown)
                        Text("Completed batches will appear here.", fontFamily = NunitoFont, fontSize = 14.sp, color = TextBrownSoft)
                    }
                }
            }

            itemsIndexed(completed) { index, batch ->
                var visible by remember { mutableStateOf(false) }
                LaunchedEffect(Unit) { kotlinx.coroutines.delay(index * 80L); visible = true }
                AnimatedVisibility(visible, enter = slideInHorizontally(initialOffsetX = { -60 }) + fadeIn()) {
                    HistoryCard(batch, onClick = { onBatchClick(batch.id) }, modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp))
                }
            }

            item {
                Spacer(Modifier.height(20.dp))
                Column(Modifier.padding(horizontal = 20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    SpringButton(
                        text = "🗑️  Clear History",
                        onClick = { showClearDialog = true },
                        color = CriticalRed.copy(0.15f),
                        textColor = CriticalRed,
                        modifier = Modifier.fillMaxWidth()
                    )
                    SpringButton(
                        text = "➕  New Batch",
                        onClick = onCreateBatch,
                        color = AccentOrange,
                        textColor = Color.White,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }

    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            containerColor = CreamPanel,
            title = { Text("Clear History?", fontFamily = NunitoFont, fontWeight = FontWeight.ExtraBold, color = TextBrown) },
            text = { Text("This will delete all completed batches permanently.", fontFamily = NunitoFont, color = TextBrownSoft) },
            confirmButton = {
                TextButton(onClick = { viewModel.clearHistory(); showClearDialog = false }) {
                    Text("Clear", color = CriticalRed, fontFamily = NunitoFont, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearDialog = false }) {
                    Text("Cancel", color = TextBrownSoft, fontFamily = NunitoFont)
                }
            }
        )
    }
}

@Composable
private fun HistoryCard(batch: Batch, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val successRate = if (batch.totalEggs > 0) (batch.successCount.toFloat() / batch.totalEggs * 100).toInt() else 0
    val indicatorColor = when {
        successRate >= 80 -> StatusGold
        successRate >= 50 -> AccentOrange
        else -> CriticalRed
    }

    Row(
        modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(20.dp))
            .clip(RoundedCornerShape(20.dp))
            .background(CardSandy)
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            Modifier.size(6.dp).clip(RoundedCornerShape(3.dp)).background(indicatorColor)
        )
        Spacer(Modifier.width(10.dp))
        Text(batch.eggType.toEmoji(), fontSize = 28.sp)
        Spacer(Modifier.width(10.dp))
        Column(Modifier.weight(1f)) {
            Text(batch.name, fontFamily = NunitoFont, fontWeight = FontWeight.ExtraBold, fontSize = 15.sp, color = TextBrown)
            Text(
                SimpleDateFormat("MMM d, yyyy", Locale.getDefault()).format(Date(batch.startDate)),
                fontFamily = NunitoFont, fontSize = 12.sp, color = TextBrownSoft
            )
        }
        Column(horizontalAlignment = Alignment.End) {
            Text("$successRate%", fontFamily = NunitoFont, fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, color = indicatorColor)
            Text("success", fontFamily = NunitoFont, fontSize = 11.sp, color = TextBrownSoft)
        }
    }
}