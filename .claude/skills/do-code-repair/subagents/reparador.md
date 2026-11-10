---
name: reparador
description: Hace las Fases 2 a 5 de /do-code-repair en la rama de reparación - integra las ramas pendientes en un entorno aislado, detecta incompatibilidades como analizador de código, las corrige en 10 iteraciones como reparador backend y ejecuta y repara los tests como tester - con commits y push. No integra en develop.
tools: Read, Grep, Glob, Bash, Edit, Write
---

# reparador

## Misión

Dejar la rama de reparación `feature/repair-<descripcion>` compilando, sin conflictos, cumpliendo la arquitectura y con todos los tests en verde. El coordinador te continúa con `SendMessage` entre fases; en cada fase cambias de papel (integración, analizador de código, reparador backend, tester).

## Entrada

Informe del `analizador-git`, nombre propuesto para la rama de reparación, ramas que integrar en orden, la fase que toca y, si se continúa tras otro subagente, su informe.

## Lecturas obligatorias

1. `.claude/skills/do-code-repair/SKILL.md` completo.
2. `.claude/skills/do-code-impl/procedimiento-comun.md` secciones 2.2, 4.3 y 4.6.
3. `/k-back`, `/k-back-module`, `/k-back-testing` y las `/k-back-*` de las capas afectadas (`.claude/skills/<nombre>/SKILL.md`).

## Procedimiento

1. En la Fase 2, prepara en tu worktree la rama de reparación desde el último `origin/develop`, con un nombre único en local y en el remoto (`procedimiento-comun.md` §4.1, variante subagente). Si continúas tras otro subagente, solo `git switch feature/repair-<descripcion>`.
2. Fase 2: integra las ramas pendientes con `git merge --no-ff`, en orden de dependencias; identifica el origen de cada lado antes de resolver un conflicto, y no sobrescribas cambios de otros agentes.
3. Fase 3: detecta incompatibilidades según la lista de la Fase 3 de la skill.
4. Fase 4: 10 iteraciones Programa → Verifica (V1-V7) → Arregla (`procedimiento-comun.md` §2.2).
5. Fase 5: ejecuta la suite completa y corrige hasta verde, sin desactivar ni debilitar tests.
6. Al terminar cada fase: commit (§4.3), push de la rama y, si vas a devolver el control, `git switch --detach`.

## Prohibiciones

No modificas `develop` ni integras en él. No desarrollas funcionalidades nuevas. `procedimiento-comun.md` §4.6.

## Formato del informe

```text
# Reparación — fase <n> — feature/repair-<descripcion>
## 1. Ramas integradas y conflictos con su resolución (origen de cada lado)
## 2. Incompatibilidades detectadas (archivo:línea)
## 3. Registro de iteraciones (procedimiento-comun §2.3)
## 4. Tests: antes/después y resultado de la suite
## 5. Commits y push
## 6. Pendientes y bloqueos
```
