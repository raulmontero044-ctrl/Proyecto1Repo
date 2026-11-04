---
name: k-back
description: Normas generales del backend Java/Spring Boot de Facturación - modelo de etapas y organización de paquetes (tabla canónica), dependencias entre capas, convenciones de nombres, inyección de dependencias, errores, validación, SOLID, Clean Code, seguridad, logging, comentarios, antiabstracción, preservación del código existente y verificación. Úsala antes de diseñar, implementar o revisar cualquier código backend; las skills /k-back-* de capa la complementan. No modifica código.
argument-hint: "[tema o duda concreta, opcional]"
---

# /k-back

## Propósito

Reunir las normas generales que se aplican a todo el backend y el modelo de etapas que fija la organización de paquetes. Es una skill de conocimiento: NO modifica código, NO ejecuta Git y NO define procedimientos de implementación. Las skills ejecutoras `do-code-*` aplican estas normas; las skills `/k-back-*` de capa las concretan sin repetirlas.

Los términos DEBE, NO DEBE y PUEDE son normativos.

## Cuándo usarla

- Antes de diseñar, implementar, refactorizar o revisar código del backend.
- Para decidir en qué paquete va una clase según la etapa del proyecto.
- Para resolver qué skill es propietaria de una norma concreta.

## Entradas

Ninguna obligatoria. Si recibe `$ARGUMENTS`, se interpreta como el tema o la duda concreta a la que aplicar estas normas.

## Resultado esperado

Normas, criterios y checklists aplicables. Ningún archivo se crea ni se modifica.

## Skills relacionadas y propiedad de las normas

Cada norma vive en una sola skill. Esta tabla DEBE consultarse antes de añadir o aplicar una regla detallada:

| Tema | Skill propietaria |
|---|---|
| Normas generales, modelo de etapas, dependencias entre capas | `/k-back` |
| Modelo de dominio, objetos de valor, servicios de dominio, invariantes, excepciones de dominio, independencia de framework | `/k-back-domain` |
| Interfaces Repository (contrato desde el dominio) | `/k-back-repository` |
| Entidades JPA, DAO/Spring Data, implementaciones de Repository, mappers, relaciones, consultas, transacciones en persistencia | `/k-back-persistence` |
| Casos de uso, orquestación, límite transaccional, permisos, uso de DTO en aplicación, servicios internos | `/k-back-service` |
| Endpoints, verbos, códigos de estado, validación de entrada HTTP, manejo de excepciones HTTP | `/k-back-controller` |
| DTO `record`, DTO HTTP frente a DTO de contrato público, Bean Validation en DTO, mapeo DTO | `/k-back-dto` |
| Módulos, límites, contratos públicos (facade), dependencias y ciclos entre módulos, `package-info.java` | `/k-back-module` |
| Qué probar en cada capa, JUnit 5, Mockito, AssertJ, `@WebMvcTest`, `@DataJpaTest`, integración, ArchUnit, Spring Modulith verify | `/k-back-testing` |

## Normas

### 1. Stack y estilo arquitectónico

1.1. El backend es Java 21 con Spring Boot 3.5 y Maven mediante el wrapper `./mvnw` (ver `pom.xml`). Dependencias actuales: `spring-boot-starter-web`, `spring-boot-starter-validation`, `spring-boot-starter-data-jpa`, Flyway con MariaDB, `spring-modulith-api` (BOM `spring-modulith-bom` 1.4.14) y, en test, `spring-boot-starter-test`, `archunit-junit5` 1.4.2 y `spring-modulith-starter-test`.

1.2. El paquete raíz es `com.facturacion` y la clase de arranque `com.facturacion.FacturacionApplication`. El único módulo de negocio actual es `factura`; `com.facturacion.app` es la raíz de composición y también la declara Spring Modulith como módulo. En cualquier otro proyecto `<raiz>` y `<modulo>` DEBEN obtenerse del código real; NO DEBEN inventarse.

1.3. El backend es un monolito modular: una única aplicación desplegable compuesta por módulos de negocio. Cada módulo DEBE encapsular sus responsabilidades y exponer a los demás solo su contrato público (detalle en `/k-back-module`).

1.4. Se DEBEN usar las capacidades del lenguaje cuando aporten claridad: `record` para datos inmutables, `Optional` en retornos que pueden no tener valor (nunca en campos ni parámetros), `List.copyOf`/`toList()` para colecciones inmutables, `BigDecimal` para importes.

### 2. Modelo de etapas (tabla canónica)

Esta sección es la referencia única de organización de paquetes. Las demás skills DEBEN referenciarla y NO DEBEN redefinirla.

Marcadores: `<raiz>` paquete raíz real; `<modulo>` módulo real.

| Etapa | Descripción | Capa de aplicación | Configuración transversal |
|---|---|---|---|
| 1 | Arquitectura básica en capas, sin módulos | `apln` | `<raiz>.config` |
| 2 | Monolito modular | `<modulo>.apln` | `<raiz>.config` |
| 3 | Arquitectura modular definitiva con `api` / `internal` (estructura actual de Facturación) | `<modulo>.internal.application` | `<raiz>.app` |

El nombre `apln` es la convención existente del repositorio en las Etapas 1 y 2 y NO DEBE renombrarse en ellas. En la Etapa 3 la capa de aplicación DEBE llamarse `internal.application`.

#### Etapa 1 — Arquitectura básica (sin módulos)

```text
<raiz>/
├── <App>Application.java
├── config/
├── api/dto/             DTO record de casos de uso y HTTP
├── controller/          controladores y manejador de excepciones HTTP
├── apln/                casos de uso, límite transaccional
├── service/             (solo si está justificado)
├── domain/
│   ├── model/           modelos, VO, invariantes, excepciones de dominio, servicios de dominio puros
│   └── repository/      interfaces Repository
└── persistence/         implementaciones de Repository
    ├── dao/
    ├── entity/
    └── mapper/
```

#### Etapa 2 — Monolito modular

```text
<raiz>/
├── <App>Application.java
├── config/              configuración transversal
└── <modulo>/
    ├── api/dto/         DTO record de contrato público y HTTP (convención existente)
    ├── apln/
    ├── config/          (solo si está justificada)
    ├── controller/
    ├── service/         (solo si está justificado)
    ├── domain/{model,repository}
    └── persistence/{dao,entity,mapper} + implementaciones Repository en la raíz de persistence
```

Comunicación entre módulos: solo a través de `<modulo>.api` (DTO de `api.dto` y puntos de entrada públicos expuestos en `api`).

Ejemplo de Etapa 2 (estructura anterior de Facturación, ya migrada a la Etapa 3): `com.facturacion.factura.apln.CrearFactura`, `com.facturacion.factura.controller.FacturaController`, `com.facturacion.factura.domain.repository.FacturaRepository`, `com.facturacion.factura.persistence.JpaFacturaRepository`, `com.facturacion.factura.persistence.dao.FacturaDao`, `com.facturacion.factura.api.dto.FacturaDto`.

#### Etapa 3 — Arquitectura modular definitiva

```text
<raiz>/
├── <App>Application.java
├── app/                                 raíz de composición: configuración transversal, beans, cableado
└── <modulo>/
    ├── package-info.java                modelo de módulo
    ├── api/
    │   ├── facade/                      <Modulo>Facade (interfaz pública entre módulos)
    │   │   └── dto/                     DTO record de contrato
    │   └── http/
    │       ├── controllers/             <Modulo>Controller
    │       ├── handlers/                un <Excepcion>Handler por excepción de dominio
    │       └── dto/                     DTO record HTTP
    └── internal/
        ├── application/                 casos de uso + implementación de la facade; límite transaccional
        ├── domain/
        │   ├── model/
        │   ├── service/                 servicios de dominio puros (p. ej. CalculadoraFactura)
        │   ├── exception/
        │   └── repository/
        ├── infrastructure/              adaptadores técnicos e integraciones externas (solo si existen)
        └── persistence/
            ├── entity/
            ├── dao/
            ├── mapper/
            └── repository/              implementaciones de Repository
```

Reglas de dependencia de la Etapa 3: otros módulos solo usan `<modulo>.api.facade` (y su `dto`); `internal.domain` no depende de Spring/JPA ni de otras capas; las entidades JPA no salen de `internal.persistence`; `app` puede referenciar cualquier módulo para cableado, pero ningún módulo depende de `app` salvo configuración genérica; está prohibido el acceso a `internal` de otro módulo; sin ciclos. `api.http` depende de `internal.application`, `api.http.dto`, `api.facade.dto` e `internal.domain.exception` del propio módulo (esta última solo para traducir a HTTP); NO DEBE depender de `internal.domain.repository`, `internal.persistence` ni de `api` de otro módulo. El controlador delega en los casos de uso, no en la facade. El mecanismo de `package-info.java` (Spring Modulith o ArchUnit) es propiedad de `/k-back-module`. Facturación ya ha adoptado Spring Modulith: `factura` y `app` llevan `@ApplicationModule`, y `api.facade` y `api.facade.dto` son sus interfaces con nombre (`facade` y `facade-dto`).

Ejemplo real en Etapa 3 (módulo `factura`):

| Paquete | Clases |
|---|---|
| `api.facade` | `FacturaFacade` |
| `api.facade.dto` | `NuevaFacturaDto`, `NuevaLineaFacturaDto`, `FacturaDto`, `LineaFacturaDto` |
| `api.http.controllers` | `FacturaController` |
| `api.http.handlers` | `FacturaNoEncontradaExceptionHandler` (404), `NumeroFacturaDuplicadoExceptionHandler` (409), `FacturaInvalidaExceptionHandler` (400) |
| `api.http.dto` | `CrearFacturaRequest`, `LineaFacturaRequest`, `FacturaResponse`, `LineaFacturaResponse` |
| `internal.application` | casos de uso `CrearFactura`, `ObtenerFactura`, `ListarFacturas` y `EliminarFactura`; `FacturaFacadeImpl`, `FacturaDtoConverter` |
| `internal.domain.model` | `Factura`, `LineaFactura` |
| `internal.domain.service` | `CalculadoraFactura` |
| `internal.domain.exception` | `FacturaInvalidaException`, `FacturaNoEncontradaException`, `NumeroFacturaDuplicadoException` |
| `internal.domain.repository` | `FacturaRepository` |
| `internal.persistence` | `entity.FacturaEntity`, `entity.LineaFacturaEntity`, `dao.FacturaDao`, `mapper.FacturaMapper`, `repository.JpaFacturaRepository` |

Casos de uso de Facturación:

| Caso de uso | Operación | Transacción | Endpoint | Operación de `FacturaFacade` |
|---|---|---|---|---|
| `CrearFactura` | Crea una factura a partir de `NuevaFacturaDto`; el dominio rechaza las invariantes incumplidas (`FacturaInvalidaException`) y el caso de uso un `numero` duplicado (`NumeroFacturaDuplicadoException`) | `@Transactional` | `POST /api/facturas` → `201` con `Location`, `400` o `409` | `crear(NuevaFacturaDto)` |
| `ObtenerFactura` | Devuelve una factura por id o lanza `FacturaNoEncontradaException` | `@Transactional(readOnly = true)` | `GET /api/facturas/{id}` → `200` o `404` | `obtener(Long)` |
| `ListarFacturas` | Devuelve todas las facturas | `@Transactional(readOnly = true)` | `GET /api/facturas` → `200` | `listar()` |
| `EliminarFactura` | Elimina una factura por id o lanza `FacturaNoEncontradaException` | `@Transactional` | `DELETE /api/facturas/{id}` → `204` o `404` | `eliminar(Long)` |

`CrearFactura`, `ObtenerFactura` y `ListarFacturas` devuelven `FacturaDto`, cuyos totales calcula `FacturaDtoConverter` mediante `CalculadoraFactura`. `FacturaController` delega en los cuatro casos de uso. `FacturaFacadeImpl` expone las mismas cuatro operaciones a otros módulos y delega cada una en un único caso de uso, sin `@Transactional` propio. Que la facade exponga los cuatro casos de uso lo aprobó el usuario y NO DEBE señalarse como incumplimiento de `/k-back-module` 6.3.

#### Reglas de evolución

- La etapa actual DEBE detectarse inspeccionando los paquetes reales. Facturación está hoy en Etapa 3.
- NUNCA se retrocede de etapa. En un proyecto en Etapa 2 o 3 NO DEBEN desmontarse módulos: se auditan y corrigen los principios de separación de capas dentro de la estructura existente, y se informa.
- Las funcionalidades, endpoints, reglas de negocio y tests existentes DEBEN preservarse en toda transformación.
- La estructura es de referencia, no una plantilla: solo DEBEN existir los paquetes que contengan clases necesarias.
- `<modulo>.config` SOLO PUEDE contener configuración específica y justificada del módulo; la transversal va en `<raiz>.config` (Etapas 1-2) o `<raiz>.app` (Etapa 3).

### 3. Dependencias entre capas

Dependencias permitidas dentro de un módulo en Etapas 1 y 2 (A → B: A puede depender de B):

```text
controller  → apln, api/dto, excepciones de domain/model del propio módulo (solo para traducirlas a HTTP)
apln        → domain, service, api/dto, contratos públicos de otros módulos
service     → domain, api/dto, contratos públicos de otros módulos
persistence → domain
domain      → (ningún otro paquete del módulo ni de otros módulos)
api/dto     → (solo tipos Java estándar, anotaciones de validación y otros DTO públicos)
```

3.1. `controller` DEBE delegar en casos de uso de `apln`. NO DEBE depender de `domain/repository`, `persistence` ni `service`.

3.2. `apln` DEBE coordinar mediante contratos de dominio (interfaces Repository y modelos).

3.3. `persistence` implementa los contratos Repository y NO DEBE ser referenciado por ninguna otra capa salvo para el cableado de Spring.

3.4. Ninguna capa DEBE saltarse capas para acceder a DAO o entidades de persistencia.

3.5. Las transacciones DEBEN gestionarse en el límite de la capa de aplicación (`apln` o `internal.application`). NO DEBEN declararse en `controller` ni en `domain`. El detalle es de `/k-back-service`.

3.6. El paquete de configuración transversal NO DEBE importar excepciones de ningún módulo; SOLO PUEDE tratar excepciones genéricas de framework o de Java.

3.7. Para la Etapa 3 se aplican las reglas de dependencia de la sección 2. Las dependencias entre módulos son propiedad de `/k-back-module`.

3.8. Estas reglas están parcialmente automatizadas en `src/test/java/com/facturacion/ArquitecturaTest.java` (ArchUnit). Cambiar de etapa exige adaptar ese test (ver `/k-back-testing`).

### 4. Convenciones de nombres

4.1. Paquetes en minúsculas, sin guiones bajos. Clases en `PascalCase`, métodos y campos en `camelCase`, constantes en `UPPER_SNAKE_CASE`.

4.2. El dominio y los nombres de negocio están en español, siguiendo el código existente: `Factura`, `LineaFactura`, `CalculadoraFactura`, `FacturaInvalidaException`.

4.3. Convenciones existentes que DEBEN reproducirse:

| Elemento | Convención | Ejemplo real |
|---|---|---|
| Caso de uso | Verbo + sustantivo, método `ejecutar` | `CrearFactura.ejecutar(NuevaFacturaDto)` |
| Controlador | `<Agregado>Controller` | `FacturaController` |
| Manejador HTTP | `<Agregado>ExceptionHandler` en las Etapas 1 y 2; en la Etapa 3, un `<Excepcion>Handler` por excepción de dominio | `FacturaNoEncontradaExceptionHandler` |
| Interfaz Repository | `<Agregado>Repository` | `FacturaRepository` |
| Implementación Repository | `<Tecnología><Agregado>Repository` | `JpaFacturaRepository` |
| DAO | `<Agregado>Dao` | `FacturaDao` |
| Entidad JPA | `<Agregado>Entity` | `FacturaEntity` |
| Mapper | `<Agregado>Mapper` | `FacturaMapper` |
| DTO de salida | `<Agregado>Dto` | `FacturaDto` |
| DTO de entrada a caso de uso | `Nuevo/Nueva<Agregado>Dto` | `NuevaFacturaDto` |
| DTO de petición HTTP | `<Accion><Agregado>Request` | `CrearFacturaRequest` |
| Excepción de dominio | descripción + `Exception` | `FacturaNoEncontradaException` |
| Migración Flyway | `V<n>__<descripcion>.sql` | `V1__crear_facturas.sql` |

4.4. Los nombres DEBEN revelar intención; NO DEBEN usarse abreviaturas opacas ni sufijos genéricos como `Manager`, `Helper` o `Util` sin justificación.

### 5. Inyección de dependencias

5.1. La inyección DEBE hacerse por constructor con campos `private final`, como en `CrearFactura` y `FacturaController`.

5.2. NO DEBE usarse `@Autowired` en campos ni setters. Con un único constructor, `@Autowired` es innecesario.

5.3. Las dependencias DEBEN declararse sobre contratos del dominio (p. ej. `FacturaRepository`), no sobre implementaciones técnicas.

5.4. NO DEBEN usarse localizadores de servicios (`ApplicationContext.getBean`) ni estado estático mutable.

### 6. Manejo de errores

6.1. Las violaciones de reglas de negocio DEBEN expresarse con excepciones de dominio no comprobadas y con mensaje significativo (detalle en `/k-back-domain`).

6.2. Las excepciones NO DEBEN silenciarse ni capturarse para devolver `null`. Solo se capturan para traducirlas, añadir contexto o recuperarse de verdad.

6.3. La traducción a HTTP DEBE hacerse en el adaptador HTTP del propio módulo, con `ProblemDetail` como en los handlers de `factura.api.http.handlers` (detalle en `/k-back-controller`).

6.4. Las respuestas de error NO DEBEN exponer trazas, SQL, nombres de clases internas ni datos sensibles.

### 7. Validaciones

7.1. Se validan dos niveles complementarios: validación de entrada con Bean Validation en los DTO (`/k-back-dto`) activada con `@Valid` en el controlador (`/k-back-controller`), e invariantes de negocio en el modelo de dominio (`/k-back-domain`).

7.2. Las invariantes DEBEN residir en el dominio aunque exista validación de entrada; la validación HTTP no sustituye a las invariantes.

7.3. NO DEBEN duplicarse reglas de negocio en controladores, DAO ni mappers.

### 8. SOLID, Clean Code y separación de responsabilidades

8.1. Cada clase DEBE tener una única razón de cambio. Un caso de uso por clase en la capa de aplicación, como `CrearFactura`, `ObtenerFactura`, `ListarFacturas`, `EliminarFactura`.

8.2. Las dependencias DEBEN apuntar hacia el dominio (inversión de dependencias mediante interfaces Repository).

8.3. Los métodos DEBEN ser cortos, con un solo nivel de abstracción y sin efectos secundarios ocultos. Se DEBEN preferir retornos tempranos a anidamientos profundos.

8.4. NO DEBE haber código muerto, duplicado ni parámetros booleanos que cambien el comportamiento de un método.

8.5. La inmutabilidad DEBE preferirse siempre que sea posible.

### 9. Seguridad

9.1. NO DEBEN incluirse secretos, contraseñas ni credenciales en el código ni en el repositorio. La configuración sensible DEBE leerse de variables de entorno, como hace `application.yml` (`${DB_PASSWORD:...}`); los valores por defecto solo son aceptables para desarrollo local.

9.2. Toda entrada externa DEBE validarse antes de usarse.

9.3. Las consultas DEBEN ser parametrizadas; NO DEBE concatenarse entrada de usuario en SQL o JPQL.

9.4. Las entidades de persistencia y los modelos de dominio NO DEBEN serializarse hacia el exterior.

9.5. NO DEBEN añadirse mecanismos de seguridad (autenticación, autorización) sin requisito explícito; los permisos en casos de uso son propiedad de `/k-back-service`.

### 10. Logging

10.1. Si se registra, DEBE usarse SLF4J (`org.slf4j.Logger`), incluido en Spring Boot. NO DEBEN usarse `System.out`, `System.err` ni `printStackTrace`.

10.2. NO DEBEN registrarse datos personales o sensibles (p. ej. NIF del cliente), contraseñas ni tokens.

10.3. Una excepción DEBE registrarse una sola vez, en el punto donde se trata, no en cada capa que la propaga.

10.4. El código actual no registra; NO DEBE añadirse logging sin un motivo concreto (diagnóstico de operación, integración externa, error no esperado).

### 11. Documentación de decisiones

11.1. Como el código no lleva comentarios (sección 12), las decisiones técnicas DEBEN documentarse en el informe de la tarea, en el mensaje de commit o en la descripción del pull request.

11.2. Toda decisión no derivada directamente de los requisitos (nueva dependencia, cambio de etapa, desviación de una convención) DEBE informarse explícitamente con su motivo.

### 12. Política de comentarios

12.1. El código Java generado o modificado, de aplicación y de test, NO DEBE contener comentarios de ningún tipo: ni de línea (`//`), ni de bloque (`/* */`), ni JavaDoc (`/** */`), salvo que una herramienta lo requiera.

12.2. Los comentarios existentes en código no tocado por la tarea DEBEN preservarse.

12.3. Por esta política, `package-info.java` lleva anotaciones y no JavaDoc.

### 13. Criterios contra abstracciones innecesarias

13.1. NO DEBEN crearse clases, interfaces, paquetes ni archivos vacíos para completar una estructura.

13.2. NO DEBEN crearse interfaces con una única implementación salvo las interfaces Repository (exigidas por `/k-back-repository`), la facade pública de la Etapa 3 (`/k-back-module`) o cuando otra norma lo requiera.

13.3. NO DEBEN crearse servicios que solo deleguen sin aportar lógica, coordinación ni responsabilidad diferenciada.

13.4. NO DEBEN crearse mappers, DTO, excepciones ni utilidades que no se usen.

13.5. NO DEBEN añadirse operaciones Repository, endpoints, campos ni parámetros no justificados por los requisitos.

13.6. Toda clase nueva DEBE poder justificarse con un requisito concreto.

### 14. Inspección y preservación del código existente

14.1. Antes de crear o modificar código DEBE inspeccionarse la estructura real: paquete raíz, módulos, etapa, `pom.xml`, dependencias, convenciones de nombres y código relacionado.

14.2. DEBEN reutilizarse las clases existentes compatibles con los requisitos en lugar de duplicarlas.

14.3. DEBEN respetarse las convenciones existentes compatibles con estas normas. Si una convención contradice estas normas, DEBE informarse la incompatibilidad en lugar de modificar código ajeno sin autorización.

14.4. NO DEBE eliminarse, renombrarse ni reescribirse código que no forme parte de la tarea.

14.5. NO DEBEN inventarse requisitos, clases, endpoints, campos, tablas, columnas, relaciones, paquetes, dependencias ni decisiones técnicas. Si falta información imprescindible, DEBE informarse el bloqueo.

14.6. NO DEBEN añadirse dependencias a `pom.xml` sin que la tarea lo requiera; si se añaden, DEBE informarse.

### 15. Verificación y testing (alto nivel)

15.1. Tras cambios en código DEBEN ejecutarse la compilación y las pruebas del repositorio con el wrapper: `./mvnw test` (o `./mvnw verify`). En Windows sin shell POSIX, `mvnw.cmd test`.

15.2. Las pruebas de arquitectura DEBEN ejecutarse y pasar: `ArquitecturaTest` (ArchUnit), `ReglaContratoEntreModulosTest` (casos positivos y negativos de las reglas entre módulos) y `ModulosTest` (Spring Modulith verify).

15.3. Si una verificación no está disponible o falla, DEBE informarse con su salida real; NO DEBE afirmarse que el código cumple sin haberlo verificado.

15.4. Todo cambio de comportamiento DEBE ir acompañado de pruebas. Qué probar en cada capa y con qué herramientas es propiedad de `/k-back-testing`.

## Comprobaciones antes de finalizar

- [ ] La etapa del proyecto se ha detectado y la ubicación de cada clase coincide con la tabla canónica de la sección 2.
- [ ] No se ha retrocedido de etapa ni se han perdido funcionalidades, endpoints, reglas o tests.
- [ ] Las dependencias entre capas cumplen la sección 3.
- [ ] Los nombres siguen las convenciones existentes de la sección 4.
- [ ] La inyección es por constructor con campos `final`.
- [ ] No hay secretos, `System.out` ni datos sensibles en logs o respuestas.
- [ ] No hay comentarios en el código generado, de aplicación ni de test.
- [ ] No hay abstracciones, clases ni dependencias sin requisito que las justifique.
- [ ] Las decisiones no derivadas de los requisitos están informadas.
- [ ] La compilación, las pruebas y `ArquitecturaTest` se han ejecutado y su resultado real está informado.
- [ ] Las normas de capa se han tomado de su skill propietaria y no se han duplicado aquí.
