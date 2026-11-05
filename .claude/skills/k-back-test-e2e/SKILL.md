---
name: k-back-test-e2e
description: Normas de los tests extremo a extremo del backend Java/Spring Boot de Facturación - aplicación completa arrancada con @SpringBootTest(RANDOM_PORT), peticiones HTTP reales, MariaDB real con migraciones Flyway, flujos de negocio críticos, aislamiento de datos, infraestructura que siempre requiere autorización (Testcontainers y Docker) y mutaciones que los tests deben detectar. Úsala al escribir, reparar, falsar o revisar tests e2e; /do-test y /do-test-e2e-* la aplican. No modifica código.
argument-hint: "[flujo o duda concreta sobre tests e2e, opcional]"
---

# /k-back-test-e2e

## Propósito

Fijar cómo se hace un test extremo a extremo en Facturación: la aplicación completa arrancada, peticiones HTTP reales y base de datos real con las migraciones Flyway. Es una skill de conocimiento: NO modifica código, NO ejecuta Git y NO define procedimientos. El ciclo de testing es de `.claude/skills/do-test/procedimiento-testing.md`.

Las normas de escritura son de `/k-back-testing` secciones 2, 3.7, 6 y 8. NO se repiten aquí. Los términos DEBE, NO DEBE y PUEDE son normativos.

## Cuándo usarla

- Al crear, reparar o revisar tests e2e.
- Al falsar esos tests, para elegir las mutaciones.
- Antes de proponer la infraestructura que estos tests necesitan.

## Entradas

Ninguna obligatoria. Si recibe `$ARGUMENTS`, se interpreta como el flujo o la duda concreta a la que aplicar estas normas.

## Resultado esperado

Normas, criterios y checklist del tipo `e2e`. Ningún archivo se crea ni se modifica.

## Skills relacionadas

- `/k-back-testing`: normas comunes, integración (sección 3.7) y estado de la infraestructura (1.1, 1.3).
- `/k-back-controller`, `/k-back-persistence`, `/k-back-module`: contratos HTTP, migraciones y cableado que se ejercitan.
- `/k-back-test-unit`, `/k-back-test-arch`, `/k-back-test-api`: los demás tipos.

## Normas

### 1. Qué abarca

- Flujos de negocio críticos por HTTP real, de principio a fin: crear una factura y recuperarla, listarla, eliminarla y comprobar que deja de existir; rechazo de un número duplicado contra la restricción real de la base de datos; errores con su `ProblemDetail` atravesando toda la pila.
- Cableado completo de Spring (módulos, facade, handlers), transacciones reales y migraciones Flyway aplicadas sobre MariaDB con `ddl-auto: validate`.

Herramientas: `@SpringBootTest(webEnvironment = RANDOM_PORT)` con un cliente HTTP real del stack existente (`TestRestTemplate` o `RestClient`) y MariaDB en contenedor.

### 2. Criterios propios del tipo

2.1. Pocos tests y centrados en flujos (`/k-back-testing` sección 3.7): NO DEBEN replicar casos ya cubiertos por `unit` o `api`; antes de escribirlos DEBEN listarse esos casos para evitarlo.

2.2. Cada test DEBE dejar la base de datos en un estado que no afecte a los demás (datos propios con números únicos o limpieza explícita), sin depender del orden de ejecución.

2.3. Las aserciones DEBEN comprobar el efecto observable por HTTP (códigos, cuerpo, `Location`) y, cuando el flujo lo exija, el estado persistido leído de nuevo por la API.

### 3. Infraestructura

3.1. Este tipo SIEMPRE necesita infraestructura que el proyecto no tiene (`/k-back-testing` secciones 1.1 y 1.3): Testcontainers MariaDB y Docker. Antes de crear ningún test DEBE comprobarse si Docker está disponible (`docker info`) y obtenerse autorización del usuario entre:

1. Autorizar `spring-boot-testcontainers` y el módulo MariaDB de Testcontainers con `@ServiceConnection`, aceptando que la suite pase a requerir Docker.
2. Usar la MariaDB de `compose.yaml` levantada por el usuario, lo que deja los tests fuera de la ejecución normal y exige decidir cómo se ejecutan.
3. Cancelar.

3.2. Sin una de las dos primeras autorizaciones, NO DEBE crearse ningún test e2e. H2, `assumeTrue` y desactivaciones NO son alternativas.

3.3. Los tests DEBEN llamarse `*Test` mientras no exista Failsafe (`/k-back-testing` sección 2.2); añadir Failsafe es otra decisión que también requiere autorización.

### 4. Mutaciones que los tests DEBEN detectar

Quitar `@Transactional` de un caso de uso que guarda varias cosas; eliminar la restricción `UNIQUE` de la migración en un worktree desechable; cambiar el mapeo de una columna; romper el cableado de un handler; devolver `200` en lugar de `201`.

## Comprobaciones antes de finalizar

- [ ] Existe autorización de infraestructura (sección 3.1) antes de cualquier test e2e.
- [ ] Los tests cubren flujos y no duplican casos de `unit` ni `api`.
- [ ] Cada test es independiente del orden y de los datos de los demás.
- [ ] Las mutaciones de la sección 4 aplicables hacen fallar algún test.
