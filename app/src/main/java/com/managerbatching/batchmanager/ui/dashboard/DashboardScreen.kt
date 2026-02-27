package com.managerbatching.batchmanager.ui.dashboard

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.managerbatching.batchmanager.model.*
import com.managerbatching.batchmanager.ui.theme.*
import com.managerbatching.batchmanager.viewmodel.BatchViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: BatchViewModel,
    onCreateBatch: () -> Unit,
    onBatchClick: (String) -> Unit
) {
    val batches by viewModel.batches.collectAsState()
    val active by viewModel.activeBatches.collectAsState()
    val pending by viewModel.pendingBatches.collectAsState()
    val completed by viewModel.completedBatches.collectAsState()

    Scaffold(
        containerColor = BackgroundYellow,
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onCreateBatch,
                containerColor = AccentOrange,
                contentColor = CreamPanel,
                icon = { Icon(Icons.Default.Add, "Create") },
                text = {
                    Text(
                        "New Batch",
                        fontFamily = NunitoFont,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            )
        }
    ) { padding ->
        LazyColumn(
            Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(bottom = 100.dp)
        ) {
            item {
                // Header
                Box(
                    Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.verticalGradient(listOf(CreamPanel, BackgroundYellow))
                        )
                        .padding(24.dp)
                ) {
                    Column {
                        Text("🥚 Batch Manager", fontFamily = NunitoFont, fontWeight = FontWeight.ExtraBold, fontSize = 26.sp, color = TextBrown)
                        Text(
                            SimpleDateFormat("EEEE, MMMM d", Locale.getDefault()).format(Date()),
                            fontFamily = NunitoFont, fontSize = 14.sp, color = TextBrownSoft
                        )
                    }
                }
            }

            item {
                // Stats summary card
                SummaryCard(
                    active = active.size,
                    pending = pending.size,
                    completed = completed.size,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
                )
            }

            item {
                Spacer(Modifier.height(8.dp))
                Text(
                    "  Your Batches",
                    fontFamily = NunitoFont,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 18.sp,
                    color = TextBrown,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)
                )
            }

            if (batches.isEmpty()) {
                item {
                    EmptyState(onCreateBatch)
                }
            }

            itemsIndexed(batches) { index, batch ->
                var visible by remember { mutableStateOf(false) }
                LaunchedEffect(Unit) {
                    kotlinx.coroutines.delay(index * 80L)
                    visible = true
                }
                AnimatedVisibility(
                    visible = visible,
                    enter = slideInVertically(initialOffsetY = { 60 }) + fadeIn()
                ) {
                    BatchCard(
                        batch = batch,
                        onClick = { onBatchClick(batch.id) },
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun SummaryCard(active: Int, pending: Int, completed: Int, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .shadow(6.dp, RoundedCornerShape(24.dp))
            .clip(RoundedCornerShape(24.dp))
            .background(CreamPanel)
            .padding(20.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        StatChip("🟡", "Active", active, StatusGold)
        StatChip("🟠", "Pending", pending, AccentOrange)
        StatChip("✅", "Done", completed, SuccessGreen)
    }
}

@Composable
private fun StatChip(icon: String, label: String, count: Int, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            Modifier
                .size(52.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(color.copy(0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Text(icon, fontSize = 24.sp)
        }
        Spacer(Modifier.height(6.dp))
        Text(
            count.toString(),
            fontFamily = NunitoFont,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 22.sp,
            color = color
        )
        Text(label, fontFamily = NunitoFont, fontSize = 12.sp, color = TextBrownSoft)
    }
}

@Composable
fun BatchCard(batch: Batch, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(if (isPressed) 0.97f else 1f, spring(Spring.DampingRatioMediumBouncy), label = "cardScale")

    Box(
        modifier = modifier
            .fillMaxWidth()
            .scale(scale)
            .shadow(4.dp, RoundedCornerShape(20.dp))
            .clip(RoundedCornerShape(20.dp))
            .background(CardSandy)
            .clickable(interactionSource = interactionSource, indication = null, onClick = onClick)
            .padding(18.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            // Egg type icon
            Box(
                Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(PrimaryYellow.copy(0.3f)),
                contentAlignment = Alignment.Center
            ) {
                Text(batch.eggType.toEmoji(), fontSize = 28.sp)
            }

            Spacer(Modifier.width(14.dp))

            Column(Modifier.weight(1f)) {
                Text(batch.name, fontFamily = NunitoFont, fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, color = TextBrown)
                Text(
                    "${batch.eggType.name.lowercase().replaceFirstChar { it.uppercase() }} • ${batch.totalEggs} eggs",
                    fontFamily = NunitoFont, fontSize = 13.sp, color = TextBrownSoft
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                StatusBadge(batch.status)
                Spacer(Modifier.height(4.dp))
                Text(
                    SimpleDateFormat("MMM d", Locale.getDefault()).format(Date(batch.startDate)),
                    fontFamily = NunitoFont, fontSize = 11.sp, color = TextBrownSoft
                )
            }
        }
    }
}

@Composable
private fun EmptyState(onCreateBatch: () -> Unit) {
    Column(
        Modifier
            .fillMaxWidth()
            .padding(48.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("🪹", fontSize = 72.sp)
        Spacer(Modifier.height(16.dp))
        Text("No batches yet!", fontFamily = NunitoFont, fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, color = TextBrown)
        Text("Create your first batch to get started.", fontFamily = NunitoFont, fontSize = 14.sp, color = TextBrownSoft, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
        Spacer(Modifier.height(24.dp))
        SpringButton("Create First Batch 🥚", onCreateBatch, color = AccentOrange, textColor = Color.White)
    }
}

fun EggType.toEmoji() = when (this) {
    EggType.CHICKEN -> "🐔"
    EggType.QUAIL -> "🐦"
    EggType.DUCK -> "🦆"
    EggType.GOOSE -> "🪿"
    EggType.TURKEY -> "🦃"
}