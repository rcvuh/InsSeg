package com.pascm.inspseg.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.unit.dp
import com.pascm.inspseg.domain.model.AnswerResult
import com.pascm.inspseg.domain.model.CriticalityLevel
import com.pascm.inspseg.domain.model.IncidenceStatus
import com.pascm.inspseg.domain.model.InspectionModule
import com.pascm.inspseg.domain.model.InspectionStatus
import com.pascm.inspseg.ui.theme.CriticalContainer
import com.pascm.inspseg.ui.theme.CriticalRed
import com.pascm.inspseg.ui.theme.InfoContainer
import com.pascm.inspseg.ui.theme.InfoSteel
import com.pascm.inspseg.ui.theme.SafeContainer
import com.pascm.inspseg.ui.theme.SafeGreen
import com.pascm.inspseg.ui.theme.Spacing
import com.pascm.inspseg.ui.theme.WarningAmber
import com.pascm.inspseg.ui.theme.WarningContainer

/** Par color + icono + contenedor que define visualmente un estado de seguridad. */
data class StatusVisual(
    val label: String,
    val color: Color,
    val container: Color,
    val icon: ImageVector
)

fun InspectionStatus.visual(): StatusVisual = when (this) {
    InspectionStatus.EN_CAPTURA ->
        StatusVisual(label, InfoSteel, InfoContainer, Icons.Filled.EditNote)
    InspectionStatus.CONFORME ->
        StatusVisual(label, SafeGreen, SafeContainer, Icons.Filled.CheckCircle)
    InspectionStatus.REINSPECCION_24H ->
        StatusVisual(label, WarningAmber, WarningContainer, Icons.Filled.Schedule)
    InspectionStatus.DETENCION_LABORES ->
        StatusVisual(label, CriticalRed, CriticalContainer, Icons.Filled.Block)
}

fun IncidenceStatus.visual(): StatusVisual = when (this) {
    IncidenceStatus.ABIERTO ->
        StatusVisual(label, CriticalRed, CriticalContainer, Icons.Filled.Warning)
    IncidenceStatus.EN_MITIGACION ->
        StatusVisual(label, WarningAmber, WarningContainer, Icons.Filled.Schedule)
    IncidenceStatus.CERRADO ->
        StatusVisual(label, SafeGreen, SafeContainer, Icons.Filled.CheckCircle)
}

fun CriticalityLevel.visual(): StatusVisual = when (this) {
    CriticalityLevel.ALTO ->
        StatusVisual("Riesgo alto", CriticalRed, CriticalContainer, Icons.Filled.Warning)
    CriticalityLevel.MEDIO ->
        StatusVisual("Riesgo medio", WarningAmber, WarningContainer, Icons.Filled.Warning)
    CriticalityLevel.BAJO ->
        StatusVisual("Riesgo bajo", InfoSteel, InfoContainer, Icons.Filled.Warning)
}

fun AnswerResult.visual(): StatusVisual = when (this) {
    AnswerResult.CONFORME ->
        StatusVisual(label, SafeGreen, SafeContainer, Icons.Filled.CheckCircle)
    AnswerResult.NO_CONFORME ->
        StatusVisual(label, CriticalRed, CriticalContainer, Icons.Filled.Warning)
    AnswerResult.NO_APLICA ->
        StatusVisual(label, InfoSteel, InfoContainer, Icons.Filled.Block)
    AnswerResult.SIN_EVALUAR ->
        StatusVisual(label, InfoSteel, InfoContainer, Icons.Filled.EditNote)
}

fun InspectionModule.icon(): ImageVector = when (this) {
    InspectionModule.EPP -> Icons.Filled.Shield
    InspectionModule.MAQUINARIA -> Icons.Filled.Build
    InspectionModule.ENTORNO -> Icons.Filled.Place
}

/**
 * Etiqueta de estado. Siempre combina icono y texto: el color nunca es el unico
 * portador de la informacion, por accesibilidad y por vision cromatica reducida.
 */
@Composable
fun StatusBadge(
    visual: StatusVisual,
    modifier: Modifier = Modifier,
    compact: Boolean = false
) {
    Row(
        modifier = modifier
            .background(visual.container, RoundedCornerShape(8.dp))
            .border(1.dp, visual.color.copy(alpha = 0.45f), RoundedCornerShape(8.dp))
            .padding(horizontal = if (compact) 8.dp else Spacing.m, vertical = 6.dp)
            .clearAndSetSemantics { contentDescription = "Estado: ${visual.label}" },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Icon(
            imageVector = visual.icon,
            contentDescription = null,
            tint = visual.color,
            modifier = Modifier.size(if (compact) 14.dp else 16.dp)
        )
        Text(
            text = visual.label,
            style = if (compact) MaterialTheme.typography.labelSmall
            else MaterialTheme.typography.labelMedium,
            color = visual.color
        )
    }
}
