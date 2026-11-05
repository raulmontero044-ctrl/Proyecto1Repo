# Perfil de tipo: `unit`

Alcance de los tests unitarios en Facturación. Las normas de escritura (ubicación, nombres, AssertJ, dobles de prueba, determinismo, casos límite) son de `/k-back-testing` secciones 2, 3, 6 y 8 y no se repiten aquí.

## Qué abarca

| Pieza | Sección de `/k-back-testing` | Herramientas |
|---|---|---|
| Modelo de dominio e invariantes (`internal.domain.model`) | 3.1 | JUnit 5 + AssertJ, sin Spring ni Mockito |
| Servicios de dominio puros (`internal.domain.service`) | 3.1 | Igual; redondeos, escalas y fronteras |
| Mappers de persistencia (`internal.persistence.mapper`) | 3.2 | Ida y vuelta, sin Spring |
| Implementación de Repository con el DAO simulado (`internal.persistence.repository`) | 1.2, 3.2 | Mockito para `FacturaDao`, mapper real |
| Casos de uso (`internal.application`) | 3.3 | Fake en memoria del Repository (`FacturaRepositoryEnMemoria`), sin Spring |
| Implementación de la facade (`FacturaFacadeImpl`) | 3.3, 3.6 | Casos de uso reales y fake |
| DTO validados fuera de HTTP | 3.5 | `jakarta.validation.Validator` |

## Qué NO abarca

Tests con contexto Spring (`@WebMvcTest`, `@JsonTest`, `@DataJpaTest`, `@SpringBootTest`): son de `api` o `e2e`. Reglas ArchUnit y Spring Modulith: son de `arch`.

## Infraestructura

Ninguna adicional: todo se ejecuta con `./mvnw -B test` sin servicios externos. La puerta de decisión del procedimiento sección 3.3 solo se activa si una propuesta necesitara una dependencia nueva, lo que en este tipo DEBE considerarse una señal de que el test no es unitario.

## Mutaciones orientativas para el falsador

Invertir o eliminar la comprobación de una invariante; cambiar el modo o la escala de redondeo; sumar una línea de menos en un total; omitir un campo en el mapper o en el converter; no guardar o guardar dos veces en un caso de uso; lanzar otra excepción de dominio; devolver la lista sin copia defensiva.
