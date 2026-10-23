package com.pascm.inspseg.data.mapper

import com.pascm.inspseg.data.remote.entity.AnswerDocument
import com.pascm.inspseg.data.remote.entity.IncidenceDocument
import com.pascm.inspseg.data.remote.entity.InspectionDocument
import com.pascm.inspseg.data.remote.entity.InspectorDocument
import com.pascm.inspseg.domain.model.Answer
import com.pascm.inspseg.domain.model.AnswerResult
import com.pascm.inspseg.domain.model.CriticalityLevel
import com.pascm.inspseg.domain.model.Incidence
import com.pascm.inspseg.domain.model.IncidenceStatus
import com.pascm.inspseg.domain.model.Inspection
import com.pascm.inspseg.domain.model.InspectionStatus
import com.pascm.inspseg.domain.model.Inspector

/*
 * Mappers entre modelos de dominio y entidades de Firebase (documentos de
 * Firestore). Los enums viajan como texto y se leen con valores de respaldo
 * para que un documento editado a mano en la consola no tumbe la app.
 * [photoUrls] trae las URL de Cloud Storage: la ruta local (content://) del
 * telefono nunca se sube a la nube.
 */

fun Answer.toDocument(photoUrl: String?) = AnswerDocument(
    puntoId = itemId,
    resultado = result.name,
    nota = note,
    fotoUrl = photoUrl,
    respondidoEn = answeredAt
)

fun AnswerDocument.toDomain(inspectionId: Long) = Answer(
    inspectionId = inspectionId,
    itemId = puntoId,
    result = enumOr(resultado, AnswerResult.SIN_EVALUAR),
    note = nota,
    photoUri = fotoUrl,
    answeredAt = respondidoEn
)

fun Inspection.toDocument(
    ownerUid: String,
    answers: List<Answer>,
    photoUrls: Map<Long, String>,
    now: Long
) = InspectionDocument(
    idLocal = id,
    zonaId = zoneId,
    zona = zoneName,
    inspector = inspector,
    inspectorUid = ownerUid,
    inicio = startedAt,
    cierre = finishedAt,
    cumplimiento = scorePercent,
    estado = status.name,
    paroCritico = criticalStop,
    reinspeccionAntesDe = reinspectionDueAt,
    latitud = latitude,
    longitud = longitude,
    respuestas = answers.map { it.toDocument(photoUrls[it.itemId]) },
    actualizadoEn = now
)

fun InspectionDocument.toDomain() = Inspection(
    id = idLocal,
    zoneId = zonaId,
    zoneName = zona,
    inspector = inspector,
    startedAt = inicio,
    finishedAt = cierre,
    scorePercent = cumplimiento,
    status = enumOr(estado, InspectionStatus.EN_CAPTURA),
    criticalStop = paroCritico,
    reinspectionDueAt = reinspeccionAntesDe,
    latitude = latitud,
    longitude = longitud,
    ownerUid = inspectorUid,
    syncedAt = actualizadoEn
)

fun Incidence.toDocument(ownerUid: String, photoUrl: String?) = IncidenceDocument(
    idLocal = id,
    recorridoId = inspectionId,
    puntoId = itemId,
    punto = itemTitle,
    zona = zoneName,
    norma = normativeCode,
    criticidad = criticality.name,
    detieneLabores = stopsWork,
    nota = note,
    fotoUrl = photoUrl,
    estado = status.name,
    creadoEn = createdAt,
    actualizadoEn = updatedAt,
    latitud = latitude,
    longitud = longitude,
    inspectorUid = ownerUid
)

fun IncidenceDocument.toDomain() = Incidence(
    id = idLocal,
    inspectionId = recorridoId,
    itemId = puntoId,
    itemTitle = punto,
    zoneName = zona,
    normativeCode = norma,
    criticality = enumOr(criticidad, CriticalityLevel.MEDIO),
    stopsWork = detieneLabores,
    note = nota,
    photoUri = fotoUrl,
    status = enumOr(estado, IncidenceStatus.ABIERTO),
    createdAt = creadoEn,
    updatedAt = actualizadoEn,
    latitude = latitud,
    longitude = longitud,
    ownerUid = inspectorUid,
    syncedAt = actualizadoEn
)

fun Inspector.toDocument(now: Long) = InspectorDocument(
    uid = uid,
    nombre = displayName,
    correo = email,
    creadoEn = now
)

fun InspectorDocument.toDomain() = Inspector(uid = uid, email = correo, displayName = nombre)

/** Convierte texto a enum sin lanzar excepcion si el valor no existe. */
inline fun <reified T : Enum<T>> enumOr(value: String?, fallback: T): T =
    enumValues<T>().firstOrNull { it.name == value } ?: fallback
