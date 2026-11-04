---
name: k-back-persistence
description: Normas de la capa de persistencia del backend Java/Spring Boot de Facturación - entidades JPA, DAO con Spring Data, implementaciones de Repository, mappers entidad-dominio, relaciones, consultas, transacciones en persistencia, migraciones Flyway, separación entidad/dominio y ubicación por etapa. Úsala antes de diseñar, implementar o revisar código de persistencia o migraciones. No modifica código.
argument-hint: "[tema o duda sobre persistencia]"
---

# /k-back-persistence

## Propósito

Definir cómo se implementa técnicamente el contrato Repository del dominio: entidades JPA, DAO de Spring Data, mappers, implementaciones de Repository, relaciones, consultas, transacciones en persistencia y migraciones Flyway. Es una skill de conocimiento: NO modifica código, NO ejecuta Git y NO define procedimientos de implementación.

El contrato que se implementa es propiedad de `/k-back-repository`; los modelos que se mapean, de `/k-back-domain`. Las normas generales y la tabla canónica de etapas son de `/k-back` y NO se repiten aquí.

## Cuándo usarla

- Al crear o modificar entidades JPA, DAO, mappers o implementaciones de Repository.
- Al añadir consultas, relaciones o cambios de esquema (migraciones Flyway).
- Al revisar que las entidades no salen de persistencia y que no hay reglas de negocio en esta capa.

## Entradas

Ninguna obligatoria. Si recibe `$ARGUMENTS`, se interpreta como el tema o la duda concreta a la que aplicar estas normas.

## Resultado esperado

Normas, criterios, ejemplos breves y checklist aplicables a la persistencia. Ningún archivo se crea ni se modifica.

## Skills relacionadas

- `/k-back`: tabla canónica de etapas, dependencias entre capas, seguridad (consultas parametrizadas) y verificación.
- `/k-back-domain`: modelos e invariantes que el mapper reconstruye.
- `/k-back-repository`: interfaz Repository que se implementa.
- `/k-back-service`: límite transaccional de los casos de uso.
- `/k-back-testing`: pruebas de mappers, DAO (`@DataJpaTest`) e integración.

## Normas

### 1. Stack y ubicación por etapa

1.1. La persistencia usa Spring Data JPA (Hibernate) sobre MariaDB, con esquema gestionado por Flyway (`flyway-core`, `flyway-mysql`), según `pom.xml`. NO DEBEN introducirse otras tecnologías de acceso a datos (JDBC directo, jOOQ, MyBatis), Lombok ni MapStruct sin requisito explícito (`/k-back` sección 14).

1.2. La ubicación DEBE seguir la tabla canónica de `/k-back`. Resumen para la persistencia:

| Elemento | Etapas 1 y 2 | Etapa 3 |
|---|---|---|
| Entidades JPA | `persistence.entity` | `internal.persistence.entity` |
| DAO Spring Data | `persistence.dao` | `internal.persistence.dao` |
| Mappers entidad ↔ dominio | `persistence.mapper` | `internal.persistence.mapper` |
| Implementaciones de Repository | raíz de `persistence` | `internal.persistence.repository` |

1.3. Ejemplo real (Etapa 2): `com.facturacion.factura.persistence.JpaFacturaRepository`, `...persistence.dao.FacturaDao`, `...persistence.entity.FacturaEntity` y `LineaFacturaEntity`, `...persistence.mapper.FacturaMapper`.

### 2. Separación entidad / dominio

2.1. Las entidades JPA y los modelos de dominio DEBEN ser clases distintas. NO DEBEN anotarse modelos de dominio con JPA ni usarse entidades como modelo.

2.2. Las entidades NO DEBEN salir de persistencia: no pueden aparecer en firmas, campos ni retornos de clases de otras capas. Los DAO y las entidades SOLO PUEDEN usarse desde la implementación de Repository y los mappers del propio módulo.

2.3. Ninguna otra capa DEBE referenciar persistencia salvo el cableado de Spring. Esto está automatizado en `ArquitecturaTest` (`entidadesSoloEnPersistencia`, `persistenciaNoUsadaFueraDePersistencia`) y DEBE seguir pasando.

2.4. Persistencia NO DEBE acceder a la persistencia de otros módulos.

### 3. Entidades JPA

3.1. Nombre `<Agregado>Entity` y una entidad por tabla. Anotaciones explícitas `@Entity`, `@Table(name = ...)` y `@Column(name = ..., nullable, length, precision, scale)` coherentes con la migración.

3.2. Identificador `Long` con `@GeneratedValue(strategy = GenerationType.IDENTITY)`, coherente con `AUTO_INCREMENT` en MariaDB.

3.3. Constructor sin argumentos `protected` (exigido por JPA) y un constructor público con los datos; solo getters, sin setters públicos. Ejemplo real: `FacturaEntity` y `LineaFacturaEntity`.

3.4. Las entidades NO DEBEN contener reglas de negocio ni validaciones de negocio; solo PUEDEN contener métodos técnicos de consistencia de la relación, como `FacturaEntity.agregarLinea`, que asigna la factura a la línea.

3.5. Los campos, tablas, columnas y relaciones NO DEBEN inventarse: DEBEN derivarse del modelo de dominio, los requisitos y el esquema existente. Una columna técnica PUEDE existir solo en la entidad cuando la persistencia lo requiera (p. ej. `linea_factura.posicion`, `factura_id`).

### 4. Relaciones

4.1. Las partes de un agregado DEBEN persistirse en cascada desde la entidad raíz. Ejemplo real: `FacturaEntity.lineas` es `@OneToMany(mappedBy = "factura", cascade = CascadeType.ALL, orphanRemoval = true)` con `@OrderBy("posicion ASC")`, y `LineaFacturaEntity.factura` es `@ManyToOne(fetch = FetchType.LAZY, optional = false)`.

4.2. Las asociaciones `@ManyToOne` DEBEN ser `LAZY`. Las colecciones se cargan de forma explícita cuando el caso de uso las necesita (sección 6).

4.3. El orden de colecciones que el dominio modela como lista DEBE preservarse con una columna de posición y `@OrderBy`.

4.4. NO DEBEN crearse relaciones entre entidades de módulos distintos; la referencia entre módulos es por identificador y a través del contrato público (`/k-back-module`).

### 5. DAO (Spring Data)

5.1. Nombre `<Agregado>Dao`, interfaz que extiende `JpaRepository<<Agregado>Entity, Long>`. Ejemplo real: `FacturaDao`.

5.2. Los DAO SOLO realizan acceso a datos y NO DEBEN contener reglas de negocio ni métodos `default` con lógica.

5.3. Se DEBEN preferir consultas derivadas del nombre (`existsByNumero`, `findAllByOrderByIdAsc`). `@Query` en JPQL SOLO DEBE usarse cuando una consulta derivada no baste, siempre con parámetros enlazados; NO DEBE concatenarse entrada en la consulta. SQL nativo solo con justificación informada.

5.4. Los métodos del DAO DEBEN existir solo si los usa una implementación de Repository.

### 6. Consultas y carga

6.1. `spring.jpa.open-in-view` está desactivado en `application.yml`. Toda asociación que el mapper recorra DEBE cargarse dentro de la consulta, con `@EntityGraph` o `JOIN FETCH`. Ejemplo real:

```java
@EntityGraph(attributePaths = "lineas")
Optional<FacturaEntity> findWithLineasById(Long id);
```

6.2. Se DEBEN evitar consultas N+1: los listados que mapean colecciones DEBEN cargarlas en la misma consulta (`findAllByOrderByIdAsc` con `@EntityGraph`).

6.3. Las comprobaciones de existencia DEBEN usar consultas `exists...` en lugar de cargar entidades.

### 7. Mappers

7.1. Nombre `<Agregado>Mapper`, `@Component` en `<raiz>.persistence.mapper` (Etapa 1) / `<raiz>.<modulo>.persistence.mapper` (Etapa 2) / `<raiz>.<modulo>.internal.persistence.mapper` (Etapa 3), conversión manual con métodos `aEntidad(modelo)` y `aDominio(entidad)`. Ejemplo real: `FacturaMapper`.

7.2. Un mapper por agregado: convierte la raíz y sus partes (`FacturaMapper` convierte también las líneas). NO DEBEN crearse mappers separados para partes internas sin necesidad.

7.3. `aDominio` DEBE reconstruir el modelo mediante su constructor, de modo que las invariantes del dominio se comprueben; NO DEBE saltarse ni reimplementar invariantes.

7.4. El mapper NO DEBE contener reglas de negocio ni cálculos (importes, totales): solo traslada datos. Los valores técnicos (p. ej. `posicion` a partir del índice de la lista) sí son responsabilidad del mapper.

### 8. Implementaciones de Repository

8.1. Nombre `<Tecnología><Agregado>Repository`, anotada con `@Repository`, que implementa la interfaz del dominio e inyecta DAO y mapper por constructor. Ejemplo real:

```java
@Repository
public class JpaFacturaRepository implements FacturaRepository {
    public Factura guardar(Factura factura) {
        return mapper.aDominio(dao.save(mapper.aEntidad(factura)));
    }
}
```

8.2. Solo las implementaciones de Repository PUEDEN usar DAO. DEBEN devolver únicamente modelos de dominio y tipos Java.

8.3. NO DEBEN contener reglas de negocio: solo traducen operaciones del contrato a llamadas al DAO y convierten con el mapper.

8.4. Las excepciones técnicas NO DEBEN silenciarse. Si un requisito exige traducir un error técnico (p. ej. violación de `uk_factura_numero`) a una excepción de dominio, la traducción DEBE hacerse aquí y DEBE informarse.

### 9. Transacciones en persistencia

9.1. El límite transaccional es el caso de uso (`/k-back-service`); las operaciones del Repository participan en esa transacción.

9.2. Las implementaciones de Repository, DAO, mappers y entidades NO DEBEN declarar `@Transactional` para delimitar casos de uso. Se apoyan en la transaccionalidad por defecto de los métodos de Spring Data y en la del caso de uso. Ejemplo real: `JpaFacturaRepository` no declara transacciones.

9.3. NO DEBEN usarse `EntityManager` ni `flush` manual salvo necesidad técnica informada.

### 10. Migraciones Flyway

10.1. El esquema DEBE gestionarse solo con migraciones Flyway en `src/main/resources/db/migration`, con nombre `V<n>__<descripcion>.sql` y numeración consecutiva. Ejemplo real: `V1__crear_facturas.sql`.

10.2. `spring.jpa.hibernate.ddl-auto` es `validate`: las entidades DEBEN coincidir con el esquema y todo cambio de entidad que afecte al esquema DEBE ir acompañado de su migración. NO DEBE cambiarse `ddl-auto` a `update` ni `create`.

10.3. Una migración ya publicada NO DEBE modificarse; los cambios DEBEN hacerse con una migración nueva.

10.4. El SQL DEBE ser compatible con MariaDB y nombrar explícitamente las restricciones, siguiendo el estilo existente: `pk_<tabla>`, `uk_<tabla>_<columna>`, `fk_<tabla>_<referenciada>`.

10.5. Las restricciones de integridad del esquema (`NOT NULL`, `UNIQUE`, `FOREIGN KEY ... ON DELETE CASCADE`) complementan pero NO sustituyen a las invariantes del dominio.

10.6. NO DEBEN incluirse datos de prueba ni secretos en las migraciones.

## Comprobaciones antes de finalizar

- [ ] Cada clase está en el paquete de persistencia que corresponde a la etapa detectada.
- [ ] Ninguna entidad, DAO ni mapper se referencia fuera de persistencia del propio módulo.
- [ ] Las entidades no tienen reglas de negocio, tienen constructor `protected` sin argumentos y no tienen setters públicos.
- [ ] Columnas y relaciones coinciden con las migraciones y no hay campos inventados.
- [ ] Las colecciones que se mapean se cargan con `@EntityGraph` o `JOIN FETCH`, sin N+1.
- [ ] Las consultas son derivadas o JPQL parametrizado; no hay concatenación de entrada.
- [ ] El mapper reconstruye el dominio por su constructor y no calcula ni valida reglas de negocio.
- [ ] La implementación de Repository cumple la interfaz completa y devuelve solo tipos de dominio y Java.
- [ ] No hay `@Transactional` de caso de uso en persistencia.
- [ ] Los cambios de esquema tienen migración nueva `V<n>__...sql`; ninguna migración publicada se ha modificado.
- [ ] No se han añadido dependencias sin requisito; las pruebas siguen `/k-back-testing` y `ArquitecturaTest` pasa.
