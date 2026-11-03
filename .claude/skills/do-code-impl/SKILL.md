---
name: do-code-impl
description: Ejecuta un plan de implementación backend redactado en lenguaje natural. Lo descompone en tareas, las contrasta con el agente falsador y delega cada tarea en la Skill backend especializada. Uso explícito: /do-code-impl plan.md
argument-hint: <ruta-plan.md>
disable-model-invocation: true
---

# /do-code-impl

Coordina la implementación de un plan existente. No implementa directamente tareas que corresponden a las Skills especializadas.

Las reglas arquitectónicas comunes están en `.claude/rules/architecture.md`. Aplícalas; esta Skill no las repite.

Esta Skill debe ejecutarse en el hilo principal de la conversación, no en un subagente, porque necesita las herramientas `Agent` y `Skill`.

Alcance de esta versión: solo tareas backend Java con Spring Boot. Si el plan contiene tareas frontend, márcalas como fuera de alcance en el informe y no las implementes ni invoques Skills frontend.

## Entrada

`$ARGUMENTS` es la ruta del plan, por ejemplo `plan.md`. El plan puede estar en lenguaje natural; el usuario no tiene que convertirlo a JSON Lines.

Si no se indica ruta o el fichero no existe, informa y detente.

## Mecanismos de invocación

- Falsador: herramienta `Agent` con `subagent_type: "falsador"` y, en el prompt, las líneas JSON Lines numeradas.
- Skills: herramienta `Skill` con `skill` igual a `k-back-module`, `k-back-repository`, `k-back-service` o `k-back-controller`, y `args` con las tareas asignadas.

Una Skill invocada se carga en el contexto actual. Esperar su resultado significa completar todas sus instrucciones antes de seguir.

Si la herramienta `Agent`, el agente `falsador`, la herramienta `Skill` o alguna Skill necesaria no están disponibles, informa de la limitación y detente en ese punto. No simules invocaciones ni hagas el trabajo de la Skill o del falsador por tu cuenta. No afirmes haber invocado nada que no se haya invocado con la herramienta real.

## Procedimiento

1. Leer íntegramente el fichero indicado en `$ARGUMENTS`.
2. Inspeccionar el repositorio y los ficheros relevantes: estructura de paquetes, paquete raíz, módulos existentes, fichero de build y documentación.
3. Descomponer el plan en tareas atómicas y verificables. Cada tarea debe derivar de algo escrito en el plan; no añadas requisitos.
4. Representar cada tarea con tres campos:
   - `fichero`: ruta del fichero afectado, adaptada a la estructura real del repositorio.
   - `funcion`: clase, interfaz, record o método principal de la tarea.
   - `descripcion`: qué hay que hacer, con las afirmaciones del plan que la justifican.
5. Crear una entrada JSON Lines con un objeto JSON por línea y por tarea, en el mismo orden que la lista de tareas. La línea N corresponde a la tarea N.
6. Invocar obligatoriamente al falsador con `Agent` y `subagent_type: "falsador"` antes de implementar nada, pasándole las líneas JSON Lines.
7. Esperar su resultado y asociar a cada tarea la clasificación de su número de línea. Si falta la clasificación de alguna línea, trata esa tarea como `INDETERMINADA`.
8. Aplicar estas reglas:
   - `MENTIRA`: no implementar esa tarea. Informar de la afirmación falsa y de la evidencia aportada por el falsador.
   - `NO DETECTADA`: la tarea continúa en el proceso.
   - `INDETERMINADA`: no tratarla como falsa. Continuar solo si hay información suficiente para implementar sin inventar requisitos; si falta información imprescindible, pedir aclaración al usuario antes de implementarla.
9. Identificar la Skill correspondiente a cada tarea según la tabla de delegación.
10. Ordenar las tareas por dependencias. Una tarea que depende de otra descartada por `MENTIRA` o pendiente de aclaración queda bloqueada y se informa.
11. Invocar las Skills necesarias con la herramienta `Skill`, pasando en `args` las tareas asignadas con sus campos.
12. Esperar el resultado de cada Skill antes de iniciar tareas que dependan de él.
13. Paralelismo: las Skills invocadas con `Skill` se ejecutan secuencialmente en este hilo. Solo se pueden ejecutar en paralelo tareas independientes y sin ficheros en común, delegando cada grupo en un subagente con `Agent` al que se indique que invoque la Skill correspondiente con `Skill`. Si no se puede garantizar la independencia, ejecutar secuencialmente.
14. Utilizar `k-back-module` cuando el plan pida un módulo completo.
15. Utilizar `k-back-repository`, `k-back-service` o `k-back-controller` para tareas específicas.
16. No duplicar ni simular el trabajo de las Skills especializadas.
17. Ejecutar compilación, pruebas y ArchUnit cuando estén disponibles.
18. Entregar un resumen trazable respecto al plan original.

## Formato JSON Lines

```jsonl
{"fichero":"src/main/java/es/ejemplo/usuarios/domain/model/Usuario.java","funcion":"Usuario","descripcion":"Crear el modelo de dominio Usuario según los atributos y reglas definidos en el plan."}
{"fichero":"src/main/java/es/ejemplo/usuarios/domain/repository/UsuarioRepository.java","funcion":"UsuarioRepository","descripcion":"Definir el contrato Repository con las operaciones necesarias para los casos de uso descritos."}
{"fichero":"src/main/java/es/ejemplo/usuarios/apln/CrearUsuarioUseCase.java","funcion":"crearUsuario","descripcion":"Implementar el caso de uso de creación de usuario conforme a las reglas del plan."}
```

Las rutas son ejemplos. Adáptalas al paquete raíz y a la estructura real del repositorio; si no se pueden determinar, indícalo en `descripcion` en lugar de inventar una ruta definitiva.

## Tabla de delegación

| Tarea | Skill |
| --- | --- |
| Módulo completo pedido en el plan | `k-back-module` |
| `domain/repository`, `persistence/dao`, `persistence/entity`, `persistence/mapper`, implementaciones Repository | `k-back-repository` |
| `apln`, `service`, servicios de dominio, `domain/model` con sus reglas e invariantes | `k-back-service` |
| `api/dto` de casos de uso y contratos públicos entre módulos | `k-back-service` |
| `controller`, DTO `record` exclusivamente HTTP de entrada y salida | `k-back-controller` |

A `k-back-controller` solo van los DTO exclusivamente HTTP. Si no está claro de qué tipo es un DTO, asígnalo a la Skill dueña del caso de uso que lo usa.

Cuando se use `k-back-module` para un módulo, no invoques además las Skills especializadas para las mismas tareas de ese módulo.

Si una tarea no encaja en ninguna Skill, por ejemplo configuración global en `config`, no la implementes por tu cuenta sin justificación: informa de ella en el resumen como no delegada.

## Verificación

Detecta el sistema de build real antes de ejecutar nada:

- `pom.xml` y `mvnw`: usar `./mvnw`; solo `pom.xml`: usar `mvn`.
- `build.gradle` o `build.gradle.kts` y `gradlew`: usar `./gradlew`; sin wrapper: usar `gradle`.
- Sin fichero de build: informar de que no se puede compilar.

Ejecuta compilación y pruebas con el sistema detectado, y las pruebas ArchUnit si existen. No inventes comandos, tareas ni perfiles. Si algo falla, informa con la salida real.

## Resumen final

Para cada tarea, en el orden del plan:

| Línea | fichero | funcion | Clasificación | Skill | Ficheros afectados | Resultado |
| --- | --- | --- | --- | --- | --- | --- |

`Resultado` es uno de: implementada, descartada por `MENTIRA`, pendiente de aclaración, bloqueada por dependencia, fuera de alcance o fallida.

Añade además:

- Afirmaciones falsas detectadas y su evidencia.
- Aclaraciones pedidas al usuario.
- Bloqueos comunicados por las Skills.
- Resultado de compilación, pruebas y ArchUnit, o el motivo por el que no se ejecutaron.
