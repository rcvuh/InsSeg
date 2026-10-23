package com.pascm.inspseg.data.local

import com.pascm.inspseg.data.local.entity.ChecklistItemEntity
import com.pascm.inspseg.data.local.entity.NormativeEntity
import com.pascm.inspseg.data.local.entity.WorkZoneEntity
import com.pascm.inspseg.domain.model.CriticalityLevel
import com.pascm.inspseg.domain.model.CriticalityLevel.ALTO
import com.pascm.inspseg.domain.model.CriticalityLevel.BAJO
import com.pascm.inspseg.domain.model.CriticalityLevel.MEDIO
import com.pascm.inspseg.domain.model.InspectionModule
import com.pascm.inspseg.domain.model.InspectionModule.ENTORNO
import com.pascm.inspseg.domain.model.InspectionModule.EPP
import com.pascm.inspseg.domain.model.InspectionModule.MAQUINARIA
import com.pascm.inspseg.domain.model.ZoneRequirement

/**
 * Catalogo normativo precargado. Las normas no se muestran como texto teorico:
 * cada punto de control se ancla a su norma y de ahi se derivan las reglas de
 * negocio que valida el ViewModel.
 */
object SeedData {

    val normatives = listOf(
        NormativeEntity(
            "NOM-017-STPS-2008",
            "Equipo de proteccion personal",
            "Seleccion, uso y mantenimiento del EPP de acuerdo con los riesgos del puesto.",
            "OSHA 29 CFR 1910.132"
        ),
        NormativeEntity(
            "NOM-009-STPS-2011",
            "Trabajos en altura",
            "Sistemas personales de detencion de caidas y plataformas certificadas y vigentes.",
            "OSHA 29 CFR 1926.501"
        ),
        NormativeEntity(
            "NOM-011-STPS-2001",
            "Ruido en el centro de trabajo",
            "Proteccion auditiva obligatoria cuando el nivel sonoro supera 80 dB(A).",
            "OSHA 29 CFR 1910.95"
        ),
        NormativeEntity(
            "NOM-002-STPS-2010",
            "Prevencion y proteccion contra incendios",
            "Extintores operables, presurizados, señalizados y con recarga vigente.",
            "OSHA 29 CFR 1910.157"
        ),
        NormativeEntity(
            "NOM-004-STPS-1999",
            "Sistemas de proteccion en maquinaria",
            "Bloqueo y etiquetado de energias peligrosas antes de cualquier intervencion.",
            "OSHA 29 CFR 1910.147 (LOTO)"
        ),
        NormativeEntity(
            "NOM-029-STPS-2011",
            "Mantenimiento de instalaciones electricas",
            "Ausencia de partes vivas expuestas y control del riesgo electrico.",
            "OSHA 29 CFR 1910.333"
        ),
        NormativeEntity(
            "NOM-022-STPS-2015",
            "Electricidad estatica",
            "Conexion a tierra fisica verificada en equipos y tableros.",
            "OSHA 29 CFR 1910.304(g)"
        ),
        NormativeEntity(
            "NOM-026-STPS-2008",
            "Colores y señales de seguridad",
            "Señalizacion visible e inequivoca de rutas y salidas de emergencia.",
            "OSHA 29 CFR 1910.37"
        ),
        NormativeEntity(
            "NOM-025-STPS-2008",
            "Iluminacion en centros de trabajo",
            "Iluminacion de emergencia funcional en las rutas de evacuacion.",
            "OSHA 29 CFR 1910.37(b)"
        ),
        NormativeEntity(
            "NOM-018-STPS-2015",
            "Sustancias quimicas peligrosas",
            "Control de derrames, contencion y manejo adecuado de residuos.",
            "OSHA 29 CFR 1910.1200"
        )
    )

    val zones = listOf(
        WorkZoneEntity(
            id = 1, name = "Patio de maniobras",
            description = "Carga, descarga y transito de montacargas.",
            requiresEyeProtection = true,
            requiresHearingProtection = false,
            involvesHeightWork = false
        ),
        WorkZoneEntity(
            id = 2, name = "Nave de produccion",
            description = "Maquinaria en operacion continua, por encima de 85 dB(A).",
            requiresEyeProtection = true,
            requiresHearingProtection = true,
            involvesHeightWork = false
        ),
        WorkZoneEntity(
            id = 3, name = "Obra en altura / fachada",
            description = "Andamios y plataformas por encima de 1.80 m.",
            requiresEyeProtection = true,
            requiresHearingProtection = false,
            involvesHeightWork = true
        ),
        WorkZoneEntity(
            id = 4, name = "Subestacion electrica",
            description = "Tableros energizados y trabajos con bloqueo LOTO.",
            requiresEyeProtection = true,
            requiresHearingProtection = true,
            involvesHeightWork = false
        ),
        WorkZoneEntity(
            id = 5, name = "Almacen de quimicos",
            description = "Acopio de sustancias peligrosas y residuos.",
            requiresEyeProtection = true,
            requiresHearingProtection = false,
            involvesHeightWork = false
        )
    )

    val checklist: List<ChecklistItemEntity> = listOf(
        // --- A. Equipo de proteccion personal ---
        item(
            1, EPP, "Casco sin fisuras",
            "Casco libre de fisuras, abolladuras o deformaciones en la copa.",
            "NOM-017-STPS-2008", ALTO, requiresPhoto = true, stopsWork = true
        ),
        item(
            2, EPP, "Tafilete vigente",
            "Suspension interna sin caducar y sin desgaste en las bandas.",
            "NOM-017-STPS-2008", MEDIO
        ),
        item(
            3, EPP, "Barbiquejo ajustado",
            "Barbiquejo presente y ajustado; el casco no se desprende al inclinarse.",
            "NOM-017-STPS-2008", MEDIO
        ),
        item(
            4, EPP, "Suela antideslizante",
            "Suela con dibujo vigente, sin desgaste liso ni desprendimientos.",
            "NOM-017-STPS-2008", MEDIO
        ),
        item(
            5, EPP, "Casquillo intacto",
            "Casquillo de proteccion sin exposicion, hundimientos ni corrosion.",
            "NOM-017-STPS-2008", ALTO, requiresPhoto = true
        ),
        item(
            6, EPP, "Costuras del arnes integras",
            "Sin costuras rotas, fisuras ni hilos cortados en las cintas de carga.",
            "NOM-009-STPS-2011", ALTO,
            requiresPhoto = true, stopsWork = true, zone = ZoneRequirement.ALTURA
        ),
        item(
            7, EPP, "Arnes sin desgaste quimico o por friccion",
            "Sin abrasion, quemaduras, rigidez ni manchas de solventes.",
            "NOM-009-STPS-2011", ALTO,
            requiresPhoto = true, stopsWork = true, zone = ZoneRequirement.ALTURA
        ),
        item(
            8, EPP, "Certificacion de lineas de vida vigente",
            "Etiqueta de ultima certificacion legible y dentro de vigencia.",
            "NOM-009-STPS-2011", ALTO,
            requiresPhoto = true, stopsWork = true, zone = ZoneRequirement.ALTURA
        ),
        item(
            9, EPP, "Proteccion ocular en uso",
            "Lentes o goggles disponibles y usados efectivamente en la zona.",
            "NOM-017-STPS-2008", MEDIO, zone = ZoneRequirement.OCULAR
        ),
        item(
            10, EPP, "Proteccion auditiva en uso",
            "Tapones u orejeras disponibles y usados efectivamente en la zona.",
            "NOM-011-STPS-2001", MEDIO, zone = ZoneRequirement.AUDITIVA
        ),

        // --- B. Maquinaria y herramientas criticas ---
        item(
            11, MAQUINARIA, "Manometro del extintor en zona verde",
            "Aguja dentro del rango verde de presion operativa.",
            "NOM-002-STPS-2010", ALTO, requiresPhoto = true
        ),
        item(
            12, MAQUINARIA, "Sello de seguridad intacto",
            "Seguro y sello sin violar; pasador en su lugar.",
            "NOM-002-STPS-2010", MEDIO
        ),
        item(
            13, MAQUINARIA, "Fecha de recarga vigente",
            "Etiqueta de recarga legible y dentro del periodo de un año.",
            "NOM-002-STPS-2010", ALTO
        ),
        item(
            14, MAQUINARIA, "Bloqueo y etiquetado LOTO",
            "Candado y tarjeta de bloqueo instalados en el tablero intervenido.",
            "NOM-004-STPS-1999", ALTO, requiresPhoto = true, stopsWork = true
        ),
        item(
            15, MAQUINARIA, "Cables sin partes vivas expuestas",
            "Aislamiento integro, sin empalmes improvisados ni conductor a la vista.",
            "NOM-029-STPS-2011", ALTO, requiresPhoto = true, stopsWork = true
        ),
        item(
            16, MAQUINARIA, "Conexion a tierra fisica",
            "Continuidad de tierra verificada en gabinete y equipos.",
            "NOM-022-STPS-2015", ALTO
        ),
        item(
            17, MAQUINARIA, "Tarjeta verde de andamio",
            "Tarjeta Apto para uso vigente, firmada por persona autorizada.",
            "NOM-009-STPS-2011", ALTO,
            requiresPhoto = true, stopsWork = true, zone = ZoneRequirement.ALTURA
        ),
        item(
            18, MAQUINARIA, "Barandales y rodapie instalados",
            "Baranda superior, intermedia y rodapie completos en toda la plataforma.",
            "NOM-009-STPS-2011", ALTO, requiresPhoto = true, zone = ZoneRequirement.ALTURA
        ),
        item(
            19, MAQUINARIA, "Nivelacion y apoyo del andamio",
            "Base nivelada, husillos apoyados sobre durmiente firme.",
            "NOM-009-STPS-2011", MEDIO, zone = ZoneRequirement.ALTURA
        ),

        // --- C. Condiciones del entorno de trabajo ---
        item(
            20, ENTORNO, "Vias de evacuacion despejadas",
            "Ruta libre de material, tarimas o equipo en todo su ancho.",
            "NOM-002-STPS-2010", ALTO, requiresPhoto = true, stopsWork = true
        ),
        item(
            21, ENTORNO, "Señalizacion de salidas visible",
            "Señales fotoluminiscentes visibles desde cualquier punto de la ruta.",
            "NOM-026-STPS-2008", MEDIO
        ),
        item(
            22, ENTORNO, "Iluminacion de respaldo funcional",
            "Las luminarias de emergencia encienden al corte y cubren la ruta.",
            "NOM-025-STPS-2008", MEDIO
        ),
        item(
            23, ENTORNO, "Sin derrames de aceites o quimicos",
            "Piso libre de derrames; contencion secundaria en su lugar.",
            "NOM-018-STPS-2015", ALTO, requiresPhoto = true
        ),
        item(
            24, ENTORNO, "Acopio adecuado de residuos",
            "Residuos clasificados y confinados en contenedores identificados.",
            "NOM-018-STPS-2015", BAJO
        )
    )

    private fun item(
        order: Int,
        module: InspectionModule,
        title: String,
        criterion: String,
        normativeCode: String,
        criticality: CriticalityLevel,
        requiresPhoto: Boolean = false,
        stopsWork: Boolean = false,
        zone: ZoneRequirement = ZoneRequirement.SIEMPRE
    ) = ChecklistItemEntity(
        id = order.toLong(),
        module = module,
        title = title,
        criterion = criterion,
        normativeCode = normativeCode,
        criticality = criticality,
        requiresPhoto = requiresPhoto,
        stopsWork = stopsWork,
        zoneRequirement = zone,
        orderIndex = order
    )
}
