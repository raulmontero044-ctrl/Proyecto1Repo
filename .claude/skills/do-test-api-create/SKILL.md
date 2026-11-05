---
name: do-test-api-create
description: Crea tests de API e integración (capa HTTP y, si se autoriza, persistencia e integración con base de datos) del backend Java/Spring Boot de Facturación con cinco propuestas independientes en worktrees, cada una falsada con code-falsador.md por un test-falsifier, elige la mejor con un test-evaluator y la integra en una rama feature/test-api-*. Úsala cuando el usuario pida crear tests de API e integración (capa HTTP y, si se autoriza, persistencia e integración con base de datos); funciona sola o desde /do-test. Hace commits, push e integración.
argument-hint: "[requisitos o alcance, opcional]"
disable-model-invocation: true
---

# /do-test-api-create

## Propósito

Ejecutar la fase CREATE del tipo `api`. El procedimiento es `.claude/skills/do-test/procedimiento-testing.md`; cómo se hace cada test del tipo, `/k-back-test-api`; las normas de testing, `/k-back-testing`. Esta skill NO los repite.

Los términos DEBE, NO DEBE y PUEDE son normativos.

## Modos

- **Sola** (`/do-test-api-create` invocada por el usuario): ejecuta todos los pasos y termina integrando en `develop` (procedimiento sección 7.3).
- **Desde `/do-test`**: recibe el informe del analizador y las autorizaciones, omite el paso 1 y termina en el paso 6 entregando `<base>` y `B1` (procedimiento sección 7.2).

## Entradas

- `$ARGUMENTS` (opcional): requisitos o alcance que acotan los comportamientos de `/k-back-test-api`. NO DEBEN inventarse requisitos: si el alcance es ambiguo, se pregunta.

## Procedimiento

Se ejecuta en el hilo principal (procedimiento sección 1.2).

1. **Análisis**: lanza un `test-analyzer` de tipo `api` sobre `origin/develop` y aplica la puerta de decisión (procedimiento sección 3). Este tipo DEBE preguntar al usuario el alcance de infraestructura de `/k-back-test-api` sección 3 antes del paso 2.
2. **Ramas**: lanza el subagente de preparación que crea `<base>` = `feature/test-api-<descripcion>` y `<base>-create-1` … `<base>-create-5` en `B0` (procedimiento sección 2.2).
3. **Propuestas**: lanza en un mismo mensaje cinco `test-creator` con `isolation: "worktree"` (procedimiento secciones 1.6, 1.8 y 4.1).
4. **Falsación**: lanza en un mismo mensaje un `test-falsifier` con `isolation: "worktree"` por propuesta entregada (procedimiento sección 4.2).
5. **Evaluación**: lanza un `test-evaluator` en fase CREATE (procedimiento secciones 4.3 y 6). Si no hay ganadora aceptable, detente e informa.
6. **Integración en `<base>`**: lanza un `test-integrator` en modo `base` (procedimiento sección 8.1) y anota `B1`. En modo «desde `/do-test`», devuelve aquí el control.
7. **Integración en `develop`** (solo modo sola): lanza un `test-integrator` en modo `final` que omite el merge de una candidata y aplica los pasos 3 a 6 de la sección 8.2 a `<base>`.
8. **Informe** según el procedimiento sección 9 (apartados de CREATE).

## Comprobaciones antes de finalizar

- [ ] Cinco `test-creator` lanzados en paralelo desde `B0`, cada uno en su rama y worktree.
- [ ] Un informe de `test-falsifier` por propuesta entregada, siguiendo `code-falsador.md`.
- [ ] Una única ganadora justificada por el `test-evaluator`, sin descalificaciones pendientes.
- [ ] `<base>` publicada, en verde y sin regresiones (procedimiento sección 8.3).
- [ ] Sin dependencias, configuración ni cambios en `src/main` no autorizados.
- [ ] Modo sola: `<base>` integrada en `develop` y `develop` local igual a `origin/develop`.
