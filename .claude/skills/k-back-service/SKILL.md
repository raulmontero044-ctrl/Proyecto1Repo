---
name: k-back-service
description: Implementa la capa de aplicación y dominio de un módulo backend Java/Spring Boot - casos de uso en apln, modelos y servicios de dominio, servicios internos en service y transacciones - usando contratos Repository existentes. Úsala cuando haya que crear o modificar casos de uso o reglas de negocio de un módulo. No crea persistencia, controladores ni endpoints.
argument-hint: "<módulo> <casos de uso o requisitos a implementar>"
---

# k-back-service

Implementa la lógica de aplicación y de dominio de un módulo backend.

Entrada recibida: $ARGUMENTS

## Reglas comunes

Aplica `.claude/rules/architecture.md`. Si no está en contexto, léelo antes de empezar. Esta Skill solo detalla lo específico de la capa de aplicación y dominio; ante cualquier duda prevalece ese archivo.

## Entradas

- Módulo afectado.
- Casos de uso, reglas de negocio e invariantes a implementar.
- Opcionalmente, referencia a un plan o tarea concreta (`fichero`, `funcion`, `descripcion`).

Si la entrada no identifica el módulo o los casos de uso (salvo en el encargo de solo modelo de dominio), pide aclaración antes de escribir código.

### Encargo de solo modelo de dominio

La entrada puede pedir solo el modelo de dominio: modelos, objetos de valor, invariantes y excepciones de `domain/model`, sin casos de uso. `/k-back-module` lo invoca así antes de `/k-back-repository` en módulos nuevos. En ese caso:

- Implementa solo `domain/model` y sus pruebas unitarias.
- No crees casos de uso, servicios ni `record` de `api/dto`.
- No exijas que existan interfaces Repository: el paso 5 del procedimiento y su bloqueo no se aplican.
- Aplica la verificación que afecta a `domain`.

## Alcance permitido

Dentro del módulo indicado:

- `apln/`: casos de uso y orquestación de operaciones de aplicación.
- `domain/model/`: modelos, objetos de valor, invariantes y excepciones de dominio que los casos de uso necesiten. Esta Skill es la propietaria de las reglas de negocio del modelo.
- Servicios de dominio puros en `domain` (architecture.md §3.1).
- `service/`: servicios de dominio o internos solo cuando tengan una responsabilidad diferenciada que no encaje en un caso de uso ni en el modelo.
- `api/dto/`: `record` de entrada y salida de los casos de uso y contratos públicos que el módulo ofrezca a otros módulos.
- Pruebas unitarias de lo anterior, siguiendo las convenciones de pruebas existentes.

## Contrato de los casos de uso

- Los casos de uso reciben y devuelven `record` de `api/dto` o tipos simples de Java. Nunca exponen modelos de dominio ni entidades de persistencia en su firma pública.
- La conversión entre `api/dto` y `domain/model` se realiza dentro de `apln`.
- El límite transaccional se sitúa en el caso de uso de `apln`, no en controladores ni en DAO.
- Las operaciones sobre datos se realizan exclusivamente mediante interfaces de `domain/repository`.
- La interacción con otros módulos se hace solo a través de sus contratos públicos y sus `record` de `api/dto`.

## Fuera de alcance

- Interfaces Repository, DAO, entidades, mappers e implementaciones de persistencia: corresponden a `/k-back-repository`.
- Controladores, endpoints, validación HTTP, códigos de estado y DTO exclusivos de HTTP: corresponden a `/k-back-controller`.
- Acceso a DAO o entidades JPA, incluso indirecto.
- Clases internas de otros módulos (controladores, servicios, repositorios, DAO, entidades, modelos).
- Servicios que solo delegan en otro objeto sin aportar lógica.
- Modificar los `record` exclusivamente HTTP que haya creado `/k-back-controller`.
- Duplicar reglas de negocio fuera del dominio o de `apln`.

## Procedimiento

1. Lee `.claude/rules/architecture.md` si no está en contexto.
2. Inspecciona el repositorio real: herramienta de build (`pom.xml`, `build.gradle`, `build.gradle.kts`), paquete raíz, módulos existentes y convenciones de nombres, transacciones, excepciones y pruebas.
3. Localiza el módulo indicado y revisa lo que ya existe en `apln`, `domain`, `service` y `api/dto` para reutilizarlo y no duplicarlo.
4. Comprueba qué interfaces de `domain/repository` necesitan los casos de uso y si existen con las operaciones requeridas.
5. Si falta una interfaz Repository o una operación, detente en esa parte y comunica el bloqueo (ver abajo). No la crees.
6. Implementa o amplía modelos de dominio, objetos de valor y excepciones necesarios, sin dependencias de Spring, JPA ni HTTP.
7. Implementa los casos de uso en `apln` y, solo si está justificado, los servicios de `service`.
8. Define en `api/dto` los `record` de entrada y salida de los casos de uso.
9. Añade o actualiza pruebas unitarias si el proyecto tiene infraestructura de pruebas.
10. Ejecuta la verificación.

No inventes campos, reglas, nombres de paquete ni operaciones que no se deriven de la entrada o del código existente. No escribas comentarios de ningún tipo en el código generado (ni de línea, ni de bloque, ni JavaDoc).

## Bloqueos que se deben comunicar

- El módulo no existe o no se puede identificar el paquete raíz.
- Falta una interfaz Repository u operación necesaria: indica el nombre propuesto, las operaciones y que corresponde a `/k-back-repository`.
- Falta un contrato público de otro módulo necesario para el caso de uso.
- Los requisitos no definen campos o reglas imprescindibles.

Un bloqueo no autoriza a implementar el trabajo de otra Skill. Implementa lo que sea posible sin inventar e informa del resto.

## Verificación

- Compila con la herramienta de build detectada y ejecuta las pruebas existentes, incluidas las de ArchUnit si las hay. Si no hay build o pruebas, indícalo.
- Comprueba que `domain` no importa `org.springframework`, `jakarta.persistence`, `javax.persistence`, `org.hibernate` ni clases de `persistence` o `controller`.
- Comprueba que `apln` y `service` no importan nada de `persistence` (DAO, entidades, mappers).
- Comprueba que ninguna firma pública de `apln` expone tipos de `domain/model` ni de `persistence`.
- Comprueba que todos los DTO creados son `record`.
- Comprueba que no se importan clases internas de otros módulos.
- Comprueba que no hay comentarios en el código generado.

## Informe final

- Archivos creados y modificados.
- Casos de uso implementados con su firma (entrada y salida).
- Decisiones tomadas.
- Bloqueos y dependencias pendientes, con la Skill responsable.
- Resultado de compilación, pruebas y comprobaciones.
