package com.managerbatching.batchmanager.ui.batch

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.managerbatching.batchmanager.model.*
import com.managerbatching.batchmanager.ui.dashboard.toEmoji
import com.managerbatching.batchmanager.ui.theme.*
import com.managerbatching.batchmanager.viewmodel.BatchViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BatchDetailsScreen(
    batchId: String,
    viewModel: BatchViewModel,
    onBack: () -> Unit,
    onAddEvent: (String) -> Unit,
    onIncubationMonitor: (String) -> Unit
) {
    val batch by viewModel.selectedBatch.collectAsState()

    LaunchedEffect(batchId) { viewModel.selectBatch(batchId) }

    if (batch == null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = AccentOrange)
        }
        return
    }

    val b = batch!!
    var showStatusDialog by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = BackgroundYellow,
        topBar = {
            TopAppBar(
                title = { Text(b.name, fontFamily = NunitoFont, fontWeight = FontWeight.ExtraBold, color = TextBrown) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "Back", tint = TextBrown)
                    }
                },
                actions = {
                    IconButton(onClick = { showStatusDialog = true }) {
                        Icon(Icons.Default.Edit, "Edit", tint = AccentOrange)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BackgroundYellow)
            )
        }
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(padding)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Hero card
            Box(
                Modifier
                    .fillMaxWidth()
                    .shadow(8.dp, RoundedCornerShape(28.dp))
                    .clip(RoundedCornerShape(28.dp))
                    .background(
                        Brush.linearGradient(listOf(CreamPanel, CardSandy))
                    )
                    .padding(20.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(b.eggType.toEmoji(), fontSize = 56.sp)
                    Spacer(Modifier.width(16.dp))
                    Column(Modifier.weight(1f)) {
                        Text(b.name, fontFamily = NunitoFont, fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, color = TextBrown)
                        Text(
                            "${b.eggType.name.lowercase().replaceFirstChar { it.uppercase() }} Eggs",
                            fontFamily = NunitoFont, fontSize = 14.sp, color = TextBrownSoft
                        )
                        Text("${b.totalEggs} eggs total", fontFamily = NunitoFont, fontSize = 13.sp, color = TextBrownSoft)
                        Spacer(Modifier.height(8.dp))
                        StatusBadge(b.status)
                    }
                }
                Spacer(Modifier.height(12.dp))
            }

            // Incubation params
            SectionCard(backgroundColor = CreamPanel) {
                Text("🌡️ Incubation Parameters", fontFamily = NunitoFont, fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, color = TextBrown)
                Spacer(Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ParamChip("🌡️", "${b.incubationParams.temperature}°C", "Temp", PrimaryYellow, Modifier.weight(1f))
                    ParamChip("💧", "${b.incubationParams.humidity}%", "Humidity", Color(0xFF4FC3F7), Modifier.weight(1f))
                    ParamChip("📅", "${viewModel.getDaysRemaining(b)}d", "Remaining", AccentOrange, Modifier.weight(1f))
                }
            }

            // Timeline
            if (b.events.isNotEmpty()) {
                SectionCard {
                    Text("📋 Event Timeline", fontFamily = NunitoFont, fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, color = TextBrown)
                    Spacer(Modifier.height(8.dp))
                    b.events.reversed().take(5).forEachIndexed { idx, event ->
                        AnimatedVisibility(visible = true, enter = slideInVertically(initialOffsetY = { 20 }) + fadeIn()) {
                            EventRow(event)
                        }
                        if (idx < minOf(4, b.events.size - 1)) {
                            Divider(color = PrimaryYellow.copy(0.3f), modifier = Modifier.padding(vertical = 4.dp))
                        }
                    }
                }
            }

            // Notes
            if (b.notes.isNotBlank()) {
                SectionCard(backgroundColor = CreamPanel) {
                    Text("📝 Notes", fontFamily = NunitoFont, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextBrownSoft)
                    Spacer(Modifier.height(4.dp))
                    Text(b.notes, fontFamily = NunitoFont, fontSize = 14.sp, color = TextBrown)
                }
            }

            // Action buttons
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                SpringButton(
                    text = "📊 Monitor",
                    onClick = { onIncubationMonitor(batchId) },
                    color = PrimaryYellow,
                    modifier = Modifier.weight(1f)
                )
                SpringButton(
                    text = "➕ Event",
                    onClick = { onAddEvent(batchId) },
                    color = AccentOrange,
                    textColor = Color.White,
                    modifier = Modifier.weight(1f)
                )
            }

            if (b.status == BatchStatus.PENDING) {
                SpringButton(
                    text = "🌡️  Start Incubation",
                    onClick = { viewModel.startIncubation(batchId) },
                    color = StatusGold,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(Modifier.height(32.dp))
        }
    }

    if (showStatusDialog) {
        StatusChangeDialog(
            current = b.status,
            onDismiss = { showStatusDialog = false },
            onSelect = { status ->
                viewModel.updateStatus(batchId, status)
                showStatusDialog = false
            }
        )
    }
}

@Composable
private fun ParamChip(icon: String, value: String, label: String, color: Color, modifier: Modifier = Modifier) {
    Column(
        modifier
            .clip(RoundedCornerShape(16.dp))
            .background(color.copy(0.15f))
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(icon, fontSize = 20.sp)
        Text(value, fontFamily = NunitoFont, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, color = color)
        Text(label, fontFamily = NunitoFont, fontSize = 11.sp, color = TextBrownSoft)
    }
}

@Composable
private fun EventRow(event: BatchEvent) {
    Row(Modifier.padding(vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(event.type.emoji, fontSize = 20.sp)
        Spacer(Modifier.width(10.dp))
        Column(Modifier.weight(1f)) {
            Text(event.type.label, fontFamily = NunitoFont, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = TextBrown)
            if (event.description.isNotBlank()) {
                Text(event.description, fontFamily = NunitoFont, fontSize = 12.sp, color = TextBrownSoft)
            }
        }
        Text(
            SimpleDateFormat("MM/dd HH:mm", Locale.getDefault()).format(Date(event.timestamp)),
            fontFamily = NunitoFont, fontSize = 11.sp, color = TextBrownSoft
        )
    }
}

@Composable
private fun StatusChangeDialog(
    current: BatchStatus,
    onDismiss: () -> Unit,
    onSelect: (BatchStatus) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = CreamPanel,
        title = { Text("Change Status", fontFamily = NunitoFont, fontWeight = FontWeight.ExtraBold, color = TextBrown) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                BatchStatus.values().forEach { status ->
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (status == current) CardSandy else Color.Transparent)
                            .clickable { onSelect(status) }
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        StatusBadge(status)
                        Spacer(Modifier.width(12.dp))
                        Text(status.name.lowercase().replaceFirstChar { it.uppercase() }, fontFamily = NunitoFont, fontWeight = FontWeight.SemiBold, color = TextBrown)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", fontFamily = NunitoFont, color = TextBrownSoft)
            }
        }
    )
}