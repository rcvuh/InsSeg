package com.pascm.inspseg.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.pascm.inspseg.domain.model.AnswerResult
import com.pascm.inspseg.domain.model.ChecklistPoint
import com.pascm.inspseg.ui.theme.CriticalContainer
import com.pascm.inspseg.ui.theme.CriticalRed
import com.pascm.inspseg.ui.theme.InfoSteel
import com.pascm.inspseg.ui.theme.OutlineStrong
import com.pascm.inspseg.ui.theme.OutlineSubtle
import com.pascm.inspseg.ui.theme.SafeContainer
import com.pascm.inspseg.ui.theme.SafeGreen
import com.pascm.inspseg.ui.theme.Spacing
import com.pascm.inspseg.ui.theme.SurfaceBase
import com.pascm.inspseg.ui.theme.SurfaceHigh
import com.pascm.inspseg.ui.theme.TextSecondary
import com.pascm.inspseg.ui.theme.TextTertiary

/**
 * Punto de control del formulario.
 *
 * Cuando el resultado es "No conforme" la tarjeta despliega el bloque de
 * evidencia; si ademas el punto detiene labores, muestra el aviso de paro
 * critico. El bloqueo del guardado lo decide el ViewModel, aqui solo se refleja.
 */
@Composable
fun ChecklistItemCard(
    point: ChecklistPoint,
    showEvidenceError: Boolean,
    onResult: (AnswerResult) -> Unit,
    onNoteChange: (String) -> Unit,
    onCapturePhoto: () -> Unit,
    onNormativeClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val criticality = point.item.criticality.visual()
    val borderColor = when {
        point.triggersStop -> CriticalRed
        point.result == AnswerResult.NO_CONFORME -> CriticalRed.copy(alpha = 0.5f)
        else -> OutlineSubtle
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(SurfaceBase, RoundedCornerShape(14.dp))
            .border(1.dp, borderColor, RoundedCornerShape(14.dp))
            .padding(Spacing.l)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(Spacing.s),
            verticalAlignment = Alignment.CenterVertically
        ) {
            StatusBadge(visual = criticality, compact = true)
            NormativeChip(code = point.item.normativeCode, onClick = onNormativeClick)
            if (point.item.requiresPhoto) {
                Icon(
                    imageVector = Icons.Filled.PhotoCamera,
                    contentDescription = "Requiere fotografia si resulta no conforme",
                    tint = TextTertiary,
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        Text(
            text = point.item.title,
            style = MaterialTheme.typography.titleMedium,
            color = if (point.applicable) MaterialTheme.colorScheme.onSurface else TextTertiary,
            modifier = Modifier.padding(top = Spacing.m)
        )
        Text(
            text = point.item.criterion,
            style = MaterialTheme.typography.bodyMedium,
            color = if (point.applicable) TextSecondary else TextTertiary,
            modifier = Modifier.padding(top = Spacing.xs)
        )

        if (!point.applicable) {
            Row(
                modifier = Modifier.padding(top = Spacing.m),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Spacing.s)
            ) {
                Icon(
                    imageVector = Icons.Filled.Block,
                    contentDescription = null,
                    tint = TextTertiary,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = "No aplica en esta zona de trabajo",
                    style = MaterialTheme.typography.labelMedium,
                    color = TextTertiary
                )
            }
            return@Column
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = Spacing.l),
            horizontalArrangement = Arrangement.spacedBy(Spacing.s)
        ) {
            ChoiceButton(
                label = "Conforme",
                icon = Icons.Filled.Check,
                isSelected = point.result == AnswerResult.CONFORME,
                accent = SafeGreen,
                container = SafeContainer,
                onClick = { onResult(AnswerResult.CONFORME) },
                modifier = Modifier.weight(1f)
            )
            ChoiceButton(
                label = "No conforme",
                icon = Icons.Filled.Close,
                isSelected = point.result == AnswerResult.NO_CONFORME,
                accent = CriticalRed,
                container = CriticalContainer,
                onClick = { onResult(AnswerResult.NO_CONFORME) },
                modifier = Modifier.weight(1f)
            )
        }

        AnimatedVisibility(visible = point.result == AnswerResult.NO_CONFORME) {
            Column(modifier = Modifier.padding(top = Spacing.l)) {
                if (point.item.stopsWork) {
                    AlertBanner(
                        icon = Icons.Filled.Block,
                        title = "Detencion de labores",
                        detail = "Este hallazgo obliga a suspender la actividad en la zona " +
                            "hasta su mitigacion. ${point.normative.code}.",
                        accent = CriticalRed,
                        container = CriticalContainer
                    )
                }

                OutlinedTextField(
                    value = point.note,
                    onValueChange = onNoteChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = Spacing.m),
                    label = { Text("Nota de evidencia (obligatoria)") },
                    minLines = 2,
                    isError = showEvidenceError && point.needsNote,
                    supportingText = {
                        Text(
                            text = if (showEvidenceError && point.needsNote) {
                                "Describe el hallazgo con al menos " +
                                    "${ChecklistPoint.MIN_NOTE_LENGTH} caracteres."
                            } else {
                                "${point.note.trim().length}/${ChecklistPoint.MIN_NOTE_LENGTH} " +
                                    "caracteres minimos"
                            }
                        )
                    }
                )

                if (point.item.requiresPhoto) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = Spacing.m),
                        horizontalArrangement = Arrangement.spacedBy(Spacing.m),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (point.photoUri != null) {
                            EvidenceThumbnail(uri = point.photoUri)
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            TextButton(
                                onClick = onCapturePhoto,
                                modifier = Modifier.heightIn(min = Spacing.minTouchTarget)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.PhotoCamera,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = if (point.photoUri == null) {
                                        "Tomar fotografia"
                                    } else {
                                        "Repetir fotografia"
                                    },
                                    modifier = Modifier.padding(start = Spacing.s)
                                )
                            }
                            if (showEvidenceError && point.needsPhoto) {
                                Text(
                                    text = "La norma exige evidencia fotografica de este hallazgo.",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = CriticalRed
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ChoiceButton(
    label: String,
    icon: ImageVector,
    isSelected: Boolean,
    accent: Color,
    container: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(10.dp)
    Row(
        modifier = modifier
            .height(Spacing.minTouchTarget)
            .background(if (isSelected) container else Color.Transparent, shape)
            .border(1.dp, if (isSelected) accent else OutlineStrong, shape)
            .clickable(role = Role.RadioButton, onClick = onClick)
            .semantics { selected = isSelected }
            .padding(horizontal = Spacing.s),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (isSelected) accent else TextSecondary,
            modifier = Modifier.size(18.dp)
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = if (isSelected) accent else TextSecondary,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(start = 6.dp)
        )
    }
}

/** Referencia normativa del punto de control; abre el detalle de la norma. */
@Composable
fun NormativeChip(code: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .background(SurfaceHigh, RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .semantics { contentDescription = "Ver norma $code" }
            .padding(horizontal = Spacing.s, vertical = 6.dp)
    ) {
        Text(
            text = code,
            style = MaterialTheme.typography.labelSmall,
            color = InfoSteel
        )
    }
}
