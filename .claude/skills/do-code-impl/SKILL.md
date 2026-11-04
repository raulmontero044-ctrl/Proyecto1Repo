---
name: do-code-impl
description: Coordina la ejecución de un plan de implementación backend (plan.md) - analiza tareas y dependencias, asigna cada tarea a una skill ejecutora do-code-* o a las normas k-back, lanza subagentes en paralelo con worktree y rama propia, espera a las dependientes, supervisa, verifica y hace integrar todas las ramas en develop. Úsala cuando el usuario pida ejecutar un plan.md completo. Hace commits, push e integración.
argument-hint: "<ruta a plan.md>"
disable-model-invocation: true
---

# /do-code-impl

## Propósito

Coordinar la ejecución completa del plan `$ARGUMENTS`: dividirlo en tareas, decidir quién ejecuta cada una, lanzar y supervisar subagentes, integrar sus ramas en `develop` y comprobar que todo queda terminado y verificado. Esta skill delega y supervisa: NO implementa código ni duplica las instrucciones de las ejecutoras ni las normas de las `k-back-*`.

Los términos DEBE, NO DEBE y PUEDE son normativos.

## Cuándo usarla

- El usuario pide ejecutar un `plan.md` con una o varias tareas de backend.
- El plan combina transformaciones de etapa, módulos nuevos o cambios en varias capas que conviene repartir entre agentes.

Para una única tarea de etapa o de módulo, el usuario PUEDE invocar directamente `/do-code-basic`, `/do-code-modular`, `/do-code-architecture` o `/do-code-module`.

## Entradas

- `$ARGUMENTS`: ruta a `plan.md`. Si falta, no existe o no es legible, DEBE pedirse al usuario y detenerse.

## Resultado esperado

- Todas las tareas del plan implementadas en ramas `feature/*` publicadas, verificadas e integradas en `develop` con `merge --no-ff`, y `origin/develop` actualizado.
- Un informe final con el estado de cada tarea, las decisiones arquitectónicas y el informe Git.

## Skills relacionadas

- Procedimiento común (modos, subagentes, Fase A, Fase B, Git, informes): `.claude/skills/do-code-impl/procedimiento-comun.md`.
- Ejecutoras: `/do-code-basic` (Etapa 1), `/do-code-modular` (Etapa 1→2), `/do-code-architecture` (Etapa 2→3), `/do-code-module` (módulo nuevo).
- Normas: `/k-back` (modelo de etapas y propiedad de normas) y las `/k-back-*` de capa.
- `/do-code-repair`: reparación opcional de integraciones tras trabajo paralelo.

## Procedimiento

Esta skill se ejecuta en el hilo principal, en modo coordinador (procedimiento común, sección 1). Si la herramienta `Agent` no está disponible, DEBE informarlo y detenerse: no puede coordinar.

### 1. Leer el plan

1.1. Lee `$ARGUMENTS` completo. Lee `.claude/skills/k-back/SKILL.md` para conocer el modelo de etapas y la propiedad de cada norma.

1.2. Detecta la etapa actual del proyecto inspeccionando los paquetes reales (`/k-back` sección 2) y anótala.

1.3. Registra la línea base global en un subagente con `isolation: "worktree"`, sin cambios, que ejecuta `git fetch origin`, `git switch --detach origin/develop` y `./mvnw -B test`, con número total de tests, clases de test y resultado. Si no está en verde, DEBE informarse con la salida real y detenerse. Esta línea base es la referencia de la verificación final (sección 6.2).

1.4. Si el plan es ambiguo o le falta información imprescindible (campos, reglas, endpoints, módulos), DEBE preguntarse al usuario antes de lanzar ningún subagente. NO DEBEN inventarse requisitos.

### 2. Identificar tareas y dependencias

2.1. Extrae cada tarea con: identificador, objetivo, requisitos literales, capas y paquetes afectados y criterios de aceptación.

2.2. Construye el grafo de dependencias: una tarea B depende de A si necesita código, paquetes, migraciones o la etapa que produce A, o si ambas modifican los mismos archivos de forma no separable. Registra el grafo en la respuesta (lista de aristas `A → B`) y detecta ciclos; un ciclo DEBE informarse y resolverse con el usuario.

2.3. Las transformaciones de etapa son SECUENCIALES: `/do-code-basic` → `/do-code-modular` → `/do-code-architecture`, cada una desde el `develop` resultante de integrar la anterior. Nunca se lanzan en paralelo entre sí ni con tareas que toquen los paquetes que reubican. Nunca se retrocede de etapa.

2.4. Un módulo nuevo (`/do-code-module`) depende de la última transformación de etapa del plan, porque debe crearse en la etapa resultante.

### 3. Asignar ejecutor a cada tarea

| Tipo de tarea | Ejecutor |
|---|---|
| Separación en capas sin módulos, o auditoría de capas | `/do-code-basic` |
| Paso a monolito modular o auditoría de módulos | `/do-code-modular` |
| Paso a arquitectura definitiva `api` / `internal` | `/do-code-architecture` |
| Crear un módulo de negocio nuevo | `/do-code-module` |
| Cambio acotado dentro de capas existentes (p. ej. un caso de uso o un endpoint en un módulo existente) | Subagente que sigue el procedimiento común aplicando las `/k-back-*` de las capas tocadas |

Las ejecutoras tienen `disable-model-invocation: true`, así que NO DEBEN invocarse con la herramienta `Skill`. Para ejecutar una:

- En el hilo principal (tarea única o secuencial): lee su `.claude/skills/<ejecutora>/SKILL.md` y síguelo en modo coordinador.
- En un subagente (tareas paralelas): pasa en su prompt la ruta de su `SKILL.md`, la de `.claude/skills/do-code-impl/procedimiento-comun.md`, la indicación de modo subagente y las rutas `.claude/skills/<k-back-x>/SKILL.md` que debe leer (las que indique la ejecutora). El subagente NO integra en `develop`.

### 4. Dividir el trabajo y lanzar subagentes

4.1. Agrupa las tareas en oleadas: cada oleada contiene las tareas cuyas dependencias ya están integradas en `develop`.

4.2. Para cada oleada, lanza un subagente de preparación Git (procedimiento común, sección 4.1) que fije un nombre `feature/<nombre-descriptivo>` único por tarea y cree las ramas desde `origin/develop`.

4.3. Lanza en un mismo mensaje, en paralelo, un subagente por tarea independiente con la herramienta `Agent` e `isolation: "worktree"`, con el prompt de la sección 1.4 del procedimiento común. Una oleada con una sola tarea de etapa PUEDE ejecutarse en el hilo principal siguiendo la ejecutora en modo coordinador.

4.4. Espera a que terminen todos los subagentes de la oleada. Las tareas dependientes NO DEBEN lanzarse hasta que sus dependencias estén integradas en `develop` (sección 5) y DEBEN partir del `develop` resultante.

### 5. Supervisar e integrar

5.1. Revisa el informe de cada subagente: 10 iteraciones registradas (o aviso justificado de que no había trabajo para 10), V1-V7 en verde, Fase B ejecutada, commits y push hechos. Si falta algo, continúa ese subagente con `SendMessage` o relanza la parte pendiente; NO lo completes implementándolo tú.

5.2. Al terminar cada oleada, lanza el subagente integrador (procedimiento común, sección 4.5) con las ramas de la oleada en orden de dependencias.

5.3. Si varios agentes han trabajado en paralelo y la integración se detiene por conflictos o la verificación global falla tras integrar, PUEDE usarse como paso opcional `/do-code-repair`: se ejecuta en el hilo principal leyendo y siguiendo `.claude/skills/do-code-repair/SKILL.md` (o lo invoca el usuario). Tras la reparación, se comprueba qué ramas quedaron integradas a través de la rama de reparación y solo se integran las restantes.

### 6. Comprobar que todo está terminado

6.1. Cada tarea del plan tiene su rama publicada, integrada en `develop` y sus criterios de aceptación cubiertos.

6.2. Verificación final sobre `origin/develop` en un subagente con worktree, sin cambios: `git fetch origin`, `git switch --detach origin/develop`, `./mvnw -B verify` (incluye `ArquitecturaTest` y `ModulosTest` si existe) y comprobación de que el número de tests no ha bajado respecto a la línea base global de la sección 1.3.

6.3. Si la verificación final falla, se trata como una tarea nueva de corrección (o `/do-code-repair`) y se repiten las secciones 4 a 6.

### 7. Coordinar commits y pushes

Los commits y pushes los hacen los subagentes en sus ramas y el integrador en `develop`, según el procedimiento común (secciones 4.3 a 4.6). Esta skill comprueba que se han hecho y que nadie ha forzado un push, usado `git pull` ni borrado ramas.

### 8. Informe final

- Plan ejecutado, etapa inicial y final.
- Tabla de tareas: id, ejecutor, rama, subagente, estado, hash de integración.
- Grafo de dependencias y oleadas.
- Resumen del registro de iteraciones y de la Fase B de cada tarea.
- Decisiones arquitectónicas (procedimiento común, sección 5).
- Informe final Git (procedimiento común, sección 4.7), incluido el hash final de `origin/develop`.
- Si se usó `/do-code-repair`, su resultado.
- Bloqueos, incompatibilidades y trabajo pendiente.

## Comprobaciones antes de finalizar

- [ ] El plan se ha leído entero y no se ha inventado ningún requisito.
- [ ] El grafo de dependencias está registrado y las transformaciones de etapa se han ejecutado en secuencia, cada una desde el `develop` resultante.
- [ ] Ninguna ejecutora se ha invocado con la herramienta `Skill`; se ha seguido su `SKILL.md` o se ha pasado su ruta al subagente.
- [ ] Las tareas independientes se han lanzado en paralelo con `isolation: "worktree"` y rama propia; las dependientes han esperado.
- [ ] Esta skill no ha implementado código ni repetido normas de las `k-back-*`.
- [ ] Cada rama tiene commits, push y está integrada en `develop` con `--no-ff`; ninguna rama se ha borrado.
- [ ] La verificación final sobre `origin/develop` está en verde y su salida real se ha informado.
- [ ] El informe final incluye decisiones arquitectónicas e informe Git.
