package com.pascm.inspseg.domain.model

/** Los tres modulos criticos de inspeccion del recorrido diario. */
enum class InspectionModule(val label: String, val shortLabel: String) {
    EPP("Equipo de proteccion personal", "EPP"),
    MAQUINARIA("Maquinaria y herramientas criticas", "Maquinaria"),
    ENTORNO("Condiciones del entorno de trabajo", "Entorno")
}

/**
 * Nivel de criticidad normativa del punto de control.
 * [weight] es la ponderacion usada por la regla de riesgo (ver SafetyRuleEngine).
 */
enum class CriticalityLevel(val label: String, val weight: Int) {
    ALTO("Alto", 5),
    MEDIO("Medio", 3),
    BAJO("Bajo", 1)
}

/** Resultado capturado por el inspector para un punto de control. */
enum class AnswerResult(val label: String) {
    SIN_EVALUAR("Sin evaluar"),
    CONFORME("Conforme"),
    NO_CONFORME("No conforme"),
    NO_APLICA("No aplica")
}

/** Ciclo de vida del hallazgo detectado en campo. */
enum class IncidenceStatus(val label: String) {
    ABIERTO("Abierto"),
    EN_MITIGACION("En mitigacion"),
    CERRADO("Cerrado")
}

/** Estado global calculado del recorrido. */
enum class InspectionStatus(val label: String) {
    EN_CAPTURA("En captura"),
    CONFORME("Conforme"),
    REINSPECCION_24H("Reinspeccion 24 h"),
    DETENCION_LABORES("Detencion de labores")
}

/**
 * Condicion de zona que hace aplicable un punto de control.
 * Permite que la proteccion ocular/auditiva y los trabajos en altura solo se
 * evaluen en las zonas donde la norma los exige.
 */
enum class ZoneRequirement {
    SIEMPRE, OCULAR, AUDITIVA, ALTURA
}
