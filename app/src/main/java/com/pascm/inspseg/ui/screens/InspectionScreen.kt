package com.pascm.inspseg.ui.screens

import android.Manifest
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pascm.inspseg.domain.model.ChecklistPoint
import com.pascm.inspseg.domain.model.InspectionModule
import com.pascm.inspseg.domain.model.InspectionStatus
import com.pascm.inspseg.domain.rules.SafetyRuleEngine
import com.pascm.inspseg.ui.components.AlertBanner
import com.pascm.inspseg.ui.components.CaptureProgress
import com.pascm.inspseg.ui.components.ChecklistItemCard
import com.pascm.inspseg.ui.components.ComplianceMeter
import com.pascm.inspseg.ui.components.StatusBadge
import com.pascm.inspseg.ui.components.ThinDivider
import com.pascm.inspseg.ui.components.visual
import com.pascm.inspseg.ui.theme.CriticalContainer
import com.pascm.inspseg.ui.theme.CriticalRed
import com.pascm.inspseg.ui.theme.OutlineSubtle
import com.pascm.inspseg.ui.theme.Spacing
import com.pascm.inspseg.ui.theme.SurfaceBase
import com.pascm.inspseg.ui.theme.SurfaceRaised
import com.pascm.inspseg.ui.theme.TextSecondary
import com.pascm.inspseg.ui.theme.TextTertiary
import com.pascm.inspseg.ui.theme.WarningAmber
import com.pascm.inspseg.util.EvidenceFiles
import com.pascm.inspseg.util.LocationProvider
import com.pascm.inspseg.ui.viewmodel.AppViewModelProvider
import com.pascm.inspseg.ui.viewmodel.InspectionUiState
import com.pascm.inspseg.ui.viewmodel.InspectionViewModel
import kotlin.math.roundToInt

/**
 * Captura del recorrido. Es la pantalla donde las normas se vuelven flujo:
 * el motor de reglas recalcula puntaje, paro critico y evidencia pendiente en
 * cada toque, y esta vista solo refleja ese estado.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InspectionScreen(
    zoneId: Long,
    inspector: String,
    onFinished: () -> Unit,
    onBack: () -> Unit,
    viewModel: InspectionViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val context = LocalContext.current
    val state by viewModel.state.collectAsState()
    val evaluation = state.evaluation

    var selectedModule by remember { mutableStateOf(InspectionModule.EPP) }
    var showEvidenceErrors by remember { mutableStateOf(false) }
    var showConfirmSheet by remember { mutableStateOf(false) }
    var normativeSheetPoint by remember { mutableStateOf<ChecklistPoint?>(null) }

    var requestLocation by remember { mutableIntStateOf(0) }
    var pendingItemId by remember { mutableStateOf<Long?>(null) }
    var pendingUri by remember { mutableStateOf<Uri?>(null) }

    val takePicture = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        val itemId = pendingItemId
        val uri = pendingUri
        if (success && itemId != null && uri != null) viewModel.setPhoto(itemId, uri.toString())
        pendingItemId = null
        pendingUri = null
    }

    fun startCapture(itemId: Long) {
        val uri = EvidenceFiles.newEvidenceUri(context, itemId)
        pendingItemId = itemId
        pendingUri = uri
        takePicture.launch(uri)
    }

    val cameraPermission = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        val itemId = pendingItemId
        if (granted && itemId != null) startCapture(itemId) else pendingItemId = null
    }

    val locationPermission = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        if (result.values.any { it }) requestLocation = requestLocation + 1
    }

    LaunchedEffect(zoneId) { viewModel.load(zoneId, inspector) }

    // La ubicacion se resuelve aparte de la carga del formulario: puede tardar
    // hasta 8 s en llegar el primer fix y no debe bloquear la captura.
    LaunchedEffect(requestLocation) {
        if (LocationProvider.hasPermission(context)) {
            viewModel.setLocation(LocationProvider.current(context))
        } else {
            locationPermission.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    LaunchedEffect(state.savedInspectionId) {
        if (state.savedInspectionId != null) onFinished()
    }

    Column(modifier = Modifier.fillMaxSize()) {

        // --- Encabezado ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Spacing.s, vertical = Spacing.s),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack, modifier = Modifier.size(Spacing.minTouchTarget)) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Volver al tablero"
                )
            }
            Column(modifier = Modifier.padding(start = Spacing.s)) {
                Text(
                    text = state.zone?.name ?: "Cargando zona",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.semantics { heading() }
                )
                Text(
                    text = state.inspector.ifBlank { "Sin inspector" },
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary
                )
            }
        }

        // --- Panel de estado normativo ---
        Column(
            modifier = Modifier
                .padding(horizontal = Spacing.screen)
                .fillMaxWidth()
                .background(SurfaceBase, RoundedCornerShape(16.dp))
                .border(1.dp, OutlineSubtle, RoundedCornerShape(16.dp))
                .padding(Spacing.l)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                StatusBadge(visual = evaluation.status.visual())
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.Place,
                        contentDescription = null,
                        tint = if (state.location != null) TextSecondary else TextTertiary,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = state.location?.format() ?: "GPS no disponible",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (state.location != null) TextSecondary else TextTertiary,
                        modifier = Modifier.padding(start = Spacing.xs)
                    )
                }
            }

            ComplianceMeter(
                scorePercent = evaluation.scorePercent,
                evaluatedCount = evaluation.evaluatedCount,
                modifier = Modifier.padding(top = Spacing.l)
            )
            CaptureProgress(
                progress = evaluation.progress,
                answered = evaluation.answeredCount,
                total = evaluation.applicableCount,
                modifier = Modifier.padding(top = Spacing.m)
            )
        }

        if (evaluation.criticalStop) {
            AlertBanner(
                icon = Icons.Filled.Block,
                title = "Incidencia critica: detencion de labores",
                detail = "Se inhabilita el cierre como conforme. " +
                    "Adjunta fotografia y nota en: " +
                    evaluation.stopPointTitles.joinToString(", ") + ".",
                accent = CriticalRed,
                container = CriticalContainer,
                modifier = Modifier.padding(
                    start = Spacing.screen,
                    end = Spacing.screen,
                    top = Spacing.m
                )
            )
        }

        // --- Modulos ---
        TabRow(
            selectedTabIndex = selectedModule.ordinal,
            modifier = Modifier.padding(top = Spacing.m),
            containerColor = MaterialTheme.colorScheme.background,
            contentColor = MaterialTheme.colorScheme.onSurface
        ) {
            InspectionModule.entries.forEach { module ->
                val (answered, total) = state.moduleProgress(module)
                Tab(
                    selected = selectedModule == module,
                    onClick = { selectedModule = module },
                    modifier = Modifier.height(56.dp),
                    text = {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = module.shortLabel,
                                style = MaterialTheme.typography.labelLarge
                            )
                            Text(
                                text = "$answered/$total",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextSecondary
                            )
                        }
                    }
                )
            }
        }

        val visiblePoints = state.pointsOf(selectedModule)

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentPadding = PaddingValues(
                start = Spacing.screen,
                end = Spacing.screen,
                top = Spacing.l,
                bottom = Spacing.l
            ),
            verticalArrangement = Arrangement.spacedBy(Spacing.m)
        ) {
            item {
                Text(
                    text = selectedModule.label,
                    style = MaterialTheme.typography.labelMedium,
                    color = TextSecondary
                )
            }
            items(visiblePoints, key = { it.id }) { point ->
                ChecklistItemCard(
                    point = point,
                    showEvidenceError = showEvidenceErrors,
                    onResult = { viewModel.setResult(point.id, it) },
                    onNoteChange = { viewModel.setNote(point.id, it) },
                    onCapturePhoto = {
                        pendingItemId = point.id
                        cameraPermission.launch(Manifest.permission.CAMERA)
                    },
                    onNormativeClick = { normativeSheetPoint = point }
                )
            }
        }

        // --- Accion de cierre ---
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(SurfaceBase)
                .padding(Spacing.l)
        ) {
            ThinDivider(modifier = Modifier.padding(bottom = Spacing.m))
            Text(
                text = blockingReason(state, evaluation),
                style = MaterialTheme.typography.labelSmall,
                color = if (evaluation.canSave) TextSecondary else WarningAmber
            )
            Button(
                onClick = {
                    if (evaluation.canSave) {
                        showConfirmSheet = true
                    } else {
                        showEvidenceErrors = true
                    }
                },
                modifier = Modifier
                    .padding(top = Spacing.s)
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                Text("Revisar y cerrar recorrido")
            }
        }
    }

    if (showConfirmSheet) {
        ModalBottomSheet(
            onDismissRequest = { showConfirmSheet = false },
            sheetState = rememberModalBottomSheetState(),
            containerColor = SurfaceRaised
        ) {
            ConfirmSheetContent(
                state = state,
                evaluation = evaluation,
                onConfirm = {
                    showConfirmSheet = false
                    viewModel.save()
                },
                onCancel = { showConfirmSheet = false }
            )
        }
    }

    normativeSheetPoint?.let { point ->
        ModalBottomSheet(
            onDismissRequest = { normativeSheetPoint = null },
            sheetState = rememberModalBottomSheetState(),
            containerColor = SurfaceRaised
        ) {
            Column(
                modifier = Modifier
                    .padding(horizontal = Spacing.screen)
                    .padding(bottom = Spacing.xxl)
            ) {
                Text(
                    text = point.normative.code,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = point.normative.title,
                    style = MaterialTheme.typography.bodyLarge,
                    color = TextSecondary,
                    modifier = Modifier.padding(top = Spacing.xs)
                )
                Text(
                    text = point.normative.requirement,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(top = Spacing.l)
                )
                Text(
                    text = "Equivalente internacional: ${point.normative.internationalEquivalent}",
                    style = MaterialTheme.typography.labelMedium,
                    color = TextSecondary,
                    modifier = Modifier.padding(top = Spacing.m)
                )
                Text(
                    text = "Punto de control: ${point.item.title} · " +
                        "criticidad ${point.item.criticality.label.lowercase()} · " +
                        (if (point.item.requiresPhoto) "evidencia fotografica obligatoria"
                        else "sin evidencia fotografica obligatoria"),
                    style = MaterialTheme.typography.labelSmall,
                    color = TextTertiary,
                    modifier = Modifier.padding(top = Spacing.m)
                )
            }
        }
    }
}

/** Texto que explica por que no se puede cerrar todavia (o que pasara al cerrar). */
private fun blockingReason(
    state: InspectionUiState,
    evaluation: SafetyRuleEngine.Evaluation
): String = when {
    evaluation.pendingAnswers > 0 && evaluation.pendingEvidenceIds.isNotEmpty() ->
        "Faltan ${evaluation.pendingAnswers} puntos por evaluar y " +
            "${evaluation.pendingEvidenceIds.size} con evidencia incompleta."
    evaluation.pendingAnswers > 0 ->
        "Faltan ${evaluation.pendingAnswers} puntos por evaluar."
    evaluation.pendingEvidenceIds.isNotEmpty() ->
        "${evaluation.pendingEvidenceIds.size} hallazgo(s) requieren nota y fotografia " +
            "antes de guardar."
    evaluation.criticalStop ->
        "Se guardara como detencion de labores. No es posible marcarlo como conforme."
    evaluation.status == InspectionStatus.REINSPECCION_24H ->
        "Cumplimiento bajo el umbral: se programara reinspeccion en 24 h."
    else ->
        "Listo para cerrar. ${state.zone?.name.orEmpty()} sin desviaciones bloqueantes."
}

@Composable
private fun ConfirmSheetContent(
    state: InspectionUiState,
    evaluation: SafetyRuleEngine.Evaluation,
    onConfirm: () -> Unit,
    onCancel: () -> Unit
) {
    Column(
        modifier = Modifier
            .padding(horizontal = Spacing.screen)
            .padding(bottom = Spacing.xxl)
    ) {
        Text(
            text = "Cierre del recorrido",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
        StatusBadge(
            visual = evaluation.status.visual(),
            modifier = Modifier.padding(top = Spacing.m)
        )

        SummaryLine("Zona", state.zone?.name.orEmpty())
        SummaryLine("Inspector", state.inspector)
        SummaryLine("Cumplimiento ponderado", "${evaluation.scorePercent.roundToInt()} %")
        SummaryLine("Puntos evaluados", "${evaluation.evaluatedCount} de ${evaluation.applicableCount}")
        SummaryLine("Hallazgos a registrar", evaluation.nonConformCount.toString())
        SummaryLine("Coordenadas", state.location?.format() ?: "No disponibles")

        if (evaluation.reinspectionDueAt != null) {
            Row(
                modifier = Modifier.padding(top = Spacing.m),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Filled.Schedule,
                    contentDescription = null,
                    tint = WarningAmber,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = "Se programa reinspeccion dentro de 24 h.",
                    style = MaterialTheme.typography.labelMedium,
                    color = WarningAmber,
                    modifier = Modifier.padding(start = Spacing.s)
                )
            }
        }

        Button(
            onClick = onConfirm,
            enabled = !state.saving,
            modifier = Modifier
                .padding(top = Spacing.l)
                .fillMaxWidth()
                .height(52.dp)
        ) {
            Text(if (state.saving) "Guardando..." else "Guardar recorrido")
        }
        TextButton(
            onClick = onCancel,
            modifier = Modifier
                .fillMaxWidth()
                .height(Spacing.minTouchTarget)
        ) {
            Text("Seguir capturando")
        }
    }
}

@Composable
private fun SummaryLine(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = Spacing.m),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary
        )
        Box(modifier = Modifier.padding(start = Spacing.l)) {
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
