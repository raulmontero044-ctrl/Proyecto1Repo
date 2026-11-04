---
name: do-code-repair
description: Detecta y repara conflictos de integración, incompatibilidades y errores introducidos por varios agentes que han trabajado simultáneamente en el proyecto. Úsala cuando dos o más agentes hayan terminado en ramas feature distintas y haya que dejar develop estable (sin conflictos Git, errores de compilación, incumplimientos arquitectónicos ni tests fallidos). No desarrolla funcionalidades nuevas.
argument-hint: "[ramas o agentes a reparar (opcional; si se omite se descubren con Git)]"
---

# /do-code-repair

## 0. Mecanismos de invocación

- Esta skill DEBE ejecutarse en el hilo principal de la conversación, no en un subagente, porque necesita la herramienta `Agent` para lanzar subagentes.
- "Lanza un subagente" significa usar la herramienta `Agent`. Los subagentes que modifiquen archivos DEBEN lanzarse con `isolation: "worktree"` y trabajar sobre la rama `feature/repair-conflicts` (o una rama `feature/repair-<descripcion>` derivada de ella cuando trabajen en paralelo sobre archivos distintos).
- Las skills de conocimiento se consultan con la herramienta `Skill` o leyendo su `SKILL.md` en `.claude/skills/<nombre>/`. Si una skill de la sección 4 no existe, se informa y se continúa con las disponibles.
- `$ARGUMENTS` PUEDE indicar ramas o agentes concretos; si está vacío, las ramas se descubren con Git (Fase 1).
- Si la herramienta `Agent` no está disponible, informa de la limitación y detente. No simules subagentes ni afirmes haberlos lanzado.

## 1. Objetivo

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

## 2. Condiciones de ejecución

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

- Lanza un subagente.
- Crea una rama `feature/repair-conflicts` desde el último `develop`.
- Integra en ella las ramas feature pendientes, respetando sus dependencias.
- Si las transformaciones arquitectónicas son secuenciales, respeta su orden.
- No modifiques directamente `develop`.
- No sobrescribas cambios de otros agentes.

Si una rama ya está integrada en `develop`, no vuelvas a integrarla.

Si aparecen conflictos de Git, identifica el origen de cada cambio antes de resolverlos.

### Fase 3. Detección de conflictos

**Tarea: analizar la compatibilidad de las implementaciones.**

- Lanza un subagente especializado en análisis de código.
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

Se realizarán exactamente 10 iteraciones siguiendo el procedimiento:

**1. Programa**

- Lanza un subagente.
- Selecciona una unidad de trabajo conflictiva.
- Realiza las modificaciones necesarias para resolverla.
- Respeta las responsabilidades originales de cada componente.

**2. Verifica**

- Comprueba que el cambio es correcto.
- Ejecuta compilación cuando corresponda.
- Comprueba las dependencias.
- Comprueba las interfaces.
- Comprueba que no se han introducido nuevos errores.

**3. Arregla**

- Lanza un subagente si la reparación puede delegarse.
- Corrige los problemas detectados.
- Repite las comprobaciones necesarias.

Repite el procedimiento hasta completar las 10 iteraciones.

Reglas:

- Cada iteración debe representar un avance real.
- No se permite introducir modificaciones artificiales.
- No se deben eliminar funcionalidades para conseguir que compile.
- No se deben desactivar tests para ocultar errores.
- No se deben utilizar soluciones temporales que comprometan la arquitectura.
- No se deben duplicar clases para evitar resolver incompatibilidades.
- Se deben preservar las decisiones arquitectónicas establecidas por las skills de conocimiento.

Si no quedan incidencias, las iteraciones restantes deberán utilizarse para revisar y verificar distintas áreas de integración, sin introducir cambios innecesarios.

### Fase 5. Tests finales

Una vez completadas las diez iteraciones:

- Lanza un subagente especializado en testing.
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

- Lanza un subagente.
- Comprueba que todos los cambios están committeados en `feature/repair-conflicts`.
- Comprueba que las verificaciones han terminado correctamente.
- Crea commits descriptivos cuando sea necesario.
- Publica la rama:

```bash
git push -u origin feature/repair-conflicts
```

- Actualiza `develop` mediante `git fetch`.
- Comprueba que no han aparecido cambios nuevos que invaliden la integración.
- Integra `feature/repair-conflicts` en `develop`.
- Ejecuta los tests finales sobre `develop`.
- Publica los cambios:

```bash
git push origin develop
```

No se utilizará `git push --force`.

Si aparecen nuevos conflictos durante la integración, deberán resolverse antes de publicar.

No se eliminarán automáticamente las ramas originales de los agentes.

## 4. Skills que debe consultar

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

| Subagente | Responsabilidad |
|---|---|
| Analizador Git | Identificar ramas, commits y conflictos |
| Analizador de código | Detectar incompatibilidades |
| Reparador backend | Corregir errores de implementación |
| Arquitecto | Comprobar límites y dependencias |
| Tester | Ejecutar y reparar tests |
| Integrador | Coordinar la integración final |

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

Si no puede resolver un problema sin modificar decisiones arquitectónicas importantes, deberá detenerse y solicitar autorización.

## 7. Resultado esperado

Al finalizar, deberá entregar un informe con:

1. Agentes y ramas analizados.
2. Conflictos de Git encontrados.
3. Incompatibilidades de código detectadas.
4. Reparaciones realizadas.
5. Archivos modificados.
6. Commits creados.
7. Resultado de compilación.
8. Resultado de tests unitarios.
9. Resultado de tests de integración.
10. Resultado de tests de arquitectura.
11. Resultado de la integración.
12. Estado de publicación de `develop`.

**Criterio de éxito:** todos los cambios de los agentes deben convivir correctamente, el proyecto debe compilar, los tests deben pasar y la arquitectura debe respetar las reglas establecidas.
