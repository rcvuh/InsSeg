package com.pascm.inspseg.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.unit.dp
import com.pascm.inspseg.domain.rules.SafetyRuleEngine
import com.pascm.inspseg.ui.theme.CriticalRed
import com.pascm.inspseg.ui.theme.SafeGreen
import com.pascm.inspseg.ui.theme.Spacing
import com.pascm.inspseg.ui.theme.SurfaceHigh
import com.pascm.inspseg.ui.theme.TextPrimary
import com.pascm.inspseg.ui.theme.WarningAmber
import kotlin.math.roundToInt

/**
 * Medidor de cumplimiento ponderado, con la marca del umbral normativo de 85 %.
 * El valor se comunica con numero y con etiqueta de texto, no solo con color.
 */
@Composable
fun ComplianceMeter(
    scorePercent: Double,
    evaluatedCount: Int,
    modifier: Modifier = Modifier
) {
    val hasData = evaluatedCount > 0
    val target = if (hasData) (scorePercent / 100.0).toFloat().coerceIn(0f, 1f) else 0f
    val animated by animateFloatAsState(targetValue = target, label = "cumplimiento")

    val barColor = when {
        !hasData -> SurfaceHigh
        scorePercent >= SafetyRuleEngine.MIN_COMPLIANCE_PERCENT -> SafeGreen
        scorePercent >= 60 -> WarningAmber
        else -> CriticalRed
    }
    val readout = if (hasData) "${scorePercent.roundToInt()} %" else "Sin datos"
    val verdict = when {
        !hasData -> "Aun no hay puntos evaluados"
        scorePercent >= SafetyRuleEngine.MIN_COMPLIANCE_PERCENT -> "Sobre el umbral de 85 %"
        else -> "Bajo el umbral: reinspeccion en 24 h"
    }

    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            Text(
                text = "Cumplimiento ponderado",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = readout,
                style = MaterialTheme.typography.headlineSmall,
                color = if (hasData) barColor else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Canvas(
            modifier = Modifier
                .padding(top = Spacing.s)
                .fillMaxWidth()
                .height(10.dp)
                .clearAndSetSemantics {
                    contentDescription = "Cumplimiento $readout. $verdict."
                }
        ) {
            val radius = CornerRadius(size.height / 2, size.height / 2)
            drawRoundRect(color = SurfaceHigh, size = size, cornerRadius = radius)
            if (animated > 0f) {
                drawRoundRect(
                    color = barColor,
                    size = Size(size.width * animated, size.height),
                    cornerRadius = radius
                )
            }
            // Marca del umbral normativo (85 %).
            val thresholdX = size.width * (SafetyRuleEngine.MIN_COMPLIANCE_PERCENT / 100f).toFloat()
            drawLine(
                color = TextPrimary,
                start = Offset(thresholdX, -2f),
                end = Offset(thresholdX, size.height + 2f),
                strokeWidth = 2f
            )
        }

        Text(
            text = verdict,
            modifier = Modifier.padding(top = Spacing.s),
            style = MaterialTheme.typography.labelSmall,
            color = if (hasData) barColor else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/** Barra de avance de captura, neutra: mide progreso, no riesgo. */
@Composable
fun CaptureProgress(progress: Float, answered: Int, total: Int, modifier: Modifier = Modifier) {
    val animated by animateFloatAsState(targetValue = progress.coerceIn(0f, 1f), label = "avance")
    Column(modifier = modifier) {
        Text(
            text = "Avance de captura: $answered de $total puntos aplicables",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Canvas(
            modifier = Modifier
                .padding(top = 6.dp)
                .fillMaxWidth()
                .height(6.dp)
        ) {
            val radius = CornerRadius(size.height / 2, size.height / 2)
            drawRoundRect(color = SurfaceHigh, size = size, cornerRadius = radius)
            if (animated > 0f) {
                drawRoundRect(
                    color = Color(0xFFF5F5F7),
                    size = Size(size.width * animated, size.height),
                    cornerRadius = radius
                )
            }
        }
    }
}
