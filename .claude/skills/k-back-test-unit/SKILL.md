---
name: k-back-test-unit
description: Normas de los tests unitarios del backend Java/Spring Boot de Facturación - qué piezas abarcan (modelo e invariantes, servicios de dominio puros, mappers, implementación de Repository con DAO simulado, casos de uso y facade con fake de Repository, DTO validados fuera de HTTP), herramientas por pieza, qué queda fuera, infraestructura y mutaciones que los tests deben detectar. Úsala al escribir, reparar, falsar o revisar tests unitarios; /do-test y /do-test-unit-* la aplican. No modifica código.
argument-hint: "[pieza o duda concreta sobre tests unitarios, opcional]"
---

# /k-back-test-unit

## Propósito

Fijar cómo se hace un test unitario en Facturación: qué piezas abarca el tipo `unit`, con qué herramientas se prueba cada una, qué queda fuera y qué errores reales debe detectar. Es una skill de conocimiento: NO modifica código, NO ejecuta Git y NO define procedimientos. El ciclo de testing (agentes, ramas, CREATE, REPAIR) es de `.claude/skills/do-test/procedimiento-testing.md`.

Las normas de escritura comunes a todos los tipos (ubicación, nombres, AssertJ, dobles de prueba, determinismo, casos límite, tests válidos) son de `/k-back-testing` secciones 2, 3, 6 y 8 y NO se repiten aquí. Los términos DEBE, NO DEBE y PUEDE son normativos.

## Cuándo usarla

- Al crear, reparar o revisar tests unitarios.
- Al falsar una suite unitaria, para elegir las mutaciones.
- Para decidir si un test es unitario o pertenece a `/k-back-test-api`, `/k-back-test-arch` o `/k-back-test-e2e`.

## Entradas

Ninguna obligatoria. Si recibe `$ARGUMENTS`, se interpreta como la pieza o la duda concreta a la que aplicar estas normas.

## Resultado esperado

Normas, criterios y checklist del tipo `unit`. Ningún archivo se crea ni se modifica.

## Skills relacionadas

- `/k-back-testing`: normas comunes de escritura y criterios de tests válidos.
- `/k-back-domain`, `/k-back-persistence`, `/k-back-service`, `/k-back-dto`: reglas de las piezas que se prueban.
- `/k-back-test-api`, `/k-back-test-arch`, `/k-back-test-e2e`: los demás tipos.

## Normas

### 1. Qué abarca

| Pieza | Sección de `/k-back-testing` | Herramientas |
|---|---|---|
| Modelo de dominio e invariantes (`internal.domain.model`) | 3.1 | JUnit 5 + AssertJ, sin Spring ni Mockito |
| Servicios de dominio puros (`internal.domain.service`) | 3.1 | Igual; redondeos, escalas y fronteras |
| Mappers de persistencia (`internal.persistence.mapper`) | 3.2 | Ida y vuelta, sin Spring |
| Implementación de Repository con el DAO simulado (`internal.persistence.repository`) | 1.2, 3.2 | Mockito para `FacturaDao`, mapper real |
| Casos de uso (`internal.application`) | 3.3 | Fake en memoria del Repository (`FacturaRepositoryEnMemoria`), sin Spring |
| Implementación de la facade (`FacturaFacadeImpl`) | 3.3, 3.6 | Casos de uso reales y fake |
| DTO validados fuera de HTTP | 3.5 | `jakarta.validation.Validator` |

### 2. Qué NO abarca

Tests con contexto Spring (`@WebMvcTest`, `@JsonTest`, `@DataJpaTest`, `@SpringBootTest`): son de `/k-back-test-api` o `/k-back-test-e2e`. Reglas ArchUnit y Spring Modulith: son de `/k-back-test-arch`.

### 3. Infraestructura

Ninguna adicional: todo se ejecuta con `./mvnw -B test` sin servicios externos. Si un test unitario necesitara una dependencia nueva, DEBE considerarse una señal de que el test no es unitario; añadirla requiere autorización del usuario (`/k-back-testing` sección 7.4).

### 4. Mutaciones que los tests DEBEN detectar

Invertir o eliminar la comprobación de una invariante; cambiar el modo o la escala de redondeo; sumar una línea de menos en un total; omitir un campo en el mapper o en el converter; no guardar o guardar dos veces en un caso de uso; lanzar otra excepción de dominio; devolver la lista sin copia defensiva.

## Comprobaciones antes de finalizar

- [ ] Ningún test del tipo arranca contexto Spring.
- [ ] Cada pieza usa la herramienta de la sección 1 (dominio puro, DAO simulado solo en la implementación Repository, fake de Repository en casos de uso y facade).
- [ ] No se han añadido dependencias.
- [ ] Las mutaciones de la sección 4 aplicables a las piezas probadas hacen fallar algún test.
