package com.pascm.inspseg.domain.model

/** Inspector autenticado. Se construye desde FirebaseUser mediante un mapper. */
data class Inspector(
    val uid: String,
    val email: String,
    val displayName: String
)

/** Resultado de una operacion de autenticacion, ya traducido para la UI. */
sealed interface AuthResult {
    data object Success : AuthResult
    data class Failure(val message: String) : AuthResult
}

/** Estado de la copia en la nube que se muestra en el tablero. */
data class SyncSummary(
    val pendingInspections: Int = 0,
    val pendingIncidences: Int = 0,
    val cloudInspections: Int? = null
) {
    val pendingTotal: Int get() = pendingInspections + pendingIncidences
}

/** Resultado de un intento de sincronizacion. */
data class SyncReport(
    val uploadedInspections: Int,
    val uploadedIncidences: Int,
    val uploadedPhotos: Int,
    val error: String? = null
)
