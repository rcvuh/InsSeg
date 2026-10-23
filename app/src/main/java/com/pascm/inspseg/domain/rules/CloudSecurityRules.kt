package com.pascm.inspseg.domain.rules

import com.pascm.inspseg.domain.model.Incidence
import com.pascm.inspseg.domain.model.Inspection
import com.pascm.inspseg.domain.model.InspectionStatus

/**
 * Reglas de seguridad para subir informacion a la nube. Son las mismas que
 * repiten las reglas de Firestore y Storage del servidor (carpeta firebase/),
 * pero se evaluan primero aqui para no intentar escrituras que serian rechazadas.
 */
object CloudSecurityRules {

    /** Limite de tamaño de una fotografia de evidencia (igual que storage.rules). */
    const val MAX_PHOTO_BYTES = 5L * 1024 * 1024

    /** Raiz de los datos de un inspector en Firestore: inspectores/{uid}. */
    fun inspectorPath(uid: String) = "inspectores/$uid"

    fun inspectionPath(uid: String, inspectionId: Long) = "${inspectorPath(uid)}/recorridos/$inspectionId"

    fun incidencePath(uid: String, incidenceId: Long) = "${inspectorPath(uid)}/hallazgos/$incidenceId"

    /** Ruta de la evidencia en Cloud Storage: evidencias/{uid}/{recorrido}/{punto}.jpg */
    fun evidencePath(uid: String, inspectionId: Long, itemId: Long) =
        "evidencias/$uid/$inspectionId/punto_$itemId.jpg"

    /**
     * Un recorrido solo se sube si esta cerrado y pertenece al inspector con
     * sesion. Asi un inspector nunca escribe en la carpeta de otro.
     */
    fun canUpload(inspection: Inspection, sessionUid: String?): Boolean =
        sessionUid != null &&
            inspection.ownerUid == sessionUid &&
            inspection.finishedAt != null &&
            inspection.status != InspectionStatus.EN_CAPTURA

    fun canUpload(incidence: Incidence, sessionUid: String?): Boolean =
        sessionUid != null && incidence.ownerUid == sessionUid

    fun photoSizeAllowed(bytes: Long): Boolean = bytes in 1..MAX_PHOTO_BYTES
}
