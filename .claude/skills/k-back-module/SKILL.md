---
name: k-back-module
description: Normas de módulos del backend Java/Spring Boot de Facturación - identificación de módulos, límites y responsabilidades, contrato público (facade y DTO), dependencias permitidas y ciclos entre módulos, raíz de composición app, integración de un módulo nuevo, package-info.java con Spring Modulith o ArchUnit y checklist de límites. Úsala al crear, ampliar, separar o revisar módulos o cualquier dependencia entre módulos. No modifica código.
argument-hint: "[módulo o duda concreta sobre límites o dependencias entre módulos]"
---

# /k-back-module

## Propósito

Fijar qué es un módulo de negocio, dónde están sus límites, cómo se comunica con otros módulos y cómo se declaran y verifican esos límites. Es una skill de conocimiento: NO modifica código, NO ejecuta Git y NO define procedimientos de implementación.

Las normas generales, la tabla canónica de paquetes por etapa y las dependencias entre capas dentro de un módulo son de `/k-back` y aquí solo se referencian. Los términos DEBE, NO DEBE y PUEDE son normativos.

## Cuándo usarla

- Antes de crear un módulo nuevo o de decidir si una funcionalidad pertenece a un módulo existente.
- Cuando un módulo necesite datos u operaciones de otro módulo.
- Al pasar un módulo de la Etapa 2 a la Etapa 3 (`api` / `internal`, `package-info.java`).
- Al revisar dependencias, ciclos o accesos indebidos entre módulos.

## Entradas

Ninguna obligatoria. Si recibe `$ARGUMENTS`, se interpreta como el módulo o la duda concreta a la que aplicar estas normas.

## Resultado esperado

Normas, criterios, ejemplos breves y un checklist de límites de módulo. Ningún archivo se crea ni se modifica.

## Skills relacionadas

- `/k-back`: normas generales, tabla canónica de etapas (sección 2) y dependencias entre capas (sección 3).
- `/k-back-dto`: forma de los DTO `record` de contrato público y HTTP.
- `/k-back-service`: casos de uso y límite transaccional; la implementación de la facade es un elemento de esa capa.
- `/k-back-controller`: adaptador HTTP (`controller` en Etapa 2, `api.http` en Etapa 3).
- `/k-back-persistence`: entidades, DAO e implementaciones de Repository dentro del módulo.
- `/k-back-testing`: tests de arquitectura (ArchUnit, Spring Modulith `verify`).

## Normas

### 1. Qué es un módulo y cómo identificarlo

1.1. Un módulo es una unidad de negocio cohesiva dentro del monolito modular: agrupa un concepto de negocio, sus reglas, sus casos de uso, su persistencia y su API, y los demás módulos solo lo conocen por su contrato público.

1.2. Los módulos DEBEN identificarse con criterios funcionales, NO técnicos:

- Un concepto de negocio con identidad y ciclo de vida propios (p. ej. una factura se crea, se consulta y se elimina).
- Un conjunto de invariantes que deben cumplirse juntas (la factura y sus líneas, calculadas por `CalculadoraFactura`).
- Un vocabulario propio y un responsable funcional identificable.
- Datos de los que el módulo es el único propietario.

1.3. NO DEBEN crearse módulos por capa técnica (`utils`, `common`, `dtos`, `repositories`), por tabla ni por endpoint.

1.4. Un módulo nuevo DEBE estar justificado por requisitos. Si un requisito cabe en un módulo existente sin romper su cohesión, DEBE implementarse en él. NO DEBEN inventarse módulos.

1.5. Estado real de Facturación: un único módulo, `com.facturacion.factura`, en Etapa 2. Los datos del cliente (`clienteNombre`, `clienteNif`) son hoy atributos propios de la factura; NO existe un módulo `cliente` ni requisito que lo pida.

### 2. Estructura interna por etapa

2.1. La organización de paquetes de cada módulo DEBE seguir la tabla canónica de `/k-back` (sección 2). Esta skill no la redefine.

2.2. Etapa 2 (estado actual): el módulo vive en `<raiz>.<modulo>` con `api/dto`, `apln`, `controller`, `domain/{model,repository}` y `persistence/{dao,entity,mapper}`. Su contrato público es el paquete `<modulo>.api`.

2.3. Etapa 3: el módulo se divide en `api` (lo que el exterior puede ver) e `internal` (todo lo demás), con `package-info.java` en la raíz del módulo (sección 9).

2.4. Todos los módulos de una aplicación DEBEN estar en la misma etapa. Un módulo nuevo DEBE crearse en la etapa actual del proyecto; NUNCA se retrocede de etapa.

### 3. Límites y responsabilidades

3.1. Cada módulo DEBE ser el único propietario de su modelo de dominio, sus reglas, sus tablas y sus entidades de persistencia.

3.2. Un módulo NO DEBE leer ni escribir tablas de otro módulo, ni mediante DAO, entidades, SQL nativo ni relaciones JPA entre entidades de módulos distintos. Las referencias a otro módulo DEBEN hacerse por identificador y resolverse a través de su contrato público.

3.3. Un módulo NO DEBE acceder a controladores, casos de uso, servicios, repositorios, DAO, entidades, mappers ni modelos de dominio de otro módulo. Solo PUEDE usar su contrato público (sección 5).

3.4. Las excepciones de dominio de un módulo NO DEBEN importarse en otro módulo. El adaptador HTTP solo traduce las excepciones de su propio módulo (`/k-back-controller`).

3.5. El código de negocio NO DEBE moverse a un módulo «compartido» para evitar una dependencia; ver sección 7.

### 4. Dependencias permitidas entre módulos

4.1. Una dependencia entre módulos SOLO PUEDE ir desde la capa de aplicación (`apln` en Etapa 2, `internal.application` en Etapa 3) o desde `service` (solo Etapa 2) del módulo consumidor hacia el contrato público del módulo proveedor.

4.2. NO DEBEN depender de otro módulo: `domain`, `persistence`, `controller` / `api.http` ni los DTO HTTP.

4.3. Toda dependencia entre módulos DEBE estar justificada por un requisito y DEBE informarse. En Etapa 3 con Spring Modulith DEBE declararse en `allowedDependencies` (sección 9).

4.4. Las dependencias DEBEN formar un grafo dirigido acíclico. NO DEBEN existir ciclos entre módulos, ni directos (A → B → A) ni transitivos (A → B → C → A).

4.5. `ArquitecturaTest.modulosSinCiclos` (slices `com.facturacion.(*)..`) ya comprueba los ciclos entre paquetes de primer nivel; DEBE seguir pasando.

Ejemplo hipotético (no existe requisito): si se creara un módulo `cliente` y la creación de facturas necesitara validar el cliente, la dependencia permitida sería `factura.apln` (o `factura.internal.application`) → `cliente.api...ClienteFacade`, y `cliente` NO DEBERÍA depender de `factura` en ningún caso.

### 5. Contrato público y comunicación entre módulos

5.1. La comunicación entre módulos DEBE hacerse solo mediante el contrato público del proveedor: interfaces públicas de entrada y DTO `record` (forma de los DTO en `/k-back-dto`).

5.2. El contrato público NO DEBE exponer modelos de dominio, entidades de persistencia, excepciones internas ni tipos de Spring Data. Sus firmas DEBEN usar solo DTO del propio contrato y tipos Java estándar.

5.3. Etapa 2: el contrato público es `<modulo>.api`. Si otro módulo necesita operaciones de `factura`, el punto de entrada PUEDE ser una interfaz pública en `com.facturacion.factura.api` implementada en `factura.apln`, con DTO de `factura.api.dto`. Mientras ningún otro módulo lo necesite, NO DEBE crearse (antiabstracción, `/k-back` sección 13). Hoy no existe.

5.4. Los DTO HTTP de `api.dto` en Etapa 2 (`CrearFacturaRequest`, `LineaFacturaRequest`) NO DEBEN usarse como contrato entre módulos aunque compartan paquete; entre módulos se usan los DTO de caso de uso y salida (`NuevaFacturaDto`, `FacturaDto`).

### 6. API pública en Etapa 3

6.1. El contrato entre módulos DEBE ser una interfaz `<Modulo>Facade` en `<modulo>.api.facade`, con sus DTO `record` en `<modulo>.api.facade.dto`. Ejemplo real de destino: `com.facturacion.factura.api.facade.FacturaFacade` y `com.facturacion.factura.api.facade.dto.FacturaDto`.

6.2. La facade es una de las interfaces con única implementación permitidas (`/k-back` 13.2). Su implementación DEBE llamarse `<Modulo>FacadeImpl` y ubicarse en `<modulo>.internal.application` (p. ej. `com.facturacion.factura.internal.application.FacturaFacadeImpl`), DEBE delegar en los casos de uso o coordinarlos, y el límite transaccional es el de esa capa (`/k-back-service`).

6.3. La facade DEBE exponer solo las operaciones que otros módulos necesitan según los requisitos; NO DEBE replicar todos los casos de uso por defecto. En la Etapa 3 la facade también DEBE crearse cuando la especificación de arquitectura del usuario la exija, con las operaciones que esta enumere (o confirmadas por el usuario).

6.4. `<modulo>.api.http` (controlador, manejador de excepciones y `api.http.dto`) es el adaptador HTTP del módulo. NO es contrato entre módulos: ningún otro módulo DEBE depender de `api.http` ni de `api.http.dto`.

6.5. Ningún módulo DEBE acceder a `<otro>.internal..` bajo ningún concepto, ni siquiera para tipos «inofensivos» como enumerados o constantes. Si un tipo interno es necesario fuera, DEBE exponerse como DTO del contrato.

### 7. Dependencia mutua entre módulos

7.1. Si dos módulos necesitan dependerse mutuamente, NO DEBE resolverse accediendo a clases internas, duplicando modelos ni moviendo código a un paquete compartido. DEBE informarse como incompatibilidad de diseño, con el requisito que la provoca.

7.2. Opciones que PUEDEN proponerse en el informe (la decisión no se toma unilateralmente):

- Eventos de dominio: el módulo proveedor publica un evento (`record` de su contrato público: `api.dto` en Etapa 2, `api.facade.dto` en Etapa 3) mediante `ApplicationEventPublisher`, y el otro módulo lo escucha. El sentido de la dependencia queda en una sola dirección.
- Extraer módulo: el concepto que ambos necesitan pasa a un módulo nuevo del que dependen los dos, solo si es un concepto de negocio real (sección 1.2).
- Reasignar responsabilidad: la operación que provoca el ciclo pertenece en realidad a uno solo de los módulos.

### 8. Raíz de composición `app` e integración de un módulo nuevo

8.1. En Etapa 3, `<raiz>.app` es la raíz de composición: configuración transversal, beans técnicos y cableado. PUEDE referenciar el contrato público de cualquier módulo; ningún módulo DEBE depender de `app` salvo configuración genérica. En Etapas 1 y 2 su papel lo cumple `<raiz>.config` (`/k-back` sección 2).

8.2. El cableado de los componentes internos DEBE hacerse por el escaneo de componentes de `@SpringBootApplication`. `app` NO DEBE referenciar tipos de `internal` de otro módulo; con Spring Modulith esa referencia rompe `verify()` y, si fuera imprescindible, DEBE informarse como incompatibilidad.

8.3. Spring Modulith detecta `app` como un módulo más por ser subpaquete directo de `com.facturacion`; DEBE quedar fuera de cualquier ciclo.

8.4. Integración de un módulo nuevo en la aplicación:

- DEBE ubicarse como subpaquete directo de `<raiz>` (`com.facturacion.<modulo>`), de modo que `FacturacionApplication` lo cubra con el escaneo de componentes, de entidades JPA y de repositorios Spring Data por defecto. NO DEBEN añadirse `@ComponentScan`, `@EntityScan` ni `@EnableJpaRepositories` salvo necesidad justificada.
- Sus migraciones Flyway DEBEN ir en `src/main/resources/db/migration` continuando la numeración global `V<n>__...` (hoy existe `V1__crear_facturas.sql`); sus tablas son propiedad exclusiva del módulo.
- DEBE seguir la estructura de la etapa actual y, en Etapa 3, incluir su `package-info.java`.
- Sus dependencias hacia otros módulos DEBEN declararse e informarse (sección 4).
- Las reglas de `ArquitecturaTest` DEBEN seguir pasando; si el módulo exige reglas nuevas, su contenido es de `/k-back-testing`.

### 9. `package-info.java`

9.1. En Etapa 3 cada módulo DEBE tener `package-info.java` en su paquete raíz. Por la política de comentarios (`/k-back` sección 12) NO DEBE contener JavaDoc ni comentarios: solo anotaciones y la declaración `package`.

9.2. Mecanismo recomendado: Spring Modulith.

- Compatibilidad comprobada en Maven Central (`org.springframework.modulith`, 2026-11): la línea 1.4.x es la de Spring Boot 3.5 (1.4.14 se publica contra Spring Boot 3.5.16, Spring Framework 6.2.x y ArchUnit 1.4.2); la línea 2.x requiere Spring Boot 4 y NO DEBE usarse con el Spring Boot 3.5.6 del proyecto. Versión recomendada: la última 1.4.x (1.4.14 a fecha de redacción); DEBE revalidarse en Maven Central al añadirla.
- Dependencias necesarias: `spring-modulith-bom` importado en `dependencyManagement`, `spring-modulith-api` (o `spring-modulith-starter-core`) para las anotaciones del código principal y `spring-modulith-starter-test` en `test`. Modulith arrastra ArchUnit 1.4.2 y el `pom.xml` fija `archunit.version` 1.4.1: la alineación DEBE revisarse e informarse.
- Añadir Spring Modulith es una decisión de la Etapa 3 que DEBE informarse (`/k-back` 14.6).

9.3. Declaración del módulo en `<modulo>/package-info.java`, con `displayName` y `allowedDependencies` explícitos. Ejemplo real (el módulo `factura` no depende de ningún otro):

```java
@org.springframework.modulith.ApplicationModule(
        displayName = "Facturación",
        allowedDependencies = {})
package com.facturacion.factura;
```

Un `allowedDependencies` vacío y explícito significa «ninguna dependencia permitida»; omitir el atributo permite cualquier dependencia y NO DEBE hacerse.

9.4. Exposición del contrato con `@NamedInterface` en `api/facade/package-info.java` y en `api/facade/dto/package-info.java`. Para Modulith, todo subpaquete del módulo es interno salvo que se exponga como interfaz con nombre; por eso `api.http` y `internal` NO DEBEN anotarse.

```java
@org.springframework.modulith.NamedInterface("facade")
package com.facturacion.factura.api.facade;
```

```java
@org.springframework.modulith.NamedInterface("facade-dto")
package com.facturacion.factura.api.facade.dto;
```

9.5. Ejemplo hipotético (módulo `cliente` inexistente, sin requisito): si `factura` pasara a depender de `cliente`, su declaración sería `allowedDependencies = {"cliente :: facade", "cliente :: facade-dto"}`.

9.6. `app/package-info.java` PUEDE declararse con `@ApplicationModule`, con `displayName` y `allowedDependencies` explícito (sección 9.3); sus dependencias hacia contratos públicos se declaran igual que en cualquier módulo.

9.7. Verificación: `ApplicationModules.of(FacturacionApplication.class).verify()` comprueba ciclos, acceso a tipos no expuestos y dependencias no declaradas. Cómo y dónde se escribe ese test es de `/k-back-testing`.

9.8. Alternativa sin Spring Modulith (si no se aprueba la dependencia o resultara incompatible): `package-info.java` solo con la declaración `package <raiz>.<modulo>;`, sin anotaciones de framework ni comentarios, como marcador de la raíz del módulo; los límites se verifican con reglas ArchUnit equivalentes (ningún acceso a `..internal..` de otro módulo, solo `api.facade` como dependencia entre módulos, sin ciclos), definidas en `/k-back-testing`. NO DEBEN crearse anotaciones propias para sustituir a Modulith sin requisito.

## Comprobaciones antes de finalizar

- [ ] Cada módulo responde a un concepto de negocio con criterios funcionales y está justificado por requisitos.
- [ ] La estructura del módulo coincide con la etapa actual según la tabla canónica de `/k-back`; todos los módulos están en la misma etapa.
- [ ] Ningún módulo accede a clases de otro fuera de su contrato público (en Etapa 3, nada de `<otro>.internal..` ni `<otro>.api.http..`).
- [ ] Ningún módulo accede a tablas, entidades o DAO de otro; las referencias cruzadas son por identificador.
- [ ] El contrato público expone solo DTO `record` y tipos Java estándar; ni modelos de dominio, ni entidades, ni excepciones internas.
- [ ] Las dependencias entre módulos salen solo de la capa de aplicación o `service`, están justificadas e informadas.
- [ ] No hay ciclos directos ni transitivos; `ArquitecturaTest.modulosSinCiclos` pasa.
- [ ] Toda dependencia mutua se ha informado como incompatibilidad con opciones, sin resolverla con accesos internos.
- [ ] No se ha creado una facade ni un punto de entrada público sin un consumidor real ni requisito explícito.
- [ ] En Etapa 3: `package-info.java` en cada módulo, sin comentarios; `allowedDependencies` coincide con las dependencias reales; `@NamedInterface` solo en `api.facade` y `api.facade.dto`.
- [ ] Si se usa Spring Modulith: versión 1.4.x revalidada, dependencia informada y `verify()` ejecutado; si no, reglas ArchUnit equivalentes ejecutadas.
- [ ] Un módulo nuevo está bajo `com.facturacion`, sin escaneos adicionales injustificados y con migraciones Flyway en la numeración global.
