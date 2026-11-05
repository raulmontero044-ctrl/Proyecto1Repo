# Procedimiento común de las skills de testing

Archivo de apoyo de `/do-test`, referenciado por las ocho skills especializadas `/test-<tipo>-<fase>` (`<tipo>` ∈ `unit`, `arch`, `api`, `e2e`; `<fase>` ∈ `create`, `repair`) con la ruta `.claude/skills/do-test/procedimiento-testing.md`. Define CÓMO se ejecuta un ciclo de testing: modos, agentes, ramas, fases CREATE y REPAIR, falsación, evaluación, integración e informes.

QUÉ se prueba, con qué herramientas, cómo se nombran y ubican los tests y qué es un test válido es propiedad de `/k-back-testing` (`.claude/skills/k-back-testing/SKILL.md`). Qué abarca cada tipo de testing en Facturación es propiedad de su perfil (`.claude/skills/do-test/tipos/<tipo>.md`). Este archivo NO repite esas normas.

Los términos DEBE, NO DEBE y PUEDE son normativos.

## 1. Modos de ejecución y mecánica de agentes

1.1. `/do-test` y las ocho skills especializadas tienen `disable-model-invocation: true`: solo las invoca el usuario con `/<nombre>`. Cuando `/do-test` necesita una especializada, DEBE leer su `SKILL.md` y seguirlo en el hilo principal. NO DEBE invocarla con la herramienta `Skill`.

1.2. Todas estas skills se ejecutan en el hilo principal, en modo coordinador, porque necesitan la herramienta `Agent` y los subagentes no pueden lanzar subagentes. Si `Agent` no está disponible, la skill DEBE informarlo y detenerse: cinco propuestas independientes no pueden simularse en un único hilo. NO DEBE simular agentes ni afirmar haberlos lanzado.

1.3. Agentes del ecosistema, definidos en `.claude/agents/<nombre>.md`:

| Agente | Responsabilidad | Modifica archivos | Worktree |
|---|---|---|---|
| `test-analyzer` | Analiza el proyecto, registra la línea base y prepara el informe de contexto | No (solo ejecuta la suite) | Sí |
| `test-creator` | Desarrolla una propuesta CREATE completa en su rama candidata | Sí, solo tests | Sí |
| `test-falsifier` | Verifica una única propuesta siguiendo `.claude/agents/code-falsador.md` | No (mutaciones temporales sin commit) | Sí |
| `test-evaluator` | Compara las propuestas y sus informes y elige una ganadora | No | No |
| `test-repairer` | Repara y refuerza la suite ganadora en su rama candidata | Sí, solo tests | Sí |
| `test-integrator` | Integra la ganadora, valida y publica | Sí (merges) | Sí |

1.4. Se lanzan con la herramienta `Agent` y `subagent_type: "<nombre>"`. Si el tipo no está disponible en la sesión (p. ej. porque los agentes se crearon después de arrancarla), se lanza `general-purpose` con un prompt que empiece por «Lee `.claude/agents/<nombre>.md` y sigue sus instrucciones como si fueran tu definición de agente», seguido del mismo contenido, y DEBE informarse en el informe final.

1.5. Los agentes que ejecutan Maven o tocan archivos DEBEN lanzarse con `isolation: "worktree"`; cada uno trabaja solo en su worktree y su rama (o en `HEAD` desacoplado si no debe commitear). Ningún agente modifica la rama de otro.

1.6. Las cinco propuestas de una fase DEBEN tener una oportunidad equivalente: los cinco agentes se lanzan en un mismo mensaje, en paralelo, con el mismo prompt salvo el número de propuesta y el nombre de la rama, y desde el mismo commit base. `SendMessage` solo PUEDE usarse con un agente para que complete una entrega incompleta (informe, commit), nunca para que mejore su propuesta después de conocer las demás.

1.7. Los informes se intercambian a través del coordinador: este conserva literalmente el informe de cada agente y lo pasa íntegro en el prompt de los agentes posteriores que lo necesiten. Los informes NO se commitean en el repositorio.

1.8. Cada prompt DEBE incluir: tipo de testing, fase, ruta del perfil del tipo, ruta de este archivo, ruta de `.claude/skills/k-back-testing/SKILL.md`, informe del `test-analyzer` íntegro, requisitos y autorizaciones del usuario (sección 3.3), rama y commit base exactos, número de propuesta si procede, y los informes previos que indica cada fase.

## 2. Ramas y Git

2.1. Nombres. El coordinador elige una rama base `<base>` = `feature/test-<tipo>-<descripcion>` en kebab-case. Las candidatas derivan de ella:

| Rama | Parte de | Publicada |
|---|---|---|
| `<base>` | `origin/develop` (o la rama indicada al reparar, sección 7.1) | Sí |
| `<base>-create-1` … `<base>-create-5` | El mismo commit que `<base>` (`B0`) | No |
| `<base>-repair-1` … `<base>-repair-5` | `<base>` tras integrar la ganadora CREATE (`B1`) | No |

Las candidatas solo se commitean en local: los worktrees comparten las referencias del repositorio, así que falsadores, evaluador e integrador las ven sin publicarlas, y se evita publicar diez ramas descartables por ejecución. NO DEBEN borrarse.

2.2. Preparación. Un subagente de preparación sin worktree aplica `.claude/skills/do-code-impl/procedimiento-comun.md` sección 4.1 (repositorio limpio, `git fetch origin`, `origin/develop` existente, `develop` solo por fast-forward), comprueba que ni `<base>` ni sus candidatas existen en local ni en el remoto, y crea las ramas con `git branch <rama> <commit>` sin desprotegerlas. Devuelve los nombres y el hash del commit base. Las candidatas REPAIR se crean en una segunda preparación, tras la sección 5.

2.3. Cada agente que commitea hace `git switch <su rama>` en su worktree. Commits según el procedimiento común sección 4.3 (mensajes en español con prefijo `test:` o `fix:` según corresponda, línea `Co-Authored-By`, archivos añadidos por ruta). Antes de terminar, el agente DEBE dejar su worktree en `git switch --detach` para que la rama pueda desprotegerse en otro worktree.

2.4. Se aplican las prohibiciones del procedimiento común sección 4.6: sin `push --force`, sin `git pull`, sin `reset --hard` sobre trabajo ajeno, sin borrar ramas.

## 3. Análisis

3.1. Si la skill no ha recibido un informe de `test-analyzer` del coordinador que la invoca, DEBE lanzar uno con `isolation: "worktree"` sobre el commit del que partirá `<base>`.

3.2. El informe incluye la línea base: resultado de `./mvnw -B verify`, número de tests y lista completa de métodos de test (`Clase#metodo`, leída de `target/surefire-reports/*.xml`). Si la línea base no está en verde, el coordinador DEBE informar al usuario con la salida real y detenerse: no se trabaja sobre una base rota.

3.3. Puerta de decisión. Si el informe concluye que el tipo de testing necesita algo que el proyecto no tiene (dependencias como Testcontainers, Docker, una base de datos, plugin Failsafe, configuración de test), o si los requisitos son ambiguos, el coordinador DEBE preguntar al usuario con `AskUserQuestion` antes de lanzar ningún agente que modifique archivos. Las autorizaciones obtenidas se copian literalmente en todos los prompts posteriores. Sin autorización, ningún agente PUEDE añadir dependencias, plugins ni configuración (`/k-back` y `/k-back-testing` sección 7.4).

## 4. CREATE

4.1. Desarrollo. Cinco `test-creator` en paralelo (sección 1.6), cada uno en `<base>-create-<n>`. Además de lo indicado en la sección 1.8, reciben el alcance del perfil del tipo. Cada uno entrega el informe de implementación de su definición de agente. Una propuesta que no entregue informe, deje la suite en rojo o no tenga commits se marca como «no entregada»: no se evalúa ni se repite.

4.2. Falsación. Cinco `test-falsifier` en paralelo, uno por propuesta entregada. Cada uno recibe solo su rama candidata, el commit base `B0`, el informe de su `test-creator` y el informe del analizador. Ninguno conoce las demás propuestas.

4.3. Evaluación. Un `test-evaluator` recibe las propuestas entregadas (ramas e informes) y sus informes de falsación, aplica los criterios CREATE de la sección 6 y elige una única ganadora. Si ninguna es aceptable (sección 6.3), el coordinador DEBE detenerse e informar al usuario: no se integra una propuesta descalificada.

4.4. Integración intermedia. Un `test-integrator` en modo «base» integra la ganadora en `<base>` (sección 8.1) y devuelve el commit `B1`. REPAIR NO DEBE empezar sin `B1` publicado y en verde.

## 5. REPAIR

5.1. Informe de partida. Los reparadores DEBEN recibir un informe de falsación de la base: el de la propuesta ganadora en CREATE, o, si REPAIR se ejecuta sin CREATE previo, el de un `test-falsifier` lanzado sobre `B1` con alcance «tests existentes del tipo».

5.2. Desarrollo. Cinco `test-repairer` en paralelo (sección 1.6), cada uno en `<base>-repair-<n>`, todos desde `B1`. Reciben además el informe de falsación de partida y, si existe, el informe de evaluación CREATE.

5.3. Alcance de REPAIR: mejorar los tests. NO DEBE modificarse código de producción (`src/main`) para que un test pase. Un defecto real de producción detectado se documenta en la sección «Defectos de producción» del informe, con el test que lo demuestra y su salida, y ese test NO se commitea en rojo ni desactivado (`/k-back-testing` sección 8.2). Solo con autorización expresa del usuario (sección 3.3) PUEDE corregirse, en un commit `fix:` separado de los commits `test:`.

5.4. Falsación. Cinco `test-falsifier` en paralelo, uno por rama reparada, que reciben además el informe de falsación de partida para comprobar qué hallazgos se han resuelto y si se han introducido defectos nuevos.

5.5. Evaluación. Un `test-evaluator` con los criterios REPAIR de la sección 6.

## 6. Criterios de evaluación

6.1. Escala de gravedad de los hallazgos (la usan falsadores y evaluador):

| Gravedad | Ejemplos |
|---|---|
| CRÍTICA | Test que pasa sin verificar lo que afirma (`MENTIRA` según `code-falsador.md`); suite en rojo; tests existentes borrados, desactivados o debilitados; producción modificada sin autorización; dependencia añadida sin autorización |
| ALTA | Mutante relevante que sobrevive; aserción insuficiente sobre una regla de negocio o un código HTTP; test no determinista |
| MEDIA | Caso límite o negativo exigido por `/k-back-testing` sección 6 ausente; dobles de prueba contrarios a la sección 2.8 |
| BAJA | Nombres, estructura o estilo contrarios a la sección 2 sin efecto en lo que se verifica |

6.2. Orden de prioridad, de mayor a menor. La fiabilidad prima sobre la cantidad: más tests NO es mejor si verifican menos.

- CREATE: corrección; fiabilidad (hallazgos CRÍTICA y ALTA del falsador, mutantes que sobreviven); calidad de las aserciones; cobertura de los comportamientos del alcance; resultados de ejecución; compatibilidad con el proyecto y sus convenciones; mantenibilidad y legibilidad; menor complejidad.
- REPAIR: ausencia de regresiones (ningún test ni aserción perdidos respecto a `B1`); hallazgos de partida resueltos; ausencia de falsos positivos; corrección y fiabilidad; estabilidad (determinismo); cobertura; cumplimiento de `code-falsador.md` y `/k-back-testing`; resultados de ejecución; compatibilidad; mantenibilidad.

6.3. Descalifican una propuesta: suite en rojo, algún hallazgo CRÍTICA no refutado, o cambios fuera del alcance de testing sin autorización. Desempate: menos hallazgos ALTA, después menos mutantes supervivientes, después el diff más pequeño.

## 7. Variantes de entrada

7.1. `/test-<tipo>-repair` sin CREATE previo: `<base>` parte de `origin/develop` o de la rama indicada en `$ARGUMENTS` (comprobando que existe y está publicada), y `B1` es ese mismo commit. Se aplica la sección 5.1.

7.2. Desde `/do-test`: la especializada CREATE termina en la sección 4.4 y entrega `<base>` y `B1` a `/do-test`, que sigue con la especializada REPAIR sin repetir el análisis ni la preparación de `<base>`.

7.3. `/test-<tipo>-create` sola: tras la sección 4.4, el coordinador lanza el `test-integrator` en modo «final» solo para integrar `<base>` en `develop` (sección 8.2, pasos 4 a 6).

## 8. Integración

8.1. Modo «base» (tras una evaluación): en su worktree, `git switch <base>`, `git merge --no-ff <ganadora>` con mensaje `chore: integrar <ganadora> en <base>` y `Co-Authored-By`, `./mvnw -B verify`, comprobaciones de la sección 8.3 frente al commit de partida de la candidata, `git push -u origin <base>` y `git switch --detach`. Devuelve el hash resultante.

8.2. Modo «final» (tras REPAIR):

1. Integra la ganadora REPAIR en `<base>` como en la sección 8.1.
2. Resuelve conflictos solo si la resolución es segura y evidente; si no, aborta el merge y se detiene.
3. Ejecuta `./mvnw -B verify` y las comprobaciones de la sección 8.3 frente a `B1` y frente a la línea base del analizador.
4. Integra `<base>` en `develop` según el procedimiento común sección 4.5 (fetch, `develop` por fast-forward o `HEAD` desacoplado, `merge --no-ff`, verificación global antes de publicar, push sin forzar, actualización del `develop` local).
5. Si `develop` avanzó y el merge produce conflictos no evidentes, se detiene y lo informa; el coordinador PUEDE proponer `/do-code-repair`.
6. Verifica que `origin/develop` y el `develop` local tienen el mismo hash, o informa del motivo.

8.3. Comprobaciones de no regresión de la suite, con evidencia en el informe:

- Ningún método de test de la referencia falta en el resultado; un test renombrado o movido DEBE estar justificado en el informe de su autor con su correspondencia `antes → después`.
- El número de tests no es menor que el de la referencia.
- No se han añadido `@Disabled`, `assumeTrue`, `-DskipTests` ni exclusiones de Surefire.
- En el diff, cada línea de aserción o `verify` eliminada tiene una sustituta igual o más estricta, o está justificada.
- `src/main` no ha cambiado, salvo commits `fix:` autorizados.
- `pom.xml` y la configuración solo han cambiado según las autorizaciones de la sección 3.3.

## 9. Informe final del coordinador

1. Tipo de testing, fases ejecutadas, ramas y commits base (`B0`, `B1`, hash final).
2. Resumen del análisis y decisiones del usuario de la puerta de la sección 3.3.
3. Agentes lanzados por fase (tipo, rama, resultado) y si se usó la variante `general-purpose` de la sección 1.4.
4. Tabla de propuestas CREATE: tests añadidos, resultado de la suite, hallazgos por gravedad y mutantes supervivientes; ganadora y justificación del evaluador.
5. La misma tabla para REPAIR, con los hallazgos de partida resueltos y pendientes.
6. Resultado de la integración final y de las comprobaciones de la sección 8.3.
7. Defectos de producción detectados, separados de los cambios de testing, y si se autorizó su corrección.
8. Informe Git según el procedimiento común sección 4.7, con la lista de ramas candidatas conservadas en local.
9. Carencias conocidas y trabajo pendiente.
