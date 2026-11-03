---
name: k-back-module
description: Crea e implementa un módulo backend Java/Spring Boot completo coordinando las Skills especializadas: /k-back-service para el modelo de dominio si falta, después /k-back-repository, /k-back-service para los casos de uso y /k-back-controller. Úsala cuando se pida un módulo completo, directamente o desde /do-code-impl.
argument-hint: <nombre-del-módulo y requisitos o ruta a fichero de requisitos>
---

# /k-back-module

Coordina la creación de un módulo backend completo. No implementa por sí misma persistencia, casos de uso ni controladores: delega cada capa en su Skill especializada.

Las reglas arquitectónicas comunes están en `.claude/rules/architecture.md`. Aplícalas; esta Skill no las repite.

Esta Skill debe ejecutarse en el hilo principal de la conversación, no en un subagente, porque necesita la herramienta `Skill`.

## Entrada

`$ARGUMENTS` contiene el nombre del módulo y sus requisitos, o la ruta a un fichero que los describe. Si viene de `/do-code-impl`, contiene las tareas del módulo con sus campos `fichero`, `funcion` y `descripcion`.

Si los requisitos no bastan para identificar modelos, casos de uso o endpoints sin inventarlos, pide aclaración antes de invocar ninguna Skill.

## Mecanismo de invocación

Las Skills especializadas se invocan con la herramienta `Skill` de Claude Code:

- `Skill` con `skill: "k-back-repository"` y `args` con el alcance de persistencia.
- `Skill` con `skill: "k-back-service"` y `args` con el alcance de aplicación y dominio.
- `Skill` con `skill: "k-back-controller"` y `args` con el alcance HTTP.

Una Skill invocada se carga en el contexto actual. Esperar a que termine significa completar todas sus instrucciones, incluidos sus criterios de finalización, antes de pasar a la siguiente.

Si la herramienta `Skill` no está disponible o alguna Skill no existe, informa de la limitación y detente. No simules la invocación ni hagas tú el trabajo de esa Skill. No afirmes haber invocado una Skill si no se ha usado la herramienta `Skill`.

## Procedimiento

1. Analizar el proyecto y los requisitos recibidos: leer `$ARGUMENTS`, la documentación del repositorio y el fichero de build.
2. Identificar límites, contratos públicos, modelos, casos de uso y endpoints del módulo, solo a partir de los requisitos.
3. Inspeccionar los módulos existentes, el paquete raíz y el paquete global `config` para evitar duplicidades y dependencias circulares.
4. Determinar qué trabajo hace falta en persistencia, aplicación y HTTP. Redactar para cada Skill un alcance concreto: ficheros, elementos y requisitos de los que deriva cada uno.
   4b. Si los modelos de dominio que necesita la persistencia no existen, invocar primero `Skill` con `skill: "k-back-service"` y un encargo de solo modelo de dominio: modelos, objetos de valor, invariantes y excepciones de `domain/model`, sin casos de uso. Esperar su finalización y revisar el resultado antes de continuar.
5. Invocar `Skill` con `skill: "k-back-repository"` y su alcance.
6. Esperar su finalización y revisar el resultado: contratos Repository creados, bloqueos comunicados y ficheros modificados.
7. Invocar `Skill` con `skill: "k-back-service"` y su alcance, indicando los contratos Repository disponibles.
8. Esperar su finalización y revisar el resultado: casos de uso en `apln`, modelos y servicios, bloqueos comunicados.
9. Invocar `Skill` con `skill: "k-back-controller"` y su alcance, indicando los casos de uso disponibles.
10. Esperar su finalización y revisar el resultado: endpoints, DTO `record` y bloqueos comunicados.
11. Integrar los resultados y resolver incompatibilidades de coordinación, como nombres o firmas que no encajan entre capas. Si una corrección pertenece a una capa, vuelve a invocar la Skill de esa capa con el ajuste concreto en lugar de editarla tú.
12. Ejecutar compilación, pruebas y verificaciones arquitectónicas disponibles.

## Orden

La secuencia normal es Repository → Service (`apln`) → Controller. Si faltan los modelos de dominio que necesita la persistencia, va antes el encargo de solo modelo de dominio a `k-back-service` (paso 4b): Service (`domain/model`) → Repository → Service (`apln`) → Controller. Solo se altera cuando haya una justificación técnica clara, por ejemplo que la persistencia ya exista y no requiera cambios, y sin romper dependencias. Si se omite o reordena una Skill, explica el motivo en el informe.

Si una Skill comunica un bloqueo que impide la siguiente capa, no continúes con esa capa: resuelve el bloqueo con la Skill responsable o pide aclaración al usuario.

## Verificación

Detecta el sistema de build real del repositorio antes de ejecutar nada:

- `pom.xml` y `mvnw`: usar `./mvnw`; solo `pom.xml`: usar `mvn`.
- `build.gradle` o `build.gradle.kts` y `gradlew`: usar `./gradlew`; sin wrapper: usar `gradle`.
- Sin fichero de build: informar de que no se puede compilar.

Ejecuta compilación y pruebas con el sistema detectado. Ejecuta las pruebas ArchUnit si existen en el proyecto. No inventes comandos, tareas ni perfiles que el proyecto no define. Si algo falla, informa con la salida real.

## Informe

Al terminar, informa de:

- Skills invocadas, en orden, y resultado de cada una.
- Ficheros creados o modificados por capa.
- Bloqueos, dudas y decisiones tomadas.
- Resultado de compilación, pruebas y ArchUnit, o el motivo por el que no se ejecutaron.
