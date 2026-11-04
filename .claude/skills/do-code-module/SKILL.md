---
name: do-code-module
description: Crea un módulo de negocio nuevo en el backend Java/Spring Boot en la etapa actual del proyecto - raíz, package-info, API pública, API HTTP, casos de uso, dominio, infraestructura, persistencia con migración Flyway, mappers, integración y comprobación de dependencias - pidiendo los requisitos que falten sin inventar campos. Úsala cuando el usuario pida un módulo nuevo; funciona sola o desde /do-code-impl. Hace commits y push.
argument-hint: "<nombre del módulo> [requisitos o ruta a su especificación]"
disable-model-invocation: true
---

# /do-code-module

## Propósito

Crear un módulo de negocio nuevo, completo e integrado, con la estructura de la etapa actual del proyecto (en Etapa 3, la arquitectura definitiva). Puede ejecutarse directamente por el usuario o desde `/do-code-impl`. El procedimiento es el común; las normas son de las `k-back-*` y esta skill NO las repite.

## Cuándo usarla

- Los requisitos introducen un concepto de negocio nuevo que no cabe en un módulo existente (`/k-back-module` sección 1).
- Desde `/do-code-impl`, después de la última transformación de etapa del plan.

## Entradas

- `$ARGUMENTS`: nombre del módulo y requisitos, o ruta a su especificación.
- Requisitos imprescindibles: concepto de negocio y justificación como módulo; agregados con sus campos, tipos, obligatoriedad y límites; reglas e invariantes; operaciones y endpoints (verbo, ruta, códigos); relaciones con otros módulos y sentido de la dependencia; consumidores del contrato público.

Si falta cualquiera de ellos, DEBE preguntarse al usuario antes de crear código, listando exactamente lo que falta. NO DEBEN inventarse campos, columnas, reglas, endpoints ni dependencias.

## Resultado esperado

- Paquete `<raiz>.<modulo>` con la estructura de la etapa actual de la tabla canónica de `/k-back`, solo con los paquetes que tengan clases necesarias.
- Migración Flyway con la siguiente numeración global.
- Módulo integrado en la aplicación, sin ciclos, con `ArquitecturaTest` (y `ModulosTest` en Etapa 3) en verde.
- Rama feature publicada; integrada en `develop` en modo coordinador.

## Skills relacionadas

Procedimiento: `.claude/skills/do-code-impl/procedimiento-comun.md`.

Skills de conocimiento que DEBEN leerse (`.claude/skills/<nombre>/SKILL.md`) y pasarse a los subagentes:

| Skill | Para qué |
|---|---|
| `/k-back` | Etapa actual y su estructura, nombres, antiabstracción |
| `/k-back-module` | Justificación del módulo, contrato público, dependencias, `package-info.java`, integración |
| `/k-back-dto` | DTO de contrato y HTTP |
| `/k-back-controller` | API HTTP y manejo de excepciones |
| `/k-back-service` | Casos de uso, facade, transacciones |
| `/k-back-domain` | Modelo, invariantes, servicios y excepciones |
| `/k-back-repository` | Interfaces Repository |
| `/k-back-persistence` | Entidades, DAO, mappers, Repository, Flyway |
| `/k-back-testing` | V1-V7 y tests por capa |

## Procedimiento

### 0. Modo y Git

Determina el modo (procedimiento común, sección 1) y prepara la rama `feature/modulo-<modulo>` según la sección 4.1.

### 1. Validar requisitos y etapa

1.1. Comprueba que están todos los requisitos de «Entradas»; si no, pregunta y detente.

1.2. Valida con `/k-back-module` sección 1 que es un módulo nuevo y no una ampliación de uno existente; si cabe en uno existente, DEBE informarse y proponerse ampliar ese módulo.

1.3. Detecta la etapa actual inspeccionando los paquetes (`/k-back` sección 2). El módulo se crea en esa etapa; si los módulos existentes no están todos en la misma etapa, DEBE informarse antes de seguir. En Facturación: Etapa 2 hoy; Etapa 3 tras `/do-code-architecture`.

1.4. Comprueba el grafo de dependencias con el módulo nuevo; una dependencia mutua se informa como incompatibilidad (`/k-back-module` sección 7) y se espera decisión.

### 2. Responsabilidades, en este orden

Cada responsabilidad se resuelve con la norma de la skill indicada. Lo que no aplique en la etapa actual o según los requisitos se registra como «no aplica» con su motivo; NO se crean paquetes vacíos.

| N.º | Responsabilidad | Etapa 3 | Etapa 2 | Skill |
|---|---|---|---|---|
| 1 | Raíz del módulo | `<raiz>.<modulo>` | `<raiz>.<modulo>` | `/k-back`, `/k-back-module` |
| 2 | `package-info.java` | `@ApplicationModule` con `allowedDependencies` explícito (o alternativa sin Modulith si así está el proyecto) | No aplica | `/k-back-module` §9 |
| 3 | API pública | `api.facade.<Modulo>Facade` + `api.facade.dto` + `package-info` con `@NamedInterface`, solo si hay consumidor o lo exige la especificación | Interfaz en `api` solo con consumidor real | `/k-back-module` §5-§6, `/k-back-dto` |
| 4 | API HTTP | `api.http` + `api.http.dto` | `controller` + `api.dto` | `/k-back-controller`, `/k-back-dto` |
| 5 | Casos de uso | `internal.application` (+ `<Modulo>FacadeImpl`) | `apln` | `/k-back-service` |
| 6 | Dominio | `internal.domain.{model,service,exception,repository}` | `domain.{model,repository}` | `/k-back-domain`, `/k-back-repository` |
| 7 | Infraestructura | `internal.infrastructure`, solo si hay integración externa en los requisitos | `service` solo si se justifica | `/k-back`, `/k-back-service` |
| 8 | Persistencia | `internal.persistence.{entity,dao,repository}` + migración `V<n+1>__<descripcion>.sql` | `persistence.{entity,dao}` + impl en raíz + migración | `/k-back-persistence` |
| 9 | Mappers | `internal.persistence.mapper` (entidad ↔ dominio); conversión DTO ↔ dominio en aplicación | `persistence.mapper`; conversión en `apln` | `/k-back-persistence`, `/k-back-dto` |
| 10 | Integración | Escaneo por `@SpringBootApplication` sin escaneos extra; `allowedDependencies` de los módulos consumidores; reglas de `ArquitecturaTest` que lo cubran | Igual sin Modulith | `/k-back-module` §8, `/k-back-testing` |
| 11 | Comprobación de dependencias | `ArquitecturaTest` + `ModulosTest` (`verify()`), sin ciclos ni accesos a `internal` | `ArquitecturaTest` (`modulosSinCiclos`) | `/k-back-module`, `/k-back-testing` |

Esta tabla es un resumen para ordenar el trabajo: si discrepa de la tabla canónica de `/k-back` (sección 2) o de la skill propietaria, prevalecen estas.

La numeración de la migración se obtiene listando `src/main/resources/db/migration` (hoy existe solo `V1__crear_facturas.sql`). Las columnas y restricciones salen solo de los requisitos.

### 3. Planificar las 10 unidades (Fase A)

Registra 10 unidades coherentes según el procedimiento común (sección 2.1), siguiendo el orden de la sección 2. Agrupación orientativa: (1) raíz y `package-info`; (2) dominio y sus invariantes; (3) servicios y excepciones de dominio; (4) Repository; (5) migración Flyway y entidades; (6) DAO, mapper e implementación de Repository; (7) DTO y casos de uso; (8) API pública e implementación de la facade; (9) API HTTP y manejador de excepciones; (10) integración y comprobación de dependencias. Si un módulo sencillo no da para 10 unidades reales, DEBE informarse (sección 2.1.4); no se crean clases para rellenar.

### 4. Ejecutar la Fase A

Ejecuta las 10 iteraciones Programa → Verifica (V1-V7 de `/k-back-testing`) → Arregla según el procedimiento común (sección 2.2), con los tests de cada capa en la misma unidad que la crea.

### 5. Ejecutar la Fase B

Según el procedimiento común (sección 3). La persistencia del módulo nuevo hereda la limitación de base de datos de `/k-back-testing`: DEBE informarse si queda sin test de integración.

### 6. Publicar e integrar

Push de la rama. En modo coordinador, subagente integrador; en modo subagente, se entrega la rama sin integrar.

### 7. Informe

Informe final según el procedimiento común (sección 6), añadiendo:

- Requisitos recibidos y preguntas hechas al usuario con sus respuestas.
- Tabla de las 11 responsabilidades: clases creadas o «no aplica» con motivo.
- Dependencias del módulo con otros módulos y su declaración.
- Migración creada.

## Comprobaciones antes de finalizar

- [ ] Ningún campo, regla, endpoint ni dependencia se ha inventado; las dudas se preguntaron.
- [ ] El módulo está en la etapa actual y en `<raiz>.<modulo>`; no hay paquetes vacíos.
- [ ] Las 11 responsabilidades están resueltas en orden o marcadas como «no aplica» con motivo.
- [ ] La migración sigue la numeración global y no modifica migraciones existentes.
- [ ] No hay facade ni punto de entrada público sin consumidor o requisito explícito.
- [ ] Sin ciclos ni accesos internos entre módulos; `ArquitecturaTest` (y `ModulosTest` en Etapa 3) en verde.
- [ ] Las 10 iteraciones, la Fase B y la suite completa registradas con su resultado real.
- [ ] Commits con `Co-Authored-By`, push e integración hechos o entregados al coordinador.
