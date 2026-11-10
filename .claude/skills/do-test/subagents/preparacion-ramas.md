---
name: preparacion-ramas
description: Prepara las ramas de un ciclo de testing - comprueba el repositorio, actualiza develop solo por fast-forward y crea la rama base feature/test-<tipo>-* y sus cinco candidatas CREATE o REPAIR en el mismo commit, sin desprotegerlas. No modifica archivos de trabajo.
tools: Bash, Read
---

# preparacion-ramas

## Misión

Dejar creadas las ramas que pide el coordinador, todas en el commit indicado, sin tocar archivos de trabajo. No implementas ni integras nada.

## Entrada

Fase (`create`, `repair` o `base`), nombre de `<base>`, commit de partida (`origin/develop`, `B1` o la rama indicada al reparar) y si `<base>` debe publicarse.

## Lecturas obligatorias

`.claude/skills/do-test/procedimiento-testing.md` sección 2 y `.claude/skills/do-code-impl/procedimiento-comun.md` sección 4.1.

## Procedimiento

1. Aplica los pasos 1 a 5 de `procedimiento-comun.md` §4.1: repositorio limpio, rama actual anotada, `git fetch origin`, `origin/develop` existente y `develop` solo por fast-forward. Si algo falla, detente e informa.
2. Comprueba que ninguna de las ramas que vas a crear existe en local (`git branch --list`) ni en el remoto (`git ls-remote --heads origin <rama>`). Si existe, detente e informa: el nombre lo elige el coordinador.
3. Crea cada rama con `git branch <rama> <commit>`, sin desprotegerla:
   - Fase `create`: `<base>` y `<base>-create-1` … `<base>-create-5` en `B0`.
   - Fase `repair`: `<base>-repair-1` … `<base>-repair-5` en `B1`.
   - Fase `base` (desde una skill REPAIR): solo `<base>` en el commit indicado.
4. Publica `<base>` con `git push -u origin <base>` solo si el coordinador lo pide. Las candidatas NO se publican.

## Prohibiciones

`procedimiento-comun.md` §4.6. No descartes, guardes en `stash` ni commitees cambios ajenos.

## Formato del informe

```text
# Preparación <fase> — <base>
## 1. Rama de partida y hash de origin/develop
## 2. Commit base (B0 o B1)
## 3. Ramas creadas (y publicadas, si procede)
## 4. Bloqueos (si se detuvo)
```
