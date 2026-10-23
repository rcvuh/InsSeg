package com.pascm.inspseg.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ReportProblem
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pascm.inspseg.domain.model.Inspection
import com.pascm.inspseg.domain.model.WorkZone
import com.pascm.inspseg.ui.components.CloudCard
import com.pascm.inspseg.ui.viewmodel.AppViewModelProvider
import com.pascm.inspseg.ui.viewmodel.HomeViewModel
import com.pascm.inspseg.ui.components.SectionHeader
import com.pascm.inspseg.ui.components.AlertBanner
import com.pascm.inspseg.ui.components.StatusBadge
import com.pascm.inspseg.ui.components.visual
import com.pascm.inspseg.ui.theme.CriticalContainer
import com.pascm.inspseg.ui.theme.CriticalRed
import com.pascm.inspseg.ui.theme.OutlineStrong
import com.pascm.inspseg.ui.theme.SafeGreen
import com.pascm.inspseg.ui.theme.OutlineSubtle
import com.pascm.inspseg.ui.theme.Spacing
import com.pascm.inspseg.ui.theme.SurfaceBase
import com.pascm.inspseg.ui.theme.TextSecondary
import com.pascm.inspseg.ui.theme.TextTertiary
import com.pascm.inspseg.ui.theme.WarningAmber
import com.pascm.inspseg.util.asDateTime
import com.pascm.inspseg.util.deadlineLabel
import kotlin.math.roundToInt

/**
 * Tablero del inspector: estado de los hallazgos abiertos, reinspecciones
 * vencidas y arranque de un nuevo recorrido por zona.
 */
@Composable
fun HomeScreen(
    onStartInspection: (Long, String) -> Unit,
    onOpenIncidences: () -> Unit,
    viewModel: HomeViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val state by viewModel.state.collectAsState()
    val inspector by viewModel.inspector.collectAsState()
    val cloud by viewModel.cloud.collectAsState()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = Spacing.screen,
            end = Spacing.screen,
            top = Spacing.l,
            bottom = Spacing.xxl
        ),
        verticalArrangement = Arrangement.spacedBy(Spacing.l)
    ) {
        item {
            Column {
                Text(
                    text = "Inspeccion y seguridad",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.semantics { heading() }
                )
                Text(
                    text = "Recorrido diario de verificacion operacional",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary,
                    modifier = Modifier.padding(top = Spacing.xs)
                )
            }
        }

        item {
            CloudCard(
                state = cloud,
                onSync = viewModel::syncNow,
                onSignOut = viewModel::signOut
            )
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(Spacing.m)) {
                KpiTile(
                    value = state.openIncidences.toString(),
                    label = "Hallazgos abiertos",
                    modifier = Modifier.weight(1f),
                    onClick = onOpenIncidences
                )
                KpiTile(
                    value = state.openCriticalIncidences.toString(),
                    label = "Detienen labores",
                    accent = if (state.openCriticalIncidences > 0) CriticalRed else null,
                    modifier = Modifier.weight(1f),
                    onClick = onOpenIncidences
                )
                KpiTile(
                    value = state.pendingReinspections.size.toString(),
                    label = "Reinspecciones",
                    accent = if (state.pendingReinspections.isNotEmpty()) WarningAmber else null,
                    modifier = Modifier.weight(1f),
                    onClick = onOpenIncidences
                )
            }
        }

        if (state.openCriticalIncidences > 0) {
            item {
                AlertBanner(
                    icon = Icons.Filled.ReportProblem,
                    title = "Hay labores detenidas",
                    detail = "${state.openCriticalIncidences} hallazgo(s) critico(s) sin cerrar. " +
                        "Verifica su mitigacion antes de autorizar la reanudacion.",
                    accent = CriticalRed,
                    container = CriticalContainer,
                    modifier = Modifier.clickable(onClick = onOpenIncidences)
                )
            }
        }

        state.pendingReinspections.firstOrNull()?.let { pending ->
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(SurfaceBase, RoundedCornerShape(14.dp))
                        .border(1.dp, OutlineSubtle, RoundedCornerShape(14.dp))
                        .padding(Spacing.l),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(Spacing.m)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Schedule,
                        contentDescription = null,
                        tint = WarningAmber,
                        modifier = Modifier.size(20.dp)
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Reinspeccion programada: ${pending.zoneName}",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = pending.reinspectionDueAt?.let { deadlineLabel(it) }.orEmpty(),
                            style = MaterialTheme.typography.labelMedium,
                            color = WarningAmber
                        )
                    }
                }
            }
        }

        item {
            OutlinedTextField(
                value = inspector,
                onValueChange = viewModel::setInspector,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Inspector responsable") },
                leadingIcon = { Icon(Icons.Filled.Person, contentDescription = null) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Words,
                    imeAction = ImeAction.Done
                ),
                supportingText = {
                    Text("Queda registrado en cada hallazgo del recorrido.")
                }
            )
        }

        item {
            SectionHeader(
                title = "Iniciar recorrido",
                trailing = "${state.zones.size} zonas"
            )
        }

        items(state.zones, key = { "zona-${it.id}" }) { zone ->
            ZoneCard(
                zone = zone,
                enabled = inspector.isNotBlank(),
                onClick = { onStartInspection(zone.id, inspector.trim()) }
            )
        }

        if (state.recentInspections.isNotEmpty()) {
            item { SectionHeader(title = "Recorridos recientes") }
            items(state.recentInspections, key = { "recorrido-${it.id}" }) { inspection ->
                InspectionRow(inspection)
            }
        }
    }
}

@Composable
private fun KpiTile(
    value: String,
    label: String,
    modifier: Modifier = Modifier,
    accent: androidx.compose.ui.graphics.Color? = null,
    onClick: () -> Unit
) {
    Column(
        modifier = modifier
            .background(SurfaceBase, RoundedCornerShape(14.dp))
            .border(1.dp, accent?.copy(alpha = 0.5f) ?: OutlineSubtle, RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .heightIn(min = 92.dp)
            .padding(Spacing.m)
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.headlineSmall,
            color = accent ?: MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = TextSecondary,
            modifier = Modifier.padding(top = Spacing.xs)
        )
    }
}

@Composable
private fun ZoneCard(zone: WorkZone, enabled: Boolean, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(SurfaceBase, RoundedCornerShape(14.dp))
            .border(1.dp, if (enabled) OutlineStrong else OutlineSubtle, RoundedCornerShape(14.dp))
            .clickable(enabled = enabled, onClick = onClick)
            .padding(Spacing.l)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = zone.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = if (enabled) MaterialTheme.colorScheme.onSurface else TextTertiary
                )
                Text(
                    text = zone.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (enabled) TextSecondary else TextTertiary,
                    modifier = Modifier.padding(top = Spacing.xs)
                )
            }
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                tint = if (enabled) MaterialTheme.colorScheme.onSurface else TextTertiary
            )
        }

        Row(
            modifier = Modifier.padding(top = Spacing.m),
            horizontalArrangement = Arrangement.spacedBy(Spacing.s)
        ) {
            if (zone.requiresEyeProtection) RequirementChip("Ocular")
            if (zone.requiresHearingProtection) RequirementChip("Auditiva")
            if (zone.involvesHeightWork) RequirementChip("Altura")
        }

        if (!enabled) {
            Text(
                text = "Captura el nombre del inspector para iniciar.",
                style = MaterialTheme.typography.labelSmall,
                color = TextTertiary,
                modifier = Modifier.padding(top = Spacing.s)
            )
        }
    }
}

@Composable
private fun RequirementChip(label: String) {
    Text(
        text = label,
        style = MaterialTheme.typography.labelSmall,
        color = TextSecondary,
        modifier = Modifier
            .background(
                MaterialTheme.colorScheme.surfaceContainerHighest,
                RoundedCornerShape(6.dp)
            )
            .padding(horizontal = Spacing.s, vertical = Spacing.xs)
    )
}

@Composable
private fun InspectionRow(inspection: Inspection) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(SurfaceBase, RoundedCornerShape(14.dp))
            .border(1.dp, OutlineSubtle, RoundedCornerShape(14.dp))
            .padding(Spacing.l),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = inspection.zoneName,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "${inspection.startedAt.asDateTime()} · ${inspection.inspector}",
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary,
                modifier = Modifier.padding(top = Spacing.xs)
            )
            Text(
                text = "Cumplimiento ${inspection.scorePercent.roundToInt()} %",
                style = MaterialTheme.typography.labelMedium,
                color = TextSecondary,
                modifier = Modifier.padding(top = Spacing.xs)
            )
        }
        Icon(
            imageVector = if (inspection.isSynced) Icons.Filled.CloudDone else Icons.Filled.CloudUpload,
            contentDescription = if (inspection.isSynced) "Respaldado en Firestore" else "Pendiente de subir",
            tint = if (inspection.isSynced) SafeGreen else TextTertiary,
            modifier = Modifier.padding(end = Spacing.s).size(18.dp)
        )
        StatusBadge(visual = inspection.status.visual(), compact = true)
    }
}
