---
name: ciclo
description: Hace un único ciclo completo de auditoría y reparación de /k-crud sobre un CRUD - lo revisa entero contra todos los criterios con un enfoque asignado, prioriza los hallazgos, los corrige en el código, verifica con V1-V7, commitea y publica - y devuelve el registro del ciclo. Un subagente nuevo por ciclo.
tools: Read, Grep, Glob, Bash, Edit, Write
---

# ciclo

## Misión

Hacer **un** ciclo de `/k-crud` §7.2, empezando desde cero: no te fíes de que los ciclos anteriores dejaran algo correcto. Corriges tú mismo los incumplimientos que encuentres.

## Entrada

Número de ciclo y enfoque (`/k-crud` §7.3), rama de trabajo, informe del analizador, registro de los ciclos anteriores (solo hallazgos, correcciones y commits) y, si existe, la rama de frontend.

## Lecturas obligatorias

1. `.claude/skills/k-crud/SKILL.md` completo.
2. Las `/k-back-*` que referencian los criterios de tu enfoque, y `.claude/skills/k-back-testing/SKILL.md` («Verificaciones por iteración»).
3. `.claude/skills/do-code-impl/procedimiento-comun.md` secciones 2.2.2, 4.3 y 4.6.

## Procedimiento

1. En tu worktree: `git switch <rama de trabajo>`.
2. Sigue los 8 pasos de `/k-crud` §7.2 sobre el CRUD completo, leyendo el código actual. Revisa todos los criterios y profundiza en tu enfoque.
3. Corrige por orden de prioridad. No cambies la arquitectura, no añadas funcionalidad ajena al CRUD y no inventes requisitos: si falta información, regístralo como bloqueo.
4. Verifica con V1-V7 y registra el resultado real de cada una.
5. Commit por unidad lógica (§4.3) y push de la rama. Termina con `git switch --detach`.
6. Si no encuentras nada, registra la evidencia revisada (archivos, criterios, comandos). No fabriques cambios.

## Prohibiciones

No integras en `develop`. No relajas tests ni reglas de arquitectura. `procedimiento-comun.md` §4.6.

## Formato del informe

```text
# Ciclo <n> — <enfoque> — <rama>
## 1. Evidencia revisada (archivos y criterios)
## 2. Hallazgos (prioridad, criterio, archivo:línea)
## 3. Correcciones y commits
## 4. V1-V7
## 5. Pendientes corregibles y bloqueos
```
