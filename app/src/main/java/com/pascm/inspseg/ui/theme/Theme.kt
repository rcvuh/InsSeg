package com.pascm.inspseg.ui.theme

import android.app.Activity
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat

/**
 * Tema de la aplicacion.
 *
 * Excepcion deliberada a "respetar la apariencia del sistema": la app usa una
 * apariencia oscura permanente en cualquier dispositivo. Es una herramienta de
 * campo que se opera en naves, subestaciones y recorridos nocturnos, donde una
 * pantalla clara deslumbra y arruina la adaptacion a la oscuridad. La guia de
 * Modo Oscuro contempla este caso: "en casos contados, considere usar solo la
 * apariencia oscura". No se ofrece un ajuste de apariencia dentro de la app.
 */
private val InspSegColorScheme = darkColorScheme(
    // Accion principal monocromatica: el color queda reservado para el estado de seguridad.
    primary = TextPrimary,
    onPrimary = Void,
    primaryContainer = SurfaceHigh,
    onPrimaryContainer = TextPrimary,

    secondary = TextSecondary,
    onSecondary = Void,
    secondaryContainer = SurfaceHigh,
    onSecondaryContainer = TextPrimary,

    tertiary = InfoSteel,
    onTertiary = Void,
    tertiaryContainer = InfoContainer,
    onTertiaryContainer = InfoSteel,

    background = Void,
    onBackground = TextPrimary,

    surface = Void,
    onSurface = TextPrimary,
    surfaceVariant = SurfaceHigh,
    onSurfaceVariant = TextSecondary,

    surfaceContainerLowest = Void,
    surfaceContainerLow = SurfaceBase,
    surfaceContainer = SurfaceBase,
    surfaceContainerHigh = SurfaceRaised,
    surfaceContainerHighest = SurfaceHigh,

    outline = OutlineStrong,
    outlineVariant = OutlineSubtle,

    error = CriticalRed,
    onError = Void,
    errorContainer = CriticalContainer,
    onErrorContainer = CriticalRed,

    scrim = Void
)

private val InspSegShapes = Shapes(
    extraSmall = RoundedCornerShape(6.dp),
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(14.dp),
    large = RoundedCornerShape(20.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

/** Espaciado en multiplos de 4 dp para mantener un ritmo vertical consistente. */
object Spacing {
    val xs = 4.dp
    val s = 8.dp
    val m = 12.dp
    val l = 16.dp
    val xl = 24.dp
    val xxl = 32.dp

    /** Margen lateral del contenido. */
    val screen = 20.dp

    /** Area tactil minima recomendada. */
    val minTouchTarget = 48.dp
}

@Composable
fun InspSegTheme(content: @Composable () -> Unit) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            // Iconos claros en barras de sistema: el fondo siempre es negro.
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = false
                isAppearanceLightNavigationBars = false
            }
        }
    }
    MaterialTheme(
        colorScheme = InspSegColorScheme,
        typography = InspSegTypography,
        shapes = InspSegShapes,
        content = content
    )
}
