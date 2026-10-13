# =============================================================================
# Etapa 1: Build y empaquetado del artefacto con Maven
# =============================================================================
FROM maven:3.9.11-eclipse-temurin-17 AS build

WORKDIR /workspace

# Caché de capas de dependencias en Docker
COPY pom.xml ./
RUN mvn dependency:go-offline -B || true

# Copiar código fuente y empaquetar
COPY src ./src
RUN mvn --batch-mode --no-transfer-progress clean package -DskipTests

# =============================================================================
# Etapa 2: Imagen final de ejecución con JRE 17
# =============================================================================
FROM eclipse-temurin:17-jre

# Usuario sin privilegios por seguridad
RUN groupadd --system spring && useradd --system --gid spring spring
WORKDIR /app

COPY --from=build --chown=spring:spring /workspace/target/users-management-*.jar app.jar
USER spring:spring

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
