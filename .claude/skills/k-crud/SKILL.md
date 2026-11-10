---
name: k-crud
description: Especificación técnica y auditoría reparadora de CRUD - criterios de CREATE, READ, UPDATE (PUT frente a PATCH) y DELETE, capas del backend, API REST, frontend, seguridad y calidad adaptados a la arquitectura existente, y bucle obligatorio de al menos cinco ciclos independientes de revisión y reparación, cada uno en un subagente nuevo, que corrige los incumplimientos del CRUD existente. Úsala cuando el usuario pida auditar o corregir un CRUD; /do-crud la sigue como especificación y revisión final. No crea CRUD nuevos. Modifica código. Hace commits, push e integración.
argument-hint: "<recurso o módulo a auditar> [alcance o requisitos, opcional]"
disable-model-invocation: true
---

# /k-crud

## Propósito

Definir cómo debe diseñarse, implementarse y mantenerse un CRUD en este proyecto y auditar los CRUD existentes corrigiendo sus incumplimientos. Es la especificación técnica que sigue `/do-crud` y el auditor de calidad que la revisa.

A diferencia del resto de skills `k-*`, que solo contienen normas, esta skill **sí modifica código** en su fase de auditoría (sección 7). NO DEBE implementar un CRUD nuevo desde cero: eso es de `/do-crud`.

Las normas de cada capa del backend ya tienen propietaria (`/k-back` y las `/k-back-*`); esta skill NO las repite: las aplica al CRUD y solo añade los criterios propios de un CRUD que ninguna cubre. Si esta skill discrepa de una `/k-back-*`, prevalece la `/k-back-*`.

Los términos DEBE, NO DEBE y PUEDE son normativos.

## Cuándo usarla

- El usuario pide revisar, auditar o corregir el CRUD de un recurso.
- Desde `/do-crud`: como especificación antes de implementar y como revisión obligatoria al terminar.

## Entradas

- `$ARGUMENTS`: recurso o módulo que se audita (p. ej. `terceros`) y, opcionalmente, alcance o requisitos.
- Si no se indica recurso, DEBE preguntarse con `AskUserQuestion` ofreciendo los módulos de negocio existentes. NO DEBE auditarse todo el proyecto sin confirmación.

## Resultado esperado

- CRUD auditado y corregido en una rama `feature/crud-auditoria-<recurso>` integrada en `develop` (invocada directamente), o commits en la rama recibida (desde `/do-crud` o en modo subagente).
- Registro de al menos cinco ciclos completos de revisión y reparación.
- Informe final (sección 10).

## Skills y archivos relacionados

| Recurso | Ruta | Para qué |
|---|---|---|
| `/k-back` | `.claude/skills/k-back/SKILL.md` | Etapa, tabla canónica, dependencias, nombres, errores, validación, seguridad, comentarios, antiabstracción, preservación |
| `/k-back-controller` | `.claude/skills/k-back-controller/SKILL.md` | Endpoints, verbos, códigos, `@Valid`, `ProblemDetail` |
| `/k-back-dto` | `.claude/skills/k-back-dto/SKILL.md` | DTO de entrada y salida, Bean Validation, mapeo |
| `/k-back-service` | `.claude/skills/k-back-service/SKILL.md` | Casos de uso, transacciones, permisos |
| `/k-back-domain` | `.claude/skills/k-back-domain/SKILL.md` | Modelo, invariantes, excepciones |
| `/k-back-repository` | `.claude/skills/k-back-repository/SKILL.md` | Interfaces Repository y operaciones CRUD |
| `/k-back-persistence` | `.claude/skills/k-back-persistence/SKILL.md` | Entidades, DAO, mappers, Flyway |
| `/k-back-module` | `.claude/skills/k-back-module/SKILL.md` | Límites, facade, dependencias entre módulos |
| `/k-back-testing` | `.claude/skills/k-back-testing/SKILL.md` | Verificaciones V1-V7 y comandos |
| `/do-test` | `.claude/skills/do-test/SKILL.md` | Crear o reparar tests cuando hagan falta |
| Procedimiento común | `.claude/skills/do-code-impl/procedimiento-comun.md` | Modos, subagentes y flujo Git (secciones 1 y 4) |
| Subagentes propios | `.claude/skills/k-crud/subagents/` | `analizador.md`, `ciclo.md`, `verificador.md` |
| Subagentes compartidos | `.claude/skills/do-code-impl/subagents/` | `preparacion-git.md`, `integrador.md` |

## Normas

### 1. Análisis inicial

1.1. Antes de evaluar o modificar nada DEBE analizarse:

1. La arquitectura existente y su etapa (`/k-back` sección 2).
2. Lenguaje, framework y tecnologías (`pom.xml` y, si existe, el `package.json` del frontend).
3. La organización de los módulos (`/k-back-module` secciones 1 a 4).
4. Los CRUD existentes, para detectar convenciones (p. ej. `FacturaController` y sus casos de uso).
5. Las capas y sus responsabilidades.
6. Cómo se gestionan errores, validaciones y excepciones (manejadores del módulo, `ProblemDetail`).
7. Cómo se accede a los datos (Repository, DAO, mappers).
8. Cómo se comunican frontend y backend, si existen ambos.

1.2. NO DEBE imponerse una arquitectura nueva ni introducirse tecnologías, dependencias o librerías que el proyecto no use. Los criterios de esta skill se adaptan a la arquitectura real y a la etapa detectada; se inspecciona y preserva el código según `/k-back` sección 14.

### 2. Especificación de las operaciones

Cada criterio indica su norma propietaria; lo que aparece sin propietaria es propio de esta skill.

#### 2.1. CREATE

| Criterio | Norma |
|---|---|
| Recepción con `@Valid @RequestBody` sobre un DTO `record` de entrada | `/k-back-controller` §4, `/k-back-dto` §1 y §3 |
| Validaciones de formato en el DTO; invariantes en el dominio | `/k-back-dto` §3, `/k-back-domain` §4 |
| Reglas de negocio (unicidad, existencia de referencias) en el caso de uso o el dominio, nunca en el controlador | `/k-back-service` §2, `/k-back-domain` §4 |
| Creación del modelo de dominio a través de su constructor o factoría, que garantiza las invariantes | `/k-back-domain` §3 y §4 |
| Persistencia mediante la interfaz Repository dentro de la transacción del caso de uso | `/k-back-repository` §4, `/k-back-service` §5 |
| Respuesta `201 Created` con `Location` y DTO de salida | `/k-back-controller` §3.4 |
| Errores de dominio traducidos a `ProblemDetail` (`400`, `409`) en el manejador del módulo | `/k-back-controller` §5 |

#### 2.2. READ

| Criterio | Norma |
|---|---|
| Consulta individual por identificador en la ruta: `200` con DTO o `404` si no existe | `/k-back-controller` §3 y §5 |
| Consulta de colecciones: `200` con lista (vacía si no hay registros, nunca `404`) | `/k-back-controller` §3 |
| Transformación a DTO; nunca se exponen entidades ni modelos | `/k-back-dto` §4 y §5 |
| Consultas de solo lectura sin efectos (`@Transactional(readOnly = true)` si es la convención) | `/k-back-service` §5, `/k-back-persistence` §6 |

2.2.1. Filtros, paginación y ordenación DEBEN añadirse solo con requisito o cuando el CRUD del proyecto ya los use. Si se añaden: parámetros de consulta (`?page=&size=&sort=`) en el controlador, `Pageable` NO DEBE cruzar al dominio (el Repository recibe tipos Java estándar, `/k-back-repository` §3), tamaño de página acotado y orden por defecto determinista.

2.2.2. El registro inexistente DEBE señalarse con la excepción de dominio «no encontrado» del módulo, lanzada en el caso de uso; NO DEBE devolverse `null` ni `200` vacío.

#### 2.3. UPDATE

| Criterio | Norma |
|---|---|
| Identificador en la ruta; el del cuerpo, si existe, NO DEBE prevalecer sobre el de la ruta | Propia |
| Comprobación de existencia antes de modificar: `404` si no existe | `/k-back-controller` §5 |
| Validación del DTO de entrada | `/k-back-dto` §3 |
| Cambios aplicados con métodos del modelo que mantienen las invariantes; sin setters públicos que las salten | `/k-back-domain` §3 y §4 |
| Persistencia de la modificación dentro de la transacción del caso de uso | `/k-back-service` §5 |
| Devolución de la información actualizada: `200` con DTO | Propia |

2.3.1. **PUT frente a PATCH**:

- `PUT /api/<recurso>/{id}` sustituye el estado modificable completo. El DTO DEBE exigir todos los campos obligatorios, como en la creación. Un campo omitido es un error de validación, no «sin cambios».
- `PATCH /api/<recurso>/{id}` modifica solo los campos presentes. DEBE distinguirse «ausente» (no se toca) de «presente con `null`» (se borra, solo si el campo es opcional). Las invariantes se comprueban sobre el estado resultante, no sobre el parcial.
- Solo DEBE implementarse el verbo que pidan los requisitos. Si el proyecto ya usa uno, se sigue su convención. NO DEBEN añadirse los dos sin requisito.
- Los campos no modificables (identificador, número legal, fecha de creación, propietario) NO DEBEN aceptarse en el DTO de actualización.

#### 2.4. DELETE

| Criterio | Norma |
|---|---|
| Identificador en la ruta y comprobación de existencia: `404` si no existe | `/k-back-controller` §3 y §5 |
| Restricciones de negocio (registros dependientes, estados que impiden borrar) comprobadas en el caso de uso con una excepción de dominio, traducida a `409 Conflict` en el manejador | `/k-back-domain` §6, `/k-back-controller` §5 |
| Respuesta `204 No Content` sin cuerpo | `/k-back-controller` §3.4 |

2.4.1. El borrado DEBE ser físico salvo que el proyecto o los requisitos ya usen borrado lógico (anulación, desactivación) para ese recurso. NO DEBE introducirse borrado lógico sin requisito. Si es lógico, las lecturas DEBEN excluir los registros desactivados o señalarlos explícitamente.

2.4.2. Si el registro tiene dependientes en otro módulo, la comprobación DEBE hacerse por la API pública de ese módulo (`/k-back-module` §5), nunca consultando sus tablas o entidades.

### 3. Backend por capa

3.1. Cada responsabilidad DEBE estar en la capa y el paquete de la etapa vigente (tabla canónica de `/k-back` §2):

| Responsabilidad | Norma propietaria |
|---|---|
| Controller / HTTP: recibe la petición, valida, delega en un caso de uso y responde | `/k-back-controller` §2 |
| DTO de entrada y salida | `/k-back-dto` |
| Casos de uso: un caso de uso por operación, orquestación, transacción y permisos | `/k-back-service` §2, §5 y §6 |
| Dominio: reglas, invariantes y excepciones | `/k-back-domain` |
| Repository: contrato definido desde el dominio | `/k-back-repository` |
| Persistencia: entidad, DAO, implementación de Repository y migración Flyway | `/k-back-persistence` |
| Mappers: entidad ↔ dominio en persistencia; DTO ↔ dominio en aplicación o controlador | `/k-back-persistence` §7, `/k-back-dto` §4 |

3.2. En arquitectura modular:

- Un módulo NO DEBE acceder a implementaciones internas de otro; solo a su API pública o facade (`/k-back-module` §5 y §6).
- NO DEBEN crearse dependencias circulares (`/k-back-module` §4 y §7).
- El dominio NO DEBE depender de Spring, JPA ni base de datos (`/k-back-domain` §2).
- NO DEBEN mezclarse responsabilidades entre capas (`/k-back` §3).

### 4. API REST

4.1. Verbos, rutas en plural, identificador en la ruta, códigos y validación siguen `/k-back-controller` §3 y §4. Además, para las operaciones de modificación:

| Operación | Respuesta correcta |
|---|---|
| `PUT /api/<recurso>/{id}` | `200 OK` con el DTO actualizado |
| `PATCH /api/<recurso>/{id}` | `200 OK` con el DTO actualizado |
| Error de validación | `400` |
| Recurso inexistente | `404` |
| Conflicto de negocio (duplicado, dependientes) | `409` |

Si el proyecto ya devuelve `204` en las modificaciones, se respeta esa convención.

4.2. Los contratos de entrada y salida son DTO `record` del módulo. Los controladores NO DEBEN devolver entidades de persistencia ni modelos de dominio (`/k-back-dto` §5).

4.3. Los errores DEBEN tratarse de forma uniforme: `ProblemDetail` en el manejador del propio módulo, igual que el resto de módulos (`/k-back-controller` §5).

4.4. La documentación OpenAPI/Swagger DEBE actualizarse solo si el proyecto ya la usa. NO DEBE añadirse la dependencia.

### 5. Frontend

Esta sección aplica solo si el proyecto tiene frontend. Hoy Facturación no lo tiene: se registra «no aplica» y NO DEBE crearse uno.

5.1. Si existe, el CRUD del frontend DEBE cubrir, siguiendo el framework y las convenciones existentes:

| Elemento | Criterio |
|---|---|
| Servicio HTTP | Un servicio por recurso con un método por endpoint; URL base de la configuración de entorno existente |
| Modelos tipados | Interfaces o tipos que replican exactamente los DTO del backend (nombres y tipos de campo) |
| DTO de comunicación | Tipos de petición separados de los de respuesta cuando el backend los separa |
| Listado | Muestra la colección, con paginación si el backend la ofrece |
| Formulario de creación | Validaciones equivalentes a las del DTO de entrada del backend |
| Formulario de edición | Carga el registro, envía PUT o PATCH según el contrato y no permite editar campos no modificables |
| Eliminación | Confirmación previa y tratamiento de `404` y `409` |
| Estados de carga | Indicador visible y controles deshabilitados durante las peticiones |
| Gestión de errores | Muestra el `detail` del `ProblemDetail`; no expone trazas |
| Validaciones | En el cliente para la usabilidad; el backend sigue siendo la autoridad |
| Actualización de la interfaz | Tras cada operación correcta, la vista refleja el estado del servidor |

5.2. Con Angular DEBEN respetarse su arquitectura, servicios inyectables, componentes, signals y convenciones existentes (standalone o módulos, formularios reactivos o de plantilla, según use el proyecto). NO DEBEN crearse componentes o servicios genéricos que el proyecto no use.

### 6. Seguridad y calidad

6.1. DEBE comprobarse:

- Validación de toda entrada (`/k-back` §9.2, `/k-back-dto` §3).
- Protección frente a modificaciones no autorizadas y comprobación de permisos si existe autenticación (`/k-back-service` §6 y la configuración de seguridad del módulo `auth`).
- Que no se expone información sensible: contraseñas, tokens, trazas, SQL ni nombres internos (`/k-back` §9, `/k-back-controller` §5.5).
- Gestión correcta de identificadores: los genera el servidor, se toman de la ruta y no se aceptan en el cuerpo de creación.
- Integridad de los datos: restricciones en la migración coherentes con las invariantes del dominio (`/k-back-persistence` §10).
- Ausencia de lógica de negocio en los controladores (`/k-back-controller` §2.2).

### 7. Auditoría y reparación obligatoria

Es la responsabilidad principal de la skill: al invocarla DEBE auditarse el CRUD indicado y DEBEN corregirse los incumplimientos.

#### 7.1. Modo, subagentes y Git

7.1.1. El modo se determina según `procedimiento-comun.md` §1.2:

- **Coordinador**: invocada por el usuario, o seguida por `/do-crud` en el hilo principal. El hilo principal NO DEBE revisar ni corregir código: lanza los subagentes de la tabla 7.1.2, supervisa sus informes y decide.
- **Subagente**: un subagente sin herramienta `Agent`. Hace él mismo todos los pasos, uno tras otro.
- **Sin herramienta `Agent`** en el hilo principal: se aplica `procedimiento-comun.md` §1.5. NO DEBEN simularse subagentes.

7.1.2. Subagentes en modo coordinador, lanzados según `procedimiento-comun.md` §1.7:

| Paso | Subagente | Worktree | Devuelve |
|---|---|---|---|
| Preparación Git (solo si se invoca directamente) | `.claude/skills/do-code-impl/subagents/preparacion-git.md`, rama `feature/crud-auditoria-<recurso>` | No | Rama creada y hash de `origin/develop` |
| Análisis inicial (sección 1) | `.claude/skills/k-crud/subagents/analizador.md` | Sí, sin cambios (ejecuta Maven) | Informe de contexto: etapa, clases del CRUD por capa, convenciones, operaciones existentes, frontend sí/no, línea base de `./mvnw -B test` |
| Cada ciclo (7.2) | `.claude/skills/k-crud/subagents/ciclo.md`, **un subagente nuevo por ciclo** | Sí, sobre la rama de trabajo | Registro del ciclo (7.4), commits y push de la rama |
| Verificación final (8.1) | `.claude/skills/k-crud/subagents/verificador.md` | Sí, sin cambios | Informe de verificación |
| Integración (solo si se invoca directamente) | `.claude/skills/do-code-impl/subagents/integrador.md` | Sí | Hash final de `origin/develop` y resultado de `./mvnw -B verify` |

7.1.3. El prompt de cada subagente de ciclo DEBE incluir:

- Número de ciclo y enfoque asignado (7.3).
- Rama de trabajo y «modo subagente».
- Ruta de este archivo y de `procedimiento-comun.md`.
- Las `/k-back-*` que debe leer.
- El informe del analizador.
- El registro de los ciclos anteriores: solo hallazgos, correcciones y commits, sin sus conclusiones de «todo correcto».
- El formato de registro que debe devolver (7.4).

7.1.4. Los ciclos son **secuenciales**: cada uno parte del último commit publicado por el anterior. Un subagente nuevo por ciclo garantiza que cada revisión empiece sin el sesgo del ciclo anterior.

7.1.5. Si la línea base del analizador no está en verde, DEBE informarse con la salida real y detenerse: no se audita sobre una base rota.

#### 7.2. Bucle obligatorio

Cada ciclo DEBE ser COMPLETO y seguir estos pasos:

1. Analizar la implementación existente del CRUD completo (todas las capas y, si existe, el frontend), leyendo el código actual y no un resumen.
2. Compararla con **todos** los criterios de las secciones 2 a 6 y con las normas propietarias que referencian.
3. Identificar errores, malas prácticas e incumplimientos, con archivo y línea.
4. Priorizarlos: crítico (rompe el contrato, pierde datos o es un fallo de seguridad), alto (incumple una norma DEBE), medio y bajo.
5. Corregir directamente en el código los problemas, por orden de prioridad.
6. Comprobar que no hay regresiones con las verificaciones V1-V7 de `/k-back-testing` (sección «Verificaciones por iteración»), con sus resultados reales.
7. Registrar qué se ha corregido (7.4) y hacer commit por unidad lógica (`procedimiento-comun.md` §4.3), con push de la rama.
8. Volver a analizar el CRUD completo para confirmar el estado con el que empieza el ciclo siguiente.

DEBEN hacerse como mínimo **5 ciclos completos**.

#### 7.3. Independencia de los ciclos

7.3.1. Cada ciclo lo hace un subagente nuevo, revisa de nuevo el CRUD entero contra todos los criterios y, además, profundiza en un enfoque distinto:

| Ciclo | Enfoque principal |
|---|---|
| 1 | Contratos HTTP: verbos, rutas, códigos, DTO, validación de entrada, PUT/PATCH |
| 2 | Dominio: invariantes, reglas de negocio, excepciones, modelo inmutable o protegido |
| 3 | Persistencia y transacciones: Repository, DAO, mappers, migración, consultas, límites transaccionales |
| 4 | Seguridad y errores: permisos, exposición de datos, identificadores, `ProblemDetail`, casos límite |
| 5 | Módulos e integración: dependencias entre módulos, facade, frontend-backend, regresiones y tests |
| 6 y siguientes | Los hallazgos pendientes y las áreas donde los ciclos anteriores encontraron más problemas |

7.3.2. NO DEBEN contarse como ciclos varias comprobaciones superficiales del mismo código ni la repetición de una verificación sin un análisis nuevo.

7.3.3. Un ciclo sin hallazgos DEBE registrarse con la evidencia revisada (archivos leídos, criterios comprobados, comandos ejecutados). NO DEBEN fabricarse cambios para justificar un ciclo.

#### 7.4. Registro de ciclos

| Ciclo | Enfoque | Hallazgos (prioridad, archivo:línea) | Correcciones | V1-V7 | Commits | Pendientes |
|---|---|---|---|---|---|---|

#### 7.5. Regla de continuidad

7.5.1. Después del quinto ciclo, si siguen existiendo incumplimientos conocidos que puedan corregirse, el coordinador DEBE lanzar otro ciclo. NO DEBE declararse terminada la revisión mientras queden errores conocidos corregibles.

7.5.2. Un problema que no puede resolverse por falta de información o por una limitación externa DEBE documentarse como bloqueo, con su causa y lo que haría falta para resolverlo. Si falta información del usuario, se pregunta.

7.5.3. Si un hallazgo vuelve a aparecer dos ciclos seguidos sin que haya cambiado el código ni la hipótesis, se aplica `procedimiento-comun.md` §2.2.2: se detiene y se informa el bloqueo.

7.5.4. NO DEBEN introducirse cambios que contradigan la arquitectura del proyecto, ni siquiera para cerrar un hallazgo.

### 8. Verificación final

8.1. Al terminar los ciclos, el coordinador lanza el subagente `subagents/verificador.md` (con worktree, sin cambios, sobre la rama de trabajo), que DEBE comprobar:

- Que las cuatro operaciones CRUD se comportan según la sección 2.
- Los contratos HTTP (sección 4).
- La coherencia entre frontend y backend, si hay frontend.
- Las reglas de negocio.
- Las dependencias entre módulos (`ArquitecturaTest`, `Regla*Test` y `ModulosTest` si existe).
- La ausencia de regresiones: `./mvnw -B verify` en verde y número de tests igual o mayor que en la línea base.

8.2. Si hacen falta tests nuevos o reparar los existentes, DEBE leerse `.claude/skills/do-test/SKILL.md` y seguirse en el hilo principal por cada tipo necesario (desde `/do-crud`, eso se hace en su paso de testing). Los criterios de calidad de testing son de `/k-back-testing` y `/k-back-test-<tipo>`: esta skill NO los sustituye.

8.3. Publicación:

- Invocada directamente: push e integración en `develop` con el subagente integrador (`procedimiento-comun.md` §4.4 y §4.5).
- Desde `/do-crud` o en modo subagente: solo commits y push en la rama recibida; la integración es de quien la invocó.

### 9. Restricciones

- NO DEBEN rehacerse partes que ya funcionan y cumplen los criterios.
- NO DEBE cambiarse la arquitectura sin justificación documentada (`/k-back` §11).
- NO DEBEN añadirse funcionalidades ajenas al CRUD.
- NO DEBE duplicarse código.
- NO DEBEN inventarse requisitos, campos, endpoints ni reglas; las dudas se preguntan.
- NO DEBEN eliminarse funcionalidades existentes.
- NO DEBEN añadirse comentarios innecesarios (`/k-back` §12).
- DEBEN respetarse las convenciones de nombres (`/k-back` §4).
- DEBEN preferirse soluciones simples, mantenibles y coherentes con el resto del proyecto (`/k-back` §13).
- Git: rigen las prohibiciones de `procedimiento-comun.md` §4.6.

### 10. Informe final

1. Recurso auditado, etapa y modo de ejecución.
2. Subagentes lanzados (paso, rama, resultado).
3. Informe del analizador y línea base.
4. Registro de ciclos (7.4), con el total de ciclos y el motivo de parar.
5. Correcciones aplicadas por prioridad.
6. Resultado de la verificación final (sección 8), con el número de tests antes y después.
7. Bloqueos y pendientes.
8. Informe Git (`procedimiento-comun.md` §4.7).

## Comprobaciones antes de finalizar

- [ ] El análisis inicial se ha hecho antes de modificar nada y la línea base estaba en verde.
- [ ] Ningún criterio aplicado contradice las `/k-back-*` ni impone arquitectura o tecnologías nuevas.
- [ ] Se han hecho al menos cinco ciclos completos, cada uno en un subagente nuevo (o, sin `Agent`, informado), con su enfoque y su registro; ninguno es superficial.
- [ ] Tras el quinto ciclo no quedan incumplimientos conocidos corregibles; los no corregibles están documentados como bloqueos.
- [ ] Las verificaciones V1-V7 de cada ciclo y la verificación final tienen su resultado real.
- [ ] El número de tests no ha bajado y `./mvnw -B verify` está en verde.
- [ ] No se ha creado un CRUD nuevo ni añadido funcionalidad ajena al CRUD.
- [ ] Commits con `Co-Authored-By`, push e integración hechos, o rama entregada a quien invocó.
