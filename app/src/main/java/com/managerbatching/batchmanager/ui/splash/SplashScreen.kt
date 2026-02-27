package com.managerbatching.batchmanager.ui.splash

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.managerbatching.batchmanager.ui.theme.*
import kotlinx.coroutines.delay
import kotlin.math.*
import kotlin.random.Random

data class Particle(
    val x: Float, val y: Float,
    val vx: Float, val vy: Float,
    val radius: Float, val color: Color,
    var life: Float = 1f
)

@Composable
fun SplashContent(onFinished: () -> Unit) {
    var phase by remember { mutableStateOf(0) } // 0=grow, 1=crack, 2=explode, 3=fade

    val eggScale by animateFloatAsState(
        targetValue = if (phase >= 1) 1f else 0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
        label = "eggScale"
    )

    val crackAlpha by animateFloatAsState(
        targetValue = if (phase >= 1) 1f else 0f,
        animationSpec = tween(300),
        label = "crackAlpha"
    )

    val titleAlpha by animateFloatAsState(
        targetValue = if (phase >= 2) 1f else 0f,
        animationSpec = tween(600),
        label = "titleAlpha"
    )

    val screenAlpha by animateFloatAsState(
        targetValue = if (phase >= 3) 0f else 1f,
        animationSpec = tween(500),
        label = "screenAlpha",
        finishedListener = { if (phase >= 3) onFinished() }
    )

    var particles by remember { mutableStateOf(emptyList<Particle>()) }
    val time by rememberInfiniteTransition(label = "time").animateFloat(
        initialValue = 0f, targetValue = 1000f,
        animationSpec = infiniteRepeatable(tween(100000, easing = LinearEasing)),
        label = "time"
    )

    LaunchedEffect(Unit) {
        delay(300); phase = 1
        delay(600); phase = 2
        particles = List(40) {
            Particle(
                x = 0.5f, y = 0.45f,
                vx = (Random.nextFloat() - 0.5f) * 0.015f,
                vy = (Random.nextFloat() - 0.8f) * 0.015f,
                radius = Random.nextFloat() * 12f + 4f,
                color = listOf(PrimaryYellow, AccentOrange, StatusGold, HighlightYellow).random()
            )
        }
        delay(2000); phase = 3
    }

    Box(
        Modifier
            .fillMaxSize()
            .alpha(screenAlpha)
            .background(BackgroundYellow),
        contentAlignment = Alignment.Center
    ) {
        Canvas(Modifier.fillMaxSize()) {
            // Animated particles
            particles.forEachIndexed { idx, p ->
                val t = (time / 60f + idx) % 1000f
                val nx = p.x + p.vx * t
                val ny = p.y + p.vy * t + 0.00005f * t * t
                val life = maxOf(0f, 1f - t / 80f)
                if (life > 0f) {
                    drawCircle(
                        color = p.color.copy(alpha = life),
                        radius = p.radius * life,
                        center = Offset(nx * size.width, ny * size.height)
                    )
                }
            }

            // Draw egg
            if (eggScale > 0f) {
                drawEgg(
                    center = Offset(size.width / 2f, size.height * 0.42f),
                    scale = eggScale,
                    crackAlpha = crackAlpha
                )
            }
        }

        Column(
            Modifier
                .fillMaxWidth()
                .padding(top = 280.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "Batch Manager",
                fontFamily = NunitoFont,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 34.sp,
                color = TextBrown,
                modifier = Modifier.alpha(titleAlpha)
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "Your egg incubation companion",
                fontFamily = NunitoFont,
                fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp,
                color = TextBrownSoft,
                modifier = Modifier.alpha(titleAlpha)
            )
        }
    }
}

private fun DrawScope.drawEgg(center: Offset, scale: Float, crackAlpha: Float) {
    val w = 120f * scale
    val h = 160f * scale
    // Egg body
    drawOval(
        color = PrimaryYellow,
        topLeft = Offset(center.x - w / 2f, center.y - h / 2f),
        size = Size(w, h)
    )
    // Shine
    drawOval(
        color = HighlightYellow,
        topLeft = Offset(center.x - w * 0.25f, center.y - h * 0.3f),
        size = Size(w * 0.25f, h * 0.2f)
    )
    // Crack lines
    if (crackAlpha > 0f) {
        val alpha = crackAlpha
        drawLine(
            color = TextBrown.copy(alpha = alpha * 0.6f),
            start = Offset(center.x - 10f, center.y - 20f),
            end = Offset(center.x + 5f, center.y),
            strokeWidth = 3f
        )
        drawLine(
            color = TextBrown.copy(alpha = alpha * 0.6f),
            start = Offset(center.x + 5f, center.y),
            end = Offset(center.x - 15f, center.y + 15f),
            strokeWidth = 3f
        )
        drawLine(
            color = TextBrown.copy(alpha = alpha * 0.4f),
            start = Offset(center.x + 5f, center.y),
            end = Offset(center.x + 20f, center.y + 10f),
            strokeWidth = 2f
        )
    }
}