# Mapa de skills

> Generado por `/do-organizar-skills`. No lo edites a mano: vuelve a ejecutar la skill.
> Las skills siguen en `.claude/skills/<nombre>/`; este mapa solo las agrupa.

```
back/
├── codigo/
│   ├── do-code-architecture
│   ├── do-code-basic
│   ├── do-code-impl
│   ├── do-code-modular
│   ├── do-code-module
│   └── do-code-repair
├── testing/
│   ├── do-test
│   ├── do-test-api-create
│   ├── do-test-api-repair
│   ├── do-test-arch-create
│   ├── do-test-arch-repair
│   ├── do-test-e2e-create
│   ├── do-test-e2e-repair
│   ├── do-test-unit-create
│   └── do-test-unit-repair
├── normas/
│   ├── k-back
│   ├── k-back-controller
│   ├── k-back-domain
│   ├── k-back-dto
│   ├── k-back-module
│   ├── k-back-persistence
│   ├── k-back-repository
│   └── k-back-service
└── normas-testing/
    ├── k-back-test-api
    ├── k-back-test-arch
    ├── k-back-test-e2e
    ├── k-back-test-unit
    └── k-back-testing
front/
└── (sin skills)
comun/
├── codigo/
│   └── do-crud
├── normas/
│   └── k-crud
└── utilidades/
    └── do-organizar-skills
```

## back

### codigo

| Skill | Ruta | Resumen |
|---|---|---|
| `/do-code-architecture` | `.claude/skills/do-code-architecture/SKILL.md` | Transforma el backend Java/Spring Boot de la Etapa 2 (monolito modular) a la Etapa 3 (arquitectura modular definitiva con api/internal, facade, app y package-info con Spring Modulith) según la tabla canónica de /k-back, preservando endpoints, JSON, reglas, migraciones Flyway y todos los tests; si ya está en Etapa 3, audita y corrige |
| `/do-code-basic` | `.claude/skills/do-code-basic/SKILL.md` | Aplica la Etapa 1 (arquitectura básica en capas, sin módulos) al backend Java/Spring Boot |
| `/do-code-impl` | `.claude/skills/do-code-impl/SKILL.md` | Coordina la ejecución de un plan de implementación backend (plan.md) |
| `/do-code-modular` | `.claude/skills/do-code-modular/SKILL.md` | Transforma el backend Java/Spring Boot de la Etapa 1 a la Etapa 2 (monolito modular) |
| `/do-code-module` | `.claude/skills/do-code-module/SKILL.md` | Crea un módulo de negocio nuevo en el backend Java/Spring Boot en la etapa actual del proyecto |
| `/do-code-repair` | `.claude/skills/do-code-repair/SKILL.md` | Detecta y repara conflictos de integración, incompatibilidades y errores introducidos por varios agentes que han trabajado simultáneamente en el proyecto |

### testing

| Skill | Ruta | Resumen |
|---|---|---|
| `/do-test` | `.claude/skills/do-test/SKILL.md` | Orquesta un ciclo completo de testing del backend Java/Spring Boot de Facturación |
| `/do-test-api-create` | `.claude/skills/do-test-api-create/SKILL.md` | Crea tests de API e integración (capa HTTP y, si se autoriza, persistencia e integración con base de datos) del backend Java/Spring Boot de Facturación con cinco propuestas independientes en worktrees, cada una falsada con code-falsador.md por un test-falsifier, elige la mejor con un test-evaluator y la integra en una rama feature/test-api-* |
| `/do-test-api-repair` | `.claude/skills/do-test-api-repair/SKILL.md` | Analiza y repara los tests de API e integración (capa HTTP y, si se autoriza, persistencia e integración con base de datos) del backend Java/Spring Boot de Facturación con cinco reparaciones independientes en worktrees sobre la misma base, cada una falsada con code-falsador.md por un test-falsifier, elige la mejor con un test-evaluator y la integra en develop comprobando que no se pierde ningún test |
| `/do-test-arch-create` | `.claude/skills/do-test-arch-create/SKILL.md` | Crea tests de arquitectura (ArchUnit y Spring Modulith) del backend Java/Spring Boot de Facturación con cinco propuestas independientes en worktrees, cada una falsada con code-falsador.md por un test-falsifier, elige la mejor con un test-evaluator y la integra en una rama feature/test-arch-* |
| `/do-test-arch-repair` | `.claude/skills/do-test-arch-repair/SKILL.md` | Analiza y repara los tests de arquitectura (ArchUnit y Spring Modulith) del backend Java/Spring Boot de Facturación con cinco reparaciones independientes en worktrees sobre la misma base, cada una falsada con code-falsador.md por un test-falsifier, elige la mejor con un test-evaluator y la integra en develop comprobando que no se pierde ningún test |
| `/do-test-e2e-create` | `.claude/skills/do-test-e2e-create/SKILL.md` | Crea tests end-to-end (aplicación completa por HTTP real con base de datos real) del backend Java/Spring Boot de Facturación con cinco propuestas independientes en worktrees, cada una falsada con code-falsador.md por un test-falsifier, elige la mejor con un test-evaluator y la integra en una rama feature/test-e2e-* |
| `/do-test-e2e-repair` | `.claude/skills/do-test-e2e-repair/SKILL.md` | Analiza y repara los tests end-to-end (aplicación completa por HTTP real con base de datos real) del backend Java/Spring Boot de Facturación con cinco reparaciones independientes en worktrees sobre la misma base, cada una falsada con code-falsador.md por un test-falsifier, elige la mejor con un test-evaluator y la integra en develop comprobando que no se pierde ningún test |
| `/do-test-unit-create` | `.claude/skills/do-test-unit-create/SKILL.md` | Crea tests unitarios del backend Java/Spring Boot de Facturación con cinco propuestas independientes en worktrees, cada una falsada con code-falsador.md por un test-falsifier, elige la mejor con un test-evaluator y la integra en una rama feature/test-unit-* |
| `/do-test-unit-repair` | `.claude/skills/do-test-unit-repair/SKILL.md` | Analiza y repara los tests unitarios del backend Java/Spring Boot de Facturación con cinco reparaciones independientes en worktrees sobre la misma base, cada una falsada con code-falsador.md por un test-falsifier, elige la mejor con un test-evaluator y la integra en develop comprobando que no se pierde ningún test |

### normas

| Skill | Ruta | Resumen |
|---|---|---|
| `/k-back` | `.claude/skills/k-back/SKILL.md` | Normas generales del backend Java/Spring Boot de Facturación |
| `/k-back-controller` | `.claude/skills/k-back-controller/SKILL.md` | Normas de la capa HTTP del backend Java/Spring Boot de Facturación |
| `/k-back-domain` | `.claude/skills/k-back-domain/SKILL.md` | Normas del modelo de dominio del backend Java/Spring Boot de Facturación |
| `/k-back-dto` | `.claude/skills/k-back-dto/SKILL.md` | Normas de los DTO del backend Java/Spring Boot de Facturación |
| `/k-back-module` | `.claude/skills/k-back-module/SKILL.md` | Normas de módulos del backend Java/Spring Boot de Facturación |
| `/k-back-persistence` | `.claude/skills/k-back-persistence/SKILL.md` | Normas de la capa de persistencia del backend Java/Spring Boot de Facturación |
| `/k-back-repository` | `.claude/skills/k-back-repository/SKILL.md` | Normas de las interfaces Repository del backend Java/Spring Boot de Facturación |
| `/k-back-service` | `.claude/skills/k-back-service/SKILL.md` | Normas de la capa de aplicación del backend Java/Spring Boot de Facturación |

### normas-testing

| Skill | Ruta | Resumen |
|---|---|---|
| `/k-back-test-api` | `.claude/skills/k-back-test-api/SKILL.md` | Normas de los tests de API e integración del backend Java/Spring Boot de Facturación |
| `/k-back-test-arch` | `.claude/skills/k-back-test-arch/SKILL.md` | Normas de los tests de arquitectura del backend Java/Spring Boot de Facturación |
| `/k-back-test-e2e` | `.claude/skills/k-back-test-e2e/SKILL.md` | Normas de los tests extremo a extremo del backend Java/Spring Boot de Facturación |
| `/k-back-test-unit` | `.claude/skills/k-back-test-unit/SKILL.md` | Normas de los tests unitarios del backend Java/Spring Boot de Facturación |
| `/k-back-testing` | `.claude/skills/k-back-testing/SKILL.md` | Normas de testing y calidad del backend Java/Spring Boot de Facturación |

## front

Sin skills.

## comun

### codigo

| Skill | Ruta | Resumen |
|---|---|---|
| `/do-crud` | `.claude/skills/do-crud/SKILL.md` | Implementa un CRUD completo de un recurso con subagentes |

### normas

| Skill | Ruta | Resumen |
|---|---|---|
| `/k-crud` | `.claude/skills/k-crud/SKILL.md` | Especificación técnica y auditoría reparadora de CRUD |

### utilidades

| Skill | Ruta | Resumen |
|---|---|---|
| `/do-organizar-skills` | `.claude/skills/do-organizar-skills/SKILL.md` | Organiza las skills del proyecto en un mapa front/back → categoría sin mover ni renombrar ninguna |
