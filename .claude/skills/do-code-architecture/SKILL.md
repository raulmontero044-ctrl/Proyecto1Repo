---
name: do-code-architecture
description: Transforma el backend Java/Spring Boot de la Etapa 2 (monolito modular) a la Etapa 3 (arquitectura modular definitiva con api/internal, facade, app y package-info con Spring Modulith) según la tabla canónica de /k-back, preservando endpoints, JSON, reglas, migraciones Flyway y todos los tests; si ya está en Etapa 3, audita y corrige. Úsala cuando el usuario pida la arquitectura modular definitiva. Hace commits y push.
argument-hint: "[módulo o alcance, opcional; por defecto todos los módulos]"
disable-model-invocation: true
---

# /do-code-architecture

## Propósito

Pasar todos los módulos de la Etapa 2 a la Etapa 3 de la tabla canónica de `/k-back` (sección 2), con contrato público `api.facade`, adaptador HTTP `api.http`, implementación en `internal`, raíz de composición `app`, `package-info.java` por módulo y verificación de límites con ArchUnit y Spring Modulith. El procedimiento es el común; las normas son de las `k-back-*` y esta skill NO las repite: aporta el mapa concreto de Facturación y el orden de trabajo.

## Cuándo usarla

- El proyecto está en Etapa 2 y el usuario pide la arquitectura modular definitiva.
- Como tercera transformación de un plan en `/do-code-impl`, desde el `develop` resultante de `/do-code-modular`.
- El proyecto ya está en Etapa 3 y se quiere auditar que cumple la tabla canónica.

## Entradas

- `$ARGUMENTS` (opcional): módulo o alcance. Todos los módulos DEBEN quedar en la misma etapa (`/k-back-module` sección 2.4), así que un alcance parcial solo se admite si el resto ya está en Etapa 3; si no, DEBE informarse y tratarse todos.
- Especificación de la arquitectura objetivo del usuario: fija que la Etapa 3 incluye `FacturaFacade`. Si enumera las operaciones de la facade, se usan esas; si no, DEBE preguntarse al usuario (sección 3).

## Resultado esperado

- Clases reubicadas según el mapa de la sección 2, con los mismos endpoints, códigos HTTP y JSON.
- `FacturaFacade` + `FacturaFacadeImpl`, `FacturaResponse` + `LineaFacturaResponse`, `package-info.java` y paquete `app` creados con su justificación informada.
- Spring Modulith 1.4.x añadido e informado, ArchUnit alineado, `ArquitecturaTest` adaptado y ampliado, `ModulosTest` nuevo.
- Todos los tests existentes movidos y en verde; el número de tests no baja.
- Rama feature publicada; integrada en `develop` en modo coordinador.

## Skills relacionadas

Procedimiento: `.claude/skills/do-code-impl/procedimiento-comun.md`.

Skills de conocimiento que DEBEN leerse (`.claude/skills/<nombre>/SKILL.md`) y pasarse a los subagentes:

| Skill | Para qué |
|---|---|
| `/k-back` | Tabla canónica de la Etapa 3, reglas de dependencia de la Etapa 3, evolución |
| `/k-back-module` | Facade, `app`, `package-info.java`, Spring Modulith, `allowedDependencies`, `@NamedInterface` |
| `/k-back-dto` | DTO de `api.facade.dto` frente a `api.http.dto`, `<X>Response` |
| `/k-back-service` | Casos de uso e implementación de la facade en `internal.application`, transacciones |
| `/k-back-controller` | `api.http`: controlador y manejador de excepciones |
| `/k-back-domain` | `internal.domain.{model,service,exception}` |
| `/k-back-repository` | `internal.domain.repository` |
| `/k-back-persistence` | `internal.persistence.{entity,dao,mapper,repository}`, Flyway |
| `/k-back-testing` | V1-V7, reglas ArchUnit de Etapa 3, `ModulosTest`, preservación de tests |

Anterior: `/do-code-modular`. Para módulos nuevos tras la transformación: `/do-code-module`.

## Procedimiento

### 0. Modo y Git

Determina el modo (procedimiento común, sección 1) y prepara la rama (p. ej. `feature/arquitectura-modular-definitiva`) según la sección 4.1.

### 1. Detectar la etapa

- Etapa 1: DEBE informarse y proponerse ejecutar antes `/do-code-modular`.
- Etapa 2 → **modo transformación**. Facturación está en Etapa 2.
- Etapa 3 → **modo auditoría**: se revisa cada fila de la sección 2 y cada regla de la sección 4 y se corrigen las desviaciones.

Antes de mover nada, anota el número de tests medido en la línea base (procedimiento común, sección 2.1.1; 51 en 6 clases a 2026-11-04) y el JSON de las respuestas que cubre `FacturaControllerTest`, para comprobar al final que se preservan.

### 2. Mapa de Facturación: clase actual → destino

Prefijo común: `com.facturacion.factura`.

| Clase actual | Destino | Nota |
|---|---|---|
| `apln.CrearFactura`, `apln.ObtenerFactura`, `apln.ListarFacturas`, `apln.EliminarFactura` | `internal.application.*` | Misma clase, solo cambia el paquete |
| `apln.FacturaDtoConverter` | `internal.application.FacturaDtoConverter` | |
| — | `api.facade.FacturaFacade` | Nueva (sección 3) |
| — | `internal.application.FacturaFacadeImpl` | Nueva, decisión D1 |
| `controller.FacturaController` | `api.http.FacturaController` | Sigue delegando en los casos de uso de `internal.application`, no en la facade; convierte `api.http.dto` ↔ `api.facade.dto` (D2, sección 3.4) |
| `controller.FacturaExceptionHandler` | `api.http.FacturaExceptionHandler` | Mismos códigos y `ProblemDetail` |
| `api.dto.CrearFacturaRequest`, `api.dto.LineaFacturaRequest` | `api.http.dto.*` | Conservan Bean Validation |
| `api.dto.FacturaDto`, `api.dto.LineaFacturaDto`, `api.dto.NuevaFacturaDto`, `api.dto.NuevaLineaFacturaDto` | `api.facade.dto.*` | Contrato de casos de uso y facade |
| — | `api.http.dto.FacturaResponse`, `api.http.dto.LineaFacturaResponse` | Nuevas, D3: mismos campos y mismo JSON que `FacturaDto` / `LineaFacturaDto` |
| `domain.model.Factura`, `domain.model.LineaFactura` | `internal.domain.model.*` | |
| `domain.model.CalculadoraFactura` | `internal.domain.service.CalculadoraFactura` | |
| `domain.model.FacturaInvalidaException`, `FacturaNoEncontradaException`, `NumeroFacturaDuplicadoException` | `internal.domain.exception.*` | `api.http.FacturaExceptionHandler` las importa solo para traducirlas a HTTP (`/k-back-controller`) |
| `domain.repository.FacturaRepository` | `internal.domain.repository.FacturaRepository` | |
| `persistence.entity.FacturaEntity`, `persistence.entity.LineaFacturaEntity` | `internal.persistence.entity.*` | Sin cambiar `@Table`/`@Column` |
| `persistence.dao.FacturaDao` | `internal.persistence.dao.FacturaDao` | |
| `persistence.mapper.FacturaMapper` | `internal.persistence.mapper.FacturaMapper` | |
| `persistence.JpaFacturaRepository` | `internal.persistence.repository.JpaFacturaRepository` | |
| — | `package-info.java` en `factura`, `api.facade` y `api.facade.dto` | Contenido según `/k-back-module` sección 9 |
| — | `com.facturacion.app.package-info.java` | Sección 3 |
| `FacturacionApplication` | Sin cambios en `com.facturacion` | |

Tests (paquete espejo, `/k-back-testing`):

| Test actual | Destino |
|---|---|
| `factura.domain.model.FacturaTest` | `factura.internal.domain.model.FacturaTest` |
| `factura.domain.model.CalculadoraFacturaTest` | `factura.internal.domain.service.CalculadoraFacturaTest` |
| `factura.persistence.mapper.FacturaMapperTest` | `factura.internal.persistence.mapper.FacturaMapperTest` |
| `factura.apln.CasosDeUsoFacturaTest`, `factura.apln.FacturaRepositoryEnMemoria` | `factura.internal.application.*` |
| `factura.controller.FacturaControllerTest` | `factura.api.http.FacturaControllerTest`: solo cambian el paquete y los imports; casos, mocks de casos de uso y aserciones de JSON y códigos idénticos |
| `com.facturacion.ArquitecturaTest` | Mismo paquete, reglas adaptadas y ampliadas (sección 4) |
| — | `com.facturacion.ModulosTest` (nuevo) y test de `FacturaFacadeImpl` en `factura.internal.application` |

Flyway: `src/main/resources/db/migration/V1__crear_facturas.sql` NO se mueve ni se modifica (ubicación global de `/k-back-module` sección 8.4 y migraciones publicadas inmutables de `/k-back-persistence`). La reubicación de entidades no exige migración nueva; DEBE comprobarse que las entidades movidas conservan exactamente su mapeo para que `ddl-auto: validate` siga cuadrando.

Para cualquier otro módulo existente se construye un mapa equivalente aplicando la misma tabla canónica, y se registra antes de mover código.

### 3. Decisiones de la Etapa 3 que DEBEN informarse

3.1. **`FacturaFacade` y `FacturaFacadeImpl`.** `/k-back-module` prohíbe crear una facade sin consumidor real, pero la especificación de la arquitectura objetivo del usuario exige `FacturaFacade` en la Etapa 3: se crea porque es un requisito explícito del usuario, y así DEBE documentarse en el informe y en el mensaje de commit. Sus operaciones DEBEN ser las que enumere esa especificación o el plan; si no las enumera, DEBE preguntarse al usuario antes de crearla (no se replican todos los casos de uso por defecto). La implementación se llama `FacturaFacadeImpl` (D1) y sigue `/k-back-service` sección 8.

3.2. **Paquete `app`.** La arquitectura objetivo incluye `<raiz>.app` como raíz de composición. Hoy no hay configuración transversal en Facturación: NO DEBEN crearse clases de configuración vacías. Se crea `com/facturacion/app/package-info.java` con `@ApplicationModule` con `displayName` y `allowedDependencies` explícito (hoy vacío, porque `app` no referencia ningún módulo) según `/k-back-module` secciones 9.3 y 9.6, como única pieza justificada por la arquitectura objetivo, y se informa. Si el usuario no la quiere sin configuración real, se omite y se informa.

3.3. **Spring Modulith.** Añadir a `pom.xml` la línea 1.4.x compatible con Spring Boot 3.5 (versión revalidada en Maven Central al ejecutar), con las dependencias que indica `/k-back-module` sección 9.2, y alinear `archunit.version` con la versión de ArchUnit que arrastra Modulith (hoy `pom.xml` fija 1.4.1). Si resulta incompatible, se aplica la alternativa sin Modulith de `/k-back-module` sección 9.8. Ambas situaciones DEBEN informarse (`/k-back` sección 14.6).

3.4. **Controlador, casos de uso y DTO** (D2, D3). Decisión mínima y coherente que DEBE documentarse:

- `FacturaController` (`api.http`) sigue delegando en los casos de uso de `internal.application`, según `/k-back-controller` sección 2.3. La facade es solo el contrato para otros módulos.
- Los casos de uso reciben y devuelven DTO de `api.facade.dto` (`NuevaFacturaDto`, `FacturaDto`), que es donde quedan los DTO de contrato (`/k-back-dto` sección 6.3). Por eso el controlador depende de `api.facade.dto` del propio módulo (D2): convierte `CrearFacturaRequest` → `NuevaFacturaDto` y `FacturaDto` → `FacturaResponse` con métodos privados estáticos, como hace hoy `aNuevaFactura`. No se crea un tercer juego de DTO.
- `FacturaResponse` / `LineaFacturaResponse` (D3): mismos campos y nombres que `FacturaDto` / `LineaFacturaDto`, de modo que el JSON HTTP es idéntico al actual.
- `FacturaControllerTest` solo cambia paquete e imports: sigue simulando los casos de uso con `@MockitoBean` y sus aserciones de JSON y códigos no cambian. Si alguna aserción tuviera que cambiar, el JSON no se ha preservado y DEBE corregirse el código, no el test.

### 4. Tests de arquitectura

4.1. `ArquitecturaTest`: adaptar las 6 reglas existentes a los paquetes de la Etapa 3 sin perder ninguna restricción, y añadir las reglas de `internal` no accesible desde otros módulos y de ningún módulo dependiendo de `app`, todo según `/k-back-testing` sección 4.3. Ninguna regla se relaja.

4.2. Las reglas usan `allowEmptyShould(true)`, así que una regla cuyo paquete ya no existe pasa sin comprobar nada. Por eso cada unidad que reubica una capa DEBE adaptar, EN LA MISMA UNIDAD, las reglas de `ArquitecturaTest` que nombran esa capa (p. ej. al mover `controller` a `api.http`, la regla `controladoresSinRepositoryNiServiceInterno`), y V3 DEBE comprobar que ninguna regla ha quedado vacía tras mover paquetes (regla de V3 de `/k-back-testing`). La unidad 10 queda solo para las reglas nuevas y `ModulosTest`.

4.3. `ModulosTest`: verificación de módulos con Spring Modulith según `/k-back-testing` sección 5.

### 5. Planificar las 10 unidades (Fase A)

Registra las 10 unidades según el procedimiento común (sección 2.1). Unidades para Facturación, en este orden:

1. Línea base: número de tests, JSON de respuestas y mapa de la sección 2 registrados; Spring Modulith y alineación de ArchUnit en `pom.xml` (sección 3.3).
2. Dominio: `Factura`, `LineaFactura` → `internal.domain.model`; excepciones → `internal.domain.exception`; `CalculadoraFactura` → `internal.domain.service`; con sus tests y la regla `dominioIndependienteDeFrameworks` apuntando ya a `..internal.domain..`.
3. `FacturaRepository` → `internal.domain.repository` y `FacturaRepositoryEnMemoria` adaptado.
4. Persistencia → `internal.persistence.{entity,dao,mapper,repository}` con `FacturaMapperTest` y las reglas `entidadesSoloEnPersistencia` y `persistenciaNoUsadaFueraDePersistencia` adaptadas y no vacías. La comprobación del mapeo frente a `V1__crear_facturas.sql` es manual (revisión de `@Table`/`@Column` contra la migración), porque `ddl-auto: validate` no se ejecuta en los tests; PUEDE completarse arrancando la aplicación con `docker compose up mariadb` si Docker está disponible y, si no, la carencia DEBE informarse.
5. DTO de contrato → `api.facade.dto`; DTO HTTP de entrada → `api.http.dto`.
6. Casos de uso y `FacturaDtoConverter` → `internal.application` con `CasosDeUsoFacturaTest`, y las reglas que nombran `..apln..` (`aplicacionSinControladores`, `dominioIndependienteDeFrameworks`) adaptadas a `..internal.application..`.
7. `FacturaFacade` en `api.facade` y `FacturaFacadeImpl` en `internal.application` (sección 3.1).
8. `FacturaController` y `FacturaExceptionHandler` → `api.http`; `FacturaResponse` y `LineaFacturaResponse`; `FacturaControllerTest` movido (solo paquete e imports) y en verde; las reglas que nombran `..controller..` (`controladoresSinRepositoryNiServiceInterno`, `aplicacionSinControladores`, `dominioIndependienteDeFrameworks`) adaptadas a `..api.http..`.
9. `package-info.java` de `factura`, `api.facade`, `api.facade.dto` y `app` (secciones 3.2 y 3.3).
10. Reglas nuevas de `ArquitecturaTest` (`internal` no accesible desde otros módulos, ningún módulo depende de `app`), `ModulosTest`, comprobación de que no quedan paquetes antiguos y suite completa. Las reglas existentes ya se adaptaron en las unidades 2, 4, 6 y 8 (sección 4.2).

En modo auditoría, cada unidad revisa el área equivalente; si no da trabajo real para 10, se informa (procedimiento común, sección 2.1.4).

### 6. Ejecutar la Fase A

Ejecuta las 10 iteraciones Programa → Verifica (V1-V7 de `/k-back-testing`; en V3 se incluye `ModulosTest` desde que exista) → Arregla según el procedimiento común (sección 2.2). Los movimientos de archivos DEBEN hacerse con `git mv` para conservar el historial. Cada unidad DEBE dejar el proyecto compilando; no se acumulan paquetes a medio mover entre iteraciones.

### 7. Ejecutar la Fase B

Según el procedimiento común (sección 3). Como mínimo: test de `FacturaFacadeImpl` a través de su interfaz (`/k-back-testing` sección 3.6), reglas ArchUnit nuevas, `ModulosTest` y comprobación de que el número de tests es igual o mayor que la línea base.

### 8. Publicar e integrar

Push de la rama. En modo coordinador, subagente integrador; en modo subagente, se entrega la rama sin integrar.

### 9. Informe

Informe final según el procedimiento común (sección 6), añadiendo:

- Mapa aplicado (sección 2) con cualquier desviación justificada.
- Decisiones de la sección 3: facade y sus operaciones, `app`, versión de Spring Modulith y de ArchUnit, `Response` HTTP.
- Número de tests antes y después, y confirmación de que el JSON HTTP no ha cambiado.

## Comprobaciones antes de finalizar

- [ ] Todas las clases y tests están en su destino del mapa; no quedan paquetes `apln`, `controller`, `api.dto`, `domain` ni `persistence` de la Etapa 2.
- [ ] Endpoints, códigos, `ProblemDetail` y JSON idénticos a los de partida.
- [ ] `FacturaFacade` creada con operaciones confirmadas por el usuario o la especificación, e implementada en `FacturaFacadeImpl`; decisión documentada.
- [ ] `package-info.java` sin comentarios según `/k-back-module`; `app` decidido e informado.
- [ ] Spring Modulith y alineación de ArchUnit informados (o alternativa sin Modulith informada).
- [ ] `V1__crear_facturas.sql` intacta y mapeo de entidades sin cambios.
- [ ] `ArquitecturaTest` sin reglas relajadas ni vacías tras mover paquetes, adaptado en la misma unidad que cada capa y ampliado; `ModulosTest` en verde.
- [ ] `FacturaController` delega en los casos de uso, no en la facade; `FacturaControllerTest` solo ha cambiado paquete e imports.
- [ ] Ningún test perdido; suite completa en verde con su salida real.
- [ ] Las 10 iteraciones y la Fase B registradas; commits con `Co-Authored-By`, push e integración hechos o entregados al coordinador.
