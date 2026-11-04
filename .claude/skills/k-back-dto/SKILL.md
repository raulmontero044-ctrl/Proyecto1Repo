---
name: k-back-dto
description: Normas de los DTO del backend Java/Spring Boot de Facturación - DTO record de entrada y salida, DTO de contrato público entre módulos frente a DTO HTTP, Bean Validation en DTO de entrada, mapeo DTO (FacturaDtoConverter y conversión en el controlador), prohibición de exponer entidades JPA y modelos de dominio, y ubicación por etapa. Úsala antes de crear, modificar o revisar cualquier DTO o su conversión. No modifica código.
argument-hint: "[tema o duda concreta sobre DTO]"
---

# /k-back-dto

## Propósito

Fijar las normas de los DTO: forma, tipos, validación, clasificación (contrato público frente a HTTP), mapeo y ubicación por etapa. Es una skill de conocimiento: NO modifica código, NO ejecuta Git y NO define procedimientos de implementación. Las normas generales y la tabla de paquetes son de `/k-back`.

Los términos DEBE, NO DEBE y PUEDE son normativos.

## Cuándo usarla

- Antes de crear, modificar o revisar un DTO de petición, de respuesta o de contrato entre módulos.
- Para decidir dónde se valida, dónde se convierte y en qué paquete va un DTO.

## Entradas

Ninguna obligatoria. Si recibe `$ARGUMENTS`, se interpreta como el tema o la duda concreta a la que aplicar estas normas.

## Resultado esperado

Normas, criterios, ejemplos breves y checklist de DTO. Ningún archivo se crea ni se modifica.

## Skills relacionadas

- `/k-back`: tabla canónica de paquetes, convenciones de nombres y validación en dos niveles.
- `/k-back-service`: uso de los DTO en las firmas de los casos de uso y de la facade.
- `/k-back-controller`: uso de los DTO HTTP y activación de la validación con `@Valid`.
- `/k-back-domain`: invariantes que la validación de entrada no sustituye.
- `/k-back-persistence`: mappers entidad ↔ dominio (no son DTO).
- `/k-back-module`: qué forma parte del contrato público entre módulos.
- `/k-back-testing`: pruebas de validación y de conversión.

## Normas

### 1. Forma

1.1. Todo DTO DEBE ser un `record` Java inmutable, sin lógica de negocio, sin métodos de cálculo y sin estado mutable.

1.2. Un DTO SOLO PUEDE depender de tipos Java estándar (`String`, `Long`, `BigDecimal`, `LocalDate`, `List`), de otros DTO y de anotaciones `jakarta.validation`. NO DEBE referenciar modelos de dominio, entidades JPA, tipos de Spring ni tipos de persistencia.

1.3. Los importes y porcentajes DEBEN ser `BigDecimal`; las fechas, `LocalDate`; las colecciones, `List` de DTO (`List<LineaFacturaDto>`).

1.4. Los campos DEBEN corresponderse con los requisitos o con el modelo y el esquema existentes. NO DEBEN inventarse campos ni crearse DTO sin uso.

### 2. Clasificación

2.1. **DTO de entrada a caso de uso** (`Nuevo/Nueva<Agregado>Dto`): datos que recibe la capa de aplicación. Ejemplos: `NuevaFacturaDto`, `NuevaLineaFacturaDto`. NO llevan Bean Validation, porque pueden llegar de llamadas no HTTP; la defensa está en el conversor y en el dominio.

2.2. **DTO de salida** (`<Agregado>Dto`): resultado de un caso de uso, que PUEDE incluir valores calculados por el dominio. Ejemplos: `FacturaDto` (con `base`, `cuotaIva`, `total`) y `LineaFacturaDto` (con `importe`).

2.3. **DTO de petición HTTP** (`<Accion><Agregado>Request`): forma del cuerpo HTTP, con Bean Validation. Ejemplos: `CrearFacturaRequest`, `LineaFacturaRequest`. Son exclusivamente HTTP: NO DEBEN usarse en firmas de casos de uso ni en contratos entre módulos.

2.4. Los DTO de entrada y salida de los casos de uso forman el contrato público del módulo con otros módulos; los DTO HTTP son contrato externo con los clientes REST. Ambos contratos DEBEN mantenerse separados aunque compartan campos, para que cambiar uno no obligue a cambiar el otro.

### 3. Bean Validation en DTO de entrada HTTP

3.1. Los DTO de petición HTTP DEBEN declarar las restricciones de entrada con `jakarta.validation` (`spring-boot-starter-validation` ya está en `pom.xml`); se activan con `@Valid` en el controlador (`/k-back-controller`).

3.2. Los límites DEBEN derivarse del esquema real (`V1__crear_facturas.sql`) o de los requisitos, no inventarse:

| Columna | Restricción en el DTO |
|---|---|
| `numero VARCHAR(50)` | `@NotBlank @Size(max = 50)` |
| `cliente_nombre VARCHAR(200)` | `@NotBlank @Size(max = 200)` |
| `cliente_nif VARCHAR(20)` | `@NotBlank @Size(max = 20)` |
| `porcentaje_iva DECIMAL(5, 2)` | `@NotNull @DecimalMin("0") @Digits(integer = 3, fraction = 2)` |
| `concepto VARCHAR(500)` | `@NotBlank @Size(max = 500)` |
| `cantidad DECIMAL(12, 3)` | `@NotNull @DecimalMin(value = "0", inclusive = false) @Digits(integer = 9, fraction = 3)` |
| `precio_unitario DECIMAL(12, 2)` | `@NotNull @DecimalMin("0") @Digits(integer = 10, fraction = 2)` |

3.3. Las colecciones anidadas DEBEN validarse en cascada y rechazar elementos nulos: `@NotEmpty List<@NotNull @Valid LineaFacturaRequest> lineas`.

3.4. La Bean Validation solo cubre forma y límites técnicos; las reglas de negocio DEBEN residir en el dominio (`/k-back-domain`).

### 4. Mapeo

4.1. HTTP → DTO de aplicación: DEBE hacerse en el controlador, como el método privado estático `FacturaController.aNuevaFactura(CrearFacturaRequest)`, copiando campos sin lógica de negocio.

4.2. DTO de aplicación ↔ dominio: DEBE hacerse en la capa de aplicación con un conversor del módulo, como `FacturaDtoConverter` (`final`, package-private, constructor privado y métodos estáticos `aDominio(NuevaFacturaDto)` y `aDto(Factura)`). Los valores calculados se obtienen del dominio (`CalculadoraFactura.importe`, `base`, `cuotaIva`, `total`), no se recalculan en el conversor.

4.3. El conversor `aDominio` PUEDE rechazar entradas estructuralmente imposibles (líneas nulas) con la excepción de dominio del módulo (`FacturaInvalidaException`); el resto de invariantes las aplica el constructor o la factoría del dominio (`Factura.nueva`).

4.4. Entidad ↔ dominio NO es mapeo DTO: es de `persistence/mapper` (`FacturaMapper`, `/k-back-persistence`). Un DTO NUNCA DEBE convertirse directamente desde o hacia una entidad JPA.

4.5. NO DEBEN introducirse librerías de mapeo (MapStruct, ModelMapper) sin requisito; si se añaden, DEBE informarse (`/k-back`).

### 5. Prohibición de exposición

5.1. Las respuestas HTTP y los contratos entre módulos NO DEBEN exponer modelos de dominio (`Factura`, `LineaFactura`) ni entidades JPA (`FacturaEntity`, `LineaFacturaEntity`), ni siquiera como campo anidado de un DTO.

5.2. Los DTO NO DEBEN incluir datos internos que no formen parte del contrato (claves técnicas como `posicion` o `factura_id` de `linea_factura`).

### 6. Ubicación por etapa

6.1. Etapas 1 y 2: según la convención existente, todos los DTO del módulo (contrato e HTTP) DEBEN ubicarse en `<modulo>.api.dto` (hoy `com.facturacion.factura.api.dto`). En la Etapa 1 sin módulos, los DTO HTTP van en `controller/dto` según `/k-back`.

6.2. Etapa 3: los DTO de contrato entre módulos (`FacturaDto`, `LineaFacturaDto`, `NuevaFacturaDto`, `NuevaLineaFacturaDto`) DEBEN ubicarse en `<modulo>.api.facade.dto`; los DTO HTTP (`CrearFacturaRequest`, `LineaFacturaRequest` y los de respuesta HTTP) en `<modulo>.api.http.dto`.

6.3. En la Etapa 3 los casos de uso y la facade DEBEN recibir y devolver DTO de `api.facade.dto`; `api.http.dto` NO DEBE usarse fuera de `api.http`. El controlador convierte entre ambos.

6.4. En la Etapa 3 la respuesta HTTP DEBE ser un `record` propio de `api.http.dto` con nombre `<Agregado>Response` (p. ej. `FacturaResponse`, `LineaFacturaResponse`), con los mismos campos que el DTO de salida actual salvo requisito, para no duplicar nombres simples entre `api.facade.dto` y `api.http.dto` y preservar el JSON existente.

## Comprobaciones antes de finalizar

- [ ] Todos los DTO son `record` sin lógica y solo usan tipos Java estándar, otros DTO y anotaciones de validación.
- [ ] Cada DTO está clasificado (entrada de caso de uso, salida, petición HTTP) y en el paquete de su etapa.
- [ ] Los DTO HTTP tienen Bean Validation con límites tomados del esquema o de los requisitos; los DTO de aplicación no la tienen.
- [ ] El mapeo HTTP → aplicación está en el controlador y el mapeo aplicación ↔ dominio en el conversor de la capa de aplicación.
- [ ] Ningún DTO contiene ni se convierte directamente desde modelos de dominio expuestos o entidades JPA.
- [ ] No hay campos, DTO ni librerías de mapeo inventados o sin uso.
- [ ] El JSON de los endpoints existentes se mantiene.
