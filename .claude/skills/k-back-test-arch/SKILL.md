---
name: k-back-test-arch
description: Normas de los tests de arquitectura del backend Java/Spring Boot de Facturación - ArquitecturaTest con ArchUnit, pruebas de reglas con clases ficticias (ReglaContratoEntreModulosTest, ReglaHandlersSeparadosTest), ModulosTest con Spring Modulith, reglas que no seleccionan clases, derivación de reglas desde las normas k-back, infraestructura y mutaciones que los tests deben detectar. Úsala al escribir, reparar, falsar o revisar tests de arquitectura; /do-test y /do-test-arch-* la aplican. No modifica código.
argument-hint: "[regla o duda concreta sobre tests de arquitectura, opcional]"
---

# /k-back-test-arch

## Propósito

Fijar cómo se hace un test de arquitectura en Facturación: qué abarca el tipo `arch`, con qué herramientas, qué criterios propios tiene y qué violaciones reales debe detectar. Es una skill de conocimiento: NO modifica código, NO ejecuta Git y NO define procedimientos. El ciclo de testing es de `.claude/skills/do-test/procedimiento-testing.md`.

Las reglas que deben existir y su estilo son de `/k-back-testing` secciones 4 y 5; las normas de las que se derivan, de `/k-back` y `/k-back-module`. NO se repiten aquí. Los términos DEBE, NO DEBE y PUEDE son normativos.

## Cuándo usarla

- Al crear, reparar o revisar reglas ArchUnit o `ModulosTest`.
- Al falsar los tests de arquitectura, para elegir las violaciones.

## Entradas

Ninguna obligatoria. Si recibe `$ARGUMENTS`, se interpreta como la regla o la duda concreta a la que aplicar estas normas.

## Resultado esperado

Normas, criterios y checklist del tipo `arch`. Ningún archivo se crea ni se modifica.

## Skills relacionadas

- `/k-back-testing`: reglas existentes (sección 4.2), clases ficticias (4.5) y Spring Modulith (5).
- `/k-back` y `/k-back-module`: normas de las que se deriva cada regla.
- `/k-back-test-unit`, `/k-back-test-api`, `/k-back-test-e2e`: los demás tipos.

## Normas

### 1. Qué abarca

| Pieza | Sección de `/k-back-testing` | Herramientas |
|---|---|---|
| `ArquitecturaTest`: capas, dependencias, ubicación de entidades, handlers, accesos a `internal` | 4.1, 4.2, 4.4 | ArchUnit (`@AnalyzeClasses`, `@ArchTest static final ArchRule`, `allowEmptyShould(true)`) |
| Pruebas de que una regla rechaza lo que debe (`ReglaContratoEntreModulosTest`, `ReglaHandlersSeparadosTest`) con clases ficticias de `com.facturacion.ficticio..` y `com.facturacion.app.ficticio` | 4.5 | `ClassFileImporter` sobre clases ficticias |
| `ModulosTest` | 5 | Spring Modulith (`ApplicationModules.verify()`) |

### 2. Criterios propios del tipo

2.1. Una regla con `allowEmptyShould(true)` cuyo `that()` no selecciona ninguna clase real pasa sin comprobar nada (V3 de `/k-back-testing`). Toda regla nueva DEBE seleccionar clases reales o ir acompañada de una prueba con clases ficticias que demuestre que la regla falla ante una violación.

2.2. Una regla nueva DEBE derivarse de una norma escrita en `/k-back` o `/k-back-*`, citando la sección. NO DEBEN inventarse restricciones arquitectónicas.

2.3. Ninguna regla existente se elimina, relaja ni llena de excepciones (`/k-back-testing` sección 4.4). Si el código de producción incumple una regla nueva, es un defecto de producción que se documenta, no un motivo para relajar la regla.

2.4. Las clases ficticias son código de test: no se cuentan como tests, pero DEBEN preservarse.

### 3. Infraestructura

Ninguna adicional. Añadir otra herramienta (p. ej. jMolecules, Taikai) requiere autorización del usuario (`/k-back-testing` sección 7.4).

### 4. Violaciones que los tests DEBEN detectar

Añadir, en un worktree desechable, una clase de producción que viole la regla (dominio que importa Spring o JPA, `@Entity` fuera de `internal.persistence.entity`, acceso a `internal` desde otro módulo o desde `app`, handler con dos `@ExceptionHandler`, controlador que usa el Repository) y comprobar que la regla correspondiente falla; cambiar el patrón de paquete de una regla y comprobar si deja de seleccionar clases.

## Comprobaciones antes de finalizar

- [ ] Cada regla nueva cita la norma de la que se deriva.
- [ ] Ninguna regla nueva es vacía: selecciona clases reales o tiene su prueba con clases ficticias.
- [ ] Ninguna regla existente se ha eliminado, relajado ni llenado de excepciones.
- [ ] Las clases ficticias siguen en la capa que su nombre indica.
- [ ] Las violaciones de la sección 4 aplicables hacen fallar la regla correspondiente.
