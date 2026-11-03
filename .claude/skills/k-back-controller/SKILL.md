---
name: k-back-controller
description: Implementa la capa HTTP de un módulo backend Java/Spring Boot - controladores REST, endpoints, DTO record de petición y respuesta, validación de entrada y códigos de estado - delegando en casos de uso existentes de apln. Úsala cuando haya que exponer o modificar endpoints de un módulo. No implementa reglas de negocio, casos de uso ni persistencia.
argument-hint: "<módulo> <endpoints a implementar>"
---

# k-back-controller

Implementa el adaptador HTTP de un módulo backend.

Entrada recibida: $ARGUMENTS

## Reglas comunes

Aplica `.claude/rules/architecture.md`. Si no está en contexto, léelo antes de empezar. Esta Skill solo detalla lo específico de la capa HTTP; ante cualquier duda prevalece ese archivo.

## Entradas

- Módulo afectado.
- Endpoints a implementar: método, ruta, parámetros, cuerpo, respuestas y códigos de estado, según el plan o los requisitos.
- Opcionalmente, referencia a un plan o tarea concreta (`fichero`, `funcion`, `descripcion`).

Si la entrada no define un endpoint con suficiente detalle, pide aclaración en lugar de suponerlo.

## Alcance permitido

Dentro del módulo indicado:

- `controller/`: controladores REST y su manejo de errores HTTP específico del módulo.
- `api/dto/`, o junto a `controller` si esa es la convención existente (architecture.md §6.4): `record` de petición y respuesta HTTP cuando no coincidan con los `record` de entrada y salida de los casos de uso ya existentes.
- Pruebas de la capa web, siguiendo las convenciones de pruebas existentes.

## Contrato con la capa de aplicación

- El controlador solo invoca casos de uso de `apln` del propio módulo.
- Los casos de uso reciben y devuelven `record` de `api/dto` o tipos simples. Si coinciden con el contrato HTTP, se reutilizan; si no, el controlador define `record` HTTP propios y los convierte.
- La conversión entre DTO HTTP y DTO de casos de uso se hace en `controller`, sin lógica de negocio.
- El controlador puede importar únicamente las excepciones de dominio de su propio módulo, solo para traducirlas a códigos de estado HTTP.
- Esa traducción se hace en el controlador o en un manejador de excepciones situado en `controller` del módulo, nunca en el paquete `config` global.

## Fuera de alcance

- Reglas de negocio e invariantes: corresponden a `/k-back-service`.
- Casos de uso, servicios y modelos de dominio: corresponden a `/k-back-service`.
- Interfaces Repository, DAO, entidades y mappers: corresponden a `/k-back-repository`.
- Acceso directo a Repository o DAO.
- Exponer modelos de dominio o entidades JPA en peticiones o respuestas.
- Invocar servicios internos o casos de uso de otros módulos.
- Inventar endpoints, rutas, parámetros, cabeceras o formatos de respuesta.

## Procedimiento

1. Lee `.claude/rules/architecture.md` si no está en contexto.
2. Inspecciona el repositorio real: herramienta de build, paquete raíz, versión de Spring Boot, dependencias de validación disponibles, convenciones de rutas, versionado de API, manejo de errores y pruebas web.
3. Localiza el módulo indicado y revisa los controladores y `record` de `api/dto` existentes para reutilizarlos y no duplicarlos.
4. Comprueba que existen los casos de uso de `apln` que necesita cada endpoint, con su firma.
5. Si falta un caso de uso o su firma no permite atender el endpoint, no lo implementes ni lo modifiques: comunica el bloqueo.
6. Define los `record` de petición y respuesta necesarios, con las anotaciones de validación disponibles en el proyecto.
7. Implementa los endpoints: delegación en el caso de uso, conversión de DTO y códigos de estado.
8. Añade o actualiza pruebas de la capa web si el proyecto tiene infraestructura de pruebas.
9. Ejecuta la verificación.

No escribas comentarios de ningún tipo en el código generado (ni de línea, ni de bloque, ni JavaDoc).

## Bloqueos que se deben comunicar

- El módulo no existe o no se puede identificar el paquete raíz.
- Falta el caso de uso necesario o su firma no lo permite: indica qué entrada y salida requiere y que corresponde a `/k-back-service`.
- El plan no define ruta, método, parámetros o respuestas del endpoint.
- No hay dependencia de validación disponible y el plan exige validación declarativa.

Un bloqueo no autoriza a implementar el trabajo de otra Skill. Implementa los endpoints posibles sin inventar e informa del resto.

## Verificación

- Compila con la herramienta de build detectada y ejecuta las pruebas existentes, incluidas las de ArchUnit si las hay. Si no hay build o pruebas, indícalo.
- Comprueba que `controller` no importa nada de `domain/repository`, `persistence` (DAO, entidades, mappers) ni `service`.
- Comprueba que de `domain` solo se importan excepciones de dominio del propio módulo, y únicamente para traducirlas a HTTP.
- Comprueba que ninguna petición o respuesta usa tipos de `domain/model` o de `persistence`.
- Comprueba que no se ha añadido manejo de excepciones del módulo en el paquete `config` global.
- Comprueba que no se importan clases de otros módulos salvo sus `record` públicos de `api/dto`.
- Comprueba que todos los DTO creados son `record`.
- Comprueba que no hay comentarios en el código generado.

## Informe final

- Archivos creados y modificados.
- Endpoints implementados: método, ruta, caso de uso invocado y códigos de estado.
- Decisiones tomadas.
- Bloqueos y dependencias pendientes, con la Skill responsable.
- Resultado de compilación, pruebas y comprobaciones.
