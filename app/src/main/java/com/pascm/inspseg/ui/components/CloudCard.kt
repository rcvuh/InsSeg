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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.pascm.inspseg.ui.theme.InfoSteel
import com.pascm.inspseg.ui.theme.OutlineSubtle
import com.pascm.inspseg.ui.theme.SafeGreen
import com.pascm.inspseg.ui.theme.Spacing
import com.pascm.inspseg.ui.theme.SurfaceBase
import com.pascm.inspseg.ui.theme.TextPrimary
import com.pascm.inspseg.ui.theme.TextSecondary
import com.pascm.inspseg.ui.theme.WarningAmber
import com.pascm.inspseg.ui.viewmodel.CloudUiState

/**
 * Respaldo en la nube: quien tiene la sesion, cuanto falta por subir y cuantos
 * recorridos hay ya en Firestore (conteo en tiempo real).
 */
@Composable
fun CloudCard(
    state: CloudUiState,
    onSync: () -> Unit,
    onSignOut: () -> Unit,
    modifier: Modifier = Modifier
) {
    val inspector = state.inspector
    val pending = state.summary.pendingTotal
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(SurfaceBase, RoundedCornerShape(14.dp))
            .border(1.dp, OutlineSubtle, RoundedCornerShape(14.dp))
            .padding(Spacing.l),
        verticalArrangement = Arrangement.spacedBy(Spacing.s)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = if (inspector == null) Icons.Filled.CloudOff else Icons.Filled.Cloud,
                contentDescription = null,
                tint = if (inspector == null) TextSecondary else InfoSteel,
                modifier = Modifier.size(20.dp)
            )
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = Spacing.m)
            ) {
                Text(
                    text = if (inspector == null) "Modo local (sin respaldo en la nube)" else "Respaldo en Firebase",
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary
                )
                Text(
                    text = inspector?.let { "${it.displayName} · ${it.email}" }
                        ?: "Inicia sesion para guardar tus recorridos en Firestore.",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary
                )
            }
            if (inspector != null) {
                IconButton(onClick = onSignOut) {
                    Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = "Cerrar sesion", tint = TextSecondary)
                }
            }
        }
        if (inspector != null) {
            Text(
                text = buildString {
                    append(
                        if (pending == 0) "Todo respaldado" else "$pending registro(s) pendientes de subir"
                    )
                    state.summary.cloudInspections?.let { append(" · $it recorrido(s) en Firestore") }
                },
                style = MaterialTheme.typography.labelMedium,
                color = if (pending == 0) SafeGreen else WarningAmber
            )
            state.lastMessage?.let {
                Text(text = it, style = MaterialTheme.typography.labelSmall, color = TextSecondary)
            }
            OutlinedButton(onClick = onSync, enabled = !state.syncing) {
                if (state.syncing) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                } else {
                    Icon(Icons.Filled.Sync, contentDescription = null, modifier = Modifier.size(18.dp))
                }
                Text("Sincronizar ahora", modifier = Modifier.padding(start = Spacing.s))
            }
        }
    }
}
