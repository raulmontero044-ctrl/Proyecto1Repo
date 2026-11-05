---
name: k-back-test-api
description: Normas de los tests de API e integración del backend Java/Spring Boot de Facturación - controladores y handlers con @WebMvcTest y MockMvc, validación de DTO HTTP, serialización JSON con @JsonTest, persistencia real con @DataJpaTest contra MariaDB e integración con @SpringBootTest, infraestructura que requiere autorización (Testcontainers), qué queda fuera y mutaciones que los tests deben detectar. Úsala al escribir, reparar, falsar o revisar tests de API o integración; /do-test y /do-test-api-* la aplican. No modifica código.
argument-hint: "[pieza o duda concreta sobre tests de API e integración, opcional]"
---

# /k-back-test-api

## Propósito

Fijar cómo se hace un test de API o de integración en Facturación: qué abarca el tipo `api`, con qué herramientas, qué infraestructura necesita cada pieza y qué errores reales debe detectar. Es una skill de conocimiento: NO modifica código, NO ejecuta Git y NO define procedimientos. El ciclo de testing es de `.claude/skills/do-test/procedimiento-testing.md`.

Las normas de escritura son de `/k-back-testing` secciones 2, 3.2, 3.4, 3.5, 3.7, 6 y 8, y los contratos HTTP, de `/k-back-controller` y `/k-back-dto`. NO se repiten aquí. Los términos DEBE, NO DEBE y PUEDE son normativos.

## Cuándo usarla

- Al crear, reparar o revisar tests de controladores, handlers, validación HTTP, JSON, persistencia real o integración.
- Al falsar esos tests, para elegir las mutaciones.
- Antes de proponer infraestructura de test (base de datos, Testcontainers).

## Entradas

Ninguna obligatoria. Si recibe `$ARGUMENTS`, se interpreta como la pieza o la duda concreta a la que aplicar estas normas.

## Resultado esperado

Normas, criterios y checklist del tipo `api`. Ningún archivo se crea ni se modifica.

## Skills relacionadas

- `/k-back-testing`: normas comunes de escritura, estado de la infraestructura (secciones 1.3 y 8.6).
- `/k-back-controller`, `/k-back-dto`, `/k-back-persistence`: contratos que se prueban.
- `/k-back-test-unit`, `/k-back-test-arch`, `/k-back-test-e2e`: los demás tipos.

## Normas

### 1. Qué abarca

| Pieza | Sección de `/k-back-testing` | Herramientas | Necesita infraestructura |
|---|---|---|---|
| Controladores (`api.http.controllers`): verbos, rutas, códigos, `Location`, traslado exacto al caso de uso | 3.4 | `@WebMvcTest` + `MockMvc` + `@MockitoBean` | No |
| Handlers (`api.http.handlers`): cada excepción de dominio → código y `ProblemDetail` | 3.4 | Igual | No |
| Validación de DTO HTTP: valor que viola, límite exacto aceptado, ausentes, cuerpo mal formado, tipos incorrectos en la ruta | 3.5 | Igual, con `verify(..., never())` | No |
| Serialización JSON de las respuestas | 1.2 | `@JsonTest`, `@WebMvcTest` | No |
| Persistencia real (`FacturaDao`, `JpaFacturaRepository`, esquema Flyway frente a entidades, cascadas, unicidad) | 3.2 | `@DataJpaTest` con `Replace.NONE` contra MariaDB | Sí |
| Integración con contexto completo (cableado, transacciones reales, `FacturaFacadeImpl` real) | 3.7 | `@SpringBootTest` | Sí |

### 2. Qué NO abarca

Tests sin contexto Spring (`/k-back-test-unit`), reglas de arquitectura (`/k-back-test-arch`) y flujos de negocio completos por HTTP real contra la aplicación arrancada (`/k-back-test-e2e`).

### 3. Infraestructura

3.1. Las filas que necesitan infraestructura no pueden ejecutarse hoy en `./mvnw -B test` (`/k-back-testing` secciones 1.3, 3.7 y 8.6): falta MariaDB y no hay Testcontainers. Antes de crear tests de esas filas DEBE comprobarse si hay Docker disponible y obtenerse autorización del usuario entre, al menos:

1. Limitar el alcance a la capa HTTP (`@WebMvcTest`, `@JsonTest`), sin dependencias nuevas.
2. Autorizar Testcontainers MariaDB (`spring-boot-testcontainers`, `@ServiceConnection`) para persistencia e integración, con Docker como requisito de la suite.

3.2. H2 NO DEBE proponerse como sustituto (`/k-back-testing` sección 3.2). Tampoco PUEDE ocultarse la falta de infraestructura con `assumeTrue` o desactivaciones (sección 8.2).

### 4. Mutaciones que los tests DEBEN detectar

Cambiar el código de estado de un endpoint o de un handler; quitar `@Valid` o una restricción Bean Validation; cambiar un límite (`@Size(max = 50)` → `51`); omitir el `Location`; trasladar un campo incorrecto del request al DTO del caso de uso; cambiar el `detail` del `ProblemDetail`; renombrar una propiedad JSON; quitar el `cascade` o la restricción de unicidad.

## Comprobaciones antes de finalizar

- [ ] Cada pieza usa la herramienta de la sección 1; no hay un test aparte por handler.
- [ ] Los rechazos por validación comprueban `verify(..., never())`.
- [ ] Ningún test con infraestructura se ha creado sin la autorización de la sección 3.1; no se ha usado H2 ni `assumeTrue`.
- [ ] Las mutaciones de la sección 4 aplicables hacen fallar algún test.
