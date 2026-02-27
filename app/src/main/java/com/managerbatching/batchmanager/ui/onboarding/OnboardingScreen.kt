package com.managerbatching.batchmanager.ui.onboarding

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.managerbatching.batchmanager.ui.theme.*
import kotlinx.coroutines.launch

data class OnboardingPage(
    val emoji: String,
    val title: String,
    val subtitle: String,
    val backgroundColor: Color,
    val accentColor: Color
)

val onboardingPages = listOf(
    OnboardingPage("🥚", "Track Your Batches", "Manage all your egg batches from collection to hatching in one beautiful place.", BackgroundYellow, PrimaryYellow),
    OnboardingPage("🌡️", "Monitor Incubation", "Keep real-time tabs on temperature, humidity, and turning schedules for perfect results.", CreamPanel, AccentOrange),
    OnboardingPage("📊", "Analyze Results", "Review hatch rates, track issues, and export detailed reports for each batch.", CardSandy, StatusGold),
    OnboardingPage("🐣", "Celebrate Success", "Watch your batches hatch and build a history of your best-performing egg batches!", BackgroundYellow, SuccessGreen)
)

@Composable
fun OnboardingScreen(onFinish: () -> Unit) {
    var currentPage by remember { mutableStateOf(0) }
    val scope = rememberCoroutineScope()

    // Float animation for illustration
    val floatAnim by rememberInfiniteTransition(label = "float").animateFloat(
        initialValue = -12f, targetValue = 12f,
        animationSpec = infiniteRepeatable(tween(2000, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "float"
    )

    val rotateAnim by rememberInfiniteTransition(label = "rotate").animateFloat(
        initialValue = -5f, targetValue = 5f,
        animationSpec = infiniteRepeatable(tween(3000, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "rotate"
    )

    val page = onboardingPages[currentPage]

    val bgColor by animateColorAsState(page.backgroundColor, tween(500), label = "bg")
    val accentColor by animateColorAsState(page.accentColor, tween(500), label = "accent")
    val emojiScale by animateFloatAsState(
        targetValue = 1f, animationSpec = spring(Spring.DampingRatioMediumBouncy), label = "emojiScale",
        // trigger scale pulse on page change
    )

    var dragOffset by remember { mutableStateOf(0f) }

    Box(
        Modifier
            .fillMaxSize()
            .background(bgColor)
            .pointerInput(Unit) {
                detectHorizontalDragGestures(
                    onDragEnd = {
                        if (dragOffset < -80f && currentPage < onboardingPages.size - 1) currentPage++
                        else if (dragOffset > 80f && currentPage > 0) currentPage--
                        dragOffset = 0f
                    },
                    onHorizontalDrag = { _, delta -> dragOffset += delta }
                )
            }
    ) {
        // Decorative circles in background
        Box(
            Modifier
                .size(300.dp)
                .offset(y = (-50).dp, x = (-80).dp)
                .clip(CircleShape)
                .background(accentColor.copy(alpha = 0.1f))
        )
        Box(
            Modifier
                .size(200.dp)
                .align(Alignment.BottomEnd)
                .offset(y = 50.dp, x = 50.dp)
                .clip(CircleShape)
                .background(accentColor.copy(alpha = 0.15f))
        )

        Column(
            Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(Modifier.weight(1f))

            // Animated emoji illustration
            Box(
                Modifier
                    .size(160.dp)
                    .offset(y = floatAnim.dp)
                    .rotate(rotateAnim)
                    .clip(RoundedCornerShape(40.dp))
                    .background(
                        Brush.radialGradient(
                            colors = listOf(accentColor.copy(0.3f), accentColor.copy(0.05f))
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(page.emoji, fontSize = 80.sp)
            }

            Spacer(Modifier.height(48.dp))

            Text(
                page.title,
                fontFamily = NunitoFont,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 28.sp,
                color = TextBrown,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(16.dp))

            Text(
                page.subtitle,
                fontFamily = NunitoFont,
                fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp,
                color = TextBrownSoft,
                textAlign = TextAlign.Center,
                lineHeight = 24.sp
            )

            Spacer(Modifier.weight(1f))

            // Page indicators
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                onboardingPages.forEachIndexed { idx, _ ->
                    val width by animateDpAsState(
                        if (idx == currentPage) 32.dp else 8.dp,
                        spring(Spring.DampingRatioMediumBouncy), label = "indicator"
                    )
                    Box(
                        Modifier
                            .height(8.dp)
                            .width(width)
                            .clip(RoundedCornerShape(4.dp))
                            .background(if (idx == currentPage) accentColor else accentColor.copy(0.3f))
                    )
                }
            }

            Spacer(Modifier.height(32.dp))

            if (currentPage < onboardingPages.size - 1) {
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onFinish) {
                        Text(
                            "Skip",
                            fontFamily = NunitoFont,
                            fontWeight = FontWeight.Bold,
                            color = TextBrownSoft
                        )
                    }
                    SpringButton(
                        text = "Next →",
                        onClick = { currentPage++ },
                        color = accentColor,
                        modifier = Modifier.width(140.dp)
                    )
                }
            } else {
                SpringButton(
                    text = "🐣  Get Started!",
                    onClick = onFinish,
                    color = accentColor,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(Modifier.height(48.dp))
        }
    }
}