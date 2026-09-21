# Users Management — AI Audit & Script Generation Context

## 🤖 Contexto para la Inteligencia Artificial

**Este archivo está diseñado específicamente para proveer el contexto arquitectónico y de negocio a otra IA.** El objetivo principal es que puedas auditar este proyecto y generar guiones o material educativo para explicar cómo se ha implementado la Arquitectura Hexagonal y DDD en Spring Boot de manera pura.

---

## 🏗️ Arquitectura General

El proyecto sigue estrictamente la **Arquitectura Hexagonal (Ports and Adapters)** combinada con patrones de **Domain-Driven Design (DDD)**.

### Regla de Dependencias (Estricta)

`domain` ← `application` ← `infrastructure`

- **Ninguna** clase del dominio o aplicación puede importar clases, anotaciones o librerías de infraestructura (como Spring, Jackson, JDBC, etc.).
- Las dependencias hacia fuera (base de datos, email) se definen mediante **interfaces en Application (Ports)** y se implementan en **Infrastructure (Adapters)**.
- **Composition Root:** Spring Boot (`Main.java`) es el único encargado de inyectar dependencias y levantar el contexto. No existen contenedores manuales paralelos.

### Estructura de Paquetes

```text
src/main/java/com/jcaa/usersmanagement/
├── domain/          ← Java puro. Sin frameworks. Value Objects (records/final classes) y Entities.
├── application/
│   ├── port/in/     ← Interfaces de Casos de Uso (ej: CreateUserUseCase)
│   ├── port/out/    ← Interfaces de salida (ej: SaveUserPort)
│   ├── service/     ← Implementación de casos de uso (@Service, inyección por constructor)
│   └── dto/         ← Commands y Queries (records) con Bean Validation
└── infrastructure/
    ├── adapter/
    │   ├── email/       ← JavaMailEmailSenderAdapter (javax.mail)
    │   └── persistence/ ← UserRepositoryMySQL (Raw JDBC, sin ORM/JPA)
    ├── entrypoint/
    │   ├── rest/        ← API REST activa (Controladores Spring MVC)
    │   └── desktop/     ← CLI inactiva (Mantenida como ejemplo de otro adaptador de entrada)
    └── config/      ← @Configuration y @Bean wiring (DataSource, Smtp)
```

---

## 🛠️ Stack Tecnológico

- **Lenguaje:** Java 17
- **Framework Core:** Spring Boot 3.3.5
- **Persistencia:** MySQL vía **Raw JDBC** (HikariCP), no se usa Spring Data JPA ni Hibernate.
- **Email:** `javax.mail` 1.6.2 (Mantenido intencionalmente, sin migrar a jakarta.mail).
- **Seguridad:** Hashing de contraseñas con `at.favre.lib:bcrypt` (factor de coste 12).
- **Documentación API:** SpringDoc OpenAPI (Swagger UI).
- **Testing:** JUnit 5, Mockito, AssertJ, JaCoCo.

---

## 📜 Reglas de Codificación (Non-negotiable)

Al auditar o explicar este código, presta extrema atención a los siguientes patrones, ya que son reglas invariables del repositorio:

1. **Inmutabilidad y Estructuras:**
   - DTOs, Commands y Queries DEBEN ser `record`.
   - **Value Objects:** `record` o `final class`. Deben validarse al momento de la construcción (en un compact constructor o factory method). **Nunca** deben aceptar un estado inválido.
2. **Excepciones de Dominio:**
   - Todas heredan de `DomainException` (que a su vez hereda de `RuntimeException`).
   - Obligatorio usar _Static Factory Methods_ con semántica clara. Ej: `UserNotFoundException.becauseIdWasNotFound(id)`.
3. **Mapeo entre Capas:**
   - Se utiliza un mapeador por frontera de capa (ej: `UserApplicationMapper`, `UserPersistenceMapper`).
   - Deben ser clases anotadas con `@UtilityClass` (Lombok) conteniendo solo métodos estáticos.
4. **Control de Nulos:**
   - **Prohibido retornar `null`.** Utilizar `Optional<T>`, colecciones vacías, o lanzar excepciones.
   - Usar `Objects.isNull()` y `Objects.nonNull()` para objetos. El operador `==` / `!=` queda relegado estrictamente a primitivos y enums.
5. **Privacidad de la Información (Logs):**
   - **Nunca** loguear PII (Personal Identifiable Information) como emails, contraseñas, nombres o datos puros del usuario. Loguear únicamente identificadores técnicos (UUIDs).
6. **Clean Code:**
   - No usar importaciones con `*`.
   - Cero imports no utilizados.
   - Sin magic strings (usar `private static final String`).

---

## 🧪 Convenciones de Testing

- El framework base es `spring-boot-starter-test` (JUnit 5 + Mockito).
- Las aserciones se hacen con **AssertJ**.
- Estructura estricta en cada test mediante comentarios: `// Arrange`, `// Act`, `// Assert`.
- Se agrupan aserciones con `assertAll(...)`.
- Excepciones verificadas mediante `assertThrows` (nunca bloques try/catch con `fail()`).
- Los servicios de aplicación que validan reglas de DTOs reciben un `Validator` construido desde `Validation.buildDefaultValidatorFactory()` en el `@BeforeEach` (`setUp()`).
- **Antipatrón evitado:** No se hacen tests de records puros, DTOs sin lógica o getters/setters triviales.

---

## 🚀 Flujos de Trabajo Comunes

### Ejecución de Pruebas y Cobertura

```bash
./mvnw clean install
./mvnw verify   # Genera reporte de JaCoCo en target/site/jacoco/index.html
```

### Ejecución Local

Requiere configuración de MySQL y SMTP en `src/main/resources/application.properties`.

```bash
./mvnw spring-boot:run
```

---

**AI Audit Note:** Si vas a generar un guion de video para este código, enfócate fuertemente en cómo se mantiene aislado el dominio (sin dependencias externas), cómo el Value Object `UserPassword` gestiona su propio cifrado al instanciarse (vía texto plano) o al recuperarse de BD (vía hash), y cómo la persistencia ignora los ORMs en favor del control absoluto con Raw JDBC.
