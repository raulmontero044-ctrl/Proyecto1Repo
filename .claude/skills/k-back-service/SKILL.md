---
name: k-back-service
description: Normas de la capa de aplicación del backend Java/Spring Boot de Facturación - casos de uso (apln o internal.application), orquestación, límite transaccional, permisos, uso de DTO en firmas, acceso a datos mediante interfaces Repository del dominio, servicios internos justificados e implementación de la facade en la Etapa 3. Úsala antes de diseñar, implementar o revisar casos de uso o servicios de aplicación. No modifica código.
argument-hint: "[tema o duda concreta sobre la capa de aplicación]"
---

# /k-back-service

## Propósito

Fijar las normas de la capa de aplicación: casos de uso, orquestación, transacciones, permisos, DTO en las firmas y servicios internos. Es una skill de conocimiento: NO modifica código, NO ejecuta Git y NO define procedimientos de implementación. Las normas generales, el modelo de etapas y las dependencias entre capas son de `/k-back` y aquí no se repiten.

Los términos DEBE, NO DEBE y PUEDE son normativos.

## Cuándo usarla

- Antes de crear, modificar o revisar un caso de uso o un servicio de aplicación.
- Para decidir dónde va el límite transaccional o una comprobación de permisos.
- Para implementar la facade pública de un módulo en la Etapa 3.

## Entradas

Ninguna obligatoria. Si recibe `$ARGUMENTS`, se interpreta como el tema o la duda concreta a la que aplicar estas normas.

## Resultado esperado

Normas, criterios, ejemplos breves y checklist de la capa de aplicación. Ningún archivo se crea ni se modifica.

## Skills relacionadas

- `/k-back`: normas generales, tabla canónica de paquetes por etapa, dependencias entre capas, nombres e inyección.
- `/k-back-domain`: modelo, invariantes, servicios de dominio puros y excepciones de dominio que los casos de uso usan.
- `/k-back-repository`: interfaces Repository en las que se apoyan los casos de uso.
- `/k-back-persistence`: implementaciones de Repository y transacciones en persistencia.
- `/k-back-dto`: forma de los DTO de entrada y salida de los casos de uso y su conversión.
- `/k-back-controller`: adaptador HTTP que invoca los casos de uso.
- `/k-back-module`: contrato de la facade entre módulos, límites y ciclos.
- `/k-back-testing`: qué y cómo probar los casos de uso.

## Normas

### 1. Ubicación

1.1. Los casos de uso DEBEN ubicarse en la capa de aplicación de la etapa vigente según la tabla de `/k-back`: `<raiz>.apln` (Etapa 1) / `<raiz>.<modulo>.apln` (Etapa 2, hoy `com.facturacion.factura.apln`) y `<raiz>.<modulo>.internal.application` (Etapa 3).

1.2. El paquete `service` (Etapas 1 y 2) SOLO PUEDE existir si hay un servicio interno justificado (sección 7). Hoy no existe en `factura` y NO DEBE crearse vacío.

### 2. Casos de uso

2.1. Cada caso de uso DEBE ser una clase propia con una única operación de negocio, nombre verbo + agregado y un único método público `ejecutar`. Ejemplos reales: `CrearFactura.ejecutar(NuevaFacturaDto)`, `ObtenerFactura.ejecutar(Long)`, `ListarFacturas.ejecutar()`, `EliminarFactura.ejecutar(Long)`.

2.2. Los casos de uso DEBEN anotarse con `@Service`, recibir sus dependencias por constructor (`private final FacturaRepository repository`) y NO DEBEN tener una interfaz propia (antiabstracción de `/k-back`).

2.3. Un caso de uso DEBE orquestar: convertir la entrada a dominio, aplicar comprobaciones que requieren datos persistidos, delegar las reglas en el modelo de dominio, persistir mediante Repository y convertir el resultado a DTO.

```java
@Transactional
public FacturaDto ejecutar(NuevaFacturaDto nuevaFactura) {
    Factura factura = FacturaDtoConverter.aDominio(nuevaFactura);
    if (repository.existePorNumero(factura.numero())) {
        throw new NumeroFacturaDuplicadoException(factura.numero());
    }
    return FacturaDtoConverter.aDto(repository.guardar(factura));
}
```

2.4. Las invariantes del agregado (líneas obligatorias, importes, IVA) DEBEN residir en el dominio (`/k-back-domain`). El caso de uso NO DEBE recalcular ni duplicar reglas que ya garantiza el modelo; los cálculos se piden al dominio (`CalculadoraFactura`).

2.5. Las comprobaciones que dependen del estado persistido (unicidad del número, existencia antes de eliminar) PUEDEN hacerse en el caso de uso y DEBEN señalizarse con excepciones de dominio del propio módulo (`NumeroFacturaDuplicadoException`, `FacturaNoEncontradaException`), nunca con códigos HTTP ni `null`.

2.6. Un caso de uso NO DEBE invocar a otro caso de uso para encadenar lógica; si dos casos de uso comparten lógica que no es de dominio, se aplica la sección 7.

### 3. Firmas y DTO

3.1. Los casos de uso invocados por controladores o por otros módulos DEBEN recibir y devolver DTO `record` o tipos Java estándar (`Long`, `List<FacturaDto>`, `void`). NO DEBEN exponer modelos de dominio ni entidades de persistencia.

3.2. Los casos de uso NO DEBEN recibir ni devolver DTO exclusivamente HTTP (`CrearFacturaRequest`, `LineaFacturaRequest`); la conversión HTTP → DTO de aplicación es del controlador (`/k-back-controller`).

3.3. La conversión DTO ↔ dominio DEBE hacerse dentro de la capa de aplicación, en un conversor del propio paquete como `FacturaDtoConverter` (clase `final`, package-private, métodos estáticos `aDominio` y `aDto`). Su forma y reglas son de `/k-back-dto`.

3.4. La conversión a DTO DEBE completarse dentro de la transacción, porque `spring.jpa.open-in-view` es `false` en `application.yml`.

### 4. Acceso a datos

4.1. La capa de aplicación DEBE acceder a los datos solo mediante interfaces de `domain/repository` (p. ej. `FacturaRepository.existePorNumero`, `guardar`, `buscarPorId`, `listar`, `eliminar`).

4.2. NO DEBE importar nada de `persistence` (DAO, entidades, mappers, `JpaFacturaRepository`) ni tipos de Spring Data (`Pageable`, `Page`, `Sort`).

4.3. Si falta una operación Repository, DEBE informarse como bloqueo para `/k-back-repository`; NO DEBE resolverse accediendo a DAO.

### 5. Transacciones

5.1. El límite transaccional DEBE situarse en el método público del caso de uso (o de la implementación de la facade, sección 8) con `org.springframework.transaction.annotation.Transactional`.

5.2. Las operaciones de escritura DEBEN usar `@Transactional` (`CrearFactura`, `EliminarFactura`) y las de solo lectura `@Transactional(readOnly = true)` (`ObtenerFactura`, `ListarFacturas`).

5.3. Las transacciones NO DEBEN declararse en controladores ni en el dominio. Las de la propia persistencia son de `/k-back-persistence`.

5.4. Las excepciones de dominio son no comprobadas y provocan el rollback por defecto; NO DEBEN capturarse en el caso de uso para ocultarlas.

### 6. Permisos

6.1. Hoy no hay requisitos ni dependencias de seguridad (no hay Spring Security en `pom.xml`). NO DEBEN añadirse comprobaciones de permisos, roles ni dependencias de seguridad sin requisito explícito.

6.2. Cuando exista ese requisito, la autorización de negocio DEBE comprobarse en el caso de uso antes de modificar estado, expresarse con una excepción del propio módulo y traducirse a HTTP 403 en el manejador del módulo (`/k-back-controller`). La incorporación de la dependencia DEBE informarse (`/k-back`).

### 7. Servicios internos

7.1. Un servicio interno SOLO PUEDE crearse cuando varios casos de uso compartan lógica de aplicación que no pertenece al modelo de dominio o cuando requiera infraestructura o Spring. Si es lógica pura de negocio, va al dominio (`/k-back-domain`), como `CalculadoraFactura`.

7.2. NO DEBEN crearse servicios que solo deleguen en un Repository o en otro caso de uso.

7.3. Un servicio interno DEBE ubicarse en `<raiz>.service` (Etapa 1) / `<raiz>.<modulo>.service` (Etapa 2) o en `<raiz>.<modulo>.internal.application` (Etapa 3) y NO DEBE ser accesible desde otros módulos.

### 8. Implementación de la facade (Etapa 3)

8.1. En la Etapa 3 la interfaz `<Modulo>Facade` de `api.facade` (contrato definido por `/k-back-module`) DEBE implementarse en `<modulo>.internal.application` con el nombre `<Modulo>FacadeImpl` (p. ej. `FacturaFacadeImpl`). La facade es solo para otros módulos: el controlador del propio módulo delega en los casos de uso, no en ella (`/k-back-controller`).

8.2. La implementación DEBE recibir y devolver solo DTO de `api.facade.dto` o tipos Java estándar y DEBE apoyarse en los casos de uso del módulo; NO DEBE duplicar su lógica.

8.3. Si un método de la facade solo invoca un caso de uso, la transacción la marca el caso de uso; si coordina varios, el método de la facade DEBE ser el límite transaccional.

8.4. En las Etapas 1 y 2 NO DEBE crearse una facade. Si otro módulo necesita operaciones, el punto de entrada es una interfaz pública en `<modulo>.api` implementada en `apln`, solo con consumidor real (`/k-back-module` sección 5.3); otro módulo NO DEBE invocar directamente los casos de uso de `apln`.

8.5. Para usar otro módulo, la capa de aplicación DEBE depender solo de su contrato público (`api` en la Etapa 2, `api.facade` en la Etapa 3); los límites son de `/k-back-module`.

## Comprobaciones antes de finalizar

- [ ] Cada caso de uso está en la capa de aplicación de la etapa vigente, con nombre verbo + agregado y método `ejecutar`.
- [ ] La inyección es por constructor y depende de interfaces Repository, no de `persistence`.
- [ ] Las firmas públicas solo usan DTO `record` o tipos Java estándar; ningún modelo de dominio, entidad ni DTO HTTP.
- [ ] La conversión DTO ↔ dominio se hace en la capa de aplicación y dentro de la transacción.
- [ ] `@Transactional` está en el caso de uso (o facade), con `readOnly = true` en lecturas, y no en controladores ni dominio.
- [ ] Las reglas de negocio están en el dominio y no se duplican; los errores son excepciones de dominio del módulo.
- [ ] No se han añadido permisos, seguridad, servicios ni facades sin requisito.
- [ ] No se accede a clases internas de otros módulos.
