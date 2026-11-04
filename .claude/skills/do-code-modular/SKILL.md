---
name: do-code-modular
description: Transforma el backend Java/Spring Boot de la Etapa 1 a la Etapa 2 (monolito modular) - identifica módulos con criterio funcional, mueve cada capa a <raiz>.<modulo>, establece los contratos api, elimina ciclos y accesos internos entre módulos y adapta los tests; si el proyecto ya está en Etapa 2 (Facturación), audita y corrige los límites de módulo. Úsala cuando el usuario pida modularizar o auditar módulos. Hace commits y push.
argument-hint: "[módulos propuestos o alcance, opcional]"
disable-model-invocation: true
---

# /do-code-modular

## Propósito

Pasar de la Etapa 1 a la Etapa 2 del modelo de etapas de `/k-back` (sección 2) o, si el proyecto ya está en Etapa 2, auditar y corregir sus límites de módulo. El procedimiento es el común; las normas de módulos son de `/k-back-module` y las de capa de sus skills, y esta skill NO las repite.

## Cuándo usarla

- El proyecto está en Etapa 1 y se quiere un monolito modular.
- El proyecto está en Etapa 2 y se quiere auditar que los módulos están bien delimitados.
- Como segunda transformación de un plan en `/do-code-impl`, desde el `develop` resultante de `/do-code-basic`.

## Entradas

- `$ARGUMENTS` (opcional): módulos propuestos por el usuario o alcance. Si propone módulos, se validan con los criterios funcionales de `/k-back-module` sección 1; si alguno no los cumple, DEBE informarse antes de crearlo.

## Resultado esperado

- Cada módulo en `<raiz>.<modulo>` con la estructura de la Etapa 2 de `/k-back`; DTO movidos de `<raiz>.api.dto` a `<raiz>.<modulo>.api.dto` (D4: el paso 1→2 es añadir el prefijo de módulo); configuración transversal en `<raiz>.config`.
- Comunicación entre módulos solo por su contrato `api`; sin ciclos; `ArquitecturaTest` en verde.
- Rama feature publicada; integrada en `develop` en modo coordinador.

## Skills relacionadas

Procedimiento: `.claude/skills/do-code-impl/procedimiento-comun.md`.

Skills de conocimiento que DEBEN leerse (`.claude/skills/<nombre>/SKILL.md`) y pasarse a los subagentes:

| Skill | Carácter | Para qué |
|---|---|---|
| `/k-back` | Obligatoria | Tabla canónica de la Etapa 2, reglas de evolución, dependencias entre capas |
| `/k-back-module` | Obligatoria | Identificación de módulos, límites, contrato `api`, dependencias y ciclos |
| `/k-back-dto` | Obligatoria | Ubicación de DTO en `<modulo>.api.dto` y DTO de contrato frente a HTTP |
| `/k-back-service` | Obligatoria | Punto de entrada en `apln` y uso de otros módulos desde aplicación |
| `/k-back-domain` | Obligatoria | Reubicación del dominio sin cambiar su comportamiento |
| `/k-back-testing` | Obligatoria | V1-V7, paquete espejo de los tests, `modulosSinCiclos` |
| `/k-back-controller` | Complementaria | Ubicación del controlador por módulo |
| `/k-back-repository` | Complementaria | Reubicación de las interfaces Repository |
| `/k-back-persistence` | Complementaria | Reubicación de entidades, DAO, mappers e implementaciones |

Las complementarias amplían la lista obligatoria de la especificación (sección 4.2) y DEBEN leerse cuando la unidad toque su capa (V4 de `/k-back-testing`).

Anterior: `/do-code-basic`. Siguiente: `/do-code-architecture`.

## Procedimiento

### 0. Modo y Git

Determina el modo (procedimiento común, sección 1) y prepara la rama (p. ej. `feature/monolito-modular` o `feature/auditoria-modulos`) según la sección 4.1.

### 1. Detectar la etapa

- Etapa 0 (sin capas): DEBE informarse y proponerse ejecutar antes `/do-code-basic`; no se modulariza un código sin capas.
- Etapa 1 → **modo transformación**.
- Etapa 2 → **modo auditoría**. Facturación está en Etapa 2 con un único módulo `factura`.
- Etapa 3 → **modo auditoría** de límites dentro de la Etapa 3; NO DEBE retrocederse a `api.dto`/`apln`.

### 2. Identificar módulos (modo transformación)

2.1. Agrupa las clases por concepto de negocio con los criterios funcionales de `/k-back-module` sección 1 (no por capa, tabla ni endpoint). Registra la propuesta: módulo, concepto, agregados, tablas propias, clases que contiene.

2.2. Determina las dependencias entre módulos que exige el código actual y comprueba que forman un grafo acíclico. Una dependencia mutua DEBE informarse como incompatibilidad con las opciones de `/k-back-module` sección 7, y DEBE esperarse la decisión del usuario antes de continuar.

2.3. Si la división en módulos no es evidente, DEBE presentarse la propuesta al usuario y esperar confirmación antes de mover código.

### 3. Planificar las 10 unidades (Fase A)

Registra las 10 unidades según el procedimiento común (sección 2.1).

**Modo transformación** — unidades orientativas (por módulo, en orden de dependencias; las de un mismo módulo PUEDEN agruparse si son pocas clases):

1. Mapa de clases actuales → `<raiz>.<modulo>.<capa>` y grafo de dependencias entre módulos.
2. Dominio (`domain.model`) del primer módulo y sus tests en el paquete espejo.
3. Contratos `domain.repository` y persistencia (`persistence.{entity,dao,mapper}` e implementaciones) del módulo.
4. DTO de `<raiz>.api.dto` → `<modulo>.api.dto`.
5. Casos de uso `apln` y controladores `controller` del módulo.
6. Unidades 2-5 para el resto de módulos (una unidad por módulo restante si hay varios).
7. Contratos públicos entre módulos: sustituir todo acceso a clases internas de otro módulo por su contrato `api` (punto de entrada en `api` implementado en `apln` solo si hay consumidor real, `/k-back-module` sección 5).
8. Configuración: transversal en `<raiz>.config`, específica solo si se justifica en `<modulo>.config`.
9. `ArquitecturaTest`: reglas con los nuevos paquetes, `modulosSinCiclos` incluido, sin relajar ninguna.
10. Revisión final de límites y de que ningún test se ha perdido.

**Modo auditoría (Facturación)**:

1. Cohesión del módulo `factura` con los criterios de `/k-back-module` sección 1 (sin crear módulos sin requisito; p. ej. no existe módulo `cliente`).
2. Paquetes de `factura` frente a la tabla de la Etapa 2.
3. `factura.api.dto` como contrato: solo `record`, sin dominio ni entidades.
4. Ningún acceso desde fuera de `factura` a sus clases internas.
5. Propiedad exclusiva de la tablas `factura` y `linea_factura` (migración `V1__crear_facturas.sql`) y de sus entidades.
6. Ausencia de punto de entrada público sin consumidor.
7. Dependencias entre módulos y ciclos (`modulosSinCiclos`).
8. Configuración transversal frente a `factura.config` (hoy no existe ninguna; no se crea).
9. Tests en paquete espejo y reglas de `ArquitecturaTest` vigentes.
10. Revisión transversal de `/k-back` sobre lo corregido.

Si la auditoría no da trabajo real para 10 unidades, DEBE informarse según el procedimiento común (sección 2.1.4).

### 4. Ejecutar la Fase A

Ejecuta las 10 iteraciones Programa → Verifica (V1-V7 de `/k-back-testing`) → Arregla según el procedimiento común (sección 2.2). Al mover clases DEBEN moverse también sus tests al paquete espejo, conservando todos sus casos.

### 5. Ejecutar la Fase B

Según el procedimiento común (sección 3), incluidas reglas ArchUnit nuevas que la división en módulos exija (contenido de `/k-back-testing`).

### 6. Publicar e integrar

Push de la rama. En modo coordinador, subagente integrador; en modo subagente, se entrega la rama sin integrar.

### 7. Informe

Informe final según el procedimiento común (sección 6), añadiendo:

- Módulos identificados con su justificación funcional y grafo de dependencias.
- Mapa de clase origen → clase destino.
- Incompatibilidades de diseño detectadas (dependencias mutuas) y decisión tomada por el usuario.

## Comprobaciones antes de finalizar

- [ ] Los módulos responden a conceptos de negocio y su división está confirmada o es evidente; no se ha inventado ningún módulo.
- [ ] Cada clase está en `<raiz>.<modulo>` según la Etapa 2 de `/k-back` y los DTO en `<modulo>.api.dto`.
- [ ] Ningún módulo usa clases de otro fuera de su contrato `api`; no hay ciclos.
- [ ] No se ha creado ningún punto de entrada público sin consumidor real.
- [ ] Los tests se han movido al paquete espejo sin perder casos; `ArquitecturaTest` pasa sin reglas relajadas.
- [ ] Las 10 iteraciones, la Fase B y la suite completa están registradas con su resultado real.
- [ ] Commits con `Co-Authored-By`, push hecho e integración hecha o entregada al coordinador.
