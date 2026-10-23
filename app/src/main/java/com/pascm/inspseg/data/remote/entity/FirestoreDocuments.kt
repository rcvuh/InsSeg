package com.pascm.inspseg.data.remote.entity

/*
 * Entidades de Firebase (documentos de Cloud Firestore).
 *
 * Son POJOs (var + valores por defecto) porque Firestore necesita un constructor
 * vacio para toObject(). Los campos usan nombres en español y tipos simples
 * (String, Long, Double, listas y mapas) que Firestore serializa sin anotaciones.
 * Los enums viajan como texto. Ninguna clase de la UI o del dominio depende de
 * estas: la conversion se hace solo en data/mapper/RemoteMappers.kt.
 *
 * Estructura en Firestore:
 *   inspectores/{uid}                      -> InspectorDocument
 *   inspectores/{uid}/recorridos/{id}      -> InspectionDocument (respuestas embebidas)
 *   inspectores/{uid}/hallazgos/{id}       -> IncidenceDocument
 */

data class InspectorDocument(
    var uid: String = "",
    var nombre: String = "",
    var correo: String = "",
    var rol: String = "INSPECTOR",
    var creadoEn: Long = 0L
)

data class AnswerDocument(
    var puntoId: Long = 0L,
    var resultado: String = "SIN_EVALUAR",
    var nota: String = "",
    var fotoUrl: String? = null,
    var respondidoEn: Long? = null
)

data class InspectionDocument(
    var idLocal: Long = 0L,
    var zonaId: Long = 0L,
    var zona: String = "",
    var inspector: String = "",
    var inspectorUid: String = "",
    var inicio: Long = 0L,
    var cierre: Long? = null,
    var cumplimiento: Double = 0.0,
    var estado: String = "EN_CAPTURA",
    var paroCritico: Boolean = false,
    var reinspeccionAntesDe: Long? = null,
    var latitud: Double? = null,
    var longitud: Double? = null,
    var respuestas: List<AnswerDocument> = emptyList(),
    var actualizadoEn: Long = 0L
)

data class IncidenceDocument(
    var idLocal: Long = 0L,
    var recorridoId: Long = 0L,
    var puntoId: Long = 0L,
    var punto: String = "",
    var zona: String = "",
    var norma: String = "",
    var criticidad: String = "MEDIO",
    var detieneLabores: Boolean = false,
    var nota: String = "",
    var fotoUrl: String? = null,
    var estado: String = "ABIERTO",
    var creadoEn: Long = 0L,
    var actualizadoEn: Long = 0L,
    var latitud: Double? = null,
    var longitud: Double? = null,
    var inspectorUid: String = ""
)
