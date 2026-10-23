package com.pascm.inspseg.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pascm.inspseg.domain.model.Incidence
import com.pascm.inspseg.ui.viewmodel.AppViewModelProvider
import com.pascm.inspseg.ui.viewmodel.IncidenceViewModel
import com.pascm.inspseg.domain.model.IncidenceStatus
import com.pascm.inspseg.ui.components.EvidenceThumbnail
import com.pascm.inspseg.ui.components.StatusBadge
import com.pascm.inspseg.ui.components.visual
import com.pascm.inspseg.ui.theme.CriticalRed
import com.pascm.inspseg.ui.theme.OutlineSubtle
import com.pascm.inspseg.ui.theme.Spacing
import com.pascm.inspseg.ui.theme.SurfaceBase
import com.pascm.inspseg.ui.theme.SurfaceHigh
import com.pascm.inspseg.ui.theme.TextPrimary
import com.pascm.inspseg.ui.theme.TextSecondary
import com.pascm.inspseg.ui.theme.TextTertiary
import com.pascm.inspseg.util.asDateTime

/** Seguimiento de hallazgos: de "Abierto" a "Cerrado" sin salir del dispositivo. */
@Composable
fun IncidencesScreen(
    viewModel: IncidenceViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val state by viewModel.state.collectAsState()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = Spacing.screen,
            end = Spacing.screen,
            top = Spacing.l,
            bottom = Spacing.xxl
        ),
        verticalArrangement = Arrangement.spacedBy(Spacing.m)
    ) {
        item {
            Column {
                Text(
                    text = "Hallazgos",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.semantics { heading() }
                )
                Text(
                    text = "Desviaciones detectadas en campo y su mitigacion",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary,
                    modifier = Modifier.padding(top = Spacing.xs)
                )
            }
        }

        item {
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(Spacing.s)
            ) {
                StatusFilterChip("Todos", state.filter == null) { viewModel.setFilter(null) }
                IncidenceStatus.entries.forEach { status ->
                    StatusFilterChip(status.label, state.filter == status) {
                        viewModel.setFilter(status)
                    }
                }
            }
        }

        if (state.incidences.isEmpty()) {
            item {
                Text(
                    text = "No hay hallazgos con este filtro.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextTertiary,
                    modifier = Modifier.padding(top = Spacing.l)
                )
            }
        }

        items(state.incidences, key = { it.id }) { incidence ->
            IncidenceCard(
                incidence = incidence,
                onAdvance = { viewModel.advance(incidence.id, it) }
            )
        }
    }
}

@Composable
private fun StatusFilterChip(label: String, selected: Boolean, onClick: () -> Unit) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(label) },
        modifier = Modifier.height(Spacing.minTouchTarget),
        colors = FilterChipDefaults.filterChipColors(
            containerColor = SurfaceBase,
            labelColor = TextSecondary,
            selectedContainerColor = SurfaceHigh,
            selectedLabelColor = TextPrimary
        )
    )
}

@Composable
private fun IncidenceCard(incidence: Incidence, onAdvance: (IncidenceStatus) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(SurfaceBase, RoundedCornerShape(14.dp))
            .border(
                1.dp,
                if (incidence.stopsWork && incidence.status != IncidenceStatus.CERRADO) {
                    CriticalRed.copy(alpha = 0.6f)
                } else {
                    OutlineSubtle
                },
                RoundedCornerShape(14.dp)
            )
            .padding(Spacing.l)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(Spacing.s),
            verticalAlignment = Alignment.CenterVertically
        ) {
            StatusBadge(visual = incidence.status.visual(), compact = true)
            StatusBadge(visual = incidence.criticality.visual(), compact = true)
        }

        Text(
            text = incidence.itemTitle,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(top = Spacing.m)
        )
        Text(
            text = "${incidence.zoneName} · ${incidence.normativeCode}",
            style = MaterialTheme.typography.labelSmall,
            color = TextSecondary,
            modifier = Modifier.padding(top = Spacing.xs)
        )

        Row(
            modifier = Modifier.padding(top = Spacing.m),
            horizontalArrangement = Arrangement.spacedBy(Spacing.m)
        ) {
            if (incidence.photoUri != null) {
                EvidenceThumbnail(uri = incidence.photoUri, size = 64.dp)
            }
            Text(
                text = incidence.note,
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
                modifier = Modifier.weight(1f)
            )
        }

        Row(
            modifier = Modifier.padding(top = Spacing.m),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Filled.Place,
                contentDescription = null,
                tint = TextTertiary,
                modifier = Modifier.size(14.dp)
            )
            Text(
                text = buildString {
                    append(incidence.createdAt.asDateTime())
                    val lat = incidence.latitude
                    val lon = incidence.longitude
                    if (lat != null && lon != null) {
                        append(" · ")
                        append(String.format(java.util.Locale.US, "%.5f, %.5f", lat, lon))
                    } else {
                        append(" · sin coordenadas")
                    }
                },
                style = MaterialTheme.typography.labelSmall,
                color = TextTertiary,
                modifier = Modifier.padding(start = Spacing.xs)
            )
        }

        val next = when (incidence.status) {
            IncidenceStatus.ABIERTO -> IncidenceStatus.EN_MITIGACION
            IncidenceStatus.EN_MITIGACION -> IncidenceStatus.CERRADO
            IncidenceStatus.CERRADO -> null
        }
        if (next != null) {
            OutlinedButton(
                onClick = { onAdvance(next) },
                modifier = Modifier
                    .padding(top = Spacing.m)
                    .fillMaxWidth()
                    .height(Spacing.minTouchTarget)
            ) {
                Text(
                    when (next) {
                        IncidenceStatus.EN_MITIGACION -> "Marcar en mitigacion"
                        else -> "Cerrar hallazgo"
                    }
                )
            }
        }
    }
}
