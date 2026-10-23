package com.pascm.inspseg

import com.pascm.inspseg.domain.model.AnswerResult
import com.pascm.inspseg.domain.model.ChecklistItem
import com.pascm.inspseg.domain.model.ChecklistPoint
import com.pascm.inspseg.domain.model.CriticalityLevel
import com.pascm.inspseg.domain.model.InspectionModule
import com.pascm.inspseg.domain.model.InspectionStatus
import com.pascm.inspseg.domain.model.Normative
import com.pascm.inspseg.domain.model.ZoneRequirement
import com.pascm.inspseg.domain.rules.SafetyRuleEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.concurrent.TimeUnit

/** Verificacion de las reglas normativas traducidas a logica de negocio. */
class SafetyRuleEngineTest {

    private val norma = Normative("NOM-009-STPS-2011", "Altura", "Requisito", "OSHA 1926.501")

    private fun point(
        id: Long,
        criticality: CriticalityLevel,
        result: AnswerResult,
        stopsWork: Boolean = false,
        requiresPhoto: Boolean = false,
        applicable: Boolean = true,
        note: String = "",
        photoUri: String? = null
    ) = ChecklistPoint(
        item = ChecklistItem(
            id = id,
            module = InspectionModule.EPP,
            title = "Punto $id",
            criterion = "Criterio",
            normativeCode = norma.code,
            criticality = criticality,
            requiresPhoto = requiresPhoto,
            stopsWork = stopsWork,
            zoneRequirement = ZoneRequirement.SIEMPRE,
            orderIndex = id.toInt()
        ),
        normative = norma,
        applicable = applicable,
        result = result,
        note = note,
        photoUri = photoUri
    )

    @Test
    fun `todo conforme cierra como conforme`() {
        val result = SafetyRuleEngine.evaluate(
            listOf(
                point(1, CriticalityLevel.ALTO, AnswerResult.CONFORME),
                point(2, CriticalityLevel.MEDIO, AnswerResult.CONFORME)
            )
        )

        assertEquals(100.0, result.scorePercent, 0.01)
        assertEquals(InspectionStatus.CONFORME, result.status)
        assertTrue(result.canMarkAsConform)
        assertTrue(result.canSave)
        assertNull(result.reinspectionDueAt)
    }

    @Test
    fun `un punto de paro critico detiene labores e inhabilita el cierre conforme`() {
        val points = listOf(
            point(
                id = 1,
                criticality = CriticalityLevel.ALTO,
                result = AnswerResult.NO_CONFORME,
                stopsWork = true,
                requiresPhoto = true,
                note = "Andamio sin tarjeta verde de autorizacion",
                photoUri = "content://evidencia/1.jpg"
            ),
            point(2, CriticalityLevel.BAJO, AnswerResult.CONFORME)
        )

        val result = SafetyRuleEngine.evaluate(points)

        assertTrue(result.criticalStop)
        assertFalse(result.canMarkAsConform)
        assertEquals(InspectionStatus.DETENCION_LABORES, result.status)
        assertNotNull(result.reinspectionDueAt)
    }

    @Test
    fun `el hallazgo critico no se puede guardar sin fotografia ni nota`() {
        val sinEvidencia = listOf(
            point(
                id = 1,
                criticality = CriticalityLevel.ALTO,
                result = AnswerResult.NO_CONFORME,
                stopsWork = true,
                requiresPhoto = true
            )
        )
        assertFalse(SafetyRuleEngine.evaluate(sinEvidencia).canSave)

        val soloNota = listOf(
            point(
                id = 1,
                criticality = CriticalityLevel.ALTO,
                result = AnswerResult.NO_CONFORME,
                stopsWork = true,
                requiresPhoto = true,
                note = "Arnes con costuras rotas en cinta pectoral"
            )
        )
        assertFalse(SafetyRuleEngine.evaluate(soloNota).canSave)

        val completo = listOf(
            point(
                id = 1,
                criticality = CriticalityLevel.ALTO,
                result = AnswerResult.NO_CONFORME,
                stopsWork = true,
                requiresPhoto = true,
                note = "Arnes con costuras rotas en cinta pectoral",
                photoUri = "content://evidencia/1.jpg"
            )
        )
        assertTrue(SafetyRuleEngine.evaluate(completo).canSave)
    }

    @Test
    fun `bajo el 85 por ciento se programa reinspeccion a 24 horas`() {
        val now = 1_000_000L
        // ALTO(5) no conforme + MEDIO(3) y BAJO(1) conformes = 4/9 = 44.4 %
        val result = SafetyRuleEngine.evaluate(
            listOf(
                point(
                    id = 1,
                    criticality = CriticalityLevel.ALTO,
                    result = AnswerResult.NO_CONFORME,
                    note = "Casquillo del calzado expuesto por desgaste"
                ),
                point(2, CriticalityLevel.MEDIO, AnswerResult.CONFORME),
                point(3, CriticalityLevel.BAJO, AnswerResult.CONFORME)
            ),
            now = now
        )

        assertTrue(result.scorePercent < SafetyRuleEngine.MIN_COMPLIANCE_PERCENT)
        assertEquals(InspectionStatus.REINSPECCION_24H, result.status)
        assertEquals(now + TimeUnit.HOURS.toMillis(24), result.reinspectionDueAt)
    }

    @Test
    fun `los puntos no aplicables quedan fuera del puntaje`() {
        val result = SafetyRuleEngine.evaluate(
            listOf(
                point(1, CriticalityLevel.ALTO, AnswerResult.CONFORME),
                point(
                    id = 2,
                    criticality = CriticalityLevel.ALTO,
                    result = AnswerResult.NO_APLICA,
                    applicable = false
                )
            )
        )

        assertEquals(1, result.applicableCount)
        assertEquals(100.0, result.scorePercent, 0.01)
        assertTrue(result.canSave)
    }

    @Test
    fun `un formulario a medias no se puede cerrar`() {
        val result = SafetyRuleEngine.evaluate(
            listOf(
                point(1, CriticalityLevel.ALTO, AnswerResult.CONFORME),
                point(2, CriticalityLevel.MEDIO, AnswerResult.SIN_EVALUAR)
            )
        )

        assertEquals(InspectionStatus.EN_CAPTURA, result.status)
        assertEquals(1, result.pendingAnswers)
        assertFalse(result.canSave)
    }
}
