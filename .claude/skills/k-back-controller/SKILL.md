---
name: k-back-controller
description: Normas de la capa HTTP del backend Java/Spring Boot de Facturación - endpoints REST, verbos, códigos de respuesta, validación de entrada con @Valid, traducción de excepciones de dominio a ProblemDetail en el manejador del propio módulo, DTO web y delegación en casos de uso, con su ubicación por etapa. Úsala antes de diseñar, implementar o revisar controladores o manejadores de excepciones HTTP. No modifica código.
argument-hint: "[tema o duda concreta sobre la capa HTTP]"
---

# /k-back-controller

## Propósito

Fijar las normas del adaptador HTTP: endpoints, verbos, códigos de estado, validación de entrada, manejo de excepciones y delegación en la capa de aplicación. Es una skill de conocimiento: NO modifica código, NO ejecuta Git y NO define procedimientos de implementación. Las normas generales y la tabla de paquetes son de `/k-back`.

Los términos DEBE, NO DEBE y PUEDE son normativos.

## Cuándo usarla

- Antes de crear, modificar o revisar un controlador REST o su manejador de excepciones.
- Para decidir verbo, ruta, código de estado o traducción de un error.

## Entradas

Ninguna obligatoria. Si recibe `$ARGUMENTS`, se interpreta como el tema o la duda concreta a la que aplicar estas normas.

## Resultado esperado

Normas, criterios, ejemplos breves y checklist de la capa HTTP. Ningún archivo se crea ni se modifica.

## Skills relacionadas

- `/k-back`: normas generales, tabla canónica de paquetes, dependencias entre capas y nombres.
- `/k-back-service`: casos de uso en los que delega el controlador.
- `/k-back-dto`: forma de los DTO HTTP, Bean Validation y separación HTTP / contrato.
- `/k-back-domain`: excepciones de dominio que se traducen a HTTP.
- `/k-back-module`: límites entre módulos.
- `/k-back-testing`: pruebas `@WebMvcTest` de la capa HTTP.

## Normas

### 1. Ubicación

1.1. Según la tabla de `/k-back`: el controlador y su manejador de excepciones DEBEN estar en `<raiz>.controller` (Etapa 1) / `<raiz>.<modulo>.controller` (Etapa 2, hoy `com.facturacion.factura.controller.FacturaController` y `FacturaExceptionHandler`); en la Etapa 3, el controlador en `<raiz>.<modulo>.api.http.controllers` y los handlers de excepciones en `<raiz>.<modulo>.api.http.handlers` (hoy `com.facturacion.factura.api.http.controllers.FacturaController` y, en `handlers`, `FacturaNoEncontradaExceptionHandler`, `NumeroFacturaDuplicadoExceptionHandler` y `FacturaInvalidaExceptionHandler`), con sus DTO en `<raiz>.<modulo>.api.http.dto`. Como los handlers referencian al controlador desde otro paquete en `assignableTypes`, el controlador DEBE ser `public`.

1.2. El nombre DEBE ser `<Agregado>Controller` y, para el manejador, `<Agregado>ExceptionHandler` en las Etapas 1 y 2; en la Etapa 3, `<Excepcion>Handler` (p. ej. `FacturaNoEncontradaExceptionHandler`).

### 2. Responsabilidad y dependencias

2.1. El controlador DEBE limitarse a: recibir la petición, validar la entrada, convertir el DTO HTTP en el DTO del caso de uso, delegar en un caso de uso del propio módulo y construir la respuesta con su código de estado.

2.2. El controlador NO DEBE contener reglas de negocio, cálculos, transacciones ni acceso a Repository, DAO, entidades o servicios internos.

2.3. Cada endpoint DEBE delegar en un único caso de uso del propio módulo (`crearFactura.ejecutar(...)`, `obtenerFactura.ejecutar(id)`), inyectado por constructor. En la Etapa 3 el controlador NO DEBE delegar en la facade de su propio módulo: delega en los casos de uso de `internal.application`.

2.4. La conversión HTTP → DTO de aplicación PUEDE hacerse en un método privado estático del controlador, como `aNuevaFactura(CrearFacturaRequest)`, sin lógica de negocio. Las reglas del mapeo son de `/k-back-dto`.

2.5. NO DEBEN invocarse controladores, casos de uso ni servicios de otros módulos.

### 3. Endpoints, verbos y códigos

3.1. Los endpoints existentes DEBEN preservarse con su contrato:

| Verbo y ruta | Caso de uso | Respuesta correcta |
|---|---|---|
| `POST /api/facturas` | `CrearFactura` | `201 Created`, cabecera `Location` con `/api/facturas/{id}` y cuerpo `FacturaDto` |
| `GET /api/facturas/{id}` | `ObtenerFactura` | `200 OK` con `FacturaDto` |
| `GET /api/facturas` | `ListarFacturas` | `200 OK` con `List<FacturaDto>` |
| `DELETE /api/facturas/{id}` | `EliminarFactura` | `204 No Content` sin cuerpo |

3.2. Las rutas DEBEN agruparse con `@RequestMapping("/api/<recurso en plural>")` y usar sustantivos; el identificador va en la ruta (`@PathVariable Long id`).

3.3. Los verbos DEBEN respetar su semántica: `GET` sin efectos, `POST` para crear, `PUT`/`PATCH` para modificar y `DELETE` para eliminar. NO DEBEN añadirse endpoints, verbos, parámetros ni cabeceras sin requisito.

3.4. Una creación DEBE devolver `201` con `Location` construida con `ServletUriComponentsBuilder.fromCurrentRequest()`; una eliminación, `204` con `ResponseEntity.noContent()`. Cuando la respuesta es `200` con cuerpo PUEDE devolverse el DTO directamente.

### 4. Validación de entrada

4.1. Los cuerpos de petición DEBEN ser DTO `record` HTTP anotados con Bean Validation (`/k-back-dto`) y DEBEN recibirse con `@Valid @RequestBody`, como `crear(@Valid @RequestBody CrearFacturaRequest request)`.

4.2. Una entrada inválida DEBE responder `400` sin invocar el caso de uso. Hoy ese `400` lo produce el tratamiento por defecto de Spring (`MethodArgumentNotValidException`, cuerpo mal formado, `@PathVariable` no numérico); NO DEBE reescribirse sin requisito.

4.3. La validación de entrada NO sustituye a las invariantes del dominio (`/k-back`).

### 5. Gestión de excepciones

5.1. La traducción de excepciones de dominio a HTTP DEBE hacerse en el manejador del propio módulo, anotado con `@RestControllerAdvice(assignableTypes = <Agregado>Controller.class)` para no afectar a otros módulos. En la Etapa 3 DEBE haber un handler separado por excepción de dominio: cada clase de `api.http.handlers` declara un único método `@ExceptionHandler` con una sola excepción (regla `cadaHandlerTraduceUnaSolaExcepcion` de `ArquitecturaTest`).

5.2. Cada handler DEBE devolver `ProblemDetail` con `ProblemDetail.forStatusAndDetail(status, excepcion.getMessage())`. Traducciones existentes:

| Excepción de dominio | Estado |
|---|---|
| `FacturaNoEncontradaException` | `404 Not Found` |
| `NumeroFacturaDuplicadoException` | `409 Conflict` |
| `FacturaInvalidaException` | `400 Bad Request` |

5.3. El controlador y su manejador SOLO PUEDEN importar excepciones de dominio del propio módulo, y solo para traducirlas. NO DEBEN importar excepciones de otros módulos.

5.4. El paquete de configuración transversal (`<raiz>.config` o `<raiz>.app`) NO DEBE tratar excepciones de ningún módulo; SOLO PUEDE tratar excepciones genéricas de framework o Java, y solo con requisito.

5.5. Las respuestas de error NO DEBEN exponer trazas, SQL ni nombres de clases internas; el `detail` es el mensaje de la excepción de dominio.

5.6. Los controladores NO DEBEN capturar excepciones con `try/catch` para construir respuestas; la traducción es del manejador.

### 6. Tipos expuestos

6.1. Las respuestas HTTP DEBEN ser DTO `record`; NO DEBEN serializarse modelos de dominio ni entidades JPA.

6.2. En las Etapas 1 y 2 el controlador PUEDE usar como entrada y salida los DTO de `api/dto` del propio módulo (convención existente: `CrearFacturaRequest` de entrada, `FacturaDto` de salida).

6.3. En la Etapa 3 la petición y la respuesta DEBEN ser DTO de `api.http.dto`; el controlador convierte entre ellos y los DTO que reciben y devuelven los casos de uso (`/k-back-dto`).

## Comprobaciones antes de finalizar

- [ ] El controlador y el manejador están en el paquete de la etapa vigente y siguen los nombres `<Agregado>Controller` / `<Agregado>ExceptionHandler` (en la Etapa 3, un `<Excepcion>Handler` por excepción).
- [ ] Cada endpoint delega en un único caso de uso del propio módulo y no contiene lógica de negocio ni transacciones.
- [ ] No se importan Repository, `persistence`, `service` ni clases de otros módulos.
- [ ] Verbos, rutas y códigos (`201` + `Location`, `200`, `204`, `400`, `404`, `409`) se mantienen y no se han inventado endpoints.
- [ ] Los cuerpos se validan con `@Valid @RequestBody` sobre DTO `record` anotados.
- [ ] Las excepciones de dominio del propio módulo se traducen a `ProblemDetail` en su manejador con `assignableTypes`; ninguna se trata en la configuración global.
- [ ] Ninguna respuesta expone modelos de dominio, entidades ni detalles internos.
