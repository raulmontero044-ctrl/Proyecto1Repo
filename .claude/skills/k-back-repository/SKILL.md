---
name: k-back-repository
description: Implementa exclusivamente la capa de persistencia de un módulo backend Java/Spring Boot (interfaces Repository en domain/repository, DAO, entidades de persistencia, mappers e implementaciones técnicas de Repository en persistence). Usar cuando haya que crear o modificar contratos Repository o el acceso a datos de un módulo; no implementa casos de uso, servicios, controladores ni modelos de dominio.
argument-hint: "<módulo y requisitos de persistencia>"
---

# k-back-repository

## Propósito

Implementar la parte de persistencia de un módulo backend conforme a las reglas comunes de `.claude/rules/architecture.md`. Esta Skill no repite esas reglas; aplica solo lo específico de persistencia.

Si `.claude/rules/architecture.md` no está en contexto, léelo antes de empezar. Si no existe, comunícalo en el informe y aplica como mínimo: interfaces Repository en `domain/repository`, implementaciones en `persistence`, DAO sin reglas de negocio, entidades de persistencia confinadas en `persistence` y `domain` sin dependencias de Spring, JPA ni Hibernate.

## Entrada

`$ARGUMENTS`: módulo objetivo y requisitos o tarea de persistencia. Puede ser texto libre, una referencia a un plan o una tarea con `fichero`, `funcion` y `descripcion`.

Si `$ARGUMENTS` está vacío o no identifica el módulo y las operaciones de datos requeridas, pide aclaración antes de escribir código.

## Alcance permitido

Dentro del paquete del módulo:

- `domain/repository`: interfaces Repository definidas desde las necesidades del dominio.
- `persistence/dao`: acceso técnico a datos.
- `persistence/entity`: entidades de persistencia.
- `persistence/mapper`: conversión entre modelos de dominio y entidades de persistencia.
- `persistence`: implementaciones técnicas de las interfaces Repository.
- Consultas y operaciones de acceso a datos justificadas por los requisitos.
- Recursos de persistencia estrictamente necesarios que ya sigan una convención existente en el proyecto, como scripts de migración, solo si el proyecto ya utiliza ese mecanismo.

## Fuera de alcance

- Controladores, endpoints y DTO HTTP.
- Casos de uso en `apln` y servicios en `service`.
- Modelos de dominio en `domain/model`, objetos de valor e invariantes.
- Reglas de negocio en DAO, mappers o implementaciones Repository.
- Persistencia interna de otros módulos.
- Configuración transversal de la aplicación en el paquete global `config`.
- Nuevas dependencias del build sin autorización expresa.

Si la tarea requiere trabajo fuera de este alcance, no lo realices: indícalo como dependencia en el informe.

## Procedimiento

1. Lee `$ARGUMENTS` y, si referencia un plan o requisitos en archivos, léelos completos.
2. Inspecciona el repositorio:
   - Localiza el build (`pom.xml`, `build.gradle`, `build.gradle.kts`) y el paquete raíz real.
   - Identifica la tecnología de persistencia realmente declarada (Spring Data JPA, JDBC, jOOQ, MyBatis u otra). No asumas JPA si no está en las dependencias.
   - Revisa módulos existentes y reproduce sus convenciones de nombres, anotaciones, estructura y estilo.
   - Comprueba si ya existen el Repository, DAO, entidad o mapper solicitados para extenderlos en lugar de duplicarlos.
3. Verifica las dependencias previas:
   - El modelo de dominio que debe persistirse existe en `domain/model` del módulo.
   - Los requisitos definen los campos, identificadores, relaciones y consultas necesarios.
   - Existe un build con la dependencia de persistencia correspondiente.
4. Si falta alguna dependencia previa, detente en esa parte y comunica el bloqueo. No crees el modelo de dominio, no inventes campos, tablas, columnas, relaciones ni consultas, y no añadas dependencias al build por tu cuenta.
5. Define o completa la interfaz Repository en `domain/repository`:
   - Firmas expresadas con tipos del dominio y tipos estándar de Java.
   - Solo las operaciones que exigen los requisitos.
   - Sin anotaciones ni tipos de Spring, Spring Data, JPA o Hibernate.
6. Implementa la persistencia en `persistence`:
   - Entidad de persistencia en `persistence/entity` con los campos derivados de los requisitos y del modelo de dominio.
   - DAO en `persistence/dao` con la tecnología detectada, limitado al acceso a datos.
   - Mapper en `persistence/mapper` entre modelo de dominio y entidad, siguiendo el mecanismo de mapeo ya usado en el proyecto o, si no hay ninguno, conversión manual sin nuevas dependencias.
   - Implementación de la interfaz Repository en `persistence`, que usa el DAO y el mapper y devuelve únicamente tipos del dominio.
7. No crees clases, paquetes ni interfaces vacíos o sin uso justificado por los requisitos.
8. No escribas comentarios en el código generado: ni de línea, ni de bloque, ni JavaDoc.
9. Ejecuta las verificaciones de finalización.

## Verificación de finalización

- Compila el proyecto con el build existente (`./mvnw -q compile`, `mvn -q compile`, `./gradlew compileJava` o equivalente). Si existen pruebas de persistencia o ArchUnit, ejecútalas. Si no hay build, indícalo en el informe.
- Cada interfaz Repository tiene una implementación en `persistence` y sus firmas coinciden.
- `domain` no importa `org.springframework`, `jakarta.persistence`, `javax.persistence`, `org.hibernate` ni clases de `persistence`.
- Las clases de `persistence/entity` y `persistence/dao` no se referencian fuera de `persistence` del propio módulo.
- No hay referencias a la persistencia de otros módulos.
- No hay reglas de negocio en DAO, mappers ni implementaciones Repository.
- El código generado no contiene comentarios.

Usa búsquedas en el código para estas comprobaciones e incluye los resultados en el informe.

## Informe final

```text
Módulo: <nombre>
Archivos creados: <rutas>
Archivos modificados: <rutas>
Contratos Repository: <interfaz → implementación, operaciones>
Tecnología de persistencia: <detectada en el build>
Verificaciones: <compilación, pruebas, comprobaciones de dependencias y resultado>
Bloqueos: <dependencias faltantes o NINGUNO>
Dependencias para otras Skills: <trabajo de dominio, aplicación o HTTP necesario o NINGUNA>
Dudas: <cuestiones abiertas o NINGUNA>
```
