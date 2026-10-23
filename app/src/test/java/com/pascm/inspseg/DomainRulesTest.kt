package com.pascm.inspseg

import com.pascm.inspseg.domain.model.CriticalityLevel
import com.pascm.inspseg.domain.model.Incidence
import com.pascm.inspseg.domain.model.IncidenceStatus
import com.pascm.inspseg.domain.model.Inspection
import com.pascm.inspseg.domain.model.InspectionStatus
import com.pascm.inspseg.domain.rules.CloudSecurityRules
import com.pascm.inspseg.domain.rules.CredentialRules
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/** Reglas de validacion y seguridad que viven en la capa de dominio. */
class DomainRulesTest {

    private fun inspection(owner: String?, status: InspectionStatus = InspectionStatus.CONFORME, finished: Long? = 2_000) =
        Inspection(1, 1, "Nave", "Roberto", 1_000, finished, 95.0, status, false, null, null, null, owner, null)

    @Test
    fun `credenciales de acceso`() {
        assertTrue(CredentialRules.forSignIn("roberto.uaemex@example.com", "x").isValid)
        assertNotNull(CredentialRules.forSignIn("roberto", "x").emailError)
        assertNotNull(CredentialRules.forSignIn("roberto@uaemex.mx", "").passwordError)
    }

    @Test
    fun `registro exige contraseña de 8 con letras y numeros y nombre completo`() {
        assertNotNull(CredentialRules.validateNewPassword("abc123"))
        assertNotNull(CredentialRules.validateNewPassword("solamenteletras"))
        assertNull(CredentialRules.validateNewPassword("Inspector2026"))
        assertNotNull(CredentialRules.validateName("Rob"))
        assertTrue(CredentialRules.forRegistration("Roberto Gonzalez", "rob@uaemex.mx", "Inspector2026").isValid)
    }

    @Test
    fun `solo el dueño sube su recorrido cerrado`() {
        assertTrue(CloudSecurityRules.canUpload(inspection("uid-a"), "uid-a"))
        assertFalse(CloudSecurityRules.canUpload(inspection("uid-a"), "uid-b"))
        assertFalse(CloudSecurityRules.canUpload(inspection("uid-a"), null))
        assertFalse(CloudSecurityRules.canUpload(inspection("uid-a", InspectionStatus.EN_CAPTURA), "uid-a"))
        assertFalse(CloudSecurityRules.canUpload(inspection("uid-a", finished = null), "uid-a"))
    }

    @Test
    fun `hallazgo de otro inspector no se sube`() {
        val inc = Incidence(1, 1, 1, "Casco", "Nave", "NOM-017", CriticalityLevel.ALTO, true, "nota de prueba",
            null, IncidenceStatus.ABIERTO, 1, 1, null, null, "uid-a", null)
        assertTrue(CloudSecurityRules.canUpload(inc, "uid-a"))
        assertFalse(CloudSecurityRules.canUpload(inc, "uid-b"))
    }

    @Test
    fun `rutas en la nube quedan bajo el uid del inspector`() {
        assertEquals("inspectores/u1/recorridos/7", CloudSecurityRules.inspectionPath("u1", 7))
        assertEquals("inspectores/u1/hallazgos/11", CloudSecurityRules.incidencePath("u1", 11))
        assertEquals("evidencias/u1/7/punto_4.jpg", CloudSecurityRules.evidencePath("u1", 7, 4))
    }

    @Test
    fun `limite de 5 MB por fotografia`() {
        assertTrue(CloudSecurityRules.photoSizeAllowed(2L * 1024 * 1024))
        assertFalse(CloudSecurityRules.photoSizeAllowed(6L * 1024 * 1024))
        assertFalse(CloudSecurityRules.photoSizeAllowed(0))
    }
}
