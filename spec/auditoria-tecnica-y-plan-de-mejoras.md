# Auditoría técnica y plan de mejoras

## Propósito

Este documento presenta una revisión profunda del repositorio `users-management-spring-boot-hexagonal-ddd`, tomando como referencia los objetivos pedagógicos descritos en `spec/Contexto-resumido-de-la-conversación.md`.

La auditoría analiza:

- seguridad;
- arquitectura;
- escalabilidad;
- rendimiento;
- resiliencia;
- consistencia y concurrencia;
- diseño de la API REST;
- persistencia;
- testing y calidad;
- observabilidad y operación;
- entrega y despliegue;
- preparación del repositorio para las actividades prácticas.

La revisión se realizó sin modificar el código fuente.

## Resumen ejecutivo

El repositorio es una buena base pedagógica para provocar problemas reales de ingeniería de software, pero todavía no está preparado como una línea base cercana a producción.

Su principal fortaleza es que contiene suficientes debilidades auténticas para construir los talleres propuestos. Su principal riesgo es que algunas actividades se apoyarían sobre una base inconsistente y podrían confundir fallos accidentales del repositorio con el concepto que se desea enseñar.

Estado comprobado durante la auditoría:

- El proyecto compila y genera un JAR ejecutable.
- Las 193 pruebas existentes pasan.
- La cobertura aproximada es:
  - líneas: 558 de 764, equivalente a 73,0 %;
  - ramas: 61 de 86, equivalente a 70,9 %;
  - instrucciones: 2362 de 3379, equivalente a 69,9 %.
- No existen pruebas reales de integración, seguridad, concurrencia, carga o arranque completo.
- No existen Docker, Docker Compose, CI/CD, migraciones versionadas ni observabilidad.
- La API REST se encuentra completamente desprotegida.
- Existen divergencias importantes entre la documentación y la implementación actual.

## Hallazgos críticos

### 1. API completamente abierta

Todos los endpoints de `UserRestController` pueden utilizarse sin autenticación ni autorización.

Actualmente, un cliente anónimo puede:

- crear usuarios administradores;
- modificar el rol de cualquier usuario;
- activar, bloquear o desactivar cuentas;
- consultar todos los usuarios;
- consultar usuarios individuales;
- cambiar contraseñas;
- eliminar cualquier usuario.

Archivo relacionado:

`src/main/java/com/jcaa/usersmanagement/infrastructure/entrypoint/rest/controller/UserRestController.java`

No existe:

- `spring-boot-starter-security`;
- filtro JWT;
- sesión autenticada;
- integración OIDC;
- RBAC;
- comprobación de propiedad del recurso;
- rate limiting;
- auditoría de acciones sensibles.

Además, `UpdateUserRestRequest` permite que el cliente envíe directamente los campos `role` y `status`. Esto permite que un usuario se asigne el rol `ADMIN` o establezca su cuenta como `ACTIVE`.

Archivo relacionado:

`src/main/java/com/jcaa/usersmanagement/infrastructure/entrypoint/rest/dto/request/UpdateUserRestRequest.java`

Este problema corresponde a categorías como Broken Access Control y BOLA/IDOR.

**Prioridad:** bloqueante antes de cualquier despliegue público.

### 2. Envío de contraseñas en texto plano por correo

Después de crear un usuario, el sistema inserta la contraseña original en una plantilla HTML y la envía por SMTP.

Archivo relacionado:

`src/main/java/com/jcaa/usersmanagement/application/service/EmailNotificationService.java`

Esto expone la contraseña en:

- el proveedor SMTP;
- la bandeja de entrada del usuario;
- respaldos del correo;
- dispositivos comprometidos;
- reenvíos;
- herramientas de soporte y auditoría.

La solución recomendada es implementar un flujo de activación o establecimiento de contraseña mediante un token aleatorio, de un solo uso y con expiración. La contraseña nunca debe enviarse por correo.

### 3. Escrituras y correo no son atómicos

La creación de un usuario realiza, como mínimo, las siguientes operaciones:

1. consulta por correo;
2. inserción del usuario;
3. consulta por ID;
4. envío SMTP síncrono.

No existe una transacción que defina los límites de consistencia.

Archivo relacionado:

`src/main/java/com/jcaa/usersmanagement/application/service/CreateUserService.java`

Si el usuario se guarda y luego falla SMTP:

- la API devuelve un error;
- el usuario ya quedó almacenado;
- un reintento puede devolver conflicto por correo duplicado;
- el cliente no puede determinar con certeza si la operación ocurrió.

Este comportamiento es un excelente caso para enseñar:

- consistencia;
- idempotencia;
- eventos;
- procesamiento asíncrono;
- Transactional Outbox.

Sin embargo, debe documentarse como un problema deliberado en la rama `starter` del taller correspondiente.

### 4. SMTP puede agotar los hilos HTTP

El envío de correo se ejecuta dentro del hilo que atiende la petición HTTP.

Archivo relacionado:

`src/main/java/com/jcaa/usersmanagement/infrastructure/adapter/email/JavaMailEmailSenderAdapter.java`

La configuración no establece:

- timeout de conexión;
- timeout de lectura;
- timeout de escritura;
- circuit breaker;
- bulkhead;
- retry controlado;
- cola asíncrona.

Si el servidor SMTP responde lentamente, los hilos de Tomcat pueden quedar bloqueados hasta degradar toda la API.

También se utiliza `javax.mail` 1.6.2, una dependencia heredada, mientras el resto del proyecto utiliza Spring Boot 3 y APIs Jakarta.

### 5. Condiciones de carrera en la unicidad de correos

La creación comprueba primero si el correo existe y después intenta insertar el usuario.

Dos solicitudes concurrentes pueden observar simultáneamente que el correo no existe y luego intentar insertar el mismo correo.

La restricción `UNIQUE` de MySQL protege la integridad final, pero la solicitud perdedora termina como `PersistenceException` y probablemente devuelve HTTP 500 en lugar de HTTP 409.

El mismo patrón existe en la actualización de usuarios.

Archivo relacionado:

`src/main/java/com/jcaa/usersmanagement/application/service/UpdateUserService.java`

Este comportamiento puede emplearse como laboratorio de concurrencia, siempre que se acompañe de una prueba concurrente reproducible.

## Seguridad

### Problemas de alta prioridad

- No hay autenticación.
- No hay autorización.
- No existe endpoint REST de login, aunque existe `LoginService`.
- No hay protección contra fuerza bruta.
- No hay protección frente a credential stuffing.
- No hay rate limiting.
- No existe una política CORS explícita.
- Swagger/OpenAPI queda expuesto públicamente.
- Los IDs son suministrados por el cliente.
- Los roles son controlados por el cliente.
- Los estados de cuenta son controlados por el cliente.
- No hay auditoría de cambios de rol, estado o contraseña.
- No hay revocación de sesiones o tokens.
- La contraseña solo exige ocho caracteres.
- No hay detección de contraseñas comprometidas.
- BCrypt se ejecuta en el hilo HTTP sin límites de concurrencia.

### Filtración de información y enumeración de cuentas

Algunas excepciones incluyen correos electrónicos en sus mensajes.

Archivo relacionado:

`src/main/java/com/jcaa/usersmanagement/domain/exception/UserAlreadyExistsException.java`

El manejador global devuelve directamente varios mensajes del dominio al cliente.

Archivo relacionado:

`src/main/java/com/jcaa/usersmanagement/infrastructure/entrypoint/rest/advice/GlobalExceptionHandler.java`

Esto puede facilitar:

- enumeración de cuentas;
- filtración de PII;
- exposición de detalles internos;
- incorporación accidental de información sensible en logs.

El diseño contradice parcialmente la regla declarada en `AGENTS.md` que prohíbe incluir PII en logs.

### Inyección de HTML en correos

El supuesto motor de plantillas utiliza reemplazos directos mediante `String.replace` y no escapa los valores introducidos por el usuario.

Un nombre con contenido HTML se incorporaría directamente al cuerpo del correo.

Aunque muchos clientes de correo aplican filtros de seguridad, sigue siendo contenido HTML no confiable y puede utilizarse para phishing, alteración visual o carga de recursos remotos.

### Configuración de secretos

`application.properties` contiene valores de ejemplo y no secretos reales, lo cual es positivo. Sin embargo:

- no usa variables de entorno de forma explícita;
- no hay perfiles separados para `local`, `test` y `prod`;
- la contraseña de MySQL está vacía por defecto;
- se utiliza `root` como usuario por defecto;
- no hay validación temprana de placeholders SMTP;
- no existe `.env.example`;
- no hay escaneo automático de secretos.

### Usuario administrador inicial defectuoso

`schema.sql` anuncia la contraseña `Admin1234!`, pero contiene un hash marcador que no representa un hash BCrypt válido.

Archivo relacionado:

`src/main/resources/schema.sql`

Esto genera una cuenta engañosa o inutilizable. Además, publicar una contraseña administrativa conocida sería inseguro fuera de un entorno pedagógico completamente aislado.

## Escalabilidad y rendimiento

### Listado sin paginación

`GET /api/users` recupera todos los usuarios y ordena la tabla completa por nombre.

Archivo relacionado:

`src/main/java/com/jcaa/usersmanagement/infrastructure/adapter/persistence/repository/UserRepositoryMySQL.java`

Consecuencias:

- uso de memoria proporcional al total de usuarios;
- respuestas HTTP potencialmente enormes;
- mayor tiempo de serialización;
- ordenamiento global en la base de datos;
- ausencia de límites de seguridad;
- imposibilidad de navegación eficiente por cursor o página.

Se recomienda introducir paginación desde la primera versión profesional de la API.

### Carga innecesaria de hashes de contraseña

Las consultas de lectura seleccionan siempre la columna `password`, incluso cuando se listan usuarios para una respuesta pública.

Esto:

- mueve datos sensibles innecesariamente;
- aumenta el impacto de volcados de memoria;
- acopla las lecturas públicas al agregado completo;
- impide optimizar las consultas mediante proyecciones;
- consume ancho de banda entre aplicación y base de datos.

Conviene separar:

- proyecciones de lectura pública;
- datos de administración;
- credenciales necesarias exclusivamente para autenticación.

### Consultas adicionales después de cada escritura

Los métodos `save()` y `update()` ejecutan la escritura y luego realizan un `SELECT` adicional por ID.

Esto aumenta:

- la latencia;
- el uso del pool de conexiones;
- la cantidad de viajes a la base de datos;
- la ventana para condiciones de carrera.

### Pool de conexiones rígido

La configuración de Hikari fija:

- máximo de conexiones: 10;
- conexiones mínimas inactivas: 2;
- timeout de conexión: 30 segundos.

Archivo relacionado:

`src/main/java/com/jcaa/usersmanagement/infrastructure/config/DataSourceSpringConfig.java`

Estos valores no son configurables por ambiente.

También faltan decisiones explícitas sobre:

- `maxLifetime`;
- `idleTimeout`;
- `validationTimeout`;
- detección temporal de fugas;
- métricas del pool;
- coordinación con el límite de conexiones de MySQL.

### BCrypt bajo carga

BCrypt utiliza coste 12, lo cual es razonable desde seguridad, pero cada creación, cambio de contraseña y login consume CPU intencionalmente.

No existen:

- límites de concurrencia;
- mediciones de duración del hash;
- protección contra abuso;
- pruebas de capacidad;
- rate limiting para login.

Este comportamiento es apropiado para una práctica de carga con k6 o JMeter.

## Resiliencia y consistencia

El repositorio no dispone de los siguientes mecanismos:

- timeouts de consulta a base de datos;
- cancelación de consultas lentas;
- retry diferenciado por tipo de error;
- circuit breaker para SMTP;
- bulkhead;
- procesamiento asíncrono;
- idempotency keys;
- optimistic locking;
- control de versión de agregados;
- Transactional Outbox;
- dead-letter queue;
- deduplicación de mensajes;
- health probes;
- readiness probes;
- estrategia documentada de apagado ordenado;
- degradación funcional cuando SMTP falla.

Los métodos `UPDATE` y `DELETE` tampoco verifican el número de filas afectadas. En condiciones concurrentes, el servicio podría reportar éxito aunque el registro haya desaparecido antes de la escritura.

## Arquitectura

### Fortalezas

- Separación reconocible entre dominio, aplicación e infraestructura.
- Puertos de entrada y salida explícitos.
- JDBC encapsulado en un adaptador.
- Uso de `PreparedStatement`, sin SQL injection directa identificada.
- Value Objects con validaciones.
- Mappers por frontera.
- Contraseñas almacenadas mediante BCrypt.
- Excepciones de dominio específicas.
- Inyección por constructor.
- El dominio no depende directamente de infraestructura.

### Documentación arquitectónica desactualizada

`AGENTS.md` afirma que:

- no existe una capa REST activa;
- Spring DI no se usa;
- `Main` construye manualmente `DependencyContainer`;
- `entrypoint/rest` se encuentra vacío.

Estas afirmaciones no corresponden a la implementación actual.

`Main` ejecuta `SpringApplication.run`, Spring administra los beans y existe un controlador REST activo.

Archivo relacionado:

`src/main/java/com/jcaa/usersmanagement/Main.java`

Esta divergencia es especialmente peligrosa para:

- estudiantes;
- docentes;
- colaboradores;
- agentes de IA;
- futuras actividades prácticas.

### Dos composition roots

Existen simultáneamente:

- configuración real mediante Spring;
- `DependencyContainer`, que construye manualmente la antigua aplicación CLI.

Archivo relacionado:

`src/main/java/com/jcaa/usersmanagement/infrastructure/config/DependencyContainer.java`

Esto duplica la configuración de:

- DataSource;
- SMTP;
- servicios;
- validación;
- controladores.

El código duplicado puede divergir y provocar que la CLI y REST tengan comportamientos diferentes.

Se recomienda elegir un único composition root o separar explícitamente ambas aplicaciones en módulos distintos.

### Acoplamiento de application a frameworks

La capa `application` utiliza:

- `@Service` de Spring;
- Lombok;
- Jakarta Bean Validation;
- `ConstraintViolationException`.

Esto no invalida automáticamente una arquitectura hexagonal, pero contradice una interpretación estricta de independencia del framework.

Debe tomarse y documentarse una decisión explícita:

- mantener `application` framework-agnostic; o
- aceptar Spring y Jakarta en `application` como una decisión pragmática.

### Domain Events decorativos

Existen:

- `UserCreatedDomainEvent`;
- `UserUpdatedDomainEvent`;
- `UserDeletedDomainEvent`.

Sin embargo, estos eventos no se crean ni publican desde los casos de uso. Actualmente son objetos aislados cubiertos por pruebas, no un mecanismo funcional.

Además, algunos payloads contienen nombre y correo, incrementando el riesgo de propagación de PII a brokers, logs o sistemas externos.

### Agregado con pocas reglas

`UserModel` permite construir públicamente combinaciones arbitrarias de rol y estado.

Sus métodos `activate()` y `deactivate()`:

- no validan transiciones;
- no comprueban el estado anterior;
- no emiten eventos;
- no registran la razón del cambio.

Las reglas relevantes permanecen principalmente en servicios y mappers.

### Modelo temporal débil

La base de datos almacena `created_at` y `updated_at`, pero el dominio y la API no los utilizan.

Persistencia los representa como `String`, perdiendo:

- semántica temporal;
- zona horaria;
- capacidad de comparación segura;
- claridad del contrato.

Se recomienda utilizar tipos como `Instant`, `OffsetDateTime` o `LocalDateTime`, según la semántica elegida.

## Diseño de la API REST

La API funciona como CRUD básico, pero aún no es una API profesional.

Problemas identificados:

- no está versionada;
- `POST` permite que el cliente elija el ID;
- no devuelve encabezado `Location`;
- no hay paginación;
- no hay filtros;
- no hay metadatos de navegación;
- no hay ETags;
- no hay precondiciones para actualización;
- `PUT` funciona como reemplazo, pero la contraseña es opcional;
- no hay control explícito de `Content-Type`;
- los errores no incluyen `timestamp`;
- los errores no incluyen `path`;
- los errores no incluyen un código estable;
- los errores no incluyen `correlationId`;
- los errores de validación no se representan como una lista estructurada;
- se mezclan mensajes en inglés y español;
- no se utiliza `ProblemDetail` ni RFC 9457;
- las path variables no tienen validación formal;
- no existen longitudes máximas en los DTO;
- las reglas de los DTO no coinciden completamente con el esquema;
- roles y estados se reciben como `String`;
- no existe un contrato explícito de compatibilidad;
- no hay pruebas del controlador REST;
- no hay pruebas del manejador global de errores.

Como primera mejora puede considerarse `/api/v1/users`, aunque la estrategia de versionado debe discutirse dentro del taller y no imponerse sin explicar sus implicaciones.

## Base de datos

Problemas identificados:

- `schema.sql` no es una migración versionada;
- no se utiliza Flyway o Liquibase;
- el script intenta crear la base y ejecutar `USE`;
- los `ENUM` de MySQL acoplan el modelo al motor;
- no hay índices para futuros filtros por nombre, rol o estado;
- no existe columna de versión para optimistic locking;
- no hay soft delete;
- no hay auditoría;
- no hay estrategia de respaldo y restauración;
- no hay usuario de mínimo privilegio;
- el JDBC URL no declara opciones de SSL o zona horaria;
- no existen pruebas con MySQL real o Testcontainers;
- las restricciones de longitud están parcialmente duplicadas;
- no hay una estrategia de evolución del esquema.

### Validación insuficiente del ID

`UserId` acepta cualquier texto no vacío, pero la columna correspondiente solo permite 36 caracteres.

Archivo relacionado:

`src/main/java/com/jcaa/usersmanagement/domain/valueobject/UserId.java`

Un ID demasiado largo o con formato inesperado puede superar la capa de dominio y terminar como un error de persistencia HTTP 500.

Si el sistema utiliza UUID, el dominio debería representar el ID mediante `UUID` o validar estrictamente su formato y longitud.

## Testing y calidad

### Aspectos positivos

- Las 193 pruebas existentes pasan.
- Existe buena cobertura en Value Objects, servicios y mappers.
- Se utiliza JUnit 5.
- Se utiliza Mockito.
- Se utiliza AssertJ.
- Los tests son rápidos.
- El proyecto genera un JAR ejecutable.
- Se aplican pruebas a varias ramas de validación del dominio.

### Vacíos de pruebas

No existen pruebas de:

- arranque de Spring;
- carga completa del ApplicationContext;
- controlador REST;
- serialización JSON;
- manejo global de errores;
- autenticación;
- autorización;
- base de datos real;
- restricciones reales de MySQL;
- migraciones;
- SMTP real o Mailpit;
- transacciones;
- concurrencia;
- idempotencia;
- timeouts;
- circuit breakers;
- Docker;
- rendimiento;
- carga;
- arquitectura mediante ArchUnit.

`UserRepositoryMySQLTest` simula JDBC. Por tanto, demuestra comportamiento frente a mocks, pero no demuestra que el SQL funcione correctamente contra una instancia real de MySQL.

### Cobertura engañosa

Aunque la cobertura global alcanza aproximadamente 73 % de líneas, tienen 0 % de cobertura componentes relevantes como:

- `UserRestController`;
- `GlobalExceptionHandler`;
- `UserRestMapper`;
- `DataSourceSpringConfig`;
- `SmtpSpringConfig`;
- varios componentes de la CLI.

JaCoCo tampoco define una regla mínima que haga fallar el build cuando la cobertura disminuye.

### Compatibilidad de herramientas

La verificación se ejecutó con JDK 23 y terminó con `BUILD SUCCESS`, pero JaCoCo 0.8.11 produjo numerosos errores de instrumentación porque no reconoce el bytecode major version 67.

El proyecto declara Java 17. Se recomienda:

- ejecutar obligatoriamente con JDK 17 en CI; o
- actualizar JaCoCo si se desea admitir JDK más nuevos;
- incorporar Maven Enforcer o Maven Toolchains;
- documentar claramente la versión soportada.

## Operación y entrega

No se encontraron los siguientes componentes:

- `Dockerfile`;
- `compose.yaml`;
- `.dockerignore`;
- MySQL local orquestado;
- Mailpit local;
- healthchecks de contenedores;
- ejecución con usuario no root;
- imagen Docker multi-stage;
- configuración completa por variables de entorno;
- pipeline de GitHub Actions;
- análisis estático;
- escaneo de dependencias;
- escaneo de secretos;
- SBOM;
- Spring Boot Actuator;
- métricas Prometheus;
- logs estructurados;
- correlation ID;
- OpenTelemetry;
- trazas distribuidas;
- dashboards;
- alertas;
- scripts de carga;
- estrategia de despliegue;
- estrategia de rollback.

El archivo `README.md` contiene muy poca información, presenta errores tipográficos y no permite levantar el sistema de forma reproducible.

## Dependencias y mantenimiento

El proyecto fija, entre otras, las siguientes versiones:

- Spring Boot 3.3.5;
- Springdoc 2.6.0;
- Swagger annotations 2.2.22;
- JaCoCo 0.8.11;
- JavaMail `javax.mail` 1.6.2.

No existe:

- Renovate;
- Dependabot;
- OWASP Dependency-Check;
- Trivy;
- política de actualización;
- escaneo automático de vulnerabilidades.

No deben afirmarse vulnerabilidades concretas únicamente a partir de la antigüedad de una versión. Es necesario ejecutar un escáner sobre el árbol de dependencias resuelto y revisar los resultados para evitar falsos positivos.

También existen dependencias o configuraciones potencialmente redundantes:

- `swagger-annotations-jakarta` llega transitivamente mediante Springdoc;
- Mockito tiene versión explícita pese a estar administrado por el BOM;
- se incluye `spring-boot-starter-jdbc`, pero se excluye la autoconfiguración del DataSource;
- existe `DatabaseConnectionFactory` basado en `DriverManager`, aunque el runtime real utiliza Hikari;
- Lombok atraviesa dominio, aplicación e infraestructura.

## Mejoras priorizadas

### Fase 0: estabilizar la línea base

Antes de comenzar los talleres principales se recomienda:

1. Actualizar `AGENTS.md`.
2. Crear un `README.md` reproducible.
3. Elegir Spring DI como único composition root o separar formalmente CLI y REST.
4. Retirar o aislar `DependencyContainer`.
5. Definir JDK 17 mediante Maven Enforcer o Toolchains.
6. Actualizar o ajustar JaCoCo.
7. Agregar umbrales de cobertura.
8. Introducir Flyway.
9. Configurar perfiles y variables de entorno.
10. Añadir Docker Compose con MySQL y Mailpit.
11. Crear una prueba mínima de arranque.
12. Crear pruebas de integración con Testcontainers.
13. Ejecutar un escaneo real de dependencias.

### Fase 1: REST profesional y errores

- Versionar el contrato de API.
- Generar IDs en el servidor.
- Incorporar paginación.
- Introducir `ProblemDetail`.
- Definir códigos estables de error.
- Alinear validaciones con el esquema.
- Agregar longitudes máximas.
- Crear pruebas con MockMvc.
- Verificar el contrato OpenAPI.
- Evitar PII innecesaria en respuestas.

### Fase 2: seguridad

- Incorporar Spring Security.
- Implementar OIDC con Keycloak local o JWT con diseño explícito.
- Aplicar RBAC.
- Aplicar autorización por propiedad del recurso.
- Separar endpoints administrativos y de perfil.
- Eliminar contraseñas de correos.
- Incorporar rate limiting.
- Definir CORS.
- Registrar auditoría.
- Proteger Swagger según ambiente.
- Añadir pruebas negativas de autorización.

### Fase 3: datos y concurrencia

- Definir transacciones.
- Traducir restricciones únicas a HTTP 409.
- Introducir optimistic locking.
- Comprobar filas afectadas.
- Escribir pruebas concurrentes.
- Probar con MySQL real mediante Testcontainers.
- Incorporar idempotency keys donde corresponda.

### Fase 4: resiliencia y asincronía

- Configurar timeouts explícitos.
- Introducir circuit breaker.
- Introducir bulkhead.
- Usar Mailpit y WireMock.
- Extraer el correo del camino crítico.
- Incorporar una cola local.
- Implementar reintentos con backoff.
- Incorporar una DLQ.
- Diseñar consumidores idempotentes.
- Implementar Transactional Outbox.

### Fase 5: operación y observabilidad

- Incorporar Actuator.
- Crear readiness y liveness probes.
- Exponer métricas Prometheus.
- Incorporar logs JSON.
- Propagar correlation ID.
- Incorporar OpenTelemetry y Jaeger.
- Crear pruebas de carga con k6.
- Crear pipeline de CI.
- Añadir SAST, análisis de dependencias y escaneo de secretos.
- Crear una imagen Docker segura.
- Preparar despliegue gratuito opcional.
- Diseñar y demostrar rollback.

## Aprovechamiento pedagógico

No se recomienda corregir todos los problemas directamente en `main` antes de los talleres.

El repositorio puede funcionar mejor con una línea base estable y ramas en las que cada debilidad sea deliberada.

Cada taller debería contar con:

- una rama o estado `starter` con un fallo reproducible;
- una rama o estado `solution` con la solución;
- una prueba que demuestre el problema;
- una prueba que demuestre la solución.

Cada actividad debería definir:

1. hipótesis inicial;
2. pregunta disparadora;
3. mecanismo para provocar el fallo;
4. evidencia observable;
5. explicación conceptual;
6. cambio mínimo guiado;
7. prueba automatizada;
8. criterio de aceptación;
9. reto adicional;
10. aplicación al proyecto de grado;
11. evidencia y sustentación.

## Experimentos que ya permite el código

### Experimento 1: correo duplicado concurrente

Ejecutar dos solicitudes simultáneas para crear usuarios con el mismo correo.

Objetivos:

- observar la condición de carrera;
- diferenciar validación previa y restricción de base de datos;
- traducir correctamente el error a HTTP 409;
- introducir una prueba concurrente.

### Experimento 2: SMTP caído después de guardar

Detener SMTP durante la creación de un usuario.

Objetivos:

- comprobar que el usuario queda almacenado;
- observar que la API devuelve error;
- analizar la ambigüedad para el cliente;
- introducir asincronía, idempotencia u Outbox.

### Experimento 3: listado masivo

Insertar una cantidad grande de usuarios y llamar `GET /api/users`.

Objetivos:

- medir latencia;
- observar memoria y tamaño de respuesta;
- incorporar paginación;
- comparar offset y cursor.

### Experimento 4: escalamiento de privilegios

Enviar una actualización con `role=ADMIN` desde un cliente anónimo.

Objetivos:

- demostrar Broken Access Control;
- separar autenticación y autorización;
- introducir RBAC;
- crear pruebas negativas.

### Experimento 5: SMTP lento

Configurar un servidor SMTP o simulador que no responda.

Objetivos:

- observar hilos HTTP bloqueados;
- medir saturación;
- introducir timeouts;
- aplicar circuit breaker y bulkhead.

### Experimento 6: eliminación concurrente

Eliminar el usuario entre la consulta de existencia y el `DELETE`.

Objetivos:

- comprobar que no se verifican filas afectadas;
- discutir consistencia;
- simplificar operaciones redundantes;
- analizar transacciones y locking.

### Experimento 7: respuesta perdida

Procesar un `POST`, perder la respuesta y repetir la solicitud.

Objetivos:

- comprender at-least-once desde la perspectiva del cliente;
- diseñar idempotency keys;
- almacenar y reutilizar resultados;
- demostrar la solución mediante pruebas.

### Experimento 8: eventos que no se publican

Crear, actualizar y eliminar usuarios, y comprobar que los Domain Events existentes nunca son publicados.

Objetivos:

- diferenciar declarar un evento y operar con eventos;
- introducir un puerto de publicación;
- analizar entrega confiable;
- preparar el taller de Outbox.

## Ruta recomendada para iniciar las actividades

La secuencia recomendada es:

1. saneamiento y documentación de la línea base;
2. Docker local con MySQL y Mailpit;
3. migraciones y pruebas de integración;
4. arquitectura existente y decisiones explícitas;
5. API REST profesional;
6. validación y manejo de errores;
7. autenticación y autorización;
8. seguridad ofensiva controlada;
9. testing y regresión;
10. CI/CD;
11. base de datos bajo presión;
12. concurrencia;
13. idempotencia;
14. caché y rendimiento;
15. resiliencia;
16. procesamiento asíncrono;
17. mensajería confiable;
18. Transactional Outbox;
19. observabilidad;
20. carga, despliegue y rollback.

## Veredicto

El repositorio sí es adecuado como repositorio maestro educativo porque sus problemas coinciden con los objetivos definidos en `Contexto-resumido-de-la-conversación.md`.

Sin embargo, antes de iniciar las prácticas principales debe crearse una fase 0 de saneamiento para evitar que problemas accidentales de configuración, documentación o reproducibilidad interfieran con los objetivos de aprendizaje.

La prioridad inmediata debería ser:

1. corregir documentación y reproducibilidad;
2. crear Docker Compose local con MySQL y Mailpit;
3. incorporar migraciones y pruebas de integración;
4. establecer una API REST profesional y un contrato de errores;
5. introducir seguridad antes de cualquier despliegue público;
6. conservar deliberadamente los problemas de concurrencia, idempotencia, resiliencia, mensajería y observabilidad para los talleres correspondientes.

La meta no debe ser ocultar las debilidades del repositorio, sino convertir cada una en un problema pedagógico reproducible, medible y acompañado de una solución verificable.
