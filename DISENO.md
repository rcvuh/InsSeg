# Revisión de diseño — InspSeg

Plataforma: móvil (Android nativo) · Framework: Jetpack Compose + Material 3 · Categoría:
herramienta de productividad para trabajo de campo.

Las guías citadas son principios de diseño de interfaz humana aplicados a Compose; donde la guía
habla de *Dynamic Type*, *Safe Area* o *SF Symbols*, aquí se traduce a escalado de texto en `sp`,
`WindowInsets` e iconografía Material.

---

## Resumen

Interfaz monocromática sobre negro puro, con el color reservado exclusivamente para el estado de
seguridad. El objetivo no es estético: es que en una nave ruidosa, con guantes y a contraluz, el
inspector distinga en un vistazo qué punto detiene labores. Valoración general: **buena**, con dos
tensiones deliberadas frente a las guías, documentadas abajo.

---

## Decisiones y su fundamento

### 1. Apariencia oscura permanente (tensión declarada)

> **Guía — Modo Oscuro > Buenas prácticas:** «Evite ofrecer un ajuste de apariencia propio de la
> app… la gente espera que todas las apps respeten su preferencia del sistema».

Se incumple a propósito, amparado en la excepción de la misma guía: *«en casos contados, considere
usar solo la apariencia oscura»*. Es una herramienta de campo que se usa en subestaciones, naves sin
iluminación natural y recorridos nocturnos, donde una pantalla blanca deslumbra y arruina la
adaptación a la oscuridad. Consecuencia asumida: la app no responderá al ajuste claro del sistema.
Mitigación aplicada: **no** se ofrece un selector de apariencia dentro de la app (eso sí sería un
error de la guía), el tema es único y no hay `values-night`, de modo que el resultado es idéntico en
cualquier dispositivo.

Implementación: `themes.xml` con fondo negro para la ventana (evita el destello blanco al arrancar)
y `InspSegTheme` con un `darkColorScheme` fijo; `forceDarkAllowed=false` para que el sistema no
intente invertir nada.

### 2. Contraste medido, no estimado

> **Guía — Accesibilidad:** texto hasta 17 pt necesita 4.5:1; en modo oscuro conviene apuntar a 7:1
> para texto pequeño.

Ratios contra el fondo `#000000`:

| Token | Valor | Contraste | Uso |
|---|---|---|---|
| `TextPrimary` | `#F5F5F7` | 19.0:1 | Títulos y cuerpo |
| `TextSecondary` | `#A9A9B2` | 8.4:1 | Texto de apoyo |
| `TextTertiary` | `#83838D` | 5.1:1 | Sólo metadatos (fecha, GPS) |
| `CriticalRed` | `#FF6B5E` | 7.5:1 | Paro crítico / no conforme |
| `WarningAmber` | `#FFB84D` | 12.2:1 | Reinspección / riesgo medio |
| `SafeGreen` | `#3ED598` | 11.2:1 | Conforme |
| `InfoSteel` | `#8FB8FF` | 9.6:1 | Referencia normativa |

Ninguno baja del mínimo de 4.5:1, y los colores de estado superan el objetivo de 7:1.

### 3. Un color, un significado

> **Guía — Color > Buenas prácticas:** «Evite usar el mismo color para significar cosas distintas».

Por eso la **acción principal es monocromática** (botón blanco sobre negro) en lugar del ámbar de
alta visibilidad que sería la tentación temática: el ámbar ya significa *riesgo medio /
reinspección*, y usarlo también para «guardar» habría vuelto ambiguo el único canal que importa.
La paleta de estado tiene tres colores y nada más los usa.

### 4. El color nunca viaja solo

> **Guía — Accesibilidad:** no dependa únicamente del color para transmitir información.

Cada estado se comunica con **icono + texto + color** (`StatusBadge`). El medidor de cumplimiento
muestra el porcentaje numérico y un veredicto escrito («Bajo el umbral: reinspección en 24 h»),
además de una marca física del umbral de 85 % en la barra. Un inspector con deuteranopía lee lo
mismo que cualquier otro.

### 5. Imágenes en escala de grises

> **Guía — Modo Oscuro > Iconos e imágenes:** atenúe las imágenes con fondo claro para que no
> «brillen» dentro de un contexto oscuro.

Toda fotografía de evidencia pasa por `GrayscaleFilter` (`ColorMatrix` con saturación 0). Además de
integrarse con el registro visual de la app, evita que una foto a pleno sol compita con las señales
de color del estado de seguridad, que es la información crítica de la pantalla.

### 6. Áreas táctiles y tipografía

> **Guía — Accesibilidad:** objetivo táctil mínimo 44×44 pt en móvil, con separación suficiente.
> **Guía — Tipografía:** evite pesos ligeros; respete el tamaño de texto del sistema.

- `Spacing.minTouchTarget = 48.dp` aplicado a los selectores Conforme / No conforme, botones de
  cambio de estado del hallazgo, chips de filtro y botón de retroceso.
- Escala tipográfica con la fuente del sistema, en `sp` (escala con el ajuste del dispositivo),
  cuerpo de 17 sp, mínimo de 12 sp para metadatos, y sin pesos por debajo de `Normal`.
- Ritmo de espaciado en múltiplos de 4 dp, con margen lateral de 20 dp.

### 7. Convenciones de plataforma

> **Guía — Navegación móvil:** la navegación primaria va en una barra inferior, no en un menú
> lateral oculto.

Tres destinos (Recorridos · Hallazgos · Normas) en `NavigationBar`. La pantalla de captura se
presenta como pila completa **sin** barra inferior: es una tarea de un solo hilo y no conviene
invitar a abandonarla a media inspección. Edge-to-edge con `WindowInsets` gestionados por el
`Scaffold`.

### 8. Retroalimentación en lugar de botones muertos

> **Guía — Retroalimentación:** los errores necesitan una ruta de recuperación clara.

El botón de cierre **no se deshabilita en silencio**. Siempre está activo; al tocarlo con el
formulario incompleto revela los errores de evidencia en las tarjetas afectadas, y sobre el botón
hay una línea permanente que dice exactamente qué falta («Faltan 3 puntos por evaluar», «2
hallazgos requieren nota y fotografía»). Un botón gris sin explicación deja al inspector adivinando.

### 9. Modalidad usada con moderación

> **Guía — Modalidad:** use la modalidad con moderación, y confirme las acciones importantes.

Sólo dos hojas modales: la confirmación de cierre del recorrido (que resume puntaje, hallazgos,
estado resultante y coordenadas antes de escribir en la base) y el detalle de una norma. El resto
del flujo es navegación en pila.

---

## Mejoras pendientes (no bloqueantes)

| Prioridad | Punto | Propuesta |
|---|---|---|
| Media | La lista de puntos de control no anuncia cambio de estado a TalkBack | Añadir `liveRegion` al panel de estado normativo para que el paro crítico se locute al dispararse |
| Media | Sin persistencia del borrador | Guardar el recorrido en curso en Room para sobrevivir a la muerte del proceso |
| Baja | Tablets y horizontal | Diseño de dos paneles (lista + detalle) por encima de 600 dp de ancho |
| Baja | Reinspección | Notificación local al vencer el plazo de 24 h |
