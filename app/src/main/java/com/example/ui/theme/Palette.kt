package com.example.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import com.example.data.models.AppSettings

/**
 * Full palette definition containing every theme token for the entire app.
 */
data class ThemePaletteDefinition(
    val id: String,
    val name: String,
    val description: String,
    val primary: Color,
    val onPrimary: Color,
    val primaryContainer: Color,
    val onPrimaryContainer: Color,
    val secondary: Color,
    val onSecondary: Color,
    val secondaryContainer: Color,
    val onSecondaryContainer: Color,
    val tertiary: Color,
    val onTertiary: Color,
    val tertiaryContainer: Color,
    val onTertiaryContainer: Color,
    val background: Color,
    val onBackground: Color,
    val surface: Color,
    val onSurface: Color,
    val surfaceVariant: Color,
    val onSurfaceVariant: Color,
    val surfaceContainerLowest: Color,
    val surfaceContainerLow: Color,
    val surfaceContainer: Color,
    val surfaceContainerHigh: Color,
    val surfaceContainerHighest: Color,
    val surfaceBright: Color,
    val outline: Color,
    val outlineVariant: Color,
    val error: Color,
    val errorContainer: Color,
    val onError: Color,
    val statusReading: Color,
    val statusPlanned: Color,
    val statusCompleted: Color,
    val statusPaused: Color,
    val statusDropped: Color
)

/**
 * 1. Preset: Классическая тёмная (Синяя) — Оригинальная тема приложения
 * Тёмный графитовый фон (#131313) с классическим синим акцентом (#9ECAFF / #2196F3).
 */
val ClassicDarkPalette = ThemePaletteDefinition(
    id = "CLASSIC_DARK",
    name = "Классическая Синяя",
    description = "Оригинальная тёмная тема приложения с классическим синим акцентом",
    primary = Color(0xFF9ECAFF),
    onPrimary = Color(0xFF003258),
    primaryContainer = Color(0xFF2196F3),
    onPrimaryContainer = Color(0xFFD1E4FF),
    secondary = Color(0xFF78DC77),
    onSecondary = Color(0xFF00390A),
    secondaryContainer = Color(0xFF00761F),
    onSecondaryContainer = Color(0xFF95FB92),
    tertiary = Color(0xFFFFB77B),
    onTertiary = Color(0xFF4D2700),
    tertiaryContainer = Color(0xFFDB7900),
    onTertiaryContainer = Color(0xFFFFDCC2),
    background = Color(0xFF131313),
    onBackground = Color(0xFFE5E2E1),
    surface = Color(0xFF131313),
    onSurface = Color(0xFFE5E2E1),
    surfaceVariant = Color(0xFF404752),
    onSurfaceVariant = Color(0xFFBFC7D4),
    surfaceContainerLowest = Color(0xFF0E0E0E),
    surfaceContainerLow = Color(0xFF1C1B1B),
    surfaceContainer = Color(0xFF201F1F),
    surfaceContainerHigh = Color(0xFF2A2A2A),
    surfaceContainerHighest = Color(0xFF353534),
    surfaceBright = Color(0xFF393939),
    outline = Color(0xFF89919D),
    outlineVariant = Color(0xFF404752),
    error = Color(0xFFFFB4AB),
    errorContainer = Color(0xFF93000A),
    onError = Color(0xFF690005),
    statusReading = Color(0xFF78DC77),
    statusPlanned = Color(0xFFFFB77B),
    statusCompleted = Color(0xFF9ECAFF),
    statusPaused = Color(0xFFBFC7D4),
    statusDropped = Color(0xFFFFB4AB)
)

/**
 * 2. Preset: Графитовый Монохром (Monochrome Dark)
 * Спокойная тёмная тема в оттенках серого с мягкими белыми акцентами.
 */
val MonochromeDarkPalette = ThemePaletteDefinition(
    id = "MONOCHROME_DARK",
    name = "Графитовый Монохром",
    description = "Спокойная тёмная тема в оттенках серого с мягкими белыми акцентами",
    primary = Color(0xFFE2E2E2),
    onPrimary = Color(0xFF1E1E1E),
    primaryContainer = Color(0xFF333333),
    onPrimaryContainer = Color(0xFFF5F5F5),
    secondary = Color(0xFFB4B4B4),
    onSecondary = Color(0xFF242424),
    secondaryContainer = Color(0xFF383838),
    onSecondaryContainer = Color(0xFFD6D6D6),
    tertiary = Color(0xFF9E9E9E),
    onTertiary = Color(0xFF1F1F1F),
    tertiaryContainer = Color(0xFF2C2C2C),
    onTertiaryContainer = Color(0xFFC7C7C7),
    background = Color(0xFF0D0D0D),
    onBackground = Color(0xFFEAEAEA),
    surface = Color(0xFF141414),
    onSurface = Color(0xFFEAEAEA),
    surfaceVariant = Color(0xFF2C2C2C),
    onSurfaceVariant = Color(0xFFAFAFAF),
    surfaceContainerLowest = Color(0xFF000000),
    surfaceContainerLow = Color(0xFF111111),
    surfaceContainer = Color(0xFF1A1A1A),
    surfaceContainerHigh = Color(0xFF222222),
    surfaceContainerHighest = Color(0xFF292929),
    surfaceBright = Color(0xFF333333),
    outline = Color(0xFF757575),
    outlineVariant = Color(0xFF424242),
    error = Color(0xFFCF6679),
    errorContainer = Color(0xFF93000A),
    onError = Color(0xFF370B11),
    statusReading = Color(0xFF81C995),
    statusPlanned = Color(0xFFFDE293),
    statusCompleted = Color(0xFFA8C7FA),
    statusPaused = Color(0xFFC4C7C5),
    statusDropped = Color(0xFFF28B82)
)

/**
 * 3. Preset: Багровая Ночь (Crimson Night)
 * Элегантная тёмная тема с приглушёнными винными и бордовыми оттенками.
 */
val CrimsonNightPalette = ThemePaletteDefinition(
    id = "CRIMSON_NIGHT",
    name = "Багровая Ночь",
    description = "Элегантная тёмная тема с приглушёнными винными и бордовыми оттенками",
    primary = Color(0xFFE29B9B),
    onPrimary = Color(0xFF421515),
    primaryContainer = Color(0xFF6B2B2B),
    onPrimaryContainer = Color(0xFFFFDADA),
    secondary = Color(0xFFD4A3A3),
    onSecondary = Color(0xFF3D2020),
    secondaryContainer = Color(0xFF5C3333),
    onSecondaryContainer = Color(0xFFFADCDC),
    tertiary = Color(0xFFCFA07C),
    onTertiary = Color(0xFF3B2011),
    tertiaryContainer = Color(0xFF5A341E),
    onTertiaryContainer = Color(0xFFFCE1CE),
    background = Color(0xFF141010),
    onBackground = Color(0xFFEAE0E0),
    surface = Color(0xFF1A1414),
    onSurface = Color(0xFFEAE0E0),
    surfaceVariant = Color(0xFF3B2E2E),
    onSurfaceVariant = Color(0xFFBDB2B2),
    surfaceContainerLowest = Color(0xFF0F0B0B),
    surfaceContainerLow = Color(0xFF140F0F),
    surfaceContainer = Color(0xFF1C1515),
    surfaceContainerHigh = Color(0xFF261C1C),
    surfaceContainerHighest = Color(0xFF302323),
    surfaceBright = Color(0xFF3B2B2B),
    outline = Color(0xFF8C7D7D),
    outlineVariant = Color(0xFF473A3A),
    error = Color(0xFFE57373),
    errorContainer = Color(0xFF8C1D1D),
    onError = Color(0xFF400A0A),
    statusReading = Color(0xFF81C995),
    statusPlanned = Color(0xFFE2B785),
    statusCompleted = Color(0xFFE29B9B),
    statusPaused = Color(0xFFB5A9A9),
    statusDropped = Color(0xFFE57373)
)

/**
 * 4. Preset: Глубокий Лес (Deep Forest)
 * Приятная для глаз тёмно-зелёная тема с мягкими оливковыми акцентами.
 */
val DeepForestPalette = ThemePaletteDefinition(
    id = "DEEP_FOREST",
    name = "Глубокий Лес",
    description = "Приятная для глаз тёмно-зелёная тема с мягкими оливковыми акцентами",
    primary = Color(0xFFA3CFA3),
    onPrimary = Color(0xFF1B331B),
    primaryContainer = Color(0xFF325432),
    onPrimaryContainer = Color(0xFFD3EED3),
    secondary = Color(0xFFA9C9A9),
    onSecondary = Color(0xFF213621),
    secondaryContainer = Color(0xFF385238),
    onSecondaryContainer = Color(0xFFCFEACF),
    tertiary = Color(0xFFBCCAA0),
    onTertiary = Color(0xFF293517),
    tertiaryContainer = Color(0xFF404F2A),
    onTertiaryContainer = Color(0xFFDFEED3),
    background = Color(0xFF101410),
    onBackground = Color(0xFFE1E6E1),
    surface = Color(0xFF141A14),
    onSurface = Color(0xFFE1E6E1),
    surfaceVariant = Color(0xFF2C382C),
    onSurfaceVariant = Color(0xFFAAB5AA),
    surfaceContainerLowest = Color(0xFF0B0F0B),
    surfaceContainerLow = Color(0xFF0F140F),
    surfaceContainer = Color(0xFF151C15),
    surfaceContainerHigh = Color(0xFF1C261C),
    surfaceContainerHighest = Color(0xFF243024),
    surfaceBright = Color(0xFF2D3B2D),
    outline = Color(0xFF7A877A),
    outlineVariant = Color(0xFF3A473A),
    error = Color(0xFFE57373),
    errorContainer = Color(0xFF8C1D1D),
    onError = Color(0xFF400A0A),
    statusReading = Color(0xFFA3CFA3),
    statusPlanned = Color(0xFFD6C894),
    statusCompleted = Color(0xFF90C2C2),
    statusPaused = Color(0xFFABB5AB),
    statusDropped = Color(0xFFE57373)
)

/**
 * 5. Preset: Индиго Океан (Indigo Ocean)
 * Глубокая сине-фиолетовая тема, идеальная для чтения в темноте.
 */
val IndigoOceanPalette = ThemePaletteDefinition(
    id = "INDIGO_OCEAN",
    name = "Индиго Океан",
    description = "Глубокая сине-фиолетовая тема, идеальная для чтения в темноте",
    primary = Color(0xFFA3AFCF),
    onPrimary = Color(0xFF1B2333),
    primaryContainer = Color(0xFF324154),
    onPrimaryContainer = Color(0xFFD3DDEE),
    secondary = Color(0xFFA8B1C4),
    onSecondary = Color(0xFF212938),
    secondaryContainer = Color(0xFF394457),
    onSecondaryContainer = Color(0xFFCDD6EA),
    tertiary = Color(0xFFC3ADD4),
    onTertiary = Color(0xFF302040),
    tertiaryContainer = Color(0xFF483559),
    onTertiaryContainer = Color(0xFFE6D6F5),
    background = Color(0xFF0F1116),
    onBackground = Color(0xFFE2E4E8),
    surface = Color(0xFF14171E),
    onSurface = Color(0xFFE2E4E8),
    surfaceVariant = Color(0xFF2E3340),
    onSurfaceVariant = Color(0xFFB0B4C0),
    surfaceContainerLowest = Color(0xFF0A0C10),
    surfaceContainerLow = Color(0xFF0E1116),
    surfaceContainer = Color(0xFF151820),
    surfaceContainerHigh = Color(0xFF1D212B),
    surfaceContainerHighest = Color(0xFF252A36),
    surfaceBright = Color(0xFF2F3545),
    outline = Color(0xFF818694),
    outlineVariant = Color(0xFF404552),
    error = Color(0xFFE57373),
    errorContainer = Color(0xFF8C1D1D),
    onError = Color(0xFF400A0A),
    statusReading = Color(0xFF92C7A3),
    statusPlanned = Color(0xFFCDB693),
    statusCompleted = Color(0xFFA3AFCF),
    statusPaused = Color(0xFF9EA3B0),
    statusDropped = Color(0xFFC78F8F)
)

val PresetPalettes = listOf(
    ClassicDarkPalette,
    MonochromeDarkPalette,
    CrimsonNightPalette,
    DeepForestPalette,
    IndigoOceanPalette
)

/**
 * 🌟 REDESIGNED 2.0 SIGNATURE MASTER PALETTE
 * Exquisite obsidian-sapphire palette with amber/emerald neon accents, tailored specifically for ReadTracker 2.0.
 */
val RedesignedMasterPalette = ThemePaletteDefinition(
    id = "REDESIGNED_MASTER",
    name = "Obsidian Aurora 2.0",
    description = "Фирменная палитра обновленного интерфейса 2.0 с глубоким обсидиановым фоном и сапфировыми акцентами",
    primary = Color(0xFF60A5FA), // Electric Sapphire Blue
    onPrimary = Color(0xFF091E42),
    primaryContainer = Color(0xFF1E3A8A),
    onPrimaryContainer = Color(0xFFDBEAFE),
    secondary = Color(0xFF10B981), // Emerald Sage
    onSecondary = Color(0xFF022C22),
    secondaryContainer = Color(0xFF065F46),
    onSecondaryContainer = Color(0xFFD1FAE5),
    tertiary = Color(0xFFF59E0B), // Warm Luminous Amber
    onTertiary = Color(0xFF451A03),
    tertiaryContainer = Color(0xFF78350F),
    onTertiaryContainer = Color(0xFFFEF3C7),
    background = Color(0xFF0B0F19), // Deep Obsidian Navy
    onBackground = Color(0xFFF8FAFC),
    surface = Color(0xFF0F172A), // Slate Obsidian Glass
    onSurface = Color(0xFFF8FAFC),
    surfaceVariant = Color(0xFF1E293B),
    onSurfaceVariant = Color(0xFF94A3B8),
    surfaceContainerLowest = Color(0xFF06080F),
    surfaceContainerLow = Color(0xFF0D1322),
    surfaceContainer = Color(0xFF131B2E),
    surfaceContainerHigh = Color(0xFF1B253D),
    surfaceContainerHighest = Color(0xFF253250),
    surfaceBright = Color(0xFF2E3D60),
    outline = Color(0xFF334155),
    outlineVariant = Color(0xFF1E293B),
    error = Color(0xFFF87171),
    errorContainer = Color(0xFF7F1D1D),
    onError = Color(0xFF450A0A),
    statusReading = Color(0xFF10B981), // Emerald
    statusPlanned = Color(0xFF8B5CF6), // Amethyst Violet
    statusCompleted = Color(0xFF38BDF8), // Sky Blue
    statusPaused = Color(0xFF94A3B8), // Cool Slate
    statusDropped = Color(0xFFF43F5E) // Coral Rose
)

/**
 * Status colors bundle exposed via CompositionLocal
 */
data class StatusColors(
    val reading: Color,
    val planned: Color,
    val completed: Color,
    val paused: Color,
    val dropped: Color,
    val starGold: Color = Color(0xFFFFC107)
)

val LocalStatusColors = staticCompositionLocalOf {
    ClassicDarkPalette.toStatusColors()
}

val LocalStatusBadgeStyle = staticCompositionLocalOf {
    "PILL"
}

/**
 * Safe HEX Color parser with fallback
 */
fun parseColorHex(hex: String, defaultColor: Color): Color {
    val clean = hex.trim().removePrefix("#")
    return try {
        when (clean.length) {
            6 -> Color(android.graphics.Color.parseColor("#$clean"))
            8 -> Color(android.graphics.Color.parseColor("#$clean"))
            3 -> {
                val r = clean[0]
                val g = clean[1]
                val b = clean[2]
                Color(android.graphics.Color.parseColor("#$r$r$g$g$b$b"))
            }
            else -> defaultColor
        }
    } catch (e: Exception) {
        defaultColor
    }
}

/**
 * Extension to convert Jetpack Compose Color to 6-character uppercase HEX string (e.g. "#38BDF8")
 */
fun Color.toHex(includeAlpha: Boolean = false): String {
    val alpha = (this.alpha * 255).toInt().coerceIn(0, 255)
    val red = (this.red * 255).toInt().coerceIn(0, 255)
    val green = (this.green * 255).toInt().coerceIn(0, 255)
    val blue = (this.blue * 255).toInt().coerceIn(0, 255)
    return if (includeAlpha) {
        String.format("#%02X%02X%02X%02X", alpha, red, green, blue)
    } else {
        String.format("#%02X%02X%02X", red, green, blue)
    }
}

/**
 * Constructs a custom palette definition from user-configured AppSettings HEX fields.
 */
fun createCustomPalette(settings: AppSettings): ThemePaletteDefinition {
    val base = ClassicDarkPalette
    return ThemePaletteDefinition(
        id = "CUSTOM",
        name = "Пользовательская (HEX)",
        description = "Индивидуально настроенные цвета фона, поверхностей, кнопок и статусов",
        primary = parseColorHex(settings.customPrimaryHex, base.primary),
        onPrimary = parseColorHex(settings.customOnPrimaryHex, base.onPrimary),
        primaryContainer = parseColorHex(settings.customPrimaryContainerHex, base.primaryContainer),
        onPrimaryContainer = parseColorHex(settings.customOnPrimaryContainerHex, base.onPrimaryContainer),
        secondary = parseColorHex(settings.customSecondaryHex, base.secondary),
        onSecondary = parseColorHex(settings.customOnSecondaryHex, base.onSecondary),
        secondaryContainer = parseColorHex(settings.customSecondaryContainerHex, base.secondaryContainer),
        onSecondaryContainer = parseColorHex(settings.customOnSecondaryContainerHex, base.onSecondaryContainer),
        tertiary = parseColorHex(settings.customTertiaryHex, base.tertiary),
        onTertiary = parseColorHex(settings.customOnTertiaryHex, base.onTertiary),
        tertiaryContainer = parseColorHex(settings.customTertiaryContainerHex, base.tertiaryContainer),
        onTertiaryContainer = parseColorHex(settings.customOnTertiaryContainerHex, base.onTertiaryContainer),
        background = parseColorHex(settings.customBackgroundHex, base.background),
        onBackground = parseColorHex(settings.customOnBackgroundHex, base.onBackground),
        surface = parseColorHex(settings.customSurfaceHex, base.surface),
        onSurface = parseColorHex(settings.customOnSurfaceHex, base.onSurface),
        surfaceVariant = parseColorHex(settings.customSurfaceVariantHex, base.surfaceVariant),
        onSurfaceVariant = parseColorHex(settings.customOnSurfaceVariantHex, base.onSurfaceVariant),
        surfaceContainerLowest = parseColorHex(settings.customSurfaceContainerLowestHex, base.surfaceContainerLowest),
        surfaceContainerLow = parseColorHex(settings.customSurfaceContainerLowHex, base.surfaceContainerLow),
        surfaceContainer = parseColorHex(settings.customSurfaceContainerHex, base.surfaceContainer),
        surfaceContainerHigh = parseColorHex(settings.customSurfaceContainerHighHex, base.surfaceContainerHigh),
        surfaceContainerHighest = parseColorHex(settings.customSurfaceContainerHighestHex, base.surfaceContainerHighest),
        surfaceBright = parseColorHex(settings.customSurfaceBrightHex, base.surfaceBright),
        outline = parseColorHex(settings.customOutlineHex, base.outline),
        outlineVariant = parseColorHex(settings.customOutlineVariantHex, base.outlineVariant),
        error = parseColorHex(settings.customErrorHex, base.error),
        errorContainer = base.errorContainer,
        onError = base.onError,
        statusReading = parseColorHex(settings.customStatusReadingHex, base.statusReading),
        statusPlanned = parseColorHex(settings.customStatusPlannedHex, base.statusPlanned),
        statusCompleted = parseColorHex(settings.customStatusCompletedHex, base.statusCompleted),
        statusPaused = parseColorHex(settings.customStatusPausedHex, base.statusPaused),
        statusDropped = parseColorHex(settings.customStatusDroppedHex, base.statusDropped)
    )
}

/**
 * Converts palette definition into Android Material 3 ColorScheme.
 */
fun ThemePaletteDefinition.toColorScheme(): ColorScheme {
    return darkColorScheme(
        primary = primary,
        onPrimary = onPrimary,
        primaryContainer = primaryContainer,
        onPrimaryContainer = onPrimaryContainer,
        secondary = secondary,
        onSecondary = onSecondary,
        secondaryContainer = secondaryContainer,
        onSecondaryContainer = onSecondaryContainer,
        tertiary = tertiary,
        onTertiary = onTertiary,
        tertiaryContainer = tertiaryContainer,
        onTertiaryContainer = onTertiaryContainer,
        background = background,
        onBackground = onBackground,
        surface = surface,
        onSurface = onSurface,
        surfaceVariant = surfaceVariant,
        onSurfaceVariant = onSurfaceVariant,
        surfaceContainer = surfaceContainer,
        surfaceContainerLow = surfaceContainerLow,
        surfaceContainerHigh = surfaceContainerHigh,
        surfaceContainerHighest = surfaceContainerHighest,
        surfaceContainerLowest = surfaceContainerLowest,
        surfaceBright = surfaceBright,
        outline = outline,
        outlineVariant = outlineVariant,
        error = error,
        errorContainer = errorContainer,
        onError = onError
    )
}

/**
 * Converts palette definition into StatusColors bundle.
 */
fun ThemePaletteDefinition.toStatusColors(): StatusColors {
    return StatusColors(
        reading = statusReading,
        planned = statusPlanned,
        completed = statusCompleted,
        paused = statusPaused,
        dropped = statusDropped
    )
}

/**
 * Builds dynamic ColorScheme from active AppSettings.
 */
fun buildColorSchemeFromSettings(settings: AppSettings): ColorScheme {
    if (settings.redesignedUiEnabled) {
        return RedesignedMasterPalette.toColorScheme()
    }
    val palette = when (settings.activePalette) {
        "CLASSIC_DARK", "DEFAULT", "CLASSIC" -> ClassicDarkPalette
        "MONOCHROME_DARK" -> MonochromeDarkPalette
        "CRIMSON_NIGHT" -> CrimsonNightPalette
        "DEEP_FOREST" -> DeepForestPalette
        "INDIGO_OCEAN" -> IndigoOceanPalette
        "CUSTOM" -> createCustomPalette(settings)
        else -> ClassicDarkPalette
    }
    return palette.toColorScheme()
}

/**
 * Builds dynamic StatusColors from active AppSettings.
 */
fun buildStatusColorsFromSettings(settings: AppSettings): StatusColors {
    if (settings.redesignedUiEnabled) {
        return RedesignedMasterPalette.toStatusColors()
    }
    val palette = when (settings.activePalette) {
        "CLASSIC_DARK", "DEFAULT", "CLASSIC" -> ClassicDarkPalette
        "MONOCHROME_DARK" -> MonochromeDarkPalette
        "CRIMSON_NIGHT" -> CrimsonNightPalette
        "DEEP_FOREST" -> DeepForestPalette
        "INDIGO_OCEAN" -> IndigoOceanPalette
        "CUSTOM" -> createCustomPalette(settings)
        else -> ClassicDarkPalette
    }
    return palette.toStatusColors()
}
