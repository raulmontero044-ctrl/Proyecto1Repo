---
name: k-back-domain
description: Normas del modelo de dominio del backend Java/Spring Boot de Facturación - modelos y objetos de valor, invariantes, reglas de negocio, servicios de dominio puros (p. ej. CalculadoraFactura), excepciones de dominio, independencia de Spring/JPA/BD y ubicación por etapa. Úsala antes de diseñar, implementar o revisar clases de dominio. No modifica código.
argument-hint: "[tema o duda sobre el dominio]"
---

# /k-back-domain

## Propósito

Definir cómo se modela el dominio de negocio: modelos, objetos de valor, invariantes, reglas de negocio, servicios de dominio y excepciones de dominio, y su independencia total de frameworks e infraestructura. Es una skill de conocimiento: NO modifica código, NO ejecuta Git y NO define procedimientos de implementación.

Complementa a `/k-back`, cuyas normas generales (nombres, errores, antiabstracción, comentarios, verificación) y tabla canónica de etapas NO se repiten aquí.

## Cuándo usarla

- Al crear o modificar modelos, objetos de valor, invariantes o reglas de negocio.
- Al decidir si una regla pertenece al dominio, a un caso de uso (`/k-back-service`) o a la validación de entrada (`/k-back-dto`).
- Al crear servicios de dominio o excepciones de dominio.
- Al revisar que el dominio no dependa de Spring, JPA ni de otras capas.

## Entradas

Ninguna obligatoria. Si recibe `$ARGUMENTS`, se interpreta como el tema o la duda concreta a la que aplicar estas normas.

## Resultado esperado

Normas, criterios, ejemplos breves y checklist aplicables al dominio. Ningún archivo se crea ni se modifica.

## Skills relacionadas

- `/k-back`: normas generales, tabla canónica de etapas y dependencias entre capas.
- `/k-back-repository`: interfaces Repository, que también residen en el dominio.
- `/k-back-persistence`: conversión entre entidades JPA y modelos de dominio.
- `/k-back-service`: casos de uso que orquestan el dominio, transacciones y conversión DTO ↔ dominio.
- `/k-back-dto`: validación de entrada con Bean Validation (complementaria a las invariantes).
- `/k-back-controller`: traducción de excepciones de dominio a HTTP.
- `/k-back-testing`: qué y cómo probar en el dominio.

## Normas

### 1. Ubicación por etapa

1.1. La ubicación DEBE seguir la tabla canónica de `/k-back` (sección 2). Resumen para el dominio:

| Elemento | Etapas 1 y 2 | Etapa 3 |
|---|---|---|
| Modelos y objetos de valor | `domain.model` | `internal.domain.model` |
| Servicios de dominio puros | `domain.model` | `internal.domain.service` |
| Excepciones de dominio | `domain.model` | `internal.domain.exception` |
| Interfaces Repository | `domain.repository` | `internal.domain.repository` |

1.2. En Facturación (Etapa 2) todo el dominio está en `com.facturacion.factura.domain.model` (`Factura`, `LineaFactura`, `CalculadoraFactura` y las tres excepciones) y `com.facturacion.factura.domain.repository` (`FacturaRepository`). NO DEBEN crearse subpaquetes `service` o `exception` dentro de `domain` antes de la Etapa 3.

1.3. Los servicios que necesiten Spring o infraestructura NO son servicios de dominio: van en `service` (Etapas 1 y 2) o en `internal.application` (Etapa 3) y son propiedad de `/k-back-service`.

### 2. Independencia del dominio

2.1. El dominio NO DEBE importar ni depender de:

- Spring Boot ni Spring Framework, incluidas anotaciones como `@Component`, `@Service`, `@Transactional` o `@Autowired`.
- Spring Data (`JpaRepository`, `Pageable`, `Page`, `Sort`, etc.).
- JPA / Jakarta Persistence ni Hibernate.
- Jackson, Bean Validation (`jakarta.validation`) ni tipos HTTP o de serialización web.
- Clases de `controller`, `apln`, `service`, `persistence`, `config` (o sus equivalentes de Etapa 3) ni de otros módulos.

2.2. El dominio PUEDE contener: clases Java planas, `record`, enumerados, objetos de valor, reglas de negocio, excepciones propias, servicios de dominio puros e interfaces Repository.

2.3. El dominio NO DEBE conocer detalles de base de datos: nombres de tabla o columna, claves foráneas, posiciones de ordenación técnicas ni estrategias de generación de identificadores. Ejemplo real: `LineaFactura` no tiene `id` ni `posicion`; el orden de las líneas es el de la lista y la columna `posicion` solo existe en `LineaFacturaEntity`.

2.4. Esta independencia está automatizada por la regla `dominioIndependienteDeFrameworks` de `ArquitecturaTest`; DEBE seguir pasando.

### 3. Modelos y objetos de valor

3.1. Los modelos DEBEN ser inmutables. Se DEBE preferir `record`, como `Factura` y `LineaFactura`.

3.2. Las colecciones DEBEN copiarse de forma inmutable en el constructor (`lineas = List.copyOf(lineas)` en `Factura`).

3.3. Los importes y porcentajes DEBEN ser `BigDecimal`; las fechas, tipos de `java.time` (`LocalDate`).

3.4. Un modelo PUEDE ofrecer métodos de fábrica con intención de negocio. Ejemplo real: `Factura.nueva(...)` crea una factura sin identificador (`id` nulo) antes de persistirse.

3.5. Los campos del modelo DEBEN derivarse de los requisitos o del código existente; NO DEBEN inventarse. Los campos actuales de `Factura` son `id`, `numero`, `fecha`, `clienteNombre`, `clienteNif`, `porcentajeIva` y `lineas`; los de `LineaFactura`, `concepto`, `cantidad` y `precioUnitario`.

3.6. Los valores derivados (importe, base, cuota de IVA, total) NO DEBEN almacenarse en el modelo si pueden calcularse; se calculan en el dominio (sección 5).

3.7. Un objeto de valor nuevo SOLO DEBE crearse cuando encapsule una invariante o un comportamiento reutilizado; NO DEBE envolverse un tipo primitivo sin regla que lo justifique.

### 4. Invariantes y reglas de negocio

4.1. Las invariantes DEBEN residir en el modelo de dominio y comprobarse al construirlo, de modo que no pueda existir un objeto inválido. En un `record` se comprueban en el constructor compacto.

4.2. Ejemplo real abreviado:

```java
public record LineaFactura(String concepto, BigDecimal cantidad, BigDecimal precioUnitario) {
    public LineaFactura {
        if (cantidad == null || cantidad.signum() <= 0) {
            throw new FacturaInvalidaException("La cantidad de la línea debe ser mayor que cero");
        }
    }
}
```

4.3. Las invariantes NO DEBEN duplicarse en controladores, DAO, entidades ni mappers. La validación Bean Validation de los DTO (`/k-back-dto`) es complementaria y NO sustituye a las invariantes.

4.4. Las reglas que requieren consultar el estado persistido (p. ej. número de factura único) NO pertenecen al modelo: las orquesta el caso de uso con el Repository (`CrearFactura` usa `existePorNumero` y lanza `NumeroFacturaDuplicadoException`); ver `/k-back-service`. El dominio aporta la excepción.

4.5. Las reglas NO DEBEN inventarse: cada invariante DEBE corresponder a un requisito o al código existente.

### 5. Servicios de dominio

5.1. Un servicio de dominio contiene lógica de negocio que no pertenece de forma natural a un único modelo o que se reutiliza entre varios. DEBE ser Java puro, sin estado mutable y sin dependencias de framework.

5.2. Ejemplo real: `CalculadoraFactura` es una clase `final` con constructor privado y métodos estáticos (`importe`, `base`, `cuotaIva`, `total`) que calculan con `BigDecimal`, escala 2 y `RoundingMode.HALF_UP`. Es la única fuente de esos cálculos; NO DEBEN reimplementarse en otras capas.

5.3. Un servicio de dominio NO DEBE acceder a Repository ni a otros puertos de infraestructura; si necesita datos persistidos, la coordinación es un caso de uso (`/k-back-service`).

5.4. NO DEBEN crearse servicios de dominio que solo deleguen ni interfaces para ellos (ver `/k-back` sección 13).

### 6. Excepciones de dominio

6.1. Las violaciones de reglas de negocio DEBEN expresarse con excepciones de dominio propias que extiendan `RuntimeException`, con mensaje significativo en español.

6.2. Excepciones existentes que DEBEN reutilizarse antes de crear otras:

| Excepción | Uso |
|---|---|
| `FacturaInvalidaException(String mensaje)` | Invariante incumplida al construir `Factura` o `LineaFactura` |
| `FacturaNoEncontradaException(Long id)` | La factura solicitada no existe |
| `NumeroFacturaDuplicadoException(String numero)` | Ya existe una factura con ese número |

6.3. Una excepción nueva SOLO DEBE crearse si representa una situación de negocio distinta que el llamante necesite distinguir; NO DEBEN crearse jerarquías sin uso.

6.4. Los mensajes NO DEBEN incluir datos sensibles (p. ej. el NIF del cliente).

6.5. Las excepciones de dominio NO DEBEN contener códigos HTTP ni anotaciones web. Solo el controlador del propio módulo PUEDE importarlas para traducirlas a HTTP (ver `/k-back-controller`); otros módulos NO DEBEN usarlas.

## Comprobaciones antes de finalizar

- [ ] Cada clase de dominio está en el paquete que corresponde a la etapa detectada (sección 1 y tabla de `/k-back`).
- [ ] El dominio no importa `org.springframework`, `jakarta.persistence`, `jakarta.validation`, `org.hibernate`, Jackson ni clases de otras capas o módulos.
- [ ] No hay detalles de base de datos (columnas, posiciones técnicas, claves) en el modelo.
- [ ] Los modelos son inmutables, preferentemente `record`, con colecciones copiadas.
- [ ] Las invariantes se comprueban en la construcción y no están duplicadas en otras capas.
- [ ] Los cálculos de negocio están en el dominio (`CalculadoraFactura`) y no se reimplementan fuera.
- [ ] Se reutilizan las excepciones existentes; las nuevas están justificadas y sin datos sensibles.
- [ ] No hay campos, reglas, servicios ni excepciones sin requisito que los justifique.
- [ ] `ArquitecturaTest` sigue cubriendo y pasando la independencia del dominio; las pruebas siguen `/k-back-testing`.
