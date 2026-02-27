package com.managerbatching.batchmanager.ui.theme

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.managerbatching.batchmanager.R

// Colors
val BackgroundYellow = Color(0xFFFFF6D5)
val CardSandy = Color(0xFFFFE8A8)
val CreamPanel = Color(0xFFFFF1C2)
val PrimaryYellow = Color(0xFFFFB800)
val AccentOrange = Color(0xFFFF7A00)
val CriticalRed = Color(0xFFFF2E00)
val TextBrown = Color(0xFF3A2400)
val TextBrownSoft = Color(0xFF6B4A12)
val StatusGold = Color(0xFFFFCF57)
val SuccessGreen = Color(0xFF3FDA52)
val HighlightYellow = Color(0xFFFFE58F)

val NunitoFont = FontFamily(
    Font(R.font.nunito, FontWeight.Normal),
    Font(R.font.nunito_semibold, FontWeight.SemiBold),
    Font(R.font.nunito_bold, FontWeight.Bold),
    Font(R.font.nunito_extrabold, FontWeight.ExtraBold)
)

val BatchTypography = Typography(
    displayLarge = TextStyle(fontFamily = NunitoFont, fontWeight = FontWeight.ExtraBold, fontSize = 32.sp, color = TextBrown),
    displayMedium = TextStyle(fontFamily = NunitoFont, fontWeight = FontWeight.ExtraBold, fontSize = 26.sp, color = TextBrown),
    headlineLarge = TextStyle(fontFamily = NunitoFont, fontWeight = FontWeight.Bold, fontSize = 22.sp, color = TextBrown),
    headlineMedium = TextStyle(fontFamily = NunitoFont, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = TextBrown),
    titleLarge = TextStyle(fontFamily = NunitoFont, fontWeight = FontWeight.SemiBold, fontSize = 16.sp, color = TextBrown),
    bodyLarge = TextStyle(fontFamily = NunitoFont, fontWeight = FontWeight.Normal, fontSize = 16.sp, color = TextBrown),
    bodyMedium = TextStyle(fontFamily = NunitoFont, fontWeight = FontWeight.Normal, fontSize = 14.sp, color = TextBrownSoft),
    labelLarge = TextStyle(fontFamily = NunitoFont, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextBrown),
)

private val BatchColorScheme = lightColorScheme(
    primary = PrimaryYellow,
    onPrimary = TextBrown,
    secondary = AccentOrange,
    onSecondary = Color.White,
    background = BackgroundYellow,
    onBackground = TextBrown,
    surface = CardSandy,
    onSurface = TextBrown,
    error = CriticalRed,
    onError = Color.White
)

@Composable
fun BatchManagerTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = BatchColorScheme,
        typography = BatchTypography,
        content = content
    )
}