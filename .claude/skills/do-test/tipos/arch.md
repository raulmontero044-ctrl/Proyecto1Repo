# Perfil de tipo: `arch`

Alcance de los tests de arquitectura en Facturación. Las reglas que deben existir y su estilo son de `/k-back-testing` secciones 4 y 5; las normas de las que se derivan, de `/k-back` y `/k-back-module`. No se repiten aquí.

## Qué abarca

| Pieza | Sección de `/k-back-testing` | Herramientas |
|---|---|---|
| `ArquitecturaTest`: capas, dependencias, ubicación de entidades, handlers, accesos a `internal` | 4.1, 4.2, 4.4 | ArchUnit (`@AnalyzeClasses`, `@ArchTest static final ArchRule`, `allowEmptyShould(true)`) |
| Pruebas de que una regla rechaza lo que debe (`ReglaContratoEntreModulosTest`, `ReglaHandlersSeparadosTest`) con clases ficticias de `com.facturacion.ficticio..` y `com.facturacion.app.ficticio` | 4.5 | `ClassFileImporter` sobre clases ficticias |
| `ModulosTest` | 5 | Spring Modulith (`ApplicationModules.verify()`) |

## Criterios propios del tipo

- Una regla con `allowEmptyShould(true)` cuyo `that()` no selecciona ninguna clase real pasa sin comprobar nada (V3 de `/k-back-testing`). Toda regla nueva DEBE seleccionar clases reales o ir acompañada de una prueba con clases ficticias que demuestre que la regla falla ante una violación.
- Una regla nueva DEBE derivarse de una norma escrita en `/k-back` o `/k-back-*`, citando la sección. NO DEBEN inventarse restricciones arquitectónicas.
- Ninguna regla existente se elimina, relaja ni llena de excepciones (`/k-back-testing` sección 4.4). Si el código de producción incumple una regla nueva, es un defecto de producción (procedimiento sección 5.3), no un motivo para relajar la regla.
- Las clases ficticias son código de test: no se cuentan como tests, pero sí DEBEN preservarse.

## Infraestructura

Ninguna adicional. Añadir otra herramienta (p. ej. jMolecules, Taikai) activa la puerta de decisión del procedimiento sección 3.3.

## Mutaciones orientativas para el falsador

Añadir en un worktree desechable una clase de producción que viole la regla (dominio que importa Spring o JPA, `@Entity` fuera de `internal.persistence.entity`, acceso a `internal` desde otro módulo o desde `app`, handler con dos `@ExceptionHandler`, controlador que usa el Repository) y comprobar que la regla correspondiente falla; cambiar el patrón de paquete de una regla y comprobar si deja de seleccionar clases.
