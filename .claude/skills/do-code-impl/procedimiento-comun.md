# Procedimiento común de las skills ejecutoras `do-code-*`

Archivo de apoyo de `/do-code-impl`, referenciado por `/do-code-basic`, `/do-code-modular`, `/do-code-architecture` y `/do-code-module` con la ruta `.claude/skills/do-code-impl/procedimiento-comun.md`. Define CÓMO se ejecuta una tarea de código: modos de ejecución, subagentes, Fase A (10 iteraciones), Fase B (tests), flujo Git e informe final. QUÉ normas debe cumplir el código NO está aquí: es propiedad de `/k-back` y de las skills `/k-back-*` (se leen en `.claude/skills/<nombre>/SKILL.md`).

Los términos DEBE, NO DEBE y PUEDE son normativos.

## 1. Modos de ejecución y mecánica de subagentes

1.1. Las skills `do-code-*` tienen `disable-model-invocation: true`: solo las invoca el usuario con `/<nombre>`. Por eso NO PUEDEN invocarse con la herramienta `Skill`. Cuando `/do-code-impl` necesita una ejecutora, DEBE leer su `SKILL.md` y seguirlo en el hilo principal, o pasar la ruta de ese `SKILL.md` (y la de este archivo) en el prompt de un subagente.

1.2. Los subagentes NO PUEDEN lanzar subagentes. Por eso cada ejecutora tiene dos modos, y DEBE determinar el suyo al empezar con este orden de prioridad:

1. Si el prompt recibido indica «modo subagente», prevalece esa indicación.
2. Si el usuario invocó `/<nombre>` directamente, es modo coordinador.
3. La disponibilidad de la herramienta `Agent` solo sirve como comprobación adicional: si falta en modo coordinador, se aplica la sección 1.5.

| Modo | Quién lo ejecuta | Qué hace | Qué NO hace |
|---|---|---|---|
| Coordinador | El hilo principal, con herramienta `Agent` | Planifica, lanza el subagente de preparación Git (sección 4.1), el de implementación (Fases A y B) y el integrador (sección 4.5); supervisa sus informes y emite el informe final | No implementa código directamente si puede delegarlo |
| Subagente | Un subagente sin herramienta `Agent` (p. ej. lanzado por `/do-code-impl` en paralelo) | Hace por sí mismo la preparación Git en su worktree (sección 4.1, variante subagente), las Fases A y B, los commits y el push de su rama | NO integra en `develop`: eso corresponde al coordinador que lo lanzó |

1.3. Todo subagente que modifique archivos DEBE lanzarse con la herramienta `Agent` y `isolation: "worktree"`, trabajar solo en su rama feature y publicarla. Los subagentes de solo lectura PUEDEN lanzarse sin worktree.

1.4. El prompt de cada subagente de implementación DEBE incluir: tarea y requisitos concretos (sin resumir los que afecten a campos, endpoints o reglas), nombre exacto de la rama, base (`origin/develop`), modo subagente, ruta de este archivo, ruta del `SKILL.md` de la ejecutora que sigue, lista de skills `/k-back-*` que DEBE leer (`.claude/skills/<nombre>/SKILL.md`), las 10 unidades de trabajo planificadas y el formato de informe que debe devolver (sección 6).

1.5. Si la herramienta `Agent` no está disponible en el hilo principal, la ejecutora hace ella misma la preparación (sección 4.1, variante subagente), la implementación y el push y, al terminar, integra ella misma siguiendo la sección 4.5, informando de que no se usaron subagentes. NO DEBE simular subagentes ni afirmar haberlos lanzado.

1.6. El agente `falsador` NO forma parte de este procedimiento.

## 2. Fase A — Implementación en exactamente 10 iteraciones

### 2.1. Planificación previa

2.1.1. Antes de la primera iteración DEBEN inspeccionarse el código, `pom.xml`, la etapa actual (`/k-back` sección 2) y los tests existentes, y DEBEN planificarse exactamente 10 unidades de trabajo.

Además, antes de la primera iteración DEBE ejecutarse `./mvnw -B test` y registrarse la línea base: número total de tests, clases de test y resultado (leído de `target/surefire-reports/`). Si la línea base no está en verde, DEBE informarse con la salida real y detenerse: no se empieza a trabajar sobre una base rota. La línea base es la referencia para comprobar que el número de tests no baja (`/k-back-testing` sección 8.4).

2.1.2. Una unidad de trabajo es un incremento coherente y verificable (p. ej. «reubicar el dominio y sus tests», «introducir la facade con su implementación»), NO un archivo suelto. Cada unidad DEBE dejar el proyecto compilando.

2.1.3. Las 10 unidades DEBEN registrarse antes de empezar (en la respuesta al usuario o al coordinador y en la lista de tareas si existe), con su objetivo y los paquetes que toca.

2.1.4. NO DEBE fabricarse trabajo artificial para llegar a 10. Si la tarea real no da para 10 unidades, DEBE informarse explícitamente al usuario (o al coordinador) con el número de unidades reales y el motivo, y las iteraciones restantes NO se rellenan con cambios inventados: se registran como «sin unidad de trabajo real» y se dedican, si aporta valor, a una verificación adicional documentada, nunca a repetir comprobaciones sin avance.

### 2.2. Ciclo de cada iteración: Programa → Verifica → Arregla

1. **Programa**: implementa la unidad aplicando las normas de `/k-back` y de la skill `/k-back-*` de cada capa tocada.
2. **Verifica**: ejecuta las verificaciones V1-V7 definidas en la sección «Verificaciones por iteración» de `/k-back-testing` (`.claude/skills/k-back-testing/SKILL.md`). Este archivo NO las redefine; también se aplican los avisos de `/k-back-testing` sección 7 sobre `-Dtest` sin coincidencias. Cada verificación se registra con su resultado real: correcto, fallo (con la salida relevante) o no aplicable (con motivo).
3. **Arregla**: todo problema detectado DEBE corregirse antes de pasar a la siguiente iteración, y la verificación afectada DEBE repetirse tras la corrección.

2.2.1. Una iteración sin incidencias DEBE documentarse igualmente como verificada.

2.2.2. NO DEBEN repetirse comprobaciones sin avance: si una verificación falla dos veces seguidas sin que haya cambiado el código o la hipótesis, se detiene la iteración y se informa el bloqueo con su salida.

2.2.3. DEBE haber commits por conjuntos coherentes de cambios (sección 4.3). El commit PUEDE hacerse al cerrar cada iteración con cambios, o agrupar varias iteraciones si forman una única unidad lógica.

2.2.4. En la última iteración, o antes de dar la Fase A por terminada, DEBE ejecutarse la suite completa según `/k-back-testing`.

### 2.3. Registro de iteraciones

El registro se entrega en el informe (no se commitea como archivo salvo que el plan lo pida):

| N.º | Unidad de trabajo | V1 | V2 | V3 | V4 | V5 | V6 | V7 | Incidencias | Correcciones |
|---|---|---|---|---|---|---|---|---|---|---|
| 1 | ... | correcto | correcto | correcto | correcto | n/a: motivo | correcto | correcto | ninguna | — |

## 3. Fase B — Tests

La Fase B NO sustituye a la Fase A: se ejecuta después y sobre el resultado verificado de las 10 iteraciones. Qué probar en cada capa, con qué herramientas y con qué nombres es propiedad de `/k-back-testing`.

1. **Identificar casos**: a partir de los requisitos y del código cambiado, lista los casos positivos, negativos y límite pendientes por capa.
2. **Tests unitarios y de capa web**: dominio, mappers y aplicación, según `/k-back-testing` sección 3; y tests de capa web con `@WebMvcTest` (códigos de estado, `ProblemDetail` y validación de los DTO de entrada), según `/k-back-testing` secciones 3.4 y 3.5.
3. **Tests de integración**: solo donde aporten valor y respetando la limitación de base de datos de `/k-back-testing` secciones 1.3, 3.2 y 3.7.
4. **Tests de arquitectura**: `ArquitecturaTest` (y `ModulosTest` si existe) adaptados a la etapa, sin relajar reglas.
5. **Ejecutar todos**: suite completa con el wrapper (`./mvnw -B verify` o `./mvnw -B test`).
6. **Corregir** los fallos (en el código o en el test, sin debilitar aserciones) y **reejecutar** hasta verde.
7. **Informe final de tests**: número de tests antes y después, clases nuevas o movidas, resultado real de la suite y carencias conocidas.

## 4. Flujo Git

### 4.1. Preparación (subagente de preparación)

En modo coordinador se lanza un subagente de preparación sin worktree y sin modificar archivos de trabajo. En modo subagente, el propio subagente hace estos pasos en su worktree.

1. Comprueba que el repositorio está limpio (`git status --porcelain`). Si hay cambios sin commit, se detiene e informa: NO los descarta, NO los guarda en `stash` ni los commitea.
2. Anota la rama actual.
3. Ejecuta `git fetch origin`.
4. Comprueba que existe `origin/develop`. Si no existe, se detiene e informa.
5. Actualiza `develop` local solo por fast-forward y sin pisar cambios locales: si `develop` no está desprotegida en ningún worktree, `git fetch origin develop:develop` (falla de forma segura si no es fast-forward); si está desprotegida y limpia, `git merge --ff-only origin/develop` en ese worktree. Si `develop` local tiene commits propios no publicados, se detiene e informa.
6. Elige un nombre `feature/<nombre-descriptivo>` en kebab-case y comprueba que no existe ni en local (`git branch --list`) ni en el remoto (`git ls-remote --heads origin feature/<nombre>`). Si existe, elige otro nombre y lo informa.
7. Crea la rama desde `origin/develop`:
   - Modo coordinador: `git branch feature/<nombre> origin/develop` (sin desprotegerla, para que el worktree de implementación pueda hacerlo).
   - Modo subagente: `git switch -c feature/<nombre> origin/develop` en su worktree.
8. Devuelve: rama de partida, hash de `origin/develop`, nombre de la rama creada.

Si una operación sobre `.git/config` falla por bloqueo concurrente (`could not lock config file`), DEBE reintentarse tras una breve espera, sin borrar archivos de bloqueo ajenos.

### 4.2. Trabajo en la rama

- Todo el trabajo se hace en la rama feature propia, dentro del worktree. NO DEBE trabajarse en `develop` ni en `main`.
- En modo coordinador, el subagente de implementación ejecuta en su worktree `git switch feature/<nombre>`.

### 4.3. Commits

- Un commit por unidad lógica, con mensaje convencional en español (`feat:`, `refactor:`, `test:`, `build:`, `docs:`, `chore:`, `fix:`) que describa la unidad y, si procede, la decisión arquitectónica tomada.
- Todo mensaje DEBE terminar con la línea:

  ```text
  Co-Authored-By: Claude Opus 5.5 <noreply@anthropic.com>
  ```

- Antes de cada commit: `git status` y `git diff --cached`, revisando que no entran temporales (`target/`, logs, volcados), secretos (contraseñas, tokens, `.env`), configuración local (`.idea/`, `.vscode/`, `application-local.*`) ni archivos ajenos a la tarea. Se añaden archivos por ruta, no con `git add -A` a ciegas.

### 4.4. Publicación

- `git push -u origin feature/<nombre>`.
- Si el push falla, DEBE analizarse la causa (`git fetch`, `git status`, `git log origin/feature/<nombre>..HEAD`) e informarse. NO DEBE forzarse.

### 4.5. Integración (subagente integrador, solo modo coordinador)

El subagente integrador se lanza con `isolation: "worktree"` cuando todas las ramas de la tarea están terminadas:

1. Comprueba que cada rama a integrar existe en local y está publicada en `origin` con el mismo hash, que no hay cambios sin commit en ella y que el informe de su implementación tiene las verificaciones en verde.
2. Ejecuta `git fetch origin` y prepara `develop` desde el remoto: si `develop` no está desprotegida en otro worktree, `git switch develop` y `git merge --ff-only origin/develop`; si lo está, trabaja en `git switch --detach origin/develop` y publicará con `git push origin HEAD:develop`.
3. Integra las ramas una a una, en el orden de dependencias, con `git merge --no-ff feature/<nombre>` y mensaje convencional (`chore: integrar feature/<nombre> en develop` + línea `Co-Authored-By`).
4. Tras cada integración relevante (toda rama con código Java, `pom.xml` o recursos) ejecuta la verificación global: `./mvnw -B verify` y, si existen, `ArquitecturaTest` y `ModulosTest`. Si la verificación falla, NO DEBE publicar: se detiene e informa con la salida real. Solo PUEDE deshacer su propio merge local no publicado (`git merge --abort` si sigue en curso, o volver a `ORIG_HEAD` si ya se completó y no se ha publicado), o proponer `/do-code-repair`.
5. Si hay un conflicto que no pueda resolverse de forma segura y evidente (p. ej. ambos lados cambian la misma lógica), DEBE abortar el merge (`git merge --abort`), detenerse y pedir intervención; el coordinador PUEDE proponer `/do-code-repair`.
6. Publica con `git push origin develop` (o `git push origin HEAD:develop`). Si el push es rechazado porque `develop` avanzó, vuelve al paso 2; NUNCA fuerza.
7. Si publicó con `git push origin HEAD:develop`, actualiza el `develop` local con `git fetch origin develop:develop`; si falla porque `develop` está desprotegida en otro worktree, lo informa en lugar de tocar ese worktree.

### 4.6. Prohibiciones

- NO DEBE usarse `git push --force` ni `--force-with-lease`, `git pull`, `git reset --hard` sobre trabajo ajeno, ni reescribirse historia publicada.
- NO DEBE pisarse el trabajo de otros agentes ni del usuario.
- NO DEBEN borrarse ramas feature, ni locales ni remotas.
- Nada está terminado sin commit, push e integración en `develop` (la integración la hace el coordinador).

### 4.7. Informe final Git

- Ramas creadas (local y remota) y rama de partida.
- Commits por rama (hash corto + mensaje).
- Estado del push de cada rama.
- Ramas integradas en `develop`, orden, commits de merge y hash final de `origin/develop`.
- Rama final: `develop`.
- Resultado de la verificación global tras cada integración y, en `/do-code-impl`, de la verificación final de su sección 6.2.
- Confirmación de que `develop` está publicado y actualizado en local y en remoto (mismo hash), o motivo por el que el local no se pudo actualizar.
- Conflictos encontrados y cómo se resolvieron, o por qué se detuvo la integración.
- Confirmación de que no se ha forzado ningún push, no se ha usado `git pull` y no se ha borrado ninguna rama.

## 5. Decisiones arquitectónicas

Toda decisión no derivada directamente de los requisitos (dependencia nueva, cambio de etapa, interfaz o paquete creado por exigencia de la arquitectura objetivo, desviación de una convención, interpretación de un requisito ambiguo) DEBE documentarse en el informe final con su motivo y, si procede, en el cuerpo del mensaje de commit, según `/k-back` sección 11.

## 6. Informe final de una ejecutora

1. Tarea, etapa detectada al inicio y etapa final.
2. Modo de ejecución y subagentes lanzados (tipo, rama, resultado).
3. Plan de 10 unidades y registro de iteraciones (sección 2.3), o aviso de que la tarea no daba para 10.
4. Informe de la Fase B (sección 3, paso 7).
5. Decisiones arquitectónicas (sección 5).
6. Informe final Git (sección 4.7).
7. Incompatibilidades, bloqueos y trabajo pendiente.
