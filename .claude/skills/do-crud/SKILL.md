---
name: do-crud
description: Implementa un CRUD completo de un recurso con subagentes - lee /k-crud como especificación, analiza el proyecto, planifica, implementa backend y, si existe, frontend en worktrees propios, ejecuta la auditoría obligatoria de /k-crud (al menos cinco ciclos de revisión y reparación, uno por subagente), genera los tests con /do-test, verifica e integra en develop. Úsala cuando el usuario pida crear el CRUD de un recurso. Hace commits, push e integración.
argument-hint: "<recurso> [módulo] [requisitos o ruta a su especificación]"
disable-model-invocation: true
---

# /do-crud

## Propósito

Implementar las cuatro operaciones CRUD de un recurso (backend y, si existe, frontend), revisadas por `/k-crud`, con tests e integradas en `develop`. Esta skill coordina: el trabajo lo hacen subagentes. QUÉ criterios debe cumplir un CRUD es de `/k-crud`; esta skill NO DEBE definir criterios de arquitectura o calidad propios que la contradigan.

Los términos DEBE, NO DEBE y PUEDE son normativos.

## Cuándo usarla

- El usuario pide el CRUD de un recurso nuevo o completar las operaciones que le faltan a uno existente.
- Si el recurso exige un módulo de negocio nuevo (`/k-back-module` §1), DEBE informarse y proponerse antes `/do-code-module`; si se acepta, el módulo se crea con esa skill y luego se sigue con esta.
- Para auditar un CRUD ya implementado sin añadir nada, se usa `/k-crud`.

## Entradas

- `$ARGUMENTS`: recurso, módulo y requisitos, o ruta a su especificación.
- Requisitos imprescindibles: recurso y módulo; campos con tipo, obligatoriedad y límites; campos no modificables; reglas e invariantes; operaciones pedidas (con PUT o PATCH para la modificación); restricciones al borrar; permisos si hay autenticación; relaciones con otros módulos.

Si falta alguno, DEBE preguntarse al usuario antes de crear código, listando exactamente lo que falta. NO DEBEN inventarse campos, reglas, endpoints ni dependencias.

## Resultado esperado

- Rama `feature/crud-<recurso>` (y `feature/crud-<recurso>-front` si hay frontend) con el CRUD implementado, auditado por `/k-crud` y con tests, integrada en `develop`.
- Informe final (paso 10).

## Skills y archivos relacionados

| Recurso | Ruta | Para qué |
|---|---|---|
| `/k-crud` | `.claude/skills/k-crud/SKILL.md` | Especificación obligatoria (secciones 1 a 6) y auditoría (secciones 7 y 8) |
| `/k-back` y `/k-back-*` de capa | `.claude/skills/<nombre>/SKILL.md` | Normas propietarias que `/k-crud` referencia |
| Procedimiento común | `.claude/skills/do-code-impl/procedimiento-comun.md` | Modos, subagentes (sección 1) y flujo Git (sección 4) |
| `/do-test` | `.claude/skills/do-test/SKILL.md` | Generar y reparar los tests por tipo |
| `/k-back-test-<tipo>` | `.claude/skills/k-back-test-<tipo>/SKILL.md` | Qué cubre cada tipo de test |
| `/do-code-module` | `.claude/skills/do-code-module/SKILL.md` | Solo si el recurso exige un módulo nuevo |

## Subagentes

El hilo principal es el coordinador: NO DEBE implementar, revisar ni verificar código directamente, sino lanzar el subagente de cada paso, supervisar su informe y decidir el siguiente. Todos son `general-purpose`; los que modifican archivos se lanzan con `isolation: "worktree"` (`procedimiento-comun.md` §1.3).

| Paso | Subagente | Worktree | Paralelismo |
|---|---|---|---|
| 0 | Preparación Git | No | — |
| 2 | Analizador de solo lectura | No | — |
| 4 | Implementador backend | Sí | En paralelo con el 5 |
| 5 | Implementador frontend (si existe) | Sí | En paralelo con el 4 |
| 6 | Verificador de integración de solo lectura | No | — |
| 7 | Un subagente por ciclo de `/k-crud` | Sí | Secuencial |
| 8 | Agentes `test-*` lanzados según `/do-test` | Según `/do-test` | Según `/do-test` |
| 9 | Verificador final de solo lectura | No | — |
| 9 | Integrador | Sí | — |

El prompt de cada subagente DEBE incluir: tarea y requisitos sin resumir los que afecten a campos, endpoints o reglas; rama y base; «modo subagente»; rutas de este archivo, de `.claude/skills/k-crud/SKILL.md` y de `procedimiento-comun.md`; las `/k-back-*` que debe leer; el informe del analizador y el plan del paso 3; y el formato de informe que debe devolver.

Los subagentes NO PUEDEN lanzar subagentes: por eso los ciclos de `/k-crud` y `/do-test` los coordina el hilo principal. Si falta la herramienta `Agent`, se aplica `procedimiento-comun.md` §1.5: los pasos se hacen sin delegar, se informa de ello y NO DEBEN simularse subagentes.

## Procedimiento

### 0. Modo y Git

Determina el modo (`procedimiento-comun.md` §1.2). Lanza el subagente de preparación (§4.1), que crea `feature/crud-<recurso>` y, si hay frontend, `feature/crud-<recurso>-front`, ambas desde `origin/develop`.

### 1. Leer /k-crud

El coordinador DEBE leer íntegro `.claude/skills/k-crud/SKILL.md` antes de planificar. Sus secciones 1 a 6 son la especificación de todo lo que se implementa; las secciones 7 y 8, la revisión del paso 7.

### 2. Análisis previo

Lanza el analizador. DEBE cubrir `/k-crud` §1 y, además:

1. El módulo en el que va el CRUD y si el recurso ya existe.
2. Sus relaciones con otros módulos y el sentido de las dependencias.
3. Los CRUD similares del proyecto y sus contratos (rutas, DTO, códigos, manejadores).
4. Las operaciones ya implementadas del recurso.
5. Si hay frontend, su framework y convenciones.
6. La siguiente versión de migración Flyway (listando `src/main/resources/db/migration`).
7. La línea base de `./mvnw -B test` (tests, clases y resultado).

NO DEBE presuponerse ninguna arquitectura: se adopta la que detecte el análisis. Si la línea base no está en verde, se detiene e informa.

### 3. Planificación

Antes de modificar código, el coordinador DEBE registrar un plan con estos elementos, cada uno marcado como «existente», «nuevo», «modificar» o «no aplica» (con su motivo):

| Elemento | Detalle |
|---|---|
| Entidad y modelo de dominio | Campos, invariantes y métodos de cambio |
| DTO de entrada y salida | Creación, actualización (PUT o PATCH) y respuesta |
| Casos de uso | Uno por operación |
| Controlador y endpoints | Verbo, ruta y códigos |
| Servicios | Solo si se justifican (`/k-back-service` §7) |
| Repository y DAO | Operaciones necesarias |
| Persistencia | Entidad, mapper, migración `V<n+1>__<descripcion>.sql` |
| Validaciones | DTO y dominio |
| Manejo de errores | Excepciones de dominio y sus handlers |
| Frontend | Servicio, modelos y componentes, si existe |
| Pruebas | Tipos de `/do-test` previstos |

El plan DEBE fijar el contrato HTTP completo (rutas, DTO con nombres y tipos de campo, códigos y errores) antes de lanzar los implementadores, porque backend y frontend trabajan en paralelo sobre él. NO DEBEN duplicarse componentes que ya existen. Si el plan contiene una decisión no derivada de los requisitos, se documenta (`procedimiento-comun.md` §5).

### 4. Implementación del backend

Lanza el implementador backend en `feature/crud-<recurso>`. DEBE:

- Implementar CREATE, READ, UPDATE y DELETE según `/k-crud` §2 a §4 y §6, en todas las capas que correspondan a la etapa vigente.
- Separar responsabilidades: nada de lógica en el controlador, invariantes en el dominio, DTO y mappers donde corresponda.
- Hacer las verificaciones V1-V7 de `/k-back-testing` tras cada unidad lógica.
- Hacer un commit por unidad lógica (`procedimiento-comun.md` §4.3) y push de la rama.
- Devolver: clases creadas y modificadas, endpoints, migración, verificaciones y commits.

### 5. Implementación del frontend

Si el analizador detectó frontend, lanza en paralelo con el paso 4 el implementador frontend en `feature/crud-<recurso>-front`. Implementa listado, consulta individual si corresponde, formularios de creación y edición, eliminación, servicio HTTP, validaciones, estados de carga, errores y actualización de la vista, según `/k-crud` §5 y las convenciones existentes. NO DEBE crear componentes genéricos innecesarios. Si no hay frontend, se registra «no aplica».

### 6. Integración backend-frontend

Con las dos ramas publicadas, lanza el verificador de integración de solo lectura. DEBE comprobar:

- Que frontend y backend usan los mismos contratos.
- Que los nombres y tipos de los campos coinciden.
- Que los endpoints y los códigos HTTP son correctos y coherentes.
- Que las validaciones funcionan en ambos extremos cuando corresponde.
- Que los errores se comunican correctamente y las operaciones actualizan la información.

Las discrepancias se corrigen relanzando el implementador correspondiente. Sin frontend, este paso se limita a contrastar el contrato del backend con el plan.

### 7. Revisión con /k-crud

El coordinador DEBE seguir `/k-crud` §7 y §8.1 en el hilo principal sobre `feature/crud-<recurso>` (y la rama de frontend, si existe):

- Al menos **5 ciclos completos**, cada uno en un subagente nuevo, secuenciales, con su enfoque y su registro.
- Los ciclos corrigen directamente los incumplimientos.
- Se aplica la regla de continuidad: tras el quinto ciclo se sigue mientras queden errores conocidos corregibles; los no corregibles se documentan como bloqueos.
- La preparación y la integración de `/k-crud` NO se ejecutan aquí: las hacen los pasos 0 y 9.

La implementación NO DEBE considerarse terminada hasta completar esta fase sin errores conocidos corregibles.

### 8. Testing

8.1. El coordinador DEBE preguntar con `AskUserQuestion` (multiSelect) qué tipos ejecutar: `unit`, `arch`, `api` y `e2e`. Cada opción lleva su descripción de una línea, tomada de `/k-back-test-<tipo>`, y una recomendación según lo que se ha cambiado (normalmente `unit` y `api`; `arch` si se han tocado paquetes o dependencias entre módulos). `e2e` y Testcontainers requieren autorización expresa (`/k-back-test-e2e`).

8.2. Por cada tipo elegido, DEBE leerse `.claude/skills/do-test/SKILL.md` (y lo que referencia) y seguirse en el hilo principal con ese tipo como argumento y, como alcance, el CRUD implementado. `/do-test` lanza sus propios agentes `test-*` e integra en `develop`; por eso, antes de empezar los tests, DEBE integrarse en `develop` la rama del CRUD (paso 9.2), para que los tests partan de ella.

8.3. Los tests DEBEN verificar el comportamiento esperado de los requisitos, no copiar la implementación (`/k-back-testing` §8). Si un agente de testing documenta defectos de producción, se corrigen en la rama del CRUD con un ciclo adicional de `/k-crud` antes de cerrar.

### 9. Verificación final e integración

9.1. Lanza el verificador final de solo lectura, que DEBE comprobar con sus resultados reales:

- Compilación del backend y `./mvnw -B verify` en verde.
- Compilación del frontend y sus tests, si existe.
- `ArquitecturaTest`, `Regla*Test` y `ModulosTest` si existe.
- Que los endpoints y contratos coinciden con el plan.
- Las cuatro operaciones CRUD.
- Que el número de tests no ha bajado respecto a la línea base.

Los errores encontrados se corrigen relanzando el implementador o un ciclo de `/k-crud`, y se verifica de nuevo.

9.2. Lanza el integrador (`procedimiento-comun.md` §4.4 y §4.5) para integrar `feature/crud-<recurso>` y, después, `feature/crud-<recurso>-front` en `develop`. Ocurre antes del paso 8 (8.2) y de nuevo al final si hubo correcciones posteriores.

### 10. Informe final

1. CRUD implementado (recurso, módulo, etapa).
2. Archivos creados.
3. Archivos modificados.
4. Operaciones implementadas.
5. Endpoints disponibles, con verbo, ruta y códigos.
6. Componentes frontend implementados, o «no aplica».
7. Validaciones y reglas de negocio.
8. Pruebas ejecutadas por tipo y sus resultados, con el número de tests antes y después.
9. Resultado de los ciclos de `/k-crud`: registro completo (`/k-crud` §7.4) y motivo de parar.
10. Problemas pendientes y bloqueos, si existen.

Se añaden: subagentes lanzados (paso, rama y resultado), decisiones arquitectónicas (`procedimiento-comun.md` §5) e informe Git (§4.7).

## Comprobaciones antes de finalizar

- [ ] `/k-crud` se ha leído íntegra antes de planificar, y ningún criterio aplicado la contradice.
- [ ] Ningún campo, regla, endpoint ni dependencia se ha inventado; las dudas se preguntaron.
- [ ] El plan se registró con el contrato HTTP fijado antes de implementar, sin duplicar componentes.
- [ ] Cada paso lo ha hecho su subagente (o se ha informado de la ausencia de `Agent`), y el coordinador no ha escrito código.
- [ ] Están las cuatro operaciones, o las que faltan están justificadas por los requisitos.
- [ ] Se han registrado al menos cinco ciclos completos de `/k-crud` y no quedan errores conocidos corregibles.
- [ ] Los tipos de test se eligieron con el usuario y se ejecutaron con `/do-test`.
- [ ] La verificación final está en verde y el número de tests no ha bajado.
- [ ] Commits con `Co-Authored-By`, push e integración en `develop` hechos, sin forzar ni borrar ramas.
