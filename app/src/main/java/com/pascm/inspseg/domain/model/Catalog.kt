package com.pascm.inspseg.domain.model

/*
 * Modelos de dominio del catalogo normativo. Son clases Kotlin puras: no saben
 * nada de Room ni de Firebase. La UI y las reglas trabajan solo con ellas; las
 * capas de datos las obtienen a traves de los Mappers (data/mapper).
 */

/** Norma oficial que respalda un punto de control. */
data class Normative(
    val code: String,
    val title: String,
    val requirement: String,
    val internationalEquivalent: String
)

/** Zona de trabajo; sus banderas deciden que puntos de control aplican. */
data class WorkZone(
    val id: Long,
    val name: String,
    val description: String,
    val requiresEyeProtection: Boolean,
    val requiresHearingProtection: Boolean,
    val involvesHeightWork: Boolean
)

/** Definicion de un punto de control del formulario. */
data class ChecklistItem(
    val id: Long,
    val module: InspectionModule,
    val title: String,
    val criterion: String,
    val normativeCode: String,
    val criticality: CriticalityLevel,
    val requiresPhoto: Boolean,
    val stopsWork: Boolean,
    val zoneRequirement: ZoneRequirement,
    val orderIndex: Int
)
