---
name: do-code-basic
description: Aplica la Etapa 1 (arquitectura básica en capas, sin módulos) al backend Java/Spring Boot - organiza controller, apln, domain, persistence y DTO según la tabla canónica, o, si el proyecto ya está en una etapa posterior (Facturación está en Etapa 2), audita y corrige la separación de capas sin desmontar módulos. Úsala cuando el usuario pida estructurar en capas o auditar las capas. Hace commits y push.
argument-hint: "[alcance o paquete a tratar, opcional]"
disable-model-invocation: true
---

# /do-code-basic

## Propósito

Llevar el backend a la Etapa 1 del modelo de etapas de `/k-back` (sección 2) o, si ya está en una etapa posterior, garantizar dentro de la estructura existente los principios de separación de capas de la Etapa 1. El procedimiento (modos, iteraciones, tests, Git) es el común; las normas son las de las `k-back-*`, que esta skill NO repite.

## Cuándo usarla

- El proyecto no tiene capas separadas (lógica en controladores, entidades expuestas, acceso a datos desde la capa web) y se quiere la arquitectura básica.
- Se quiere auditar y corregir la separación de capas de un proyecto en Etapa 2 o 3 sin cambiar de etapa.
- Como primera transformación de un plan en `/do-code-impl`.

## Entradas

- `$ARGUMENTS` (opcional): alcance (paquete, clases o tema). Si está vacío, se trata todo `src/main/java` y `src/test/java`.

## Resultado esperado

- Etapa 1 o 0→1: código en `<raiz>.{controller,apln,service,domain.model,domain.repository,persistence.{dao,entity,mapper}}` y todos los DTO `record` en `<raiz>.api.dto` (decisión D4 del coordinador), con `ArquitecturaTest` en verde.
- Etapa 2 o 3: misma estructura de módulos, con las violaciones de separación de capas corregidas e informadas.
- Rama feature publicada; integrada en `develop` en modo coordinador.

## Skills relacionadas

Procedimiento: `.claude/skills/do-code-impl/procedimiento-comun.md`.

Skills de conocimiento que DEBEN leerse (`.claude/skills/<nombre>/SKILL.md`) y pasarse a los subagentes:

| Skill | Para qué |
|---|---|
| `/k-back` | Tabla canónica de la Etapa 1, reglas de evolución, dependencias entre capas, nombres |
| `/k-back-domain` | Modelo, invariantes, excepciones y servicios de dominio puros |
| `/k-back-repository` | Interfaces Repository |
| `/k-back-persistence` | Entidades, DAO, mappers, implementaciones de Repository, Flyway |
| `/k-back-service` | Casos de uso y límite transaccional |
| `/k-back-controller` | Controladores y manejo de excepciones HTTP |
| `/k-back-dto` | DTO `record` y su ubicación |
| `/k-back-testing` | V1-V7, tests por capa, `ArquitecturaTest` |

Siguiente transformación: `/do-code-modular`.

## Procedimiento

### 0. Modo y Git

Determina el modo (procedimiento común, sección 1) y prepara la rama `feature/<nombre-descriptivo>` (p. ej. `feature/capas-basicas` o `feature/auditoria-capas`) según la sección 4.1.

### 1. Detectar la etapa

Inspecciona los paquetes reales con los criterios de `/k-back` sección 2:

- Sin paquetes de capa o capas mezcladas → **modo transformación** (Etapa 0 → 1).
- Paquetes de capa directamente bajo `<raiz>` → Etapa 1 → **modo auditoría** dentro de la Etapa 1.
- Paquetes `<raiz>.<modulo>.{api,apln,controller,domain,persistence}` → Etapa 2 → **modo auditoría** sin desmontar módulos.
- Paquetes `<modulo>.api.*` / `<modulo>.internal.*` → Etapa 3 → **modo auditoría** sin desmontar módulos.

Facturación está en Etapa 2 (`com.facturacion.factura`): esta skill DEBE trabajar en modo auditoría, NO DEBE mover clases fuera de `factura` y DEBE informar al usuario de que la estructura de la Etapa 1 no se aplica literalmente porque nunca se retrocede de etapa.

### 2. Planificar las 10 unidades (Fase A)

Planifica y registra las 10 unidades según el procedimiento común (sección 2.1).

**Modo transformación** — unidades orientativas, en este orden:

1. Inventario de clases y asignación de cada una a su capa de destino (sin cambios de código; si no hay nada que mover en esta unidad, se combina con la 2).
2. Modelo de dominio e invariantes en `domain.model` con sus tests.
3. Excepciones de dominio y servicios de dominio puros.
4. Interfaces Repository en `domain.repository`.
5. Entidades y DAO en `persistence.entity` / `persistence.dao`.
6. Mappers e implementaciones de Repository en `persistence`.
7. DTO `record` en `<raiz>.api.dto`.
8. Casos de uso en `apln` con su límite transaccional.
9. Controladores y manejador de excepciones en `controller`, delegando en `apln`.
10. `ArquitecturaTest` con las reglas de la Etapa 1 y limpieza final.

**Modo auditoría (Facturación, Etapa 2)** — una unidad por área auditada; cada una revisa contra su skill, corrige las violaciones encontradas y añade, o adapta sin debilitar (`/k-back-testing` sección 8), el test que la cubra:

1. Dominio puro en `factura.domain.model` (`Factura`, `LineaFactura`, `CalculadoraFactura`, excepciones).
2. Invariantes en el modelo y no en controladores, DAO ni mappers.
3. Contrato `FacturaRepository` en `factura.domain.repository`.
4. Entidades `FacturaEntity` / `LineaFacturaEntity` y `FacturaDao` confinados a `persistence`.
5. `FacturaMapper` y `JpaFacturaRepository`.
6. DTO `record` de `factura.api.dto` y su uso (nada de dominio ni entidades expuestos).
7. Casos de uso de `factura.apln` (`CrearFactura`, `ObtenerFactura`, `ListarFacturas`, `EliminarFactura`, `FacturaDtoConverter`) y transacciones.
8. `FacturaController` y `FacturaExceptionHandler`: solo HTTP, delegación en `apln`, `ProblemDetail`.
9. Reglas de `ArquitecturaTest` frente a las dependencias reales; ninguna se relaja.
10. Revisión transversal de `/k-back` (inyección por constructor, nombres, comentarios, secretos, logging) en todo el código.

Si una unidad no encuentra ninguna violación, se documenta como verificada sin cambios. Si el resultado global es que la auditoría no da para 10 unidades con trabajo real, DEBE informarse según el procedimiento común (sección 2.1.4), sin fabricar cambios.

### 3. Ejecutar la Fase A

Ejecuta las 10 iteraciones Programa → Verifica (V1-V7 de `/k-back-testing`) → Arregla según el procedimiento común (sección 2.2), con un commit por unidad con cambios.

### 4. Ejecutar la Fase B

Según el procedimiento común (sección 3). En modo auditoría, cada violación corregida DEBE quedar cubierta por un test que fallaría sin la corrección (`/k-back-testing`).

### 5. Publicar e integrar

Push de la rama (sección 4.4). En modo coordinador, lanza el subagente integrador (sección 4.5); en modo subagente, entrega la rama al coordinador sin integrar.

### 6. Informe

Informe final según el procedimiento común (sección 6), añadiendo:

- Etapa detectada y modo (transformación o auditoría), con el aviso de no retroceso si aplica.
- Tabla de hallazgos de auditoría: área, regla y skill propietaria, clase afectada, corrección, test que la cubre.

## Comprobaciones antes de finalizar

- [ ] La etapa se ha detectado inspeccionando paquetes y no se ha retrocedido de etapa ni desmontado módulos.
- [ ] Las 10 iteraciones están registradas con V1-V7, o se ha informado de que no había trabajo real para 10.
- [ ] Todas las normas aplicadas se han tomado de su skill propietaria; esta skill no ha introducido normas propias.
- [ ] Los DTO están en `<raiz>.api.dto` (Etapa 1) o en la ubicación de la etapa actual.
- [ ] No se ha perdido ninguna funcionalidad, endpoint, regla ni test; el número de tests no ha bajado.
- [ ] La Fase B y la suite completa están ejecutadas con su resultado real.
- [ ] Commits con `Co-Authored-By`, push hecho e integración hecha (modo coordinador) o entregada al coordinador (modo subagente).
