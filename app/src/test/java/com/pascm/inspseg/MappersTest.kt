package com.pascm.inspseg

import com.pascm.inspseg.data.local.entity.AnswerEntity
import com.pascm.inspseg.data.local.entity.ChecklistItemEntity
import com.pascm.inspseg.data.local.entity.IncidenceEntity
import com.pascm.inspseg.data.local.entity.InspectionEntity
import com.pascm.inspseg.data.local.entity.NormativeEntity
import com.pascm.inspseg.data.local.entity.WorkZoneEntity
import com.pascm.inspseg.data.mapper.enumOr
import com.pascm.inspseg.data.mapper.toDocument
import com.pascm.inspseg.data.mapper.toDomain
import com.pascm.inspseg.data.mapper.toEntity
import com.pascm.inspseg.data.remote.entity.IncidenceDocument
import com.pascm.inspseg.domain.model.AnswerResult
import com.pascm.inspseg.domain.model.CriticalityLevel
import com.pascm.inspseg.domain.model.IncidenceStatus
import com.pascm.inspseg.domain.model.InspectionModule
import com.pascm.inspseg.domain.model.InspectionStatus
import com.pascm.inspseg.domain.model.ZoneRequirement
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Test

/** Los mappers deben ser reversibles y no perder ni inventar datos. */
class MappersTest {

    private val inspectionEntity = InspectionEntity(
        id = 7, zoneId = 3, zoneName = "Obra en altura / fachada", inspector = "Roberto Carlos Gonzalez Caballero",
        startedAt = 1_000, finishedAt = 2_000, scorePercent = 92.0, status = InspectionStatus.DETENCION_LABORES,
        criticalStop = true, reinspectionDueAt = 90_000, latitude = 19.1833, longitude = -99.4667,
        ownerUid = "uid-123", syncedAt = null
    )

    private val incidenceEntity = IncidenceEntity(
        id = 11, inspectionId = 7, itemId = 4, itemTitle = "Costuras del arnes integras",
        zoneName = "Obra en altura / fachada", normativeCode = "NOM-009-STPS-2011",
        criticality = CriticalityLevel.ALTO, stopsWork = true, note = "Costuras rotas en cinta pectoral",
        photoUri = "content://com.pascm.inspseg.fileprovider/evidencias/hallazgo_4.jpg",
        status = IncidenceStatus.ABIERTO, createdAt = 2_000, updatedAt = 2_000,
        latitude = 19.1833, longitude = -99.4667, ownerUid = "uid-123"
    )

    @Test
    fun `entidad de recorrido ida y vuelta sin perdidas`() {
        assertEquals(inspectionEntity, inspectionEntity.toDomain().toEntity())
    }

    @Test
    fun `entidad de hallazgo ida y vuelta sin perdidas`() {
        assertEquals(incidenceEntity, incidenceEntity.toDomain().toEntity())
    }

    @Test
    fun `catalogo ida y vuelta`() {
        val norma = NormativeEntity("NOM-017-STPS-2008", "EPP", "Requisito", "OSHA 1910.132")
        val zona = WorkZoneEntity(2, "Nave", "Desc", true, true, false)
        val punto = ChecklistItemEntity(5, InspectionModule.EPP, "Casco", "Sin fisuras", norma.code,
            CriticalityLevel.ALTO, requiresPhoto = true, stopsWork = true, zoneRequirement = ZoneRequirement.SIEMPRE, orderIndex = 1)
        assertEquals(norma, norma.toDomain().toEntity())
        assertEquals(zona, zona.toDomain().toEntity())
        assertEquals(punto, punto.toDomain().toEntity())
    }

    @Test
    fun `documento de Firestore usa la URL de Storage y no la ruta local`() {
        val answers = listOf(
            AnswerEntity(inspectionId = 7, itemId = 4, result = AnswerResult.NO_CONFORME,
                note = "Costuras rotas", photoUri = "content://local/foto.jpg", answeredAt = 1_500).toDomain(),
            AnswerEntity(inspectionId = 7, itemId = 5, result = AnswerResult.CONFORME, answeredAt = 1_600).toDomain()
        )
        val doc = inspectionEntity.toDomain().toDocument(
            ownerUid = "uid-123", answers = answers,
            photoUrls = mapOf(4L to "https://firebasestorage.googleapis.com/foto.jpg"), now = 3_000
        )
        assertEquals("DETENCION_LABORES", doc.estado)
        assertEquals("uid-123", doc.inspectorUid)
        assertEquals(2, doc.respuestas.size)
        assertEquals("https://firebasestorage.googleapis.com/foto.jpg", doc.respuestas[0].fotoUrl)
        assertNull(doc.respuestas[1].fotoUrl)
        assertFalse(doc.respuestas.any { it.fotoUrl?.startsWith("content://") == true })
    }

    @Test
    fun `documento de recorrido regresa al mismo modelo de dominio`() {
        val domain = inspectionEntity.toDomain()
        val back = domain.toDocument("uid-123", emptyList(), emptyMap(), now = 3_000).toDomain()
        assertEquals(domain.copy(syncedAt = 3_000), back)
    }

    @Test
    fun `hallazgo a documento y de regreso`() {
        val domain = incidenceEntity.toDomain()
        val doc = domain.toDocument("uid-123", photoUrl = "https://storage/foto.jpg")
        assertEquals("ALTO", doc.criticidad)
        assertEquals("ABIERTO", doc.estado)
        val back = doc.toDomain()
        assertEquals(domain.copy(photoUri = "https://storage/foto.jpg", syncedAt = 2_000), back)
    }

    @Test
    fun `texto desconocido en Firestore no tumba la app`() {
        val doc = IncidenceDocument(idLocal = 1, criticidad = "EXTREMO", estado = "BORRADO")
        val domain = doc.toDomain()
        assertEquals(CriticalityLevel.MEDIO, domain.criticality)
        assertEquals(IncidenceStatus.ABIERTO, domain.status)
        assertEquals(AnswerResult.NO_APLICA, enumOr("NO_APLICA", AnswerResult.SIN_EVALUAR))
    }
}
