package com.loantracker.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

// Paleta principal: fundo bem escuro pro "hero" da tela inicial e destaques,
// corpo claro e minimalista pro resto — um visual único e consistente,
// independente do tema (claro/escuro) do sistema do usuário.
val CorFundoEscuro = Color(0xFF0E0E0F)
val CorSuperficieEscura = Color(0xFF232326)
val PrimaryGreen = Color(0xFF2E7D32)
val PrimaryGreenDark = Color(0xFF1B5E20)
val AlertRed = Color(0xFFE53935)
val WarningOrange = Color(0xFFFB8C00)
val CorAmarela = Color(0xFFFDD835)
val NeutralBackground = Color(0xFFFAFAFA)

private val AppColors = lightColorScheme(
    primary = CorFundoEscuro,
    onPrimary = Color.White,
    secondary = PrimaryGreenDark,
    background = NeutralBackground,
    surface = Color.White,
    surfaceVariant = Color(0xFFF0F0F0),
    error = AlertRed
)

// Cantos mais arredondados em tudo (cards, botões, campos) pra um visual
// mais suave e moderno, seguindo o estilo do mockup (formas em pílula).
private val AppShapes = Shapes(
    extraSmall = androidx.compose.foundation.shape.RoundedCornerShape(8.dp),
    small = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
    medium = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
    large = androidx.compose.foundation.shape.RoundedCornerShape(24.dp),
    extraLarge = androidx.compose.foundation.shape.RoundedCornerShape(32.dp)
)

private val AppTypography = Typography().let { base ->
    base.copy(
        headlineMedium = base.headlineMedium.copy(fontWeight = FontWeight.Bold),
        headlineSmall = base.headlineSmall.copy(fontWeight = FontWeight.Bold),
        titleLarge = base.titleLarge.copy(fontWeight = FontWeight.Bold),
        titleMedium = base.titleMedium.copy(fontWeight = FontWeight.SemiBold)
    )
}

@Composable
fun LoanTrackerTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = AppColors,
        shapes = AppShapes,
        typography = AppTypography,
        content = content
    )
}
