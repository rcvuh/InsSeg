# InspSeg — Inspección y Seguridad Operacional

Aplicación Android nativa para el recorrido diario del inspector de seguridad operacional en
logística, construcción y mantenimiento industrial. Sustituye el formulario impreso y la hoja de
cálculo compartida por una captura validada en campo, con las normas convertidas en reglas de
negocio y persistencia local en el dispositivo.

**Stack:** Kotlin · Jetpack Compose · Material 3 · Room · MVVM · Navigation Compose ·
**Firebase** (Authentication, Cloud Firestore, Cloud Storage) · Clean Architecture con mappers.

> **Version 2 (T4.4_2):** la informacion local se respalda en la nube con Firebase. Ver la
> seccion 0.

---

## 0. Firebase (nuevo en la version 2)

### Configurar el proyecto de Firebase
1. En la consola de Firebase, dentro del proyecto `authapp-uaemex-7073`, pulsa **Agregar app >
   Android** con el paquete `com.pascm.inspseg` y descarga `google-services.json` en `app/`.
2. **Authentication > Metodo de acceso:** Correo electronico/contraseña (ya habilitado).
3. **Firestore Database** y **Storage:** crearlos y publicar las reglas de `firebase/`
   (`firestore.rules` y `storage.rules`), o con la CLI: `firebase deploy --only firestore:rules,storage`.

Sin `google-services.json` el proyecto compila igual y la app trabaja en modo local.

### Que se guarda en la nube
```
inspectores/{uid}                    perfil del inspector
inspectores/{uid}/recorridos/{id}    recorrido + respuestas (URL de la foto en Storage)
inspectores/{uid}/hallazgos/{id}     hallazgo con su estado de mitigacion
evidencias/{uid}/{recorrido}/punto_{id}.jpg   fotografias (Cloud Storage, max 5 MB)
```
Offline-first: todo se guarda primero en Room (`syncedAt = NULL`) y se sube al cerrar el
recorrido, al cambiar el estado de un hallazgo, al iniciar sesion o con *Sincronizar ahora*.

### Separacion de capas
```
data/local/entity   Entidades de Room            ┐
data/remote/entity  Entidades de Firebase (docs) ├─ data/mapper ─> domain/model ─> ui/viewmodel ─> ui/screens
                                                 ┘   (LocalMappers, RemoteMappers, FirebaseMappers)
domain/rules        SafetyRuleEngine, CredentialRules, CloudSecurityRules
domain/repository   Interfaces que usan los ViewModels
```

### Generar el APK sin Android Studio
Sube el proyecto a GitHub y ejecuta la accion **Construir APK de InspSeg** (pestaña *Actions*).
Para que el APK tenga Firebase, `app/google-services.json` debe estar en el repositorio.


---

## 1. Cómo compilar y ejecutar

```bash
# Requiere JDK 17 y el SDK de Android (compileSdk 35)
./gradlew :app:assembleDebug          # genera app/build/outputs/apk/debug/app-debug.apk
./gradlew :app:testDebugUnitTest      # pruebas del motor de reglas
```

En Android Studio: `File > Open` sobre la carpeta del proyecto y ejecutar la configuración `app`.
`local.properties` apunta al SDK local y no se versiona; Android Studio lo genera al abrir.

> **JDK de Gradle:** el proyecto usa AGP 8.7.3 / Gradle 8.9, que admiten Java 17 o 21 pero **no**
> Java 25. Si Android Studio trae un JBR 25, cambiar en
> `Settings > Build, Execution, Deployment > Build Tools > Gradle > Gradle JDK` a un JDK 17 o 21.
> De lo contrario la sincronización falla con un mensaje escueto: `25.0.2`.

Pruebas unitarias: 19 (motor de reglas, mappers y reglas de seguridad del dominio).

Permisos que solicita en tiempo de ejecución: cámara (evidencia fotográfica) y ubicación
(coordenadas del hallazgo). Ambos son opcionales para navegar, obligatorios sólo donde la norma
exige evidencia.

---

## 2. Arquitectura (MVVM)

```
ui/            Compose. Sólo dibuja estado y emite eventos. No calcula reglas.
 ├ theme/      Paleta, tipografía, espaciado (apariencia oscura permanente)
 ├ components/ Piezas reutilizables (tarjeta de punto de control, medidor, insignias)
 ├ screens/    Pantallas + ViewModels + UiState
 └ navigation/ NavHost y barra inferior

domain/        Lógica pura, sin dependencias de Android
 ├ model/      Enums, ChecklistPoint
 └ rules/      SafetyRuleEngine  ← las normas hechas código

data/
 ├ local/      Room: entidades, DAOs, converters, catálogo precargado
 └ repository/ InspectionRepository, única fuente de verdad

util/          GPS, archivos de evidencia, formatos
```

Flujo: `Composable → ViewModel.setResult() → SafetyRuleEngine.evaluate() → UiState → Composable`.
Cada toque del inspector vuelve a pasar por el motor de reglas; la UI nunca decide si algo cumple.

---

## 3. Modelo de datos (Room)

| Tabla | Entidad | Contenido |
|---|---|---|
| `normatives` | `NormativeEntity` | Código de norma (NOM-009-STPS-2011…), requisito y equivalente OSHA |
| `checklist_items` | `ChecklistItemEntity` | Punto de control → norma, criticidad (ALTO/MEDIO/BAJO), si exige foto, si detiene labores, condición de zona |
| `work_zones` | `WorkZoneEntity` | Zona y qué protecciones exige (ocular, auditiva, altura) |
| `inspections` | `InspectionEntity` | Cabecera del recorrido: zona, inspector, puntaje, estado, plazo de reinspección, GPS |
| `answers` | `AnswerEntity` | Respuesta por punto de control, con nota y URI de foto |
| `incidences` | `IncidenceEntity` | Hallazgo: estado (Abierto / En mitigación / Cerrado), `timestamp` y coordenadas GPS |

Relaciones con claves foráneas: `checklist_items → normatives`, `answers → inspections` (CASCADE) y
`incidences → inspections` (CASCADE). El catálogo de 10 normas, 5 zonas y 24 puntos de control se
precarga en el primer arranque (`SeedData`).

**La zona filtra el formulario.** Los puntos de protección ocular, auditiva y trabajos en altura
sólo son aplicables donde la zona los exige; el resto queda como *No aplica* y sale del
denominador del puntaje.

---

## 4. Las normas como reglas de negocio

Todo vive en `domain/rules/SafetyRuleEngine.kt`, código puro y probado con JUnit.

### Regla de paro crítico
Los puntos marcados `stopsWork = true` (arnés con costuras dañadas, líneas de vida sin
certificación, andamio sin tarjeta verde, LOTO ausente, cables con partes vivas, vía de evacuación
obstruida, casco fisurado) disparan, al marcarse *No conforme*:

- el estado del formulario pasa a **Incidencia crítica / Detención de labores**;
- `canMarkAsConform = false`: se inhabilita cerrar el recorrido como conforme aunque el puntaje sea alto;
- `canSave = false` mientras falte **fotografía + nota** de evidencia; la tarjeta señala el faltante
  y el botón de cierre explica qué impide guardar.

### Regla de ponderación de riesgo
Cumplimiento = `Σ peso(conformes) / Σ peso(evaluados)`, con ALTO = 5, MEDIO = 3, BAJO = 1.
Si el resultado cae por debajo de **85 %**, la app fija automáticamente
`reinspectionDueAt = ahora + 24 h` y el recorrido se guarda como **Reinspección 24 h**, que aparece
en el tablero con su cuenta regresiva.

### Evidencia obligatoria
Todo hallazgo *No conforme* exige nota de al menos 10 caracteres; los puntos con
`requiresPhoto = true` exigen además fotografía. La verificación se hace en el `UiState` y se
repite en `ViewModel.save()`: la UI no es la autoridad.

---

## 5. Recorrido de la app

1. **Recorridos** — hallazgos abiertos, cuántos detienen labores, reinspecciones vencidas; captura
   del inspector y selección de zona para iniciar.
2. **Captura** — tres pestañas (EPP · Maquinaria · Entorno) con los 24 puntos de control. Encabezado
   fijo con estado normativo, medidor de cumplimiento con la marca del umbral de 85 %, avance de
   captura y coordenadas. Cierre mediante hoja de confirmación que resume qué se va a guardar.
3. **Hallazgos** — seguimiento de Abierto → En mitigación → Cerrado, con evidencia, fecha y GPS.
4. **Normas** — consulta de respaldo del catálogo normativo.

---

## 6. Pruebas

`app/src/test/java/com/pascm/inspseg/SafetyRuleEngineTest.kt` cubre las reglas normativas:
cierre conforme, paro crítico, evidencia incompleta, umbral de 85 % con plazo de 24 h, exclusión de
puntos no aplicables y formulario incompleto.

---

## 7. Diseño

Interfaz negra permanente e imágenes en escala de grises, por decisión operativa. El detalle de
la revisión de diseño, con contrastes medidos y las guías aplicadas, está en
[`DISENO.md`](DISENO.md).
