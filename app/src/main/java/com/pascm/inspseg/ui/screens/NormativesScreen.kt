package com.pascm.inspseg.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pascm.inspseg.ui.theme.InfoSteel
import com.pascm.inspseg.ui.viewmodel.AppViewModelProvider
import com.pascm.inspseg.ui.viewmodel.HomeViewModel
import com.pascm.inspseg.ui.theme.OutlineSubtle
import com.pascm.inspseg.ui.theme.Spacing
import com.pascm.inspseg.ui.theme.SurfaceBase
import com.pascm.inspseg.ui.theme.TextSecondary
import com.pascm.inspseg.ui.theme.TextTertiary

/**
 * Catalogo normativo. Se presenta como consulta de respaldo: el uso cotidiano de
 * la norma ocurre dentro del formulario, no aqui.
 */
@Composable
fun NormativesScreen(
    viewModel: HomeViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val normatives by viewModel.normatives.collectAsState()

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
                    text = "Normas aplicables",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.semantics { heading() }
                )
                Text(
                    text = "Cada punto de control del recorrido cita una de estas normas.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary,
                    modifier = Modifier.padding(top = Spacing.xs)
                )
            }
        }

        items(normatives, key = { it.code }) { normative ->
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SurfaceBase, RoundedCornerShape(14.dp))
                    .border(1.dp, OutlineSubtle, RoundedCornerShape(14.dp))
                    .padding(Spacing.l)
            ) {
                Text(
                    text = normative.code,
                    style = MaterialTheme.typography.labelLarge,
                    color = InfoSteel
                )
                Text(
                    text = normative.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(top = Spacing.xs)
                )
                Text(
                    text = normative.requirement,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary,
                    modifier = Modifier.padding(top = Spacing.s)
                )
                Text(
                    text = normative.internationalEquivalent,
                    style = MaterialTheme.typography.labelSmall,
                    color = TextTertiary,
                    modifier = Modifier.padding(top = Spacing.s)
                )
            }
        }
    }
}
