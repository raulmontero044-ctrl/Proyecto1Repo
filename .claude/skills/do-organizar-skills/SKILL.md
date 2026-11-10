---
name: do-organizar-skills
description: Organiza las skills del proyecto en un mapa front/back → categoría sin mover ni renombrar ninguna - lee .claude/skills/*/SKILL.md, clasifica cada skill por prefijo y genera .claude/skills/README.md y la sección de skills de AGENTS.md. Úsala al añadir, quitar o cambiar skills.
argument-hint: "[opcional: skill concreta a reclasificar]"
disable-model-invocation: true
---

# /do-organizar-skills

## Propósito

Mantener un mapa de las skills del proyecto agrupado primero por lado (`back`, `front`, `comun`) y después por categoría. Las skills NO se mueven ni se renombran: Claude Code solo detecta las que están en `.claude/skills/<nombre>/SKILL.md`, a un nivel, y las skills y agentes se referencian entre sí por esa ruta. La organización vive en el mapa, no en las carpetas.

Los términos DEBE, NO DEBE y PUEDE son normativos.

## Cuándo usarla

- Al crear, borrar o cambiar la descripción de una skill.
- Cuando el mapa `.claude/skills/README.md` no exista o no coincida con las carpetas.

## Entradas

Ninguna obligatoria. Si recibe `$ARGUMENTS`, se interpreta como el nombre de una skill cuya clasificación hay que revisar con el usuario aunque encaje en una regla.

## Resultado esperado

- `.claude/skills/README.md` regenerado por completo.
- El bloque entre `<!-- skills:inicio -->` y `<!-- skills:fin -->` de `AGENTS.md` (en la raíz del repositorio) actualizado.
- Un informe de altas, bajas y reclasificaciones.

Ningún otro archivo se crea, modifica, mueve ni renombra.

## Procedimiento

### 1. Inventario

1.1. DEBE listar las carpetas de `.claude/skills/` y leer el frontmatter (`name`, `description`) de cada `SKILL.md`. `README.md` no es una skill.

1.2. Si una carpeta no tiene `SKILL.md`, o su `name` no coincide con el nombre de la carpeta, DEBE informarse y NO DEBE corregirse: el nombre de las skills no se toca.

### 2. Clasificación

2.1. Cada skill se clasifica con la primera regla que encaje, en este orden:

| Prefijo | Lado | Categoría |
|---|---|---|
| `k-back-test-*`, `k-back-testing` | back | normas-testing |
| `k-back*` | back | normas |
| `do-test*` | back | testing |
| `do-code-*` | back | codigo |
| `k-front-test-*`, `k-front-testing` | front | normas-testing |
| `k-front*` | front | normas |
| `do-front-test*` | front | testing |
| `do-front-*` | front | codigo |
| `k-crud` | comun | normas |
| `do-crud` | comun | codigo |
| `do-organizar-skills` | comun | utilidades |

2.2. Si una skill no encaja en ninguna regla, o es la indicada en `$ARGUMENTS`, DEBE proponerse lado y categoría a partir de su `description` y confirmarse con el usuario (AskUserQuestion) antes de escribir. Si la decisión depende de un prefijo nuevo, DEBE proponerse añadirlo a la tabla 2.1.

2.3. NO DEBEN inventarse categorías vacías dentro de un lado; `front/` se muestra aunque no tenga skills.

### 3. `.claude/skills/README.md`

3.1. DEBE sobrescribirse completo con esta forma:

````markdown
# Mapa de skills

> Generado por `/do-organizar-skills`. No lo edites a mano: vuelve a ejecutar la skill.
> Las skills siguen en `.claude/skills/<nombre>/`; este mapa solo las agrupa.

```
back/
├── codigo/
│   ├── do-code-basic
│   └── ...
└── normas/
    └── ...
front/
└── (sin skills)
comun/
└── utilidades/
    └── do-organizar-skills
```

## back

### codigo

| Skill | Ruta | Resumen |
|---|---|---|
| `/do-code-basic` | `.claude/skills/do-code-basic/SKILL.md` | Aplica la Etapa 1 (...) al backend Java/Spring Boot |
````

3.2. Orden fijo: lados `back`, `front`, `comun`; categorías `codigo`, `testing`, `normas`, `normas-testing`, `utilidades` y después las nuevas por orden alfabético; skills por orden alfabético.

3.3. El resumen es la `description` hasta el primer ` - ` o, si no lo hay, hasta el primer punto seguido de espacio o fin de texto (así `code-falsador.md` no corta el resumen), sin cambiar su texto. Las `|` del resumen DEBEN escaparse como `\|`.

### 4. `AGENTS.md`

4.1. Si no existe, DEBE crearse con el título `# AGENTS.md` y el bloque marcado.

4.2. Solo DEBE reemplazarse el contenido entre `<!-- skills:inicio -->` y `<!-- skills:fin -->`; el resto del archivo NO DEBE tocarse. Si faltan los marcadores, el bloque se añade al final.

4.3. El bloque contiene el encabezado `## Skills`, el árbol del apartado 3.1 y el enlace `Detalle: [.claude/skills/README.md](.claude/skills/README.md)`.

### 5. Informe

5.1. DEBE compararse con el `README.md` anterior (si existía) e informar de skills añadidas, eliminadas y reclasificadas, con el recuento por lado y categoría. Si no hay diferencias, el informe dice "sin cambios".

5.2. NO DEBE ejecutarse Git ni hacer commit.

## Comprobaciones antes de finalizar

- [ ] Todas las carpetas de `.claude/skills/` con `SKILL.md` aparecen una sola vez en el mapa.
- [ ] Cada ruta del mapa existe.
- [ ] Ninguna skill se ha movido, renombrado ni editado (`git status` solo muestra `README.md`, `AGENTS.md` y, si es nueva, esta skill).
- [ ] Ejecutar la skill dos veces seguidas sin cambios produce el mismo `README.md`.
