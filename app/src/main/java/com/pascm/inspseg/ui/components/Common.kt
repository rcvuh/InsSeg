package com.pascm.inspseg.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.pascm.inspseg.ui.theme.OutlineSubtle
import com.pascm.inspseg.ui.theme.Spacing

/** Encabezado de seccion, marcado como heading para lectores de pantalla. */
@Composable
fun SectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    trailing: String? = null
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = Spacing.s),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.semantics { heading() }
        )
        if (trailing != null) {
            Text(
                text = trailing,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * Aviso de bloque. Icono + titulo + detalle, con borde del color del estado.
 * Se usa para el paro critico y para la reinspeccion programada.
 */
@Composable
fun AlertBanner(
    icon: ImageVector,
    title: String,
    detail: String,
    accent: Color,
    container: Color,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(container, RoundedCornerShape(14.dp))
            .border(1.dp, accent.copy(alpha = 0.6f), RoundedCornerShape(14.dp))
            .padding(Spacing.l),
        horizontalArrangement = Arrangement.spacedBy(Spacing.m)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = accent,
            modifier = Modifier.size(22.dp)
        )
        Column(verticalArrangement = Arrangement.spacedBy(Spacing.xs)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = accent
            )
            Text(
                text = detail,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
fun ThinDivider(modifier: Modifier = Modifier) {
    HorizontalDivider(modifier = modifier, thickness = 1.dp, color = OutlineSubtle)
}
