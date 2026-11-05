# Perfil de tipo: `e2e`

Alcance de los tests extremo a extremo en Facturación: la aplicación completa arrancada, peticiones HTTP reales y base de datos real con las migraciones Flyway. Las normas de escritura son de `/k-back-testing` secciones 2, 3.7, 6 y 8. No se repiten aquí.

## Qué abarca

- Flujos de negocio críticos por HTTP real, de principio a fin: crear una factura y recuperarla, listarla, eliminarla y comprobar que deja de existir; rechazo de un número duplicado contra la restricción real de la base de datos; errores con su `ProblemDetail` atravesando toda la pila.
- Cableado completo de Spring (módulos, facade, handlers), transacciones reales y migraciones Flyway aplicadas sobre MariaDB con `ddl-auto: validate`.

Herramientas: `@SpringBootTest(webEnvironment = RANDOM_PORT)` con un cliente HTTP real del stack existente (`TestRestTemplate` o `RestClient`) y MariaDB en contenedor.

## Criterios propios del tipo

- Pocos tests y centrados en flujos (`/k-back-testing` sección 3.7): NO DEBEN replicar casos ya cubiertos por `unit` o `api`; el analizador DEBE listar esos casos para evitarlo.
- Cada test DEBE dejar la base de datos en un estado que no afecte a los demás (datos propios con números únicos o limpieza explícita), sin depender del orden de ejecución.
- Las aserciones DEBEN comprobar el efecto observable por HTTP (códigos, cuerpo, `Location`) y, cuando el flujo lo exija, el estado persistido leído de nuevo por la API.

## Infraestructura y puerta de decisión

Este tipo SIEMPRE necesita infraestructura que el proyecto no tiene (`/k-back-testing` secciones 1.1 y 1.3): Testcontainers MariaDB y Docker. El `test-analyzer` DEBE comprobar si Docker está disponible (`docker info`), y el coordinador DEBE preguntar al usuario (procedimiento sección 3.3) antes de CREATE:

1. Autorizar `spring-boot-testcontainers` y el módulo MariaDB de Testcontainers con `@ServiceConnection`, aceptando que la suite pase a requerir Docker.
2. Usar la MariaDB de `compose.yaml` levantada por el usuario, lo que deja los tests fuera de la ejecución normal y exige decidir cómo se ejecutan.
3. Cancelar.

Sin una de las dos primeras autorizaciones, NO DEBE crearse ningún test e2e. H2, `assumeTrue` y desactivaciones NO son alternativas. Los tests DEBEN llamarse `*Test` mientras no exista Failsafe (`/k-back-testing` sección 2.2); añadir Failsafe es otra decisión que también requiere autorización.

## Mutaciones orientativas para el falsador

Quitar `@Transactional` de un caso de uso que guarda varias cosas; eliminar la restricción `UNIQUE` de la migración en el worktree desechable; cambiar el mapeo de una columna; romper el cableado de un handler; devolver `200` en lugar de `201`.
