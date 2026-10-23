package com.pascm.inspseg.data.mapper

import com.pascm.inspseg.data.local.entity.AnswerEntity
import com.pascm.inspseg.data.local.entity.ChecklistItemEntity
import com.pascm.inspseg.data.local.entity.IncidenceEntity
import com.pascm.inspseg.data.local.entity.InspectionEntity
import com.pascm.inspseg.data.local.entity.NormativeEntity
import com.pascm.inspseg.data.local.entity.WorkZoneEntity
import com.pascm.inspseg.domain.model.Answer
import com.pascm.inspseg.domain.model.ChecklistItem
import com.pascm.inspseg.domain.model.Incidence
import com.pascm.inspseg.domain.model.Inspection
import com.pascm.inspseg.domain.model.Normative
import com.pascm.inspseg.domain.model.WorkZone

/*
 * Mappers entre entidades de Room (data/local/entity) y modelos de dominio
 * (domain/model). Son funciones de extension puras: el repositorio las aplica al
 * leer (toDomain) y al escribir (toEntity), y asi ninguna entidad sale de la capa
 * de datos.
 */

// ---------- Catalogo ----------

fun NormativeEntity.toDomain() = Normative(
    code = code,
    title = title,
    requirement = requirement,
    internationalEquivalent = internationalEquivalent
)

fun Normative.toEntity() = NormativeEntity(
    code = code,
    title = title,
    requirement = requirement,
    internationalEquivalent = internationalEquivalent
)

fun WorkZoneEntity.toDomain() = WorkZone(
    id = id,
    name = name,
    description = description,
    requiresEyeProtection = requiresEyeProtection,
    requiresHearingProtection = requiresHearingProtection,
    involvesHeightWork = involvesHeightWork
)

fun WorkZone.toEntity() = WorkZoneEntity(
    id = id,
    name = name,
    description = description,
    requiresEyeProtection = requiresEyeProtection,
    requiresHearingProtection = requiresHearingProtection,
    involvesHeightWork = involvesHeightWork
)

fun ChecklistItemEntity.toDomain() = ChecklistItem(
    id = id,
    module = module,
    title = title,
    criterion = criterion,
    normativeCode = normativeCode,
    criticality = criticality,
    requiresPhoto = requiresPhoto,
    stopsWork = stopsWork,
    zoneRequirement = zoneRequirement,
    orderIndex = orderIndex
)

fun ChecklistItem.toEntity() = ChecklistItemEntity(
    id = id,
    module = module,
    title = title,
    criterion = criterion,
    normativeCode = normativeCode,
    criticality = criticality,
    requiresPhoto = requiresPhoto,
    stopsWork = stopsWork,
    zoneRequirement = zoneRequirement,
    orderIndex = orderIndex
)

// ---------- Recorridos ----------

fun InspectionEntity.toDomain() = Inspection(
    id = id,
    zoneId = zoneId,
    zoneName = zoneName,
    inspector = inspector,
    startedAt = startedAt,
    finishedAt = finishedAt,
    scorePercent = scorePercent,
    status = status,
    criticalStop = criticalStop,
    reinspectionDueAt = reinspectionDueAt,
    latitude = latitude,
    longitude = longitude,
    ownerUid = ownerUid,
    syncedAt = syncedAt
)

fun Inspection.toEntity() = InspectionEntity(
    id = id,
    zoneId = zoneId,
    zoneName = zoneName,
    inspector = inspector,
    startedAt = startedAt,
    finishedAt = finishedAt,
    scorePercent = scorePercent,
    status = status,
    criticalStop = criticalStop,
    reinspectionDueAt = reinspectionDueAt,
    latitude = latitude,
    longitude = longitude,
    ownerUid = ownerUid,
    syncedAt = syncedAt
)

fun AnswerEntity.toDomain() = Answer(
    inspectionId = inspectionId,
    itemId = itemId,
    result = result,
    note = note,
    photoUri = photoUri,
    answeredAt = answeredAt
)

fun Answer.toEntity() = AnswerEntity(
    inspectionId = inspectionId,
    itemId = itemId,
    result = result,
    note = note,
    photoUri = photoUri,
    answeredAt = answeredAt
)

// ---------- Hallazgos ----------

fun IncidenceEntity.toDomain() = Incidence(
    id = id,
    inspectionId = inspectionId,
    itemId = itemId,
    itemTitle = itemTitle,
    zoneName = zoneName,
    normativeCode = normativeCode,
    criticality = criticality,
    stopsWork = stopsWork,
    note = note,
    photoUri = photoUri,
    status = status,
    createdAt = createdAt,
    updatedAt = updatedAt,
    latitude = latitude,
    longitude = longitude,
    ownerUid = ownerUid,
    syncedAt = syncedAt
)

fun Incidence.toEntity() = IncidenceEntity(
    id = id,
    inspectionId = inspectionId,
    itemId = itemId,
    itemTitle = itemTitle,
    zoneName = zoneName,
    normativeCode = normativeCode,
    criticality = criticality,
    stopsWork = stopsWork,
    note = note,
    photoUri = photoUri,
    status = status,
    createdAt = createdAt,
    updatedAt = updatedAt,
    latitude = latitude,
    longitude = longitude,
    ownerUid = ownerUid,
    syncedAt = syncedAt
)

/** Atajo para listas que vienen de los Flow de Room. */
@JvmName("inspectionsToDomain")
fun List<InspectionEntity>.toDomain(): List<Inspection> = map { it.toDomain() }

@JvmName("incidencesToDomain")
fun List<IncidenceEntity>.toDomain(): List<Incidence> = map { it.toDomain() }
