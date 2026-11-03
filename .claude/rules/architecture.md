# Reglas arquitectónicas del backend

Reglas normativas compartidas por todas las Skills backend (Java + Spring Boot). Los términos DEBE, NO DEBE y PUEDE son obligatorios en el sentido literal. Las secciones están numeradas de forma estable para poder referenciarlas como `architecture.md §N`.

Marcadores usados en este documento:

- `<paquete-raiz>`: paquete Java raíz real de la aplicación. DEBE obtenerse del código existente; NO DEBE inventarse.
- `<modulo>`: nombre real de un módulo de negocio. DEBE obtenerse de los requisitos o del código existente.

## 1. Estilo arquitectónico

1.1. El backend es un **monolito modular**: una única aplicación desplegable compuesta por módulos de negocio.

1.2. Cada módulo DEBE encapsular sus responsabilidades y exponer al resto de módulos únicamente sus contratos públicos (§6).

1.3. Cada módulo DEBE ubicarse en `<paquete-raiz>.<modulo>`.

## 2. Estructura de referencia por módulo

```text
<modulo>/
├── api/
│   └── dto/
├── apln/
├── config/
├── controller/
├── domain/
│   ├── model/
│   └── repository/
├── persistence/
│   ├── dao/
│   ├── entity/
│   └── mapper/
└── service/
```

2.1. El nombre de la capa de aplicación DEBE ser `apln`. NO DEBE renombrarse a `application`, `app`, `usecase` ni similares.

2.2. La configuración transversal de toda la aplicación DEBE ubicarse en el paquete global `<paquete-raiz>.config`.

2.3. El paquete `<modulo>.config` SOLO PUEDE contener configuración específica de ese módulo cuando esté justificada. NO DEBE contener configuración transversal.

2.4. La estructura es de referencia, no una plantilla obligatoria: solo DEBEN existir los paquetes que contengan clases necesarias (§9).

## 3. Responsabilidades de cada paquete

| Paquete | Responsabilidad | NO DEBE contener |
|---|---|---|
| `api/dto` | Contratos públicos de datos del módulo, especialmente para comunicación entre módulos | Lógica de negocio, entidades JPA, modelos de dominio |
| `apln` | Casos de uso y orquestación de operaciones de aplicación; límite transaccional | Lógica HTTP, acceso directo a DAO o entidades JPA |
| `config` | Configuración Spring específica del módulo (§2.3) | Configuración transversal, lógica de negocio |
| `controller` | Adaptadores HTTP: endpoints, validación de entrada, códigos de estado, transformación de respuestas | Reglas de negocio, acceso a Repository, DAO o entidades |
| `domain/model` | Modelos, objetos de valor, invariantes, reglas de dominio y excepciones de dominio | Dependencias de framework o infraestructura (§4) |
| `domain/repository` | Interfaces Repository definidas desde las necesidades del dominio | Implementaciones, tipos de Spring Data o JPA |
| `persistence/dao` | Acceso técnico a datos | Reglas de negocio |
| `persistence/entity` | Entidades de persistencia (p. ej. entidades JPA) | Reglas de negocio, uso fuera de `persistence` |
| `persistence/mapper` | Conversión entre modelos de dominio y entidades de persistencia | Reglas de negocio |
| `persistence` (raíz) | Implementaciones técnicas de las interfaces Repository | Reglas de negocio |
| `service` | Servicios de dominio o servicios internos de aplicación con responsabilidad diferenciada y justificada | Lógica HTTP, servicios que solo delegan |

3.1. Los servicios de dominio sin dependencias de framework PUEDEN ubicarse en `domain`. Los servicios que requieren infraestructura o Spring DEBEN ubicarse en `service`.

## 4. Independencia del dominio

4.1. `domain` (incluidos `domain/model` y `domain/repository`) NO DEBE importar ni depender de:

- Spring Boot ni Spring Framework (incluidas anotaciones como `@Component`, `@Service`, `@Transactional`, `@Autowired`).
- Spring Data (`JpaRepository`, `CrudRepository`, `Pageable`, `Page`, `Sort`, etc.).
- JPA / Jakarta Persistence ni Hibernate.
- Clases de `controller`, `apln`, `service`, `persistence` ni `config`.
- Tipos HTTP o de serialización web.

4.2. `domain` PUEDE contener: clases Java planas, `record`, enumerados, objetos de valor, reglas de negocio, excepciones propias, servicios de dominio puros e interfaces Repository.

4.3. Las invariantes del dominio DEBEN residir en el modelo de dominio, no en controladores, DAO ni mappers.

## 5. Persistencia

5.1. Las interfaces Repository DEBEN declararse en `<modulo>.domain.repository`.

5.2. Sus implementaciones técnicas DEBEN ubicarse en `<modulo>.persistence`.

5.3. Las firmas de las interfaces Repository DEBEN usar únicamente tipos del dominio del propio módulo y tipos estándar de Java. NO DEBEN usar entidades de persistencia ni tipos de Spring Data.

5.4. Los DAO (`persistence/dao`) SOLO realizan acceso a datos. NO DEBEN contener reglas de negocio.

5.5. Solo las implementaciones de Repository de `persistence` PUEDEN usar DAO.

5.6. `controller`, `apln`, `service` y `domain` NO DEBEN acceder a DAO.

5.7. Las entidades de persistencia NO DEBEN salir de `persistence`: no pueden aparecer en firmas, campos ni retornos de clases de otras capas.

5.8. La conversión entre entidad y modelo de dominio DEBE realizarse en `persistence/mapper`.

5.9. NO DEBEN inventarse campos, tablas, columnas ni relaciones que no estén en los requisitos o en el código/esquema existente.

## 6. DTO y comunicación entre módulos

6.1. Todos los DTO Java DEBEN ser `record`.

6.2. Los DTO se usan para entrada/salida HTTP y para contratos públicos entre módulos.

6.3. Los DTO de contrato público entre módulos DEBEN ubicarse en `<modulo>.api.dto`.

6.4. Los DTO exclusivamente HTTP de un módulo PUEDEN ubicarse junto a `controller` o en `api/dto`, siguiendo la convención existente en el repositorio; si no existe convención, DEBEN ubicarse en `api/dto`.

6.5. Controladores y contratos públicos NO DEBEN exponer modelos de dominio ni entidades de persistencia.

6.6. El contrato público de un módulo está formado por los DTO de `api/dto` y las interfaces o puntos de entrada públicos expuestos explícitamente por el módulo en `api`.

6.7. Un módulo NO DEBE acceder a controladores, casos de uso internos, servicios internos, repositorios, DAO, entidades ni modelos de dominio de otro módulo. Solo PUEDE usar su contrato público (§6.6).

6.8. NO DEBEN existir dependencias circulares entre módulos, ni directas ni transitivas.

6.9. Si dos módulos necesitan dependerse mutuamente, NO DEBE resolverse accediendo a clases internas; DEBE informarse como incompatibilidad de diseño.

## 7. Dependencias entre capas

Dependencias permitidas dentro de un módulo (A → B significa "A puede depender de B"):

```text
controller  → apln, api/dto, excepciones de domain/model del propio módulo (solo §7.7)
apln        → domain, service, api/dto, contratos públicos de otros módulos
service     → domain, api/dto, contratos públicos de otros módulos
persistence → domain
domain      → (ningún otro paquete del módulo ni de otros módulos)
api/dto     → (solo tipos Java estándar y otros DTO públicos)
```

7.1. `controller` DEBE delegar en casos de uso de `apln`.

7.2. `apln` DEBE coordinar operaciones mediante contratos de dominio (interfaces Repository y modelos).

7.3. `persistence` implementa los contratos Repository y NO DEBE ser referenciado por ninguna otra capa salvo para el cableado de Spring.

7.4. Ninguna capa DEBE saltarse capas para acceder directamente a DAO o entidades de persistencia.

7.5. `controller` NO DEBE depender de `domain/repository`, `persistence` ni de `service` de otro módulo.

7.6. Las transacciones DEBEN gestionarse en el límite de `apln`. NO DEBEN declararse en `controller` ni en `domain`.

7.7. Única excepción a la tabla de dependencias de §7: `controller` PUEDE importar las excepciones de dominio de `domain/model` de su propio módulo, exclusivamente para traducirlas a códigos de estado HTTP. Esa traducción DEBE realizarse en el propio controlador o en un manejador de excepciones ubicado en `controller` del mismo módulo. `controller` NO DEBE importar excepciones de dominio de otros módulos (§6.7). El paquete global `<paquete-raiz>.config` (§2.2) NO DEBE importar excepciones de ningún módulo y SOLO PUEDE tratar excepciones genéricas de framework o de Java.

## 8. Exposición de tipos

8.1. Las respuestas HTTP DEBEN ser DTO `record`.

8.2. Las entradas HTTP DEBEN ser DTO `record` o parámetros simples.

8.3. NO DEBEN serializarse modelos de dominio ni entidades de persistencia en respuestas HTTP ni en contratos entre módulos.

8.4. Los casos de uso de `apln` invocados por controladores o por otros módulos DEBEN recibir y devolver DTO o tipos Java estándar, no modelos de dominio ni entidades de persistencia.

## 9. Criterios contra abstracciones innecesarias

9.1. NO DEBEN crearse clases, interfaces, paquetes ni archivos vacíos para completar la estructura de §2.

9.2. NO DEBEN crearse interfaces con una única implementación salvo las interfaces Repository de `domain/repository` (exigidas por §5) o cuando una regla de este documento lo requiera.

9.3. NO DEBEN crearse servicios que solo deleguen sin aportar lógica, coordinación ni responsabilidad diferenciada.

9.4. NO DEBEN crearse mappers, DTO, excepciones ni utilidades que no se usen.

9.5. NO DEBEN añadirse operaciones Repository, endpoints, campos ni parámetros no justificados por los requisitos.

9.6. Toda clase nueva DEBE poder justificarse con un requisito concreto.

## 10. Inspección y preservación del código existente

10.1. Antes de crear o modificar código DEBE inspeccionarse la estructura real del repositorio: paquete raíz, módulos existentes, build tool, dependencias, convenciones de nombres y código relacionado.

10.2. DEBEN reutilizarse las clases existentes compatibles con los requisitos en lugar de duplicarlas.

10.3. DEBEN respetarse las convenciones existentes del repositorio que sean compatibles con este documento. Si una convención existente contradice este documento, DEBE informarse la incompatibilidad en lugar de modificar código ajeno sin autorización.

10.4. NO DEBE eliminarse, renombrarse ni reescribirse código existente que no forme parte de la tarea.

10.5. NO DEBEN inventarse requisitos, clases, endpoints, campos, paquetes, dependencias ni decisiones técnicas. Si falta información imprescindible, DEBE informarse el bloqueo.

10.6. NO DEBEN añadirse dependencias al build (Maven/Gradle) sin que la tarea lo requiera; si se añaden, DEBE informarse.

## 11. Comentarios en el código generado

11.1. El código de aplicación generado o modificado NO DEBE contener comentarios de ningún tipo: ni de línea (`//`), ni de bloque (`/* */`), ni JavaDoc (`/** */`).

11.2. Los comentarios ya existentes en código no tocado por la tarea DEBEN preservarse.

## 12. Verificación

12.1. Tras cambios en código DEBEN ejecutarse la compilación y las pruebas disponibles en el repositorio.

12.2. Si existen pruebas ArchUnit u otras verificaciones arquitectónicas, DEBEN ejecutarse.

12.3. Si una verificación no está disponible o falla, DEBE informarse con su salida; NO DEBE afirmarse que el código cumple sin haberlo verificado.
