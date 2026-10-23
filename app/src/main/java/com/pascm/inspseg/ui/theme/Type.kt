package com.pascm.inspseg.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/*
 * Escala tipografica unica, con la fuente del sistema y tamaños en sp para que
 * respeten el ajuste de tamaño de texto del dispositivo.
 *
 * No se usan pesos ligeros (Thin/Light): a plena luz de dia y con guantes, el
 * texto delgado sobre negro pierde legibilidad. Minimo 12 sp para metadatos.
 */
private val System = FontFamily.Default

val InspSegTypography = Typography(
    displaySmall = TextStyle(
        fontFamily = System, fontWeight = FontWeight.SemiBold,
        fontSize = 34.sp, lineHeight = 40.sp
    ),
    headlineMedium = TextStyle(
        fontFamily = System, fontWeight = FontWeight.SemiBold,
        fontSize = 26.sp, lineHeight = 32.sp
    ),
    headlineSmall = TextStyle(
        fontFamily = System, fontWeight = FontWeight.SemiBold,
        fontSize = 22.sp, lineHeight = 28.sp
    ),
    titleLarge = TextStyle(
        fontFamily = System, fontWeight = FontWeight.SemiBold,
        fontSize = 19.sp, lineHeight = 25.sp
    ),
    titleMedium = TextStyle(
        fontFamily = System, fontWeight = FontWeight.Medium,
        fontSize = 17.sp, lineHeight = 23.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = System, fontWeight = FontWeight.Normal,
        fontSize = 17.sp, lineHeight = 24.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = System, fontWeight = FontWeight.Normal,
        fontSize = 15.sp, lineHeight = 21.sp
    ),
    labelLarge = TextStyle(
        fontFamily = System, fontWeight = FontWeight.SemiBold,
        fontSize = 15.sp, lineHeight = 20.sp, letterSpacing = 0.1.sp
    ),
    labelMedium = TextStyle(
        fontFamily = System, fontWeight = FontWeight.Medium,
        fontSize = 13.sp, lineHeight = 18.sp
    ),
    labelSmall = TextStyle(
        fontFamily = System, fontWeight = FontWeight.Medium,
        fontSize = 12.sp, lineHeight = 16.sp, letterSpacing = 0.3.sp
    )
)
