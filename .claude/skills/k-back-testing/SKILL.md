---
name: k-back-testing
description: Normas de testing y calidad del backend Java/Spring Boot de Facturación - JUnit 5, Mockito, AssertJ, convenciones de nombres y ubicación de tests, qué probar en cada capa (dominio, persistencia, aplicación, controller, DTO, módulos), @WebMvcTest, @DataJpaTest, @SpringBootTest, ArchUnit y Spring Modulith verify por etapa, casos positivos/negativos/límite, comandos de verificación y comprobaciones del paso "Verifica" de cada iteración. Úsala al escribir, adaptar o revisar tests y al verificar cualquier cambio de código backend. No modifica código.
argument-hint: "[capa o tema de testing, opcional]"
---

# /k-back-testing

## Propósito

Fijar qué se prueba en cada capa del backend, con qué herramientas, cómo se nombran y ubican los tests, qué pruebas de arquitectura corresponden a cada etapa y qué comprobaciones componen la verificación de un cambio. Es una skill de conocimiento: NO modifica código, NO ejecuta Git y NO define procedimientos de iteración. Las skills ejecutoras `do-code-*` aplican estas normas.

Las normas generales (stack, modelo de etapas, dependencias entre capas, política de comentarios, verificación de alto nivel) son propiedad de `/k-back` y NO se repiten aquí. Los términos DEBE, NO DEBE y PUEDE son normativos.

## Cuándo usarla

- Al escribir tests nuevos para una clase o cambio de comportamiento.
- Al adaptar tests existentes en un refactor de paquetes o un cambio de etapa.
- Al revisar si un cambio está suficientemente probado.
- En el paso "Verifica" de cada iteración de una skill `do-code-*` (sección "Verificaciones por iteración").

## Entradas

Ninguna obligatoria. Si recibe `$ARGUMENTS`, se interpreta como la capa (`domain`, `persistence`, `apln`, `controller`, `dto`, `module`) o el tema de testing al que aplicar estas normas.

## Resultado esperado

Normas, criterios y checklists aplicables. Ningún archivo se crea ni se modifica.

## Skills relacionadas

- `/k-back`: normas generales, modelo de etapas (tabla canónica de paquetes), verificación de alto nivel y política de comentarios.
- `/k-back-domain`: invariantes y servicios de dominio que se prueban según la sección 3.1.
- `/k-back-repository` y `/k-back-persistence`: contratos Repository, mappers, entidades y DAO que se prueban según la sección 3.2.
- `/k-back-service`: casos de uso que se prueban según la sección 3.3.
- `/k-back-controller`: endpoints, códigos y `ProblemDetail` que se prueban según la sección 3.4.
- `/k-back-dto`: restricciones Bean Validation que se prueban según la sección 3.5.
- `/k-back-module`: límites de módulo, facade y `package-info.java`; mecanismo de Spring Modulith (sección 5).

## Normas

### 1. Estado actual de los tests en Facturación

1.1. Herramientas disponibles (ver `pom.xml`): `spring-boot-starter-test` (JUnit 5 Jupiter incluido `junit-jupiter-params`, Mockito, AssertJ, Spring Test/MockMvc, Hamcrest, JSONPath) y `archunit-junit5` 1.4.1. NO hay H2, Testcontainers, Spring Modulith ni plugin Failsafe.

1.2. Tests existentes (51, todos en verde con `./mvnw -B test`):

| Test | Tipo | Capa |
|---|---|---|
| `com.facturacion.ArquitecturaTest` | ArchUnit, 6 reglas | Arquitectura |
| `factura.domain.model.FacturaTest` | Unitario puro | Dominio (invariantes) |
| `factura.domain.model.CalculadoraFacturaTest` | Unitario puro | Dominio (servicio puro) |
| `factura.persistence.mapper.FacturaMapperTest` | Unitario puro | Persistencia (mapper) |
| `factura.apln.CasosDeUsoFacturaTest` + `FacturaRepositoryEnMemoria` | Unitario con fake de Repository | Aplicación |
| `factura.controller.FacturaControllerTest` | `@WebMvcTest` + `@MockitoBean` | Controller y validación de DTO HTTP |

1.3. NO existen tests de persistencia contra base de datos (`@DataJpaTest`) ni de integración (`@SpringBootTest`). La aplicación usa MariaDB (`compose.yaml`), Flyway (`V1__crear_facturas.sql`) y `ddl-auto: validate`, por lo que cualquier test que arranque JPA necesita una MariaDB accesible. `JpaFacturaRepository` y `FacturaDao` no tienen test automatizado; es una carencia conocida que DEBE informarse si una tarea toca esas clases.

1.4. `src/test/resources` no existe. NO DEBE crearse configuración de test (`application-test.yml`, perfiles) sin un test que la necesite.

### 2. Convenciones de tests

2.1. Ubicación espejo: cada test DEBE estar en `src/test/java` en el mismo paquete que la clase que prueba (`FacturaMapper` → `src/test/java/com/facturacion/factura/persistence/mapper/FacturaMapperTest.java`). Los tests de arquitectura van en el paquete raíz (`com.facturacion`).

2.2. Nombre de clase: `<ClaseProbada>Test`. Excepción existente: los casos de uso de un mismo agregado PUEDEN agruparse en `CasosDeUso<Agregado>Test` (p. ej. `CasosDeUsoFacturaTest`). Solo los sufijos `Test`/`Tests` se ejecutan con la configuración actual de Surefire; NO DEBEN crearse tests `*IT` mientras no exista Failsafe, porque no se ejecutarían.

2.3. Nombre de método: frase en español en `camelCase` que describe el comportamiento esperado, sin prefijo `test` ni `@DisplayName` (p. ej. `rechazaNumeroDuplicado`, `facturaInexistenteDevuelve404`, `aceptaValoresEnElLimiteDeSusColumnas`).

2.4. Clases y métodos de test DEBEN ser package-private (sin `public`), como en los tests existentes.

2.5. Estructura preparar / actuar / comprobar separada por líneas en blanco, sin comentarios. El código de test sigue la política de comentarios de `/k-back`.

2.6. Datos de prueba mediante métodos factoría `private static` en la propia clase (`nueva(...)`, `factura(...)`, `linea(...)`) y bloques de texto para JSON. NO DEBEN crearse utilidades o builders de test compartidos sin que al menos dos clases de test los usen.

2.7. Aserciones con AssertJ (`assertThat`, `assertThatThrownBy`, `extracting`, `singleElement`, `satisfies`). Los `BigDecimal` DEBEN compararse con `isEqualByComparingTo`, salvo cuando la escala forme parte del requisito (entonces se comprueba `scale()` o `toPlainString()`). En `MockMvc` se usan los `ResultMatcher` (`status()`, `header()`, `jsonPath()`).

2.8. Dobles de prueba:
- Aplicación: DEBE preferirse un fake en memoria que implemente la interfaz Repository del dominio, package-private y en el paquete del test (convención `FacturaRepositoryEnMemoria`). Mockito PUEDE usarse cuando haya que verificar una interacción no observable por estado.
- Controller: los casos de uso DEBEN sustituirse con `@MockitoBean` (`org.springframework.test.context.bean.override.mockito`). NO DEBE usarse `@MockBean`, obsoleto en Spring Boot 3.4+.
- NO DEBEN simularse modelos de dominio, objetos de valor, DTO ni la clase bajo prueba.

2.9. Los tests DEBEN ser deterministas e independientes: fechas fijas (`LocalDate.of(2026, 11, 3)`), sin dependencia del orden de ejecución, sin `Thread.sleep` ni red.

### 3. Qué probar en cada capa

#### 3.1. Dominio (`domain.model`, servicios de dominio puros)

- Tests unitarios puros: sin Spring, sin Mockito, instanciando directamente el modelo.
- Invariantes: un test por regla con el caso que la viola (excepción de dominio esperada con `assertThatThrownBy(...).isInstanceOf(...)`) y el caso válido.
- Inmutabilidad y copias defensivas (p. ej. `copiaLasLineasDeFormaInmutable`).
- Servicios puros (p. ej. `CalculadoraFactura`): resultados, redondeos, escalas y valores frontera (cero, decimales, máximo).

#### 3.2. Repository y persistencia

- Interfaces Repository: no se prueban por sí mismas; su contrato se ejercita a través del fake en aplicación y de los tests de la implementación.
- Mappers: test unitario puro de ida y vuelta (`aEntidad` → `aDominio` igual al original), modelo nuevo sin id y conservación del orden y de todos los campos.
- Implementaciones Repository y DAO: `@DataJpaTest` con `@AutoConfigureTestDatabase(replace = Replace.NONE)` contra MariaDB, o `@SpringBootTest` de integración. Hoy NO son ejecutables sin una base de datos (sección 1.3). Para habilitarlos hace falta añadir dependencias (p. ej. Testcontainers MariaDB con `spring-boot-testcontainers` y `@ServiceConnection`), decisión que DEBE informarse según `/k-back`. H2 NO DEBE usarse como sustituto si las migraciones o consultas dependen de sintaxis de MariaDB. Qué cubrir cuando existan: guardar y recuperar con líneas, unicidad de `numero`, borrado en cascada, consultas derivadas y validación del esquema Flyway frente a las entidades.

#### 3.3. Aplicación (`apln` / `internal.application`)

- Casos de uso instanciados con su constructor y el fake de Repository, sin contexto Spring.
- Entrada y salida en DTO; comprobar el resultado completo (incluidos totales calculados) y el estado persistido en el fake.
- Errores: excepciones de dominio propagadas (no encontrada, duplicado, inválida) y que en caso de error no se guarda nada.
- Orquestación: que solo se afecta lo indicado (p. ej. `eliminarSoloBorraLaFacturaIndicada`).
- El comportamiento transaccional real solo se verifica en tests de integración.

#### 3.4. Controller (`controller` / `api.http`)

- `@WebMvcTest(<Agregado>Controller.class)` con `MockMvc` y casos de uso como `@MockitoBean`.
- Por endpoint: verbo y ruta, código de éxito (`201` con `Location`, `200`, `204`), cuerpo JSON relevante y traslado exacto de la petición al caso de uso (`verify(casoDeUso).ejecutar(dtoEsperado)`).
- Errores: cada excepción de dominio traducida a su código con `ProblemDetail` (comprobar `$.detail` cuando el mensaje sea parte del contrato); `400` por cuerpo mal formado, ausente, tipos incorrectos en la ruta (`/api/facturas/abc`) y validación fallida.
- En los rechazos por validación DEBE comprobarse que el caso de uso no se invoca (`verify(..., never())`).

#### 3.5. DTO (Bean Validation)

- Las restricciones de los DTO HTTP DEBEN probarse a través de `@WebMvcTest` (convención existente): cada restricción con un valor que la viola, un valor en el límite exacto aceptado y campos obligatorios ausentes.
- Cuando un DTO se valide fuera de HTTP (p. ej. contrato público de módulo), PUEDE probarse con `jakarta.validation.Validator` en un test unitario.
- Los límites DEBEN coincidir con el esquema real (p. ej. `numero` `VARCHAR(50)`: 50 aceptado, 51 rechazado).

#### 3.6. Módulos y arquitectura

- Reglas de capas y límites con ArchUnit en `ArquitecturaTest` (sección 4) y, en Etapa 3 si se adopta, Spring Modulith (sección 5).
- Contratos públicos (facade de Etapa 3): se prueban como casos de uso, a través de su interfaz pública.

#### 3.7. Integración (`@SpringBootTest`)

- Solo cuando el valor no se obtiene con tests de capa: cableado completo, transacciones reales, migraciones Flyway, flujo HTTP extremo a extremo de un caso crítico.
- Requiere base de datos (sección 1.3); NO DEBE añadirse un `@SpringBootTest` que falle en la ejecución normal de `./mvnw test` por falta de MariaDB.
- Pocos y centrados en flujos; NO DEBEN replicar los casos ya cubiertos por tests unitarios o de capa.

### 4. Tests de arquitectura por etapa (ArchUnit)

4.1. `ArquitecturaTest` usa `@AnalyzeClasses(packages = "com.facturacion", importOptions = ImportOption.DoNotIncludeTests.class)` y reglas `@ArchTest static final ArchRule`, con `allowEmptyShould(true)`. Toda regla nueva DEBE seguir ese estilo y nombrarse en español describiendo la restricción.

4.2. Reglas existentes (Etapas 1 y 2), que DEBEN mantenerse:

| Regla | Restricción |
|---|---|
| `dominioIndependienteDeFrameworks` | `..domain..` no depende de Spring, JPA, Hibernate, `..persistence..`, `..controller..`, `..apln..` |
| `entidadesSoloEnPersistencia` | `@Entity` solo en `..persistence.entity..` |
| `persistenciaNoUsadaFueraDePersistencia` | nada fuera de `..persistence..` depende de `..persistence..` |
| `controladoresSinRepositoryNiServiceInterno` | `..controller..` no depende de `..domain.repository..` ni `..service..` |
| `aplicacionSinControladores` | `..apln..` no depende de `..controller..` |
| `modulosSinCiclos` | slices `com.facturacion.(*)..` sin ciclos |

4.3. Etapa 3: al reubicar paquetes según la tabla canónica de `/k-back`, `ArquitecturaTest` DEBE adaptarse a los nuevos nombres sin perder ninguna restricción: dominio en `..internal.domain..` sin Spring/JPA, `@Entity` en `..internal.persistence.entity..`, `..api.http..` sin dependencias de `..internal.domain.repository..` ni `..internal.persistence..`, `..apln..` sustituido por `..internal.application..`, y además prohibido el acceso a `internal` de otro módulo y ningún módulo dependiendo de `<raiz>.app`. Ejemplo breve:

```java
@ArchTest
static final ArchRule internalDeFacturaNoAccesibleDesdeOtrosModulos = noClasses()
        .that().resideOutsideOfPackages("com.facturacion.factura..", "com.facturacion.app..")
        .should().dependOnClassesThat().resideInAPackage("com.facturacion.factura.internal..")
        .allowEmptyShould(true);
```

4.4. Una regla ArchUnit NO DEBE eliminarse, relajarse ni llenarse de excepciones para que el código pase; si el código la incumple, se corrige el código o se informa la incompatibilidad.

### 5. Spring Modulith (Etapa 3, opcional)

5.1. Solo si `/k-back-module` adopta Spring Modulith (decisión de Etapa 3 que DEBE informarse, con versión compatible con Spring Boot 3.5), DEBE existir un test de verificación de módulos en el paquete raíz:

```java
class ModulosTest {

    @Test
    void verificaLosLimitesDeLosModulos() {
        ApplicationModules.of(FacturacionApplication.class).verify();
    }
}
```

5.2. Spring Modulith complementa a ArchUnit, no lo sustituye: las reglas de capas internas de la sección 4 se mantienen.

### 6. Cobertura de casos

6.1. Todo cambio de comportamiento DEBE incluir al menos un caso positivo y los casos negativos de cada regla o validación afectada.

6.2. DEBEN cubrirse los límites: valor máximo aceptado y primer valor rechazado, cero, vacío, nulo, colecciones vacías o con elementos nulos, precisión decimal y redondeo.

6.3. Un error corregido DEBE ir acompañado de un test que falle sin la corrección.

6.4. NO se exige un porcentaje de cobertura (no hay JaCoCo); el criterio es que cada regla, validación, código HTTP y rama de error tenga un test.

### 7. Comandos de verificación

| Objetivo | Comando (Git Bash / Linux) | Windows sin shell POSIX |
|---|---|---|
| Compilar código de aplicación | `./mvnw -B -q compile` | `mvnw.cmd -B -q compile` |
| Compilar aplicación y tests | `./mvnw -B -q test-compile` | `mvnw.cmd -B -q test-compile` |
| Todos los tests | `./mvnw -B test` | `mvnw.cmd -B test` |
| Ciclo completo (tests + empaquetado) | `./mvnw -B verify` | `mvnw.cmd -B verify` |
| Una clase de test | `./mvnw -B test -Dtest=FacturaControllerTest` | igual con `mvnw.cmd` |
| Un método | `./mvnw -B test -Dtest='CasosDeUsoFacturaTest#rechazaNumeroDuplicado'` | igual con `mvnw.cmd` |
| Varias clases | `./mvnw -B test -Dtest='FacturaTest,CalculadoraFacturaTest'` | igual con `mvnw.cmd` |
| Solo arquitectura | `./mvnw -B test -Dtest=ArquitecturaTest` | igual con `mvnw.cmd` |

7.1. Los informes de Surefire quedan en `target/surefire-reports/`; ante un fallo DEBE leerse el informe y citarse la salida real.

7.2. Los `WARN` de `DefaultHandlerExceptionResolver` en la salida de `FacturaControllerTest` son esperados (peticiones inválidas a propósito) y no indican fallo. El aviso de carga dinámica del agente de Mockito/Byte Buddy tampoco.

### 8. Criterios de tests válidos

8.1. NO DEBEN crearse tests vacíos, sin aserciones o que solo comprueben que no se lanza excepción cuando el requisito exige un resultado.

8.2. NO DEBEN desactivarse tests (`@Disabled`, borrado, `-DskipTests`, `-Dmaven.test.skip`, `assumeTrue` artificial) para que la build pase.

8.3. NO DEBEN debilitarse aserciones existentes (cambiar valores esperados sin requisito, sustituir comprobaciones exactas por genéricas, eliminar `verify(..., never())`) ni relajar reglas ArchUnit.

8.4. Los tests existentes DEBEN preservarse. En refactors de paquetes o cambios de etapa se mueven al nuevo paquete espejo y se adaptan imports y nombres de paquete en reglas, manteniendo todos sus casos y aserciones. El número de tests tras el refactor NO DEBE ser menor que antes salvo justificación informada.

8.5. Un test solo puede modificarse en su comportamiento esperado cuando el requisito de la tarea cambia ese comportamiento, y DEBE informarse.

8.6. Los tests DEBEN pasar en la ejecución normal (`./mvnw -B test`) sin servicios externos, salvo los de integración habilitados explícitamente con su infraestructura de test (sección 3.2).

## Verificaciones por iteración

Esta sección define QUÉ compone el paso "Verifica" de cada una de las 10 iteraciones de las skills ejecutoras; el procedimiento de iteración (orden, número, delegación, Git) lo definen las `do-code-*`. Cada comprobación DEBE registrarse con su resultado real (correcto, fallo con salida, o no aplicable con motivo).

| Id | Comprobación | Criterio de éxito |
|---|---|---|
| V1 | Compilación: `./mvnw -B -q compile` y `./mvnw -B -q test-compile` | Sin errores |
| V2 | Tests rápidos afectados: `-Dtest=` con las clases de test de los paquetes tocados | Todos en verde |
| V3 | Arquitectura: `./mvnw -B test -Dtest=ArquitecturaTest` (y `ModulosTest` si existe) | Todas las reglas en verde, ninguna relajada |
| V4 | Revisión de reglas: el cambio cumple `/k-back` (etapa, dependencias, nombres, inyección, antiabstracción) y la skill de cada capa tocada (`/k-back-domain`, `/k-back-repository`, `/k-back-persistence`, `/k-back-service`, `/k-back-controller`, `/k-back-dto`, `/k-back-module`) | Sin incumplimientos o incumplimientos informados |
| V5 | Tests del cambio: los casos positivos, negativos y límite exigidos por las secciones 3 y 6 existen y cumplen la sección 8 | Sin carencias, o carencias informadas |
| V6 | Comentarios: búsqueda de `//`, `/*` y `/**` en los `.java` añadidos o modificados (comando abajo), revisando a mano los falsos positivos como URLs en cadenas | Ningún comentario nuevo |
| V7 | `git diff` revisado: solo archivos de la tarea, sin código ajeno reescrito, sin temporales, secretos ni configuración local, sin tests eliminados ni debilitados | Diff limpio |

Búsqueda de V6, donde `<base>` es la rama o commit de partida de la tarea:

```bash
git diff --name-only --diff-filter=AM <base> -- '*.java' | xargs -r grep -nE '//|/\*'
```

En la última iteración, o antes de dar por terminada la tarea, DEBE ejecutarse además la suite completa (`./mvnw -B verify` o `./mvnw -B test`).

## Comprobaciones antes de finalizar

- [ ] Cada clase de producción tocada tiene su test en el paquete espejo con nombre `<Clase>Test`.
- [ ] Las herramientas usadas por capa coinciden con la sección 3 (dominio puro, fake de Repository en aplicación, `@WebMvcTest` + `@MockitoBean` en controller).
- [ ] Hay casos positivos, negativos y límite para cada regla, validación y código HTTP afectados.
- [ ] Ningún test se ha desactivado, borrado ni debilitado; el número de tests no ha disminuido sin justificación.
- [ ] `ArquitecturaTest` está adaptado a la etapa actual y ninguna regla se ha relajado.
- [ ] No se han añadido dependencias de test (Testcontainers, H2, Spring Modulith) sin informarlo.
- [ ] Las verificaciones V1-V7 se han ejecutado y su resultado real está informado.
- [ ] La suite completa se ha ejecutado al final con su resultado real.
