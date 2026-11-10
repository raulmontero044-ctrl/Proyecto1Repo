---
name: do-code-repair
description: Detecta y repara conflictos de integración, incompatibilidades y errores introducidos por varios agentes que han trabajado simultáneamente en el proyecto. Úsala cuando dos o más agentes hayan terminado en ramas feature distintas y haya que dejar develop estable (sin conflictos Git, errores de compilación, incumplimientos arquitectónicos ni tests fallidos). No desarrolla funcionalidades nuevas.
argument-hint: "[ramas o agentes a reparar (opcional; si se omite se descubren con Git)]"
disable-model-invocation: true
---

# /do-code-repair

## 0. Mecanismos de invocación

- Esta skill DEBE ejecutarse en el hilo principal de la conversación, no en un subagente, porque necesita la herramienta `Agent` para lanzar subagentes.
- "Lanza un subagente" significa usar la herramienta `Agent`. Los subagentes que modifiquen archivos DEBEN lanzarse con `isolation: "worktree"` y trabajar sobre la rama de reparación `feature/repair-<descripcion>` (por defecto `feature/repair-conflicts`). Su nombre DEBE comprobarse único en local y en el remoto según `.claude/skills/do-code-impl/procedimiento-comun.md` sección 4.1; si ya existe, se elige otra descripción y se informa.
- Las Fases 2 a 5 las DEBE hacer un único subagente reparador, al que se continúa con `SendMessage` entre fases. Si se usan varios subagentes sucesivos, cada uno DEBE hacer commit y push de la rama de reparación y terminar con `git switch --detach` antes de devolver el control, para que el siguiente worktree pueda desprotegerla.
- El procedimiento común de las ejecutoras (`.claude/skills/do-code-impl/procedimiento-comun.md`) se aplica en lo que esta skill referencia: iteraciones (sección 2), integración (sección 4.5) y prohibiciones Git (sección 4.6).
- Las skills de conocimiento se consultan con la herramienta `Skill` o leyendo su `SKILL.md` en `.claude/skills/<nombre>/`. Si una skill de la sección 4 no existe, se informa y se continúa con las disponibles.
- Si la herramienta `Agent` no está disponible, informa de la limitación y detente. No simules subagentes ni afirmes haberlos lanzado.

## Entradas

- `$ARGUMENTS` PUEDE indicar ramas o agentes concretos; si está vacío, las ramas se descubren con Git (Fase 1).

## 1. Propósito

Esta skill se ejecuta cuando dos o más agentes han terminado sus respectivas tareas y es necesario garantizar que todos sus cambios funcionan correctamente juntos.

Su objetivo es conseguir una versión estable del proyecto, sin conflictos de Git, errores de compilación, incompatibilidades arquitectónicas ni tests fallidos.

Debe contemplar:

- Conflictos de Git.
- Conflictos entre implementaciones.
- Clases duplicadas.
- Métodos incompatibles.
- Interfaces que no coinciden con sus implementaciones.
- Dependencias incorrectas.
- Imports rotos.
- DTO incompatibles.
- Configuraciones duplicadas.
- Beans de Spring duplicados o incompatibles.
- Errores de compilación.
- Tests fallidos.
- Incumplimientos de las reglas arquitectónicas.

**No debe desarrollar funcionalidades nuevas ni introducir cambios arquitectónicos que no sean necesarios para reparar los problemas detectados.**

## 2. Cuándo usarla (condiciones de ejecución)

Esta skill solamente debe ejecutarse cuando:

1. Los agentes que han trabajado en paralelo hayan terminado.
2. Sus tareas estén finalizadas.
3. Sus cambios estén disponibles en Git.
4. Se conozcan las ramas en las que han trabajado.

Si algún agente sigue trabajando, no se debe iniciar la reparación sobre sus archivos.

Si falta información sobre alguna rama, deberá investigar el estado del repositorio antes de continuar.

## 3. Procedimiento obligatorio

### Fase 1. Análisis del repositorio

**Tarea: inspeccionar los cambios.**

- Lanza un subagente.
- Ejecuta `git fetch origin`.
- Identifica todas las ramas feature creadas por los agentes.
- Comprueba cuáles se han integrado y cuáles siguen pendientes.
- Comprueba el estado de `develop`.
- Identifica los commits realizados.
- Inspecciona los cambios introducidos por cada agente.
- Identifica archivos modificados por varios agentes.
- Identifica dependencias entre los cambios.
- Comprueba que no existen modificaciones sin commit.

No debes asumir los nombres de las ramas. Debes descubrirlos mediante Git.

Entrega un informe de los cambios y posibles incompatibilidades.

### Fase 2. Integración de cambios en un entorno aislado

**Tarea: preparar una integración segura.**

- Lanza el subagente reparador (sección 0), que continuará en las Fases 3 a 5.
- Prepara la rama de reparación `feature/repair-<descripcion>` desde el último `origin/develop`, con nombre único comprobado en local y remoto, según `.claude/skills/do-code-impl/procedimiento-comun.md` sección 4.1.
- Integra en ella las ramas feature pendientes, respetando sus dependencias.
- Si las transformaciones arquitectónicas son secuenciales, respeta su orden.
- No modifiques directamente `develop`.
- No sobrescribas cambios de otros agentes.

Si una rama ya está integrada en `develop`, no vuelvas a integrarla.

Si aparecen conflictos de Git, identifica el origen de cada cambio antes de resolverlos.

### Fase 3. Detección de conflictos

**Tarea: analizar la compatibilidad de las implementaciones.**

- Continúa el subagente reparador con `SendMessage`, en su papel de analizador de código (si se lanza otro subagente, se aplica la sección 0).
- Examina los archivos modificados.
- Busca incompatibilidades entre las implementaciones.
- Comprueba las referencias entre clases.
- Comprueba interfaces y sus implementaciones.
- Comprueba DTO y sus consumidores.
- Comprueba los contratos entre módulos.
- Comprueba las dependencias entre paquetes.
- Comprueba la configuración de Spring.
- Comprueba las migraciones de base de datos.
- Comprueba los tests existentes.

Clasifica los problemas encontrados:

1. Conflictos de Git.
2. Errores de compilación.
3. Errores de integración.
4. Incompatibilidades arquitectónicas.
5. Errores funcionales.
6. Errores de testing.
7. Problemas de configuración.

Genera un informe de incidencias antes de modificar el código.

### Fase 4. Reparación iterativa

El subagente reparador realiza exactamente 10 iteraciones Programa → Verifica → Arregla según `.claude/skills/do-code-impl/procedimiento-comun.md` sección 2.2, con las verificaciones V1-V7 de `/k-back-testing` («Verificaciones por iteración») en el paso Verifica. Cada unidad de trabajo es una incidencia (o grupo coherente de incidencias) del informe de la Fase 3.

Reglas propias de la reparación:

- Se respetan las responsabilidades originales de cada componente.
- No se deben eliminar funcionalidades para conseguir que compile ni desactivar tests para ocultar errores (`/k-back-testing` sección 8).
- No se deben utilizar soluciones temporales que comprometan la arquitectura.
- No se deben duplicar clases para evitar resolver incompatibilidades.
- Se deben preservar las decisiones arquitectónicas establecidas por las skills de conocimiento.

Si no quedan incidencias suficientes para 10 iteraciones, se aplica el procedimiento común sección 2.1.4: se informa y no se introducen cambios artificiales.

### Fase 5. Tests finales

Una vez completadas las diez iteraciones:

- Continúa el subagente reparador con `SendMessage`, en su papel de tester (si se lanza otro subagente, se aplica la sección 0).
- Ejecuta la compilación completa.
- Ejecuta todos los tests unitarios.
- Ejecuta los tests de integración.
- Ejecuta los tests de arquitectura.
- Comprueba las configuraciones de Spring.
- Comprueba las migraciones de base de datos.
- Comprueba que las funcionalidades anteriores siguen funcionando.
- Corrige los errores encontrados.
- Repite los tests hasta obtener un resultado satisfactorio.

No se considerará finalizada la reparación mientras existan fallos introducidos por los cambios integrados.

### Fase 6. Revisión arquitectónica

**Tarea: verificar que la integración respeta la arquitectura.**

- Lanza un subagente especializado en arquitectura.
- Comprueba que los módulos respetan sus límites.
- Comprueba que ningún módulo accede a `internal` de otro módulo.
- Comprueba que las fachadas públicas exponen los contratos correctos.
- Comprueba que los controladores delegan en los casos de uso.
- Comprueba que el dominio no depende de Spring ni de JPA.
- Comprueba que la persistencia permanece aislada.
- Comprueba que no existen dependencias circulares.

Utiliza las skills `/k-back-*` disponibles como referencia.

Si detecta incumplimientos, deberán repararse antes de continuar.

### Fase 7. Git y publicación

**Tarea: finalizar la integración.**

- Comprueba que todos los cambios están commiteados en la rama de reparación, con commits según el procedimiento común sección 4.3.
- Comprueba que las verificaciones han terminado correctamente.
- Publica la rama con `git push -u origin feature/repair-<descripcion>` (procedimiento común, sección 4.4).
- Lanza el subagente integrador, que integra la rama de reparación en `develop` según el procedimiento común sección 4.5: actualización desde el remoto con fetch y `merge --ff-only`, `merge --no-ff`, verificación global antes de publicar, parada ante conflictos inseguros, `git push origin develop` y actualización del `develop` local.
- Se aplican las prohibiciones del procedimiento común sección 4.6 (sin `push --force`, sin `git pull`, sin borrar ramas de los agentes).

## 4. Skills relacionadas (skills que debe consultar)

La skill deberá utilizar las siguientes skills cuando estén disponibles:

- `/k-back`: reglas generales.
- `/k-back-module`: límites y dependencias entre módulos.
- `/k-back-domain`: reglas del dominio.
- `/k-back-repository`: contratos de repositorios.
- `/k-back-service`: casos de uso.
- `/k-back-controller`: controladores.
- `/k-back-dto`: contratos y DTO.
- `/k-back-persistence`: persistencia.
- `/k-back-testing`: pruebas.

También podrá utilizar:

- `/do-code-impl` como referencia de coordinación.
- `/do-code-module` para comprender las reglas de creación de módulos.

No debe ejecutar estas skills para volver a desarrollar funcionalidades. Solamente debe consultar sus normas y utilizarlas como referencia para reparar incompatibilidades.

## 5. Gestión de subagentes

Para cada tarea delegable:

**Lanza un subagente.**

Subagentes recomendados:

| Subagente | Responsabilidad | Definición |
|---|---|---|
| Analizador Git | Identificar ramas, commits y conflictos (Fase 1) | `.claude/skills/do-code-repair/subagents/analizador-git.md` |
| Analizador de código | Detectar incompatibilidades (Fase 3) | `.claude/skills/do-code-repair/subagents/reparador.md` |
| Reparador backend | Corregir errores de implementación (Fases 2 y 4) | `.claude/skills/do-code-repair/subagents/reparador.md` |
| Arquitecto | Comprobar límites y dependencias (Fase 6) | `.claude/skills/do-code-repair/subagents/arquitecto.md` |
| Tester | Ejecutar y reparar tests (Fase 5) | `.claude/skills/do-code-repair/subagents/reparador.md` |
| Integrador | Coordinar la integración final (Fase 7) | `.claude/skills/do-code-impl/subagents/integrador.md` |

El analizador de código, el reparador backend y el tester son papeles del mismo subagente `reparador`, continuado con `SendMessage` (sección 0). Todos se lanzan según `.claude/skills/do-code-impl/procedimiento-comun.md` sección 1.7.

Los subagentes deberán tener responsabilidades independientes.

El agente coordinador será responsable de:

- Asignar tareas.
- Evitar modificaciones simultáneas sobre los mismos archivos.
- Coordinar las dependencias.
- Revisar las soluciones.
- Aprobar la integración final.

## 6. Restricciones

Está prohibido:

- Modificar directamente `develop` durante la reparación.
- Eliminar funcionalidades para solucionar errores.
- Ignorar tests fallidos.
- Desactivar comprobaciones.
- Sobrescribir cambios de otros agentes.
- Hacer commits con código que no se haya revisado.
- Utilizar `git push --force`.
- Crear dependencias circulares.
- Romper los contratos públicos de los módulos sin justificación.
- Introducir nuevas funcionalidades ajenas a la reparación.
- Dar por finalizada la tarea sin verificar el resultado integrado.

Se aplican además las prohibiciones de `.claude/skills/do-code-impl/procedimiento-comun.md` sección 4.6.

Si no puede resolver un problema sin modificar decisiones arquitectónicas importantes, deberá detenerse y solicitar autorización.

## 7. Resultado esperado

Al finalizar, deberá entregar un informe con:

1. Agentes y ramas analizados.
2. Conflictos de Git encontrados.
3. Incompatibilidades de código detectadas.
4. Reparaciones realizadas.
5. Archivos modificados.
6. Commits creados, por rama (hash corto y mensaje).
7. Resultado de compilación.
8. Resultado de tests unitarios.
9. Resultado de tests de integración.
10. Resultado de tests de arquitectura.
11. Resultado de la integración.
12. Estado de publicación de `develop`, con confirmación de que el `develop` local y `origin/develop` tienen el mismo hash (o motivo por el que el local no se pudo actualizar).
13. Registro de las 10 iteraciones (procedimiento común, sección 2.3).

**Criterio de éxito:** todos los cambios de los agentes deben convivir correctamente, el proyecto debe compilar, los tests deben pasar y la arquitectura debe respetar las reglas establecidas.

## Comprobaciones antes de finalizar

- [ ] Las ramas se han descubierto con Git y ninguna ya integrada se ha vuelto a integrar.
- [ ] La rama de reparación tiene nombre único, comprobado en local y remoto, y está publicada.
- [ ] Las 10 iteraciones están registradas con V1-V7, o se ha informado de que no había incidencias suficientes.
- [ ] No se han eliminado funcionalidades, desactivado tests ni añadido funcionalidades ajenas a la reparación.
- [ ] La suite completa, `ArquitecturaTest` y `ModulosTest` (si existe) están en verde sobre el resultado integrado.
- [ ] La integración en `develop` se ha hecho según el procedimiento común sección 4.5, sin incumplir la sección 4.6.
- [ ] El `develop` local y `origin/develop` coinciden, o se ha informado del motivo.
