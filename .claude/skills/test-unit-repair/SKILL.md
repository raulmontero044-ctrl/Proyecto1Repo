---
name: test-unit-repair
description: Analiza y repara los tests unitarios del backend Java/Spring Boot de Facturación con cinco reparaciones independientes en worktrees sobre la misma base, cada una falsada con code-falsador.md por un test-falsifier, elige la mejor con un test-evaluator y la integra en develop comprobando que no se pierde ningún test. No modifica producción para que los tests pasen. Úsala cuando el usuario pida reparar o reforzar tests unitarios; funciona sola o desde /do-test. Hace commits, push e integración.
argument-hint: "[rama base a reparar (por defecto origin/develop)] [alcance, opcional]"
disable-model-invocation: true
---

# /test-unit-repair

## Propósito

Ejecutar la fase REPAIR del tipo `unit`: mejorar la fiabilidad de los tests, eliminar falsos positivos y reforzar aserciones. El procedimiento es `.claude/skills/do-test/procedimiento-testing.md`; el alcance del tipo, `.claude/skills/do-test/tipos/unit.md`; las normas de testing, `/k-back-testing`. Esta skill NO los repite.

REPAIR NO significa cambiar el código de producción para que los tests pasen (procedimiento sección 5.3).

Los términos DEBE, NO DEBE y PUEDE son normativos.

## Modos

- **Sola** (`/test-unit-repair` invocada por el usuario): repara los tests existentes del tipo partiendo de `origin/develop` o de la rama indicada en `$ARGUMENTS` (procedimiento sección 7.1).
- **Desde `/do-test`**: recibe `<base>`, `B1`, el informe del analizador, el informe de falsación de la ganadora CREATE, el informe de evaluación CREATE y las autorizaciones; omite los pasos 1 a 3.

## Entradas

- `$ARGUMENTS` (opcional): rama base y alcance. Si la rama no existe o no está publicada, DEBE preguntarse al usuario.

## Procedimiento

Se ejecuta en el hilo principal (procedimiento sección 1.2).

1. **Análisis**: lanza un `test-analyzer` de tipo `unit` sobre el commit base y aplica la puerta de decisión (procedimiento sección 3).
2. **Base**: lanza el subagente de preparación que crea y publica `<base>` = `feature/test-unit-<descripcion>` en ese commit (`B1`) (procedimiento secciones 2.2 y 7.1).
3. **Falsación de partida**: lanza un `test-falsifier` sobre `B1` con alcance «tests existentes del tipo `unit`» (procedimiento sección 5.1).
4. **Ramas candidatas**: lanza el subagente de preparación que crea `<base>-repair-1` … `<base>-repair-5` en `B1`.
5. **Reparaciones**: lanza en un mismo mensaje cinco `test-repairer` con `isolation: "worktree"` (procedimiento secciones 1.6, 1.8 y 5.2).
6. **Falsación**: lanza en un mismo mensaje un `test-falsifier` con `isolation: "worktree"` por reparación entregada, con el informe de falsación de partida (procedimiento sección 5.4).
7. **Evaluación**: lanza un `test-evaluator` en fase REPAIR (procedimiento secciones 5.5 y 6). Si no hay ganadora aceptable, detente e informa sin integrar.
8. **Integración final**: lanza un `test-integrator` en modo `final` (procedimiento sección 8.2), con `B1` y la línea base del analizador como referencias de no regresión.
9. **Defectos de producción**: preséntalos al usuario separados de los cambios de testing; su corrección requiere autorización expresa.
10. **Informe** según el procedimiento sección 9 (apartados de REPAIR).

## Comprobaciones antes de finalizar

- [ ] Las cinco reparaciones parten del mismo `B1`, en paralelo, cada una en su rama y worktree.
- [ ] Cada reparación tiene su informe de `test-falsifier`, con el estado de los hallazgos de partida.
- [ ] Una única ganadora justificada por el `test-evaluator`.
- [ ] Ningún test eliminado, desactivado ni debilitado respecto a `B1` y a la línea base (procedimiento sección 8.3).
- [ ] `src/main` sin cambios salvo commits `fix:` autorizados y separados.
- [ ] `<base>` integrada en `develop` y `develop` local igual a `origin/develop`.
