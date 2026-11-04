---
name: k-back-repository
description: Normas de las interfaces Repository del backend Java/Spring Boot de Facturación - contrato de persistencia definido desde el dominio, operaciones CRUD y consultas específicas, firmas con modelos de dominio y tipos Java estándar, sin JPA ni Spring Data, y solo operaciones justificadas por requisitos. Úsala antes de diseñar, ampliar o revisar una interfaz Repository. No modifica código.
argument-hint: "[tema o duda sobre interfaces Repository]"
---

# /k-back-repository

## Propósito

Definir cómo se declaran las interfaces Repository: el contrato de persistencia que el dominio necesita, expresado solo con tipos del dominio y de Java. Es una skill de conocimiento: NO modifica código, NO ejecuta Git y NO define procedimientos de implementación.

La implementación técnica de estas interfaces (DAO, entidades, mappers) es propiedad de `/k-back-persistence`. Las normas generales y la tabla canónica de etapas son de `/k-back` y NO se repiten aquí.

## Cuándo usarla

- Al crear una interfaz Repository para un agregado nuevo.
- Al añadir, cambiar o eliminar operaciones de una interfaz Repository existente.
- Al revisar que un caso de uso accede a datos solo mediante contratos del dominio.

## Entradas

Ninguna obligatoria. Si recibe `$ARGUMENTS`, se interpreta como el tema o la duda concreta a la que aplicar estas normas.

## Resultado esperado

Normas, criterios, ejemplos breves y checklist aplicables a las interfaces Repository. Ningún archivo se crea ni se modifica.

## Skills relacionadas

- `/k-back`: tabla canónica de etapas, dependencias entre capas y criterios antiabstracción.
- `/k-back-domain`: modelos de dominio que el Repository recibe y devuelve.
- `/k-back-persistence`: implementación técnica del contrato (`JpaFacturaRepository`, `FacturaDao`, `FacturaMapper`).
- `/k-back-service`: casos de uso que consumen el Repository y fijan el límite transaccional.
- `/k-back-testing`: dobles de prueba del Repository y pruebas de su implementación.

## Normas

### 1. Ubicación y nombre

1.1. Las interfaces Repository DEBEN declararse en el dominio del propio módulo según la tabla de `/k-back`: `<modulo>.domain.repository` en Etapas 1 y 2, `<modulo>.internal.domain.repository` en Etapa 3. Ejemplo real: `com.facturacion.factura.domain.repository.FacturaRepository`.

1.2. El nombre DEBE ser `<Agregado>Repository`. Habrá una interfaz por agregado raíz; NO DEBEN crearse Repository para partes internas de un agregado. Ejemplo real: no existe `LineaFacturaRepository`; las líneas se guardan y recuperan siempre con su `Factura`.

1.3. Es la única interfaz con una sola implementación permitida de forma general (`/k-back` sección 13); existe para invertir la dependencia entre aplicación y persistencia.

### 2. Responsabilidad

2.1. Un Repository expresa lo que el dominio necesita de la persistencia, con el vocabulario del negocio, como si fuera una colección de agregados. NO DEBE reflejar la API de la tecnología subyacente.

2.2. Un Repository NO DEBE contener reglas de negocio ni decidir el flujo del caso de uso. Ejemplo real: `existePorNumero` solo informa; la decisión de lanzar `NumeroFacturaDuplicadoException` la toma `CrearFactura` (`/k-back-service`).

2.3. Un Repository NO DEBE declarar transacciones ni conocer el límite transaccional; ver `/k-back-service`.

### 3. Firmas

3.1. Las firmas DEBEN usar únicamente modelos de dominio del propio módulo y tipos estándar de Java (`Long`, `String`, `boolean`, `List`, `Optional`, `java.time`, `BigDecimal`).

3.2. Las firmas NO DEBEN usar entidades JPA, DTO, tipos de Spring Data (`Pageable`, `Page`, `Sort`, `Specification`, `Example`) ni tipos de JPA o Hibernate.

3.3. La interfaz NO DEBE extender `JpaRepository`, `CrudRepository` ni ninguna interfaz de framework, ni llevar anotaciones de Spring o JPA.

3.4. Las búsquedas que pueden no encontrar resultado DEBEN devolver `Optional<Agregado>`, nunca `null`. Las colecciones DEBEN devolverse como `List` (vacía si no hay resultados).

3.5. Las operaciones de guardado DEBEN devolver el agregado persistido, de modo que el llamante obtenga el identificador asignado.

3.6. Los nombres de las operaciones DEBEN estar en español y expresar intención de negocio, siguiendo el código existente.

### 4. Operaciones CRUD y consultas específicas

4.1. Contrato real actual:

```java
public interface FacturaRepository {
    Factura guardar(Factura factura);
    Optional<Factura> buscarPorId(Long id);
    List<Factura> listar();
    void eliminar(Long id);
    boolean existePorNumero(String numero);
}
```

4.2. Convenciones de nombres que DEBEN reproducirse:

| Operación | Forma | Ejemplo real |
|---|---|---|
| Alta o modificación | `guardar(Agregado)` → `Agregado` | `guardar(Factura)` |
| Búsqueda por identificador | `buscarPorId(id)` → `Optional<Agregado>` | `buscarPorId(Long)` |
| Listado completo | `listar()` → `List<Agregado>` | `listar()` |
| Borrado | `eliminar(id)` → `void` | `eliminar(Long)` |
| Existencia por criterio | `existePor<Criterio>(valor)` → `boolean` | `existePorNumero(String)` |
| Búsqueda por criterio | `buscarPor<Criterio>(valor)` → `Optional` o `List` | (solo si un requisito lo exige) |

4.3. Las consultas específicas DEBEN expresarse con parámetros del dominio y DEBEN tener un nombre de negocio; los detalles de cómo se ejecutan (consultas derivadas, `@Query`, `@EntityGraph`) pertenecen a `/k-back-persistence`.

4.4. Si un requisito exige paginación u ordenación, DEBE modelarse con tipos propios del dominio o parámetros Java simples, nunca con `Pageable` o `Sort`. El orden por defecto del listado es responsabilidad de la implementación y DEBE documentarse en el informe si se cambia (hoy `listar()` devuelve por `id` ascendente).

### 5. Operaciones justificadas

5.1. Cada operación DEBE estar justificada por un caso de uso real o un requisito. NO DEBEN añadirse operaciones "por si acaso" ni un CRUD completo por defecto.

5.2. Antes de añadir una operación DEBE comprobarse si una existente la cubre.

5.3. NO DEBEN eliminarse ni renombrarse operaciones en uso; si una deja de usarse por la tarea, DEBE informarse.

5.4. Toda operación nueva exige su implementación en persistencia (`/k-back-persistence`) y actualizar los dobles de prueba existentes, como `FacturaRepositoryEnMemoria` en los tests (`/k-back-testing`).

## Comprobaciones antes de finalizar

- [ ] La interfaz está en el paquete `domain.repository` (o `internal.domain.repository` en Etapa 3) del propio módulo.
- [ ] Hay una interfaz por agregado raíz, con nombre `<Agregado>Repository`.
- [ ] Las firmas solo usan modelos de dominio del módulo y tipos Java estándar.
- [ ] No hay imports de `org.springframework`, `jakarta.persistence`, `org.hibernate` ni de `persistence`.
- [ ] No extiende interfaces de framework ni lleva anotaciones.
- [ ] Las búsquedas devuelven `Optional` o `List`, nunca `null`.
- [ ] Cada operación está justificada por un requisito o caso de uso y no duplica otra.
- [ ] Toda operación tiene implementación en persistencia y en los dobles de prueba.
