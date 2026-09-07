# Evidencia de la Línea Base (Fase 0)

Esta evidencia documenta el estado inicial del repositorio `users-management-spring-boot-hexagonal-ddd` antes de realizar cualquier corrección o modificación, tal como se especifica en los requisitos de la Fase 0.

## 1. Versiones Registradas

- **Java:** `openjdk version "17.0.20.1"` (Ejecutado forzando el `JAVA_HOME` a `/opt/homebrew/Cellar/openjdk@17/...`).
- **Maven:** `3.9.6` (Configurado en `.mvn/wrapper/maven-wrapper.properties`).

## 2. Ejecución del Build y Pruebas (`mvn clean verify`)

**Resultado:** `BUILD SUCCESS`

**Detalles:** 
Al utilizar la versión correcta de Java 17, el proyecto compila exitosamente y las pruebas se ejecutan sin problemas. Se generó correctamente el archivo de cobertura `jacoco.exec`.

## 3. Estado Inicial y Levantamiento de la Aplicación (`mvn spring-boot:run`)

**Resultado:** `BUILD FAILURE` (No se logró levantar el servidor completamente debido a error de base de datos)

**Detalles del Error:**
```
Caused by: org.springframework.beans.BeanInstantiationException: Failed to instantiate [javax.sql.DataSource]: Factory method 'dataSource' threw exception with message: Failed to initialize pool: Communications link failure
...
Caused by: java.net.ConnectException: Connection refused
```

> [!IMPORTANT]
> **Dependencias Externas y Pasos Manuales Necesarios identificados:**
> 1. **Base de Datos Local:** Como está indicado en el log de Spring Boot, la aplicación está intentando conectarse a una base de datos MySQL local, la cual actualmente está rechazando la conexión (`Connection refused`). Es necesario levantar una instancia de MySQL y configurar adecuadamente el archivo `src/main/resources/application.properties`.
> 2. **Servidor SMTP:** Aunque el error principal bloquea el arranque en el DataSource de la base de datos, según las reglas de `AGENTS.md` también hará falta configurar las credenciales SMTP.

## 4. Estado de Git (Últimos commits)

**Comando:** `git log --oneline --graph --all -n 15`

```
* aa4ed87 refactor: delete files necessaries
*   597b339 Merge pull request #12 from arrietajohn/refactor/spring-single-composition-root
|\  
| * af3d20e refactor(di): use Spring as the single composition root
|/  
*   93b2d47 Merge pull request #5 from arrietajohn/feature/spring-boot-rest-migration
|\  
| * 3aa3442 chore(pom): downgrade version to 2.1.0
| * bd7b000 feat(docs): add OpenAPI documentation for UserRestController
| * 43915a9 feat(docs): add OpenAPI documentation for UserRestController
* | d56768a Merge pull request #4 from arrietajohn/feature/spring-boot-rest-migration
|\| 
| * 8ca7ed6 chore(pom): bump version from 1.4 to 2.0
| * 2e23a11 chore(pom): bump version from 1.4 to 2.0
| * 5d9ee48 feat(entrypoint): add REST controller, mappers, and global exception handler for user management
|/  
*   4e34320 Merge pull request #3 from arrietajohn/chore/spring-boot-build-setup
|\  
| * ca2c14e chore(setup): add initial project documentation and configuration for database and SMTP
* | 99f71fe Merge pull request #2 from arrietajohn/chore/spring-boot-build-setup
|\| 
| * ac2e206 chore(setup): add Maven wrapper and configuration files
|/  
```

> **Nota Final:** Se ha omitido la creación de la rama `team-XX/baseline` por petición explícita. El estado documentado aquí refleja la condición del código base (main/actual) sin realizar ninguna modificación al código ni al entorno.
