---
name: do-test
description: Orquesta un ciclo completo de testing del backend Java/Spring Boot de Facturación - pregunta el tipo (unit, arch, api, e2e), analiza el proyecto, ejecuta CREATE con cinco propuestas independientes en worktrees, las falsa con falsador.md, elige una ganadora, ejecuta REPAIR con cinco reparaciones sobre ella, las falsa y evalúa, e integra la mejor en develop. Úsala cuando el usuario pida crear y consolidar tests de un tipo. Hace commits, push e integración.
argument-hint: "[unit | arch | api | e2e] [requisitos o alcance, opcional]"
disable-model-invocation: true
---

# /do-test

## Propósito

Coordinar CREATE y REPAIR de un tipo de testing de principio a fin, delegando en las skills especializadas `/test-<tipo>-create` y `/test-<tipo>-repair` y en los agentes `test-*`. Esta skill NO escribe tests ni repite el procedimiento: lo define `.claude/skills/do-test/procedimiento-testing.md`, y las normas de testing, `/k-back-testing`.

Los términos DEBE, NO DEBE y PUEDE son normativos.

## Cuándo usarla

- El usuario quiere crear tests de un tipo y dejarlos verificados, reparados e integrados.
- Para solo una fase, el usuario PUEDE invocar directamente `/test-<tipo>-create` o `/test-<tipo>-repair`.

## Entradas

- `$ARGUMENTS` (opcional): tipo y requisitos o alcance (p. ej. `api solo los endpoints de factura`).

## Resultado esperado

- Rama `feature/test-<tipo>-<descripcion>` publicada con la ganadora CREATE y, encima, la ganadora REPAIR, integrada en `develop`.
- Ramas candidatas conservadas en local.
- Informe final según el procedimiento sección 9.

## Skills y agentes relacionados

| Recurso | Ruta |
|---|---|
| Procedimiento común | `.claude/skills/do-test/procedimiento-testing.md` |
| Perfiles de tipo | `.claude/skills/do-test/tipos/{unit,arch,api,e2e}.md` |
| Especializadas | `.claude/skills/test-<tipo>-create/SKILL.md`, `.claude/skills/test-<tipo>-repair/SKILL.md` |
| Agentes | `.claude/agents/test-{analyzer,creator,falsifier,evaluator,repairer,integrator}.md` |
| Falsación | `.claude/agents/falsador.md` |
| Normas | `/k-back-testing`, `/k-back` y las `/k-back-*` de capa |
| Git | `.claude/skills/do-code-impl/procedimiento-comun.md` secciones 4.1 y 4.3 a 4.7 |

## Procedimiento

Se ejecuta en el hilo principal (procedimiento sección 1.2).

### Fase 1. Selección del tipo

Si `$ARGUMENTS` no empieza por un tipo válido, DEBE preguntarse con `AskUserQuestion`: «¿Qué tipo de testing quieres realizar?», con las opciones `unit`, `arch`, `api` y `e2e` y una descripción de una línea tomada de cada perfil. NO DEBE empezar ninguna otra fase hasta conocer la respuesta.

### Fase 2. Análisis

Lanza un `test-analyzer` sobre `origin/develop` (procedimiento sección 3). Aplica la puerta de decisión de la sección 3.3 y anota los requisitos y autorizaciones del usuario.

### Fases 3 a 6. CREATE y preparación de REPAIR

Lee `.claude/skills/test-<tipo>-create/SKILL.md` y síguelo en modo «desde `/do-test`», pasándole el informe del analizador y las autorizaciones: preparación de ramas, cinco `test-creator`, cinco `test-falsifier`, un `test-evaluator` y el `test-integrator` en modo `base` (procedimiento secciones 2 y 4). Termina con `<base>` publicada y `B1`.

Si no hay ganadora aceptable o la integración en `<base>` falla, DEBE detenerse e informar: REPAIR no empieza sin `B1`.

### Fases 7 a 10. REPAIR e integración final

Lee `.claude/skills/test-<tipo>-repair/SKILL.md` y síguelo en modo «desde `/do-test`», pasándole `<base>`, `B1`, el informe del analizador, el informe de falsación de la ganadora CREATE y el informe de evaluación CREATE: cinco `test-repairer`, cinco `test-falsifier`, un `test-evaluator` y el `test-integrator` en modo `final` (procedimiento secciones 5 y 8.2).

### Defectos de producción

Si algún agente documenta defectos de producción, DEBEN presentarse al usuario al final, separados de los cambios de testing. Su corrección es una tarea aparte que requiere autorización expresa.

### Informe final

Procedimiento sección 9.

## Comprobaciones antes de finalizar

- [ ] El tipo se ha preguntado (o recibido) antes de lanzar ningún agente.
- [ ] La puerta de decisión de infraestructura se ha aplicado y nadie ha añadido dependencias sin autorización.
- [ ] Se han lanzado exactamente cinco `test-creator` y cinco `test-repairer`, en paralelo, con worktree y desde el mismo commit.
- [ ] Cada propuesta entregada tiene su informe de `test-falsifier`; ninguna se ha dado por buena solo porque sus tests pasan.
- [ ] Cada fase tiene un informe de `test-evaluator` con una única ganadora justificada.
- [ ] REPAIR ha partido de `B1` publicado y en verde.
- [ ] La integración final ha pasado `./mvnw -B verify` y las comprobaciones de no regresión de la sección 8.3.
- [ ] `src/main` no ha cambiado salvo correcciones autorizadas en commits `fix:` separados.
- [ ] No se ha forzado ningún push, no se ha usado `git pull` y no se ha borrado ninguna rama.
