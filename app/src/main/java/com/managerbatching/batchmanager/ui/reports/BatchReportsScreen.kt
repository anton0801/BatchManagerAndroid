package com.managerbatching.batchmanager.ui.reports

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.managerbatching.batchmanager.ui.theme.*
import com.managerbatching.batchmanager.viewmodel.BatchViewModel
import androidx.compose.foundation.Canvas

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BatchReportsScreen(
    batchId: String,
    viewModel: BatchViewModel,
    onBack: () -> Unit
) {
    val batch by viewModel.selectedBatch.collectAsState()
    LaunchedEffect(batchId) { viewModel.selectBatch(batchId) }

    val b = batch ?: return
    val successRate = viewModel.getSuccessRate(b)

    val sweepAngle by animateFloatAsState(
        targetValue = successRate / 100f * 360f,
        animationSpec = tween(1200, easing = FastOutSlowInEasing),
        label = "sweep"
    )

    Scaffold(
        containerColor = BackgroundYellow,
        topBar = {
            TopAppBar(
                title = { Text("Batch Report", fontFamily = NunitoFont, fontWeight = FontWeight.ExtraBold, color = TextBrown) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "Back", tint = TextBrown) } },
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
            Text(b.name, fontFamily = NunitoFont, fontWeight = FontWeight.ExtraBold, fontSize = 22.sp, color = TextBrown)

            // Donut chart
            SectionCard(backgroundColor = CreamPanel) {
                Text("🐣 Hatch Success Rate", fontFamily = NunitoFont, fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, color = TextBrown)
                Spacer(Modifier.height(16.dp))
                Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Canvas(Modifier.size(180.dp)) {
                        val stroke = 28f
                        drawArc(
                            color = CardSandy,
                            startAngle = -90f,
                            sweepAngle = 360f,
                            useCenter = false,
                            style = Stroke(stroke, cap = StrokeCap.Round),
                            topLeft = Offset(stroke / 2, stroke / 2),
                            size = Size(size.width - stroke, size.height - stroke)
                        )
                        if (sweepAngle > 0f) {
                            drawArc(
                                color = StatusGold,
                                startAngle = -90f,
                                sweepAngle = sweepAngle,
                                useCenter = false,
                                style = Stroke(stroke, cap = StrokeCap.Round),
                                topLeft = Offset(stroke / 2, stroke / 2),
                                size = Size(size.width - stroke, size.height - stroke)
                            )
                        }
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            "${successRate.toInt()}%",
                            fontFamily = NunitoFont,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 36.sp,
                            color = StatusGold
                        )
                        Text("success", fontFamily = NunitoFont, fontSize = 13.sp, color = TextBrownSoft)
                    }
                }
            }

            // Stats cards
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                ReportStatCard("🥚", b.totalEggs.toString(), "Total Eggs", PrimaryYellow, Modifier.weight(1f))
                ReportStatCard("🐣", b.successCount.toString(), "Hatched", SuccessGreen, Modifier.weight(1f))
                ReportStatCard("❌", b.discardCount.toString(), "Discarded", CriticalRed, Modifier.weight(1f))
            }

            // Event summary
            SectionCard {
                Text("📋 Event Summary", fontFamily = NunitoFont, fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, color = TextBrown)
                Spacer(Modifier.height(8.dp))
                val counts = b.events.groupBy { it.type }
                counts.entries.sortedByDescending { it.value.size }.take(5).forEach { (type, events) ->
                    Row(Modifier.padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text(type.emoji, fontSize = 18.sp)
                        Spacer(Modifier.width(8.dp))
                        Text(type.label, fontFamily = NunitoFont, fontSize = 14.sp, color = TextBrown, modifier = Modifier.weight(1f))
                        Text("×${events.size}", fontFamily = NunitoFont, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = AccentOrange)
                    }
                }
                if (b.events.isEmpty()) {
                    Text("No events recorded yet.", fontFamily = NunitoFont, color = TextBrownSoft, fontSize = 13.sp)
                }
            }

            // Export button
            SpringButton(
                text = "📤  Export Report",
                onClick = { /* TODO: Export as text/share */ },
                color = AccentOrange,
                textColor = Color.White,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
private fun ReportStatCard(icon: String, value: String, label: String, color: Color, modifier: Modifier = Modifier) {
    Column(
        modifier
            .shadow(4.dp, RoundedCornerShape(18.dp))
            .clip(RoundedCornerShape(18.dp))
            .background(CardSandy)
            .padding(14.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(icon, fontSize = 24.sp)
        Text(value, fontFamily = NunitoFont, fontWeight = FontWeight.ExtraBold, fontSize = 24.sp, color = color)
        Text(label, fontFamily = NunitoFont, fontSize = 11.sp, color = TextBrownSoft)
    }
}