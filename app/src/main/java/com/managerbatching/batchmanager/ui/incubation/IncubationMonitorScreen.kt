package com.managerbatching.batchmanager.ui.incubation

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
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.managerbatching.batchmanager.model.Batch
import com.managerbatching.batchmanager.ui.theme.*
import com.managerbatching.batchmanager.viewmodel.BatchViewModel
import androidx.compose.foundation.Canvas

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IncubationMonitorScreen(
    batchId: String,
    viewModel: BatchViewModel,
    onBack: () -> Unit
) {
    val batch by viewModel.selectedBatch.collectAsState()
    LaunchedEffect(batchId) { viewModel.selectBatch(batchId) }

    val b = batch ?: return

    // Pulsing animation for live indicator
    val pulse by rememberInfiniteTransition(label = "pulse").animateFloat(
        0.7f, 1f,
        infiniteRepeatable(tween(800, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "pulse"
    )

    val daysRemaining = viewModel.getDaysRemaining(b)

    // Simulated chart data
    val tempData = remember { generateSimulatedData(b.incubationParams.temperature, 14) }
    val humidData = remember { generateSimulatedData(b.incubationParams.humidity, 14) }

    Scaffold(
        containerColor = BackgroundYellow,
        topBar = {
            TopAppBar(
                title = { Text("Incubation Monitor", fontFamily = NunitoFont, fontWeight = FontWeight.ExtraBold, color = TextBrown) },
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
            // Live indicator
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Box(
                    Modifier.size(10.dp).clip(RoundedCornerShape(5.dp))
                        .background(SuccessGreen.copy(alpha = pulse))
                )
                Text("Live Monitoring", fontFamily = NunitoFont, fontWeight = FontWeight.Bold, color = SuccessGreen, fontSize = 13.sp)
            }

            // Main stats row
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                BigStatCard(
                    icon = "🌡️",
                    value = "${b.incubationParams.temperature}°C",
                    label = "Temperature",
                    color = AccentOrange,
                    modifier = Modifier.weight(1f)
                )
                BigStatCard(
                    icon = "💧",
                    value = "${b.incubationParams.humidity}%",
                    label = "Humidity",
                    color = Color(0xFF4FC3F7),
                    modifier = Modifier.weight(1f)
                )
            }

            // Days timer
            Box(
                Modifier
                    .fillMaxWidth()
                    .shadow(6.dp, RoundedCornerShape(24.dp))
                    .clip(RoundedCornerShape(24.dp))
                    .background(Brush.horizontalGradient(listOf(PrimaryYellow, StatusGold)))
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("⏳", fontSize = 36.sp)
                    Column {
                        Text(
                            "$daysRemaining",
                            fontFamily = NunitoFont,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 48.sp,
                            color = TextBrown
                        )
                        Text("days remaining", fontFamily = NunitoFont, fontSize = 14.sp, color = TextBrownSoft)
                    }
                }
            }

            // Status indicators
            SectionCard(backgroundColor = CreamPanel) {
                Text("Status Indicators", fontFamily = NunitoFont, fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, color = TextBrown)
                Spacer(Modifier.height(10.dp))
                StatusIndicatorRow("🌡️", "Temperature", "Normal", SuccessGreen)
                StatusIndicatorRow("💧", "Humidity", if (b.incubationParams.humidity > 70) "High" else "Normal",
                    if (b.incubationParams.humidity > 70) AccentOrange else SuccessGreen)
                StatusIndicatorRow("🔄", "Turning", "Required", PrimaryYellow)
            }

            // Temperature chart
            SectionCard(backgroundColor = CreamPanel) {
                Text("🌡️ Temperature — Last 14 Days", fontFamily = NunitoFont, fontWeight = FontWeight.ExtraBold, fontSize = 15.sp, color = TextBrown)
                Spacer(Modifier.height(12.dp))
                LineChart(data = tempData, color = AccentOrange, modifier = Modifier.fillMaxWidth().height(100.dp))
            }

            // Humidity chart
            SectionCard(backgroundColor = CreamPanel) {
                Text("💧 Humidity — Last 14 Days", fontFamily = NunitoFont, fontWeight = FontWeight.ExtraBold, fontSize = 15.sp, color = TextBrown)
                Spacer(Modifier.height(12.dp))
                LineChart(data = humidData, color = PrimaryYellow, modifier = Modifier.fillMaxWidth().height(100.dp))
            }

            // Mark turning button
            SpringButton(
                text = "🔄  Mark Turning",
                onClick = { viewModel.markTurning(batchId) },
                color = PrimaryYellow,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
private fun BigStatCard(icon: String, value: String, label: String, color: Color, modifier: Modifier = Modifier) {
    Column(
        modifier
            .shadow(6.dp, RoundedCornerShape(20.dp))
            .clip(RoundedCornerShape(20.dp))
            .background(color.copy(0.12f))
            .padding(18.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(icon, fontSize = 28.sp)
        Spacer(Modifier.height(8.dp))
        Text(value, fontFamily = NunitoFont, fontWeight = FontWeight.ExtraBold, fontSize = 26.sp, color = color)
        Text(label, fontFamily = NunitoFont, fontSize = 12.sp, color = TextBrownSoft)
    }
}

@Composable
private fun StatusIndicatorRow(icon: String, label: String, status: String, statusColor: Color) {
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(statusColor.copy(0.08f))
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(icon, fontSize = 18.sp)
        Spacer(Modifier.width(10.dp))
        Text(label, fontFamily = NunitoFont, fontWeight = FontWeight.SemiBold, color = TextBrown, modifier = Modifier.weight(1f))
        Box(
            Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(statusColor.copy(0.2f))
                .padding(horizontal = 10.dp, vertical = 4.dp)
        ) {
            Text(status, fontFamily = NunitoFont, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = statusColor)
        }
    }
    Spacer(Modifier.height(6.dp))
}

@Composable
private fun LineChart(data: List<Float>, color: Color, modifier: Modifier = Modifier) {
    Canvas(modifier) {
        if (data.size < 2) return@Canvas
        val min = data.min()
        val max = data.max()
        val range = (max - min).coerceAtLeast(0.1f)
        val points = data.mapIndexed { i, v ->
            Offset(
                x = i.toFloat() / (data.size - 1) * size.width,
                y = size.height - (v - min) / range * size.height
            )
        }
        // Fill area
        val path = Path().apply {
            moveTo(points.first().x, size.height)
            points.forEach { lineTo(it.x, it.y) }
            lineTo(points.last().x, size.height)
            close()
        }
        drawPath(path, Brush.verticalGradient(listOf(color.copy(0.3f), color.copy(0.02f))))

        // Line
        val linePath = Path().apply {
            moveTo(points.first().x, points.first().y)
            points.drop(1).forEach { lineTo(it.x, it.y) }
        }
        drawPath(linePath, color, style = Stroke(width = 3f, cap = StrokeCap.Round, join = StrokeJoin.Round))

        // Dots
        points.forEach { pt ->
            drawCircle(color, 5f, pt)
            drawCircle(Color.White, 2f, pt)
        }
    }
}

private fun generateSimulatedData(base: Float, count: Int): List<Float> {
    val rng = java.util.Random(42)
    return List(count) { base + (rng.nextFloat() - 0.5f) * 2f }
}