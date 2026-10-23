package com.pascm.inspseg.domain.model

/**
 * Punto de control tal como lo ve el inspector: la definicion normativa del item,
 * si aplica a la zona del recorrido y la captura que lleva hasta el momento.
 *
 * Usa exclusivamente modelos de dominio (ChecklistItem, Normative); las entidades
 * de Room y los documentos de Firebase nunca llegan hasta aqui.
 */
data class ChecklistPoint(
    val item: ChecklistItem,
    val normative: Normative,
    val applicable: Boolean,
    val result: AnswerResult = AnswerResult.SIN_EVALUAR,
    val note: String = "",
    val photoUri: String? = null
) {
    val id: Long get() = item.id

    /** Un hallazgo no conforme siempre exige nota; la foto depende del punto de control. */
    val needsNote: Boolean get() = result == AnswerResult.NO_CONFORME && note.trim().length < MIN_NOTE_LENGTH

    val needsPhoto: Boolean
        get() = result == AnswerResult.NO_CONFORME && item.requiresPhoto && photoUri.isNullOrBlank()

    val evidenceComplete: Boolean get() = !needsNote && !needsPhoto

    /** Este punto detiene labores cuando resulta no conforme. */
    val triggersStop: Boolean
        get() = applicable && item.stopsWork && result == AnswerResult.NO_CONFORME

    companion object {
        const val MIN_NOTE_LENGTH = 10
    }
}
