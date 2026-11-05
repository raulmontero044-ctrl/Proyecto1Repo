# Perfil de tipo: `api`

Alcance de los tests de API e integración en Facturación. Las normas de escritura son de `/k-back-testing` secciones 2, 3.2, 3.4, 3.5, 3.7, 6 y 8, y los contratos HTTP, de `/k-back-controller` y `/k-back-dto`. No se repiten aquí.

## Qué abarca

| Pieza | Sección de `/k-back-testing` | Herramientas | Necesita infraestructura |
|---|---|---|---|
| Controladores (`api.http.controllers`): verbos, rutas, códigos, `Location`, traslado exacto al caso de uso | 3.4 | `@WebMvcTest` + `MockMvc` + `@MockitoBean` | No |
| Handlers (`api.http.handlers`): cada excepción de dominio → código y `ProblemDetail` | 3.4 | Igual | No |
| Validación de DTO HTTP: valor que viola, límite exacto aceptado, ausentes, cuerpo mal formado, tipos incorrectos en la ruta | 3.5 | Igual, con `verify(..., never())` | No |
| Serialización JSON de las respuestas | 1.2 | `@JsonTest`, `@WebMvcTest` | No |
| Persistencia real (`FacturaDao`, `JpaFacturaRepository`, esquema Flyway frente a entidades, cascadas, unicidad) | 3.2 | `@DataJpaTest` con `Replace.NONE` contra MariaDB | Sí |
| Integración con contexto completo (cableado, transacciones reales, `FacturaFacadeImpl` real) | 3.7 | `@SpringBootTest` | Sí |

## Infraestructura y puerta de decisión

Las filas que necesitan infraestructura no pueden ejecutarse hoy en `./mvnw -B test` (`/k-back-testing` secciones 1.3, 3.7 y 8.6): falta MariaDB y no hay Testcontainers. El `test-analyzer` DEBE comprobar si hay Docker disponible y el coordinador DEBE preguntar al usuario (procedimiento sección 3.3) entre, al menos:

1. Limitar el alcance a la capa HTTP (`@WebMvcTest`, `@JsonTest`), sin dependencias nuevas.
2. Autorizar Testcontainers MariaDB (`spring-boot-testcontainers`, `@ServiceConnection`) para persistencia e integración, con Docker como requisito de la suite.

H2 NO DEBE proponerse como sustituto (`/k-back-testing` sección 3.2). Tampoco PUEDE ocultarse la falta de infraestructura con `assumeTrue` o desactivaciones (sección 8.2).

## Qué NO abarca

Tests sin contexto Spring (`unit`), reglas de arquitectura (`arch`) y flujos de negocio completos por HTTP real contra la aplicación arrancada (`e2e`).

## Mutaciones orientativas para el falsador

Cambiar el código de estado de un endpoint o de un handler; quitar `@Valid` o una restricción Bean Validation; cambiar un límite (`@Size(max = 50)` → `51`); omitir el `Location`; trasladar un campo incorrecto del request al DTO del caso de uso; cambiar el `detail` del `ProblemDetail`; renombrar una propiedad JSON; quitar el `cascade` o la restricción de unicidad.
